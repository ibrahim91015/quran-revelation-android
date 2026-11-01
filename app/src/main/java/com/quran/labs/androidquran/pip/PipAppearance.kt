package com.quran.labs.androidquran.pip

import android.content.Context
import androidx.preference.PreferenceManager
import com.quran.labs.androidquran.R

enum class PipCorner(val label: Int, val top: Boolean, val left: Boolean) {
  TOP_LEFT(R.string.pip_top_left, true, true),
  TOP_RIGHT(R.string.pip_top_right, true, false),
  BOTTOM_LEFT(R.string.pip_bottom_left, false, true),
  BOTTOM_RIGHT(R.string.pip_bottom_right, false, false)
}

class PipAppearance(context: Context) {
  private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
  val centered get() = preferences.getString(ALIGNMENT, "center") != "justify"
  val preferredSizeSp get() = preferences.getInt(TEXT_SIZE, 32).coerceIn(14,48)
  val marginDp get() = preferences.getInt(MARGIN, 8).coerceIn(0,24)
  val fullReference get() = preferences.getBoolean(FULL_REFERENCE, false)
  val corner get() = PipCorner.entries.firstOrNull { it.name == preferences.getString(CORNER, null) } ?: PipCorner.BOTTOM_RIGHT
  fun save(centered: Boolean, sizeSp: Int, marginDp: Int = this.marginDp,
    fullReference: Boolean = this.fullReference, corner: PipCorner = this.corner) {
    preferences.edit().putString(ALIGNMENT, if (centered) "center" else "justify")
      .putInt(TEXT_SIZE,sizeSp.coerceIn(14,48)).putInt(MARGIN, marginDp.coerceIn(0,24))
      .putBoolean(FULL_REFERENCE, fullReference).putString(CORNER, corner.name).apply()
  }
  companion object {
    const val ALIGNMENT = "revelation_pip_alignment"
    const val TEXT_SIZE = "revelation_pip_text_size"
    const val SETTINGS = "revelation_pip_appearance"
    const val MARGIN = "revelation_pip_margin"
    const val FULL_REFERENCE = "revelation_pip_full_reference"
    const val CORNER = "revelation_pip_corner"
    val KEYS = setOf(ALIGNMENT, TEXT_SIZE, MARGIN, FULL_REFERENCE, CORNER)
  }
}
