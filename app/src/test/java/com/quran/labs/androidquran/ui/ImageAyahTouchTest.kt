package com.quran.labs.androidquran.ui

import android.graphics.Matrix
import android.graphics.Bitmap
import android.widget.ImageView
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.quran.data.model.SuraAyah
import com.quran.labs.androidquran.ui.util.ImageAyahUtils
import com.quran.labs.androidquran.view.HighlightingImageView
import com.quran.page.common.data.AyahBounds
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = android.app.Application::class)
class ImageAyahTouchTest {
  @Test fun marginsAreNotMappedToTheNearestAyahEvenWithScaledPaddedPage() {
    val view = HighlightingImageView(ApplicationProvider.getApplicationContext()).apply {
      setImageBitmap(Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888))
      layout(0, 0, 1020, 1040)
      scaleType = ImageView.ScaleType.MATRIX
      setPadding(10, 20, 10, 20)
      imageMatrix = Matrix().apply { setScale(2f, 2f) }
    }
    val coords = mapOf("96:1" to listOf(AyahBounds(1, 0, 100, 100, 400, 160)),
      "96:2" to listOf(AyahBounds(2, 0, 100, 180, 400, 240)))
    fun at(x: Float, y: Float) = ImageAyahUtils.getAyahFromCoordinates(coords, view,
      x * 2 + 10, y * 2 + 20)
    assertThat(at(200f, 120f)).isEqualTo(SuraAyah(96, 1))
    assertThat(at(200f, 210f)).isEqualTo(SuraAyah(96, 2))
    listOf(200f to 50f, 200f to 290f, 50f to 120f, 450f to 210f).forEach { (x, y) ->
      assertThat(at(x, y)).isNull()
    }
    // Small gaps within the text still allow the nearest line to be selected.
    assertThat(at(200f, 170f)).isNotNull()
  }
}
