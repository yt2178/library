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

    public static void logAction(Context context, String action) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(HISTORY_DATA, Context.MODE_PRIVATE);

        List<HistoryItem> historyList = loadHistory(context);

        // תאריך עברי ושעה (לדוגמה)
        String hebrewDate = HebrewDateUtils.getHebrewDate();

        HistoryItem newItem = new HistoryItem(action, hebrewDate);

        historyList.add(newItem);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(HISTORY_KEY, convertListToString(historyList));
        editor.apply();
    }

    public static List<HistoryItem> loadHistory(Context context) {
        List<HistoryItem> historyList = new ArrayList<>();
        SharedPreferences sharedPreferences = context.getSharedPreferences(HISTORY_DATA, Context.MODE_PRIVATE);
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
