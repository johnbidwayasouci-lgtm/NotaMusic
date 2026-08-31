package org.notamusic.app.data.files

import android.content.Context
import org.notamusic.app.domain.music.FileManager
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class AppFileManager(private val context: Context) : FileManager {
    private fun file(name: String) = File(context.filesDir, name)
    override fun listScoreFiles() = context.filesDir.listFiles()?.filter { it.extension == "xml" }?.map { it.name } ?: emptyList()
    override fun open(name: String): InputStream = file(name).inputStream()
    override fun create(name: String): OutputStream = file(name).outputStream()
    override fun delete(name: String) = file(name).delete()
}
