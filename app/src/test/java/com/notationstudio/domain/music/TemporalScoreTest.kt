package com.notationstudio.domain.music

import org.junit.Assert.*
import org.junit.Test

class TemporalScoreTest {
    private fun editor(elements: List<TemporalElement> = emptyList()) = TemporalScoreEditor { TemporalMeasure(1, TimeSignature(4,4), mapOf(1 to elements)) }
    @Test fun rejects_overflow() { val r=editor().insert(1,1,TemporalElement(kind=ElementKind.NOTE,position=Fraction.of(15,16),duration=Fraction.QUARTER,pitch=Pitch(Step.C,4))); assertTrue(r is EditResult.Failure) }
    @Test fun rejects_overlap() { val n=TemporalElement(kind=ElementKind.NOTE,position=Fraction.ZERO,duration=Fraction.QUARTER,pitch=Pitch(Step.C,4)); val r=editor(listOf(n)).insert(1,1,TemporalElement(kind=ElementKind.NOTE,position=Fraction.of(1,8),duration=Fraction.QUARTER,pitch=Pitch(Step.D,4))); assertTrue(r is EditResult.Failure) }
    @Test fun accepts_exact_fill() { val r=editor().insert(1,1,TemporalElement(kind=ElementKind.NOTE,position=Fraction.ZERO,duration=Fraction.WHOLE,pitch=Pitch(Step.C,4))); assertTrue(r is EditResult.Success<*>) }
    @Test fun time_signature_is_exact() { assertEquals(Fraction.of(6,8), TimeSignature(6,8).duration) }
}
