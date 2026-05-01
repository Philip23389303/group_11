package com.example.gym_planner_app.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gym_planner_app.R;
import com.example.gym_planner_app.database.DatabaseHelper;

public class JournalActivity extends AppCompatActivity {

    GridLayout notesGrid;
    TextView tvNoNotes;
    Button btnNewNote, btnBackHome;

    DatabaseHelper db;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journal);

        notesGrid = findViewById(R.id.notesGrid);
        tvNoNotes = findViewById(R.id.tvNoNotes);
        btnNewNote = findViewById(R.id.btnNewNote);
        btnBackHome = findViewById(R.id.btnBackHome);

        db = new DatabaseHelper(this);
        userId = getIntent().getIntExtra("userId", -1);

        loadNotes();

        btnNewNote.setOnClickListener(v -> {
            Intent intent = new Intent(JournalActivity.this, NoteEditorActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        btnBackHome.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        notesGrid.removeAllViews();

        Cursor cursor = db.getNotesByUser(userId);

        if (cursor == null || cursor.getCount() == 0) {
            tvNoNotes.setVisibility(View.VISIBLE);
            return;
        }

        tvNoNotes.setVisibility(View.GONE);

        while (cursor.moveToNext()) {
            int noteId = cursor.getInt(cursor.getColumnIndexOrThrow("noteId"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));

            TextView noteCard = new TextView(this);
            noteCard.setText(title + "\n\nTap to view");
            noteCard.setTextColor(getResources().getColor(R.color.textPrimary));
            noteCard.setBackgroundResource(R.drawable.note_card);
            noteCard.setPadding(40, 40, 40, 40);
            noteCard.setTextSize(18f);
            noteCard.setTypeface(null, android.graphics.Typeface.BOLD);
            noteCard.setMinHeight(180);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = 0;
            params.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            params.setMargins(16, 16, 16, 16);
            noteCard.setLayoutParams(params);

            noteCard.setOnClickListener(v -> {
                Intent intent = new Intent(JournalActivity.this, NoteEditorActivity.class);
                intent.putExtra("noteId", noteId);
                intent.putExtra("userId", userId);
                startActivity(intent);
            });

            notesGrid.addView(noteCard);
        }

        cursor.close();
    }
}