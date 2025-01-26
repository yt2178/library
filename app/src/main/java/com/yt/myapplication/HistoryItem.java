package com.yt.myapplication;

public class HistoryItem {
    private final String action;
    private final String timestamp;

    public HistoryItem(String action, String timestamp) {
        this.action = action;
        this.timestamp = timestamp;
    }
    public String getAction() {
        return action;
    }
    public String getTimestamp() {
        return timestamp;
    }
}