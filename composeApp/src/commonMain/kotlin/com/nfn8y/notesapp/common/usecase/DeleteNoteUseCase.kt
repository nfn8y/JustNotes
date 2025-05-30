package com.nfn8y.notesapp.common.usecase

import com.nfn8y.notesapp.common.repository.NoteRepository

class DeleteNoteUseCase(private val noteRepository: NoteRepository) {
    suspend operator fun invoke(id: Long) {
        noteRepository.deleteNoteById(id)
    }
}