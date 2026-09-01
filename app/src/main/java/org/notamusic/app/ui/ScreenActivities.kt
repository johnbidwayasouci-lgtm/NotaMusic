package org.notamusic.app.ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.notamusic.app.data.persistence.ScoreFileStore
import org.notamusic.app.ui.editor.ScoreEditorView
import org.notamusic.app.ui.editor.ScoreSession

private fun launch(c: Context, cls: Class<*>) = c.startActivity(Intent(c, cls))
private fun dp(v: Int) = (v * android.content.res.Resources.getSystem().displayMetrics.density).toInt()
private fun TextView.common() { setTextColor(Color.rgb(32,32,32)); setPadding(dp(10), dp(8), dp(10), dp(8)) }

class ScoreCreationActivity : AppCompatActivity() {
    override fun onCreate(b: Bundle?) { super.onCreate(b); title = "Create score"; build() }
    private fun build() {
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18), dp(14), dp(18), dp(14)) }
        val title = EditText(this).apply { hint = "Title"; setText("Untitled") }
        val composer = EditText(this).apply { hint = "Composer" }
        val tempo = EditText(this).apply { hint = "Tempo (BPM)"; setText("120"); inputType = 2 }
        box.addView(TextView(this).apply { text = "1. Metadata"; textSize = 18f; common() }); box.addView(title); box.addView(composer); box.addView(tempo)
        box.addView(TextView(this).apply { text = "2. Instruments"; textSize = 18f; common() })
        val instrument = Spinner(this).apply { adapter = ArrayAdapter(this@ScoreCreationActivity, android.R.layout.simple_spinner_dropdown_item, listOf("Piano", "Violin", "Flute", "Guitar", "Clarinet", "Trumpet", "Cello")) }
        box.addView(instrument)
        box.addView(TextView(this).apply { text = "3. Key and meter"; textSize = 18f; common() })
        val meter = Spinner(this).apply { adapter = ArrayAdapter(this@ScoreCreationActivity, android.R.layout.simple_spinner_dropdown_item, listOf("4/4", "2/4", "3/4", "2/2", "3/8", "6/8", "1/4", "5/4", "7/8", "9/8", "11/8", "12/8")) }
        box.addView(meter)
        val key = Spinner(this).apply { adapter = ArrayAdapter(this@ScoreCreationActivity, android.R.layout.simple_spinner_dropdown_item, listOf("C major", "G major", "D major", "A major", "F major", "Bb major", "A minor", "E minor", "D minor")) }
        box.addView(key)
        val actions = LinearLayout(this).apply { gravity = Gravity.END }
        actions.addView(Button(this).apply { text = "Finish"; setOnClickListener {
            val t = title.text.toString().ifBlank { "Untitled" }; val c = composer.text.toString(); val bpm = tempo.text.toString().toIntOrNull()?.coerceIn(20,300) ?: 120
            ScoreSession.reset(t, c, bpm)
            val parts = meter.selectedItem.toString().split('/'); val beats = parts[0].toInt(); val unit = parts[1].toInt(); ScoreSession.engine.changeMeter(0, beats, unit)
            startActivity(Intent(this@ScoreCreationActivity, EditScoreActivity::class.java)); finish()
        } })
        box.addView(actions); setContentView(ScrollView(this).apply { addView(box) })
    }
}

