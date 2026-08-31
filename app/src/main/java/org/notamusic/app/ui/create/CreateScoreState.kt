package org.notamusic.app.ui.create

import org.notamusic.app.domain.model.*

data class CreateScoreDraft(val metadata: Metadata = Metadata(), val instruments: List<Instrument> = listOf(Instrument("piano", "Piano", 0)), val keySignature: KeySignature = KeySignature(), val timeSignature: TimeSignature = TimeSignature(), val tempo: Tempo = Tempo(), val pickupBeats: Int = 0)
enum class CreateStep { Metadata, Instruments, MusicalSetup }
data class CreateScoreState(val draft: CreateScoreDraft = CreateScoreDraft(), val step: CreateStep = CreateStep.Metadata, val saving: Boolean = false, val error: String? = null)
