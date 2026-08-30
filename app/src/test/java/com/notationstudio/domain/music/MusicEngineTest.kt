package com.notationstudio.domain.music

import org.junit.Assert.*
import org.junit.Test

class MusicEngineTest {
    @Test fun fractions_reduce_and_operate() {
        val a=Fraction.of(2,4); val b=Fraction.of(1,4)
        assertEquals(Fraction.of(3,4),a+b); assertEquals(Fraction.of(1,4),a-b); assertEquals(Fraction.of(1,2),a/Fraction.of(1,1)); assertTrue(a>b)
    }
    @Test fun dotted_durations_are_exact() { assertEquals(Fraction.of(3,8),NotatedDuration(BaseDuration.EIGHTH,1).value); assertEquals(Fraction.of(7,16),NotatedDuration(BaseDuration.EIGHTH,2).value) }
    @Test fun tuplet_ratio_is_exact() { assertEquals(Fraction.of(2,3),TupletRatio(3,2).multiplier) }
}
