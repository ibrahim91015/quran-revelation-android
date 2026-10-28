package com.quran.labs.androidquran.ui.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import com.quran.data.source.PageProvider
import com.quran.labs.androidquran.QuranAdvancedPreferenceActivity
import com.quran.labs.androidquran.QuranApplication
import com.quran.labs.androidquran.R
import com.quran.labs.androidquran.data.Constants
import com.quran.labs.androidquran.pageselect.PageSelectActivity
import com.quran.labs.androidquran.ui.TranslationManagerActivity
import com.quran.labs.androidquran.util.OrientationLockUtils
import com.quran.labs.androidquran.util.QuranSettings
import com.quran.labs.androidquran.util.QuranUtils
import com.quran.labs.androidquran.util.ThemeUtil
import com.quran.mobile.di.ExtraPreferencesProvider
import com.quran.mobile.feature.downloadmanager.AudioManagerActivity
import com.quran.mobile.feature.pagetheme.PageThemeSheetWrapper
import com.quran.mobile.feature.pagetheme.pageThemeSummary
import dev.zacsweers.metro.Inject

class QuranSettingsFragment : PreferenceFragmentCompat() {

  @Inject
  lateinit var pageTypes: Map<@JvmSuppressWildcards String, @JvmSuppressWildcards PageProvider>

  @Inject
  lateinit var extraPreferences: Set<@JvmSuppressWildcards ExtraPreferencesProvider>

  override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
    addPreferencesFromResource(R.xml.quran_preferences)

    val appContext = requireContext().applicationContext

    // field injection
    (appContext as QuranApplication).applicationComponent.inject(this)

    if (!OrientationLockUtils.isOrientationLockSupported(resources.configuration)) {
      hideUnsupportedOrientationPreferences()
    }

    // handle Arabic names preference
    val arabicPref: Preference? = findPreference(ARABIC_KEY)
    if (arabicPref is CheckBoxPreference) {
      arabicPref.isChecked = isCurrentlyArabic()
    }

    arabicPref?.setOnPreferenceClickListener {
      val localeList = if (isCurrentlyArabic()) {
        val locales = LocaleListCompat.getDefault()
        val tags = locales.toLanguageTags().split(",")
          .filter { it != "ar" && !it.startsWith("ar-") }
        val tagString = tags.joinToString(",").ifEmpty { "en" }
        LocaleListCompat.forLanguageTags(tagString)
      } else {
        LocaleListCompat.forLanguageTags("ar-EG")
      }
      AppCompatDelegate.setApplicationLocales(localeList)
      true
    }

