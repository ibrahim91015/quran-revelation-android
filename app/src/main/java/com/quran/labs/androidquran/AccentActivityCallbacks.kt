package com.quran.labs.androidquran

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.compose.ui.graphics.Color
import androidx.preference.PreferenceManager
import com.quran.labs.androidquran.common.ui.core.QuranAccent
import java.util.WeakHashMap

class AccentActivityCallbacks : Application.ActivityLifecycleCallbacks {
  private val applied = WeakHashMap<Activity, String>()

  override fun onActivityPreCreated(activity: Activity, savedInstanceState: Bundle?) = apply(activity)
  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    if (!applied.containsKey(activity)) apply(activity)
  }
  private fun apply(activity: Activity) {
    val key = selectedKey(activity)
    activity.theme.applyStyle(when (key) {
      "emerald" -> R.style.AccentEmerald
      "violet" -> R.style.AccentViolet
      "amber" -> R.style.AccentAmber
      else -> R.style.AccentBlue
    }, true)
    applied[activity] = key
  }
  override fun onActivityResumed(activity: Activity) {
    if (applied[activity] != selectedKey(activity)) activity.recreate()
  }
  override fun onActivityStarted(activity: Activity) = Unit
  override fun onActivityPaused(activity: Activity) = Unit
  override fun onActivityStopped(activity: Activity) = Unit
  override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
  override fun onActivityDestroyed(activity: Activity) { applied.remove(activity) }

  companion object {
    private fun selectedKey(context: Context): String =
      PreferenceManager.getDefaultSharedPreferences(context).getString("revelation_accent", "blue") ?: "blue"
    fun selectedAccent(context: Context): QuranAccent = when (selectedKey(context)) {
      "emerald" -> QuranAccent(Color(0xFF006B57), Color(0xFF75DBBE), onDark = Color(0xFF00382C))
      "violet" -> QuranAccent(Color(0xFF6750A4), Color(0xFFD0BCFF), onDark = Color(0xFF381E72))
      "amber" -> QuranAccent(Color(0xFF805600), Color(0xFFFABD49), onDark = Color(0xFF432C00))
      else -> QuranAccent(Color(0xFF1F6FB2), Color(0xFFA1C9FF), onDark = Color(0xFF003258))
    }
  }
}
