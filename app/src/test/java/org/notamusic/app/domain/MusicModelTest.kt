package org.notamusic.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import org.notamusic.app.domain.model.*

class MusicModelTest { @Test fun noteCarriesMusicalState() { val n=Note(60,4,Duration.QUARTER,dotted=true,voice=2,onset=4); assertEquals(2,n.voice); assertEquals(4,n.onset) } }
