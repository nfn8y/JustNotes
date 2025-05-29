//
// Created by Kumar Yashwant on 5/29/25.
// Copyright (c) 2025 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

struct NoteDetailView: View {
    @ObservedObject var viewModel: IOSNotesViewModel
    var noteToEdit: Note? // Pass the note to edit, or nil for a new note

    @Environment(\.dismiss) var dismiss

    @State private var title: String = ""
    @State private var content: String = ""
    @State private var isSaving = false
    @State private var errorMessage: String? = nil

    private var isNewNote: Bool { noteToEdit == nil }

    var body: some View {
        NavigationView { // For the toolbar and title
            Form {
                Section(header: Text("Title")) {
                    TextField("Enter title", text: $title)
                        .disabled(isSaving)
                }
                Section(header: Text("Content")) {
                    TextEditor(text: $content)
                        .frame(minHeight: 200, idealHeight: 300, maxHeight: .infinity)
                        .disabled(isSaving)
                }

                if let errorMessage = errorMessage {
                    Section {
                        Text(errorMessage)
                            .foregroundColor(.red)
                    }
                }
            }
            .navigationTitle(isNewNote ? "New Note" : "Edit Note")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    Button("Cancel") {
                        dismiss()
                    }
                    .disabled(isSaving)
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    if isSaving {
                        ProgressView()
                    } else {
                        Button("Save") {
                            saveNote()
                        }
                            // Enable save only if title or content is not empty, or if it's an existing note
                        .disabled(title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
                                      content.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty &&
                                      isNewNote)
                    }
                }
            }
            .onAppear {
                if let note = noteToEdit {
                    self.title = note.title
                    self.content = note.content
                }
            }
        }
    }

    private func saveNote() {
        isSaving = true
        errorMessage = nil
        let trimmedTitle = title.trimmingCharacters(in: .whitespacesAndNewlines)
        let trimmedContent = content.trimmingCharacters(in: .whitespacesAndNewlines)

        if let existingNote = noteToEdit {
            viewModel.updateNote(note: existingNote, newTitle: trimmedTitle, newContent: trimmedContent) { error in
                isSaving = false
                if let error = error {
                    errorMessage = "Error updating note: \(error.localizedDescription)"
                } else {
                    dismiss()
                }
            }
        } else {
            viewModel.addNote(title: trimmedTitle, content: trimmedContent) { error in
                isSaving = false
                if let error = error {
                    errorMessage = "Error adding note: \(error.localizedDescription)"
                } else {
                    dismiss()
                }
            }
        }
    }
}
