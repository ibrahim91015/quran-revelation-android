package com.quran.labs.androidquran.presenter.audio.service

import com.google.common.truth.Truth.assertThat
import com.quran.data.core.QuranInfo
import com.quran.data.model.SuraAyah
import com.quran.labs.androidquran.common.audio.model.QariItem
import com.quran.labs.androidquran.common.audio.model.playback.AudioPathInfo
import com.quran.labs.androidquran.common.audio.model.playback.AudioRequest
import com.quran.labs.androidquran.data.RevelationOrder
import com.quran.labs.androidquran.pages.data.madani.MadaniDataSource
import org.junit.Test
import java.io.File
import java.nio.file.Files

class AudioQueueRevelationTest {
  private val info = QuranInfo(MadaniDataSource())

  private fun request(
    start: SuraAyah = SuraAyah(96, 1),
    end: SuraAyah = SuraAyah(110, 3),
    gapless: Boolean = true,
    bounded: Boolean = false,
    repeats: Int = 0,
    rangeRepeats: Int = 0
  ) = AudioRequest(
    start, end,
    QariItem(0, "Reciter", "https://example.com/", path = "test",
      hasGaplessAlternative = false, db = if (gapless) "test" else null),
    repeatInfo = repeats, rangeRepeatInfo = rangeRepeats,
    enforceBounds = bounded, shouldStream = true,
    audioPathInfo = AudioPathInfo(
      if (gapless) "https://example.com/%03d.mp3" else "https://example.com/%03d%03d.mp3",
      "", if (gapless) "test.db" else null, listOf("opus", "mp3")
    ),
    revelationOrder = true
  )

  @Test fun `all 113 gapless surah boundaries follow revelation order`() {
    RevelationOrder.suras.toList().zipWithNext().forEach { (sura, next) ->
      val queue = AudioQueue(info, request(start = SuraAyah(sura, info.getNumberOfAyahs(sura))))
      assertThat(queue.playNextAyah()).isTrue()
      assertThat(queue.getCurrentPlaybackAyah()).isEqualTo(SuraAyah(next, 1))
      assertThat(queue.getUrl()).isEqualTo("https://example.com/%03d.mp3".format(next))
    }
  }

  @Test fun `all gapped boundaries preserve basmallah and then play the correct verse`() {
    RevelationOrder.suras.toList().zipWithNext().forEach { (sura, next) ->
      val queue = AudioQueue(info, request(
        start = SuraAyah(sura, info.getNumberOfAyahs(sura)), gapless = false))
      queue.playNextAyah()
      assertThat(queue.getCurrentPlaybackAyah()).isEqualTo(SuraAyah(next, 1))
      if (next != 1 && next != 9) {
        assertThat(queue.getUrl()).isEqualTo("https://example.com/001001.mp3")
        queue.playNextAyah()
      }
      assertThat(queue.getUrl()).isEqualTo("https://example.com/%03d001.mp3".format(next))
    }
  }

  @Test fun `backwards navigation follows all revelation boundaries`() {
    RevelationOrder.suras.toList().zipWithNext().forEach { (previous, sura) ->
      val queue = AudioQueue(info, request(start = SuraAyah(sura, 1)))
      assertThat(queue.playPreviousAyah(true)).isTrue()
      assertThat(queue.getCurrentPlaybackAyah()).isEqualTo(
        SuraAyah(previous, info.getNumberOfAyahs(previous)))
    }
  }

  @Test fun `verses inside a surah stay in normal order`() {
    val queue = AudioQueue(info, request())
    (2..19).forEach { ayah ->
      queue.playNextAyah()
      assertThat(queue.getCurrentPlaybackAyah()).isEqualTo(SuraAyah(96, ayah))
    }
    queue.playNextAyah()
    assertThat(queue.getCurrentPlaybackAyah()).isEqualTo(SuraAyah(68, 1))
  }

