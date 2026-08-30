package com.notationstudio.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.notationstudio.domain.model.*
import com.notationstudio.rendering.ScoreCanvasView
import java.text.DateFormat
import java.util.Date

private object Routes {
    const val HOME="home"; const val CREATE="create"; const val OPEN="open"; const val EDIT="edit"
    const val STAFF="staff"; const val SETTINGS="settings"; const val METADATA="metadata"; const val SHARE="share"
}

private data class InstrumentDef(val group:String,val name:String,val clef:Clef)
private val instrumentCatalog = listOf(
    InstrumentDef("Claviers","Piano",Clef.TREBLE), InstrumentDef("Claviers","Orgue",Clef.TREBLE),
    InstrumentDef("Cordes","Violon",Clef.TREBLE), InstrumentDef("Cordes","Alto",Clef.ALTO),
    InstrumentDef("Cordes","Violoncelle",Clef.BASS), InstrumentDef("Cordes","Contrebasse",Clef.BASS),
    InstrumentDef("Bois","Flûte",Clef.TREBLE), InstrumentDef("Bois","Hautbois",Clef.TREBLE),
    InstrumentDef("Bois","Clarinette",Clef.TREBLE), InstrumentDef("Bois","Basson",Clef.BASS),
    InstrumentDef("Cuivres","Trompette",Clef.TREBLE), InstrumentDef("Cuivres","Cor",Clef.TREBLE),
    InstrumentDef("Cuivres","Trombone",Clef.BASS), InstrumentDef("Cuivres","Tuba",Clef.BASS),
    InstrumentDef("Percussions","Timbales",Clef.PERCUSSION), InstrumentDef("Percussions","Batterie",Clef.PERCUSSION),
    InstrumentDef("Voix","Soprano",Clef.TREBLE), InstrumentDef("Voix","Alto",Clef.ALTO),
    InstrumentDef("Voix","Ténor",Clef.TENOR), InstrumentDef("Voix","Basse",Clef.BASS)
)

class AppViewModel : ViewModel() {
    var score by mutableStateOf<Score?>(null); private set
    var dirty by mutableStateOf(false); private set
    fun begin(score: Score) { this.score=score; dirty=false }
    fun update(value: Score) { score=value; dirty=true }
    fun save() { dirty=false }
}

@Composable fun NotationApp(vm: AppViewModel = viewModel()) {
    val nav = rememberNavController()
    var pendingBack by rememberSaveable { mutableStateOf(false) }
    fun guardedBack() { if (vm.dirty) pendingBack=true else nav.popBackStack() }
    NavHost(nav, Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(nav) }
        composable(Routes.CREATE) { CreateScreen(nav, vm) }
        composable(Routes.OPEN) { OpenScreen(nav, vm) }
        composable(Routes.EDIT) { EditorScreen(nav, vm) }
        composable(Routes.STAFF) { StaffConfigurationScreen(nav, vm) }
        composable(Routes.SETTINGS) { SettingsScreen(nav) }
        composable(Routes.METADATA) { MetadataScreen(nav, vm) }
        composable(Routes.SHARE) { ShareScreen(nav, vm) }
    }
    if (pendingBack) {
        AlertDialog(onDismissRequest={pendingBack=false}, title={Text("Modifications non enregistrées")},
            text={Text("Quitter maintenant supprimera les modifications non sauvegardées." )},
            confirmButton={TextButton({ pendingBack=false; vm.save(); nav.popBackStack() }){Text("Quitter")}},
            dismissButton={TextButton({pendingBack=false}){Text("Continuer l’édition")}})
    }
}

@Composable private fun ScreenFrame(title:String,nav:NavHostController,content:@Composable ColumnScope.()->Unit) {
    Scaffold(topBar={TopAppBar(title={Text(title)},navigationIcon={IconButton({nav.popBackStack()}){Icon(Icons.Default.ArrowBack,"Retour")}})}) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(horizontal=16.dp,vertical=12.dp), content=content)
    }
}

