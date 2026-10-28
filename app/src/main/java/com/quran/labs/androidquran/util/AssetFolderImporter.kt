package com.quran.labs.androidquran.util

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.provider.DocumentsContract
import com.quran.mobile.di.qualifier.ApplicationContext
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

/** Copies an explicitly selected export. Never opens or changes another app's private storage. */
class AssetFolderImporter @Inject constructor(
  @ApplicationContext private val context: Context,
  private val files: QuranFileUtils
) {
  data class Result(val copied: Int, val skipped: Int)

  suspend fun importFolder(tree: Uri): Result = mutex.withLock {
    withContext(Dispatchers.IO) {
      val audio = files.getQuranAudioDirectory(context)?.let(::File)
        ?: throw IOException("Audio storage is unavailable")
      val databases = files.getQuranDatabaseDirectory()
      var copied = 0
      var skipped = 0
      var visited = 0
      val resolver = context.contentResolver

      suspend fun visit(id: String, parts: List<String>) {
        currentCoroutineContext().ensureActive()
        require(parts.size <= 8 && ++visited <= 100_000) { "Folder is too large or deeply nested" }
        val children = DocumentsContract.buildChildDocumentsUriUsingTree(tree, id)
        resolver.query(children, arrayOf(
          DocumentsContract.Document.COLUMN_DOCUMENT_ID,
          DocumentsContract.Document.COLUMN_DISPLAY_NAME,
          DocumentsContract.Document.COLUMN_MIME_TYPE
        ), null, null, null)?.use { cursor ->
          while (cursor.moveToNext()) {
            currentCoroutineContext().ensureActive()
            val childId = cursor.getString(0)
            val name = cursor.getString(1) ?: continue
            if (name.isBlank() || name == "." || name == ".." || '/' in name || '\\' in name) {
              skipped++
              continue
            }
            val path = parts + name
            if (cursor.getString(2) == DocumentsContract.Document.MIME_TYPE_DIR) {
              // The selected Quran folder contains audio/ and databases/.
              if (parts.isNotEmpty() || name in setOf("audio", "databases")) visit(childId, path)
              continue
            }
            val root = when (path.firstOrNull()) {
              "audio" -> audio
              "databases" -> databases
              else -> { skipped++; continue }
            }
            val extension = name.substringAfterLast('.', "").lowercase()
            if (extension !in setOf("mp3", "opus", "db") ||
              (root == databases && (extension != "db" || path.size != 2))) {
              skipped++
              continue
            }
            val destination = path.drop(1).fold(root) { parent, part -> File(parent, part) }
            require(destination.canonicalPath.startsWith(root.canonicalPath + File.separator))
            // Existing downloads, open databases, and user data always win.
            if (destination.exists()) { skipped++; continue }
            check(destination.parentFile?.let { it.isDirectory || it.mkdirs() } == true)
            val temporary = File.createTempFile(".import-", ".part", destination.parentFile)
            try {
              val uri = DocumentsContract.buildDocumentUriUsingTree(tree, childId)
              resolver.openInputStream(uri)?.use { input ->
                temporary.outputStream().use { output ->
                  val buffer = ByteArray(64 * 1024)
                  var total = 0L
                  while (true) {
                    currentCoroutineContext().ensureActive()
                    val count = input.read(buffer)
                    if (count == -1) break
                    total += count
                    require(total <= 1_073_741_824) { "File is too large" }
                    output.write(buffer, 0, count)
                  }
                }
              } ?: throw IOException("Cannot read selected file")
              val valid = if (extension == "db") validDatabase(temporary, root == audio)
              else validAudio(temporary, extension)
              if (valid && !destination.exists() && temporary.renameTo(destination)) copied++
              else skipped++
            } finally {
              temporary.delete()
            }
          }
        } ?: throw IOException("Cannot read selected folder")
      }
      visit(DocumentsContract.getTreeDocumentId(tree), emptyList())
      Result(copied, skipped)
    }
  }

  internal fun validDatabase(file: File, timing: Boolean): Boolean = try {
    SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
      val healthy = db.rawQuery("PRAGMA quick_check(1)", null).use { it.moveToFirst() && it.getString(0) == "ok" }
      val query = if (timing) "SELECT sura, ayah, time FROM timings LIMIT 1"
      else "SELECT sura, ayah, text FROM verses LIMIT 1"
      healthy && db.rawQuery(query, null).use { it.moveToFirst() }
    }
  } catch (_: RuntimeException) { false }

  private fun validAudio(file: File, extension: String): Boolean {
    val header = ByteArray(4)
    if (file.inputStream().use { it.read(header) } != 4) return false
    return if (extension == "opus") header.contentEquals("OggS".toByteArray())
    else String(header, 0, 3, Charsets.US_ASCII) == "ID3" ||
      (header[0].toInt() and 0xff == 0xff && header[1].toInt() and 0xe0 == 0xe0)
  }

  companion object { private val mutex = Mutex() }
}
