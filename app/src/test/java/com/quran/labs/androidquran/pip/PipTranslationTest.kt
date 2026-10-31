package com.quran.labs.androidquran.pip

import android.app.Activity
import android.app.Application
import android.view.View
import com.google.common.truth.Truth.assertThat
import com.quran.data.model.QuranText
import com.quran.labs.androidquran.R
import com.quran.mobile.translation.model.LocalTranslation
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.Shadows.shadowOf
import android.os.Looper

@Config(application = Application::class, sdk = [33])
@RunWith(RobolectricTestRunner::class)
class PipTranslationTest {
  @Test fun priorityMatchesTranslationModeAndFiltersActiveChoicesBeforeSelectingOne() {
    val translations = listOf(LocalTranslation(filename="third.db",displayOrder=8),
      LocalTranslation(filename="first.db",displayOrder=1),LocalTranslation(filename="second.db",displayOrder=2))
    assertThat(PipTranslationRepository.firstTranslation(translations,setOf("third.db","first.db"))?.filename).isEqualTo("first.db")
    assertThat(PipTranslationRepository.firstTranslation(translations,setOf("second.db"))?.filename).isEqualTo("second.db")
    assertThat(PipTranslationRepository.firstTranslation(translations,emptySet())?.filename).isEqualTo("first.db")
    assertThat(PipTranslationRepository.firstTranslation(emptyList(),emptySet())).isNull()
  }
  @Test fun plainTranslationExpandsLinkedTextAndOmitsFootnotesAndHtmlTags() {
    assertThat(PipTranslationRepository.plainText(QuranText(1,1,"<b>Mercy</b> [[footnote]] and peace.")))
      .isEqualTo("Mercy and peace.")
    assertThat(PipTranslationRepository.plainText(QuranText(1,2,"4","Linked <i>translation</i>.")))
      .isEqualTo("Linked translation.")
  }
  @Test @GraphicsMode(GraphicsMode.Mode.NATIVE)
  fun completeLongTranslationsFitBothSmallAndExpandedWindowsWithoutEllipsis() {
    Robolectric.buildActivity(Activity::class.java).setup().use { controller ->
      val activity = controller.get()
      activity.setTheme(R.style.QuranToolBarDayNight)
      val view = FittingTranslationView(activity)
      val longText = ("A complete translation of a long ayah, with every word retained. ").repeat(45)
      view.showTranslation(longText)
      fun layout(width: Int, height: Int): Float {
        view.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),
          View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY))
        view.layout(0,0,width,height)
        view.forceLayout()
        view.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),
          View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY))
        view.layout(0,0,width,height)
        view.draw(android.graphics.Canvas(android.graphics.Bitmap.createBitmap(width,height,android.graphics.Bitmap.Config.ARGB_8888)))
        assertThat(view.text.toString()).isEqualTo(longText)
        assertThat(view.ellipsize).isNull()
        assertThat(view.layout.height).isAtMost(height-view.compoundPaddingTop-view.compoundPaddingBottom)
        assertThat(view.layout.getLineEnd(view.layout.lineCount-1)).isEqualTo(longText.length)
        return view.textSize
      }
      val small = layout(320,180)
      assertThat(layout(640,360)).isGreaterThan(small)
    }
  }
}
