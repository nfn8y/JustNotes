//
// Created by Kumar Yashwant on 5/29/25.
// Copyright (c) 2025 orgName. All rights reserved.
//

import SwiftUI
import ComposeApp

struct NoteListView: View {
    @StateObject private var viewModel = IOSNotesViewModel()
    @State private var showingNoteDetailSheet = false // For presenting a sheet for new/edit
    @State private var noteToEdit: Note? = nil   // To pass to the detail sheet for editing

    var body: some View {
        NavigationView {
            VStack {
                if viewModel.isLoading && viewModel.notes.isEmpty {
                    ProgressView("Loading Notes...")
                        .padding()
                } else if viewModel.notes.isEmpty {
                    Text("No notes yet. Tap '+' to add one.")
                        .foregroundColor(.gray)
                        .padding()
                } else {
                    List {
                        ForEach(viewModel.notes, id: \.id) { note in
                            Button(action: {
                                self.noteToEdit = note
                                self.showingNoteDetailSheet = true
                            }) {
                                NoteRow(note: note)
                            }
                        }
                        .onDelete { indexSet in
                            viewModel.deleteNoteFromList(offsets: indexSet) { error in
                                if let error = error {
                                    print("Error deleting notes from list: \(error.localizedDescription)")
                                    // Optionally show an alert to the user
                                }
                                // UI will update automatically if Flow collection is working
                            }
                        }
                    }
                }
            }
            .navigationTitle("KMP Notes (iOS)")
            .toolbar {
                ToolbarItem(placement: .navigationBarLeading) {
                    EditButton() // Works with .onDelete in List
                }
                ToolbarItem(placement: .navigationBarTrailing) {
                    Button {
                        self.noteToEdit = nil // Signal for new note
                        self.showingNoteDetailSheet = true
                    } label: {
                        Image(systemName: "plus.circle.fill")
                            .imageScale(.large)
                    }
                }
            }
            .sheet(isPresented: $showingNoteDetailSheet) {
                // This closure is called when the sheet is dismissed.
                // The Flow should automatically update the list, but a manual refresh can be added if needed.
                // viewModel.refreshNotesList()
            } content: {
                // Pass the viewModel and the note to edit (or nil for new)
                NoteDetailView(
                    viewModel: viewModel,
                    noteToEdit: self.noteToEdit
                )
            }
            // .onAppear {
            //    viewModel.observeNotes() // Initial observation setup, now in init
            // }
        }
    }
}

struct NoteRow: View {
    var note: Note // Kotlin's Note data class

    var body: some View {
        HStack { // Use HStack to prevent entire row from being a button if NavigationLink is used
            VStack(alignment: .leading) {
                Text(note.title.isEmpty ? "(No Title)" : note.title)
                    .font(.headline)
                Text(note.content.isEmpty ? "(No Content)" : note.content)
                    .font(.subheadline)
                    .lineLimit(2)
                    .foregroundColor(.gray)
            }
            Spacer() // Pushes content to the left
        }
        .padding(.vertical, 4)
    }
}
