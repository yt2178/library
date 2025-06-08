package com.yt.myapplication;

import com.kosherjava.zmanim.util.GeoLocation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class CityData {

    private static final Map<String, GeoLocation> cities = new LinkedHashMap<>();

    // בלוק סטטי עם רשימת הערים החדשה והממוקדת לישראל
    static {
        // אזור צפון
        addCity("צפת", 32.9647, 35.4981, "Asia/Jerusalem");
        addCity("חיפה", 32.8184, 34.9885, "Asia/Jerusalem");
        addCity("טבריה", 32.7905, 35.5310, "Asia/Jerusalem");
        addCity("נהריה", 33.0076, 35.0934, "Asia/Jerusalem");

        // אזור מרכז
        addCity("ראשון לציון", 31.9716, 34.7720, "Asia/Jerusalem");
        addCity("בני ברק", 32.0736, 34.8329, "Asia/Jerusalem");
        addCity("גבעתיים", 32.0736, 34.8329, "Asia/Jerusalem");
        addCity("רמלה", 31.9333, 34.8667, "Asia/Jerusalem");
        addCity("לוד", 31.9500, 34.8800, "Asia/Jerusalem");
        addCity("ראש העין", 32.0956, 34.9566, "Asia/Jerusalem");
        addCity("תל אביב – יפו", 32.0809, 34.7806, "Asia/Jerusalem");
        addCity("ירושלים", 31.7690, 35.2163, "Asia/Jerusalem");
        addCity("פתח תקווה", 32.0863, 34.8878, "Asia/Jerusalem");
        addCity("נתניה", 32.3329, 34.8590, "Asia/Jerusalem");
        addCity("רחובות", 31.8928, 34.8113, "Asia/Jerusalem");
        addCity("מודיעין", 31.9037, 35.0084, "Asia/Jerusalem");

        // אזור דרום
        // אזור דרום
        addCity("אופקים", 31.3167, 34.6167, "Asia/Jerusalem");
        addCity("שדרות", 31.52875, 34.60117, "Asia/Jerusalem");
        addCity("אשדוד", 31.7917, 34.6435, "Asia/Jerusalem");
        addCity("באר שבע", 31.2530, 34.7915, "Asia/Jerusalem");
        addCity("אילת", 29.5577, 34.9519, "Asia/Jerusalem");
    }

    private static void addCity(String name, double latitude, double longitude, String timeZoneId) {
        TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
        GeoLocation location = new GeoLocation(name, latitude, longitude, timeZone);
        cities.put(name, location);
    }

    public static List<String> getCityNames() {
        return new ArrayList<>(cities.keySet());
    }

    public static GeoLocation getLocationForCity(String cityName) {
        return cities.get(cityName);
    }
}