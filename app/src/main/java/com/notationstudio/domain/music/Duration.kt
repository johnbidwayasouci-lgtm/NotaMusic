package com.notationstudio.domain.music

enum class BaseDuration(val value: Fraction) { WHOLE(Fraction.WHOLE), HALF(Fraction.HALF), QUARTER(Fraction.QUARTER), EIGHTH(Fraction.of(1,8)), SIXTEENTH(Fraction.of(1,16)), THIRTY_SECOND(Fraction.of(1,32)) }

data class NotatedDuration(val base: BaseDuration, val dots: Int = 0) {
    init { require(dots in 0..2) }
    val value: Fraction get() { var total=base.value; var add=base.value; repeat(dots){add=add/Fraction.of(2,1); total += add}; return total }
}

data class TupletRatio(val actual: Int, val normal: Int) { init { require(actual > 0 && normal > 0) }; val multiplier get() = Fraction.of(normal.toLong(), actual.toLong()) }
