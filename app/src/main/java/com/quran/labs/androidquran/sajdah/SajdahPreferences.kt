package com.quran.labs.androidquran.sajdah

import android.content.Context
import androidx.preference.PreferenceManager
import com.quran.labs.androidquran.R

enum class SajdahSound(val key: String, val label: Int, val resource: Int) {
  SOFT_BELLS("soft_bells", R.string.sajdah_soft_bells, R.raw.sajdah_soft_bells),
  WARM_MARIMBA("warm_marimba", R.string.sajdah_warm_marimba, R.raw.sajdah_warm_marimba),
  GENTLE_HARP("gentle_harp", R.string.sajdah_gentle_harp, R.raw.sajdah_gentle_harp),
  CRYSTAL_CHIME("crystal_chime", R.string.sajdah_crystal_chime, R.raw.sajdah_crystal_chime),
  EVENING_GLOW("evening_glow", R.string.sajdah_evening_glow, R.raw.sajdah_evening_glow),
  PEACEFUL_RISE("peaceful_rise", R.string.sajdah_peaceful_rise, R.raw.sajdah_peaceful_rise);

  companion object {
    fun fromKey(key: String?) = entries.firstOrNull { it.key == key } ?: SOFT_BELLS
  }
}

class SajdahPreferences(context: Context) {
  val preferences = PreferenceManager.getDefaultSharedPreferences(context)
  val enabled get() = preferences.getBoolean(ENABLED, true)
  var sound: SajdahSound
    get() = SajdahSound.fromKey(preferences.getString(SOUND, null))
    set(value) { preferences.edit().putString(SOUND, value.key).apply() }
  var volume: Int
    get() = preferences.getInt(VOLUME, 50).coerceIn(0, 100)
    set(value) { preferences.edit().putInt(VOLUME, value.coerceIn(0, 100)).apply() }

  companion object {
    const val ENABLED = "revelation_sajdah_cue"
    const val SOUND = "revelation_sajdah_sound"
    const val VOLUME = "revelation_sajdah_volume"
  }
}
