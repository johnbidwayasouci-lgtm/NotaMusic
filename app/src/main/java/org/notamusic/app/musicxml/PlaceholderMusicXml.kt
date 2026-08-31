package org.notamusic.app.musicxml

import org.notamusic.app.domain.model.Score
import org.notamusic.app.domain.music.MusicXmlExporter
import org.notamusic.app.domain.music.MusicXmlImporter
import java.io.InputStream
import java.io.OutputStream

class PlaceholderMusicXml : MusicXmlImporter, MusicXmlExporter {
    override fun import(input: InputStream): Score = Score()
    override fun export(score: Score, output: OutputStream) { output.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><score-partwise version=\"3.0\"></score-partwise>".toByteArray()) }
}
