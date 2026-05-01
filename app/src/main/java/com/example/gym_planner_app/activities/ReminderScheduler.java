package com.example.gym_planner_app.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ReminderScheduler {

    public static void scheduleReminder(Context context, int workoutId, int userId, String workoutName, String day, String time) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Calendar triggerCalendar = getNextReminderTime(day, time);
        if (triggerCalendar == null) {
            return;
        }

        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.putExtra("workoutId", workoutId);
        intent.putExtra("userId", userId);
        intent.putExtra("workoutName", workoutName);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                workoutId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerCalendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * 7,
                pendingIntent
        );
    }

    public static void cancelReminder(Context context, int workoutId) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager == null) {
            return;
        }

        Intent intent = new Intent(context, ReminderReceiver.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                workoutId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.cancel(pendingIntent);
    }

    private static Calendar getNextReminderTime(String day, String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        Date parsedTime;

        try {
            parsedTime = sdf.parse(time);
        } catch (ParseException e) {
            return null;
        }

        if (parsedTime == null) {
            return null;
        }

        Calendar now = Calendar.getInstance();
        Calendar trigger = Calendar.getInstance();

        Calendar parsedCalendar = Calendar.getInstance();
        parsedCalendar.setTime(parsedTime);

        int hour = parsedCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = parsedCalendar.get(Calendar.MINUTE);

        trigger.set(Calendar.DAY_OF_WEEK, convertDayToCalendar(day));
        trigger.set(Calendar.HOUR_OF_DAY, hour);
        trigger.set(Calendar.MINUTE, minute);
        trigger.set(Calendar.SECOND, 0);
        trigger.set(Calendar.MILLISECOND, 0);

        trigger.add(Calendar.MINUTE, -30);

        while (trigger.before(now) || trigger.equals(now)) {
            trigger.add(Calendar.DAY_OF_YEAR, 7);
        }

        return trigger;
    }

    private static int convertDayToCalendar(String day) {
        switch (day) {
            case "Sunday":
                return Calendar.SUNDAY;
            case "Monday":
                return Calendar.MONDAY;
            case "Tuesday":
                return Calendar.TUESDAY;
            case "Wednesday":
                return Calendar.WEDNESDAY;
            case "Thursday":
                return Calendar.THURSDAY;
            case "Friday":
                return Calendar.FRIDAY;
            case "Saturday":
                return Calendar.SATURDAY;
            default:
                return Calendar.MONDAY;
        }
    }
}