package org.notamusic.app.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.notamusic.app.data.persistence.ScoreFileStore
import org.notamusic.app.domain.model.*
import org.notamusic.app.domain.music.ScoreFactory
import org.notamusic.app.ui.create.CreateScoreDraft
import org.notamusic.app.ui.create.InstrumentCatalog
import org.notamusic.app.ui.create.InstrumentOption
import org.notamusic.app.ui.editor.ScoreNotationView
import org.notamusic.app.ui.editor.ScoreSession
import java.io.File
import java.io.FileOutputStream

private fun Context.dp(v: Int) = (v * resources.displayMetrics.density).toInt()
private fun TextView.heading() { setTextColor(Color.rgb(31, 33, 37)); typeface = Typeface.DEFAULT_BOLD }
private fun Button.actionStyle() { minHeight = context.dp(46); textSize = 14f; isAllCaps = false }
private fun LinearLayout.card(context: Context) = LinearLayout(context).apply { orientation = LinearLayout.VERTICAL; setPadding(context.dp(22), context.dp(18), context.dp(22), context.dp(18)); setBackgroundColor(Color.WHITE) }

class ScoreCreationActivity : AppCompatActivity() {
    private var page = 0
    private lateinit var flipper: ViewFlipper
    private lateinit var next: Button
    private lateinit var back: Button
    private lateinit var titleField: EditText
    private lateinit var composerField: EditText
    private lateinit var tempoField: EditText
    private lateinit var marking: Spinner
    private lateinit var meter: Spinner
    private lateinit var key: Spinner
    private lateinit var pickup: Spinner
    private val selected = linkedMapOf<String, InstrumentOption>()

    override fun onCreate(state: Bundle?) { super.onCreate(state); requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE; title = "New score"; build() }

