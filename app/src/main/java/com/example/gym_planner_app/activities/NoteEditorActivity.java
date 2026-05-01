package com.example.gym_planner_app.activities;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gym_planner_app.R;
import com.example.gym_planner_app.database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NoteEditorActivity extends AppCompatActivity {

    EditText etNoteTitle, etNoteContent;
    Button btnSaveNote, btnDeleteNote, btnBackJournal;

    DatabaseHelper db;
    int userId;
    int noteId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_editor);

        etNoteTitle = findViewById(R.id.etNoteTitle);
        etNoteContent = findViewById(R.id.etNoteContent);
        btnSaveNote = findViewById(R.id.btnSaveNote);
        btnDeleteNote = findViewById(R.id.btnDeleteNote);
        btnBackJournal = findViewById(R.id.btnBackJournal);

        db = new DatabaseHelper(this);

        userId = getIntent().getIntExtra("userId", -1);
        noteId = getIntent().getIntExtra("noteId", -1);

        if (noteId != -1) {
            loadNote();
        }

        btnSaveNote.setOnClickListener(v -> saveNote());
        btnDeleteNote.setOnClickListener(v -> deleteNote());
        btnBackJournal.setOnClickListener(v -> finish());
    }

    private void loadNote() {
        var cursor = db.getNoteById(noteId);

        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));

            etNoteTitle.setText(title);
            etNoteContent.setText(content);

            cursor.close();
        }
    }

    private void saveNote() {
        String title = etNoteTitle.getText().toString().trim();
        String content = etNoteContent.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            Toast.makeText(this, "Enter a title", Toast.LENGTH_SHORT).show();
            return;
        }

        if (noteId == -1) {
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
            db.insertNote(userId, title, content, date);
            Toast.makeText(this, "Note saved", Toast.LENGTH_SHORT).show();
        } else {
            db.updateNote(noteId, title, content);
            Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    private void deleteNote() {
        if (noteId != -1) {
            db.deleteNoteById(noteId);
            Toast.makeText(this, "Note deleted", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}