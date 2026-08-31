package org.notamusic.app.midi

import org.notamusic.app.domain.model.Score
import org.notamusic.app.domain.music.MidiRenderer
import java.io.OutputStream

class PlaceholderMidiRenderer : MidiRenderer { override fun render(score: Score, output: OutputStream) { output.write(byteArrayOf(0x4d,0x54,0x68,0x64,0,0,0,6,0,0,0,1,0,96)) } }
