package com.yt.myapplication;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LearningPlan {
    public String id;
    public String masechetName;
    public String startDaf;
    public String endDaf;
    public long startDateTimestamp;
    public long endDateTimestamp;
    public boolean isReminderActive;

    // שדות חדשים לתזכורת מותאמת אישית
    public int reminderHour;    // שעת התזכורת (0-23)
    public int reminderMinute;  // דקת התזכורת (0-59)
    public Set<Integer> reminderDays; // סט של ימות השבוע (Calendar.SUNDAY, MONDAY...)

    // קונסטרוקטור ריק (חובה עבור ספריית Gson)
    // הוא מאתחל את כל ערכי ברירת המחדל
    public LearningPlan() {
        this.id = UUID.randomUUID().toString();
        this.startDateTimestamp = System.currentTimeMillis();
        this.isReminderActive = false;

        // הגדרות ברירת מחדל לתזכורת
        this.reminderHour = 8; // 8 בבוקר
        this.reminderMinute = 0;
        this.reminderDays = new HashSet<>();
        // כברירת מחדל, בחירה בכל ימות השבוע
        for (int i = 1; i <= 7; i++) {
            this.reminderDays.add(i); // 1=SUNDAY, 2=MONDAY, ..., 7=SATURDAY
        }
    }

    // קונסטרוקטור ליצירת תוכנית חדשה מהטופס
    public LearningPlan(String masechetName, String startDaf, String endDaf, long endDateTimestamp) {
        this(); // קורא לקונסטרוקטור הריק כדי לאתחל את כל ברירות המחדל
        this.masechetName = masechetName;
        this.startDaf = startDaf;
        this.endDaf = endDaf;
        this.endDateTimestamp = endDateTimestamp;
    }
}