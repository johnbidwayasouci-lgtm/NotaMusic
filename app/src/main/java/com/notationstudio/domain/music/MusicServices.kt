package com.notationstudio.domain.music

import com.notationstudio.domain.model.Score

interface ScoreRepository { suspend fun list(): List<Score>; suspend fun get(id: String): Score?; suspend fun save(score: Score); suspend fun delete(id: String) }
interface ScoreRenderer { fun render(score: Score, width: Float, height: Float): Any }
interface MusicXmlImporter { fun import(xml: String): Score }
interface MusicXmlExporter { fun export(score: Score): String }
interface MidiRenderer { fun render(score: Score): ByteArray }
interface PlaybackController { fun play(score: Score); fun pause(); fun stop(); val isPlaying: Boolean }
interface ScorePersistence { suspend fun read(id: String): Score?; suspend fun write(score: Score); suspend fun remove(id: String) }
interface FileManager { fun listScoreFiles(): List<String>; fun read(path: String): ByteArray; fun write(path: String, data: ByteArray) }
interface SettingsRepository { fun get(key: String, default: String = ""): String; fun set(key: String, value: String) }
