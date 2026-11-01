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
  private var edgeCrop = false
  private var corner = PipCorner.BOTTOM_RIGHT
  val referenceBounds = android.graphics.RectF()
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
  fun showTranslation(value: String, ayah: SuraAyah? = null, languageCode: String? = null, fullReference: String? = null) {
    verseReference = ayah?.let {
      if (appearance.fullReference && !fullReference.isNullOrBlank()) fullReference else "${it.sura}:${it.ayah}"
    }.orEmpty()
    contentDescription = if (verseReference.isEmpty()) value else "$verseReference. $value"
    maximumSizeSp = appearance.preferredSizeSp.toFloat()
    edgeCrop = appearance.marginDp == 0
    corner = appearance.corner
    includeFontPadding = !edgeCrop
    val centered = appearance.centered
    gravity = (if (edgeCrop) Gravity.TOP else Gravity.CENTER_VERTICAL) or if (centered) Gravity.CENTER_HORIZONTAL else Gravity.START
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
    referencePaint.typeface = typeface
    text = value
    reserveReferenceSpace()
    fit()
    reserveReferenceSpace()
  }
  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    reserveReferenceSpace()
    fit()
    reserveReferenceSpace()
  }
  private fun reserveReferenceSpace() {
    baseInset = minOf((appearance.marginDp * resources.displayMetrics.density).toInt(), minOf(width,height).coerceAtLeast(0)/4)
    referencePaint.textSize = minOf(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,12f,resources.displayMetrics),
      height.coerceAtLeast(1) * .10f).coerceAtLeast(1f)
    val referenceWidth = referencePaint.measureText(verseReference)
    val labelWidth = (width - baseInset*2).coerceAtLeast(1)
    if (referenceWidth > labelWidth) referencePaint.textSize *= labelWidth / referenceWidth
    val referenceHeight = if (verseReference.isEmpty()) 0 else
      kotlin.math.ceil(referencePaint.fontMetrics.descent - referencePaint.fontMetrics.ascent).toInt() + baseInset / 2
    val horizontal = if (edgeCrop) -kotlin.math.floor(paint.measureText("M")/2).toInt() else baseInset
    setPadding(horizontal,baseInset+if (corner.top) referenceHeight else 0,
      horizontal,baseInset+if (corner.top) 0 else referenceHeight)
    val x = if (corner.left) baseInset.toFloat() else width-baseInset-referencePaint.measureText(verseReference)
    val y = if (corner.top) baseInset.toFloat() else height-baseInset-(referencePaint.fontMetrics.descent-referencePaint.fontMetrics.ascent)
    referenceBounds.set(x,y,x+referencePaint.measureText(verseReference),y+referencePaint.fontMetrics.descent-referencePaint.fontMetrics.ascent)
  }
  override fun onDraw(canvas: Canvas) {
    canvas.save()
    if (edgeCrop && layout != null && layout.lineCount>0) {
      // Remove the first line's font ascent gap as well as all outer margins.
      val firstLine=text.subSequence(0,layout.getLineEnd(0)).toString().trimEnd()
      if (firstLine.isNotEmpty()) {
        val ink=android.graphics.Rect()
        paint.getTextBounds(firstLine,0,firstLine.length,ink)
        canvas.translate(0f,-(layout.getLineBaseline(0)+ink.top).toFloat())
      }
    }
    super.onDraw(canvas)
    canvas.restore()
    if (verseReference.isNotEmpty()) {
      canvas.drawText(verseReference,referenceBounds.left,referenceBounds.top-referencePaint.fontMetrics.ascent,referencePaint)
    }
  }
  private fun fit() {
    val availableWidth = width - baseInset*2
    val availableHeight = height - compoundPaddingTop - compoundPaddingBottom
    if (availableWidth <= 0 || availableHeight <= 0 || text.isNullOrEmpty()) return
    val measurePaint = TextPaint(paint)
    var low = 1f
    var high = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, maximumSizeSp, resources.displayMetrics)
    repeat(16) {
      val size = (low + high) / 2
      measurePaint.textSize = size
      val crop = if (edgeCrop) kotlin.math.floor(measurePaint.measureText("M")/2).toInt()*2 else 0
      val layout = StaticLayout.Builder.obtain(text, 0, text.length, measurePaint, availableWidth+crop)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL).setIncludePad(!edgeCrop)
        .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_LTR)
        .setBreakStrategy(LineBreaker.BREAK_STRATEGY_HIGH_QUALITY).build()
      if (layout.height <= availableHeight) low = size else high = size
    }
    setTextSize(TypedValue.COMPLEX_UNIT_PX, low)
    breakStrategy = LineBreaker.BREAK_STRATEGY_HIGH_QUALITY
    ellipsize = null
  }
}
