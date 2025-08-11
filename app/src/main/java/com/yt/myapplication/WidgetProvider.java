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
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import com.kosherjava.zmanim.ZmanimCalendar;
import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;
import com.kosherjava.zmanim.util.GeoLocation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class WidgetProvider extends AppWidgetProvider {

    private static final String TAG = "HebrewWidget";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        String action = intent.getAction();
        if (action != null && (action.equals(Intent.ACTION_DATE_CHANGED) || action.equals(Intent.ACTION_TIMEZONE_CHANGED) || action.equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE))) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, WidgetProvider.class));
            onUpdate(context, appWidgetManager, appWidgetIds);
        }
    }

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
            if (geoLocation == null) geoLocation = CityData.getLocationForCity("ירושלים");

            Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
            int minWidthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            int minHeightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
            boolean isTall = minHeightDp >= 100;

            RemoteViews views;
            if (isTall) {
                views = new RemoteViews(context.getPackageName(), R.layout.hebrew_widget_layout_2x2);
            } else {
                views = new RemoteViews(context.getPackageName(), R.layout.hebrew_widget_layout_2x1);
            }

            // עדכון הגדרות עיצוב כלליות
            views.setInt(R.id.iv_widget_background, "setImageAlpha", transparency);
            views.setTextColor(R.id.tc_clock, textColor);
            views.setTextViewTextSize(R.id.tc_clock, TypedValue.COMPLEX_UNIT_SP, clockSize);

            // הכנת הנתונים פעם אחת
            String hebrewDateStr = HebrewDateUtils.getHebrewDate();
            String gregorianDateStr = "(" + new SimpleDateFormat("d/M/yy", Locale.getDefault()).format(new Date()) + ")";

            if (isTall) {
                // ============== עדכון ווידג'ט גדול (2x2) ==============

                // 1. הוספת שם היום
                String dayOfWeekStr = HebrewDateUtils.getHebrewDayOfWeek();
                views.setTextColor(R.id.tv_day_of_week, textColor);
                views.setTextViewText(R.id.tv_day_of_week, dayOfWeekStr);

                // 2. לוגיקה לסידור תאריכים לפי רוחב
                if (minWidthDp > 180) { // מצב רחב (3 תאים ומעלה)
                    views.setViewVisibility(R.id.date_container_vertical, View.GONE);
                    views.setViewVisibility(R.id.date_container_horizontal, View.VISIBLE);

                    views.setTextViewText(R.id.tv_hebrew_date_h, hebrewDateStr);
                    views.setTextViewText(R.id.tv_gregorian_date_h, gregorianDateStr);
                    views.setTextColor(R.id.tv_hebrew_date_h, textColor);
                    views.setTextColor(R.id.tv_gregorian_date_h, textColor);
                    views.setViewVisibility(R.id.tv_gregorian_date_h, showGregorian ? View.VISIBLE : View.GONE);
                } else { // מצב צר (2 תאים)
                    views.setViewVisibility(R.id.date_container_vertical, View.VISIBLE);
                    views.setViewVisibility(R.id.date_container_horizontal, View.GONE);

                    views.setTextViewText(R.id.tv_hebrew_date_v, hebrewDateStr);
                    views.setTextViewText(R.id.tv_gregorian_date_v, gregorianDateStr);
                    views.setTextColor(R.id.tv_hebrew_date_v, textColor);
                    views.setTextColor(R.id.tv_gregorian_date_v, textColor);
                    views.setViewVisibility(R.id.tv_gregorian_date_v, showGregorian ? View.VISIBLE : View.GONE);
                }

                // 3. עדכון פרשה וזמני שבת
                String parashaStr = HebrewDateUtils.getParsha();
                views.setTextColor(R.id.tv_parasha, textColor);
                views.setViewVisibility(R.id.tv_parasha, showParasha && !parashaStr.isEmpty() ? View.VISIBLE : View.GONE);
                views.setTextViewText(R.id.tv_parasha, parashaStr);

                ZmanimCalendar zc = new ZmanimCalendar(geoLocation);
                if (new JewishCalendar().getDayOfWeek() == 6 || new JewishCalendar().getDayOfWeek() == 7) {
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

            } else {
                // ============== עדכון ווידג'ט בינוני (2x1) ==============
                views.setTextColor(R.id.tv_hebrew_date, textColor);
                views.setTextViewText(R.id.tv_hebrew_date, hebrewDateStr);
                // 4. הוספת התאריך הלועזי גם לווידג'ט 2x1
                views.setTextColor(R.id.tv_gregorian_date, textColor);
                views.setTextViewText(R.id.tv_gregorian_date, gregorianDateStr);
                views.setViewVisibility(R.id.tv_gregorian_date, showGregorian ? View.VISIBLE : View.GONE);
            }
            // ============== 5. עדכון פעולת הלחיצה לעדכון הווידג'ט ==============
            Intent updateIntent = new Intent(context, WidgetProvider.class);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[]{appWidgetId});
            PendingIntent updatePendingIntent = PendingIntent.getBroadcast(context, appWidgetId, updateIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.widget_root, updatePendingIntent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        } catch (Exception e) {
            Log.e(TAG, "FATAL: Widget update failed for ID " + appWidgetId, e);
        }
    }}