    // handle theme preference
    val themePref: Preference? = findPreference(Constants.PREF_APP_THEME)
    themePref?.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, newValue ->
      ThemeUtil.setTheme(newValue as String)
      true
    }
    val accent = androidx.preference.ListPreference(requireContext()).apply {
      key = "revelation_accent"
      title = getString(R.string.accent_color)
      dialogTitle = title
      isIconSpaceReserved = false
      entries = resources.getStringArray(R.array.accent_names)
      entryValues = arrayOf("blue", "emerald", "violet", "amber")
      setDefaultValue("blue")
      summaryProvider = androidx.preference.ListPreference.SimpleSummaryProvider.getInstance()
      setOnPreferenceChangeListener { _, value ->
        androidx.preference.PreferenceManager.getDefaultSharedPreferences(requireContext())
          .edit().putString(key, value as String).apply()
        com.quran.labs.androidquran.common.ui.core.QuranAccent.current =
          com.quran.labs.androidquran.AccentActivityCallbacks.selectedAccent(requireContext())
        requireActivity().recreate()
        true
      }
    }
    findPreference<PreferenceGroup>(getString(R.string.prefs_display_category_key))?.addPreference(accent)
    updatePageThemeSummary()

    // handle translation manager click
    val translationPref: Preference? = findPreference(Constants.PREF_TRANSLATION_MANAGER)
    translationPref?.setOnPreferenceClickListener {
      startActivity(Intent(activity, TranslationManagerActivity::class.java))
      true
    }

    // handle audio manager click
    val sajdahCue = androidx.preference.SwitchPreferenceCompat(requireContext()).apply {
      key = "revelation_sajdah_cue"
      title = getString(R.string.sajdah_cue_title)
      summary = getString(R.string.sajdah_cue_summary)
      setDefaultValue(true)
      isIconSpaceReserved = false
    }
    findPreference<PreferenceGroup>(getString(R.string.prefs_download_category_key))?.addPreference(sajdahCue)
    val warn = androidx.preference.SwitchPreferenceCompat(requireContext()).apply {
      key = "revelation_warn_manual_pause"
      title = getString(R.string.warn_manual_pause_title)
      summary = getString(R.string.warn_manual_pause_summary)
      setDefaultValue(false)
      isIconSpaceReserved = false
    }
    findPreference<PreferenceGroup>(getString(R.string.prefs_download_category_key))?.addPreference(warn)
    val audioManagerPref: Preference? = findPreference(Constants.PREF_AUDIO_MANAGER)
    audioManagerPref?.setOnPreferenceClickListener {
      startActivity(Intent(activity, AudioManagerActivity::class.java))
      true
    }

    val pageChangePref: Preference? = findPreference(Constants.PREF_PAGE_TYPE)
    if (pageTypes.size < 2 && pageChangePref != null) {
      val readingPrefs: Preference? = findPreference(Constants.PREF_READING_CATEGORY)
      (readingPrefs as PreferenceGroup).removePreference(pageChangePref)
    }

    // add additional injected preferences (if any)
    extraPreferences
      .sortedBy { it.order }
      .forEach { it.addPreferences(preferenceScreen) }
  }

  override fun onCreateView(
    inflater: LayoutInflater,
    container: ViewGroup?,
    savedInstanceState: Bundle?
  ): View {
    val view = super.onCreateView(inflater, container, savedInstanceState)
    val recyclerView = listView
    recyclerView.clipToPadding = false
    ViewCompat.setOnApplyWindowInsetsListener(recyclerView) { view, windowInsets ->
      val insets = windowInsets.getInsets(
        WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
      )
      recyclerView.updateLayoutParams<ViewGroup.LayoutParams> {
        // top, left, right are handled by QuranActivity
        view.setPadding(0, 0, 0, insets.bottom)
      }

      windowInsets
    }
    return view
  }

  override fun onPreferenceTreeClick(preference: Preference): Boolean {
    val key = preference.key
    if ("key_prefs_advanced" == key) {
      val intent = Intent(activity, QuranAdvancedPreferenceActivity::class.java)
      startActivity(intent)
      return true
    } else if (Constants.PREF_PAGE_TYPE == key) {
      val intent = Intent(activity, PageSelectActivity::class.java)
      startActivity(intent)
      return true
    } else if (Constants.PREF_PAGE_THEME == key) {
      val content = requireActivity().findViewById<ViewGroup>(android.R.id.content)
      lateinit var pageThemeSheet: PageThemeSheetWrapper
      pageThemeSheet = PageThemeSheetWrapper(
        requireContext(),
        QuranSettings.getInstance(requireContext())
      ) {
        content.removeView(pageThemeSheet)
        updatePageThemeSummary()
      }
      content.addView(
        pageThemeSheet,
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
      )
      return true
    }

    for (extraPref in extraPreferences) {
      if (extraPref.onPreferenceClick(preference)) {
        return true
      }
    }

    return super.onPreferenceTreeClick(preference)
  }

  override fun onDisplayPreferenceDialog(preference: Preference) {
    if (preference.key == "revelation_accent" && preference is androidx.preference.ListPreference) {
      com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
        .setTitle(preference.dialogTitle)
        .setSingleChoiceItems(preference.entries, preference.findIndexOfValue(preference.value)) { dialog, index ->
          val value = preference.entryValues[index].toString()
          if (preference.callChangeListener(value)) preference.value = value
          dialog.dismiss()
        }
        .setNegativeButton(com.quran.mobile.common.ui.core.R.string.cancel, null)
        .show()
    } else {
      super.onDisplayPreferenceDialog(preference)
    }
  }

  private fun updatePageThemeSummary() {
    findPreference<Preference>(Constants.PREF_PAGE_THEME)?.summary =
      pageThemeSummary(requireContext(), QuranSettings.getInstance(requireContext()))
  }

  private fun isCurrentlyArabic(): Boolean {
    val locale = QuranUtils.getCurrentLocale()
    return locale.language == "ar"
  }

  private fun hideUnsupportedOrientationPreferences() {
    val displayPrefs = findPreference<PreferenceGroup>(getString(R.string.prefs_display_category_key))
    val lockOrientationPref = findPreference<Preference>(Constants.PREF_LOCK_ORIENTATION)
    val landscapeOrientationPref = findPreference<Preference>(Constants.PREF_LANDSCAPE_ORIENTATION)

    if (displayPrefs != null && lockOrientationPref != null) {
      displayPrefs.removePreference(lockOrientationPref)
    }
    if (displayPrefs != null && landscapeOrientationPref != null) {
      displayPrefs.removePreference(landscapeOrientationPref)
    }
  }

  companion object {
    private const val ARABIC_KEY = "useArabicNames"
  }
}