class OpenScoresActivity : AppCompatActivity() {
    override fun onCreate(b: Bundle?) { super.onCreate(b); title = "Open"; refresh() }
    private fun refresh() {
        val store = ScoreFileStore(this); val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12),dp(12),dp(12),dp(12)) }
        if (store.list().isEmpty()) list.addView(TextView(this).apply { text = "No saved scores."; textSize = 18f; common() })
        store.list().forEach { name ->
            val row = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
            row.addView(Button(this).apply { text = name; layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f); setOnClickListener {
                store.load(name)?.let { loaded -> ScoreSession.engine = loaded.engine; ScoreSession.fileName = name; ScoreSession.title = loaded.title; ScoreSession.composer = loaded.composer; ScoreSession.tempo = loaded.tempo; startActivity(Intent(this@OpenScoresActivity, EditScoreActivity::class.java)) }
            } })
            row.addView(Button(this).apply { text = "Delete"; setOnClickListener { if (store.delete(name)) refresh() } })
            list.addView(row)
        }
        setContentView(ScrollView(this).apply { addView(list) })
    }
}

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(b: Bundle?) { super.onCreate(b); title = "Settings"
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18),dp(14),dp(18),dp(14)) }
        listOf("Prompt tone", "Auto scroll", "Highlight new elements", "Touch/dash line", "Show measure numbers", "Tutorial").forEach { label -> box.addView(CheckBox(this).apply { text = label; isChecked = label != "Tutorial" }) }
        box.addView(Button(this).apply { text = "Restore samples"; setOnClickListener { Toast.makeText(context, "Samples restored", Toast.LENGTH_SHORT).show() } })
        box.addView(TextView(this).apply { text = "Edit scale"; common() }); box.addView(SeekBar(this).apply { max = 100; progress = 50 })
        box.addView(TextView(this).apply { text = "Score scale"; common() }); box.addView(SeekBar(this).apply { max = 100; progress = 50 })
        setContentView(ScrollView(this).apply { addView(box) })
    }
}

class MetadataEditorActivity : AppCompatActivity() {
    override fun onCreate(b: Bundle?) { super.onCreate(b); title = "Metadata"
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(18),dp(14),dp(18),dp(14)) }
        val titleField = EditText(this).apply { hint = "Title"; setText(ScoreSession.title) }; val composer = EditText(this).apply { hint = "Composer"; setText(ScoreSession.composer) }
        box.addView(titleField); box.addView(composer); box.addView(Button(this).apply { text = "Save"; setOnClickListener { ScoreSession.title = titleField.text.toString().ifBlank { "Untitled" }; ScoreSession.composer = composer.text.toString(); finish() } }); setContentView(box)
    }
}

class EditScoreActivity : AppCompatActivity() {
    private lateinit var editor: ScoreEditorView
    private val playback = org.notamusic.app.playback.PcmPlaybackController()
    override fun onCreate(b: Bundle?) { super.onCreate(b); title = ScoreSession.title; build() }
    private fun build() {
        editor = ScoreEditorView(this)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val bar = LinearLayout(this).apply { gravity = Gravity.CENTER_VERTICAL }
        fun button(text: String, action: () -> Unit) = Button(this).apply { this.text = text; setOnClickListener { action() } }
        bar.addView(button("Save") { if (editor.saveCurrent()) Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show() })
        bar.addView(button("Undo") { ScoreSession.engine.undo(); editor.invalidate() }); bar.addView(button("Redo") { ScoreSession.engine.redo(); editor.invalidate() })
        bar.addView(button("+") { ScoreSession.engine.addMeasure(ScoreSession.engine.measures.lastIndex); editor.invalidate() })
        bar.addView(button("Play") { playback.play(ScoreSession.engine, ScoreSession.tempo) }); bar.addView(button("Stop") { playback.stop() })
        root.addView(bar); root.addView(editor, LinearLayout.LayoutParams(-1,0,1f)); setContentView(root)
    }
    override fun onDestroy() { playback.stop(); super.onDestroy() }
}

class StaffConfigurationActivity : AppCompatActivity() { override fun onCreate(b: Bundle?) { super.onCreate(b); setContentView(TextView(this).apply { text = "Staff configuration\n\nPiano · one staff / grand staff\nMute · volume · reorder"; textSize = 20f; common() }) } }
class ShareScoreActivity : AppCompatActivity() { override fun onCreate(b: Bundle?) { super.onCreate(b); setContentView(TextView(this).apply { text = "Share score"; textSize = 20f; common() }) } }
