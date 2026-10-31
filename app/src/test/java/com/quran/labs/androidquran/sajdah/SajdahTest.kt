package com.quran.labs.androidquran.sajdah

import android.app.Application
import android.app.NotificationManager
import android.media.AudioAttributes
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.quran.data.model.SuraAyah
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(application = Application::class, sdk = [33])
@RunWith(RobolectricTestRunner::class)
class SajdahTest {
  private val context get() = ApplicationProvider.getApplicationContext<Application>()
  @Before fun clear() { PreferenceManager.getDefaultSharedPreferences(context).edit().clear().commit() }

  @Test fun alertLastsUntilTheNextRecitedAyahCompletesAcrossRevelationBoundary() {
    val state = SajdahAlertState()
    assertThat(state.completed(SuraAyah(96,19))).isEqualTo(SajdahAlertState.Change.SHOW)
    assertThat(state.sajdah).isEqualTo(SuraAyah(96,19))
    assertThat(state.completed(SuraAyah(68,1))).isEqualTo(SajdahAlertState.Change.CLEAR)
    assertThat(state.sajdah).isNull()
    assertThat(state.completed(SuraAyah(68,2))).isEqualTo(SajdahAlertState.Change.NONE)
  }
  @Test fun repeatedSajdahKeepsTheAlertAndExplicitClearDiscardsIt() {
    val state = SajdahAlertState()
    repeat(2) { assertThat(state.completed(SuraAyah(19,58))).isEqualTo(SajdahAlertState.Change.SHOW) }
    state.clear()
    assertThat(state.completed(SuraAyah(19,59))).isEqualTo(SajdahAlertState.Change.NONE)
  }
  @Test fun chimeDefaultsAndIndependentGainAreStableAndValidated() {
    val preferences = SajdahPreferences(context)
    assertThat(preferences.enabled).isTrue()
    assertThat(preferences.sound).isEqualTo(SajdahSound.SOFT_BELLS)
    assertThat(preferences.volume).isEqualTo(50)
    preferences.volume = 120
    assertThat(preferences.volume).isEqualTo(100)
    preferences.volume = -1
    assertThat(preferences.volume).isEqualTo(0)
    assertThat(SajdahSound.fromKey("obsolete")).isEqualTo(SajdahSound.SOFT_BELLS)
    assertThat(SajdahSound.entries.map { it.resource }.distinct()).hasSize(6)
  }
  @Test fun pauseDefaultsToTwoSecondsAndSupportsOffManualAndClampedCustomTime() {
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    assertThat(SajdahPause.durationMillis(context)).isEqualTo(2000L)
    for ((mode,duration) in listOf("off" to 0L, "2" to 2000L, "5" to 5000L, "manual" to null)) {
      preferences.edit().putString(SajdahPause.MODE, mode).commit()
      assertThat(SajdahPause.durationMillis(context)).isEqualTo(duration)
    }
    for ((seconds,duration) in listOf(-1 to 0L, 0 to 0L, 17 to 17000L, 60 to 60000L, 100 to 60000L)) {
      preferences.edit().putString(SajdahPause.MODE,"custom").putInt(SajdahPause.CUSTOM_SECONDS,seconds).commit()
      assertThat(SajdahPause.durationMillis(context)).isEqualTo(duration)
    }
  }
  @Test fun alertChannelIsHighestNormalImportanceAndSilentWhileChimeUsesMedia() {
    SajdahAlerts.createChannel(context)
    val channel = context.getSystemService(NotificationManager::class.java).getNotificationChannel(SajdahAlerts.CHANNEL)
    assertThat(channel.importance).isEqualTo(NotificationManager.IMPORTANCE_HIGH)
    assertThat(channel.sound).isNull()
    assertThat(channel.shouldVibrate()).isFalse()
    val attributes = SajdahSoundPlayer.mediaAttributes()
    assertThat(attributes.usage).isEqualTo(AudioAttributes.USAGE_MEDIA)
    assertThat(attributes.contentType).isEqualTo(AudioAttributes.CONTENT_TYPE_MUSIC)
  }
}
