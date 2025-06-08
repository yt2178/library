package com.yt.myapplication;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
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
import java.util.TimeZone;

public class HebrewDateWidgetProvider extends AppWidgetProvider {

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

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        try {
            SharedPreferences prefs = context.getSharedPreferences("widget_prefs_" + appWidgetId, Context.MODE_PRIVATE);
            boolean showGregorian = prefs.getBoolean("show_gregorian", true);
            boolean showParasha = prefs.getBoolean("show_parasha", true);
            boolean isClockBold = prefs.getBoolean("clock_bold", true); // אנחנו עדיין קוראים את זה, פשוט לא משתמשים כרגע
            int clockSize = prefs.getInt("clock_size", 48);
            double latitude = Double.parseDouble(prefs.getString("latitude", "32.0853"));
            double longitude = Double.parseDouble(prefs.getString("longitude", "34.7818"));
            int transparency = prefs.getInt("transparency", 128);
            int textColor = Color.WHITE;

            Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
            int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 40);
            RemoteViews views;

            if (minHeight < 100) {
                views = new RemoteViews(context.getPackageName(), R.layout.hebrew_widget_layout_2x1);
            } else {
                views = new RemoteViews(context.getPackageName(), R.layout.hebrew_widget_layout_2x2);
            }

            views.setInt(R.id.widget_root, "setBackgroundColor", Color.argb(transparency, 0, 0, 0));

            // עדכון השעון (עם TextView רגיל)
            String currentTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
            views.setTextViewText(R.id.tv_clock, currentTime);
            views.setTextColor(R.id.tv_clock, textColor);

            // הגדרת גודל הטקסט של השעון - זה יציב
            views.setTextViewTextSize(R.id.tv_clock, TypedValue.COMPLEX_UNIT_SP, clockSize);


            views.setTextColor(R.id.tv_hebrew_date, textColor);
            String hebrewDateStr = HebrewDateUtils.getHebrewDate();
            views.setTextViewText(R.id.tv_hebrew_date, hebrewDateStr);

            if (minHeight >= 100) {
                String parashaStr = HebrewDateUtils.getParsha();
                String gregorianDateStr = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault()).format(new Date());
                views.setTextColor(R.id.tv_gregorian_date, textColor);
                views.setTextColor(R.id.tv_parasha, textColor);
                views.setTextColor(R.id.tv_shabbat_times, textColor);

                views.setViewVisibility(R.id.tv_gregorian_date, showGregorian ? View.VISIBLE : View.GONE);
                views.setTextViewText(R.id.tv_gregorian_date, gregorianDateStr);

                if (showParasha && parashaStr != null && !parashaStr.isEmpty()) {
                    views.setViewVisibility(R.id.tv_parasha, View.VISIBLE);
                    views.setTextViewText(R.id.tv_parasha, parashaStr);
                } else {
                    views.setViewVisibility(R.id.tv_parasha, View.GONE);
                }

                GeoLocation geoLocation = new GeoLocation("UserLocation", latitude, longitude, TimeZone.getDefault());
                ZmanimCalendar zc = new ZmanimCalendar(geoLocation);
                JewishCalendar jc = new JewishCalendar();
                if (jc.getDayOfWeek() == 6) {
                    SimpleDateFormat shabbatTimeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    Date candleLighting = zc.getCandleLighting();
                    views.setTextViewText(R.id.tv_shabbat_times, "כניסת שבת: " + shabbatTimeFormat.format(candleLighting));
                    views.setViewVisibility(R.id.tv_shabbat_times, View.VISIBLE);
                } else {
                    views.setViewVisibility(R.id.tv_shabbat_times, View.GONE);
                }
            }

            Intent configIntent = new Intent(context, WidgetConfigActivity.class);
            configIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            int flags = PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
            PendingIntent configPendingIntent = PendingIntent.getActivity(context, appWidgetId, configIntent, flags);
            views.setOnClickPendingIntent(R.id.widget_root, configPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, views);

        } catch (Exception e) {
            // אם משהו עדיין קורס, זה ידפיס את השגיאה ללוג
            Log.e(TAG, "FATAL: Widget update failed", e);
        }
    }
}