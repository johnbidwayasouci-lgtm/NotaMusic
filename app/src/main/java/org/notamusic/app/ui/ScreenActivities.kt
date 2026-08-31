package org.notamusic.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

private fun launch(c: Context, cls: Class<*>) = c.startActivity(Intent(c, cls))

class ScoreCreationActivity : AppCompatActivity() {
 companion object { fun start(c: Context) = launch(c, ScoreCreationActivity::class.java) }
 override fun onCreate(b: Bundle?) { super.onCreate(b); setContentView(LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24,24,24,24); addView(TextView(context).apply { text="Create score"; textSize=24f }); addView(Button(context).apply { text="Metadata"; setOnClickListener { launch(context, MetadataEditorActivity::class.java) } }); addView(Button(context).apply { text="Create"; setOnClickListener { launch(context, EditScoreActivity::class.java) } }) }) }
}
class OpenScoresActivity : AppCompatActivity() { companion object { fun start(c: Context) = launch(c, OpenScoresActivity::class.java) }; override fun onCreate(b: Bundle?) { super.onCreate(b); setContentView(TextView(this).apply { text="Open scores\n\nNo saved scores yet."; textSize=20f; setPadding(24,24,24,24) }) } }
class SettingsActivity : AppCompatActivity() { companion object { fun start(c: Context) = launch(c, SettingsActivity::class.java) }; override fun onCreate(b: Bundle?) { super.onCreate(b); setContentView(TextView(this).apply { text="Settings\n\nPrompt tone\nAuto scroll\nHighlight new elements\nShow measure numbers"; textSize=20f; setPadding(24,24,24,24) }) } }
class MetadataEditorActivity : AppCompatActivity() { override fun onCreate(b: Bundle?) { super.onCreate(b); setContentView(TextView(this).apply { text="Metadata editor"; textSize=24f; setPadding(24,24,24,24) }) } }
class EditScoreActivity : AppCompatActivity() { override fun onCreate(b: Bundle?) { super.onCreate(b); setContentView(org.notamusic.app.ui.editor.ScoreEditorView(this)) } }
class StaffConfigurationActivity : AppCompatActivity()
class ShareScoreActivity : AppCompatActivity()
