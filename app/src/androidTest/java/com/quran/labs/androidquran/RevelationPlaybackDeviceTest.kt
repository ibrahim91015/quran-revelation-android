package com.quran.labs.androidquran

import android.content.Context
import android.content.Intent
import android.app.Notification
import android.app.NotificationManager
import android.database.sqlite.SQLiteDatabase
import android.media.MediaMetadataRetriever
import android.os.SystemClock
import android.net.ConnectivityManager
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.quran.data.model.SuraAyah
import com.quran.data.page.provider.madani.MadaniPageProvider
import com.quran.labs.androidquran.common.audio.model.QariItem
import com.quran.labs.androidquran.common.audio.model.playback.AudioPathInfo
import com.quran.labs.androidquran.common.audio.model.playback.AudioRequest
import com.quran.labs.androidquran.common.audio.model.playback.AudioStatus
import com.quran.labs.androidquran.common.audio.model.playback.PlaybackStatus
import com.quran.labs.androidquran.service.AudioService
import com.quran.labs.androidquran.ui.QuranActivity
import com.quran.labs.androidquran.ui.PagerActivity
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.zip.ZipInputStream

/** Real Media3 decoding and foreground-service tests. Run on an API 36 device with Internet. */
@RunWith(AndroidJUnit4::class)
class RevelationPlaybackDeviceTest {
  private val instrumentation = InstrumentationRegistry.getInstrumentation()
  private val context = instrumentation.targetContext

  private fun shell(command: String) {
    instrumentation.uiAutomation.executeShellCommand(command).use { descriptor ->
      java.io.FileInputStream(descriptor.fileDescriptor).use { it.readBytes() }
    }
  }

  private fun download(url: String, file: File) {
    val connection = URL(url).openConnection() as HttpURLConnection
    connection.connectTimeout = 30_000
    connection.readTimeout = 30_000
    try {
      assertEquals(200, connection.responseCode)
      connection.inputStream.use { input -> file.outputStream().use { input.copyTo(it) } }
    } finally {
      connection.disconnect()
    }
  }

  private fun request(qariId: Int, opus: Boolean = false, local: Boolean = false): AudioRequest {
    val qari = QariItem.fromQari(context, MadaniPageProvider().getQaris().single { it.id == qariId })
    val extension = if (opus) "opus" else "mp3"
    val directory = File(context.cacheDir, "revelation-device-$qariId-$extension-$local").apply { mkdirs() }
    val database = qari.databaseName?.let { name ->
      val result = File(directory, "$name.db")
      if (!result.exists()) {
        val archive = File(directory, "timings.zip")
        download("https://android.quran.com/data/databases/audio/$name.zip", archive)
        ZipInputStream(archive.inputStream()).use { zip ->
          var entry = zip.nextEntry
          var extracted = false
          while (entry != null) {
            if (!entry.isDirectory && entry.name.substringAfterLast('/') == "$name.db") {
              result.outputStream().use { zip.copyTo(it) }
              extracted = true
              break
            }
            entry = zip.nextEntry
          }
          assertTrue("Timing archive must contain $name.db", extracted)
        }
      }
      result
    }
    if (local) {
      for (sura in listOf(96, 68)) {
        val filename = "%03d.$extension".format(sura)
        val file = File(directory, filename)
        if (!file.exists()) download(qari.url(extension) + filename, file)
      }
    }
    return AudioRequest(
      start = SuraAyah(96, 19), end = SuraAyah(110, 3), qari = qari,
      enforceBounds = false, playbackSpeed = 3f, shouldStream = true,
      audioPathInfo = AudioPathInfo(
        qari.url(extension) + if (qari.isGapless) "%03d.$extension" else "%03d%03d.$extension",
        directory.absolutePath, database?.absolutePath, listOf(extension)
      ),
      revelationOrder = true
    )
  }

