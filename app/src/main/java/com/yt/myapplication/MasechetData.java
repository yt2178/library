package com.yt.myapplication;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class MasechetData {
    private static final Map<String, Integer> MASECHET_PAGES = new LinkedHashMap<>();

    static {
        MASECHET_PAGES.put("ברכות", 125);
        MASECHET_PAGES.put("שבת", 337);
        MASECHET_PAGES.put("עירובין", 218);
        MASECHET_PAGES.put("פסחים", 4);
        MASECHET_PAGES.put("שקלים", 5);
        MASECHET_PAGES.put("יומא", 6);
        MASECHET_PAGES.put("סוכה", 56);
        MASECHET_PAGES.put("ביצה", 82);
        MASECHET_PAGES.put("ראש השנה", 35);
        MASECHET_PAGES.put("תענית", 31);
        MASECHET_PAGES.put("מגילה", 32);
        MASECHET_PAGES.put("מועד קטן", 29);
        MASECHET_PAGES.put("חגיגה", 27);
        MASECHET_PAGES.put("יבמות", 122);
        MASECHET_PAGES.put("כתובות", 112);
        MASECHET_PAGES.put("נדרים", 91);
        MASECHET_PAGES.put("נזיר", 66);
        MASECHET_PAGES.put("סוטה", 49);
        MASECHET_PAGES.put("גיטין", 90);
        MASECHET_PAGES.put("קידושין", 82);
        MASECHET_PAGES.put("בבא קמא", 119);
        MASECHET_PAGES.put("בבא מציעא", 119);
        MASECHET_PAGES.put("בבא בתרא", 176);
        MASECHET_PAGES.put("סנהדרין", 113);
        MASECHET_PAGES.put("מכות", 24);
        MASECHET_PAGES.put("שבועות", 49);
        MASECHET_PAGES.put("עבודה זרה", 76);
        MASECHET_PAGES.put("הוריות", 14);
        MASECHET_PAGES.put("זבחים", 120);
        MASECHET_PAGES.put("מנחות", 110);
        MASECHET_PAGES.put("חולין", 142);
        MASECHET_PAGES.put("בכורות", 61);
        MASECHET_PAGES.put("ערכין", 34);
        MASECHET_PAGES.put("תמורה", 34);
        MASECHET_PAGES.put("כריתות", 28);
        MASECHET_PAGES.put("מעילה", 22);
        MASECHET_PAGES.put("קנים", 4);
        MASECHET_PAGES.put("תמיד", 10);
        MASECHET_PAGES.put("מידות", 4);
        MASECHET_PAGES.put("נידה", 73);
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
