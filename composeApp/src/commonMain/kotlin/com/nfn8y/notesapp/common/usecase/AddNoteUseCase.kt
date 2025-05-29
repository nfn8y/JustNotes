package com.nfn8y.notesapp.common.usecase


import com.nfn8y.notesapp.common.model.Note
import com.nfn8y.notesapp.common.repository.NoteRepository
import kotlin.random.Random

class AddNoteUseCase(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(title: String, content: String) {
        val newNote = Note.createNew(
            // In a real app, ID generation might be more robust or handled by the database
            id = Random.nextInt().toString(),
            title = title,
            content = content
        )
        noteRepository.addNote(newNote)
    }
}