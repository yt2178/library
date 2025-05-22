package com.yt.myapplication;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryUtils {

    private static final String HISTORY_DATA = "history_data";
    private static final String HISTORY_KEY = "history_key";

    // פונקציית עזר פנימית לקבלת SharedPreferences
    private static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(HISTORY_DATA, Context.MODE_PRIVATE);
    }

    // שמירת פעולה עם תאריך עברי ושעה
    public static void logAction(Context context, String action) {
        SharedPreferences sharedPreferences = getPrefs(context);

        List<HistoryItem> historyList = loadHistory(context);

        // תאריך עברי + שעה
        String hebrewDate = HebrewDateUtils.getHebrewDate();
        String timestamp = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String fullDate = hebrewDate + " " + timestamp;

        // יצירת פריט חדש עם הפעולה והתאריך-שעה
        HistoryItem newItem = new HistoryItem(action, fullDate);

        // הוספה בתחילת הרשימה (כדי להציג מהחדש לישן)
        historyList.add(0, newItem);

        // שמירה
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(HISTORY_KEY, convertListToString(historyList));
        editor.apply();
    }

    public static List<HistoryItem> loadHistory(Context context) {
        List<HistoryItem> historyList = new ArrayList<>();
        SharedPreferences sharedPreferences = getPrefs(context);
        String raw = sharedPreferences.getString(HISTORY_KEY, "");
        if (!raw.isEmpty()) {
            String[] entries = raw.split("##");
            for (String entry : entries) {
                String[] parts = entry.split("\\|\\|");
                if (parts.length == 2) {
                    historyList.add(new HistoryItem(parts[0], parts[1]));
                }
            }
        }
        return historyList;
    }

    public static String convertListToString(List<HistoryItem> list) {
        StringBuilder sb = new StringBuilder();
        for (HistoryItem item : list) {
            sb.append(item.getAction()).append("||").append(item.getTimestamp()).append("##");
        }
        return sb.toString();
    }
}
