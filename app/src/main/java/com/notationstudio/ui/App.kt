package com.notationstudio.ui

import android.view.ViewGroup
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.notationstudio.domain.model.Metadata
import com.notationstudio.domain.model.Score
import com.notationstudio.rendering.ScoreCanvasView

private object Routes { const val HOME="home"; const val CREATE="create"; const val OPEN="open"; const val EDIT="edit"; const val STAFF="staff"; const val SETTINGS="settings"; const val METADATA="metadata"; const val SHARE="share" }

@Composable fun NotationApp() {
    val nav = rememberNavController()
    var score by rememberSaveable { mutableStateOf(Score(metadata = Metadata()).metadata.title) }
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen({ nav.navigate(Routes.CREATE) }, { nav.navigate(Routes.OPEN) }) }
        composable(Routes.CREATE) { CreateScreen({ score = it; nav.navigate(Routes.EDIT) }, { nav.popBackStack() }) }
        composable(Routes.OPEN) { OpenScreen({ nav.navigate(Routes.EDIT) }, { nav.popBackStack() }) }
        composable(Routes.EDIT) { EditorScreen(score, { nav.navigate(Routes.STAFF) }, { nav.navigate(Routes.METADATA) }, { nav.navigate(Routes.SHARE) }, { nav.popBackStack() }) }
        composable(Routes.STAFF) { SimpleScreen("Configuration des portées") { nav.popBackStack() } }
        composable(Routes.SETTINGS) { SimpleScreen("Paramètres") { nav.popBackStack() } }
        composable(Routes.METADATA) { SimpleScreen("Métadonnées") { nav.popBackStack() } }
        composable(Routes.SHARE) { SimpleScreen("Partager la partition") { nav.popBackStack() } }
    }
}

@Composable private fun HomeScreen(onCreate:()->Unit,onOpen:()->Unit) { Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement=Arrangement.spacedBy(12.dp)) { Text("Notation Studio", style=MaterialTheme.typography.headlineMedium); Text("Composition musicale", style=MaterialTheme.typography.bodyLarge); Button(onCreate, Modifier.fillMaxWidth()){Text("Nouvelle partition")}; OutlinedButton(onOpen, Modifier.fillMaxWidth()){Text("Ouvrir une partition")}; Spacer(Modifier.weight(1f)); Text("Fondation native Android — 0.1.0") } }

@Composable private fun CreateScreen(onDone:(String)->Unit,onBack:()->Unit) { var title by rememberSaveable{mutableStateOf("Nouvelle partition")}; Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("Créer une partition",style=MaterialTheme.typography.headlineSmall);OutlinedTextField(title,{title=it},label={Text("Titre")},modifier=Modifier.fillMaxWidth());Button({onDone(title)},Modifier.fillMaxWidth()){Text("Continuer")};TextButton(onBack){Text("Annuler")}} }

@Composable private fun OpenScreen(onOpen:()->Unit,onBack:()->Unit) { Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){Text("Partitions",style=MaterialTheme.typography.headlineSmall);Text("Aucune partition enregistrée.");Button(onOpen){Text("Ouvrir une partition de démonstration")};TextButton(onBack){Text("Retour")}} }

@Composable private fun EditorScreen(title:String,onStaff:()->Unit,onMetadata:()->Unit,onShare:()->Unit,onBack:()->Unit) { Scaffold(topBar={TopAppBar(title={Text(title)},navigationIcon={TextButton(onBack){Text("Retour")}})}, bottomBar={Row(Modifier.fillMaxWidth().padding(4.dp),horizontalArrangement=Arrangement.SpaceEvenly){TextButton(onStaff){Text("Portées")};TextButton(onMetadata){Text("Infos")};TextButton(onShare){Text("Partager")}}}){p->AndroidView(factory={ctx->ScoreCanvasView(ctx)},modifier=Modifier.fillMaxSize().padding(p)){view->view.layoutParams=ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT)}} }

@Composable private fun SimpleScreen(title:String,onBack:()->Unit){Column(Modifier.fillMaxSize().padding(24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)){Text(title,style=MaterialTheme.typography.headlineSmall);Text("Écran de fondation — implémentation fonctionnelle à venir.");TextButton(onBack){Text("Retour")}}}
