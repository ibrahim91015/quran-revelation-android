package com.quran.labs.androidquran.pip

import android.view.View
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.slider.Slider
import com.quran.labs.androidquran.R
import com.quran.labs.androidquran.ui.fragment.QuranSettingsFragment

class PipAppearanceSettings(private val fragment: QuranSettingsFragment) : AutoCloseable {
  private val context get() = fragment.requireContext()
  private val appearance = PipAppearance(context)
  private lateinit var preference: Preference
  private var dialog: AlertDialog? = null
  fun addTo(group: PreferenceGroup) {
    preference = Preference(context).apply {
      key = PipAppearance.SETTINGS
      title = context.getString(R.string.pip_appearance_title)
      isIconSpaceReserved = false
      setOnPreferenceClickListener { show(); true }
    }
    group.addPreference(preference)
    preference.dependency = TranslationPipController.PREFERENCE
    summary()
  }
  private fun summary() {
    preference.summary = context.getString(R.string.pip_appearance_summary,
      context.getString(if (appearance.centered) R.string.pip_centered else R.string.pip_justified), appearance.preferredSizeSp)
  }
  private fun show() {
    var centered=appearance.centered
    var size=appearance.preferredSizeSp
    val spacing=(24 * context.resources.displayMetrics.density).toInt()
    val content=LinearLayout(context).apply {
      orientation=LinearLayout.VERTICAL
      setPadding(spacing,spacing/2,spacing,0)
    }
    content.addView(TextView(context).apply {
      setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
      setText(R.string.pip_appearance_explanation)
    })
    val centerId=View.generateViewId()
    val justifyId=View.generateViewId()
    val alignment=RadioGroup(context).apply {
      addView(MaterialRadioButton(context).apply { id=centerId; setText(R.string.pip_centered); minHeight=spacing*2 })
      addView(MaterialRadioButton(context).apply { id=justifyId; setText(R.string.pip_justified); minHeight=spacing*2 })
      check(if (centered) centerId else justifyId)
      setOnCheckedChangeListener { _, selected -> centered=selected==centerId }
    }
    content.addView(alignment)
    val label=TextView(context).apply {
      setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium)
      text=context.getString(R.string.pip_maximum_text_size,size)
      setPadding(0,spacing/2,0,0)
    }
    content.addView(label)
    val slider=Slider(context).apply {
      valueFrom=14f; valueTo=48f; stepSize=1f; value=size.toFloat()
      contentDescription=context.getString(R.string.pip_text_size)
      setLabelFormatter { context.getString(R.string.pip_text_size_sp,it.toInt()) }
      addOnChangeListener { _, value, _ -> size=value.toInt(); label.text=context.getString(R.string.pip_maximum_text_size,size) }
    }
    content.addView(slider)
    dialog?.dismiss()
    dialog=MaterialAlertDialogBuilder(context).setTitle(R.string.pip_appearance_title).setView(content)
      .setPositiveButton(R.string.sajdah_save) { _, _ -> appearance.save(centered,size); summary() }
      .setNegativeButton(android.R.string.cancel,null).setNeutralButton(R.string.pip_reset,null)
      .create().also { window ->
        window.setOnShowListener {
          window.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener { alignment.check(centerId); slider.value=32f }
        }
        window.show()
      }
  }
  override fun close() { dialog?.dismiss(); dialog=null }
}
