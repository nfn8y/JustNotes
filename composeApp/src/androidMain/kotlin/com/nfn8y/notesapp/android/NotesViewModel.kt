package com.nfn8y.notesapp.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfn8y.notesapp.common.model.Note
import com.nfn8y.notesapp.common.repository.NoteRepository
import com.nfn8y.notesapp.common.usecase.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotesViewModel(
    private val noteRepository: NoteRepository // Injected
) : ViewModel() {

    private val getNotesUseCase = GetNotesUseCase(noteRepository)
    private val addNoteUseCase = AddNoteUseCase(noteRepository)
    private val getNoteByIdUseCase = GetNoteByIdUseCase(noteRepository)
    private val updateNoteUseCase = UpdateNoteUseCase(noteRepository)
    private val deleteNoteUseCase = DeleteNoteUseCase(noteRepository)

    val notes: StateFlow<List<Note>> = getNotesUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun addNote(title: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            addNoteUseCase(title, content)
            // UI will update via the notes flow
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch(Dispatchers.IO) {
            updateNoteUseCase(note.copy(updatedAt = kotlinx.datetime.Clock.System.now()))
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteNoteUseCase(noteId)
        }
    }

    suspend fun getNoteById(noteId: Long): Note? {
        return withContext(Dispatchers.IO) {
            getNoteByIdUseCase(noteId)
        }
    }
}