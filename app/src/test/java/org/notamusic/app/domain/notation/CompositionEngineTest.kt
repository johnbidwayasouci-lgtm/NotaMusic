package org.notamusic.app.domain.notation

import org.junit.Assert.*
import org.junit.Test

class CompositionEngineTest {
 @Test fun fractionIsExact(){assertEquals(Fraction.of(5,6),Fraction.of(1,2)+Fraction.of(1,3));assertEquals(Fraction.of(1,6),Fraction.of(1,2)-Fraction.of(1,3))}
 @Test fun rejectsOverflow(){val e=CompositionEngine();assertTrue(e.add(0,Fraction.of(3,4),MusicalDuration.QUARTER).isFailure)}
 @Test fun allowsFourQuarterNotes(){val e=CompositionEngine();repeat(4){assertTrue(e.add(0,Fraction.of(it.toLong(),4),MusicalDuration.QUARTER).isSuccess)};assertEquals(4,e.measures[0].events.size)}
 @Test fun undoRedoRestoresEvent(){val e=CompositionEngine();assertTrue(e.add(0,Fraction.ZERO,MusicalDuration.QUARTER).isSuccess);assertTrue(e.removeSelected());assertEquals(0,e.measures[0].events.size);assertTrue(e.undo());assertEquals(1,e.measures[0].events.size);assertTrue(e.redo());assertEquals(0,e.measures[0].events.size)}
 @Test fun voicesMayOccupySameTime(){val e=CompositionEngine();assertTrue(e.add(0,Fraction.ZERO,MusicalDuration.WHOLE).isSuccess);e.voice=2;assertTrue(e.add(0,Fraction.ZERO,MusicalDuration.WHOLE).isSuccess)}
 @Test fun dottedQuarterIsThreeEighths(){assertEquals(Fraction.of(3,8),MusicalDuration(Fraction.QUARTER,1).total())}
 @Test fun tupletSpecIsRepresentable(){assertEquals(TupletSpec(5,4),TupletSpec(5,4))}
}
