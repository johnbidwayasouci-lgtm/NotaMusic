package org.notamusic.app.domain.notation

import java.math.BigInteger

@ConsistentCopyVisibility
data class Fraction private constructor(
    val n: BigInteger,
    val d: BigInteger
) : Comparable<Fraction> {
    init { require(d != BigInteger.ZERO) { "denominator must not be zero" } }

    operator fun plus(o: Fraction): Fraction = of(n * o.d + o.n * d, d * o.d)
    operator fun minus(o: Fraction): Fraction = of(n * o.d - o.n * d, d * o.d)
    operator fun times(o: Fraction): Fraction = of(n * o.n, d * o.d)
    operator fun div(o: Fraction): Fraction = of(n * o.d, d * o.n)
    operator fun unaryMinus(): Fraction = of(n.negate(), d)
    override fun compareTo(o: Fraction): Int = (n * o.d).compareTo(o.n * d)
    fun toDouble(): Double = n.toDouble() / d.toDouble()
    fun toLongExact(): Long {
        if (n.remainder(d) != BigInteger.ZERO) throw ArithmeticException("fraction is not an integer")
        val value = n.divide(d)
        if (value < BigInteger.valueOf(Long.MIN_VALUE) || value > BigInteger.valueOf(Long.MAX_VALUE)) {
            throw ArithmeticException("fraction does not fit in Long")
        }
        return value.toLong()
    }

    companion object {
        val ZERO = of(0, 1)
        val ONE = of(1, 1)
        val HALF = of(1, 2)
        val QUARTER = of(1, 4)
        val EIGHTH = of(1, 8)
        val SIXTEENTH = of(1, 16)
        val THIRTY_SECOND = of(1, 32)

        fun of(numerator: Long, denominator: Long): Fraction =
            of(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator))

        fun of(numerator: BigInteger, denominator: BigInteger): Fraction {
            require(denominator != BigInteger.ZERO) { "denominator must not be zero" }
            var a = numerator
            var b = denominator
            if (b.signum() < 0) { a = a.negate(); b = b.negate() }
            val g = a.gcd(b)
            return Fraction(a.divide(g), b.divide(g))
        }
    }
}

data class MusicalDuration(val value: Fraction, val dots: Int = 0) {
    fun total(): Fraction {
        var out = value
        var add = value
        repeat(dots.coerceIn(0, 2)) {
            add /= Fraction.of(2, 1)
            out += add
        }
        return out
    }
    companion object {
        val WHOLE = MusicalDuration(Fraction.ONE)
        val HALF = MusicalDuration(Fraction.HALF)
        val QUARTER = MusicalDuration(Fraction.QUARTER)
        val EIGHTH = MusicalDuration(Fraction.EIGHTH)
        val SIXTEENTH = MusicalDuration(Fraction.SIXTEENTH)
        val THIRTY_SECOND = MusicalDuration(Fraction.THIRTY_SECOND)
    }
}
