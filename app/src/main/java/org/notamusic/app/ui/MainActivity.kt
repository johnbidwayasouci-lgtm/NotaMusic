package org.notamusic.app.ui

import android.content.Intent
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
        super.onCreate(savedInstanceState); title = "NotaMusic"
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL; setPadding(36,28,36,28); setBackgroundColor(Color.rgb(250,249,246)) }
        root.addView(TextView(this).apply { text = "NotaMusic"; textSize = 30f; gravity = Gravity.CENTER; setPadding(0,20,0,6) })
        root.addView(TextView(this).apply { text = "Music Composer"; textSize = 15f; gravity = Gravity.CENTER; setPadding(0,0,0,24) })
        fun action(label: String, target: Class<*>, reset: Boolean = false) = Button(this).apply { text = label; setOnClickListener { if (reset) ScoreSession.reset(); startActivity(Intent(this@MainActivity, target)) }; layoutParams = LinearLayout.LayoutParams(dp(260), -2).apply { bottomMargin = 10 } }
        root.addView(action("New score", ScoreCreationActivity::class.java, true))
        root.addView(action("Open", OpenScoresActivity::class.java))
        root.addView(action("Settings", SettingsActivity::class.java))
        setContentView(root)
    }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
