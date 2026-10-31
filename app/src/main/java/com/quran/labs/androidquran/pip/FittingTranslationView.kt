package com.quran.labs.androidquran.pip

import android.content.Context
import android.text.Layout
import android.text.StaticLayout
import android.text.TextDirectionHeuristics
import android.text.TextPaint
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import com.google.android.material.color.MaterialColors

/** Fit the complete translation, without ellipsis, to each OS-controlled PiP size. */
class FittingTranslationView(context: Context) : TextView(context) {
  init {
    setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyLarge)
    setTextColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorOnSurface))
    setBackgroundColor(MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface))
    includeFontPadding = false
    gravity = Gravity.CENTER_VERTICAL or Gravity.START
    textDirection = TEXT_DIRECTION_FIRST_STRONG
    setLineSpacing(0f, 1f)
    if (android.os.Build.VERSION.SDK_INT >= 26) justificationMode = Layout.JUSTIFICATION_MODE_INTER_WORD
    val inset = (8 * resources.displayMetrics.density).toInt()
    setPadding(inset, inset, inset, inset)
    importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
  }
  fun showTranslation(value: String) {
    text = value
    fit()
  }
  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    fit()
  }
  private fun fit() {
    val availableWidth = width - compoundPaddingLeft - compoundPaddingRight
    val availableHeight = height - compoundPaddingTop - compoundPaddingBottom
    if (availableWidth <= 0 || availableHeight <= 0 || text.isNullOrEmpty()) return
    val measurePaint = TextPaint(paint)
    var low = 1f
    var high = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 48f, resources.displayMetrics)
    repeat(16) {
      val size = (low + high) / 2
      measurePaint.textSize = size
      val layout = StaticLayout.Builder.obtain(text, 0, text.length, measurePaint, availableWidth)
        .setAlignment(Layout.Alignment.ALIGN_NORMAL).setIncludePad(false)
        .setTextDirection(TextDirectionHeuristics.FIRSTSTRONG_LTR)
        .setBreakStrategy(Layout.BREAK_STRATEGY_HIGH_QUALITY).build()
      if (layout.height <= availableHeight) low = size else high = size
    }
    setTextSize(TypedValue.COMPLEX_UNIT_PX, low)
    breakStrategy = Layout.BREAK_STRATEGY_HIGH_QUALITY
    ellipsize = null
  }
}
