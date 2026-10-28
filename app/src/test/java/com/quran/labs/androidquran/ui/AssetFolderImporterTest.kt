package com.quran.labs.androidquran.ui

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth.assertThat
import com.quran.labs.androidquran.base.TestApplication
import com.quran.labs.androidquran.ui.fragment.QuranAdvancedSettingsFragment
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowContentResolver
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], application = TestApplication::class)
class AssetFolderImporterTest {
  @Test fun selectedFolderCopiesValidAssetsKeepsExistingFilesAndRejectsUnrelatedOrBrokenFiles() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<TestApplication>()
    val fragment = QuranAdvancedSettingsFragment()
    context.applicationComponent.inject(fragment)
    val source = File(context.cacheDir, "export").apply { mkdirs() }
    val targetAudio = File(fragment.quranFileUtils.getQuranAudioDirectory(context)!!)
    val destination = File(targetAudio, "import-test/108.mp3").apply {
      parentFile.mkdirs(); writeText("existing download")
    }
    File(source, "audio/import-test/108.mp3").apply { parentFile.mkdirs(); writeText("ID3-audio") }
    File(source, "audio/import-test/109.opus").writeText("OggS-audio")
    File(source, "audio/import-test/broken.mp3").writeText("not audio")
    File(source, "audio/import-test/settings.xml").writeText("unrelated")
    val translation = File(source, "databases/import-test.db").apply { parentFile.mkdirs() }
    // A closed, checkpointed on-disk SQLite fixture, as supplied by a folder export.
    translation.writeBytes(javaClass.getResourceAsStream("/import-translation.db")!!.use { it.readBytes() })
    assertThat(fragment.assetImporter.validDatabase(translation, timing = false)).isTrue()
    File(source, "databases/broken.db").writeText("not SQLite")
    val provider = ExportProvider(source).apply {
      attachInfo(context, android.content.pm.ProviderInfo().apply {
        authority = "import-test"
        exported = true
        grantUriPermissions = true
      })
    }
    ShadowContentResolver.registerProviderInternal("import-test", provider)
    val result = fragment.assetImporter.importFolder(Uri.parse("content://import-test/tree/root"))
    assertThat(destination.readText()).isEqualTo("existing download")
    assertThat(File(targetAudio, "import-test/109.opus").readText()).isEqualTo("OggS-audio")
    assertThat(File(targetAudio, "import-test/settings.xml").exists()).isFalse()
    assertThat(File(targetAudio, "import-test/broken.mp3").exists()).isFalse()
    assertThat(File(fragment.quranFileUtils.getQuranDatabaseDirectory(), "import-test.db").exists()).isTrue()
    assertThat(File(fragment.quranFileUtils.getQuranDatabaseDirectory(), "broken.db").exists()).isFalse()
    assertThat(File(source, "audio/import-test/108.mp3").readText()).isEqualTo("ID3-audio")
    assertThat(result.copied).isEqualTo(2)
    assertThat(fragment.assetImporter.importFolder(Uri.parse("content://import-test/tree/root")).copied).isEqualTo(0)
    assertThat(targetAudio.walkTopDown().none { it.extension == "part" }).isTrue()
  }

  private class ExportProvider(private val root: File) : ContentProvider() {
    override fun onCreate() = true
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?,
      selectionArgs: Array<out String>?, sortOrder: String?): Cursor {
      val id = DocumentsContract.getDocumentId(uri)
      val relative = id.removePrefix("root").trimStart('/')
      val parent = File(root, relative)
      return MatrixCursor(projection!!).apply {
        parent.listFiles()?.forEach { file -> addRow(arrayOf(
          "$id/${file.name}", file.name,
          if (file.isDirectory) DocumentsContract.Document.MIME_TYPE_DIR else "application/octet-stream"
        )) }
        // A dishonest provider must not be able to escape the destination.
        addRow(arrayOf("malicious", "../outside.mp3", "audio/mpeg"))
      }
    }
    override fun openFile(uri: Uri, mode: String) = ParcelFileDescriptor.open(
      File(root, DocumentsContract.getDocumentId(uri).removePrefix("root/")), ParcelFileDescriptor.MODE_READ_ONLY)
  }
}