    private fun build() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(Color.rgb(244, 241, 235)) }
        val header = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL; setPadding(dp(24), dp(10), dp(24), dp(10)); setBackgroundColor(Color.rgb(38, 40, 44)) }
        header.addView(TextView(this).apply { text = "New score"; textSize = 22f; setTextColor(Color.WHITE); typeface = Typeface.DEFAULT_BOLD }, LinearLayout.LayoutParams(0, dp(48), 1f))
        header.addView(TextView(this).apply { text = "1  INFO   ·   2  INSTRUMENTS   ·   3  MUSICAL SETUP"; textSize = 10f; setTextColor(Color.LTGRAY) })
        root.addView(header)
        flipper = ViewFlipper(this).apply { setPadding(dp(26), dp(18), dp(26), dp(12)) }
        flipper.addView(infoPage()); flipper.addView(instrumentPage()); flipper.addView(setupPage())
        root.addView(flipper, LinearLayout.LayoutParams(-1, 0, 1f))
        val nav = LinearLayout(this).apply { gravity = Gravity.END; setPadding(dp(18), dp(6), dp(18), dp(10)); setBackgroundColor(Color.rgb(232, 228, 221)) }
        back = Button(this).apply { text = "Back"; actionStyle(); isEnabled = false; setOnClickListener { if (page > 0) { page--; flipper.showPrevious(); updateNav() } } }
        next = Button(this).apply { text = "Continue"; actionStyle(); setOnClickListener { if (page < 2) { if (page == 0 && !validInfo()) return@setOnClickListener; if (page == 1 && selected.isEmpty()) { toast("Select at least one instrument"); return@setOnClickListener }; page++; flipper.showNext(); updateNav() } else finishCreation() } }
        nav.addView(back, LinearLayout.LayoutParams(dp(120), dp(48))); nav.addView(Space(this), LinearLayout.LayoutParams(dp(12), 1)); nav.addView(next, LinearLayout.LayoutParams(dp(150), dp(48)))
        root.addView(nav); setContentView(root)
    }

    private fun infoPage(): View {
        val box = card(this)
        box.addView(TextView(this).apply { text = "Score information"; textSize = 24f; heading() })
        box.addView(TextView(this).apply { text = "Start with the identity and tempo of the composition."; textSize = 14f; setTextColor(Color.DKGRAY); setPadding(0, dp(6), 0, dp(14)) })
        titleField = EditText(this).apply { hint = "Composition title"; setText("Untitled"); textSize = 17f }
        composerField = EditText(this).apply { hint = "Composer / author"; textSize = 17f }
        tempoField = EditText(this).apply { hint = "BPM (20–400)"; setText("120"); inputType = InputType.TYPE_CLASS_NUMBER }
        marking = Spinner(this).apply { adapter = ArrayAdapter(this@ScoreCreationActivity, android.R.layout.simple_spinner_dropdown_item, listOf("Grave", "Largo", "Adagio", "Andante", "Moderato", "Allegretto", "Allegro", "Vivace", "Presto")); setSelection(6) }
        listOf(titleField, composerField, tempoField).forEach { box.addView(it, LinearLayout.LayoutParams(-1, dp(58)).apply { bottomMargin = dp(8) }) }
        box.addView(TextView(this).apply { text = "Tempo indication"; textSize = 12f; setTextColor(Color.DKGRAY) }); box.addView(marking, LinearLayout.LayoutParams(-1, dp(52)))
        return ScrollView(this).apply { addView(box) }
    }

    private fun instrumentPage(): View {
        val scroll = ScrollView(this); val box = card(this)
        box.addView(TextView(this).apply { text = "Instrumentation"; textSize = 24f; heading() })
        val count = TextView(this).apply { textSize = 13f; setTextColor(Color.DKGRAY); setPadding(0, dp(5), 0, dp(12)) }; box.addView(count)
        InstrumentCatalog.grouped.forEach { (family, options) ->
            box.addView(TextView(this).apply { text = family.uppercase(); textSize = 11f; setTextColor(Color.rgb(111, 87, 46)); typeface = Typeface.DEFAULT_BOLD; setPadding(0, dp(8), 0, dp(4)) })
            options.forEach { option ->
                box.addView(CheckBox(this).apply { text = option.instrument.name + if (option.grandStaff) "  ·  grand staff" else ""; textSize = 15f; isChecked = selected.containsKey(option.instrument.id); setOnCheckedChangeListener { _, checked -> if (checked) selected[option.instrument.id] = option else selected.remove(option.instrument.id); count.text = "${selected.size} part(s) selected" } }, LinearLayout.LayoutParams(-1, dp(46)))
            }
        }
        count.text = "${selected.size} part(s) selected"; scroll.addView(box); return scroll
    }

    private fun setupPage(): View {
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }; val left = card(this); val right = card(this)
        left.addView(TextView(this).apply { text = "Musical setup"; textSize = 24f; heading() })
        meter = Spinner(this).apply { adapter = ArrayAdapter(this@ScoreCreationActivity, android.R.layout.simple_spinner_dropdown_item, listOf("4/4", "2/2", "2/4", "3/4", "3/8", "6/8", "1/4", "5/4", "7/8", "9/8", "11/8", "12/8")) }
        key = Spinner(this).apply { adapter = ArrayAdapter(this@ScoreCreationActivity, android.R.layout.simple_spinner_dropdown_item, listOf("C major", "G major", "D major", "A major", "E major", "B major", "F major", "Bb major", "Eb major", "Ab major", "A minor", "E minor", "D minor")) }
        pickup = Spinner(this).apply { adapter = ArrayAdapter(this@ScoreCreationActivity, android.R.layout.simple_spinner_dropdown_item, listOf("No pickup", "1 beat", "2 beats", "3 beats")) }
        addLabeled(left, "Time signature", meter); addLabeled(left, "Key signature", key); addLabeled(left, "Pickup", pickup)
        right.addView(TextView(this).apply { text = "Ready to compose"; textSize = 24f; heading() })
        val summary = TextView(this).apply { textSize = 15f; setTextColor(Color.DKGRAY); setPadding(0, dp(12), 0, 0) }; right.addView(summary)
        right.addView(TextView(this).apply { text = "Your choices become the project's domain model and build the initial parts and staves."; textSize = 14f; setTextColor(Color.GRAY); setPadding(0, dp(18), 0, 0) })
        fun refreshSummary() { summary.text = "${selected.size} part(s)\n${meter.selectedItem} · ${key.selectedItem}\n${tempoField.text} BPM · ${marking.selectedItem}" }
        meter.onItemSelectedListener = simpleListener { refreshSummary() }; key.onItemSelectedListener = simpleListener { refreshSummary() }; marking.onItemSelectedListener = simpleListener { refreshSummary() }; refreshSummary()
        row.addView(left, LinearLayout.LayoutParams(0, -1, 1f)); row.addView(Space(this), LinearLayout.LayoutParams(dp(14), 1)); row.addView(right, LinearLayout.LayoutParams(0, -1, 1f)); return row
    }

    private fun addLabeled(parent: LinearLayout, label: String, control: View) { parent.addView(TextView(this).apply { text = label; textSize = 12f; setTextColor(Color.DKGRAY); setPadding(0, dp(16), 0, dp(5)) }); parent.addView(control, LinearLayout.LayoutParams(-1, dp(52))) }
    private fun simpleListener(action: () -> Unit) = object : AdapterView.OnItemSelectedListener { override fun onNothingSelected(p: AdapterView<*>?) = Unit; override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) = action() }
    private fun validInfo() = if (titleField.text.toString().isBlank()) { titleField.error = "Enter a title"; false } else true
    private fun updateNav() { back.isEnabled = page > 0; next.text = if (page == 2) "Create score" else "Continue" }

    private fun finishCreation() {
        val parts = meter.selectedItem.toString().split('/'); val beats = parts[0].toInt(); val beatType = parts[1].toInt()
        val keyName = key.selectedItem.toString().substringBefore(' ')
        val fifths = mapOf("C" to 0, "G" to 1, "D" to 2, "A" to 3, "E" to 4, "B" to 5, "F" to -1, "Bb" to -2, "Eb" to -3, "Ab" to -4)[keyName] ?: 0
        val bpm = tempoField.text.toString().toIntOrNull()?.coerceIn(20, 400) ?: 120
        val draft = CreateScoreDraft(
            metadata = Metadata(title = titleField.text.toString().ifBlank { "Untitled" }, composer = composerField.text.toString()),
            instruments = selected.values.map { it.instrument }, keySignature = KeySignature(fifths, key.selectedItem.toString().endsWith("minor")),
            timeSignature = TimeSignature(beats, beatType), tempo = Tempo(bpm, marking.selectedItem.toString()), pickupBeats = pickup.selectedItemPosition
        )
        ScoreSession.start(ScoreFactory().create(draft)); startActivity(Intent(this, EditScoreActivity::class.java)); finish()
    }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

