package com.example.gym_planner_app.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gym_planner_app.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class RestTimerActivity extends AppCompatActivity {

    EditText etTimerName, etMinutes, etSeconds;
    Button btnSaveTimer, btnStopTimer, btnBackHome;
    TextView tvCurrentTimer, tvTimerDisplay;
    LinearLayout savedTimersContainer;

    int userId;
    CountDownTimer countDownTimer;
    boolean timerRunning = false;

    SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "RestTimersPrefs";
    private static final String TIMERS_KEY = "savedTimers";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rest_timer);

        etTimerName = findViewById(R.id.etTimerName);
        etMinutes = findViewById(R.id.etMinutes);
        etSeconds = findViewById(R.id.etSeconds);
        btnSaveTimer = findViewById(R.id.btnSaveTimer);
        btnStopTimer = findViewById(R.id.btnStopTimer);
        btnBackHome = findViewById(R.id.btnBackHome);
        tvCurrentTimer = findViewById(R.id.tvCurrentTimer);
        tvTimerDisplay = findViewById(R.id.tvTimerDisplay);
        savedTimersContainer = findViewById(R.id.savedTimersContainer);

        userId = getIntent().getIntExtra("userId", -1);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        loadSavedTimers();

        btnSaveTimer.setOnClickListener(v -> saveTimer());
        btnStopTimer.setOnClickListener(v -> stopTimer());

        btnBackHome.setOnClickListener(v -> {
            stopTimer();

            Intent intent = new Intent(RestTimerActivity.this, HomeActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });
    }

    private void saveTimer() {
        String name = etTimerName.getText().toString().trim();
        String minutesText = etMinutes.getText().toString().trim();
        String secondsText = etSeconds.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter a timer name", Toast.LENGTH_SHORT).show();
            return;
        }

        int minutes = 0;
        int seconds = 0;

        try {
            if (!minutesText.isEmpty()) {
                minutes = Integer.parseInt(minutesText);
            }

            if (!secondsText.isEmpty()) {
                seconds = Integer.parseInt(secondsText);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
            return;
        }

        if (minutes == 0 && seconds == 0) {
            Toast.makeText(this, "Timer must be longer than 0 seconds", Toast.LENGTH_SHORT).show();
            return;
        }

        if (seconds >= 60) {
            Toast.makeText(this, "Seconds must be less than 60", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONArray timersArray = getTimersArray();

            JSONObject timerObject = new JSONObject();
            timerObject.put("name", name);
            timerObject.put("minutes", minutes);
            timerObject.put("seconds", seconds);

            timersArray.put(timerObject);

            sharedPreferences.edit()
                    .putString(TIMERS_KEY, timersArray.toString())
                    .apply();

            Toast.makeText(this, "Timer saved", Toast.LENGTH_SHORT).show();

            etTimerName.setText("");
            etMinutes.setText("");
            etSeconds.setText("");

            loadSavedTimers();

        } catch (Exception e) {
            Toast.makeText(this, "Failed to save timer", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSavedTimers() {
        savedTimersContainer.removeAllViews();

        JSONArray timersArray = getTimersArray();

        if (timersArray.length() == 0) {
            TextView emptyText = new TextView(this);
            emptyText.setText("No saved timers yet.");
            emptyText.setTextColor(getResources().getColor(R.color.textSecondary));
            emptyText.setTextSize(16);
            savedTimersContainer.addView(emptyText);
            return;
        }

        for (int i = 0; i < timersArray.length(); i++) {
            try {
                JSONObject timerObject = timersArray.getJSONObject(i);

                String name = timerObject.getString("name");
                int minutes = timerObject.getInt("minutes");
                int seconds = timerObject.getInt("seconds");

                addTimerCard(i, name, minutes, seconds);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void addTimerCard(int index, String name, int minutes, int seconds) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setBackgroundResource(R.drawable.card_background);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, dp(12));
        card.setLayoutParams(cardParams);

        TextView tvName = new TextView(this);
        tvName.setText(name);
        tvName.setTextColor(getResources().getColor(R.color.textPrimary));
        tvName.setTextSize(22);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvDuration = new TextView(this);
        tvDuration.setText("Duration: " + formatTime(minutes, seconds));
        tvDuration.setTextColor(getResources().getColor(R.color.textSecondary));
        tvDuration.setTextSize(16);
        tvDuration.setPadding(0, dp(6), 0, 0);

        Button btnStart = new Button(this);
        btnStart.setText("Start Timer");
        btnStart.setTextColor(getResources().getColor(R.color.textPrimary));
        btnStart.setBackgroundResource(R.drawable.button_primary);

        LinearLayout.LayoutParams startParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
        );
        startParams.setMargins(0, dp(12), 0, 0);
        btnStart.setLayoutParams(startParams);

        Button btnDelete = new Button(this);
        btnDelete.setText("Delete Timer");
        btnDelete.setTextColor(getResources().getColor(R.color.textPrimary));
        btnDelete.setBackgroundResource(R.drawable.button_secondary);

        LinearLayout.LayoutParams deleteParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(56)
        );
        deleteParams.setMargins(0, dp(10), 0, 0);
        btnDelete.setLayoutParams(deleteParams);

        btnStart.setOnClickListener(v -> startTimer(name, minutes, seconds));
        btnDelete.setOnClickListener(v -> deleteTimer(index));

        card.addView(tvName);
        card.addView(tvDuration);
        card.addView(btnStart);
        card.addView(btnDelete);

        savedTimersContainer.addView(card);
    }

    private void startTimer(String name, int minutes, int seconds) {
        stopTimer();

        long totalMillis = ((minutes * 60L) + seconds) * 1000L;

        tvCurrentTimer.setText("Current Timer: " + name);
        tvTimerDisplay.setText(formatTime(minutes, seconds));

        timerRunning = true;

        countDownTimer = new CountDownTimer(totalMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long totalSeconds = millisUntilFinished / 1000;
                int mins = (int) (totalSeconds / 60);
                int secs = (int) (totalSeconds % 60);

                tvTimerDisplay.setText(formatTime(mins, secs));
            }

            @Override
            public void onFinish() {
                timerRunning = false;
                tvTimerDisplay.setText("00:00");
                tvCurrentTimer.setText("Timer finished!");
                Toast.makeText(RestTimerActivity.this, "Rest timer finished", Toast.LENGTH_LONG).show();
            }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        timerRunning = false;
        tvCurrentTimer.setText("Current Timer: None");
        tvTimerDisplay.setText("00:00");
    }

    private void deleteTimer(int index) {
        try {
            JSONArray oldArray = getTimersArray();
            JSONArray newArray = new JSONArray();

            for (int i = 0; i < oldArray.length(); i++) {
                if (i != index) {
                    newArray.put(oldArray.getJSONObject(i));
                }
            }

            sharedPreferences.edit()
                    .putString(TIMERS_KEY, newArray.toString())
                    .apply();

            Toast.makeText(this, "Timer deleted", Toast.LENGTH_SHORT).show();
            loadSavedTimers();

        } catch (Exception e) {
            Toast.makeText(this, "Failed to delete timer", Toast.LENGTH_SHORT).show();
        }
    }

    private JSONArray getTimersArray() {
        String timersJson = sharedPreferences.getString(TIMERS_KEY, "[]");

        try {
            return new JSONArray(timersJson);
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }

    private String formatTime(int minutes, int seconds) {
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}