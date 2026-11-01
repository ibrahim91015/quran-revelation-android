package com.quran.labs.androidquran.pip

import android.content.Context
import android.graphics.Canvas
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.widget.AppCompatTextView
import android.graphics.text.LineBreaker
import com.google.android.material.color.MaterialColors
import com.quran.data.model.SuraAyah
import com.quran.labs.androidquran.R
import com.quran.labs.androidquran.data.Constants
import com.quran.labs.androidquran.ui.util.TypefaceManager
import androidx.preference.PreferenceManager

/** Fit the complete translation, without ellipsis, to each OS-controlled PiP size. */
class FittingTranslationView(context: Context) : AppCompatTextView(context) {
  private val appearance = PipAppearance(context)
  private val referencePaint = TextPaint(android.graphics.Paint.ANTI_ALIAS_FLAG)
  private var baseInset = 0
  var verseReference: String = ""
    private set
  private var maximumSizeSp = 32f
  private val translationTypeface: android.graphics.Typeface?
  init {
    setTextAppearance(R.style.TranslationText)
    translationTypeface = typeface
    setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface))
    setBackgroundColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface))
    // Preserve font extents (including Arabic marks) at the first/last line.
    includeFontPadding = true
    textDirection = TEXT_DIRECTION_FIRST_STRONG
    setLineSpacing(0f, 1f)
    referencePaint.color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurfaceVariant)
    referencePaint.typeface = translationTypeface
    importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
  }
  fun showTranslation(value: String, ayah: SuraAyah? = null, languageCode: String? = null) {
    verseReference = ayah?.let { "${it.sura}:${it.ayah}" }.orEmpty()
    contentDescription = if (verseReference.isEmpty()) value else "$verseReference. $value"
    maximumSizeSp = appearance.preferredSizeSp.toFloat()
    val centered = appearance.centered
    gravity = Gravity.CENTER_VERTICAL or if (centered) Gravity.CENTER_HORIZONTAL else Gravity.START
    textAlignment = if (centered) TEXT_ALIGNMENT_CENTER else TEXT_ALIGNMENT_GRAVITY
    if (android.os.Build.VERSION.SDK_INT >= 26) {
      justificationMode = if (centered) LineBreaker.JUSTIFICATION_MODE_NONE else LineBreaker.JUSTIFICATION_MODE_INTER_WORD
    }
    val rtl = TextDirectionHeuristics.FIRSTSTRONG_LTR.isRtl(value,0,value.length)
    typeface = when {
      languageCode == "ar" -> TypefaceManager.getTafseerTypeface(context)
      !rtl && PreferenceManager.getDefaultSharedPreferences(context).getBoolean(Constants.PREF_USE_DYSLEXIC_FONT,false) ->
        TypefaceManager.getDyslexicTypeface(context)
      else -> translationTypeface
    }
    text = value
    reserveReferenceSpace()
    fit()
  }
  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    reserveReferenceSpace()
    fit()
  }
  private fun reserveReferenceSpace() {
    baseInset = minOf((8 * resources.displayMetrics.density).toInt(), (minOf(width,height).coerceAtLeast(1) / 14)).coerceAtLeast(1)
    referencePaint.textSize = minOf(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,12f,resources.displayMetrics),
      height.coerceAtLeast(1) * .10f).coerceAtLeast(1f)
    val referenceHeight = if (verseReference.isEmpty()) 0 else
      kotlin.math.ceil(referencePaint.fontMetrics.descent - referencePaint.fontMetrics.ascent).toInt() + baseInset / 2
    setPadding(baseInset,baseInset,baseInset,baseInset+referenceHeight)
  }
  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    if (verseReference.isNotEmpty()) {
      val x = width - baseInset - referencePaint.measureText(verseReference)
      val baseline = height - baseInset - referencePaint.fontMetrics.descent
      canvas.drawText(verseReference,x,baseline,referencePaint)
    }
  }
  private fun fit() {
    val availableWidth = width - compoundPaddingLeft - compoundPaddingRight
    val availableHeight = height - compoundPaddingTop - compoundPaddingBottom
    if (availableWidth <= 0 || availableHeight <= 0 || text.isNullOrEmpty()) return
    val measurePaint = TextPaint(paint)
    var low = 1f
    var high = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, maximumSizeSp, resources.displayMetrics)
    repeat(16) {
      val size = (low + high) / 2
      measurePaint.textSize = size
      val layout = StaticLayout.Builder.obtain(text, 0, text.length, measurePaint, availableWidth)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL).setIncludePad(true)
        .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_LTR)
        .setBreakStrategy(LineBreaker.BREAK_STRATEGY_HIGH_QUALITY).build()
      if (layout.height <= availableHeight) low = size else high = size
    }
    setTextSize(TypedValue.COMPLEX_UNIT_PX, low)
    breakStrategy = LineBreaker.BREAK_STRATEGY_HIGH_QUALITY
    ellipsize = null
  }
}
