package com.example.gym_planner_app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "GymPlanner.db";
    private static final int DATABASE_VERSION = 8;

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_PASSWORD = "password";

    public static final String TABLE_WORKOUTS = "workouts";
    public static final String COLUMN_USER_ID = "userId";
    public static final String COLUMN_DAY = "day";
    public static final String COLUMN_TIME = "time";
    public static final String COLUMN_WORKOUT_NAME = "workoutName";
    public static final String COLUMN_REMINDER_ENABLED = "reminderEnabled";

    public static final String TABLE_EXERCISES = "exercises";
    public static final String COLUMN_EXERCISE_ID = "exerciseId";
    public static final String COLUMN_EXERCISE_NAME = "exerciseName";
    public static final String COLUMN_MUSCLE_GROUP = "muscleGroup";

    public static final String TABLE_EXERCISE_LOGS = "exercise_logs";
    public static final String COLUMN_LOG_ID = "logId";
    public static final String COLUMN_LOG_USER_ID = "userId";
    public static final String COLUMN_LOG_EXERCISE_NAME = "exerciseName";
    public static final String COLUMN_NOTES = "notes";
    public static final String COLUMN_DATE = "date";

    public static final String TABLE_EXERCISE_SETS = "exercise_sets";
    public static final String COLUMN_SET_ID = "setId";
    public static final String COLUMN_SET_LOG_ID = "logId";
    public static final String COLUMN_SET_NUMBER = "setNumber";
    public static final String COLUMN_SET_EXERCISE_NAME = "setExerciseName";
    public static final String COLUMN_REPS = "reps";
    public static final String COLUMN_WEIGHT = "weight";

    public static final String TABLE_NOTES = "notes";
    public static final String COLUMN_NOTE_ID = "noteId";
    public static final String COLUMN_NOTE_USER_ID = "userId";
    public static final String COLUMN_NOTE_TITLE = "title";
    public static final String COLUMN_NOTE_CONTENT = "content";
    public static final String COLUMN_NOTE_DATE = "dateCreated";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT)";
        db.execSQL(CREATE_USERS_TABLE);

        String CREATE_WORKOUT_TABLE = "CREATE TABLE " + TABLE_WORKOUTS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " INTEGER, " +
                COLUMN_DAY + " TEXT, " +
                COLUMN_TIME + " TEXT, " +
                COLUMN_WORKOUT_NAME + " TEXT, " +
                COLUMN_REMINDER_ENABLED + " INTEGER)";
        db.execSQL(CREATE_WORKOUT_TABLE);

        String CREATE_EXERCISES_TABLE = "CREATE TABLE " + TABLE_EXERCISES + " (" +
                COLUMN_EXERCISE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_ID + " INTEGER, " +
                COLUMN_EXERCISE_NAME + " TEXT, " +
                COLUMN_MUSCLE_GROUP + " TEXT)";
        db.execSQL(CREATE_EXERCISES_TABLE);

        String CREATE_EXERCISE_LOGS_TABLE = "CREATE TABLE " + TABLE_EXERCISE_LOGS + " (" +
                COLUMN_LOG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_LOG_USER_ID + " INTEGER, " +
                COLUMN_LOG_EXERCISE_NAME + " TEXT, " +
                COLUMN_NOTES + " TEXT, " +
                COLUMN_DATE + " TEXT)";
        db.execSQL(CREATE_EXERCISE_LOGS_TABLE);

        String CREATE_EXERCISE_SETS_TABLE = "CREATE TABLE " + TABLE_EXERCISE_SETS + " (" +
                COLUMN_SET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_SET_LOG_ID + " INTEGER, " +
                COLUMN_SET_NUMBER + " INTEGER, " +
                COLUMN_SET_EXERCISE_NAME + " TEXT, " +
                COLUMN_REPS + " INTEGER, " +
                COLUMN_WEIGHT + " REAL)";
        db.execSQL(CREATE_EXERCISE_SETS_TABLE);

        String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + " (" +
                COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NOTE_USER_ID + " INTEGER, " +
                COLUMN_NOTE_TITLE + " TEXT, " +
                COLUMN_NOTE_CONTENT + " TEXT, " +
                COLUMN_NOTE_DATE + " TEXT)";
        db.execSQL(CREATE_NOTES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISE_SETS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISE_LOGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORKOUTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public long insertWorkout(int userId, String day, String time, String workoutName, boolean reminderEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_DAY, day);
        values.put(COLUMN_TIME, time);
        values.put(COLUMN_WORKOUT_NAME, workoutName);
        values.put(COLUMN_REMINDER_ENABLED, reminderEnabled ? 1 : 0);

        return db.insert(TABLE_WORKOUTS, null, values);
    }

    public Cursor getWorkoutsByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_WORKOUTS + " WHERE " + COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)}
        );
    }

    public boolean deleteWorkoutById(int workoutId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(
                TABLE_WORKOUTS,
                COLUMN_ID + "=?",
                new String[]{String.valueOf(workoutId)}
        );
        return result > 0;
    }



    public boolean updateUserPasswordByEmail(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, newPassword);

        int result = db.update(
                TABLE_USERS,
                values,
                COLUMN_EMAIL + "=?",
                new String[]{email}
        );

        return result > 0;
    }

    public int getUserIdByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_ID + " FROM " + TABLE_USERS +
                        " WHERE " + COLUMN_EMAIL + "=?",
                new String[]{email}
        );

        int userId = -1;

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        }

        cursor.close();
        return userId;
    }


    public int getUserId(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_ID + " FROM " + TABLE_USERS + " WHERE " +
                        COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{email, password}
        );

        int userId = -1;

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        }

        cursor.close();
        return userId;
    }

    public boolean insertUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " +
                        COLUMN_EMAIL + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{email, password}
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_EMAIL + "=?",
                new String[]{email}
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean insertExercise(int userId, String exerciseName, String muscleGroup) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_EXERCISE_NAME, exerciseName);
        values.put(COLUMN_MUSCLE_GROUP, muscleGroup);

        long result = db.insert(TABLE_EXERCISES, null, values);
        return result != -1;
    }

    public Cursor getExercisesByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_EXERCISES +
                        " WHERE " + COLUMN_USER_ID + "=? ORDER BY " + COLUMN_MUSCLE_GROUP,
                new String[]{String.valueOf(userId)}
        );
    }

    public long insertExerciseLog(int userId, String exerciseName, String notes, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_LOG_USER_ID, userId);
        values.put(COLUMN_LOG_EXERCISE_NAME, exerciseName);
        values.put(COLUMN_NOTES, notes);
        values.put(COLUMN_DATE, date);

        return db.insert(TABLE_EXERCISE_LOGS, null, values);
    }

    public boolean insertExerciseSet(long logId, int setNumber, String setExerciseName, int reps, double weight) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_SET_LOG_ID, logId);
        values.put(COLUMN_SET_NUMBER, setNumber);
        values.put(COLUMN_SET_EXERCISE_NAME, setExerciseName);
        values.put(COLUMN_REPS, reps);
        values.put(COLUMN_WEIGHT, weight);

        long result = db.insert(TABLE_EXERCISE_SETS, null, values);
        return result != -1;
    }

    public boolean exerciseLogExistsForDate(int userId, String exerciseName, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_EXERCISE_LOGS +
                        " WHERE " + COLUMN_LOG_USER_ID + "=? AND " +
                        COLUMN_LOG_EXERCISE_NAME + "=? AND " +
                        COLUMN_DATE + "=?",
                new String[]{
                        String.valueOf(userId),
                        exerciseName,
                        date
                }
        );

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public Cursor getExerciseLogsByDate(int userId, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_EXERCISE_LOGS +
                        " WHERE " + COLUMN_LOG_USER_ID + "=? AND " +
                        COLUMN_DATE + "=? ORDER BY " + COLUMN_LOG_ID + " DESC",
                new String[]{String.valueOf(userId), date}
        );
    }

    public Cursor getLoggedWorkoutNamesByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        return db.rawQuery(
                "SELECT MIN(TRIM(" + COLUMN_LOG_EXERCISE_NAME + ")) AS " + COLUMN_LOG_EXERCISE_NAME +
                        " FROM " + TABLE_EXERCISE_LOGS +
                        " WHERE " + COLUMN_LOG_USER_ID + "=? " +
                        " AND TRIM(" + COLUMN_LOG_EXERCISE_NAME + ") != '' " +
                        " GROUP BY LOWER(TRIM(" + COLUMN_LOG_EXERCISE_NAME + ")) " +
                        " ORDER BY " + COLUMN_LOG_EXERCISE_NAME + " ASC",
                new String[]{String.valueOf(userId)}
        );
    }

    public Cursor getExerciseNamesByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT " + COLUMN_EXERCISE_NAME + " FROM " + TABLE_EXERCISES +
                        " WHERE " + COLUMN_USER_ID + "=? ORDER BY " + COLUMN_EXERCISE_NAME + " ASC",
                new String[]{String.valueOf(userId)}
        );
    }

    public Cursor getExerciseSetsByLogId(long logId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_EXERCISE_SETS +
                        " WHERE " + COLUMN_SET_LOG_ID + "=? ORDER BY " + COLUMN_SET_NUMBER + " ASC",
                new String[]{String.valueOf(logId)}
        );
    }

    public Cursor getReminderWorkoutsByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_WORKOUTS +
                        " WHERE " + COLUMN_USER_ID + "=? AND " + COLUMN_REMINDER_ENABLED + "=1" +
                        " ORDER BY " + COLUMN_DAY + " ASC, " + COLUMN_TIME + " ASC",
                new String[]{String.valueOf(userId)}
        );
    }

    public Cursor getAllExerciseLogs(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_EXERCISE_LOGS +
                        " WHERE " + COLUMN_LOG_USER_ID + "=?" +
                        " ORDER BY " + COLUMN_DATE + " DESC, " + COLUMN_LOG_ID + " DESC",
                new String[]{String.valueOf(userId)}
        );
    }

    public Cursor getPersonalBestsByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query =
                "SELECT s." + COLUMN_SET_EXERCISE_NAME + " AS exerciseName, " +
                        "s." + COLUMN_REPS + " AS reps, " +
                        "s." + COLUMN_WEIGHT + " AS bestWeight, " +
                        "l." + COLUMN_DATE + " AS date " +
                        "FROM " + TABLE_EXERCISE_SETS + " s " +
                        "JOIN " + TABLE_EXERCISE_LOGS + " l " +
                        "ON s." + COLUMN_SET_LOG_ID + " = l." + COLUMN_LOG_ID + " " +
                        "WHERE l." + COLUMN_LOG_USER_ID + "=? " +
                        "AND s." + COLUMN_WEIGHT + " = (" +
                        "SELECT MAX(s2." + COLUMN_WEIGHT + ") " +
                        "FROM " + TABLE_EXERCISE_SETS + " s2 " +
                        "JOIN " + TABLE_EXERCISE_LOGS + " l2 " +
                        "ON s2." + COLUMN_SET_LOG_ID + " = l2." + COLUMN_LOG_ID + " " +
                        "WHERE l2." + COLUMN_LOG_USER_ID + "=? " +
                        "AND LOWER(TRIM(s2." + COLUMN_SET_EXERCISE_NAME + ")) = LOWER(TRIM(s." + COLUMN_SET_EXERCISE_NAME + "))" +
                        ") " +
                        "GROUP BY LOWER(TRIM(s." + COLUMN_SET_EXERCISE_NAME + ")) " +
                        "ORDER BY s." + COLUMN_SET_EXERCISE_NAME + " ASC";

        return db.rawQuery(query, new String[]{
                String.valueOf(userId),
                String.valueOf(userId)
        });
    }

    public void deleteExerciseLog(long logId) {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TABLE_EXERCISE_SETS,
                COLUMN_SET_LOG_ID + "=?",
                new String[]{String.valueOf(logId)});

        db.delete(TABLE_EXERCISE_LOGS,
                COLUMN_LOG_ID + "=?",
                new String[]{String.valueOf(logId)});
    }

    public void deleteExerciseLogsByDate(int userId, String date) {
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + COLUMN_LOG_ID +
                        " FROM " + TABLE_EXERCISE_LOGS +
                        " WHERE " + COLUMN_LOG_USER_ID + "=? AND " + COLUMN_DATE + "=?",
                new String[]{String.valueOf(userId), date}
        );

        while (cursor.moveToNext()) {
            long logId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LOG_ID));

            db.delete(TABLE_EXERCISE_SETS,
                    COLUMN_SET_LOG_ID + "=?",
                    new String[]{String.valueOf(logId)});

            db.delete(TABLE_EXERCISE_LOGS,
                    COLUMN_LOG_ID + "=?",
                    new String[]{String.valueOf(logId)});
        }

        cursor.close();
    }

    public boolean deleteExercise(int userId, String exerciseName) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                TABLE_EXERCISES,
                COLUMN_USER_ID + "=? AND TRIM(LOWER(" + COLUMN_EXERCISE_NAME + ")) = TRIM(LOWER(?))",
                new String[]{String.valueOf(userId), exerciseName}
        );

        return result > 0;
    }

    public long insertNote(int userId, String title, String content, String dateCreated) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NOTE_USER_ID, userId);
        values.put(COLUMN_NOTE_TITLE, title);
        values.put(COLUMN_NOTE_CONTENT, content);
        values.put(COLUMN_NOTE_DATE, dateCreated);

        return db.insert(TABLE_NOTES, null, values);
    }

    public Cursor getNotesByUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_NOTES +
                        " WHERE " + COLUMN_NOTE_USER_ID + "=?" +
                        " ORDER BY " + COLUMN_NOTE_ID + " DESC",
                new String[]{String.valueOf(userId)}
        );
    }

    public Cursor getNoteById(int noteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_NOTES +
                        " WHERE " + COLUMN_NOTE_ID + "=?",
                new String[]{String.valueOf(noteId)}
        );
    }

    public boolean updateNote(int noteId, String title, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NOTE_TITLE, title);
        values.put(COLUMN_NOTE_CONTENT, content);

        int result = db.update(
                TABLE_NOTES,
                values,
                COLUMN_NOTE_ID + "=?",
                new String[]{String.valueOf(noteId)}
        );

        return result > 0;
    }

    public boolean deleteNoteById(int noteId) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                TABLE_NOTES,
                COLUMN_NOTE_ID + "=?",
                new String[]{String.valueOf(noteId)}
        );

        return result > 0;
    }
}