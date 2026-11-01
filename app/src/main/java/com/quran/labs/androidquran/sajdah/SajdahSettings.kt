package com.quran.labs.androidquran.sajdah

import android.content.Context
import android.media.AudioManager
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import androidx.preference.ListPreference
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.slider.Slider
import com.google.android.material.snackbar.Snackbar
import com.quran.labs.androidquran.R
import com.quran.labs.androidquran.ui.fragment.QuranSettingsFragment

/** Draft previews never change the saved choice or Android's media volume. */
class SajdahSettings(private val fragment: QuranSettingsFragment) : AutoCloseable {
  private val context get() = fragment.requireContext()
  private val preferences = SajdahPreferences(context)
  private val playerDelegate = lazy { SajdahSoundPlayer(context) }
  private val player by playerDelegate
  private var activeDialog: androidx.appcompat.app.AlertDialog? = null
  private lateinit var soundPreference: Preference
  private lateinit var volumePreference: Preference
  private lateinit var pausePreference: ListPreference

  fun addTo(group: PreferenceGroup) {
    group.addPreference(SwitchPreferenceCompat(context).apply {
      key = SajdahPreferences.ENABLED
      title = context.getString(R.string.sajdah_cue_title)
      summary = context.getString(R.string.sajdah_cue_summary)
      setDefaultValue(true)
      isIconSpaceReserved = false
    })
    group.addPreference(SwitchPreferenceCompat(context).apply {
      key = SajdahPreferences.NOTIFY_AT_START
      title = context.getString(R.string.sajdah_notify_start_title)
      summary = context.getString(R.string.sajdah_notify_start_summary)
      setDefaultValue(false)
      isIconSpaceReserved = false
    })
    soundPreference = Preference(context).apply {
      key = SajdahPreferences.SOUND
      title = context.getString(R.string.sajdah_sound_title)
      isIconSpaceReserved = false
      setOnPreferenceClickListener { soundDialog(); true }
    }
    group.addPreference(soundPreference)
    volumePreference = Preference(context).apply {
      key = SajdahPreferences.VOLUME
      title = context.getString(R.string.sajdah_volume_title)
      isIconSpaceReserved = false
      setOnPreferenceClickListener { volumeDialog(); true }
    }
    group.addPreference(volumePreference)
    group.addPreference(Preference(context).apply {
      key = "revelation_test_sajdah_sound"
      title = context.getString(R.string.sajdah_test_title)
      summary = context.getString(R.string.sajdah_test_summary)
      isIconSpaceReserved = false
      setOnPreferenceClickListener { preview(preferences.sound, preferences.volume); true }
    })
    pausePreference = ListPreference(context).apply {
      key = SajdahPause.MODE
      title = context.getString(R.string.sajdah_pause_title)
      entries = arrayOf(context.getString(R.string.sajdah_pause_off),
        context.getString(R.string.sajdah_pause_seconds, 2), context.getString(R.string.sajdah_pause_seconds, 5),
        context.getString(R.string.sajdah_pause_manual), context.getString(R.string.sajdah_pause_custom))
      entryValues = arrayOf("off", "2", "5", "manual", "custom")
      setDefaultValue("2")
      isIconSpaceReserved = false
      summaryProvider = Preference.SummaryProvider<ListPreference> { preference ->
        if (preference.value == "custom") context.getString(R.string.sajdah_pause_custom_summary,
          preferences.preferences.getInt(SajdahPause.CUSTOM_SECONDS, 2).coerceIn(0, 60))
        else preference.entry ?: context.getString(R.string.sajdah_pause_seconds, 2)
      }
      setOnPreferenceChangeListener { _, selection ->
        if (selection == "custom") { this@SajdahSettings.fragment.view?.post { customPauseDialog() }; false } else true
      }
    }
    group.addPreference(pausePreference)
    updateSummaries()
  }

  private fun updateSummaries() {
    soundPreference.summary = context.getString(preferences.sound.label)
    volumePreference.summary = context.getString(R.string.sajdah_volume_summary, preferences.volume)
  }

  private fun preview(sound: SajdahSound, volume: Int) {
    player.play(sound, volume)
    if (context.getSystemService(AudioManager::class.java).getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
      fragment.view?.let { Snackbar.make(it, R.string.sajdah_media_muted, Snackbar.LENGTH_LONG).show() }
    }
  }

