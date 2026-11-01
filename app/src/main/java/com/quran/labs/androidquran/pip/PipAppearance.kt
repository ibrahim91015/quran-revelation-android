package com.quran.labs.androidquran.pip

import android.content.Context
import androidx.preference.PreferenceManager

class PipAppearance(context: Context) {
  private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
  val centered get() = preferences.getString(ALIGNMENT, "center") != "justify"
  val preferredSizeSp get() = preferences.getInt(TEXT_SIZE, 32).coerceIn(14,48)
  fun save(centered: Boolean, sizeSp: Int) {
    preferences.edit().putString(ALIGNMENT, if (centered) "center" else "justify")
      .putInt(TEXT_SIZE,sizeSp.coerceIn(14,48)).apply()
  }
  companion object {
    const val ALIGNMENT = "revelation_pip_alignment"
    const val TEXT_SIZE = "revelation_pip_text_size"
    const val SETTINGS = "revelation_pip_appearance"
  }
}