class OpenScoresActivity : AppCompatActivity() {
    override fun onCreate(state: Bundle?) { super.onCreate(state); requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE; title = "Open score"; refresh() }
    private fun refresh() {
        val store = ScoreFileStore(this); val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(26), dp(20), dp(26), dp(20)); setBackgroundColor(Color.rgb(244,241,235)) }
        root.addView(TextView(this).apply { text="Your scores"; textSize=28f; heading(); setPadding(0,0,0,dp(6)) }); root.addView(TextView(this).apply { text="Saved compositions on this device"; textSize=14f; setTextColor(Color.GRAY); setPadding(0,0,0,dp(16)) })
        val names = store.list(); if (names.isEmpty()) root.addView(TextView(this).apply { text="No saved score yet. Create your first composition."; textSize=16f; setTextColor(Color.DKGRAY) })
        names.forEach { name ->
            val row=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(dp(8),dp(4),dp(8),dp(4));setBackgroundColor(Color.WHITE)}
            row.addView(Button(this).apply{text=name;actionStyle();setOnClickListener{store.load(name)?.let{loaded->ScoreSession.engine=loaded.engine;ScoreSession.fileName=name;ScoreSession.title=loaded.title;ScoreSession.composer=loaded.composer;ScoreSession.tempo=loaded.tempo;startActivity(Intent(this@OpenScoresActivity,EditScoreActivity::class.java))}}},LinearLayout.LayoutParams(0,dp(58),1f))
            row.addView(Button(this).apply{text="Delete";actionStyle();setOnClickListener{if(store.delete(name))refresh()}},LinearLayout.LayoutParams(dp(105),dp(58))); root.addView(row,LinearLayout.LayoutParams(-1,dp(68)))
        }
        setContentView(ScrollView(this).apply{addView(root)})
    }
}

class SettingsActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("settings", MODE_PRIVATE) }
    override fun onCreate(state: Bundle?) { super.onCreate(state); requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(26),dp(20),dp(26),dp(20));setBackgroundColor(Color.rgb(244,241,235))}; root.addView(TextView(this).apply{text="Settings";textSize=28f;heading();setPadding(0,0,0,dp(12))})
        listOf("prompt" to "Prompt tone","scroll" to "Auto scroll","highlight" to "Highlight new elements","touch" to "Touch / dash line","measureNumbers" to "Show measure numbers","tutorial" to "Tutorial").forEach{(key,label)->root.addView(CheckBox(this).apply{text=label;textSize=15f;isChecked=prefs.getBoolean(key,key!="tutorial");setOnCheckedChangeListener{_,checked->prefs.edit().putBoolean(key,checked).apply()}},LinearLayout.LayoutParams(-1,dp(50)))}
        root.addView(Button(this).apply{text="Restore defaults";actionStyle();setOnClickListener{prefs.edit().clear().apply();recreate()}},LinearLayout.LayoutParams(dp(220),dp(52))); setContentView(ScrollView(this).apply{addView(root)})
    }
}

class MetadataEditorActivity : AppCompatActivity() {
    override fun onCreate(state: Bundle?) { super.onCreate(state); requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(26),dp(20),dp(26),dp(20));setBackgroundColor(Color.rgb(244,241,235))}; box.addView(TextView(this).apply{text="Score metadata";textSize=28f;heading()})
        val title=EditText(this).apply{hint="Title";setText(ScoreSession.title)}; val composer=EditText(this).apply{hint="Composer";setText(ScoreSession.composer)}; val bpm=EditText(this).apply{hint="BPM";setText(ScoreSession.tempo.toString());inputType=InputType.TYPE_CLASS_NUMBER}; val mark=EditText(this).apply{hint="Tempo indication";setText(ScoreSession.tempoMarking)}
        listOf(title,composer,bpm,mark).forEach{box.addView(it,LinearLayout.LayoutParams(-1,dp(58)))}; box.addView(Button(this).apply{text="Save changes";actionStyle();setOnClickListener{ScoreSession.syncMetadata(title.text.toString(),composer.text.toString(),bpm.text.toString().toIntOrNull()?:120,mark.text.toString());finish()}});setContentView(box)
    }
}

class EditScoreActivity : AppCompatActivity() {
    private lateinit var editor: ScoreNotationView; private val playback=org.notamusic.app.playback.PcmPlaybackController()
    override fun onCreate(state: Bundle?) { super.onCreate(state); requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE; build() }
    private fun build(){
        editor=ScoreNotationView(this); val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(237,233,225))}; val bar=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(dp(6),dp(4),dp(6),dp(4));setBackgroundColor(Color.rgb(48,50,55))}
        bar.addView(TextView(this).apply{text=ScoreSession.title;setTextColor(Color.WHITE);textSize=16f;typeface=Typeface.DEFAULT_BOLD;setPadding(dp(8),0,dp(12),0)},LinearLayout.LayoutParams(0,dp(50),1f))
        fun b(label:String,action:()->Unit)=Button(this).apply{text=label;actionStyle();setTextColor(Color.WHITE);setBackgroundColor(Color.rgb(70,73,79));setOnClickListener{action()}}
        bar.addView(b("Metadata"){startActivity(Intent(this,MetadataEditorActivity::class.java))}); bar.addView(b("+ Measure"){ScoreSession.engine.addMeasure(ScoreSession.engine.measures.lastIndex);editor.invalidate()}); bar.addView(b("Undo"){ScoreSession.engine.undo();editor.invalidate()}); bar.addView(b("Redo"){ScoreSession.engine.redo();editor.invalidate()}); bar.addView(b("Play"){playback.play(ScoreSession.engine,ScoreSession.tempo)}); bar.addView(b("Stop"){playback.stop()}); bar.addView(b("Save"){if(editor.saveCurrent())Toast.makeText(this,"Score saved",Toast.LENGTH_SHORT).show()}); bar.addView(b("Staffs"){startActivity(Intent(this,StaffConfigurationActivity::class.java))}); bar.addView(b("Export"){startActivity(Intent(this,ShareScoreActivity::class.java))})
        root.addView(bar);root.addView(editor,LinearLayout.LayoutParams(-1,0,1f));setContentView(root)
    }
    override fun onDestroy(){playback.stop();super.onDestroy()}
}

