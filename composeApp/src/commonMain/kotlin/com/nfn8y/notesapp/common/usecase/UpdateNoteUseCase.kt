package com.nfn8y.notesapp.common.usecase

import com.nfn8y.notesapp.common.model.Note
import com.nfn8y.notesapp.common.repository.NoteRepository

class UpdateNoteUseCase(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(note: Note) {
        // Ensure ID is not null before calling the repository
        if (note.id != null) {
            noteRepository.updateNote(note)
        } else {
            // Handle error: cannot update note without ID
            println("Error: Attempted to update a note without an ID.")
        }
    }
}