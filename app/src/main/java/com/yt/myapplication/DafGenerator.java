package com.yt.myapplication;


import java.util.ArrayList;
import java.util.List;

public class DafGenerator {

    // פונקציה ליצירת דפים עד הדף הסופי
    public static List<String> generateDafList(String endDaf) {
        List<String> dafList = new ArrayList<>();

        // יצירת כל הדפים עד "י"
        addRegularDafs(dafList);

        // יצירת הדפים לאחר "י" (יא., יב., ...)
        addExtendedDafs(dafList, "י", 10);  // אחרי "י": יא, יב, יג, ...

        // יצירת הדפים אחרי "כ" (כא., כב., ...)
        addExtendedDafs(dafList, "כ", 20);  // אחרי "כ": כא, כב, כג, ...

        // יצירת הדפים אחרי "ק" (קא., קב., ...)
        addExtendedDafs(dafList, "ק", 30);  // אחרי "ק": קא, קב, קג, ...

        // יצירת הדפים אחרי "קי" (קיא., קיב., ...)
        addExtendedDafs(dafList, "קי", 40);  // אחרי "קי": קיא, קיב, קיג, ...

        // הוספת דפים לפי הדף הסופי
        addFinalDafs(dafList, endDaf);

        return dafList;
    }

    // הוספת דפים רגילים (ב., ב:, ג., ג:, ...)
    private static void addRegularDafs(List<String> dafList) {
        String[] letters = {"ב", "ג", "ד", "ה", "ו", "ז", "ח", "ט", "י"};
        for (String letter : letters) {
            dafList.add(letter + ".");
            dafList.add(letter + ":");
        }
    }

    // הוספת דפים אחרי אותיות כמו "יא", "כב", "קא", ...
    private static void addExtendedDafs(List<String> dafList, String prefix, int count) {
        for (int i = 1; i <= count; i++) {
            String letter = prefix + (i == 1 ? "" : Integer.toString(i));  // י, יא, יב, ...
            dafList.add(letter + ".");
            dafList.add(letter + ":");
        }
    }

    // הוספת דפים עד הדף הסופי
    private static void addFinalDafs(List<String> dafList, String endDaf) {
        // אנחנו מניחים ש"endDaf" זה הדף הסופי (למשל "סד", "קנז" וכו')
        // אם רוצים להוסיף לוגיקה לבדוק את הדף הסופי (כמו סד), צריך לשלב כאן תנאים
        dafList.add(endDaf + ".");
        dafList.add(endDaf + ":");
    }

    public static void main(String[] args) {
        // דוגמה לשימוש
        String endDaf = "סד";  // דף סיום, לדוגמה
        List<String> dafList = generateDafList(endDaf);

        // הדפסת כל הדפים
        for (String daf : dafList) {
            System.out.println(daf);
        }
    }
}
