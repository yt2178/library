package com.yt.myapplication;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";
    public static final String CHANNEL_ID = "learning_plan_channel";
    public static final String EXTRA_PLAN_ID = "extra_plan_id";
    public static final String EXTRA_PLAN_NAME = "extra_plan_name"; // נוסיף את הקבוע הזה


    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive TRIGGERED!");

        // שלוף את המידע ישירות מה-Intent
        String planId = intent.getStringExtra(EXTRA_PLAN_ID);
        String planName = intent.getStringExtra(EXTRA_PLAN_NAME);

        if (planId == null || planName == null) {
            Log.e(TAG, "Received intent with missing data. Aborting.");
            return;
        }
        Log.d(TAG, "Received alarm for plan: " + planName + " (ID: " + planId + ")");

        // --- הצגת ההתראה ---
        int notificationId = planId.hashCode();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        createNotificationChannel(notificationManager);

        Intent mainActivityIntent = new Intent(context, Welcome.class); // פותח את מסך הפתיחה
        mainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, mainActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("תזכורת יומית ללימוד")
                .setContentText("הגיע הזמן ללמוד: " + planName)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();

        Log.d(TAG, "Showing notification for " + planName);
        notificationManager.notify(notificationId, notification);

        // --- קביעת התזכורת הבאה ---
        // כדי לקבוע מחדש, אנחנו עדיין צריכים את הגדרות התוכנית המלאות
        PlanManager planManager = new PlanManager(context);
        LearningPlan plan = planManager.getPlanById(planId);

        if (plan != null && plan.isReminderActive) {
            Log.d(TAG, "Rescheduling next reminder for " + plan.masechetName);
            ReminderManager reminderManager = new ReminderManager(context);
            reminderManager.setReminder(plan);
        } else {
            Log.w(TAG, "Could not find plan or reminder is inactive. Not rescheduling.");
        }
    }

    private void createNotificationChannel(NotificationManager notificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "תזכורות לימוד";
            String description = "ערוץ לקבלת תזכורות יומיות לתוכניות הלימוד";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            notificationManager.createNotificationChannel(channel);
        }
    }
}