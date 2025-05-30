package com.nfn8y.notesapp.desktop

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.VerticalScrollbar
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollbarAdapter
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.nfn8y.notesapp.common.model.Note
import com.nfn8y.notesapp.desktop.ui.theme.AppThemeMode
import com.nfn8y.notesapp.desktop.ui.theme.DesktopAppTheme
// import kotlinx.coroutines.launch // Not directly needed in UI with StateFlow

@Composable
@Preview
fun AppPreview() { // For previewing in IntelliJ,
    // You might need to provide a CoroutineScope for the preview if ViewModel uses it in init
    val previewViewModel = DesktopNotesViewModel()
    DesktopApp(previewViewModel)
}

fun main() = application {
    val viewModel = DesktopNotesViewModel()
    Window(
        onCloseRequest = ::exitApplication,
        title = "KMP Notes - Desktop",
        state = rememberWindowState(width = 1000.dp, height = 700.dp) // Set initial window size
    ) {
        DesktopApp(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopApp(viewModel: DesktopNotesViewModel) {
    var appThemeMode by remember { mutableStateOf(AppThemeMode.System) }
    val notes by viewModel.notes.collectAsState()
    val selectedNote by viewModel.selectedNote.collectAsState()
    // Used to signal if the detail pane should be in "new note" mode
    var isCreatingNewNote by remember { mutableStateOf(false) }


    DesktopAppTheme(appThemeMode = appThemeMode) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("KMP Notes (Desktop)") },
                    actions = {
                        Text("Theme:", modifier = Modifier.align(Alignment.CenterVertically).padding(end = 8.dp))
                        Button(onClick = { appThemeMode = AppThemeMode.Light }, modifier = Modifier.padding(horizontal=2.dp)) { Text("L") }
                        Button(onClick = { appThemeMode = AppThemeMode.Dark }, modifier = Modifier.padding(horizontal=2.dp)) { Text("D") }
                        Button(onClick = { appThemeMode = AppThemeMode.System }, modifier = Modifier.padding(horizontal=2.dp)) { Text("S") }
                    }
                )
            }
        ) { innerPadding ->
            Row(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
                NoteListPane(
                    notes = notes,
                    onNoteSelected = { note ->
                        isCreatingNewNote = false
                        viewModel.selectNoteById(note.id)
                    },
                    onAddNewNote = {
                        isCreatingNewNote = true
                        viewModel.selectNoteById(null) // Deselect any current note
                    },
                    modifier = Modifier.fillMaxWidth(0.35f) // List takes 35% of width
                )
                VerticalDivider(modifier = Modifier.fillMaxHeight()) // Visual separator
                NoteDetailPane(
                    // Pass null if creating new, otherwise the selected note
                    noteToEdit = if (isCreatingNewNote) null else selectedNote,
                    onSaveNote = { title, content, existingNote ->
                        if (existingNote != null) { // Editing existing note
                            viewModel.updateNote(existingNote, title, content)
                        } else { // Adding new note
                            viewModel.addNote(title, content)
                        }
                        isCreatingNewNote = false // Reset flag after save
                        // ViewModel's addNote can optionally select the new note by ID
                    },
                    onDeleteNote = { noteId ->
                        viewModel.deleteNote(noteId)
                        isCreatingNewNote = false // Ensure not in new note mode if a delete happens
                    },
                    modifier = Modifier.weight(1f) // Detail takes remaining space
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteListPane(
    notes: List<Note>,
    onNoteSelected: (Note) -> Unit,
    onAddNewNote: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(8.dp)) {
        Button(onClick = onAddNewNote, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Add, contentDescription = "Add new note")
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("New Note")
        }
        Spacer(modifier = Modifier.height(8.dp))
        if (notes.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No notes yet. Click 'New Note' to start.")
            }
        } else {
            val listState = rememberLazyListState()
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(end = 12.dp /* for scrollbar room */)) {
                    items(notes, key = { it.id }) { note ->
                        NoteListItem(note = note, onClick = { onNoteSelected(note) })
                    }
                }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(scrollState = listState)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class) // Added for Card
@Composable
fun NoteListItem(note: Note, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(note.title.ifEmpty { "(No Title)" }, style = MaterialTheme.typography.titleMedium, maxLines = 1)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                note.content.lines().firstOrNull()?.take(120) ?: "(No Content)", // Show first line
                style = MaterialTheme.typography.bodySmall,
                maxLines = 2
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailPane(
    noteToEdit: Note?, // Null means creating a new note
    onSaveNote: (title: String, content: String, existingNote: Note?) -> Unit,
    onDeleteNote: (noteId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Remember title and content, re-initialize when noteToEdit changes
    var title by remember(noteToEdit) { mutableStateOf(noteToEdit?.title ?: "") }
    var content by remember(noteToEdit) { mutableStateOf(noteToEdit?.content ?: "") }

    // This condition determines if the detail pane should be active for editing/new note
    // or show a placeholder if no note is selected and not in new note mode.
    val isActive = noteToEdit != null || (noteToEdit == null && (title.isNotEmpty() || content.isNotEmpty() || remember {mutableStateOf(true)}.value /* this part is to keep new note open if fields are empty initially */))


    Column(modifier = modifier.padding(16.dp).fillMaxSize()) {
        if (noteToEdit == null && title.isEmpty() && content.isEmpty() && !remember { mutableStateOf(noteToEdit == null).value }) { // Initial state: no selection, not actively creating
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Select a note to view/edit, or create a new one.")
            }
            return@Column
        }

        Text(if (noteToEdit == null) "New Note" else "Edit Note", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            label = { Text("Content") },
            modifier = Modifier.fillMaxWidth().weight(1f), // Takes remaining space
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (noteToEdit != null) { // Show delete button only if editing an existing note
                OutlinedButton(
                    onClick = { onDeleteNote(noteToEdit.id) },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Filled.Delete, "Delete Note")
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Delete")
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Button(
                onClick = { onSaveNote(title, content, noteToEdit) },
                // enabled = title.isNotBlank() || content.isNotBlank() // Optionally enable save only if there's content
            ) {
                Text("Save")
            }
        }
    }
}
