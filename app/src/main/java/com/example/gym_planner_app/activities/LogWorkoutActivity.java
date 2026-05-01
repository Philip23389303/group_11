package com.example.gym_planner_app.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gym_planner_app.R;
import com.example.gym_planner_app.database.DatabaseHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class LogWorkoutActivity extends AppCompatActivity {

    CalendarView calendarViewWorkout;
    TextView tvSelectedDate;
    EditText etExerciseName, etNotes;
    LinearLayout setsContainer, logsContainer;
    Button btnAddSet, btnRemoveSet, btnSaveWorkout, btnReturnHome, btnDeleteSelected;

    ArrayList<String> exerciseNames = new ArrayList<>();

    DatabaseHelper db;
    int loggedInUserId = -1;
    String selectedDate;

    long currentLoadedLogId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_workout);

        calendarViewWorkout = findViewById(R.id.calendarViewWorkout);
        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        etExerciseName = findViewById(R.id.etExerciseName);
        etNotes = findViewById(R.id.etNotes);
        setsContainer = findViewById(R.id.setsContainer);
        logsContainer = findViewById(R.id.logsContainer);

        btnAddSet = findViewById(R.id.btnAddSet);
        btnRemoveSet = findViewById(R.id.btnRemoveSet);
        btnSaveWorkout = findViewById(R.id.btnSaveWorkout);
        btnReturnHome = findViewById(R.id.btnReturnHome);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);

        db = new DatabaseHelper(this);
        loggedInUserId = getIntent().getIntExtra("userId", -1);

        Calendar calendar = Calendar.getInstance();
        selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(calendar.getTime());

        tvSelectedDate.setText("Selected Date: " + selectedDate);

        loadExerciseNames();
        loadSavedWorkoutForDate();

        calendarViewWorkout.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format(Locale.getDefault(),
                    "%d-%02d-%02d", year, month + 1, dayOfMonth);

            tvSelectedDate.setText("Selected Date: " + selectedDate);
            loadSavedWorkoutForDate();
        });

        btnAddSet.setOnClickListener(v -> addSetRow());
        btnRemoveSet.setOnClickListener(v -> removeLastSetRow());
        btnSaveWorkout.setOnClickListener(v -> saveExerciseLog());

        btnDeleteSelected.setOnClickListener(v -> deleteSavedWorkoutForDate());

        btnReturnHome.setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("userId", loggedInUserId);
            startActivity(intent);
            finish();
        });
    }

    private void addSetRow() {
        View setView = LayoutInflater.from(this)
                .inflate(R.layout.item_set_input, setsContainer, false);

        TextView tvSetLabel = setView.findViewById(R.id.tvSetLabel);
        tvSetLabel.setText("Set " + (setsContainer.getChildCount() + 1));

        Spinner spinner = setView.findViewById(R.id.spinnerSetExercise);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                exerciseNames
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(R.color.textPrimary));
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(R.color.textPrimary));
                text.setBackgroundColor(getResources().getColor(R.color.card));
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        setsContainer.addView(setView);
    }

    private void removeLastSetRow() {
        if (setsContainer.getChildCount() > 1) {
            setsContainer.removeViewAt(setsContainer.getChildCount() - 1);
            updateSetLabels();
        }
    }

    private void updateSetLabels() {
        for (int i = 0; i < setsContainer.getChildCount(); i++) {
            View setView = setsContainer.getChildAt(i);
            TextView tvSetLabel = setView.findViewById(R.id.tvSetLabel);
            tvSetLabel.setText("Set " + (i + 1));
        }
    }

    private void saveExerciseLog() {
        String name = etExerciseName.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        if (loggedInUserId == -1) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a workout split", Toast.LENGTH_SHORT).show();
            return;
        }

        if (setsContainer.getChildCount() == 0) {
            Toast.makeText(this, "Please add at least one set", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < setsContainer.getChildCount(); i++) {
            View v = setsContainer.getChildAt(i);

            Spinner exerciseSpinner = v.findViewById(R.id.spinnerSetExercise);
            EditText repsEt = v.findViewById(R.id.etSetReps);
            EditText weightEt = v.findViewById(R.id.etSetWeight);

            String repsText = repsEt.getText().toString().trim();
            String weightText = weightEt.getText().toString().trim();

            if (exerciseSpinner.getSelectedItem() == null ||
                    exerciseSpinner.getSelectedItem().toString().equals("No exercises available")) {
                Toast.makeText(this, "Please choose an exercise for every set", Toast.LENGTH_SHORT).show();
                return;
            }

            if (repsText.isEmpty() || weightText.isEmpty()) {
                Toast.makeText(this, "Please enter reps and weight for every set", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        db.deleteExerciseLogsByDate(loggedInUserId, selectedDate);

        long logId = db.insertExerciseLog(loggedInUserId, name, notes, selectedDate);

        if (logId == -1) {
            Toast.makeText(this, "Failed to save workout", Toast.LENGTH_SHORT).show();
            return;
        }

        for (int i = 0; i < setsContainer.getChildCount(); i++) {
            View v = setsContainer.getChildAt(i);

            Spinner exerciseSpinner = v.findViewById(R.id.spinnerSetExercise);
            EditText repsEt = v.findViewById(R.id.etSetReps);
            EditText weightEt = v.findViewById(R.id.etSetWeight);

            String setExerciseName = exerciseSpinner.getSelectedItem().toString();
            int reps = Integer.parseInt(repsEt.getText().toString().trim());
            double weight = Double.parseDouble(weightEt.getText().toString().trim());

            db.insertExerciseSet(logId, i + 1, setExerciseName, reps, weight);
        }

        currentLoadedLogId = logId;

        Toast.makeText(this, "Workout saved", Toast.LENGTH_SHORT).show();

        // Important: this does NOT clear the boxes anymore.
    }

    private void loadSavedWorkoutForDate() {
        setsContainer.removeAllViews();
        logsContainer.removeAllViews();
        currentLoadedLogId = -1;

        Cursor cursor = db.getExerciseLogsByDate(loggedInUserId, selectedDate);

        if (cursor == null || cursor.getCount() == 0) {
            etExerciseName.setText("");
            etNotes.setText("");
            addSetRow();

            if (cursor != null) {
                cursor.close();
            }

            return;
        }

        if (cursor.moveToFirst()) {
            currentLoadedLogId = cursor.getLong(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_ID)
            );

            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_EXERCISE_NAME)
            );

            String notes = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NOTES)
            );

            etExerciseName.setText(name);
            etNotes.setText(notes);
        }

        cursor.close();

        Cursor setsCursor = db.getExerciseSetsByLogId(currentLoadedLogId);

        if (setsCursor == null || setsCursor.getCount() == 0) {
            addSetRow();

            if (setsCursor != null) {
                setsCursor.close();
            }

            return;
        }

        while (setsCursor.moveToNext()) {
            addSetRow();

            View setView = setsContainer.getChildAt(setsContainer.getChildCount() - 1);

            Spinner exerciseSpinner = setView.findViewById(R.id.spinnerSetExercise);
            EditText repsEt = setView.findViewById(R.id.etSetReps);
            EditText weightEt = setView.findViewById(R.id.etSetWeight);

            String setExerciseName = setsCursor.getString(
                    setsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SET_EXERCISE_NAME)
            );

            int reps = setsCursor.getInt(
                    setsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REPS)
            );

            double weight = setsCursor.getDouble(
                    setsCursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_WEIGHT)
            );

            setSpinnerToValue(exerciseSpinner, setExerciseName);
            repsEt.setText(String.valueOf(reps));
            weightEt.setText(formatWeight(weight));
        }

        setsCursor.close();
    }

    private void setSpinnerToValue(Spinner spinner, String value) {
        if (value == null) {
            return;
        }

        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equals(value)) {
                spinner.setSelection(i);
                return;
            }
        }
    }

    private String formatWeight(double weight) {
        if (weight == (int) weight) {
            return String.valueOf((int) weight);
        }

        return String.valueOf(weight);
    }

    private void deleteSavedWorkoutForDate() {
        if (currentLoadedLogId == -1) {
            Toast.makeText(this, "No saved workout for this date", Toast.LENGTH_SHORT).show();
            return;
        }

        db.deleteExerciseLog(currentLoadedLogId);

        Toast.makeText(this, "Workout deleted", Toast.LENGTH_SHORT).show();

        etExerciseName.setText("");
        etNotes.setText("");
        setsContainer.removeAllViews();
        logsContainer.removeAllViews();
        addSetRow();

        currentLoadedLogId = -1;
    }

    private void loadExerciseNames() {
        exerciseNames.clear();

        Cursor cursor = db.getExerciseNamesByUser(loggedInUserId);

        if (cursor == null || cursor.getCount() == 0) {
            exerciseNames.add("No exercises available");

            if (cursor != null) {
                cursor.close();
            }

            return;
        }

        while (cursor.moveToNext()) {
            String name = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_EXERCISE_NAME)
            );

            if (name != null && !name.trim().isEmpty()) {
                exerciseNames.add(name.trim());
            }
        }

        cursor.close();

        if (exerciseNames.isEmpty()) {
            exerciseNames.add("No exercises available");
        }
    }
}