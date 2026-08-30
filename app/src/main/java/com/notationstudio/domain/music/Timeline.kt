package com.notationstudio.domain.music

import com.notationstudio.domain.model.MusicElement
import com.notationstudio.domain.model.Measure

interface TimedElement { val position: Fraction; val duration: Fraction }

data class TimedMusicElement(val element: MusicElement, override val position: Fraction, override val duration: Fraction): TimedElement

data class VoiceTimeline(val voice: Int, val elements: List<TimedMusicElement>) {
    fun ordered() = elements.sortedBy { it.position }
    fun endPosition(): Fraction = ordered().fold(Fraction.ZERO) { end, e -> maxOf(end, e.position + e.duration) }
    fun overlaps(candidate: TimedMusicElement) = ordered().any { candidate.position < it.position + it.duration && it.position + candidate.duration > it.position }
}

object MeasureTiming {
    fun theoreticalDuration(measure: Measure): Fraction = Fraction.of(measure.timeSignature.numerator.toLong(), measure.timeSignature.denominator.toLong())
    fun canInsert(measure: Measure, voice: VoiceTimeline, candidate: TimedMusicElement): Boolean = candidate.position >= Fraction.ZERO && candidate.position + candidate.duration <= theoreticalDuration(measure) && !voice.overlaps(candidate)
    fun nextPosition(voice: VoiceTimeline): Fraction = voice.endPosition()
}
