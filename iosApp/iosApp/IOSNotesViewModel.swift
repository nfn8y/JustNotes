//
// Created by Kumar Yashwant on 5/29/25.
// Copyright (c) 2025 orgName. All rights reserved.
//

import SwiftUI
import Combine
import ComposeApp // Your KMP shared module framework

// It's highly recommended to use KMP-NativeCoroutines or SKIE for bridging.
// The following assumes a simplified bridging for clarity of the UI logic.

@MainActor // Ensure UI updates are on the main thread
class IOSNotesViewModel: ObservableObject {
    @Published var notes: [Note] = []
    @Published var isLoading: Bool = false
    // No need for selectedNote here if navigation handles passing the note object

    private let repository: NoteRepository = InMemoryNoteRepository.shared // Accessing Kotlin singleton
    private var notesCancellable: AnyCancellable? // For KMP-NativeCoroutines or Combine publishers

    init() {
        observeNotes()
    }

    deinit {
        notesCancellable?.cancel()
    }

    func observeNotes() {
        isLoading = true

        // Ideal scenario with KMP-NativeCoroutines:
        // notesCancellable = createPublisher(for: repository.getAllNotesFlow())
        //     .subscribe(on: DispatchQueue.global()) // Background thread for collection
        //     .receive(on: DispatchQueue.main)     // Main thread for UI updates
        //     .handleEvents(receiveSubscription: { _ in self.isLoading = true })
        //     .sink(receiveCompletion: { completion in
        //         self.isLoading = false
        //         if case .failure(let error) = completion {
        //             print("Error observing notes: \(error.localizedDescription)")
        //         }
        //     }, receiveValue: { notesList in
        //         self.isLoading = false
        //         self.notes = notesList
        //     })

        // Fallback to manual collection (less ideal, needs careful threading)
        // This uses the KMP `FlowCollector` which can be verbose from Swift.
        // KMP-NativeCoroutines or SKIE abstract this away.
        repository.getAllNotesFlow().collect(collector: Collector<NSArray> { notesListFromKotlin in
            // This callback can be on any thread, ensure UI updates are on main
            DispatchQueue.main.async {
                self.notes = notesListFromKotlin as? [Note] ?? []
                self.isLoading = false // Assuming flow emits initial value quickly
            }
            return KotlinUnit() // Required by the Collector interface
        }) { error in // Completion handler for the flow
            DispatchQueue.main.async {
                self.isLoading = false
                if let e = error { // error is KotlinThrowable?
                    print("Error collecting notes flow: \(e.message ?? "Unknown error")")
                }
            }
            return KotlinUnit()
        }
    }

    // Call this if you want a one-time refresh, though observing Flow is better.
    func refreshNotesList() {
        // Re-trigger observation or have a separate one-shot fetch if needed.
        // For simplicity, re-calling observeNotes (which should ideally handle not re-subscribing if already active)
        observeNotes()
    }

    func addNote(title: String, content: String, completion: @escaping (Error?) -> Void) {
        let newNote = Note.companion.createNew(id: UUID().uuidString, title: title, content: content)

        // Calling Kotlin suspend function. KMP-NativeCoroutines or SKIE make this cleaner.
        // The `completionHandler` is how Kotlin `suspend` functions are often exposed.
        repository.addNote(note: newNote) { _, error_kt in
            // error_kt is KotlinThrowable?
            // Convert KotlinThrowable to Swift Error if necessary or handle directly.
            // The refresh is now handled by the Flow observation automatically.
            completion(error_kt?.asSwiftError()) // asSwiftError is a conceptual extension
        }
    }

    func updateNote(note: Note, newTitle: String, newContent: String, completion: @escaping (Error?) -> Void) {
        let updatedNote = note.copy(
            id: note.id, // Keep original ID
            title: newTitle,
            content: newContent,
            createdAt: note.createdAt, // Keep original creation timestamp
            updatedAt: Clock.shared.now() // Update 'updatedAt' timestamp
        )
        repository.updateNote(note: updatedNote) { _, error_kt in
            completion(error_kt?.asSwiftError())
        }
    }

    func deleteNote(noteId: String, completion: @escaping (Error?) -> Void) {
        repository.deleteNoteById(id: noteId) { _, error_kt in
            completion(error_kt?.asSwiftError())
        }
    }

    func deleteNoteFromList(offsets: IndexSet, completion: @escaping (Error?) -> Void) {
        let idsToDelete = offsets.map { notes[$0].id }
        var firstError: Error? = nil
        let group = DispatchGroup()

        idsToDelete.forEach { id in
            group.enter()
            deleteNote(noteId: id) { error in
                if error != nil && firstError == nil {
                    firstError = error
                }
                group.leave()
            }
        }
        group.notify(queue: .main) {
            completion(firstError) // Report first error if any
        }
    }
}

// Conceptual extension to convert KotlinThrowable to Swift Error
extension KotlinThrowable {
    func asSwiftError() -> Error {
        return NSError(domain: "KotlinError", code: 0, userInfo: [NSLocalizedDescriptionKey: self.message ?? "Unknown Kotlin error"])
    }
}
