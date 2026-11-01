package com.quran.labs.androidquran.sajdah

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.quran.data.model.SuraAyah
import com.quran.labs.androidquran.R
import com.quran.labs.androidquran.data.QuranDisplayData

class SajdahAlerts(private val context: Context, handler: Handler,
  private val displayData: QuranDisplayData) : AutoCloseable {
  private val preferences = SajdahPreferences(context)
  private val manager = context.getSystemService(NotificationManager::class.java)
  private val soundPlayer = SajdahSoundPlayer(context)
  private val state = SajdahAlertState()
  private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
    if (key == SajdahPreferences.ENABLED) handler.post { if (!preferences.enabled) clear() }
  }
  init {
    createChannel(context)
    preferences.preferences.registerOnSharedPreferenceChangeListener(preferenceListener)
  }

  fun started(ayah: SuraAyah, pendingIntent: PendingIntent) {
    if (preferences.enabled && state.started(ayah, preferences.notifyAtStart)) {
      notify(ayah, pendingIntent, playing = true, alert = true)
    }
  }

  fun completed(ayah: SuraAyah, pendingIntent: PendingIntent, onChimeStarted: () -> Unit = {}) {
    if (!preferences.enabled) { clear(); onChimeStarted(); return }
    when (state.completed(ayah)) {
      SajdahAlertState.Change.SHOW -> {
        // Start at the completion boundary, while the service is already paused.
        soundPlayer.play(preferences.sound, preferences.volume, onChimeStarted)
        val wasAnnounced = manager.activeNotifications.any { it.id == NOTIFICATION }
        notify(ayah, pendingIntent, playing = false, alert = !wasAnnounced)
      }
      SajdahAlertState.Change.CLEAR -> manager.cancel(NOTIFICATION)
      SajdahAlertState.Change.NONE -> Unit
    }
    if (!com.quran.labs.androidquran.data.SajdahLocations.contains(ayah.sura, ayah.ayah)) onChimeStarted()
  }

  private fun notify(ayah: SuraAyah, pendingIntent: PendingIntent, playing: Boolean, alert: Boolean) {
    if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context,
        Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
    val label = context.getString(R.string.sajdah_notification_ayah,
      displayData.getSuraName(context, ayah.sura, true, false), ayah.ayah)
    val notification = NotificationCompat.Builder(context, CHANNEL)
      .setSmallIcon(R.drawable.ic_notification)
      .setContentTitle(context.getString(if (playing) R.string.sajdah_notification_playing else R.string.sajdah_notification_title))
      .setContentText(label)
      .setStyle(NotificationCompat.BigTextStyle().bigText(context.getString(
        if (playing) R.string.sajdah_notification_playing_body else R.string.sajdah_notification_body, label)))
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setCategory(NotificationCompat.CATEGORY_REMINDER)
      .setDefaults(0).setSound(null).setVibrate(VIBRATION)
      .setOnlyAlertOnce(!alert)
      .setContentIntent(pendingIntent).setAutoCancel(true)
      .build()
    try { manager.notify(NOTIFICATION, notification) } catch (_: SecurityException) { /* permission changed */ }
  }

  fun clear() { state.clear(); manager.cancel(NOTIFICATION); soundPlayer.stop() }
  override fun close() {
    preferences.preferences.unregisterOnSharedPreferenceChangeListener(preferenceListener)
    clear()
    soundPlayer.close()
  }

  companion object {
    // Notification behaviors are immutable after creation; v2 enables vibration for upgrades.
    const val CHANNEL = "revelation_sajdah_alerts_v2"
    const val NOTIFICATION = 7419
    val VIBRATION = longArrayOf(0, 250, 120, 250)
    fun createChannel(context: Context) {
      if (Build.VERSION.SDK_INT >= 26) {
        val channel = NotificationChannel(CHANNEL, context.getString(R.string.sajdah_notification_channel),
          NotificationManager.IMPORTANCE_HIGH).apply {
          description = context.getString(R.string.sajdah_channel_description)
          setSound(null, null)
          vibrationPattern = VIBRATION
          enableVibration(true)
          setShowBadge(false)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
      }
    }
  }
}
