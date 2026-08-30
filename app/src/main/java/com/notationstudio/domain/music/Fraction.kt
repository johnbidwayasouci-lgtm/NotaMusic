package com.notationstudio.domain.music

import kotlin.math.abs

data class Fraction private constructor(val numerator: Long, val denominator: Long) : Comparable<Fraction> {
    init { require(denominator > 0) }
    operator fun plus(o: Fraction) = of(numerator * o.denominator + o.numerator * denominator, denominator * o.denominator)
    operator fun minus(o: Fraction) = of(numerator * o.denominator - o.numerator * denominator, denominator * o.denominator)
    operator fun times(o: Fraction) = of(numerator * o.numerator, denominator * o.denominator)
    operator fun div(o: Fraction) = of(numerator * o.denominator, denominator * o.numerator)
    override fun compareTo(o: Fraction): Int = (numerator * o.denominator).compareTo(o.numerator * denominator)
    fun toDouble() = numerator.toDouble() / denominator
    fun toTicks(ppq: Long = 480) = numerator * ppq / denominator
    companion object {
        val ZERO = of(0, 1); val WHOLE = of(1, 1); val HALF = of(1, 2); val QUARTER = of(1, 4)
        fun of(n: Long, d: Long): Fraction { require(d != 0); val sign = if (d < 0) -1 else 1; val g = gcd(abs(n), abs(d)); return Fraction(sign * n / g, sign * d / g) }
        private fun gcd(a: Long, b: Long): Long { var x=a; var y=b; while(y!=0L){val t=x%y;x=y;y=t}; return if(x==0L) 1L else x }
    }
}