@Composable private fun HomeScreen(nav:NavHostController) {
    Scaffold(containerColor=Color(0xFFF2F1EC)) { p ->
        Column(Modifier.fillMaxSize().padding(p).padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally) {
            Spacer(Modifier.height(24.dp)); Text("Notation Studio",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.SemiBold)
            Text("atelier de composition musicale",style=MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(28.dp)); HomeAction("Nouvelle partition","Créer une partition vierge",Icons.Default.Add){nav.navigate(Routes.CREATE)}
            HomeAction("Ouvrir une partition","Parcourir les fichiers enregistrés",Icons.Default.FolderOpen){nav.navigate(Routes.OPEN)}
            HomeAction("Paramètres","Préférences de l’éditeur",Icons.Default.Settings){nav.navigate(Routes.SETTINGS)}
            Spacer(Modifier.weight(1f)); Text("édition musicale native · 0.2",style=MaterialTheme.typography.labelSmall)
        }
    }
}
@Composable private fun HomeAction(title:String,subtitle:String,icon:androidx.compose.ui.graphics.vector.ImageVector,onClick:()->Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical=5.dp).clickable(onClick=onClick),shape=RoundedCornerShape(8.dp),border=BorderStroke(1.dp,Color(0xFFCAC8C0))) {
        Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Icon(icon,null);Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)){Text(title,fontWeight=FontWeight.Medium);Text(subtitle,style=MaterialTheme.typography.bodySmall)};Icon(Icons.Default.ChevronRight,null)}
    }
}

@Composable private fun CreateScreen(nav:NavHostController,vm:AppViewModel) {
    var page by rememberSaveable{mutableIntStateOf(0)}; var title by rememberSaveable{mutableStateOf("Nouvelle partition")}; var subtitle by rememberSaveable{mutableStateOf("")}
    var composer by rememberSaveable{mutableStateOf("")}; var copyright by rememberSaveable{mutableStateOf("")}; var encoder by rememberSaveable{mutableStateOf("Notation Studio")}; var source by rememberSaveable{mutableStateOf("")}
    var minor by rememberSaveable{mutableStateOf(false)}; var fifths by rememberSaveable{mutableIntStateOf(0)}; var beats by rememberSaveable{mutableIntStateOf(4)}; var unit by rememberSaveable{mutableIntStateOf(4)}; var pickup by rememberSaveable{mutableStateOf(false)}; var tempo by rememberSaveable{mutableIntStateOf(120)}
    var instruments by rememberSaveable{mutableStateOf(listOf("Piano"))}
    ScreenFrame("Créer une partition",nav) {
        Text("Étape ${page+1} / 3",style=MaterialTheme.typography.labelLarge); Spacer(Modifier.height(8.dp))
        when(page){
            0 -> { Section("Métadonnées"); Field("Titre",title){title=it}; Field("Sous-titre",subtitle){subtitle=it}; Field("Compositeur",composer){composer=it}; Field("Droits / copyright",copyright){copyright=it}; Field("Encodeur",encoder){encoder=it}; Field("Source",source){source=it} }
            1 -> { Section("Paramètres musicaux"); Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(!minor,{minor=false},{Text("Majeur")});FilterChip(minor,{minor=true},{Text("Mineur")})}; SelectRow("Tonalité",keyName(fifths,minor),{fifths=if(fifths>=6)-6 else fifths+1}); Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){NumberField("Temps",beats){beats=it.coerceIn(1,12)};NumberField("Unité",unit){unit=if(it in listOf(2,4,8,16))it else 4}}; Row(verticalAlignment=Alignment.CenterVertically){Switch(pickup,{pickup=it});Text("Mesure de pickup")};NumberField("Tempo (BPM)",tempo){tempo=it.coerceIn(30,300)} }
            2 -> { Section("Instruments"); Text("Sélectionnez les portées qui seront créées.",style=MaterialTheme.typography.bodySmall); InstrumentPicker(instruments){instruments=it} }
        }
        Spacer(Modifier.weight(1f)); Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween){TextButton({if(page==0)nav.popBackStack() else page--}){Text(if(page==0)"Annuler" else "Précédent")};Button({if(page<2)page++ else {val score=buildScore(title,subtitle,composer,copyright,encoder,source,minor,fifths,beats,unit,pickup,tempo,instruments);vm.begin(score);nav.navigate(Routes.EDIT)}}){Text(if(page<2)"Suivant" else "Créer")}}
    }
}
private fun buildScore(title:String,subtitle:String,composer:String,copyright:String,encoder:String,source:String,minor:Boolean,fifths:Int,beats:Int,unit:Int,pickup:Boolean,tempo:Int,instruments:List<String>):Score {
    val ts=TimeSignature(beats,unit); val defs=instruments.mapNotNull{n->instrumentCatalog.find{it.name==n}}; val parts=defs.map{d->Part(instrument=d.name,displayName=d.name,staves=listOf(Staff(instrument=d.name,displayName=d.name,clef=d.clef,measures=listOf(Measure(number=1,timeSignature=ts,theoreticalDuration=beats.toDouble()/unit))))) }
    return Score(metadata=Metadata(title,subtitle,composer,copyright,encoder,source),tempo=Tempo(tempo),keySignature=KeySignature(fifths,minor),timeSignature=ts,pickup=pickup,parts=parts)
}