  private fun action(action: String, request: AudioRequest? = null) {
    val intent = Intent(context, AudioService::class.java).setAction(action)
    request?.let { intent.putExtra(AudioService.EXTRA_PLAY_INFO, it) }
    if (request != null) context.startForegroundService(intent) else context.startService(intent)
  }

  private fun awaitPlaying(probe: AudioService, sura: Int, minimumAyah: Int = 1) = runBlocking {
    withTimeout(90_000) {
      probe.audioStatusRepository.audioPlaybackFlow.first { status ->
        status is AudioStatus.Playback && status.currentAyah.sura == sura &&
          status.currentAyah.ayah >= minimumAyah && status.playbackStatus == PlaybackStatus.PLAYING
      }
    }
  }

  private fun withForeground(test: (AudioService) -> Unit) {
    shell("input keyevent KEYCODE_WAKEUP")
    shell("pm grant ${context.packageName} android.permission.POST_NOTIFICATIONS")
    ActivityScenario.launch(QuranActivity::class.java).use {
      val probe = AudioService()
      (context.applicationContext as QuranApplication).applicationComponent.inject(probe)
      try {
        test(probe)
      } finally {
        action(AudioService.ACTION_STOP)
      }
    }
  }

  @Test fun gaplessMp3StreamsAcrossRevelationBoundaryInBackgroundAfterPauseResume() = withForeground { probe ->
    action(AudioService.ACTION_PLAYBACK, request(0))
    awaitPlaying(probe, 96)
    action(AudioService.ACTION_PAUSE)
    runBlocking {
      withTimeout(10_000) {
        probe.audioStatusRepository.audioPlaybackFlow.first {
          it is AudioStatus.Playback && it.playbackStatus == PlaybackStatus.PAUSED
        }
      }
    }
    action(AudioService.ACTION_PLAY)
    context.startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
      .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    try {
      shell("input keyevent KEYCODE_SLEEP")
      awaitPlaying(probe, 68, minimumAyah = 2)
    } finally { shell("input keyevent KEYCODE_WAKEUP") }
  }

  @Test fun gaplessOpusStreamsFromAlAlaqIntoAlQalam() = withForeground { probe ->
    action(AudioService.ACTION_PLAYBACK, request(1, opus = true))
    awaitPlaying(probe, 96)
    awaitPlaying(probe, 68, minimumAyah = 2)
  }

  @Test fun downloadedGaplessAudioContinuesInRevelationOrderWithoutNetwork() = withForeground { probe ->
    val request = request(0, local = true)
    try {
      shell("svc wifi disable")
      shell("svc data disable")
      runBlocking {
        withTimeout(15_000) {
          while ((context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
              .activeNetwork != null) delay(200)
        }
      }
      action(AudioService.ACTION_PLAYBACK, request)
      awaitPlaying(probe, 96)
      awaitPlaying(probe, 68, minimumAyah = 2)
    } finally {
      shell("svc wifi enable")
      shell("svc data enable")
    }
  }

  @Test fun gappedStreamingRepeatsLastAyahThenContinuesIntoAlQalam() = withForeground { probe ->
    action(AudioService.ACTION_PLAYBACK, request(6).copy(repeatInfo = 1))
    awaitPlaying(probe, 96)
    awaitPlaying(probe, 68, minimumAyah = 2)
  }

  @Test fun pickerReturnAndRecreationKeepPausedNotificationAndTapOpensCurrentAyah() = withForeground { probe ->
    action(AudioService.ACTION_PLAYBACK, request(0))
    awaitPlaying(probe, 96)
    ActivityScenario.launch(QuranActivity::class.java).use { picker ->
      SystemClock.sleep(1000)
      assertTrue(probe.audioStatusRepository.audioPlaybackFlow.value is AudioStatus.Playback)
      action(AudioService.ACTION_PAUSE)
      val paused = runBlocking {
        withTimeout(10_000) { probe.audioStatusRepository.audioPlaybackFlow.first {
          it is AudioStatus.Playback && it.playbackStatus == PlaybackStatus.PAUSED
        } }
      } as AudioStatus.Playback
      picker.recreate()
      SystemClock.sleep(1500)
      assertEquals(PlaybackStatus.PAUSED,
        (probe.audioStatusRepository.audioPlaybackFlow.value as AudioStatus.Playback).playbackStatus)
      val manager = context.getSystemService(NotificationManager::class.java)
      val notification = manager.activeNotifications.single {
        it.id == com.quran.labs.androidquran.data.Constants.NOTIFICATION_ID_AUDIO_PLAYBACK
      }.notification
      assertTrue(notification.flags and Notification.FLAG_ONGOING_EVENT != 0)
      assertTrue(notification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT).toString()
        .contains("Revelation order"))
      notification.contentIntent.send()
      instrumentation.waitForIdleSync()
      instrumentation.runOnMainSync {
        val reader = ActivityLifecycleMonitorRegistry.getInstance()
          .getActivitiesInStage(Stage.RESUMED).filterIsInstance<PagerActivity>().single()
        assertEquals(paused.currentAyah.sura, reader.intent.getIntExtra(PagerActivity.EXTRA_HIGHLIGHT_SURA, -1))
        assertEquals(paused.currentAyah.ayah, reader.intent.getIntExtra(PagerActivity.EXTRA_HIGHLIGHT_AYAH, -1))
        assertTrue(reader.intent.getBooleanExtra(PagerActivity.EXTRA_REVELATION_ORDER, false))
        reader.finish()
      }
    }
  }

