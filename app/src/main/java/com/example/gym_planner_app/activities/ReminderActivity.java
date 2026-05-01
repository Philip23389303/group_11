package com.example.gym_planner_app.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gym_planner_app.R;
import com.example.gym_planner_app.database.DatabaseHelper;

public class ReminderActivity extends AppCompatActivity {

    LinearLayout reminderContainer, completedContainer;
    Button btnBackHome;

    DatabaseHelper db;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder);

        reminderContainer = findViewById(R.id.reminderContainer);
        completedContainer = findViewById(R.id.completedContainer);
        btnBackHome = findViewById(R.id.btnBackHomeReminder);

        db = new DatabaseHelper(this);
        userId = getIntent().getIntExtra("userId", -1);

        loadReminders();
        loadCompletedWorkouts();

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(ReminderActivity.this, HomeActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });
    }

    private void loadReminders() {
        reminderContainer.removeAllViews();

        if (userId == -1) {
            addText(reminderContainer, "User not logged in.");
            return;
        }

        Cursor cursor = db.getReminderWorkoutsByUser(userId);

        if (cursor == null || cursor.getCount() == 0) {
            addText(reminderContainer, "No active reminders.");
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        while (cursor.moveToNext()) {
            String day = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DAY));
            String time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TIME));
            String workoutName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WORKOUT_NAME));

            addText(reminderContainer, day + " | " + time + " | " + workoutName);
        }

        cursor.close();
    }

    private void loadCompletedWorkouts() {
        completedContainer.removeAllViews();

        if (userId == -1) {
            addText(completedContainer, "User not logged in.");
            return;
        }

        Cursor cursor = db.getAllExerciseLogs(userId);

        if (cursor == null || cursor.getCount() == 0) {
            addText(completedContainer, "No completed workouts yet.");
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        while (cursor.moveToNext()) {
            String exerciseName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_EXERCISE_NAME));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE));
            String notes = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTES));

            String text = date + " | " + exerciseName;
            if (notes != null && !notes.trim().isEmpty()) {
                text += " | Notes: " + notes;
            }

            addText(completedContainer, text);
        }

        cursor.close();
    }

    private void addText(LinearLayout container, String text) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextSize(16f);
        tv.setPadding(0, 12, 0, 12);
        container.addView(tv);
    }
}