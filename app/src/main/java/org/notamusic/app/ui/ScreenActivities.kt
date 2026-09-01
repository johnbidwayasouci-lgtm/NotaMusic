package org.notamusic.app.ui

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.notamusic.app.data.persistence.ScoreFileStore
import org.notamusic.app.ui.editor.ScoreNotationView
import org.notamusic.app.ui.editor.ScoreSession

private fun Context.dp(v:Int)=(v*resources.displayMetrics.density).toInt()
private fun TextView.titleStyle(){setTextColor(Color.rgb(35,37,41));typeface=Typeface.DEFAULT_BOLD}
private fun Button.uiStyle(){minHeight=context.dp(46);textSize=14f}

class ScoreCreationActivity:AppCompatActivity(){
    private var page=0
    private lateinit var flipper:ViewFlipper
    private lateinit var titleField:EditText
    private lateinit var composerField:EditText
    private lateinit var tempoField:EditText
    private lateinit var meter:Spinner
    private lateinit var key:Spinner
    private val instruments=listOf("Piano","Violin","Viola","Cello","Double Bass","Flute","Oboe","Clarinet","Bassoon","Trumpet","Horn","Trombone","Guitar")
    override fun onCreate(state:Bundle?){super.onCreate(state);requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;title="New score";build()}
    private fun build(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(244,241,235))}
        val header=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(dp(22),dp(10),dp(22),dp(10));setBackgroundColor(Color.rgb(38,40,44))}
        header.addView(TextView(this).apply{text="New score";textSize=22f;setTextColor(Color.WHITE);titleStyle()},LinearLayout.LayoutParams(0,dp(48),1f))
        header.addView(TextView(this).apply{text="1  INFO     2  INSTRUMENTS     3  KEY / METER";textSize=10f;setTextColor(Color.LTGRAY)})
        root.addView(header)
        flipper=ViewFlipper(this).apply{setPadding(dp(26),dp(18),dp(26),dp(12))}
        flipper.addView(infoPage());flipper.addView(instrumentPage());flipper.addView(notationPage())
        root.addView(flipper,LinearLayout.LayoutParams(-1,0,1f))
        val nav=LinearLayout(this).apply{gravity=Gravity.END;setPadding(dp(18),dp(6),dp(18),dp(10));setBackgroundColor(Color.rgb(232,228,221))}
        val back=Button(this).apply{text="Back";uiStyle();setOnClickListener{if(page>0){page--;flipper.showPrevious();updateButtons()}}}
        val next=Button(this).apply{text="Next";uiStyle();setOnClickListener{if(page<2){if(page==0&&!validInfo())return@setOnClickListener;page++;flipper.showNext();updateButtons()}else finishCreation()}}
        nav.addView(back,LinearLayout.LayoutParams(dp(120),dp(48)));nav.addView(Space(this),LinearLayout.LayoutParams(dp(12),1));nav.addView(next,LinearLayout.LayoutParams(dp(140),dp(48)))
        root.addView(nav);setContentView(root)
    }
    private fun infoPage():View{val box=card();box.addView(TextView(this).apply{text="Score information";textSize=23f;titleStyle()});box.addView(TextView(this).apply{text="Define the identity of the composition before entering the notation editor.";textSize=14f;setTextColor(Color.DKGRAY);setPadding(0,dp(6),0,dp(18))});titleField=EditText(this).apply{hint="Title";setText("Untitled");textSize=17f};composerField=EditText(this).apply{hint="Composer / author";textSize=17f};tempoField=EditText(this).apply{hint="Tempo (BPM)";setText("120");inputType=InputType.TYPE_CLASS_NUMBER};box.addView(titleField,fieldParams());box.addView(composerField,fieldParams());box.addView(tempoField,fieldParams());return ScrollView(this).apply{addView(box)}}
    private fun instrumentPage():View{val box=card();box.addView(TextView(this).apply{text="Instrumentation";textSize=23f;titleStyle()});box.addView(TextView(this).apply{text="Select the performers/staves for this score. Multiple instruments are supported.";textSize=14f;setTextColor(Color.DKGRAY);setPadding(0,dp(6),0,dp(10))});instruments.forEachIndexed{i,n->box.addView(CheckBox(this).apply{text=n;isChecked=i==0})};return ScrollView(this).apply{addView(box)}}
    private fun notationPage():View{val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};val left=card();left.addView(TextView(this).apply{text="Key & meter";textSize=23f;titleStyle()});meter=Spinner(this).apply{adapter=ArrayAdapter(this@ScoreCreationActivity,android.R.layout.simple_spinner_dropdown_item,listOf("4/4","2/2","2/4","3/4","3/8","6/8","1/4","5/4","7/8","9/8","11/8","12/8"))};key=Spinner(this).apply{adapter=ArrayAdapter(this@ScoreCreationActivity,android.R.layout.simple_spinner_dropdown_item,listOf("C major","G major","D major","A major","E major","F major","Bb major","Eb major","A minor","E minor","D minor"))};left.addView(TextView(this).apply{text="Time signature";textSize=13f;setPadding(0,dp(18),0,dp(5))});left.addView(meter);left.addView(TextView(this).apply{text="Key signature";textSize=13f;setPadding(0,dp(18),0,dp(5))});left.addView(key);val right=card();right.addView(TextView(this).apply{text="Ready to compose";textSize=23f;titleStyle()});right.addView(TextView(this).apply{text="Your score will open in landscape with measures, barlines, note figures, rests, editing tools and playback.";textSize=15f;setTextColor(Color.DKGRAY);setPadding(0,dp(14),0,0)});row.addView(left,LinearLayout.LayoutParams(0,-1,1f));row.addView(Space(this),LinearLayout.LayoutParams(dp(12),1));row.addView(right,LinearLayout.LayoutParams(0,-1,1f));return row}
    private fun card()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(22),dp(18),dp(22),dp(18));setBackgroundColor(Color.WHITE)}
    private fun fieldParams()=LinearLayout.LayoutParams(-1,dp(58)).apply{bottomMargin=dp(8)}
    private fun validInfo()=if(titleField.text.toString().isBlank()){titleField.error="Enter a title";false}else true
    private fun updateButtons(){}
    private fun finishCreation(){val t=titleField.text.toString().ifBlank{"Untitled"};val composer=composerField.text.toString();val bpm=tempoField.text.toString().toIntOrNull()?.coerceIn(20,300)?:120;ScoreSession.reset(t,composer,bpm);val p=meter.selectedItem.toString().split('/');ScoreSession.engine.changeMeter(0,p[0].toInt(),p[1].toInt());startActivity(Intent(this,EditScoreActivity::class.java));finish()}
}