  @Test fun `canonical sessions still advance from 96 to 97`() {
    val queue = AudioQueue(info, request(start = SuraAyah(96, 19)).copy(revelationOrder = false))
    queue.playNextAyah()
    assertThat(queue.getCurrentPlaybackAyah()).isEqualTo(SuraAyah(97, 1))
  }

  @Test fun `chronological bounds do not treat decreasing surah numbers as out of range`() {
    val queue = AudioQueue(info, request(end = SuraAyah(68, 52), bounded = true))
    queue.playAt(96, 19, true)
    assertThat(queue.playNextAyah()).isTrue()
    assertThat(queue.getCurrentPlaybackAyah()).isEqualTo(SuraAyah(68, 1))
    queue.playAt(68, 52, true)
    assertThat(queue.playNextAyah()).isFalse()
  }

  @Test fun `verse repeats finish before crossing the surah boundary`() {
    val queue = AudioQueue(info, request(start = SuraAyah(96, 19), repeats = 1))
    queue.playNextAyah()
    assertThat(queue.getCurrentPlaybackAyah()).isEqualTo(SuraAyah(96, 19))
    queue.playNextAyah()
    assertThat(queue.getCurrentPlaybackAyah()).isEqualTo(SuraAyah(68, 1))
  }

  @Test fun `bounded revelation range repeats from its chronological start`() {
    val queue = AudioQueue(info, request(end = SuraAyah(68, 52), bounded = true, rangeRepeats = 1))
    queue.playAt(68, 52, true)
    queue.playNextAyah()
    assertThat(queue.getCurrentPlaybackAyah()).isEqualTo(SuraAyah(96, 1))
    queue.playAt(68, 52, true)
    assertThat(queue.playNextAyah()).isFalse()
  }

  @Test fun `revelation playback stops after An Nasr and cannot rewind before Al Alaq`() {
    assertThat(AudioQueue(info, request(start = SuraAyah(110, 3))).playNextAyah()).isFalse()
    assertThat(AudioQueue(info, request()).playPreviousAyah()).isFalse()
  }

  @Test fun `downloaded gapless surah falls back to streaming for the next surah`() {
    val local = Files.createTempDirectory("revelation-gapless").toFile()
    try {
      val file = File(local, "096.mp3").apply { writeText("downloaded") }
      val request = request(start = SuraAyah(96, 19)).let {
        it.copy(audioPathInfo = it.audioPathInfo.copy(localDirectory = local.path))
      }
      val queue = AudioQueue(info, request)
      assertThat(queue.getUrl()).isEqualTo(file.absolutePath)
      queue.playNextAyah()
      assertThat(queue.getUrl()).isEqualTo("https://example.com/068.mp3")
    } finally {
      local.deleteRecursively()
    }
  }

  @Test fun `local opus is preferred and gapped basmallah still resolves locally`() {
    val local = Files.createTempDirectory("revelation-gapped").toFile()
    try {
      val basmallah = File(local, "1/1.mp3").apply { parentFile.mkdirs(); writeText("basmallah") }
      val first = File(local, "68/1.opus").apply { parentFile.mkdirs(); writeText("verse") }
      val request = request(start = SuraAyah(96, 19), gapless = false).let {
        it.copy(audioPathInfo = it.audioPathInfo.copy(localDirectory = local.path))
      }
      val queue = AudioQueue(info, request)
      queue.playNextAyah()
      assertThat(queue.getUrl()).isEqualTo(basmallah.absolutePath)
      queue.playNextAyah()
      assertThat(queue.getUrl()).isEqualTo(first.absolutePath)
    } finally {
      local.deleteRecursively()
    }
  }

  @Test fun `playback settings updates preserve chronological position`() {
    val request = request(start = SuraAyah(96, 19))
    val queue = AudioQueue(info, request)
    queue.playNextAyah()
    val updated = queue.withUpdatedAudioRequest(request.copy(playbackSpeed = 1.5f))
    updated.playPreviousAyah(true)
    assertThat(updated.getCurrentPlaybackAyah()).isEqualTo(SuraAyah(96, 19))
  }
}
