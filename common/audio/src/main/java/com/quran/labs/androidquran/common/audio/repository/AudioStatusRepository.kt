package com.quran.labs.androidquran.common.audio.repository

import com.quran.data.di.AppScope
import com.quran.labs.androidquran.common.audio.model.playback.AudioStatus
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@SingleIn(AppScope::class)
class AudioStatusRepository @Inject constructor() {
  @Volatile var pausedForBrowsing: Boolean = false
    private set
  @Volatile var sessionGeneration: Long = 0
    private set

  fun markPause(forBrowsing: Boolean) { pausedForBrowsing = forBrowsing }
  fun clearPauseReason() { pausedForBrowsing = false }
  fun newSession() { sessionGeneration++; clearPauseReason() }

  fun shouldWarnBeforeReplacement(alwaysWarn: Boolean): Boolean {
    val status = audioPlaybackInternalFlow.value as? AudioStatus.Playback ?: return false
    return pausedForBrowsing || (alwaysWarn &&
      status.playbackStatus == com.quran.labs.androidquran.common.audio.model.playback.PlaybackStatus.PAUSED)
  }
  private val audioPlaybackInternalFlow = MutableStateFlow<AudioStatus>(AudioStatus.Stopped)

  val audioPlaybackFlow = audioPlaybackInternalFlow.asStateFlow()

  fun updateAyahPlayback(audioStatus: AudioStatus) {
    if (audioStatus == AudioStatus.Stopped) clearPauseReason()
    audioPlaybackInternalFlow.value = audioStatus
  }
}

