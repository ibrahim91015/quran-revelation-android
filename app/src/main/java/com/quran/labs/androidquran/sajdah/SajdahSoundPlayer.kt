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
  private val sounds = mutableMapOf<SajdahSound, Int>()
  private var pending: Pair<Int, Float>? = null
  private var stream = 0
  private var closed = false

  init {
    pool.setOnLoadCompleteListener { _, id, status ->
      synchronized(this) {
        if (!closed && status == 0) {
          loaded.add(id)
          pending?.takeIf { it.first == id }?.let { (_, volume) ->
            pending = null
            stream = pool.play(id, volume, volume, 1, 0, 1f)
          }
        }
      }
    }
    SajdahSound.entries.forEach { sounds[it] = pool.load(context.applicationContext, it.resource, 1) }
  }

  @Synchronized fun play(sound: SajdahSound, volume: Int) {
    if (closed) return
    stop()
    val id = sounds[sound] ?: return
    val gain = volume.coerceIn(0, 100) / 100f
    if (gain == 0f) return
    try {
      if (id in loaded) stream = pool.play(id, gain, gain, 1, 0, 1f)
      else pending = id to gain
    } catch (exception: RuntimeException) {
      Timber.w(exception, "Unable to play sajdah chime")
    }
  }

  @Synchronized fun stop() {
    pending = null
    if (stream != 0 && !closed) pool.stop(stream)
    stream = 0
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
