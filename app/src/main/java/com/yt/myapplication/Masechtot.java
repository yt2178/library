package com.yt.myapplication;

import java.util.ArrayList;
import java.util.List;

public class Masechtot {

    private String startPage = "ב."; // כל מסכת מתחילה ב-ב.
    private String endPage; // כל מסכת מסתיימת בדף אחר

    // בנאי שמקבל את דף הסיום של המסכת
    public Masechtot(String endPage) {
        this.endPage = endPage;
    }
    // פונקציה שתחשב את כל הדפים של המסכת
    public List<String> getPages() {
        List<String> pages = new ArrayList<>();
        String currentPage = startPage;

        // נוסיף דפים עד שהגענו לדף הסיום
        while (!currentPage.equals(endPage)) {
            pages.add(currentPage);
            currentPage = getNextPage(currentPage);
        }
        pages.add(endPage); // נוסיף את דף הסיום
        return pages;
    }
    // פונקציה שתחשב את הדף הבא (מעבר בין עמוד א' לב' וכך הלאה)
    private String getNextPage(String currentPage) {
        char letter = currentPage.charAt(0); // ניקח את האות הראשונה (ב, ג, ד, וכו')
        char nextLetter = (char) (letter + 1); // נבצע עליה אחת באות
        String nextPage = nextLetter + "."; // נוסיף עמוד א' (נקודה)

        // אם הדף היה עמוד א. נעבור לעמוד ב.
        if (currentPage.endsWith(".")) {
            nextPage = nextLetter + ":"; // עמוד ב' (קולון)
        }

        return nextPage;
    }
}

