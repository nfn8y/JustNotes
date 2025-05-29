package com.nfn8y.notesapp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nfn8y.notesapp.android.ui.theme.AppTheme
import com.nfn8y.notesapp.common.model.Note

// Define navigation routes
sealed class Screen(val route: String) {
    object NoteList : Screen("noteList")
    object NoteDetail : Screen("noteDetail/{noteId}") {
        fun createRoute(noteId: String?) = if (noteId != null) "noteDetail/$noteId" else "noteDetail/new"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme { // Assuming you have a Theme.kt
                NotesAppNavigation()
            }
        }
    }
}


@Preview
@Composable
fun NotesAppNavigation(notesViewModel: NotesViewModel = viewModel()) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.NoteList.route) {
        composable(Screen.NoteList.route) {
            NoteListScreen(navController, notesViewModel)
        }
        composable(
            route = Screen.NoteDetail.route,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { backStackEntry ->
            val noteId = backStackEntry.arguments?.getString("noteId")
            NoteDetailScreen(navController, notesViewModel, if (noteId == "new") null else noteId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListScreen(navController: NavHostController, viewModel: NotesViewModel) {
    val notes by viewModel.notes.collectAsState()
    Scaffold(
        topBar = { TopAppBar(title = { Text("KMP Notes") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.NoteDetail.createRoute(null)) }) {
                Icon(Icons.Filled.Add, "Add Note")
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            items(notes, key = { it.id }) { note ->
                NoteListItem(note) {
                    navController.navigate(Screen.NoteDetail.createRoute(note.id))
                }
            }
        }
    }
}

@Composable
fun NoteListItem(note: Note, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(note.title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(note.content.take(100) + if (note.content.length > 100) "..." else "", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    navController: NavHostController,
    viewModel: NotesViewModel,
    noteId: String?
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var currentNote by remember { mutableStateOf<Note?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(noteId) {
        if (noteId != null && noteId != "new") {
            viewModel.getNoteById(noteId)?.let {
                currentNote = it
                title = it.title
                content = it.content
            }
        } else { // New note
            currentNote = null
            title = ""
            content = ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (noteId == "new" || currentNote == null) "New Note" else "Edit Note") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                if (noteId == "new" || currentNote == null) {
                    viewModel.addNote(title, content)
                } else {
                    currentNote?.let {
                        viewModel.updateNote(it.copy(title = title, content = content))
                    }
                }
                navController.popBackStack()
            }) {
                Text("Save") // Or Icon
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).padding(16.dp).fillMaxSize()) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text("Content") },
                modifier = Modifier.fillMaxWidth().weight(1f), // Takes remaining space
                maxLines = 10
            )
        }
    }
}