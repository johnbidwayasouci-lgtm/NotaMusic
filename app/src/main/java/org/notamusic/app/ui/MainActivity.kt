package org.notamusic.app.ui

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.notamusic.app.ui.editor.ScoreSession

class MainActivity:AppCompatActivity(){
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;title="NotaMusic";build()}
 private fun build(){
  val root=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;setPadding(dp(34),dp(26),dp(34),dp(26));gravity=Gravity.CENTER_VERTICAL;setBackgroundColor(Color.rgb(238,235,229))}
  val brand=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(18),0,dp(42),0)}
  brand.addView(TextView(this).apply{text="NotaMusic";textSize=42f;setTextColor(Color.rgb(31,33,37));typeface=Typeface.DEFAULT_BOLD})
  brand.addView(TextView(this).apply{text="Music notation · composition · playback";textSize=17f;setTextColor(Color.rgb(91,88,82));setPadding(0,dp(5),0,dp(20))})
  brand.addView(TextView(this).apply{text="Create a score, choose your instruments,\nset the key and meter, then compose in landscape.";textSize=14f;setTextColor(Color.rgb(105,101,94))})
  root.addView(brand,LinearLayout.LayoutParams(0,-1,1f))
  val panel=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(18),0,dp(18),0)}
  panel.addView(TextView(this).apply{text="START COMPOSING";textSize=11f;letterSpacing=.12f;setTextColor(Color.rgb(111,87,46));setPadding(0,0,0,dp(8))})
  fun action(label:String,target:Class<*>,reset:Boolean=false)=Button(this).apply{text=label;textSize=16f;setTextColor(Color.WHITE);setBackgroundColor(Color.rgb(54,57,62));setOnClickListener{if(reset)ScoreSession.reset();startActivity(Intent(this@MainActivity,target))};layoutParams=LinearLayout.LayoutParams(dp(280),dp(54)).apply{bottomMargin=dp(10)}}
  panel.addView(action("New score",ScoreCreationActivity::class.java,true));panel.addView(action("Open score",OpenScoresActivity::class.java));panel.addView(action("Settings",SettingsActivity::class.java));panel.addView(Space(this),LinearLayout.LayoutParams(1,dp(6)));panel.addView(TextView(this).apply{text="v0.2.0  ·  landscape workspace";textSize=11f;setTextColor(Color.GRAY);gravity=Gravity.CENTER})
  root.addView(panel,LinearLayout.LayoutParams(dp(330),-1));setContentView(root)
 }
 private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
}
