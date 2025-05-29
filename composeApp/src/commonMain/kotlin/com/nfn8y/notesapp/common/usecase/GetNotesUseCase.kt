package com.nfn8y.notesapp.common.usecase

import com.nfn8y.notesapp.common.model.Note
import com.nfn8y.notesapp.common.repository.NoteRepository
import kotlinx.coroutines.flow.Flow

class GetNotesUseCase(private val noteRepository: NoteRepository) {
    operator fun invoke(): Flow<List<Note>> { // Returns a Flow
        return noteRepository.getAllNotesFlow()
    }
}
