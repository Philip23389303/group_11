package com.example.gym_planner_app.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.gym_planner_app.R;
import com.example.gym_planner_app.database.DatabaseHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class WorkoutPlannerActivity extends AppCompatActivity {

    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 1001;

    Spinner spinnerWorkoutName;
    Spinner spinnerDay, spinnerTime;
    CheckBox cbReminder;
    Button btnSaveWorkout, btnDeleteSelected, btnBackHome;
    ListView lvWorkouts;

    DatabaseHelper databaseHelper;
    int userId;

    ArrayList<String> workoutNames;
    ArrayList<String> workoutDisplayList;
    ArrayList<Integer> workoutIdList;

    ArrayAdapter<String> workoutNameAdapter;
    ArrayAdapter<String> adapter;

    private static final String CHOOSE_WORKOUT_TEXT = "Choose Workout Split";
    private static final String NO_LOGGED_WORKOUTS_TEXT = "No logged workouts available";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_workout_planner);

        spinnerWorkoutName = findViewById(R.id.spinnerWorkoutName);
        spinnerDay = findViewById(R.id.spinnerDay);
        spinnerTime = findViewById(R.id.spinnerTime);
        cbReminder = findViewById(R.id.cbReminder);
        btnSaveWorkout = findViewById(R.id.btnSaveWorkout);
        btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        btnBackHome = findViewById(R.id.btnBackHome);
        lvWorkouts = findViewById(R.id.lvWorkouts);

        databaseHelper = new DatabaseHelper(this);
        userId = getIntent().getIntExtra("userId", -1);

        workoutNames = new ArrayList<>();
        workoutDisplayList = new ArrayList<>();
        workoutIdList = new ArrayList<>();

        requestNotificationPermissionIfNeeded();
        ReminderReceiver.createNotificationChannel(this);

        setupWorkoutNameSpinner();
        setupDaySpinner();
        setupTimeSpinner();
        setupWorkoutList();

        if (userId == -1) {
            Toast.makeText(this, "Error: user not logged in", Toast.LENGTH_SHORT).show();
        }

        loadWorkoutNames();
        displayWorkouts();

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(WorkoutPlannerActivity.this, HomeActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            finish();
        });

        btnSaveWorkout.setOnClickListener(v -> {

            if (userId == -1) {
                Toast.makeText(WorkoutPlannerActivity.this, "User not found. Please log in again.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (spinnerWorkoutName.getSelectedItem() == null) {
                Toast.makeText(WorkoutPlannerActivity.this, "Log a workout first before planning one", Toast.LENGTH_SHORT).show();
                return;
            }

            if (spinnerWorkoutName.getSelectedItemPosition() == 0) {
                Toast.makeText(WorkoutPlannerActivity.this, "Please choose a workout split", Toast.LENGTH_SHORT).show();
                return;
            }

            String day = spinnerDay.getSelectedItem().toString();
            String time = spinnerTime.getSelectedItem().toString();
            String workoutName = spinnerWorkoutName.getSelectedItem().toString();
            boolean reminderEnabled = cbReminder.isChecked();

            if (workoutName.equals(NO_LOGGED_WORKOUTS_TEXT)) {
                Toast.makeText(WorkoutPlannerActivity.this, "Log a workout first before planning one", Toast.LENGTH_SHORT).show();
                return;
            }

            long insertedId = databaseHelper.insertWorkout(userId, day, time, workoutName, reminderEnabled);

            if (insertedId != -1) {
                if (reminderEnabled) {
                    ReminderScheduler.scheduleReminder(
                            WorkoutPlannerActivity.this,
                            (int) insertedId,
                            userId,
                            workoutName,
                            day,
                            time
                    );
                }

                Toast.makeText(WorkoutPlannerActivity.this, "Workout saved", Toast.LENGTH_SHORT).show();

                spinnerWorkoutName.setSelection(0);
                spinnerDay.setSelection(0);
                spinnerTime.setSelection(0);
                cbReminder.setChecked(false);

                displayWorkouts();
            } else {
                Toast.makeText(WorkoutPlannerActivity.this, "Failed to save workout", Toast.LENGTH_SHORT).show();
            }
        });

        btnDeleteSelected.setOnClickListener(v -> {

            if (userId == -1) {
                Toast.makeText(WorkoutPlannerActivity.this, "User not found. Please log in again.", Toast.LENGTH_SHORT).show();
                return;
            }

            SparseBooleanArray checkedItems = lvWorkouts.getCheckedItemPositions();
            boolean anyDeleted = false;

            for (int i = workoutIdList.size() - 1; i >= 0; i--) {
                if (checkedItems.get(i)) {
                    int workoutId = workoutIdList.get(i);

                    ReminderScheduler.cancelReminder(WorkoutPlannerActivity.this, workoutId);
                    boolean deleted = databaseHelper.deleteWorkoutById(workoutId);

                    if (deleted) {
                        anyDeleted = true;
                    }
                }
            }

            if (anyDeleted) {
                Toast.makeText(WorkoutPlannerActivity.this, "Selected workouts deleted", Toast.LENGTH_SHORT).show();
                lvWorkouts.clearChoices();
                displayWorkouts();
            } else {
                Toast.makeText(WorkoutPlannerActivity.this, "No workouts selected", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWorkoutNames();
    }

    private void setupWorkoutNameSpinner() {
        workoutNameAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                workoutNames
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);

                if (position == 0) {
                    text.setTextColor(getResources().getColor(R.color.textSecondary));
                } else {
                    text.setTextColor(getResources().getColor(R.color.textPrimary));
                }

                text.setTextSize(15);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);

                text.setBackgroundColor(getResources().getColor(R.color.card));

                if (position == 0) {
                    text.setTextColor(getResources().getColor(R.color.textSecondary));
                } else {
                    text.setTextColor(getResources().getColor(R.color.textPrimary));
                }

                text.setTextSize(15);
                text.setPadding(24, 18, 24, 18);

                return view;
            }
        };

        workoutNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerWorkoutName.setAdapter(workoutNameAdapter);
        spinnerWorkoutName.setSelection(0);
    }

    private void loadWorkoutNames() {
        workoutNames.clear();

        workoutNames.add(CHOOSE_WORKOUT_TEXT);

        if (userId == -1) {
            workoutNames.add(NO_LOGGED_WORKOUTS_TEXT);
            workoutNameAdapter.notifyDataSetChanged();
            return;
        }

        Cursor cursor = databaseHelper.getLoggedWorkoutNamesByUser(userId);

        if (cursor == null || cursor.getCount() == 0) {
            workoutNames.add(NO_LOGGED_WORKOUTS_TEXT);
        } else {
            while (cursor.moveToNext()) {
                String workoutName = cursor.getString(
                        cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOG_EXERCISE_NAME)
                );
                workoutNames.add(workoutName);
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        workoutNameAdapter.notifyDataSetChanged();
        spinnerWorkoutName.setSelection(0);
    }

    private void setupDaySpinner() {
        String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        ArrayAdapter<String> dayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                days
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(R.color.textPrimary));
                text.setTextSize(15);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(R.color.textPrimary));
                text.setBackgroundColor(getResources().getColor(R.color.card));
                text.setTextSize(15);
                text.setPadding(24, 18, 24, 18);
                return view;
            }
        };

        dayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDay.setAdapter(dayAdapter);
    }

    private void setupTimeSpinner() {
        ArrayList<String> timeList = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 30);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.set(Calendar.HOUR_OF_DAY, 23);
        endCalendar.set(Calendar.MINUTE, 0);

        while (!calendar.after(endCalendar)) {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            String amPm = (hour < 12) ? "AM" : "PM";
            int displayHour = hour % 12;

            if (displayHour == 0) {
                displayHour = 12;
            }

            String time = String.format(Locale.getDefault(), "%d:%02d %s", displayHour, minute, amPm);
            timeList.add(time);

            calendar.add(Calendar.MINUTE, 15);
        }

        ArrayAdapter<String> timeAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                timeList
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(R.color.textPrimary));
                text.setTextSize(15);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(R.color.textPrimary));
                text.setBackgroundColor(getResources().getColor(R.color.card));
                text.setTextSize(15);
                text.setPadding(24, 18, 24, 18);
                return view;
            }
        };

        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTime.setAdapter(timeAdapter);
    }

    private void setupWorkoutList() {
        adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_multiple_choice,
                workoutDisplayList
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = view.findViewById(android.R.id.text1);
                text.setTextColor(getResources().getColor(R.color.textPrimary));
                view.setBackgroundColor(getResources().getColor(R.color.card));
                return view;
            }
        };

        lvWorkouts.setAdapter(adapter);
        lvWorkouts.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    }

    private void displayWorkouts() {
        workoutDisplayList.clear();
        workoutIdList.clear();

        if (userId == -1) {
            adapter.notifyDataSetChanged();
            return;
        }

        Cursor cursor = databaseHelper.getWorkoutsByUser(userId);

        if (cursor == null || cursor.getCount() == 0) {
            adapter.notifyDataSetChanged();

            if (cursor != null) {
                cursor.close();
            }

            return;
        }

        while (cursor.moveToNext()) {
            int workoutId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String day = cursor.getString(cursor.getColumnIndexOrThrow("day"));
            String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
            String workoutName = cursor.getString(cursor.getColumnIndexOrThrow("workoutName"));
            int reminderEnabled = cursor.getInt(cursor.getColumnIndexOrThrow("reminderEnabled"));

            String reminderText = reminderEnabled == 1 ? "Reminder: ON" : "Reminder: OFF";

            workoutIdList.add(workoutId);
            workoutDisplayList.add(
                    "Day: " + day +
                            " | Time: " + time +
                            " | Workout: " + workoutName +
                            " | " + reminderText
            );
        }

        cursor.close();

        lvWorkouts.clearChoices();
        adapter.notifyDataSetChanged();
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                        this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE
                );
            }
        }
    }
}