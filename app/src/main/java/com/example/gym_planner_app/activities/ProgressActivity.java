package com.example.gym_planner_app.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gym_planner_app.R;
import com.example.gym_planner_app.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.widget.Button;

public class ProgressActivity extends AppCompatActivity {

    private TextView tvTotalWorkouts;
    private TextView tvTrainingDays;
    private TextView tvMostFrequentWorkout;
    private TextView tvRecentActivity;

    private DatabaseHelper databaseHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        tvTotalWorkouts = findViewById(R.id.tvTotalWorkouts);
        tvTrainingDays = findViewById(R.id.tvTrainingDays);
        tvMostFrequentWorkout = findViewById(R.id.tvMostFrequentWorkout);
        tvRecentActivity = findViewById(R.id.tvRecentActivity);

        Button btnBackHome = findViewById(R.id.btnBackHome);

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(ProgressActivity.this, HomeActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });

        userId = getIntent().getIntExtra("userId", -1);
        databaseHelper = new DatabaseHelper(this);

        loadProgressData();
    }

    private void loadProgressData() {
        Cursor cursor = databaseHelper.getWorkoutsByUser(userId);

        if (cursor == null || cursor.getCount() == 0) {
            tvTotalWorkouts.setText("Total Workouts: 0");
            tvTrainingDays.setText("Training Days: 0");
            tvMostFrequentWorkout.setText("Most Frequent Workout: None");
            tvRecentActivity.setText("No workout data yet.");
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        int totalWorkouts = 0;
        HashSet<String> uniqueDays = new HashSet<>();
        HashMap<String, Integer> workoutCountMap = new HashMap<>();
        List<String> workoutEntries = new ArrayList<>();

        while (cursor.moveToNext()) {
            String day = cursor.getString(cursor.getColumnIndexOrThrow("day"));
            String workoutName = cursor.getString(cursor.getColumnIndexOrThrow("workoutName"));

            totalWorkouts++;
            uniqueDays.add(day);

            if (workoutCountMap.containsKey(workoutName)) {
                workoutCountMap.put(workoutName, workoutCountMap.get(workoutName) + 1);
            } else {
                workoutCountMap.put(workoutName, 1);
            }

            workoutEntries.add(day + ": " + workoutName);
        }

        cursor.close();

        String mostFrequentWorkout = "None";
        int highestCount = 0;

        for (Map.Entry<String, Integer> entry : workoutCountMap.entrySet()) {
            if (entry.getValue() > highestCount) {
                highestCount = entry.getValue();
                mostFrequentWorkout = entry.getKey();
            }
        }

        tvTotalWorkouts.setText("Total Workouts: " + totalWorkouts);
        tvTrainingDays.setText("Training Days: " + uniqueDays.size());
        tvMostFrequentWorkout.setText("Most Frequent Workout: " + mostFrequentWorkout);

        StringBuilder recentBuilder = new StringBuilder();
        recentBuilder.append("Recent Activity\n\n");

        for (int i = workoutEntries.size() - 1; i >= 0; i--) {
            recentBuilder.append("• ").append(workoutEntries.get(i)).append("\n");
        }

        tvRecentActivity.setText(recentBuilder.toString());
    }
}