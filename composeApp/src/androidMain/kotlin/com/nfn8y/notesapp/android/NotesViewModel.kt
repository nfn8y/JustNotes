package com.nfn8y.notesapp.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfn8y.notesapp.common.model.Note
import com.nfn8y.notesapp.common.repository.InMemoryNoteRepository
import com.nfn8y.notesapp.common.repository.NoteRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

class NotesViewModel(
    private val noteRepository: NoteRepository = InMemoryNoteRepository // In the real app, inject this
) : ViewModel() {

    val notes: StateFlow<List<Note>> = noteRepository.getAllNotesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addNote(title: String, content: String) {
        viewModelScope.launch {
            val newNote = Note.createNew(Random.nextInt().toString(), title, content)
            noteRepository.addNote(newNote)
        }
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            noteRepository.updateNote(note)
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.deleteNoteById(noteId)
        }
    }

    suspend fun getNoteById(noteId: String): Note? {
        return noteRepository.getNoteById(noteId)
    }
}