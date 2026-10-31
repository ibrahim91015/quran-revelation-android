package com.quran.labs.androidquran.sajdah

import android.content.Context
import androidx.preference.PreferenceManager

object SajdahPause {
  const val MODE = "revelation_sajdah_pause"
  const val CUSTOM_SECONDS = "revelation_sajdah_pause_seconds"
  /** null waits for an explicit resume; zero disables the pause. */
  fun durationMillis(context: Context): Long? {
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    return when (preferences.getString(MODE, "2")) {
      "off" -> 0L
      "manual" -> null
      "5" -> 5000L
      "custom" -> preferences.getInt(CUSTOM_SECONDS, 2).coerceIn(0, 60) * 1000L
      else -> 2000L
    }
  }
}
