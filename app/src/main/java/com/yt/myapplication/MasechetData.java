package com.yt.myapplication;
import java.util.HashMap;

public class MasechetData {
    private HashMap<String, Integer> masechetMap;

    // אתחול המידע על המסכתות
    public MasechetData() {
        masechetMap = new HashMap<>();
        masechetMap.put("ברכות", 3);
        masechetMap.put("שבת", 156);
        masechetMap.put("עירובין", 105);
        masechetMap.put("פסחים", 121);
        masechetMap.put("שקלים", 22);
        masechetMap.put("יומא", 88);
        masechetMap.put("סוכה", 56);
        masechetMap.put("ביצה", 40);
        masechetMap.put("ראש השנה", 34);
        masechetMap.put("תענית", 31);
        masechetMap.put("מגילה", 32);
        masechetMap.put("מועד קטן", 29);
        masechetMap.put("חגיגה", 27);
        masechetMap.put("יבמות", 120);
        masechetMap.put("כתובות", 112);
        masechetMap.put("נדרים", 50);
        masechetMap.put("נזיר", 51);
        masechetMap.put("סוטה", 47);
        masechetMap.put("גיטין", 90);
        masechetMap.put("קידושין", 72);
        masechetMap.put("בבא קמא", 10);
        masechetMap.put("בבא מציעא", 10);
        masechetMap.put("בבא בתרא", 10);
        masechetMap.put("סנהדרין", 11);
        masechetMap.put("מכות", 9);
        masechetMap.put("שבועות", 4);
        masechetMap.put("עבודה זרה", 4);
        masechetMap.put("הוריות", 3);
        masechetMap.put("זבחים", 14);
        masechetMap.put("מנחות", 13);
        masechetMap.put("חולין", 14);
        masechetMap.put("בכורות", 9);
        masechetMap.put("ערכין", 9);
        masechetMap.put("תמורה", 7);
        masechetMap.put("כריתות", 6);
        masechetMap.put("מעילה", 6);
        masechetMap.put("קנים", 5);
        masechetMap.put("תמיד", 7);
        masechetMap.put("מידות", 5);
        masechetMap.put("נידה", 12);
    }

    // להחזיר את ה-HashMap של המסכתות
    public HashMap<String, Integer> getMasechetMap() {
        return masechetMap;
    }

    // לקבל את מספר הדפים של מסכת נתונה
    public Integer getPages(String masechet) {
        return masechetMap.get(masechet);
    }
}