  private fun soundDialog() {
    var selected = preferences.sound
    activeDialog?.dismiss()
    activeDialog = MaterialAlertDialogBuilder(context)
      .setTitle(R.string.sajdah_sound_title)
      .setSingleChoiceItems(SajdahSound.entries.map { context.getString(it.label) }.toTypedArray(), selected.ordinal) { _, which ->
        selected = SajdahSound.entries[which]
        preview(selected, preferences.volume)
      }
      .setPositiveButton(R.string.sajdah_use_sound) { _, _ -> preferences.sound = selected; updateSummaries() }
      .setNegativeButton(android.R.string.cancel, null)
      .setNeutralButton(R.string.sajdah_preview, null)
      .create().also { dialog ->
        dialog.setOnShowListener {
          dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL)
            .setOnClickListener { preview(selected, preferences.volume) }
        }
        dialog.show()
      }
  }

  private fun volumeDialog() {
    var selected = preferences.volume
    val padding = (24 * context.resources.displayMetrics.density).toInt()
    val content = LinearLayout(context).apply {
      orientation = LinearLayout.VERTICAL
      setPadding(padding, padding / 2, padding, 0)
    }
    val label = TextView(context).apply {
      setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
      text = context.getString(R.string.sajdah_volume_explanation)
    }
    val volumeLabel = TextView(context).apply {
      setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium)
      text = context.getString(R.string.sajdah_volume_percent, selected)
      setPadding(0, padding / 2, 0, 0)
    }
    val slider = Slider(context).apply {
      valueFrom = 0f; valueTo = 100f; stepSize = 1f; this.value = selected.toFloat()
      contentDescription = context.getString(R.string.sajdah_volume_title)
      setLabelFormatter { context.getString(R.string.sajdah_volume_percent, it.toInt()) }
      addOnChangeListener { _, gain, _ ->
        selected = gain.toInt()
        volumeLabel.text = context.getString(R.string.sajdah_volume_percent, selected)
      }
    }
    content.addView(label)
    content.addView(volumeLabel)
    content.addView(slider, LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
    activeDialog?.dismiss()
    activeDialog = MaterialAlertDialogBuilder(context).setTitle(R.string.sajdah_volume_title).setView(content)
      .setPositiveButton(R.string.sajdah_save) { _, _ -> preferences.volume = selected; updateSummaries() }
      .setNegativeButton(android.R.string.cancel, null)
      .setNeutralButton(R.string.sajdah_preview, null).create().also { dialog ->
        dialog.setOnShowListener {
          dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_NEUTRAL)
            .setOnClickListener { preview(preferences.sound, selected) }
        }
        dialog.show()
      }
  }

  private fun customPauseDialog() {
    var seconds = preferences.preferences.getInt(SajdahPause.CUSTOM_SECONDS, 2).coerceIn(0, 60)
    val padding = (24 * context.resources.displayMetrics.density).toInt()
    val content = LinearLayout(context).apply {
      orientation = LinearLayout.VERTICAL
      setPadding(padding, padding / 2, padding, 0)
    }
    content.addView(TextView(context).apply {
      setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium)
      setText(R.string.sajdah_pause_explanation)
    })
    val label = TextView(context).apply {
      setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium)
      text = context.getString(R.string.sajdah_pause_seconds, seconds)
      setPadding(0, padding / 2, 0, 0)
    }
    content.addView(label)
    content.addView(Slider(context).apply {
      valueFrom = 0f; valueTo = 60f; stepSize = 1f; value = seconds.toFloat()
      contentDescription = context.getString(R.string.sajdah_pause_title)
      setLabelFormatter { context.getString(R.string.sajdah_pause_seconds, it.toInt()) }
      addOnChangeListener { _, value, _ -> seconds = value.toInt(); label.text = context.getString(R.string.sajdah_pause_seconds, seconds) }
    })
    activeDialog?.dismiss()
    activeDialog = MaterialAlertDialogBuilder(context).setTitle(R.string.sajdah_pause_custom).setView(content)
      .setPositiveButton(R.string.sajdah_save) { _, _ ->
        preferences.preferences.edit().putInt(SajdahPause.CUSTOM_SECONDS, seconds).apply()
        pausePreference.value = "custom"
        pausePreference.summaryProvider = pausePreference.summaryProvider
      }.setNegativeButton(android.R.string.cancel, null).show()
  }

  override fun close() {
    activeDialog?.dismiss(); activeDialog = null
    if (playerDelegate.isInitialized()) player.close()
  }
}
