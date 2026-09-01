package org.notamusic.app.ui

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.notamusic.app.ui.editor.ScoreSession

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "NotaMusic"
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL; setPadding(36,28,36,28); setBackgroundColor(Color.rgb(250,249,246)) }
        root.addView(TextView(this).apply { text = "NotaMusic"; textSize = 30f; setTextColor(Color.rgb(30,30,30)); gravity = Gravity.CENTER; setPadding(0,20,0,6) }, LinearLayout.LayoutParams(-1,-2))
        root.addView(TextView(this).apply { text = "Music Composer"; textSize = 15f; gravity = Gravity.CENTER; setPadding(0,0,0,24) })
        fun action(label: String, onClick: () -> Unit) = Button(this).apply { text = label; setOnClickListener { onClick() }; layoutParams = LinearLayout.LayoutParams(dp(260), -2).apply { bottomMargin = 10 } }
        root.addView(action("New score") { ScoreSession.reset(); ScoreCreationActivity.start(this) })
        root.addView(action("Open") { OpenScoresActivity.start(this) })
        root.addView(action("Settings") { SettingsActivity.start(this) })
        setContentView(root)
    }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