@Composable private fun InstrumentPicker(selected:List<String>,onChange:(List<String>)->Unit){var group by rememberSaveable{mutableStateOf("Claviers")};var show by rememberSaveable{mutableStateOf(false)};val groups=instrumentCatalog.map{it.group}.distinct();Column(Modifier.fillMaxWidth()){Row(Modifier.horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)){groups.forEach{g->FilterChip(group==g,{group=g},{Text(g)})}};Spacer(Modifier.height(8.dp));instrumentCatalog.filter{it.group==group}.forEach{d->val checked=d.name in selected;ListItem(headlineContent={Text(d.name)},supportingContent={Text(d.clef.name.lowercase())},leadingContent={Checkbox(checked,{onChange(if(checked)selected-d.name else selected+d.name)})},modifier=Modifier.clickable{onChange(if(checked)selected-d.name else selected+d.name)})};if(selected.isNotEmpty()){Spacer(Modifier.height(8.dp));Text("Sélection : ${selected.joinToString()}",style=MaterialTheme.typography.labelMedium)}}}

@Composable private fun OpenScreen(nav:NavHostController,vm:AppViewModel){val context=LocalContext.current;var files by remember{mutableStateOf(loadScoreSummaries(context))};var deleting by remember{mutableStateOf<String?>(null)};ScreenFrame("Ouvrir",nav){if(files.isEmpty()){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.LibraryMusic,null);Text("Aucune partition enregistrée");Text("Créez votre première partition depuis l’accueil.",style=MaterialTheme.typography.bodySmall)}}}else{LazyColumn{items(files){entry->ListItem(headlineContent={Text(entry.first)},supportingContent={Text(entry.second)},trailingContent={IconButton({deleting=entry.first}){Icon(Icons.Default.Delete,"Supprimer")}},modifier=Modifier.clickable{vm.begin(buildScore(entry.first,"","","","Notation Studio","",false,0,4,4,false,120,listOf("Piano")));nav.navigate(Routes.EDIT)})}}};if(deleting!=null)AlertDialog(onDismissRequest={deleting=null},title={Text("Supprimer la partition ?")},text={Text("Cette action est irréversible.")},confirmButton={TextButton({files=files.filterNot{it.first==deleting};saveScoreSummaries(context,files);deleting=null}){Text("Supprimer")}},dismissButton={TextButton({deleting=null}){Text("Annuler")}})}}}

