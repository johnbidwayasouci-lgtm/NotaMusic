package org.notamusic.app.domain.music

import org.notamusic.app.domain.model.Score
import java.io.InputStream
import java.io.OutputStream

interface ScoreRepository { fun list(): List<Score>; fun get(id: String): Score?; fun save(score: Score); fun delete(id: String) }
interface ScoreRenderer { fun render(score: Score, viewportWidth: Int, viewportHeight: Int): Any }
interface MusicXmlImporter { fun import(input: InputStream): Score }
interface MusicXmlExporter { fun export(score: Score, output: OutputStream) }
interface MidiRenderer { fun render(score: Score, output: OutputStream) }
interface PlaybackController { fun play(score: Score); fun pause(); fun stop(); fun setMuted(staffId: String, muted: Boolean); val isPlaying: Boolean }
interface ScorePersistence { fun load(id: String): Score?; fun save(score: Score); fun delete(id: String) }
interface FileManager { fun listScoreFiles(): List<String>; fun open(name: String): InputStream; fun create(name: String): OutputStream; fun delete(name: String): Boolean }
interface SettingsRepository { fun promptTone(): Boolean; fun autoScroll(): Boolean; fun highlightNewElements(): Boolean; fun showTouchLine(): Boolean; fun showMeasureNumbers(): Boolean; fun scoreScale(): Float; fun editScale(): Float }
