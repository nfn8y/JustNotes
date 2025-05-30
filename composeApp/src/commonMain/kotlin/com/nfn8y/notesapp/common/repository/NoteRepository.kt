package com.nfn8y.notesapp.common.repository

import com.nfn8y.notesapp.common.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotesFlow(): Flow<List<Note>>
    suspend fun getNoteById(id: Long): Note? // Changed to Long
    suspend fun addNote(title: String, content: String): Note // To return the Note with generated ID
    suspend fun updateNote(note: Note) // Note should have a non-null ID
    suspend fun deleteNoteById(id: Long) // Changed to Long
}