class OpenScoresActivity:AppCompatActivity(){override fun onCreate(state:Bundle?){super.onCreate(state);requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;title="Open score";refresh()};private fun refresh(){val store=ScoreFileStore(this);val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(24),dp(18),dp(24),dp(18));setBackgroundColor(Color.rgb(244,241,235))};root.addView(TextView(this).apply{text="Your scores";textSize=27f;titleStyle();setPadding(0,0,0,dp(14))});val names=store.list();if(names.isEmpty())root.addView(TextView(this).apply{text="No saved score yet. Create your first composition.";textSize=16f;setTextColor(Color.DKGRAY)}) else names.forEach{name->val row=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;setBackgroundColor(Color.WHITE);setPadding(dp(8),dp(4),dp(8),dp(4))};val open=Button(this).apply{text=name;uiStyle();setOnClickListener{store.load(name)?.let{loaded->ScoreSession.engine=loaded.engine;ScoreSession.fileName=name;ScoreSession.title=loaded.title;ScoreSession.composer=loaded.composer;ScoreSession.tempo=loaded.tempo;startActivity(Intent(this@OpenScoresActivity,EditScoreActivity::class.java))}}};row.addView(open,LinearLayout.LayoutParams(0,dp(56),1f));row.addView(Button(this).apply{text="Delete";uiStyle();setOnClickListener{if(store.delete(name))refresh()}},LinearLayout.LayoutParams(dp(100),dp(56)));root.addView(row,LinearLayout.LayoutParams(-1,dp(64)))};setContentView(ScrollView(this).apply{addView(root)})}}

