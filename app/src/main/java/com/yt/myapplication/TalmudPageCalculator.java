package com.yt.myapplication;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class TalmudPageCalculator {
    private static final String TAG = "TalmudPageCalculator";

    private static final String[] HEBREW_LETTERS = {
            "ב", "ג", "ד", "ה", "ו", "ז", "ח", "ט", "י",
            "יא", "יב", "יג", "יד", "טו", "טז", "יז", "יח", "יט", "כ",
            "כא", "כב", "כג", "כד", "כה", "כו", "כז", "כח", "כט", "ל",
            "לא", "לב", "לג", "לד", "לה", "לו", "לז", "לח", "לט", "מ",
            "מא", "מב", "מג", "מד", "מה", "מו", "מז", "מח", "מט", "נ",
            "נא", "נב", "נג", "נד", "נה", "נו", "נז", "נח", "נט", "ס",
            "סא", "סב", "סג", "סד", "סה", "סו", "סז", "סח", "סט", "ע",
            "עא", "עב", "עג", "עד", "עה", "עו", "עז", "עח", "עט", "פ",
            "פא", "פב", "פג", "פד", "פה", "פו", "פז", "פח", "פט", "צ",
            "צא", "צב", "צג", "צד", "צה", "צו", "צז", "צח", "צט", "ק",
            "קא", "קב", "קג", "קד", "קה", "קו", "קז", "קח", "קט", "קי",
            "קיא", "קיב", "קיג", "קיד", "קטו", "קטז", "קיז", "קיח", "קיט", "קכ",
            "קכא", "קכב", "קכג", "קכד", "קכה", "קכו", "קכז", "קכח", "קכט", "קל",
            "קלא", "קלב", "קלג", "קלד", "קלה", "קלו", "קלז", "קלח", "קלט", "קמ",
            "קמא", "קמב", "קמג", "קמד", "קמה", "קמו", "קמז", "קמח", "קמט", "קנ",
            "קנא", "קנב", "קנג", "קנד", "קנה", "קנו", "קנז", "קנח", "קנט", "קס",
            "קסא", "קסב", "קסג", "קסד", "קסה", "קסו", "קסז", "קסח", "קסט", "קע",
            "קעא", "קעב", "קעג", "קעד", "קעה", "קעו"
    };

    private StringBuilder resultBuilder;
    private int totalPages;

    public TalmudPageCalculator() {
        resultBuilder = new StringBuilder();
        totalPages = 0;
    }

    public String calculatePages(String endPage) {
        resultBuilder.setLength(0); // Clear previous results
        totalPages = 0;
        boolean found = false;

        resultBuilder.append("חישוב דפים מדף ב. עד ").append(endPage).append(":\n\n");

        for (String letter : HEBREW_LETTERS) {
            // Add page A (with one dot)
            resultBuilder.append(letter).append(".\n");
            totalPages++;

            if ((letter + ".").equals(endPage)) {
                found = true;
                break;
            }

            // Add page B (with two dots)
            resultBuilder.append(letter).append(":\n");
            totalPages++;

            if ((letter + ":").equals(endPage)) {
                found = true;
                break;
            }
        }

        if (!found) {
            Log.w(TAG, "End page not found: " + endPage);
        }

        resultBuilder.append("\nסך הכל דפים: ").append(totalPages);
        return resultBuilder.toString();
    }

    public int getTotalPages() {
        return totalPages;
    }

    public String getPageByNumber(int pageNumber) {
        if (pageNumber < 1 || pageNumber > HEBREW_LETTERS.length * 2) {
            return "דף לא חוקי";
        }

        int letterIndex = (pageNumber - 1) / 2;
        boolean isPageB = (pageNumber % 2) == 0;

        return HEBREW_LETTERS[letterIndex] + (isPageB ? ":" : ".");
    }

    public int getPageNumber(String page) {
        for (int i = 0; i < HEBREW_LETTERS.length; i++) {
            if ((HEBREW_LETTERS[i] + ".").equals(page)) {
                return (i * 2) + 1;
            }
            if ((HEBREW_LETTERS[i] + ":").equals(page)) {
                return (i * 2) + 2;
            }
        }
        return -1; // Page not found
    }
    public List<String> calculatePages(int totalPages) {
        List<String> pagesList = new ArrayList<>();
        int pagesToGenerate = totalPages;

        for (int i = 0; i < totalPages / 2; i++) {
            String letter = HEBREW_LETTERS[i];
            // Add page A (with one dot)
            pagesList.add(letter + ".");
            // Add page B (with two dots)
            pagesList.add(letter + ":");
        }

        return pagesList;
    }
}