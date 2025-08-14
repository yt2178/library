package com.yt.myapplication;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class PlanCalculator {
    private final LearningPlan plan;
    private final int totalAmudimInPlan;
    private final long totalDaysInPlan;
    private final int amudimPerDayBase;
    private final int remainderDays;

    public PlanCalculator(LearningPlan plan) {
        this.plan = plan;

        TalmudPageCalculator calculator = new TalmudPageCalculator();
        int startAmudNum = calculator.getPageNumber(plan.startDaf);
        int endAmudNum = calculator.getPageNumber(plan.endDaf);

        if (startAmudNum != -1 && endAmudNum != -1) {
            this.totalAmudimInPlan = endAmudNum - startAmudNum + 1;
        } else {
            this.totalAmudimInPlan = 0;
        }

        long startDay = TimeUnit.MILLISECONDS.toDays(plan.startDateTimestamp);
        long endDay = TimeUnit.MILLISECONDS.toDays(plan.endDateTimestamp);
        this.totalDaysInPlan = (endDay - startDay) + 1;

        if (this.totalDaysInPlan > 0 && this.totalAmudimInPlan > 0) {
            this.amudimPerDayBase = this.totalAmudimInPlan / (int) this.totalDaysInPlan;
            this.remainderDays = this.totalAmudimInPlan % (int) this.totalDaysInPlan;
        } else {
            this.amudimPerDayBase = 0;
            this.remainderDays = 0;
        }
    }

    public int getAmudimForDay(int dayIndex) {
        if (dayIndex < remainderDays) {
            return amudimPerDayBase + 1;
        } else {
            return amudimPerDayBase;
        }
    }
// In PlanCalculator.java

    // --- מתודה משודרגת עם טקסטים ברורים יותר ---
    public String getPacingSummary() {
        if (totalDaysInPlan <= 0 || totalAmudimInPlan <= 0) return "נתוני התוכנית אינם תקינים.";

        if (totalAmudimInPlan < totalDaysInPlan) {
            long daysWithoutLearning = totalDaysInPlan - totalAmudimInPlan;
            return String.format(Locale.getDefault(), "קצב קל: עמוד 1 ב-%d ימים, ובשאר הימים מנוחה.", totalAmudimInPlan);
        }

        if (remainderDays == 0) {
            return String.format(Locale.getDefault(), "קצב אחיד: %s.", getPacingString(amudimPerDayBase));
        }

        String basePacing = getPacingString(amudimPerDayBase);
        String extraPacing = getPacingString(amudimPerDayBase + 1);

        if (remainderDays == 1) {
            return String.format(Locale.getDefault(), "קצב משתנה: רוב הימים %s, וביום אחד %s.", basePacing, extraPacing);
        }

        return String.format(Locale.getDefault(), "קצב משתנה: רוב הימים %s, וב-%d ימים %s.", basePacing, remainderDays, extraPacing);
    }

    // --- מתודה משודרגת לסיכום הפשוט ---
    public String getSimplePacingSummary() {
        if (totalDaysInPlan <= 0 || totalAmudimInPlan <= 0) return "מידע לא זמין";

        int minAmudimPerDay = amudimPerDayBase;
        int maxAmudimPerDay = (remainderDays > 0) ? amudimPerDayBase + 1 : amudimPerDayBase;

        if (minAmudimPerDay == maxAmudimPerDay) {
            return String.format(Locale.getDefault(), "קצב: %s", getSimplePacingString(minAmudimPerDay));
        } else {
            String minPacing = getSimplePacingString(minAmudimPerDay);
            String maxPacing = getSimplePacingString(maxAmudimPerDay);
            return String.format(Locale.getDefault(), "קצב: בין %s ל-%s", minPacing, maxPacing);
        }
    }
    private String getSimplePacingString(int amudim) {
        if (amudim == 1) return "עמוד";
        if (amudim == 2) return "דף";
        if (amudim % 2 == 0) { // זוגי
            return String.format(Locale.getDefault(), "%d דפים", amudim / 2);
        } else { // אי-זוגי
            int dapim = amudim / 2;
            return String.format(Locale.getDefault(), "%d דף ועמוד", dapim);
        }
    }
    private String getPacingString(int amudim) {
        if (amudim <= 0) return "דף ריק"; // לא אמור לקרות בתרחישים הרגילים, אבל טוב שיהיה
        if (amudim == 1) return "עמוד אחד";
        if (amudim == 2) return "דף אחד (2 עמודים)";

        if (amudim % 2 == 0) { // זוגי
            return String.format(Locale.getDefault(), "%d דפים (%d עמודים)", amudim / 2, amudim);
        } else { // אי-זוגי
            int dapim = amudim / 2;
            return String.format(Locale.getDefault(), "%d דפים ועמוד (%d עמודים)", dapim, amudim);
        }
    }
}