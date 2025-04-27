package com.yt.myapplication;

import android.widget.TextView;

import com.kosherjava.zmanim.hebrewcalendar.JewishDate;
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter;

public class HebrewDateUtils {

    // מתודה שמחזירה את התאריך העברי בפורמט יפה
    public static String getHebrewDate() {
        // יצירת תאריך עברי
        JewishDate jewishDate = new JewishDate();

        // יצירת Formatter שממיר תאריך עברי למחרוזת בעברית
        HebrewDateFormatter hdf = new HebrewDateFormatter();
        hdf.setHebrewFormat(true); // קובע שהתאריך יודפס באותיות עבריות

        // קבלת מחרוזת תאריך עברי
        return hdf.format(jewishDate);
    }
}
//// קריאה למתודה ולקבלת התאריך העברי
//String hebrewDateString = HebrewDateUtils.getHebrewDate();
//// הצגת התאריך ב-TextView
//TextView hebrewDateTextView = findViewById(R.id.hebrewDateTextView);
//hebrewDateTextView.setText(hebrewDateString);