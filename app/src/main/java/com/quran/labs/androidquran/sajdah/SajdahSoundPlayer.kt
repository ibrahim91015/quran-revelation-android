package com.quran.labs.androidquran.sajdah

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import timber.log.Timber

/** No audio-focus request or system-volume changes: the recitation keeps playing. */
class SajdahSoundPlayer(context: Context) : AutoCloseable {
  private val pool = SoundPool.Builder().setMaxStreams(1)
    .setAudioAttributes(mediaAttributes()).build()
  private val loaded = mutableSetOf<Int>()
  private val failed = mutableSetOf<Int>()
  private val sounds = mutableMapOf<SajdahSound, Int>()
  private data class Pending(val id: Int, val gain: Float, val onStarted: () -> Unit)
  private var pending: Pending? = null
  private var stream = 0
  private var closed = false

  init {
    pool.setOnLoadCompleteListener { _, id, status ->
      synchronized(this) {
        if (!closed) {
          if (status == 0) loaded.add(id) else failed.add(id)
          pending?.takeIf { it.id == id }?.let { request ->
            pending = null
            if (status == 0) stream = pool.play(id, request.gain, request.gain, 1, 0, 1f)
            request.onStarted()
          }
        }
      }
    }
    SajdahSound.entries.forEach { sounds[it] = pool.load(context.applicationContext, it.resource, 1) }
  }

  @Synchronized fun play(sound: SajdahSound, volume: Int, onStarted: () -> Unit = {}) {
    if (closed) { onStarted(); return }
    stop()
    val id = sounds[sound] ?: run { onStarted(); return }
    val gain = volume.coerceIn(0, 100) / 100f
    if (gain == 0f || id == 0 || id in failed) { onStarted(); return }
    try {
      if (id in loaded) { stream = pool.play(id, gain, gain, 1, 0, 1f); onStarted() }
      else pending = Pending(id, gain, onStarted)
    } catch (exception: RuntimeException) {
      Timber.w(exception, "Unable to play sajdah chime")
      onStarted()
    }
  }

  @Synchronized fun stop() {
    val cancelled = pending
    pending = null
    if (stream != 0 && !closed) pool.stop(stream)
    stream = 0
    cancelled?.onStarted?.invoke()
  }

  @Synchronized override fun close() {
    if (!closed) {
      stop()
      closed = true
      pool.setOnLoadCompleteListener(null)
      pool.release()
    }
  }

  companion object {
    fun mediaAttributes(): AudioAttributes = AudioAttributes.Builder()
      .setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build()
  }
}
