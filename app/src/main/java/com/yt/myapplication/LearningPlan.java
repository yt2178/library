package com.yt.myapplication;

import java.util.UUID; // To give each plan a unique ID

public class LearningPlan {
    String id;
    String masechetName;
    String startDaf;
    String endDaf;
    long startDateTimestamp;
    long endDateTimestamp;
    boolean isReminderActive; // For notifications
    int reminderHour;
    int reminderMinute;

    // Constructor for creating a new plan
    public LearningPlan(String masechetName, String startDaf, String endDaf, long endDateTimestamp) {
        this.id = UUID.randomUUID().toString(); // Generate a unique ID
        this.masechetName = masechetName;
        this.startDaf = startDaf;
        this.endDaf = endDaf;
        this.startDateTimestamp = System.currentTimeMillis();
        this.endDateTimestamp = endDateTimestamp;
        this.isReminderActive = false; // Default
    }

    // A default constructor is needed for Gson
    public LearningPlan() {}
}