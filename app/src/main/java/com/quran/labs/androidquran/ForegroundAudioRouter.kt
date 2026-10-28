package com.quran.labs.androidquran

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withResumed
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import com.quran.labs.androidquran.common.audio.model.playback.AudioStatus
import com.quran.labs.androidquran.ui.PagerActivity
import com.quran.labs.androidquran.ui.QuranActivity

/** Activity transitions and configuration changes are not app reopens. */
class ForegroundAudioRouter(private val app: QuranApplication) : Application.ActivityLifecycleCallbacks {
  private val started = mutableSetOf<Activity>()
  private val freshReaders = java.util.Collections.newSetFromMap(java.util.WeakHashMap<Activity, Boolean>())
  private var background = true
  private var foregroundEntry = false
  private var userLeft = false
  private val routingJobs = java.util.WeakHashMap<Activity, Job>()
  fun onUserLeaving() { userLeft = true }
  override fun onActivityStarted(activity: Activity) {
    if (started.isEmpty()) { foregroundEntry = background; background = false }
    started.add(activity)
  }
  override fun onActivityStopped(activity: Activity) {
    started.remove(activity)
    if (started.isEmpty() && !activity.isChangingConfigurations) background = true
  }
  override fun onActivityResumed(activity: Activity) {
    val explicitNewReader = freshReaders.remove(activity)
    if (!foregroundEntry && !userLeft) return
    if (activity is QuranDataActivity || activity is com.quran.labs.androidquran.ui.whatsnew.AnnouncementActivity) return
    val fromBackground = foregroundEntry
    foregroundEntry = false
    userLeft = false
    if (activity !is QuranActivity && activity !is PagerActivity) return
    // Leaving for another in-app location also produces onUserLeaveHint. It
    // must not undo an explicit browse operation while the app remains visible.
    if (!fromBackground && explicitNewReader && activity.intent
        .getBooleanExtra(PagerActivity.EXTRA_BROWSING_OTHER_LOCATION,false)) return
    val owner = activity as LifecycleOwner
    routingJobs.remove(activity)?.cancel()
    routingJobs[activity] = owner.lifecycleScope.launch {
      owner.lifecycle.withResumed {
      if (!activity.isFinishing && !activity.isDestroyed) {
        val current = app.audioStatus.audioPlaybackFlow.value as? AudioStatus.Playback
        if (current == null) {
          if (activity is PagerActivity && !explicitNewReader) {
            activity.startActivity(Intent(activity, QuranActivity::class.java)
              .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP))
          }
          return@withResumed
        }
        val intent = readerIntent(activity, current, app.quranInfo.getPageFromSuraAyah(
          current.currentAyah.sura, current.currentAyah.ayah))
        if (activity is PagerActivity) activity.onNewIntent(intent) else activity.startActivity(intent)
      }
      }
    }
  }
  override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    if (activity is PagerActivity && savedInstanceState == null) freshReaders.add(activity)
  }
  override fun onActivityPaused(activity: Activity) { routingJobs.remove(activity)?.cancel() }
  override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
  override fun onActivityDestroyed(activity: Activity) {
    started.remove(activity)
    routingJobs.remove(activity)?.cancel()
  }

  companion object {
    fun readerIntent(context: Context, status: AudioStatus.Playback, page: Int) =
      Intent(context, PagerActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        .putExtra("page", page)
        .putExtra(PagerActivity.EXTRA_HIGHLIGHT_SURA, status.currentAyah.sura)
        .putExtra(PagerActivity.EXTRA_HIGHLIGHT_AYAH, status.currentAyah.ayah)
        .putExtra(PagerActivity.EXTRA_REVELATION_ORDER, status.audioRequest.revelationOrder)
        .putExtra(PagerActivity.EXTRA_AUDIO_SOURCE_TAB, status.audioRequest.sourceTab)
        .putExtra(PagerActivity.EXTRA_BROWSING_OTHER_LOCATION, false)
  }
}
