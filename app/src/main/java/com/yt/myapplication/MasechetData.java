package com.yt.myapplication;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class MasechetData {
    private static final Map<String, Integer> MASECHET_PAGES = new LinkedHashMap<>();

    static {
        MASECHET_PAGES.put("ברכות", 126);      // 63 דפים - נבדק
        MASECHET_PAGES.put("שבת", 312);        // 156 דפים - נבדק
        MASECHET_PAGES.put("עירובין", 207);    // 103.5 דפים - נבדק
        MASECHET_PAGES.put("פסחים", 240);      // 120 דפים - נבדק
        MASECHET_PAGES.put("ראש השנה", 67);    // 33.5  דפים - נבדק
        MASECHET_PAGES.put("יומא", 174);       // 87 דפים  - נבדק
        MASECHET_PAGES.put("סוכה", 110);       // 55 דפים - נבדק
        MASECHET_PAGES.put("ביצה", 78);        // 39 דפים - נבדק
        MASECHET_PAGES.put("תענית", 59);       // 29.5 דפים - נבדק
        MASECHET_PAGES.put("מגילה", 61);       // 30.5 דפים - נבדק
        MASECHET_PAGES.put("מועד קטן", 55);    // 27.5 דפים  - נבדק
        MASECHET_PAGES.put("חגיגה", 51);       // 25.5 דפים - נבדק
        MASECHET_PAGES.put("יבמות", 242);      // 121 דפים - נבדק
        MASECHET_PAGES.put("כתובות", 222);     // 111 דפים - נבדק
        MASECHET_PAGES.put("נדרים", 180);      // 90 דפים - נבדק
        MASECHET_PAGES.put("נזיר", 130);       // 65 דפים - נבדק
        MASECHET_PAGES.put("סוטה", 96);        // 48 דפים - נבדק
        MASECHET_PAGES.put("גיטין", 178);      // 89 דפים - נבדק
        MASECHET_PAGES.put("קידושין", 162);    // 81 דפים - נבדק
        MASECHET_PAGES.put("בבא קמא", 236);    // 118 דפים - נבדק
        MASECHET_PAGES.put("בבא מציעא", 235);  // 117.5 דפים - נבדק
        MASECHET_PAGES.put("בבא בתרא", 350);   // 175 דפים - נבדק
        MASECHET_PAGES.put("סנהדרין", 224);    // 112 דפים - נבדק
        MASECHET_PAGES.put("מכות", 46);        // 23 דפים - נבדק
        MASECHET_PAGES.put("שבועות", 96);      // 48 דפים - נבדק
        MASECHET_PAGES.put("עבודה זרה", 150);  // 75 דפים - נבדק
        MASECHET_PAGES.put("הוריות", 24);      // 12 דפים - נבדק
        MASECHET_PAGES.put("זבחים", 238);      // 119 דפים - נבדק
        MASECHET_PAGES.put("מנחות", 217);      // 108.5 דפים - נבדק
        MASECHET_PAGES.put("חולין", 281);      // 140.5 דפים - נבדק
        MASECHET_PAGES.put("בכורות", 119);     // 59.5 דפים - נבדק
        MASECHET_PAGES.put("ערכין", 65);       // 32.5 דפים - נבדק
        MASECHET_PAGES.put("תמורה", 65);       // 32.5 דפים - נבדק
        MASECHET_PAGES.put("כריתות", 54);      // 27 דפים - נבדק
        MASECHET_PAGES.put("מעילה", 41);       // 20.5 דפים - נבדק
        MASECHET_PAGES.put("תמיד", 17);        // 8.5 דפים - נבדק
        MASECHET_PAGES.put("מדות", 8);         // 4 דפים
        MASECHET_PAGES.put("קינים", 6);        // 3 דפים
        MASECHET_PAGES.put("נדה", 146);        // 73 דפים

    }

    public static Map<String, Integer> getMasechetPages() {
        return MASECHET_PAGES;
    }

    public static int getPages(String masechetName) {
        if (MASECHET_PAGES.containsKey(masechetName)) {
            return MASECHET_PAGES.get(masechetName);
        } else {
            return 0;
        }
    }


    public static List<String> getMasechetList() {
        return new ArrayList<>(MASECHET_PAGES.keySet());
    }

    public static int getTotalPages() {
        int total = 0;
        for (int pages : MASECHET_PAGES.values()) {
            total += pages;
        }
        return total;
    }
}
