package org.notamusic.app.ui

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.notamusic.app.ui.editor.ScoreSession

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        title = "NotaMusic"

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(28), dp(20), dp(28), dp(20))
            setBackgroundColor(Color.rgb(250, 249, 246))
        }

        val intro = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(20), 0, dp(36), 0)
        }
        intro.addView(TextView(this).apply {
            text = "NotaMusic"
            textSize = 34f
            setTextColor(Color.rgb(24, 24, 24))
        })
        intro.addView(TextView(this).apply {
            text = "Music Composer"
            textSize = 17f
            setTextColor(Color.rgb(90, 90, 90))
            setPadding(0, dp(4), 0, dp(18))
        })
        intro.addView(TextView(this).apply {
            text = "Compose, edit, save and play scores in landscape mode."
            textSize = 14f
            setTextColor(Color.rgb(100, 100, 100))
        })
        root.addView(intro, LinearLayout.LayoutParams(0, -2, 1f))

        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(12), 0, dp(12), 0)
        }
        fun action(label: String, target: Class<*>, reset: Boolean = false): View = Button(this).apply {
            text = label
            textSize = 15f
            setOnClickListener {
                if (reset) ScoreSession.reset()
                startActivity(Intent(this@MainActivity, target))
            }
            layoutParams = LinearLayout.LayoutParams(dp(250), dp(52)).apply { bottomMargin = dp(10) }
        }
        actions.addView(action("New score", ScoreCreationActivity::class.java, true))
        actions.addView(action("Open", OpenScoresActivity::class.java))
        actions.addView(action("Settings", SettingsActivity::class.java))
        root.addView(actions, LinearLayout.LayoutParams(dp(280), -2))

        setContentView(root)
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
