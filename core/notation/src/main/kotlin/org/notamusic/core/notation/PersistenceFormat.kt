package org.notamusic.core.notation

import java.time.Instant
import java.util.Base64

/** Versioned, deterministic text format used as the local recovery/persistence boundary. */
object PersistenceFormat {
 const val SCHEMA_VERSION = 1
 fun encode(score: Score, createdAt: Long = Instant.now().toEpochMilli(), modifiedAt: Long = createdAt): String = buildString {
  appendLine("notamusic-score|$SCHEMA_VERSION|${score.id}|$createdAt|$modifiedAt")
  appendLine("meta|${b(score.metadata.title)}|${b(score.metadata.subtitle)}|${b(score.metadata.composer)}|${b(score.metadata.copyright)}|${b(score.metadata.source)}|${b(score.metadata.encoder)}")
  appendLine("tempo|${score.tempo.bpm}|key|${score.keySignature.fifths}|${score.keySignature.minor}")
  score.parts.forEachIndexed { pi,p -> appendLine("part|$pi|${b(p.id)}|${b(p.instrument.id)}|${b(p.instrument.name)}|${p.instrument.midiProgram}"); p.staves.forEachIndexed { si,s ->
   appendLine("staff|$pi|$si|${b(s.name)}|${s.clef}|${s.mute}|${s.volume}")
   s.measures.forEach { m -> appendLine("measure|$pi|$si|${m.number}|${m.timeSignature.beats}|${m.timeSignature.beatUnit}|${m.barline.repeatStart}|${m.barline.repeatEnd}"); m.voices.forEach { (v,es) -> es.forEach { e -> when(e) {
    is Note -> appendLine("note|$pi|$si|${m.number}|$v|${e.id}|${e.start}|${e.duration.base}|${e.duration.dots}|${e.pitch.step}|${e.pitch.octave}|${e.pitch.accidental}|${e.tieStart}|${e.tieStop}|${e.dynamic}|${e.ornaments.joinToString(",")}|${e.tuplet?.actual ?: 0}|${e.tuplet?.normal ?: 0}")
    is Rest -> appendLine("rest|$pi|$si|${m.number}|$v|${e.id}|${e.start}|${e.duration.base}|${e.duration.dots}")
    else -> appendLine("element|${e::class.simpleName}|$pi|$si|${m.number}|$v|${e.id}|${e.start}|${e.duration.base}|${e.duration.dots}")
   } } } }
  } }
 }
 private fun b(s:String)=Base64.getEncoder().encodeToString(s.toByteArray(Charsets.UTF_8))
}