class StaffConfigurationActivity : AppCompatActivity() {
    override fun onCreate(state: Bundle?) { super.onCreate(state); requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE; val score=ScoreSession.score; val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(26),dp(20),dp(26),dp(20));setBackgroundColor(Color.rgb(244,241,235))}
        root.addView(TextView(this).apply{text="Staff configuration";textSize=28f;heading()});root.addView(TextView(this).apply{text="${score?.parts?.size?:1} part(s) · ${score?.parts?.sumOf{it.staves.size}?:1} staff/staves";textSize=14f;setTextColor(Color.GRAY);setPadding(0,dp(4),0,dp(16))})
        score?.parts?.forEachIndexed{i,part->val c=card(this);c.addView(TextView(this).apply{text="${i+1}. ${part.name}";textSize=18f;heading()});part.staves.forEach{staff->c.addView(TextView(this).apply{text="${staff.displayName} · ${staff.clef.name.lowercase()}${if(staff.transposition!=0) " · transpose ${staff.transposition}" else ""}";textSize=14f;setTextColor(Color.DKGRAY);setPadding(0,dp(8),0,dp(4))})};root.addView(c,LinearLayout.LayoutParams(-1,LinearLayout.LayoutParams.WRAP_CONTENT).apply{bottomMargin=dp(10)})};setContentView(ScrollView(this).apply{addView(root)})
    }
}

class ShareScoreActivity : AppCompatActivity() {
    override fun onCreate(state: Bundle?) { super.onCreate(state); requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE; val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(26),dp(20),dp(26),dp(20));setBackgroundColor(Color.rgb(244,241,235))};root.addView(TextView(this).apply{text="Export score";textSize=28f;heading()});root.addView(TextView(this).apply{text="Generate standard MusicXML or MIDI from the current notation model.";textSize=15f;setTextColor(Color.DKGRAY);setPadding(0,dp(6),0,dp(18))});root.addView(Button(this).apply{text="Export MusicXML";actionStyle();setOnClickListener{exportXml()}});root.addView(Button(this).apply{text="Export MIDI";actionStyle();setOnClickListener{exportMidi()}});root.addView(Button(this).apply{text="Show export path";actionStyle();setOnClickListener{Toast.makeText(this,"${filesDir.absolutePath}/${ScoreSession.fileName}",Toast.LENGTH_LONG).show()}});setContentView(root)
    }
    private fun exportXml(){runCatching{val f=File(filesDir,ScoreSession.fileName+".musicxml");FileOutputStream(f).use{org.notamusic.app.musicxml.PlaceholderMusicXml().export(ScoreSession.currentScore(),it)};Toast.makeText(this,"MusicXML exported",Toast.LENGTH_SHORT).show()}.onFailure{Toast.makeText(this,"Export failed: ${it.message}",Toast.LENGTH_LONG).show()}}
    private fun exportMidi(){runCatching{val f=File(filesDir,ScoreSession.fileName+".mid");FileOutputStream(f).use{org.notamusic.app.midi.PlaceholderMidiRenderer().render(ScoreSession.currentScore(),it)};Toast.makeText(this,"MIDI exported",Toast.LENGTH_SHORT).show()}.onFailure{Toast.makeText(this,"Export failed: ${it.message}",Toast.LENGTH_LONG).show()}}
}
