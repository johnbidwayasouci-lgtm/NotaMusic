package com.notationstudio.domain.music

import java.util.UUID

/** Stable, UI-independent temporal representation. One measure contains independent voice timelines. */
data class TemporalElement(
    val id: String = UUID.randomUUID().toString(),
    val kind: ElementKind,
    val position: Fraction,
    val duration: Fraction,
    val pitch: Pitch? = null,
    val dots: Int = 0,
    val tuplet: TupletRatio? = null,
    val articulation: Articulation? = null,
    val ornament: OrnamentKind? = null,
    val dynamic: DynamicKind? = null
) {
    init { require(position >= Fraction.ZERO); require(duration > Fraction.ZERO); require(dots in 0..2) }
}

enum class ElementKind { NOTE, REST, GRACE_NOTE }
enum class Articulation { STACCATO, SHORT_STACCATO, ACCENT, FERMATA }
en
enum class OrnamentKind { TRILL, MORDENT, INVERTED_MORDENT, TURN, INVERTED_TURN, TREMOLO }
en
enum class DynamicKind { PPP, PP, P, MP, MF, F, FF, FFF }

data class Pitch(val step: Step, val octave: Int, val accidental: Accidental = Accidental.NATURAL)
enum class Step { C, D, E, F, G, A, B }
enum class Accidental { SHARP, FLAT, NATURAL, DOUBLE_SHARP, DOUBLE_FLAT }

data class TimeSignature(val numerator: Int, val denominator: Int) {
    init { require(numerator > 0 && denominator > 0 && denominator and (denominator - 1) == 0) }
    val duration: Fraction get() = Fraction.of(numerator.toLong(), denominator.toLong())
}

data class TemporalMeasure(
    val number: Int,
    val timeSignature: TimeSignature,
    val voices: Map<Int, List<TemporalElement>> = emptyMap()
) {
    val capacity get() = timeSignature.duration
    fun voice(number: Int) = voices[number].orEmpty().sortedBy { it.position }
}

sealed interface EditResult { data class Success<T>(val value: T): EditResult; data class Failure(val reason: String): EditResult }

class TemporalScoreEditor(private val measureProvider: (Int) -> TemporalMeasure) {
    fun insert(measureNumber: Int, voice: Int, element: TemporalElement): EditResult {
        val measure = measureProvider(measureNumber)
        val existing = measure.voice(voice)
        val end = element.position + element.duration
        if (end > measure.capacity) return EditResult.Failure("element exceeds measure capacity")
        if (existing.any { element.position < it.position + it.duration && end > it.position }) return EditResult.Failure("element overlaps existing content")
        return EditResult.Success(element)
    }
    fun delete(measureNumber: Int, voice: Int, id: String): EditResult = if (measureProvider(measureNumber).voice(voice).any { it.id == id }) EditResult.Success(id) else EditResult.Failure("element not found")
    fun modify(measureNumber: Int, voice: Int, replacement: TemporalElement): EditResult = insert(measureNumber, voice, replacement)
}
