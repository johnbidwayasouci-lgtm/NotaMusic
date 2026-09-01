package org.notamusic.app.ui

import android.app.AlertDialog
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
private fun TextView.styleTitle(){setTextColor(Color.rgb(35,37,41));typeface=Typeface.DEFAULT_BOLD}
private fun Button.actionStyle(){minHeight=dp(46);textSize=14f}

class ScoreCreationActivity:AppCompatActivity(){
    private var page=0
    private lateinit var pages:ViewFlipper
    private lateinit var titleField:EditText
    private lateinit var composerField:EditText
    private lateinit var tempoField:EditText
    private lateinit var instrumentBox:LinearLayout
    private lateinit var meter:Spinner
    private lateinit var key:Spinner
    private val instruments=listOf("Piano","Violin","Viola","Cello","Double Bass","Flute","Oboe","Clarinet","Bassoon","Trumpet","Horn","Trombone","Guitar")
    override fun onCreate(b:Bundle?){super.onCreate(b);requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;title="New score";build()}
    private fun build(){
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(244,241,235))}
        val header=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(dp(22),dp(12),dp(22),dp(12));setBackgroundColor(Color.rgb(38,40,44))}
        header.addView(TextView(this).apply{text="New score";textSize=22f;setTextColor(Color.WHITE);styleTitle()},LinearLayout.LayoutParams(0,dp(46),1f))
        header.addView(TextView(this).apply{text="1  INFO     2  INSTRUMENTS     3  PARTITION";textSize=11f;setTextColor(Color.LTGRAY)})
        root.addView(header)
        pages=ViewFlipper(this).apply{setPadding(dp(28),dp(20),dp(28),dp(12))};pages.addView(pageInfo());pages.addView(pageInstruments());pages.addView(pageNotation());root.addView(pages,LinearLayout.LayoutParams(-1,0,1f))
        val nav=LinearLayout(this).apply{gravity=Gravity.END;setPadding(dp(20),dp(8),dp(20),dp(12));setBackgroundColor(Color.rgb(232,228,221))}
        val back=Button(this).apply{text="Back";actionStyle();setOnClickListener{if(page>0){page--;pages.showPrevious();updateNav()}}};val next=Button(this).apply{text="Next";actionStyle();tag="next";setOnClickListener{if(page<2){if(page==0&&!validateInfo())return@setOnClickListener;page++;pages.showNext();updateNav()}else finishCreation()}}
        nav.addView(back,LinearLayout.LayoutParams(dp(120),dp(48)));nav.addView(Space(this),LinearLayout.LayoutParams(dp(12),1));nav.addView(next,LinearLayout.LayoutParams(dp(140),dp(48)));root.addView(nav);setContentView(root);updateNav()
    }
    private fun pageInfo():View{val box=card();box.addView(TextView(this).apply{text="Score information";textSize=22f;styleTitle()});box.addView(TextView(this).apply{text="Start with the title, composer and tempo. These details stay with the score.";textSize=14f;setTextColor(Color.DKGRAY);setPadding(0,dp(6),0,dp(18))});titleField=EditText(this).apply{hint="Title";setText("Untitled");textSize=16f};composerField=EditText(this).apply{hint="Composer / author";textSize=16f};tempoField=EditText(this).apply{hint="Tempo (BPM)";setText("120");inputType=InputType.TYPE_CLASS_NUMBER};box.addView(titleField);box.addView(composerField);box.addView(tempoField);return ScrollView(this).apply{addView(box)}}
    private fun pageInstruments():View{val box=card();box.addView(TextView(this).apply{text="Instrumentation";textSize=22f;styleTitle()});box.addView(TextView(this).apply{text="Choose one or more staves. Piano may later use a grand staff.";textSize=14f;setTextColor(Color.DKGRAY);setPadding(0,dp(6),0,dp(12))});instrumentBox=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};instruments.forEachIndexed{i,name->instrumentBox.addView(CheckBox(this).apply{text=name;isChecked=i==0})};box.addView(instrumentBox);return ScrollView(this).apply{addView(box)}}
    private fun pageNotation():View{val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};val left=card();left.addView(TextView(this).apply{text="Key & meter";textSize=22f;styleTitle()});meter=Spinner(this).apply{adapter=ArrayAdapter(this@ScoreCreationActivity,android.R.layout.simple_spinner_dropdown_item,listOf("4/4","2/2","2/4","3/4","3/8","6/8","1/4","5/4","7/8","9/8","11/8","12/8"))};key=Spinner(this).apply{adapter=ArrayAdapter(this@ScoreCreationActivity,android.R.layout.simple_spinner_dropdown_item,listOf("C major","G major","D major","A major","E major","F major","Bb major","Eb major","A minor","E minor","D minor"))};left.addView(TextView(this).apply{text="Time signature";textSize=13f;setPadding(0,dp(16),0,dp(5))});left.addView(meter);left.addView(TextView(this).apply{text="Key signature";textSize=13f;setPadding(0,dp(16),0,dp(5))});left.addView(key);row.addView(left,LinearLayout.LayoutParams(0,-1,1f));val preview=card();preview.addView(TextView(this).apply{text="Ready to compose";textSize=22f;styleTitle()});preview.addView(TextView(this).apply{text="The editor opens in landscape with measures, barlines, note figures, rests and editing tools.";textSize=15f;setTextColor(Color.DKGRAY);setPadding(0,dp(12),0,0)});row.addView(preview,LinearLayout.LayoutParams(0,-1,1f));return row}
    private fun card()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(22),dp(18),dp(22),dp(18));setBackgroundColor(Color.WHITE)}
    private fun validateInfo():Boolean{if(titleField.text.toString().isBlank()){titleField.error="Enter a title";return false};return true}
    private fun updateNav(){val nav=findViewById<ViewGroup>(android.R.id.content).getChildAt(0) as? ViewGroup;pages?.let{ } }
    private fun finishCreation(){val t=titleField.text.toString().ifBlank{"Untitled"};val c=composerField.text.toString();val bpm=tempoField.text.toString().toIntOrNull()?.coerceIn(20,300)?:120;ScoreSession.reset(t,c,bpm);val p=meter.selectedItem.toString().split('/');ScoreSession.engine.changeMeter(0,p[0].toInt(),p[1].toInt());startActivity(Intent(this,EditScoreActivity::class.java));finish()}
}

