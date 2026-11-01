package com.quran.labs.androidquran.pip

import android.app.PictureInPictureParams
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Rational
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.PreferenceManager
import com.quran.labs.androidquran.QuranApplication
import com.quran.labs.androidquran.R
import com.quran.labs.androidquran.common.audio.model.playback.AudioStatus
import com.quran.labs.androidquran.common.audio.model.playback.PlaybackStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber

/** This is a view of the service's session. It never starts, pauses, or stops audio. */
class TranslationPipController(private val activity: ComponentActivity) : DefaultLifecycleObserver {
  private val app = activity.application as QuranApplication
  private val preferences = PreferenceManager.getDefaultSharedPreferences(activity)
  private val content = activity.findViewById<ViewGroup>(android.R.id.content)
  private val translation = FittingTranslationView(activity).apply { visibility = View.GONE }
  private val previousVisibility = mutableMapOf<View, Int>()
  private var internalNavigation = false
  private var contentReady = false
  private var loadingJob: kotlinx.coroutines.Job? = null
  private val preferenceListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
    if (key == PREFERENCE || key == PipAppearance.ALIGNMENT || key == PipAppearance.TEXT_SIZE) activity.runOnUiThread { refresh() }
  }
  @get:androidx.annotation.ChecksSdkIntAtLeast(api = 26)
  private val supported get() = Build.VERSION.SDK_INT >= 26 && activity.packageManager
    .hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
  private val enabled get() = supported && preferences.getBoolean(PREFERENCE, false)
  val isPresenting get() = translation.visibility == View.VISIBLE
  private val playback get() = app.audioStatus.audioPlaybackFlow.value as? AudioStatus.Playback

  init {
    content.addView(translation, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
    activity.lifecycle.addObserver(this)
    translation.showTranslation(activity.getString(R.string.translation_pip_loading),playback?.currentAyah)
    if (Build.VERSION.SDK_INT >= 26 && activity.isInPictureInPictureMode) showOverlay()
    preferences.registerOnSharedPreferenceChangeListener(preferenceListener)
    activity.lifecycleScope.launch {
      activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
        app.audioStatus.audioPlaybackFlow.map { status ->
          (status as? AudioStatus.Playback)?.let { it.currentAyah to it.playbackStatus }
        }.distinctUntilChanged().collectLatest { refresh() }
      }
    }
  }

  private fun refresh() {
    loadingJob?.cancel()
    if (!enabled) { updateParams(false); return }
    val current = playback
    if (current == null) {
      updateParams(false)
      if (Build.VERSION.SDK_INT >= 26 && activity.isInPictureInPictureMode) activity.finish()
      return
    }
    contentReady = false
    loadingJob = activity.lifecycleScope.launch {
      val value = try { app.pipTranslations.loadContent(current.currentAyah) }
        catch (cancelled: CancellationException) { throw cancelled }
        catch (exception: Exception) {
          Timber.w(exception, "PiP translation unavailable")
          PipTranslationRepository.Translation(activity.getString(R.string.translation_pip_unavailable),null)
        }
      translation.showTranslation(value?.text?.takeIf { it.isNotBlank() } ?: activity.getString(R.string.translation_pip_no_translation),
        current.currentAyah,value?.languageCode)
      contentReady = true
      updateParams(current.playbackStatus == PlaybackStatus.PLAYING && !internalNavigation)
    }
  }

  @androidx.annotation.RequiresApi(26)
  private fun params(autoEnter: Boolean): PictureInPictureParams {
    val builder = PictureInPictureParams.Builder().setAspectRatio(Rational(16, 9))
    if (Build.VERSION.SDK_INT >= 31) builder.setAutoEnterEnabled(autoEnter).setSeamlessResizeEnabled(false)
    return builder.build()
  }
  private fun updateParams(autoEnter: Boolean) {
    if (!supported || activity.isFinishing || activity.isDestroyed) return
    try { activity.setPictureInPictureParams(params(autoEnter && enabled && contentReady)) }
    catch (exception: RuntimeException) { Timber.w(exception, "PiP parameters unavailable") }
  }
  fun internalNavigation() { internalNavigation = true; updateParams(false) }
  fun onUserLeave() {
    if (Build.VERSION.SDK_INT >= 26 && Build.VERSION.SDK_INT <= 30 && enabled && contentReady && !internalNavigation &&
      playback?.playbackStatus == PlaybackStatus.PLAYING) {
      try { if (activity.enterPictureInPictureMode(params(false))) showOverlay() }
      catch (exception: RuntimeException) { Timber.w(exception, "PiP permission unavailable") }
    }
  }
  fun transitioningToPip() { if (enabled) showOverlay() }
  fun modeChanged(inPip: Boolean) { if (inPip) showOverlay() else hideOverlay() }
  private fun showOverlay() {
    if (translation.visibility == View.VISIBLE) return
    for (i in 0 until content.childCount) {
      val view = content.getChildAt(i)
      if (view !== translation) { previousVisibility[view] = view.visibility; view.visibility = View.INVISIBLE }
    }
    translation.visibility = View.VISIBLE
    translation.bringToFront()
  }
  private fun hideOverlay() {
    translation.visibility = View.GONE
    previousVisibility.forEach { (view, visibility) -> view.visibility = visibility }
    previousVisibility.clear()
  }
  override fun onResume(owner: LifecycleOwner) {
    if (Build.VERSION.SDK_INT < 26 || !activity.isInPictureInPictureMode) hideOverlay()
    internalNavigation = false
    refresh()
  }
  override fun onDestroy(owner: LifecycleOwner) {
    loadingJob?.cancel()
    preferences.unregisterOnSharedPreferenceChangeListener(preferenceListener)
    hideOverlay()
  }
  companion object { const val PREFERENCE = "revelation_translation_pip_beta" }
}
