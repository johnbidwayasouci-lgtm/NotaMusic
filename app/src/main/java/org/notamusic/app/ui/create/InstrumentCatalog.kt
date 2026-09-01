package org.notamusic.app.ui.create

import org.notamusic.app.domain.model.Instrument

data class InstrumentOption(val instrument: Instrument, val family: String, val grandStaff: Boolean = false, val transposition: Int = 0)

object InstrumentCatalog {
    val options: List<InstrumentOption> = listOf(
        InstrumentOption(Instrument("violin", "Violin", 40), "Strings"),
        InstrumentOption(Instrument("viola", "Viola", 41), "Strings"),
        InstrumentOption(Instrument("cello", "Cello", 42), "Strings"),
        InstrumentOption(Instrument("double_bass", "Double Bass", 43), "Strings", transposition = -12),
        InstrumentOption(Instrument("flute", "Flute", 73), "Woodwinds"),
        InstrumentOption(Instrument("oboe", "Oboe", 68), "Woodwinds"),
        InstrumentOption(Instrument("clarinet", "Clarinet", 71), "Woodwinds", transposition = 2),
        InstrumentOption(Instrument("bassoon", "Bassoon", 70), "Woodwinds"),
        InstrumentOption(Instrument("saxophone", "Saxophone", 65), "Woodwinds", transposition = 2),
        InstrumentOption(Instrument("trumpet", "Trumpet", 56), "Brass", transposition = 2),
        InstrumentOption(Instrument("horn", "Horn", 60), "Brass", transposition = 7),
        InstrumentOption(Instrument("trombone", "Trombone", 57), "Brass"),
        InstrumentOption(Instrument("tuba", "Tuba", 58), "Brass"),
        InstrumentOption(Instrument("piano", "Piano", 0), "Keyboards", grandStaff = true),
        InstrumentOption(Instrument("organ", "Organ", 16), "Keyboards", grandStaff = true),
        InstrumentOption(Instrument("percussion", "Percussion", 0), "Percussion"),
        InstrumentOption(Instrument("drum_kit", "Drum Kit", 0), "Percussion"),
        InstrumentOption(Instrument("soprano", "Soprano", 52), "Voices"),
        InstrumentOption(Instrument("alto", "Alto", 52), "Voices"),
        InstrumentOption(Instrument("tenor", "Tenor", 52), "Voices"),
        InstrumentOption(Instrument("bass", "Bass", 52), "Voices"),
        InstrumentOption(Instrument("choir", "Choir", 52), "Voices"),
        InstrumentOption(Instrument("voice", "Voice", 52), "Voices")
    )
    val grouped: Map<String, List<InstrumentOption>> = options.groupBy { it.family }
}