  @Test fun shortGaplessSurahPlaysEveryVerseAndAudibleTailDespiteEarlyTimingMarker() = withForeground { probe ->
    val original = request(0)
    val directory = File(original.audioPathInfo.localDirectory)
    val file = File(directory, "108.mp3")
    if (!file.exists()) download(original.qari.url("mp3") + "108.mp3", file)
    val duration = MediaMetadataRetriever().use { metadata ->
      metadata.setDataSource(file.path)
      metadata.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)!!.toLong()
    }
    val timing = File(directory, "short-surah-tail.db")
    File(original.audioPathInfo.gaplessDatabase!!).copyTo(timing, overwrite = true)
    SQLiteDatabase.openDatabase(timing.path, null, 0).use {
      it.execSQL("UPDATE timings SET time=1 WHERE sura=108 AND ayah=999")
    }
    val request = original.copy(start = SuraAyah(108, 1), audioPathInfo =
      original.audioPathInfo.copy(gaplessDatabase = timing.path))
    val observed = mutableSetOf<Int>()
    action(AudioService.ACTION_PLAYBACK, request)
    awaitPlaying(probe, 108)
    val started = SystemClock.elapsedRealtime()
    runBlocking {
      withTimeout(90_000) {
        probe.audioStatusRepository.audioPlaybackFlow.first { status ->
          if (status is AudioStatus.Playback && status.currentAyah.sura == 108) observed += status.currentAyah.ayah
          status is AudioStatus.Playback && status.currentAyah.sura == 102 && status.playbackStatus == PlaybackStatus.PLAYING
        }
      }
    }
    assertEquals(setOf(1, 2, 3), observed)
    assertTrue("The file's tail must finish before advancing",
      SystemClock.elapsedRealtime() - started >= duration / 3 - 2000)
  }

  @Test fun advancedSettingsAndAllAccentThemesOpenWithOriginalInstalled() {
    context.packageManager.getPackageInfo("com.quran.labs.androidquran", 0)
    val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
    try {
      for (accent in listOf("blue", "emerald", "violet", "amber")) {
        prefs.edit().putString("revelation_accent", accent).commit()
        ActivityScenario.launch(QuranAdvancedPreferenceActivity::class.java).use { scenario ->
          scenario.onActivity { activity ->
            assertTrue(activity.supportFragmentManager.findFragmentById(R.id.content) is
              com.quran.labs.androidquran.ui.fragment.QuranAdvancedSettingsFragment)
          }
        }
      }
    } finally { prefs.edit().putString("revelation_accent", "blue").commit() }
  }
}
