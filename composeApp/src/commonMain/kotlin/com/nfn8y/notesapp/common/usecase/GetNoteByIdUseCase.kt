package com.nfn8y.notesapp.common.usecase

import com.nfn8y.notesapp.common.model.Note
import com.nfn8y.notesapp.common.repository.NoteRepository

class GetNoteByIdUseCase(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(id: Long): Note? {
        return noteRepository.getNoteById(id)
    }
}