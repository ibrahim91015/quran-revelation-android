package com.quran.labs.androidquran.ui

import android.content.Intent
import android.content.DialogInterface
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.quran.data.model.SuraAyah
import com.quran.labs.androidquran.QuranApplication
import com.quran.labs.androidquran.R
import com.quran.labs.androidquran.base.TestApplication
import com.quran.labs.androidquran.common.audio.model.QariItem
import com.quran.labs.androidquran.common.audio.model.playback.*
import com.quran.labs.androidquran.fakes.FakeAudioPresenterScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowDialog

@Config(application = TestApplication::class, sdk = [33])
@RunWith(RobolectricTestRunner::class)
class HomePlaybackWarningTest {
  @get:Rule val home = ActivityScenarioRule(QuranActivity::class.java)
  private fun checkReplacement(auto: Boolean, always: Boolean, test: (PagerActivity, FakeAudioPresenterScreen) -> Unit) {
    val app = ApplicationProvider.getApplicationContext<QuranApplication>()
    val ayah = SuraAyah(68, 12)
    val oldRequest = AudioRequest(ayah, SuraAyah(110, 3),
      QariItem(0,"Reciter","https://example.com/",path="test",hasGaplessAlternative=false),
      enforceBounds=false, shouldStream=true, audioPathInfo=AudioPathInfo("","",null,emptyList()), revelationOrder=true)
    app.audioStatus.updateAyahPlayback(AudioStatus.Playback(ayah, oldRequest, PlaybackStatus.PAUSED))
    app.audioStatus.markPause(auto)
    androidx.preference.PreferenceManager.getDefaultSharedPreferences(app).edit()
      .putBoolean("revelation_warn_manual_pause", always).commit()
    val intent = Intent(app, PagerActivity::class.java).putExtra("page", 1)
      .putExtra(PagerActivity.EXTRA_BROWSING_OTHER_LOCATION,true)
      .putExtra(PagerActivity.EXTRA_AUDIO_SOURCE_TAB,"surahs")
    ActivityScenario.launch<PagerActivity>(intent).use { reader ->
      reader.onActivity {
        val screen = FakeAudioPresenterScreen()
        it.audioPresenter.bind(screen)
        test(it, screen)
      }
    }
  }
  private fun start(reader: PagerActivity) = reader.playFromAyah(SuraAyah(1,1),null,1,0,0,false,1f)
  private fun calls(screen: FakeAudioPresenterScreen) = screen.handlePlaybackCalls.size + screen.handleRequiredDownloadCalls.size

  @Test fun cancelProtectsTheOldVerseAndConfirmationStartsTheNewSelection() = checkReplacement(true,false) { reader, screen ->
    start(reader)
    val dialog = ShadowDialog.getLatestDialog() as AlertDialog
    val message = dialog.findViewById<TextView>(android.R.id.message)!!.text.toString()
    assertThat(message).contains("12")
    assertThat(message).contains(reader.getString(R.string.audio_order_revelation))
    assertThat(calls(screen)).isEqualTo(0)
    dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick()
    org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
    assertThat((reader.application as QuranApplication).audioStatus.pausedForBrowsing).isTrue()
    assertThat(calls(screen)).isEqualTo(0)
    start(reader)
    (ShadowDialog.getLatestDialog() as AlertDialog).getButton(DialogInterface.BUTTON_POSITIVE).performClick()
    org.robolectric.Shadows.shadowOf(android.os.Looper.getMainLooper()).idle()
    assertThat(calls(screen)).isEqualTo(1)
  }
  @Test fun manualPauseStartsElsewhereWithoutWarningByDefault() = checkReplacement(false,false) { reader, screen ->
    start(reader)
    assertThat(calls(screen)).isEqualTo(1)
    assertThat(ShadowDialog.getLatestDialog()?.isShowing ?: false).isFalse()
  }
  @Test fun enabledPreferenceAlsoProtectsAManuallyPausedSession() = checkReplacement(false,true) { reader, screen ->
    start(reader)
    assertThat(ShadowDialog.getLatestDialog().isShowing).isTrue()
    assertThat(calls(screen)).isEqualTo(0)
  }
}
