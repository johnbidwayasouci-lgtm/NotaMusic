package org.notamusic.core.notation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScorePersistenceTest {
 @Test fun roundTripPreservesMusicalState(){
  val m=Measure(1,TimeSignature(4,4)); val n=Note(start=Fraction.ZERO,duration=Duration(Fraction.QUARTER,1),voice=1,pitch=Pitch(PitchClass.C,4,Accidental.SHARP),tieStart=true,ornaments=setOf(Ornament.STACCATO),dynamic=Dynamic.MF,tuplet=TupletSpec(3,2)); m.voices[1]=mutableListOf(n); val s=Score(metadata=Metadata("Test","Sub","Composer","Rights","Source","Encoder"),tempo=Tempo(100),parts=mutableListOf(Part("p",Instrument("p","Piano",0),mutableListOf(Staff("Piano",Clef.TREBLE,mutableListOf(m))))),keySignature=KeySignature(2,false)); val codec=ScoreCodec(); val restored=codec.decode(codec.encode(s)).getOrThrow(); assertEquals(s.id,restored.id); assertEquals("Test",restored.metadata.title); assertEquals(100,restored.tempo.bpm); val r=restored.parts[0].staves[0].measures[0].voices[1][0] as Note; assertEquals(n.pitch,r.pitch); assertEquals(n.duration,r.duration); assertEquals(n.ornaments,r.ornaments); assertEquals(n.tuplet,r.tuplet); assertTrue(r.tieStart)
 }
}
