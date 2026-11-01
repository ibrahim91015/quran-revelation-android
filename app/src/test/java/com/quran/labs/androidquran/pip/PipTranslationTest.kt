package com.quran.labs.androidquran.pip

import android.app.Activity
import android.app.Application
import android.view.View
import com.google.common.truth.Truth.assertThat
import com.quran.data.model.QuranText
import com.quran.data.model.SuraAyah
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
      view.showTranslation(longText,SuraAyah(2,282))
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
      assertThat(view.verseReference).isEqualTo("2:282")
    }
  }

  @Test @GraphicsMode(GraphicsMode.Mode.NATIVE)
  fun alignmentFontPreferencesAndSystemFontScaleAlwaysRetainTheWholeLongAyah() {
    Robolectric.buildActivity(Activity::class.java).setup().use { controller ->
      val activity=controller.get()
      for (fontScale in listOf(1f,1.5f)) for (centered in listOf(true,false)) for (size in listOf(14,32,48)) for (language in listOf("en","ar")) {
        val configuration=android.content.res.Configuration(activity.resources.configuration).apply { this.fontScale=fontScale }
        val context=android.view.ContextThemeWrapper(activity.createConfigurationContext(configuration),R.style.QuranToolBarDayNight)
        PipAppearance(context).save(centered,size)
        val view=FittingTranslationView(context)
        val longText=if (language=="ar") "ترجمة الآية الطويلة كاملة بدون حذف أي كلمة أو قطع السطر الأخير. ".repeat(100)
          else "A very long translation must retain every word, without a clipped last line. ".repeat(80)
        view.showTranslation(longText,SuraAyah(2,282),language)
        val width=320; val height=180
        view.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY))
        view.layout(0,0,width,height)
        view.forceLayout()
        view.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY))
        view.layout(0,0,width,height)
        view.draw(android.graphics.Canvas(android.graphics.Bitmap.createBitmap(width,height,android.graphics.Bitmap.Config.ARGB_8888)))
        assertThat(view.text.toString()).isEqualTo(longText)
        assertThat(view.layout.height).isAtMost(height-view.compoundPaddingTop-view.compoundPaddingBottom)
        assertThat(view.layout.getLineEnd(view.layout.lineCount-1)).isEqualTo(longText.length)
        assertThat(view.verseReference).isEqualTo("2:282")
        assertThat(view.ellipsize).isNull()
        assertThat(view.textSize).isAtMost(android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_SP,size.toFloat(),context.resources.displayMetrics)+.1f)
      }
    }
  }

  @Test @GraphicsMode(GraphicsMode.Mode.NATIVE)
  fun darkAndLightColorsAndArabicAndDyslexicFontsComeFromTheApp() {
    Robolectric.buildActivity(Activity::class.java).setup().use { controller ->
      val activity=controller.get()
      for (night in listOf(android.content.res.Configuration.UI_MODE_NIGHT_NO,android.content.res.Configuration.UI_MODE_NIGHT_YES)) {
        val config=android.content.res.Configuration(activity.resources.configuration).apply {
          uiMode=(uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK.inv()) or night
        }
        val context=android.view.ContextThemeWrapper(activity.createConfigurationContext(config),R.style.QuranToolBarDayNight)
        val preferences=androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        preferences.edit().putBoolean(com.quran.labs.androidquran.data.Constants.PREF_USE_DYSLEXIC_FONT,true).commit()
        val view=FittingTranslationView(context)
        view.showTranslation("A complete translation.",SuraAyah(2,113),"en")
        assertThat(view.typeface).isEqualTo(com.quran.labs.androidquran.ui.util.TypefaceManager.getDyslexicTypeface(context))
        val background=(view.background as android.graphics.drawable.ColorDrawable).color
        assertThat(background).isEqualTo(com.google.android.material.color.MaterialColors.getColor(view,com.google.android.material.R.attr.colorSurface))
        assertThat(view.currentTextColor).isEqualTo(com.google.android.material.color.MaterialColors.getColor(view,com.google.android.material.R.attr.colorOnSurface))
        assertThat(androidx.core.graphics.ColorUtils.calculateContrast(view.currentTextColor,background)).isAtLeast(4.5)
        view.showTranslation("بسم الله الرحمن الرحيم",SuraAyah(1,1),"ar")
        assertThat(view.typeface).isEqualTo(com.quran.labs.androidquran.ui.util.TypefaceManager.getTafseerTypeface(context))
      }
    }
  }
}
