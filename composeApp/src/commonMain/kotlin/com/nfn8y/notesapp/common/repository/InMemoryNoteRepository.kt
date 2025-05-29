package com.nfn8y.notesapp.common.repository

import com.nfn8y.notesapp.common.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.Clock
import kotlin.random.Random // For unique IDs

// Make it a singleton or provide via DI
object InMemoryNoteRepository : NoteRepository {
    private val _notesFlow = MutableStateFlow<List<Note>>(emptyList())
    private val notes: MutableList<Note>
        get() = _notesFlow.value.toMutableList()


    override fun getAllNotesFlow(): Flow<List<Note>> {
        return _notesFlow.asStateFlow()
    }

    override suspend fun getNoteById(id: String): Note? {
        return notes.find { it.id == id }
    }

    override suspend fun addNote(note: Note) {
        val newNote = if (notes.any { it.id == note.id }) {
            // If ID exists, treat as an update or generate new ID if it's a truly new note
            // For simplicity, let's assume new notes come with a placeholder ID, or we generate one
            note.copy(id = Random.nextInt().toString(), updatedAt = Clock.System.now())
        } else {
            note
        }
        _notesFlow.update { currentNotes ->
            (currentNotes + newNote).sortedByDescending { it.updatedAt }
        }
    }

    override suspend fun updateNote(note: Note) {
        _notesFlow.update { currentNotes ->
            currentNotes.map {
                if (it.id == note.id) note.copy(updatedAt = Clock.System.now()) else it
            }.sortedByDescending { it.updatedAt }
        }
    }

    override suspend fun deleteNoteById(id: String) {
        _notesFlow.update { currentNotes ->
            currentNotes.filterNot { it.id == id }.sortedByDescending { it.updatedAt }
        }
    }

    // Helper to add some initial data
    init {
        val sampleNotes = listOf(
            Note.createNew(Random.nextInt().toString(), "Grocery List", "Milk, Eggs, Bread\n- Whole milk\n- Organic eggs"),
            Note.createNew(Random.nextInt().toString(), "Meeting Notes", "Discuss Q3 roadmap with team."),
            Note.createNew(Random.nextInt().toString(), "Book Ideas", "- Sci-fi novel\n- KMP tutorial book")
        )
        _notesFlow.value = sampleNotes.sortedByDescending { it.updatedAt }
    }
}