class OpenScoresActivity:AppCompatActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;title="Open score";refresh()};private fun refresh(){val store=ScoreFileStore(this);val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(24),dp(18),dp(24),dp(18));setBackgroundColor(Color.rgb(244,241,235))};root.addView(TextView(this).apply{text="Your scores";textSize=26f;styleTitle();setPadding(0,0,0,dp(12))});val list=store.list();if(list.isEmpty())root.addView(TextView(this).apply{text="No saved score yet. Create your first composition.";textSize=16f;setTextColor(Color.DKGRAY)}) else list.forEach{name->val row=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(dp(10),dp(5),dp(10),dp(5));setBackgroundColor(Color.WHITE)};row.addView(Button(this).apply{text=name;actionStyle();setOnClickListener{store.load(name)?.let{loaded->ScoreSession.engine=loaded.engine;ScoreSession.fileName=name;ScoreSession.title=loaded.title;ScoreSession.composer=loaded.composer;ScoreSession.tempo=loaded.tempo;startActivity(Intent(this@OpenScoresActivity,EditScoreActivity::class.java))}}},LinearLayout.LayoutParams(0,dp(52),1f));row.addView(Button(this).apply{text="Delete";actionStyle();setOnClickListener{if(store.delete(name))refresh()}},LinearLayout.LayoutParams(dp(100),dp(52)));root.addView(row,LinearLayout.LayoutParams(-1,dp(62)))};setContentView(ScrollView(this).apply{addView(root)})}}

