import SwiftUI
import Combine // If using the Combine extensions
import ComposeApp // Your KMP shared module
import KMPNativeCoroutinesCore // Import the core if needed for specific types
import KMPNativeCoroutinesCombine // Import this for Flow -> Publisher

@MainActor
class IOSNotesViewModel: ObservableObject {
    @Published var notes: [Note] = []
    @Published var errorMessage: String? = nil
    private var cancellables = Set<AnyCancellable>()

    private let repository: NoteRepository = InMemoryNoteRepository.shared // Your shared repository

    init() {
        observeNotes()
    }

    func observeNotes() {
        // 'createPublisher(for: ...)' comes from KMPNativeCoroutinesCombine
        // This converts your Kotlin Flow to a Combine Publisher
        let notesPublisher = createPublisher(for: repository.getAllNotesFlow())

        notesPublisher
        .receive(on: DispatchQueue.main) // Ensure UI updates are on the main thread
        .sink(receiveCompletion: { [weak self] completion in
            if case .failure(let error) = completion {
                // The error here is a Swift 'Error', converted from Kotlin's Throwable
                self?.errorMessage = "Failed to load notes: \(error.localizedDescription)"
                print("Error observing notes: \(error)")
            }
        }, receiveValue: { [weak self] notesListFromFlow in
            // notesListFromFlow is already your Swift-compatible List<Note>
            self?.notes = notesListFromFlow
            self?.errorMessage = nil
        })
        .store(in: &cancellables) // Manage the subscription lifecycle
    }

    // Example for calling a suspend function (if you had one for adding)
    // For this, you would use @NativeCoroutines // or @NativeCoroutinesState for StateFlows
    // on your Kotlin suspend functions or Flows in the shared module.
    // Then KMP-NativeCoroutines generates Swift-friendly async versions.

    // For example, if your Kotlin repository.addNote was:
    // @NativeCoroutines
    // suspend fun addNote(note: Note) { /* ... */ }
    //
    // Then in Swift you could do:
    // func addNoteSwift(note: Note) async {
    //     do {
    //         try await asyncFunction(for: repository.addNoteNative(note: note)) // `addNoteNative` is generated
    //         // Refresh or rely on the flow
    //     } catch {
    //         self.errorMessage = "Failed to add note: \(error.localizedDescription)"
    //     }
    // }
}
