package com.yt.myapplication;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Set;

public class ReminderManager {

    private static final String TAG = "ReminderManager";
    private final Context context;
    private final AlarmManager alarmManager;
    private static final String ACTION_TRIGGER_REMINDER = "com.yt.myapplication.ACTION_TRIGGER_REMINDER";


    public ReminderManager(Context context) {
        this.context = context;
        this.alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    public void setReminder(LearningPlan plan) {
        Log.d(TAG, "setReminder called for plan: " + plan.masechetName + " (ID: " + plan.id + ")");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(context, "אין הרשאה לקביעת תזכורות מדויקות.", Toast.LENGTH_LONG).show();
            return;
        }

        long nextReminderTime = calculateNextReminderTime(plan.reminderHour, plan.reminderMinute, plan.reminderDays);

        if (nextReminderTime == -1) {
            Log.e(TAG, "calculateNextReminderTime returned -1. No reminder set.");
            return;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        String formattedTime = sdf.format(nextReminderTime);
        Log.i(TAG, "Setting exact alarm for plan " + plan.id + " at: " + formattedTime);

        // --- שינוי ל-Explicit Intent ---
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_TRIGGER_REMINDER);
        intent.setPackage(context.getPackageName());
        intent.putExtra(AlarmReceiver.EXTRA_PLAN_ID, plan.id);
        intent.putExtra(AlarmReceiver.EXTRA_PLAN_NAME, plan.masechetName);

        int requestCode = plan.id.hashCode();
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                flags
        );

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, nextReminderTime, pendingIntent);
    }

    public void cancelReminder(LearningPlan plan) {
        Log.d(TAG, "cancelReminder called for plan: " + plan.masechetName + " (ID: " + plan.id + ")");

        // --- שינוי ל-Explicit Intent ---
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(ACTION_TRIGGER_REMINDER);
        intent.setPackage(context.getPackageName());
        intent.putExtra(AlarmReceiver.EXTRA_PLAN_ID, plan.id);

        int requestCode = plan.id.hashCode();
        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                flags
        );

        alarmManager.cancel(pendingIntent);
    }

    // המתודה calculateNextReminderTime נשארת כמו שהיא (היא תקינה)
    private long calculateNextReminderTime(int hour, int minute, Set<Integer> days) {
        if (days == null || days.isEmpty()) {
            return -1;
        }
        Calendar now = Calendar.getInstance();
        Calendar candidate = Calendar.getInstance();
        candidate.set(Calendar.HOUR_OF_DAY, hour);
        candidate.set(Calendar.MINUTE, minute);
        candidate.set(Calendar.SECOND, 0);
        candidate.set(Calendar.MILLISECOND, 0);

        for (int i = 0; i < 7; i++) {
            if (candidate.getTimeInMillis() > now.getTimeInMillis()) {
                int dayOfWeek = candidate.get(Calendar.DAY_OF_WEEK);
                if (days.contains(dayOfWeek)) {
                    return candidate.getTimeInMillis();
                }
            }
            candidate.add(Calendar.DAY_OF_YEAR, 1);
        }
        return -1;
    }
}