@Composable private fun EditorScreen(nav:NavHostController,vm:AppViewModel){val score=vm.score?:buildScore("Nouvelle partition","","","","Notation Studio","",false,0,4,4,false,120,listOf("Piano"));var voice by rememberSaveable{mutableIntStateOf(1)};var playing by rememberSaveable{mutableStateOf(false)};var zoom by rememberSaveable{mutableFloatStateOf(1f)};var selectedTool by rememberSaveable{mutableStateOf("Note")};var openGroup by rememberSaveable{mutableStateOf("Basique")};Scaffold(topBar={TopAppBar(title={Text(score.metadata.title)},navigationIcon={IconButton({if(vm.dirty)nav.navigate(Routes.HOME) else nav.popBackStack()}){Icon(Icons.Default.ArrowBack,"Retour")}},actions={IconButton({playing=!playing}){Icon(if(playing)Icons.Default.Pause else Icons.Default.PlayArrow,"Lecture")};IconButton({nav.navigate(Routes.METADATA)}){Icon(Icons.Default.Info,"Métadonnées")};IconButton({nav.navigate(Routes.SHARE)}){Icon(Icons.Default.Share,"Partager")}})},bottomBar={EditorToolBar(voice,openGroup,{voice=it},{openGroup=if(openGroup==it)"" else it},{selectedTool=it})}){p->Column(Modifier.fillMaxSize().padding(p)){Row(Modifier.fillMaxWidth().background(Color(0xFFE5E3DC)).padding(6.dp),verticalAlignment=Alignment.CenterVertically){Text("Voix $voice",fontWeight=FontWeight.Medium);Spacer(Modifier.width(12.dp));Text(selectedTool);Spacer(Modifier.weight(1f));Text("${(zoom*100).toInt()}%");Slider(zoom,{zoom=it},Modifier.width(120.dp),valueRange=.6f..2f)};Box(Modifier.fillMaxSize()){AndroidView(factory={ctx->ScoreCanvasView(ctx)},modifier=Modifier.fillMaxSize(),update={it.setScore(score);it.setZoom(zoom);it.setSelectedVoice(voice)})}}}}

@Composable private fun EditorToolBar(voice:Int,open:String,onVoice:(Int)->Unit,onGroup:(String)->Unit,onTool:(String)->Unit){Column(Modifier.fillMaxWidth().background(Color(0xFFF0EFEA))){Row(Modifier.horizontalScroll(rememberScrollState()).padding(4.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)){listOf("Basique","Notes","Add-one","Ornements","Dynamiques","Fonctions").forEach{g->FilterChip(open==g,{onGroup(g)},{Text(g)})}};if(open.isNotEmpty()){Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal=6.dp,vertical=2.dp),horizontalArrangement=Arrangement.spacedBy(4.dp)){val tools=when(open){"Notes"->listOf("Note","Pause","Point","Altération");"Add-one"->listOf("Grace","Tuplet","Tie","Slur");"Ornements"->listOf("Trille","Mordant","Appoggiature");"Dynamiques"->listOf("pp","p","mf","f","ff");"Fonctions"->listOf("Mesure +","Mesure -","Supprimer","Sélection");else->listOf("Sélection","Déplacer","Ajouter")};tools.forEach{TextButton({onTool(it)}){Text(it)}}}};Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center){(1..4).forEach{TextButton({onVoice(it)}){Text("v$it")}}}}}

