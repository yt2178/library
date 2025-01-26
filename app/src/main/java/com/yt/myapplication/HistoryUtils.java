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
    private static SharedPreferences sharedPreferences;

    // אתחול של SharedPreferences
    public static void init(Context context) {
        sharedPreferences = context.getSharedPreferences(HISTORY_DATA, Context.MODE_PRIVATE);
    }

    // שמירת פעולה עם תאריך ושעה
    public static void logAction(Context context, String action) {
        // אתחול SharedPreferences אם לא אתחול קודם
        if (sharedPreferences == null) {
            init(context);
        }

        // יצירת רשימת פעולות
        List<HistoryItem> historyList = loadHistory(context);

        // יצירת אובייקט HistoryItem עם הפעולה והזמן הנוכחי
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        String dateTime = sdf.format(new Date());
        HistoryItem newItem = new HistoryItem(action, dateTime);

        // הוספת הפעולה לרשימה
        historyList.add(0,newItem);

        // שמירה ב-SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(HISTORY_KEY, convertListToString(historyList));
        editor.apply();
    }

    // המרת רשימה למחרוזת לשמירה ב-SharedPreferences
    private static String convertListToString(List<HistoryItem> list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (HistoryItem item : list) {
            stringBuilder.append(item.getAction()).append(" - ").append(item.getTimestamp()).append("\n");
        }
        return stringBuilder.toString();
    }

    // טעינת ההיסטוריה מ-SharedPreferences
    public static List<HistoryItem> loadHistory(Context context) {
        List<HistoryItem> historyList = new ArrayList<>();
        String historyString = sharedPreferences.getString(HISTORY_KEY, "");
        if (!historyString.isEmpty()) {
            String[] historyItems = historyString.split("\n");
            for (String historyItem : historyItems) {
                String[] parts = historyItem.split(" - ");
                if (parts.length == 2) {
                    historyList.add(new HistoryItem(parts[0], parts[1]));
                }
            }
        }
        return historyList;
    }
}
