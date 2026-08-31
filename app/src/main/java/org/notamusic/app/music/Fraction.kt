package org.notamusic.app.music

import java.math.BigInteger

/** Exact non-negative/negative rational number used for musical time. */
@ConsistentCopyVisibility
data class Fraction private constructor(
    val numerator: BigInteger,
    val denominator: BigInteger
) : Comparable<Fraction> {
    init {
        require(denominator != BigInteger.ZERO) { "denominator must not be zero" }
    }

    operator fun plus(other: Fraction): Fraction = of(
        numerator * other.denominator + other.numerator * denominator,
        denominator * other.denominator
    )

    operator fun minus(other: Fraction): Fraction = of(
        numerator * other.denominator - other.numerator * denominator,
        denominator * other.denominator
    )

    operator fun times(other: Fraction): Fraction = of(numerator * other.numerator, denominator * other.denominator)
    operator fun div(other: Fraction): Fraction = of(numerator * other.denominator, denominator * other.numerator)

    operator fun unaryMinus(): Fraction = of(numerator.negate(), denominator)

    override fun compareTo(other: Fraction): Int =
        (numerator * other.denominator).compareTo(other.numerator * denominator)

    fun toDouble(): Double = numerator.toDouble() / denominator.toDouble()
    fun toLongExact(): Long {
        val value = numerator.divide(denominator)
        if (value < BigInteger.valueOf(Long.MIN_VALUE) || value > BigInteger.valueOf(Long.MAX_VALUE)) {
            throw ArithmeticException("fraction does not fit in Long")
        }
        return value.toLong()
    }

    companion object {
        val ZERO: Fraction = of(0, 1)
        val ONE: Fraction = of(1, 1)

        fun of(numerator: Long, denominator: Long): Fraction =
            of(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator))

        fun of(numerator: BigInteger, denominator: BigInteger): Fraction {
            require(denominator != BigInteger.ZERO) { "denominator must not be zero" }
            var n = numerator
            var d = denominator
            if (d.signum() < 0) {
                n = n.negate()
                d = d.negate()
            }
            val gcd = n.gcd(d)
            return Fraction(n.divide(gcd), d.divide(gcd))
        }
    }
}
