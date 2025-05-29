package com.nfn8y.notesapp.desktop

import com.nfn8y.notesapp.common.model.Note
import com.nfn8y.notesapp.common.repository.InMemoryNoteRepository // Or inject
import com.nfn8y.notesapp.common.repository.NoteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock // For updating timestamp
import kotlin.random.Random // Or UUID

class DesktopNotesViewModel(
    private val noteRepository: NoteRepository = InMemoryNoteRepository, // Inject in real app
    // Use SupervisorJob so if one coroutine fails, others in the scope aren't cancelled
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {
    private val _selectedNoteId = MutableStateFlow<String?>(null) // Store ID instead of full object

    // Combines notes list and selectedNoteId to derive the selectedNote object
    val selectedNote: StateFlow<Note?> = combine(
        noteRepository.getAllNotesFlow(),
        _selectedNoteId
    ) { notes, selectedId ->
        notes.find { it.id == selectedId }
    }.stateIn(coroutineScope, SharingStarted.Lazily, null)


    val notes: StateFlow<List<Note>> = noteRepository.getAllNotesFlow()
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNote(title: String, content: String) {
        coroutineScope.launch {
            val newNote = Note.createNew(Random.nextInt().toString(), title, content)
            noteRepository.addNote(newNote)
            _selectedNoteId.value = newNote.id // Optionally select the new note
        }
    }

    fun updateNote(noteToUpdate: Note, newTitle: String, newContent: String) {
        coroutineScope.launch {
            val updatedNoteInstance = noteToUpdate.copy(
                title = newTitle,
                content = newContent,
                updatedAt = Clock.System.now()
            )
            noteRepository.updateNote(updatedNoteInstance)
        }
    }

    fun deleteNote(noteId: String) {
        coroutineScope.launch {
            noteRepository.deleteNoteById(noteId)
            if (_selectedNoteId.value == noteId) {
                _selectedNoteId.value = null // Clear selection if deleted
            }
        }
    }

    fun selectNoteById(noteId: String?) { // Can be null to deselect or indicate new note mode
        _selectedNoteId.value = noteId
    }
}