class SettingsActivity:AppCompatActivity(){override fun onCreate(state:Bundle?){super.onCreate(state);requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;title="Settings";val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(24),dp(18),dp(24),dp(18));setBackgroundColor(Color.rgb(244,241,235))};root.addView(TextView(this).apply{text="Settings";textSize=27f;titleStyle();setPadding(0,0,0,dp(12))});listOf("Prompt tone","Auto scroll","Highlight new elements","Touch / dash line","Show measure numbers","Tutorial").forEach{root.addView(CheckBox(this).apply{text=it;isChecked=it!="Tutorial"})};root.addView(Button(this).apply{text="Restore samples";uiStyle()});setContentView(ScrollView(this).apply{addView(root)})}}

class MetadataEditorActivity:AppCompatActivity(){override fun onCreate(state:Bundle?){super.onCreate(state);requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(24),dp(18),dp(24),dp(18))};val title=EditText(this).apply{hint="Title";setText(ScoreSession.title)};val composer=EditText(this).apply{hint="Composer";setText(ScoreSession.composer)};box.addView(title);box.addView(composer);box.addView(Button(this).apply{text="Save";uiStyle();setOnClickListener{ScoreSession.title=title.text.toString().ifBlank{"Untitled"};ScoreSession.composer=composer.text.toString();finish()}});setContentView(box)}}

class EditScoreActivity:AppCompatActivity(){private lateinit var editor:ScoreNotationView;private val playback=org.notamusic.app.playback.PcmPlaybackController();override fun onCreate(state:Bundle?){super.onCreate(state);requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;build()};private fun build(){editor=ScoreNotationView(this);val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(237,233,225))};val bar=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(dp(7),dp(4),dp(7),dp(4));setBackgroundColor(Color.rgb(48,50,55))};bar.addView(TextView(this).apply{text=ScoreSession.title;setTextColor(Color.WHITE);textSize=15f;typeface=Typeface.DEFAULT_BOLD;setPadding(dp(8),0,dp(10),0)},LinearLayout.LayoutParams(0,dp(50),1f));fun button(label:String,action:()->Unit)=Button(this).apply{text=label;uiStyle();setTextColor(Color.WHITE);setBackgroundColor(Color.rgb(70,73,79));setOnClickListener{action()}};bar.addView(button("Metadata"){startActivity(Intent(this,MetadataEditorActivity::class.java))});bar.addView(button("+ Measure"){ScoreSession.engine.addMeasure(ScoreSession.engine.measures.lastIndex);editor.invalidate()});bar.addView(button("Undo"){ScoreSession.engine.undo();editor.invalidate()});bar.addView(button("Redo"){ScoreSession.engine.redo();editor.invalidate()});bar.addView(button("Play"){playback.play(ScoreSession.engine,ScoreSession.tempo)});bar.addView(button("Stop"){playback.stop()});bar.addView(button("Save"){if(editor.saveCurrent())Toast.makeText(this,"Score saved",Toast.LENGTH_SHORT).show()});root.addView(bar);root.addView(editor,LinearLayout.LayoutParams(-1,0,1f));setContentView(root)};override fun onResume(){super.onResume();title=ScoreSession.title};override fun onDestroy(){playback.stop();super.onDestroy()}}

class StaffConfigurationActivity:AppCompatActivity(){override fun onCreate(state:Bundle?){super.onCreate(state);requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;setContentView(TextView(this).apply{text="Staff configuration\n\nPiano · one staff / grand staff\nMute · reorder · instrument";textSize=20f;setPadding(dp(30),dp(30),dp(30),dp(30))})}}
class ShareScoreActivity:AppCompatActivity(){override fun onCreate(state:Bundle?){super.onCreate(state);requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;setContentView(TextView(this).apply{text="Share score\n\nMusicXML and MIDI export are available from the project engine.";textSize=20f;setPadding(dp(30),dp(30),dp(30),dp(30))})}}
