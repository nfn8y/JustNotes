package com.nfn8y.notesapp.desktop

import com.nfn8y.notesapp.common.model.Note
import com.nfn8y.notesapp.common.repository.NoteRepository // Interface
import com.nfn8y.notesapp.common.usecase.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class DesktopNotesViewModel(
    private val noteRepository: NoteRepository, // Injected
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
) {
    private val getNotesUseCase = GetNotesUseCase(noteRepository)
    private val addNoteUseCase = AddNoteUseCase(noteRepository)
    private val getNoteByIdUseCase = GetNoteByIdUseCase(noteRepository)
    private val updateNoteUseCase = UpdateNoteUseCase(noteRepository)
    private val deleteNoteUseCase = DeleteNoteUseCase(noteRepository)

    private val _selectedNoteId = MutableStateFlow<Long?>(null) // Changed to Long?

    val selectedNote: StateFlow<Note?> = _selectedNoteId.flatMapLatest { id ->
        if (id == null) flowOf(null) else flow { emit(getNoteByIdUseCase(id)) }
            .flowOn(Dispatchers.IO) // Perform DB operation on IO dispatcher
    }.stateIn(coroutineScope, SharingStarted.Lazily, null)

    val notes: StateFlow<List<Note>> = getNotesUseCase()
        .stateIn(coroutineScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    fun addNote(title: String, content: String) {
        coroutineScope.launch(Dispatchers.IO) { // Perform DB operation on IO dispatcher
            val newNote = addNoteUseCase(title, content)
            withContext(Dispatchers.Main) { // Switch back to Main for UI update
                _selectedNoteId.value = newNote.id
            }
        }
    }

    fun updateNote(noteToUpdate: Note, newTitle: String, newContent: String) {
        coroutineScope.launch(Dispatchers.IO) {
            val updatedNoteInstance = noteToUpdate.copy(
                title = newTitle,
                content = newContent,
                updatedAt = Clock.System.now()
            )
            updateNoteUseCase(updatedNoteInstance)
            // Optionally refresh selected note
            if (_selectedNoteId.value == updatedNoteInstance.id) {
                withContext(Dispatchers.Main){
                    _selectedNoteId.value = null // Force re-fetch or rely on flow from getAllNotes
                    _selectedNoteId.value = updatedNoteInstance.id
                }
            }
        }
    }

    fun deleteNote(noteId: Long) { // Changed to Long
        coroutineScope.launch(Dispatchers.IO) {
            deleteNoteUseCase(noteId)
            if (_selectedNoteId.value == noteId) {
                withContext(Dispatchers.Main){
                    _selectedNoteId.value = null
                }
            }
        }
    }

    fun selectNoteById(noteId: Long?) { // Changed to Long?
        _selectedNoteId.value = noteId
    }
}
