package com.yt.myapplication;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.View; // Import שהוספנו
import android.widget.RemoteViews;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WidgetCompactProvider extends AppWidgetProvider {

    private static final String TAG = "WidgetCompact";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        Log.d(TAG, "--- COMPACT WIDGET UPDATE START ---");
        try {
            SharedPreferences prefs = context.getSharedPreferences("compact_widget_prefs_" + appWidgetId, Context.MODE_PRIVATE);
            int contentId = prefs.getInt("content_id", R.id.radio_hebrew_date);
            int textSize = prefs.getInt("text_size", 14);
            int transparency = prefs.getInt("transparency", 128);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.hebrew_widget_layout_1x1);

            // הגדרות עיצוב כלליות
            views.setInt(R.id.iv_widget_background, "setImageAlpha", transparency);
            views.setTextColor(R.id.tv_hebrew_date, Color.WHITE); // צבע לטקסט
            views.setTextColor(R.id.tc_widget_time, Color.WHITE);   // צבע לשעון

            // לוגיקה חדשה: בודקת אם להציג שעון או טקסט
            if (contentId == R.id.radio_time) {
                // אם הבחירה היא "הצג שעה"
                views.setViewVisibility(R.id.tc_widget_time, View.VISIBLE); // מציגים את השעון
                views.setViewVisibility(R.id.tv_hebrew_date, View.GONE);    // מסתירים את הטקסט
                views.setTextViewTextSize(R.id.tc_widget_time, TypedValue.COMPLEX_UNIT_SP, textSize); // קובעים גודל טקסט לשעון
            } else {
                // אם הבחירה היא כל דבר אחר
                views.setViewVisibility(R.id.tc_widget_time, View.GONE);      // מסתירים את השעון
                views.setViewVisibility(R.id.tv_hebrew_date, View.VISIBLE);   // מציגים את הטקסט
                views.setTextViewTextSize(R.id.tv_hebrew_date, TypedValue.COMPLEX_UNIT_SP, textSize); // קובעים גודל טקסט

                // חישוב התוכן של הטקסט
                String contentToShow = "";
                if (contentId == R.id.radio_hebrew_date) {
                    contentToShow = HebrewDateUtils.getHebrewDate();
                } else if (contentId == R.id.radio_gregorian_date) {
                    contentToShow = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());
                } else if (contentId == R.id.radio_parasha) {
                    contentToShow = HebrewDateUtils.getParsha().replace("פרשת ", "");
                }
                views.setTextViewText(R.id.tv_hebrew_date, contentToShow);
            }

            Intent updateIntent = new Intent(context, WidgetCompactProvider.class); // <-- שם המחלקה של ה-Provider
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
            PendingIntent updatePendingIntent = PendingIntent.getBroadcast(context, appWidgetId, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_root, updatePendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);
            Log.d(TAG, "--- COMPACT WIDGET UPDATE SUCCESS ---");

        } catch (Exception e) {
            Log.e(TAG, "FATAL: Compact Widget update failed", e);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // בודקים אם ה-Intent הוא על שינוי תאריך במערכת (קורה בחצות)
        if (Intent.ACTION_DATE_CHANGED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetCompactProvider.class));
            onUpdate(context, appWidgetManager, appWidgetIds); // קוראים לעדכון
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            Log.d(TAG, "Deleting preferences for widget ID: " + appWidgetId);
            SharedPreferences prefs = context.getSharedPreferences("compact_widget_prefs_" + appWidgetId, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
        }
    }
}