package com.example.gym_planner_app.activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gym_planner_app.R;
import com.example.gym_planner_app.database.DatabaseHelper;

public class PersonalBestsActivity extends AppCompatActivity {

    TextView tvNoPersonalBests;
    LinearLayout personalBestsContainer;
    Button btnBackHome;

    DatabaseHelper databaseHelper;
    int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_bests);

        tvNoPersonalBests = findViewById(R.id.tvNoPersonalBests);
        personalBestsContainer = findViewById(R.id.personalBestsContainer);
        btnBackHome = findViewById(R.id.btnBackHome);

        databaseHelper = new DatabaseHelper(this);
        userId = getIntent().getIntExtra("userId", -1);

        if (userId == -1) {
            Toast.makeText(this, "User not found. Please log in again.", Toast.LENGTH_SHORT).show();
        }

        loadPersonalBests();

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(PersonalBestsActivity.this, HomeActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });
    }

    private void loadPersonalBests() {
        personalBestsContainer.removeAllViews();

        Cursor cursor = databaseHelper.getPersonalBestsByUser(userId);

        if (cursor == null || cursor.getCount() == 0) {
            tvNoPersonalBests.setVisibility(View.VISIBLE);

            if (cursor != null) {
                cursor.close();
            }

            return;
        }

        tvNoPersonalBests.setVisibility(View.GONE);

        while (cursor.moveToNext()) {
            String exerciseName = cursor.getString(cursor.getColumnIndexOrThrow("exerciseName"));
            int reps = cursor.getInt(cursor.getColumnIndexOrThrow("reps"));
            double bestWeight = cursor.getDouble(cursor.getColumnIndexOrThrow("bestWeight"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));

            addPersonalBestCard(exerciseName, reps, bestWeight, date);
        }

        cursor.close();
    }

    private void addPersonalBestCard(String exerciseName, int reps, double bestWeight, String date) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(28, 24, 28, 24);
        card.setBackgroundResource(R.drawable.card_background);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 18);
        card.setLayoutParams(cardParams);

        TextView tvExercise = new TextView(this);
        tvExercise.setText(exerciseName);
        tvExercise.setTextColor(getResources().getColor(R.color.textPrimary));
        tvExercise.setTextSize(22);
        tvExercise.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        tvExercise.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvWeight = new TextView(this);
        tvWeight.setText("Best Weight: " + formatWeight(bestWeight) + " kg");
        tvWeight.setTextColor(getResources().getColor(R.color.textPrimary));
        tvWeight.setTextSize(16);
        tvWeight.setPadding(0, 12, 0, 0);

        TextView tvReps = new TextView(this);
        tvReps.setText("Reps: " + reps);
        tvReps.setTextColor(getResources().getColor(R.color.textSecondary));
        tvReps.setTextSize(15);
        tvReps.setPadding(0, 6, 0, 0);

        TextView tvDate = new TextView(this);
        tvDate.setText("Date: " + date);
        tvDate.setTextColor(getResources().getColor(R.color.textSecondary));
        tvDate.setTextSize(15);
        tvDate.setPadding(0, 6, 0, 0);

        card.addView(tvExercise);
        card.addView(tvWeight);
        card.addView(tvReps);
        card.addView(tvDate);

        personalBestsContainer.addView(card);
    }

    private String formatWeight(double weight) {
        if (weight == (int) weight) {
            return String.valueOf((int) weight);
        }

        return String.valueOf(weight);
    }
}