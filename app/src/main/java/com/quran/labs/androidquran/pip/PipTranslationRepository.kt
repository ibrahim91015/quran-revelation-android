package com.quran.labs.androidquran.pip

import android.content.Context
import androidx.core.text.HtmlCompat
import com.quran.data.di.AppScope
import com.quran.data.model.QuranText
import com.quran.data.model.SuraAyah
import com.quran.data.model.VerseRange
import com.quran.data.pageinfo.mapper.AyahMapper
import com.quran.labs.androidquran.database.DatabaseHandler
import com.quran.labs.androidquran.database.TranslationsDBAdapter
import com.quran.labs.androidquran.util.QuranFileUtils
import com.quran.labs.androidquran.util.QuranSettings
import com.quran.labs.androidquran.util.TranslationUtil
import com.quran.mobile.di.qualifier.ApplicationContext
import com.quran.mobile.translation.model.LocalTranslation
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@SingleIn(AppScope::class)
class PipTranslationRepository @Inject constructor(
  @ApplicationContext private val context: Context,
  private val settings: QuranSettings,
  private val translations: TranslationsDBAdapter,
  private val files: QuranFileUtils,
  private val mapper: AyahMapper
) {
  suspend fun load(ayah: SuraAyah): String? = withContext(Dispatchers.IO) {
    val selected = firstTranslation(translations.getTranslations().first(), settings.activeTranslations)
      ?: return@withContext null
    val range = VerseRange(ayah.sura, ayah.ayah, ayah.sura, ayah.ayah, 1)
    val handler = DatabaseHandler.getDatabaseHandler(context, selected.filename, files)
    val texts = mapper.mapKufiData(range, handler.getVerses(mapper.mapRange(range), DatabaseHandler.TextType.TRANSLATION))
    texts.firstOrNull()?.let(::plainText)
  }

  companion object {
    /** Match translation mode: sort priority, filter selected; empty selection enables all. */
    fun firstTranslation(translations: List<LocalTranslation>, active: Set<String?>): LocalTranslation? {
      val sorted = translations.sortedBy { it.displayOrder }
      return sorted.firstOrNull { it.filename in active } ?: sorted.firstOrNull()
    }
    fun plainText(verse: QuranText): String {
      val text = if (TranslationUtil.getHyperlinkAyahId(verse) != null) verse.extraData ?: verse.text else verse.text
      return HtmlCompat.fromHtml(text.replace(Regex("\\[\\[[\\s\\S]*?]]"), ""), HtmlCompat.FROM_HTML_MODE_LEGACY)
        .toString().trim()
    }
  }
}
