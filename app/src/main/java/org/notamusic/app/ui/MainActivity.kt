package org.notamusic.app.ui

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContentView(buildHome()) }
 private fun buildHome()=LinearLayout(this).apply{ orientation=LinearLayout.VERTICAL; setPadding(28,24,28,24); addView(TextView(context).apply{ text="NotaMusic"; textSize=28f }); addView(TextView(context).apply{ text="Music notation foundation"; textSize=16f }); addView(Button(context).apply{ text="New score"; setOnClickListener{ ScoreCreationActivity.start(context) } }); addView(Button(context).apply{ text="Open"; setOnClickListener{ OpenScoresActivity.start(context) } }); addView(Button(context).apply{ text="Settings"; setOnClickListener{ SettingsActivity.start(context) } }) }
}
