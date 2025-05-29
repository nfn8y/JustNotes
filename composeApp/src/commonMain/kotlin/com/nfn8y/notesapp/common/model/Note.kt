package com.nfn8y.notesapp.common.model

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

data class Note(
    val id: String, // Or Long, UUID, etc.
    val title: String,
    val content: String,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        // Potentially a factory function for new notes
        fun createNew(id: String, title: String, content: String): Note {
            val now = Clock.System.now()
            return Note(id, title, content, now, now)
        }
    }
}