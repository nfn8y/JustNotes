package com.nfn8y.notesapp.common.repository

import com.nfn8y.notesapp.common.model.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepository {
    fun getAllNotesFlow(): Flow<List<Note>>
    suspend fun getNoteById(id: String): Note?
    suspend fun addNote(note: Note)
    suspend fun updateNote(note: Note)
    suspend fun deleteNoteById(id: String)
}