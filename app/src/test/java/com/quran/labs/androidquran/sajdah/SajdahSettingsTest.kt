package com.quran.labs.androidquran.sajdah

import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.ListPreference
import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.google.common.truth.Truth.assertThat
import com.google.android.material.slider.Slider
import com.quran.labs.androidquran.QuranPreferenceActivity
import com.quran.labs.androidquran.R
import com.quran.labs.androidquran.base.TestApplication
import com.quran.labs.androidquran.ui.fragment.QuranSettingsFragment
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowDialog
import android.view.ViewGroup

@Config(application = TestApplication::class, sdk = [33])
@RunWith(RobolectricTestRunner::class)
class SajdahSettingsTest {
  @get:Rule val rule = ActivityScenarioRule(QuranPreferenceActivity::class.java)
  private fun fragment(activity: QuranPreferenceActivity) = activity.supportFragmentManager.findFragmentById(R.id.content) as QuranSettingsFragment
  private fun dialog() = ShadowDialog.getLatestDialog() as AlertDialog
  private fun slider(group: ViewGroup): Slider? {
    for (i in 0 until group.childCount) {
      val child=group.getChildAt(i)
      if (child is Slider) return child
      if (child is ViewGroup) slider(child)?.let { return it }
    }
    return null
  }
  private fun descendants(view: android.view.View): List<android.view.View> = listOf(view) +
    if (view is ViewGroup) (0 until view.childCount).flatMap { descendants(view.getChildAt(it)) } else emptyList()

  @Test fun extendedPipAppearanceSavesMarginsFullReferenceAndCornerAndResetRestoresDefaults() {
    rule.scenario.onActivity { activity ->
      val fragment=fragment(activity)
      fragment.findPreference<androidx.preference.SwitchPreferenceCompat>(com.quran.labs.androidquran.pip.TranslationPipController.PREFERENCE)!!.apply {
        isEnabled=true; isChecked=true
      }
      val preference=fragment.findPreference<Preference>(com.quran.labs.androidquran.pip.PipAppearance.SETTINGS)!!
      val appearance=com.quran.labs.androidquran.pip.PipAppearance(activity)
      preference.performClick()
      shadowOf(Looper.getMainLooper()).idle()
      val views=descendants(dialog().window!!.decorView)
      assertThat(views.filterIsInstance<Slider>()).hasSize(2)
      views.filterIsInstance<Slider>().last().value=0f
      views.filterIsInstance<com.google.android.material.materialswitch.MaterialSwitch>().single().isChecked=true
      views.filterIsInstance<com.google.android.material.textfield.MaterialAutoCompleteTextView>().last().apply {
        onItemClickListener!!.onItemClick(null,null,0,0)
      }
      dialog().getButton(AlertDialog.BUTTON_POSITIVE).performClick()
      shadowOf(Looper.getMainLooper()).idle()
      assertThat(appearance.marginDp).isEqualTo(0)
      assertThat(appearance.fullReference).isTrue()
      assertThat(appearance.corner).isEqualTo(com.quran.labs.androidquran.pip.PipCorner.TOP_LEFT)
      preference.performClick()
      shadowOf(Looper.getMainLooper()).idle()
      dialog().getButton(AlertDialog.BUTTON_NEUTRAL).performClick()
      dialog().getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
      assertThat(appearance.marginDp).isEqualTo(0)
      preference.performClick()
      shadowOf(Looper.getMainLooper()).idle()
      dialog().getButton(AlertDialog.BUTTON_NEUTRAL).performClick()
      dialog().getButton(AlertDialog.BUTTON_POSITIVE).performClick()
      shadowOf(Looper.getMainLooper()).idle()
      assertThat(appearance.marginDp).isEqualTo(8)
      assertThat(appearance.fullReference).isFalse()
      assertThat(appearance.corner).isEqualTo(com.quran.labs.androidquran.pip.PipCorner.BOTTOM_RIGHT)
    }
  }

  @Test fun choosingASoundPreviewsSixOptionsAndCancelDoesNotPersistTheDraft() {
    rule.scenario.onActivity { activity ->
      val preferences=SajdahPreferences(activity)
      fragment(activity).findPreference<Preference>(SajdahPreferences.SOUND)!!.performClick()
      val dialog=dialog()
      assertThat(dialog.listView.adapter.count).isEqualTo(6)
      dialog.listView.performItemClick(null,5,5)
      assertThat(preferences.sound).isEqualTo(SajdahSound.SOFT_BELLS)
      dialog.getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
      shadowOf(Looper.getMainLooper()).idle()
      assertThat(preferences.sound).isEqualTo(SajdahSound.SOFT_BELLS)
    }
  }
  @Test fun volumeAndCustomPauseDialogsSaveTheirDraftsAtTheRequestedLimits() {
    rule.scenario.onActivity { activity ->
      val fragment=fragment(activity)
      val preferences=SajdahPreferences(activity)
      fragment.findPreference<Preference>(SajdahPreferences.VOLUME)!!.performClick()
      assertThat(slider(dialog().window!!.decorView as ViewGroup)!!.value).isEqualTo(50f)
      slider(dialog().window!!.decorView as ViewGroup)!!.value=75f
      dialog().getButton(AlertDialog.BUTTON_POSITIVE).performClick()
      shadowOf(Looper.getMainLooper()).idle()
      assertThat(preferences.volume).isEqualTo(75)
      val pause=fragment.findPreference<ListPreference>(SajdahPause.MODE)!!
      assertThat(pause.value).isEqualTo("2")
      for (seconds in listOf(60,0)) {
        assertThat(pause.onPreferenceChangeListener!!.onPreferenceChange(pause,"custom")).isFalse()
        shadowOf(Looper.getMainLooper()).idle()
        slider(dialog().window!!.decorView as ViewGroup)!!.value=seconds.toFloat()
        dialog().getButton(AlertDialog.BUTTON_POSITIVE).performClick()
        shadowOf(Looper.getMainLooper()).idle()
        assertThat(SajdahPause.durationMillis(activity)).isEqualTo(seconds*1000L)
      }
    }
  }
  @Test fun pipAppearanceDialogKeepsDraftsUntilSaveAndOffersAlignmentAndSize() {
    rule.scenario.onActivity { activity ->
      val fragment=fragment(activity)
      fragment.findPreference<androidx.preference.SwitchPreferenceCompat>(com.quran.labs.androidquran.pip.TranslationPipController.PREFERENCE)!!.apply {
        isEnabled=true; isChecked=true
      }
      fragment.findPreference<Preference>(com.quran.labs.androidquran.pip.PipAppearance.SETTINGS)!!.performClick()
      val appearance=com.quran.labs.androidquran.pip.PipAppearance(activity)
      assertThat(slider(dialog().window!!.decorView as ViewGroup)!!.value).isEqualTo(32f)
      slider(dialog().window!!.decorView as ViewGroup)!!.value=48f
      dialog().getButton(AlertDialog.BUTTON_NEGATIVE).performClick()
      shadowOf(Looper.getMainLooper()).idle()
      assertThat(appearance.preferredSizeSp).isEqualTo(32)
      fragment.findPreference<Preference>(com.quran.labs.androidquran.pip.PipAppearance.SETTINGS)!!.performClick()
      slider(dialog().window!!.decorView as ViewGroup)!!.value=14f
      dialog().getButton(AlertDialog.BUTTON_POSITIVE).performClick()
      shadowOf(Looper.getMainLooper()).idle()
      assertThat(appearance.preferredSizeSp).isEqualTo(14)
      assertThat(appearance.centered).isTrue()
    }
  }
}