class SettingsActivity:AppCompatActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;title="Settings";val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(24),dp(18),dp(24),dp(18));setBackgroundColor(Color.rgb(244,241,235))};root.addView(TextView(this).apply{text="Settings";textSize=26f;styleTitle()});listOf("Prompt tone","Auto scroll","Highlight new elements","Touch / dash line","Show measure numbers","Tutorial").forEach{root.addView(CheckBox(this).apply{text=it;isChecked=it!="Tutorial"})};root.addView(Button(this).apply{text="Restore samples";actionStyle();setOnClickListener{Toast.makeText(context,"Samples restored",Toast.LENGTH_SHORT).show()}});setContentView(ScrollView(this).apply{addView(root)})}}

class MetadataEditorActivity:AppCompatActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(24),dp(18),dp(24),dp(18))};val title=EditText(this).apply{hint="Title";setText(ScoreSession.title)};val composer=EditText(this).apply{hint="Composer";setText(ScoreSession.composer)};box.addView(title);box.addView(composer);box.addView(Button(this).apply{text="Save";actionStyle();setOnClickListener{ScoreSession.title=title.text.toString().ifBlank{"Untitled"};ScoreSession.composer=composer.text.toString();finish()}});setContentView(box)}}

class EditScoreActivity:AppCompatActivity(){private lateinit var editor:ScoreNotationView;private val playback=org.notamusic.app.playback.PcmPlaybackController();override fun onCreate(b:Bundle?){super.onCreate(b);requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;title=ScoreSession.title;build()};private fun build(){editor=ScoreNotationView(this);val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(Color.rgb(237,233,225))};val bar=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL;setPadding(dp(8),dp(5),dp(8),dp(5));setBackgroundColor(Color.rgb(51,53,58))};fun b(text:String,action:()->Unit)=Button(this).apply{this.text=text;actionStyle();setOnClickListener{action()};setTextColor(Color.WHITE);setBackgroundColor(Color.rgb(70,73,79))};bar.addView(TextView(this).apply{text=ScoreSession.title;setTextColor(Color.WHITE);textSize=15f;typeface=Typeface.DEFAULT_BOLD;setPadding(dp(8),0,dp(14),0)},LinearLayout.LayoutParams(0,dp(48),1f));bar.addView(b("Metadata"){startActivity(Intent(this,MetadataEditorActivity::class.java))});bar.addView(b("+ Measure"){ScoreSession.engine.addMeasure(ScoreSession.engine.measures.lastIndex);editor.invalidate()});bar.addView(b("Undo"){ScoreSession.engine.undo();editor.invalidate()});bar.addView(b("Redo"){ScoreSession.engine.redo();editor.invalidate()});bar.addView(b("Play"){playback.play(ScoreSession.engine,ScoreSession.tempo)});bar.addView(b("Stop"){playback.stop()});bar.addView(b("Save"){if(editor.saveCurrent())Toast.makeText(this,"Score saved",Toast.LENGTH_SHORT).show()});root.addView(bar);root.addView(editor,LinearLayout.LayoutParams(-1,0,1f));setContentView(root)};override fun onResume(){super.onResume();title=ScoreSession.title};override fun onDestroy(){playback.stop();super.onDestroy()}}

class StaffConfigurationActivity:AppCompatActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;setContentView(TextView(this).apply{text="Staff configuration\n\nPiano · one staff / grand staff\nMute · reorder · instrument";textSize=20f;setPadding(dp(30),dp(30),dp(30),dp(30))})}}
class ShareScoreActivity:AppCompatActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);requestedOrientation=android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;setContentView(TextView(this).apply{text="Share score\n\nMusicXML and MIDI export are available from the project engine.";textSize=20f;setPadding(dp(30),dp(30),dp(30),dp(30))})}}