@Composable private fun StaffConfigurationScreen(nav:NavHostController,vm:AppViewModel){val score=vm.score?:return;var parts by remember{mutableStateOf(score.parts)};ScreenFrame("Configuration des portées",nav){LazyColumn(Modifier.weight(1f)){items(parts){part->val staff=part.staves.firstOrNull();Card(Modifier.fillMaxWidth().padding(vertical=4.dp)){ListItem(headlineContent={Text(part.displayName)},supportingContent={Text("Clé : ${staff?.clef?.name?.lowercase()?:"—"}")},leadingContent={Checkbox(true,{})},trailingContent={Row{IconButton({parts=parts.sortedBy{it.displayName}}){Icon(Icons.Default.KeyboardArrowUp,"Monter")};IconButton({parts=parts.sortedByDescending{it.displayName}}){Icon(Icons.Default.KeyboardArrowDown,"Descendre")};IconButton({parts=parts.filterNot{it.id==part.id}}){Icon(Icons.Default.Delete,"Supprimer")}}})}}};Button({parts=parts+Part(instrument="Piano",displayName="Piano",staves=listOf(Staff(instrument="Piano",displayName="Piano",clef=Clef.TREBLE)))}){Icon(Icons.Default.Add,null);Spacer(Modifier.width(6.dp));Text("Ajouter une portée")};Spacer(Modifier.height(8.dp));Button({vm.update(score.copy(parts=parts));nav.popBackStack()}){Text("Appliquer")}}

@Composable private fun SettingsScreen(nav:NavHostController){val context=LocalContext.current;fun b(k:String)=context.getSharedPreferences("settings",Context.MODE_PRIVATE).getBoolean(k,true);var prompt by remember{mutableStateOf(b("prompt"))};var autoScroll by remember{mutableStateOf(b("autoScroll"))};var highlight by remember{mutableStateOf(b("highlight"))};var touchLine by remember{mutableStateOf(b("touchLine"))};var measureNumbers by remember{mutableStateOf(b("measureNumbers"))};var editorScale by remember{mutableFloatStateOf(context.getSharedPreferences("settings",0).getFloat("editorScale",1f))};var scoreScale by remember{mutableFloatStateOf(context.getSharedPreferences("settings",0).getFloat("scoreScale",1f))};ScreenFrame("Paramètres",nav){LazyColumn{item{Section("Éditeur")};item{SettingSwitch("Prompt tone à l’ajout d’une note",prompt){prompt=it;persist(context,"prompt",it)}};item{SettingSwitch("Défilement automatique",autoScroll){autoScroll=it;persist(context,"autoScroll",it)}};item{SettingSwitch("Highlight des nouveaux éléments",highlight){highlight=it;persist(context,"highlight",it)}};item{SettingSwitch("Ligne de toucher",touchLine){touchLine=it;persist(context,"touchLine",it)}};item{SettingSwitch("Afficher les numéros de mesure",measureNumbers){measureNumbers=it;persist(context,"measureNumbers",it)}};item{SettingSlider("Échelle de l’éditeur",editorScale){editorScale=it;context.getSharedPreferences("settings",0).edit().putFloat("editorScale",it).apply()}};item{SettingSlider("Échelle de la vue score",scoreScale){scoreScale=it;context.getSharedPreferences("settings",0).edit().putFloat("scoreScale",it).apply()}};item{Spacer(Modifier.height(12.dp));Button({Toast.makeText(context,"Fichiers d’exemple restaurés",Toast.LENGTH_SHORT).show()}){Text("Restaurer les fichiers d’exemple")}}}}

@Composable private fun MetadataScreen(nav:NavHostController,vm:AppViewModel){val s=vm.score?:return;var title by rememberSaveable{mutableStateOf(s.metadata.title)};var subtitle by rememberSaveable{mutableStateOf(s.metadata.subtitle)};var composer by rememberSaveable{mutableStateOf(s.metadata.composer)};var copyright by rememberSaveable{mutableStateOf(s.metadata.copyright)};var encoder by rememberSaveable{mutableStateOf(s.metadata.encoder)};var source by rememberSaveable{mutableStateOf(s.metadata.source)};var tempo by rememberSaveable{mutableIntStateOf(s.tempo.bpm)};ScreenFrame("Métadonnées",nav){Field("Titre",title){title=it};Field("Sous-titre",subtitle){subtitle=it};Field("Compositeur",composer){composer=it};Field("Droits / copyright",copyright){copyright=it};Field("Encodeur",encoder){encoder=it};Field("Source",source){source=it};NumberField("Tempo (BPM)",tempo){tempo=it.coerceIn(30,300)};Spacer(Modifier.weight(1f));Button({vm.update(s.copy(metadata=Metadata(title,subtitle,composer,copyright,encoder,source),tempo=Tempo(tempo)));nav.popBackStack()}){Text("Enregistrer")}}

@Composable private fun ShareScreen(nav:NavHostController,vm:AppViewModel){val context=LocalContext.current;var busy by remember{mutableStateOf(false)};ScreenFrame("Partager / exporter",nav){ShareAction("MusicXML","Exporter la partition dans un format d’échange",Icons.Default.Description){shareText(context,"MusicXML",musicXmlStub(vm.score))};ShareAction("MIDI","Partager les événements MIDI",Icons.Default.AudioFile){shareText(context,"MIDI","MIDI export — ${vm.score?.metadata?.title?:"Partition"}")};ShareAction("Image de partition","Générer une image du score",Icons.Default.Image){busy=true;Toast.makeText(context,"Génération de l’image en préparation",Toast.LENGTH_SHORT).show();busy=false};if(busy)CircularProgressIndicator()}}

@Composable private fun ShareAction(title:String,subtitle:String,icon:androidx.compose.ui.graphics.vector.ImageVector,onClick:()->Unit){ListItem(headlineContent={Text(title)},supportingContent={Text(subtitle)},leadingContent={Icon(icon,null)},trailingContent={Icon(Icons.Default.ChevronRight,null)},modifier=Modifier.clip(RoundedCornerShape(8.dp)).clickable(onClick=onClick))}
@Composable private fun Section(text:String){Text(text,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold,modifier=Modifier.padding(vertical=8.dp))}
@Composable private fun Field(label:String,value:String,onChange:(String)->Unit){OutlinedTextField(value,onChange,label={Text(label)},singleLine=true,modifier=Modifier.fillMaxWidth().padding(vertical=4.dp))}
@Composable private fun NumberField(label:String,value:Int,onChange:(Int)->Unit){OutlinedTextField(value.toString(),{onChange(it.filter(Char::isDigit).toIntOrNull()?:value)},label={Text(label)},singleLine=true,modifier=Modifier.width(150.dp).padding(vertical=4.dp))}
@Composable private fun SelectRow(label:String,value:String,onClick:()->Unit){ListItem(headlineContent={Text(label)},supportingContent={Text(value)},trailingContent={TextButton(onClick){Text("Changer")}},modifier=Modifier.clickable(onClick=onClick))}
@Composable private fun SettingSwitch(label:String,value:Boolean,onChange:(Boolean)->Unit){ListItem(headlineContent={Text(label)},trailingContent={Switch(value,onChange)})}
@Composable private fun SettingSlider(label:String,value:Float,onChange:(Float)->Unit){Column(Modifier.padding(vertical=8.dp)){Text(label);Slider(value,onChange,valueRange=.6f..1.6f);Text("${(value*100).toInt()}%",style=MaterialTheme.typography.labelSmall)}}
private fun persist(c:Context,key:String,value:Boolean)=c.getSharedPreferences("settings",0).edit().putBoolean(key,value).apply()
private fun keyName(fifths:Int,minor:Boolean):String{val major=listOf("C","G","D","A","E","B","F♯","D♭","A♭","E♭","B♭","F","C♭");return (major[fifths.coerceIn(-6,6)+6])+if(minor)" mineur" else " majeur"}
private fun musicXmlStub(score:Score?):String="<?xml version=\"1.0\"?><score-partwise><work><work-title>${score?.metadata?.title?:"Partition"}</work-title></work></score-partwise>"
private fun shareText(c:Context,title:String,text:String){val i=Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_SUBJECT,title);putExtra(Intent.EXTRA_TEXT,text)};c.startActivity(Intent.createChooser(i,"Partager via"))}
private fun loadScoreSummaries(c:Context):List<Pair<String,String>>{val raw=c.getSharedPreferences("scores",0).getStringSet("items",emptySet()).orEmpty();return raw.map{it to "Enregistrée ${DateFormat.getDateTimeInstance().format(Date())}"}.sortedBy{it.first}}
private fun saveScoreSummaries(c:Context,items:List<Pair<String,String>>){c.getSharedPreferences("scores",0).edit().putStringSet("items",items.map{it.first}.toSet()).apply()}
