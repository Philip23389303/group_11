package com.example.gym_planner_app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gym_planner_app.R;

public class HomeActivity extends AppCompatActivity {

    Button btnWorkoutPlanner, btnExerciseLibrary, btnLogWorkout, btnProgress, btnReminder, btnJournal, btnPersonalBests, btnBmiCalculator, btnRestTimer, btnLogout;    int userId;
    TextView btnProfile;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        userId = getIntent().getIntExtra("userId", -1);

        btnWorkoutPlanner = findViewById(R.id.btnWorkoutPlanner);
        btnExerciseLibrary = findViewById(R.id.btnExerciseLibrary);
        btnLogWorkout = findViewById(R.id.btnLogWorkout);
        btnProgress = findViewById(R.id.btnProgress);
        btnReminder = findViewById(R.id.btnReminder);
        btnJournal = findViewById(R.id.btnJournal);
        btnPersonalBests = findViewById(R.id.btnPersonalBests);
        btnBmiCalculator = findViewById(R.id.btnBmiCalculator);
        btnRestTimer = findViewById(R.id.btnRestTimer);
        btnLogout = findViewById(R.id.btnLogout);


        btnProfile = findViewById(R.id.btnProfile);

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        btnWorkoutPlanner.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, WorkoutPlannerActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        btnExerciseLibrary.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ExerciseLibraryActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        btnLogWorkout.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LogWorkoutActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        btnProgress.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ProgressActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        btnReminder.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ReminderActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });
        btnJournal.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, JournalActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        btnPersonalBests.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, PersonalBestsActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        btnBmiCalculator.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, BmiCalculatorActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        btnRestTimer.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, RestTimerActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}