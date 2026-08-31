package org.notamusic.app.domain.notation

import java.math.BigInteger

data class Fraction private constructor(val n: BigInteger, val d: BigInteger): Comparable<Fraction> {
    operator fun plus(o:Fraction)=of(n*o.d+o.n*d,d*o.d)
    operator fun minus(o:Fraction)=of(n*o.d-o.n*d,d*o.d)
    operator fun times(o:Fraction)=of(n*o.n,d*o.d)
    operator fun div(o:Fraction)=of(n*o.d,d*o.n)
    override fun compareTo(o:Fraction)= (n*o.d).compareTo(o.n*d)
    fun toDouble()=n.toDouble()/d.toDouble()
    companion object { val ZERO=of(0,1); val ONE=of(1,1); val HALF=of(1,2); val QUARTER=of(1,4); val EIGHTH=of(1,8); val SIXTEENTH=of(1,16); val THIRTY_SECOND=of(1,32)
        fun of(n:Long,d:Long):Fraction { require(d!=0); var a=BigInteger.valueOf(n); var b=BigInteger.valueOf(d); if(b.signum()<0){a=a.negate();b=b.negate()}; val g=a.gcd(b); return Fraction(a/g,b/g) }
    }
}

data class MusicalDuration(val value:Fraction, val dots:Int=0) {
    fun total():Fraction { var out=value; var add=value; repeat(dots){ add=add/Fraction.of(2,1); out+=add }; return out }
    companion object { val WHOLE=MusicalDuration(Fraction.ONE); val HALF=MusicalDuration(Fraction.HALF); val QUARTER=MusicalDuration(Fraction.QUARTER); val EIGHTH=MusicalDuration(Fraction.EIGHTH); val SIXTEENTH=MusicalDuration(Fraction.SIXTEENTH); val THIRTY_SECOND=MusicalDuration(Fraction.THIRTY_SECOND) }
}
