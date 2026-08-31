package org.notamusic.core.notation

import java.math.BigInteger

data class Fraction private constructor(val numerator: BigInteger, val denominator: BigInteger) : Comparable<Fraction> {
    init { require(denominator.signum() != 0) }
    operator fun plus(other: Fraction) = of(numerator * other.denominator + other.numerator * denominator, denominator * other.denominator)
    operator fun minus(other: Fraction) = of(numerator * other.denominator - other.numerator * denominator, denominator * other.denominator)
    operator fun times(other: Fraction) = of(numerator * other.numerator, denominator * other.denominator)
    operator fun div(other: Fraction) = of(numerator * other.denominator, denominator * other.numerator)
    override fun compareTo(other: Fraction): Int = (numerator * other.denominator).compareTo(other.numerator * denominator)
    fun toDouble() = numerator.toDouble() / denominator.toDouble()
    fun toTicks(ppq: Int = 480): Long = (this * of(ppq.toLong(), 1)).numerator.divide((this * of(ppq.toLong(), 1)).denominator).toLong()
    override fun toString() = if (denominator == BigInteger.ONE) numerator.toString() else "$numerator/$denominator"
    companion object {
        val ZERO = of(0, 1); val ONE = of(1, 1); val HALF = of(1, 2); val QUARTER = of(1, 4)
        fun of(n: Long, d: Long): Fraction { require(d != 0); var nn=BigInteger.valueOf(n); var dd=BigInteger.valueOf(d); if(dd.signum()<0){nn=nn.negate();dd=dd.negate()}; val g=nn.gcd(dd); return Fraction(nn/g,dd/g) }
    }
}
