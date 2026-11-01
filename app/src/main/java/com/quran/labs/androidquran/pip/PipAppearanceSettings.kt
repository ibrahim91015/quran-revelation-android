package com.quran.labs.androidquran.pip

import android.view.View
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.ScrollView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.materialswitch.MaterialSwitch
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
    var margin=appearance.marginDp
    var corner=appearance.corner
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
    fun dropdown(title: Int, items: Array<String>, selected: Int, changed: (Int) -> Unit): MaterialAutoCompleteTextView {
      val field = TextInputLayout(context, null, com.google.android.material.R.attr.textInputOutlinedExposedDropdownMenuStyle).apply {
        hint = context.getString(title)
        layoutParams = LinearLayout.LayoutParams(-1,-2).apply { topMargin=spacing/2 }
      }
      val input = MaterialAutoCompleteTextView(field.context).apply {
        inputType = 0
        setSimpleItems(items)
        setText(items[selected],false)
        setOnItemClickListener { _, _, position, _ -> changed(position) }
      }
      field.addView(input)
      content.addView(field)
      return input
    }
    val presetValues=listOf(0,4,8,16)
    val presets=arrayOf(context.getString(R.string.pip_margin_none),context.getString(R.string.pip_margin_compact),
      context.getString(R.string.pip_margin_balanced),context.getString(R.string.pip_margin_relaxed),context.getString(R.string.pip_margin_custom))
    lateinit var marginSlider: Slider
    val preset=dropdown(R.string.pip_margin_preset,presets,presetValues.indexOf(margin).takeIf { it>=0 } ?: 4) { index ->
      if (index<4) marginSlider.value=presetValues[index].toFloat()
    }
    val marginLabel=TextView(context).apply {
      setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium)
      text=context.getString(R.string.pip_margin_dp,margin)
      setPadding(0,spacing/2,0,0)
    }
    content.addView(marginLabel)
    marginSlider=Slider(context).apply {
      valueFrom=0f; valueTo=24f; stepSize=1f; value=margin.toFloat()
      contentDescription=context.getString(R.string.pip_edge_margins)
      setLabelFormatter { context.getString(R.string.pip_margin_dp,it.toInt()) }
      addOnChangeListener { _, value, _ ->
        margin=value.toInt()
        marginLabel.text=context.getString(R.string.pip_margin_dp,margin)
        preset.setText(presets[presetValues.indexOf(margin).takeIf { it>=0 } ?: 4],false)
      }
    }
    content.addView(marginSlider)
    content.addView(TextView(context).apply {
      setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodySmall)
      setText(R.string.pip_margin_explanation)
    })
    val reference=MaterialSwitch(context).apply {
      setText(R.string.pip_full_reference)
      isChecked=appearance.fullReference
      minHeight=spacing*2
      setPadding(0,spacing/2,0,0)
    }
    content.addView(reference)
    val corners=PipCorner.entries.map { context.getString(it.label) }.toTypedArray()
    val position=dropdown(R.string.pip_reference_corner,corners,corner.ordinal) { corner=PipCorner.entries[it] }
    val scroll=ScrollView(context).apply { addView(content); isFillViewport=false }
    dialog?.dismiss()
    dialog=MaterialAlertDialogBuilder(context).setTitle(R.string.pip_appearance_title).setView(scroll)
      .setPositiveButton(R.string.sajdah_save) { _, _ -> appearance.save(centered,size,margin,reference.isChecked,corner); summary() }
      .setNegativeButton(android.R.string.cancel,null).setNeutralButton(R.string.pip_reset,null)
      .create().also { window ->
        window.show()
        window.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener {
          alignment.check(centerId); slider.value=32f; marginSlider.value=8f
          reference.isChecked=false; corner=PipCorner.BOTTOM_RIGHT; position.setText(corners[corner.ordinal],false)
        }
      }
  }
  override fun close() { dialog?.dismiss(); dialog=null }
}
