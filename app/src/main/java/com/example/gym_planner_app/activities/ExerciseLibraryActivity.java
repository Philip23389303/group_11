package com.example.gym_planner_app.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gym_planner_app.R;
import com.example.gym_planner_app.database.DatabaseHelper;

public class ExerciseLibraryActivity extends AppCompatActivity {

    EditText etExerciseName;
    Spinner spinnerMuscleGroup;
    Button btnAddExercise, btnDeleteExercise, btnBackHome;
    TextView tvExerciseList;
    DatabaseHelper databaseHelper;
    int userId;

    String[] muscleGroups = {"Muscle Group", "Chest", "Back", "Legs", "Shoulders", "Arms", "Core"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise_library);

        userId = getIntent().getIntExtra("userId", -1);

        etExerciseName = findViewById(R.id.etExerciseName);
        spinnerMuscleGroup = findViewById(R.id.spinnerMuscleGroup);
        btnAddExercise = findViewById(R.id.btnAddExercise);
        btnDeleteExercise = findViewById(R.id.btnDeleteExercise);
        btnBackHome = findViewById(R.id.btnBackHome);
        tvExerciseList = findViewById(R.id.tvExerciseList);

        databaseHelper = new DatabaseHelper(this);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                muscleGroups
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);

                if (position == 0) {
                    text.setTextColor(getResources().getColor(R.color.hintText));
                } else {
                    text.setTextColor(getResources().getColor(R.color.textPrimary));
                }

                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);

                if (position == 0) {
                    text.setTextColor(getResources().getColor(R.color.hintText));
                } else {
                    text.setTextColor(getResources().getColor(R.color.textPrimary));
                }

                text.setBackgroundColor(getResources().getColor(R.color.card));
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMuscleGroup.setAdapter(adapter);
        spinnerMuscleGroup.setSelection(0);

        displayExercises();

        btnAddExercise.setOnClickListener(v -> {
            String exerciseName = etExerciseName.getText().toString().trim();

            if (TextUtils.isEmpty(exerciseName)) {
                Toast.makeText(this, "Please enter an exercise name", Toast.LENGTH_SHORT).show();
                return;
            }

            if (spinnerMuscleGroup.getSelectedItemPosition() == 0) {
                Toast.makeText(this, "Please select a muscle group", Toast.LENGTH_SHORT).show();
                return;
            }

            if (userId == -1) {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                return;
            }

            String muscleGroup = spinnerMuscleGroup.getSelectedItem().toString();

            boolean inserted = databaseHelper.insertExercise(userId, exerciseName, muscleGroup);

            if (inserted) {
                Toast.makeText(this, "Exercise added", Toast.LENGTH_SHORT).show();
                etExerciseName.setText("");
                spinnerMuscleGroup.setSelection(0);
                displayExercises();
            } else {
                Toast.makeText(this, "Failed to add exercise", Toast.LENGTH_SHORT).show();
            }
        });

        btnDeleteExercise.setOnClickListener(v -> {
            String exerciseName = etExerciseName.getText().toString().trim();

            if (TextUtils.isEmpty(exerciseName)) {
                Toast.makeText(this, "Enter exercise name to delete", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean deleted = databaseHelper.deleteExercise(userId, exerciseName);

            if (deleted) {
                Toast.makeText(this, "Exercise deleted", Toast.LENGTH_SHORT).show();
                etExerciseName.setText("");
                displayExercises();
            } else {
                Toast.makeText(this, "Exercise not found", Toast.LENGTH_SHORT).show();
            }
        });

        btnBackHome.setOnClickListener(v -> finish());
    }

    private void displayExercises() {
        Cursor cursor = databaseHelper.getExercisesByUser(userId);

        if (cursor == null || cursor.getCount() == 0) {
            tvExerciseList.setText("No exercises added yet");

            if (cursor != null) {
                cursor.close();
            }

            return;
        }

        StringBuilder builder = new StringBuilder();
        String currentGroup = "";

        while (cursor.moveToNext()) {
            String muscleGroup = cursor.getString(cursor.getColumnIndexOrThrow("muscleGroup"));
            String exerciseName = cursor.getString(cursor.getColumnIndexOrThrow("exerciseName"));

            if (!muscleGroup.equals(currentGroup)) {
                currentGroup = muscleGroup;
                builder.append(currentGroup).append("\n");
            }

            builder.append("- ").append(exerciseName).append("\n");
        }

        tvExerciseList.setText(builder.toString());
        cursor.close();
    }
}