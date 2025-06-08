package com.yt.myapplication;

import com.kosherjava.zmanim.hebrewcalendar.JewishCalendar;
import com.kosherjava.zmanim.hebrewcalendar.HebrewDateFormatter;
import java.util.Calendar;
import java.util.Date;

public class HebrewDateUtils {

    public static JewishCalendar getJewishCalendar() {
        return new JewishCalendar();
    }

    public static String getHebrewDate() {
        JewishCalendar jc = getJewishCalendar();
        HebrewDateFormatter hdf = new HebrewDateFormatter();
        hdf.setHebrewFormat(true);
        return hdf.format(jc);
    }

    public static String getParsha() {
        // מתחילים מהיום
        Date today = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(today);
        HebrewDateFormatter hdf = new HebrewDateFormatter();
        hdf.setHebrewFormat(true);

        // נבדוק לכל יום בעד שבועיים
        for (int i = 0; i < 14; i++) {
            Date checkDate = cal.getTime();
            JewishCalendar jc = new JewishCalendar(checkDate);
            String parshaName = hdf.formatParsha(jc);
            jc.setInIsrael(true);
            if (parshaName != null && !parshaName.trim().isEmpty()) {
                return "פרשת " + parshaName;
            }
            // נעבור ליום הבא
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        return "אין פרשה קרובה";
    }
}
