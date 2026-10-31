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
}
