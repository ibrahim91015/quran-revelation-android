package com.quran.labs.androidquran.ui.helpers

import android.content.Context
import androidx.preference.PreferenceManager
import com.quran.labs.androidquran.R

enum class HomeTab(val key: String, val title: Int) {
  SURAHS("surahs", R.string.quran_sura), JUZ("juz", R.string.quran_juz2),
  REVELATION("revelation", R.string.quran_revelation_order), BOOKMARKS("bookmarks", R.string.menu_bookmarks);
  companion object {
    fun fromKey(key: String?) = entries.firstOrNull { it.key == key } ?: SURAHS
    fun parseOrder(value: String?): List<HomeTab> {
      val saved = value.orEmpty().split(',').mapNotNull { key -> entries.firstOrNull { it.key == key } }.distinct()
      return saved + entries.filterNot { it in saved }
    }
  }
}

/** Order and list offsets are durable; the warm selected tab belongs to the current task. */
class HomeIndexState(context: Context) {
  private val prefs = PreferenceManager.getDefaultSharedPreferences(context)
  var order = HomeTab.parseOrder(prefs.getString("home_tab_order", null))
    private set
  var selected: HomeTab? = null
  val controllers = mutableMapOf<HomeTab, IndexScrollController>()
  val pendingTop = mutableSetOf<HomeTab>()
  fun top(tab: HomeTab) {
    val controller = controllers[tab]
    if (controller == null) pendingTop.add(tab) else controller.top()
  }
  fun move(from: HomeTab, to: HomeTab) {
    if (from == to) return
    val updated = order.toMutableList()
    updated.remove(from)
    updated.add(updated.indexOf(to).let { if (order.indexOf(from) < order.indexOf(to)) it + 1 else it }, from)
    order = updated
    prefs.edit().putString("home_tab_order", updated.joinToString(",") { it.key }).apply()
  }
  fun save(tab: HomeTab, position: Int, offset: Int) {
    if (position >= 0) prefs.edit().putInt("home_${tab.key}_position", position)
      .putInt("home_${tab.key}_offset", offset).apply()
  }
  fun position(tab: HomeTab) = prefs.getInt("home_${tab.key}_position", 0).coerceAtLeast(0)
  fun offset(tab: HomeTab) = prefs.getInt("home_${tab.key}_offset", 0)
}
