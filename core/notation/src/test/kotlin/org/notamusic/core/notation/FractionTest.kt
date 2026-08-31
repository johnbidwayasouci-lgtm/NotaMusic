package org.notamusic.core.notation

import kotlin.test.Test
import kotlin.test.assertEquals

class FractionTest {
 @Test fun arithmeticReduces(){ assertEquals(Fraction.of(5,6), Fraction.of(1,2)+Fraction.of(1,3)); assertEquals(Fraction.of(1,6),Fraction.of(1,2)-Fraction.of(1,3)) }
 @Test fun ticksAreExact(){ assertEquals(480, Fraction.ONE.toTicks()); assertEquals(240,Fraction.HALF.toTicks()) }
}
