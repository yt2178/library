package com.yt.myapplication;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.kosherjava.zmanim.ZmanimCalendar;
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;
import com.kosherjava.zmanim.util.GeoLocation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WidgetProvider extends AppWidgetProvider {

    private static final String TAG = "HebrewWidget";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        updateAppWidget(context, appWidgetManager, appWidgetId);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (Intent.ACTION_DATE_CHANGED.equals(intent.getAction()) || Intent.ACTION_TIMEZONE_CHANGED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            ComponentName componentName = new ComponentName(context, WidgetProvider.class);
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(componentName));
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            SharedPreferences prefs = context.getSharedPreferences("widget_prefs_" + appWidgetId, Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("widget_prefs_" + appWidgetId, Context.MODE_PRIVATE);
            boolean showGregorian = prefs.getBoolean("show_gregorian", true);
            boolean showParasha = prefs.getBoolean("show_parasha", true);
            int clockSize = prefs.getInt("clock_size", 48);
            String cityName = prefs.getString("city_name", "ירושלים");
            int transparency = prefs.getInt("transparency", 128);
            int textColor = Color.WHITE;

            GeoLocation geoLocation = CityData.getLocationForCity(cityName);
            if (geoLocation == null) {
                geoLocation = CityData.getLocationForCity("ירושלים");
            }

            Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
            int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 40);
            boolean isTall = minHeight >= 100;

            RemoteViews views;
            if (isTall) {
                // אנדרואיד יבחר אוטומטית את קובץ ה-XML הנכון לפי הרוחב
                views = new RemoteViews(context.getPackageName(), R.layout.hebrew_widget_layout_2x2);
            } else {
                views = new RemoteViews(context.getPackageName(), R.layout.hebrew_widget_layout_2x1);
            }

            // --- מילוי נתונים כללי (עובד על כל הגרסאות) ---
            views.setInt(R.id.iv_widget_background, "setImageAlpha", transparency);
            views.setTextColor(R.id.tc_clock, textColor);
            views.setTextViewTextSize(R.id.tc_clock, TypedValue.COMPLEX_UNIT_SP, clockSize);

            // עדכון תאריכים
            views.setTextColor(R.id.tv_hebrew_date, textColor);
            views.setTextViewText(R.id.tv_hebrew_date, HebrewDateUtils.getHebrewDate());

            views.setTextColor(R.id.tv_gregorian_date, textColor);
            String gregorianDateStr = "(" + new SimpleDateFormat("d/M/yy", Locale.getDefault()).format(new Date()) + ")";
            views.setTextViewText(R.id.tv_gregorian_date, gregorianDateStr);
            views.setViewVisibility(R.id.tv_gregorian_date, showGregorian ? View.VISIBLE : View.GONE);

            // עדכון רכיבים שקיימים רק בווידג'ט הגדול
            if (isTall) {
                String parashaStr = HebrewDateUtils.getParsha();
                views.setTextColor(R.id.tv_parasha, textColor);
                views.setViewVisibility(R.id.tv_parasha, (showParasha && parashaStr != null && !parashaStr.trim().isEmpty()) ? View.VISIBLE : View.GONE);
                views.setTextViewText(R.id.tv_parasha, parashaStr);

                ZmanimCalendar zc = new ZmanimCalendar(geoLocation);
                int dayOfWeek = new JewishCalendar().getDayOfWeek();
                views.setTextColor(R.id.tv_shabbat_entry, textColor);
                views.setTextColor(R.id.tv_shabbat_exit, textColor);
                if (dayOfWeek == 6 || dayOfWeek == 7) {
                    SimpleDateFormat shabbatTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Date candleLighting = zc.getCandleLighting();
                    Date shabbatEnd = zc.getTzais();
                    views.setViewVisibility(R.id.tv_shabbat_entry, candleLighting != null ? View.VISIBLE : View.GONE);
                    views.setTextViewText(R.id.tv_shabbat_entry, "כניסת שבת: " + (candleLighting != null ? shabbatTimeFormat.format(candleLighting) : ""));
                    views.setViewVisibility(R.id.tv_shabbat_exit, shabbatEnd != null ? View.VISIBLE : View.GONE);
                    views.setTextViewText(R.id.tv_shabbat_exit, "יציאת שבת: " + (shabbatEnd != null ? shabbatTimeFormat.format(shabbatEnd) : ""));
                } else {
                    views.setViewVisibility(R.id.tv_shabbat_entry, View.GONE);
                    views.setViewVisibility(R.id.tv_shabbat_exit, View.GONE);
                }
            }

            // הגדרת לחיצה
            Intent configIntent = new Intent(context, WidgetConfigActivity.class);
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent configPendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_root, configPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);

        } catch (Exception e) {
            Log.e(TAG, "FATAL: Widget update failed for ID " + appWidgetId, e);
        }
    }
}