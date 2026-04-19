package com.forecast.app.util;

import com.forecast.app.enums.TimeOfDay;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class DateTimeUtils {

    private DateTimeUtils() {}

    // ── Day boundaries ────────────────────────────────────────────────────────

    public static long getStartOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static long getEndOfDay(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    public static long getTodayStart() {
        return getStartOfDay(new Date());
    }

    public static long getTodayEnd() {
        return getEndOfDay(new Date());
    }

    // ── Formatting ────────────────────────────────────────────────────────────

    public static String formatDate(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        return sdf.format(date);
    }

    public static String formatTime(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(date);
    }

    public static String formatDateTime(Date date) {
        if (date == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        return sdf.format(date);
    }

    /**
     * Formats milliseconds countdown into MM:SS string.
     * Used by the timer display.
     */
    public static String formatCountdown(long millis) {
        long totalSeconds = millis / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    // ── Time of Day ───────────────────────────────────────────────────────────

    public static TimeOfDay getCurrentTimeOfDay() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) return TimeOfDay.MORNING;
        if (hour >= 12 && hour < 17) return TimeOfDay.AFTERNOON;
        if (hour >= 17 && hour < 21) return TimeOfDay.EVENING;
        return TimeOfDay.NIGHT;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public static boolean isToday(Date date) {
        if (date == null) return false;
        long start = getTodayStart();
        long end = getTodayEnd();
        long time = date.getTime();
        return time >= start && time <= end;
    }

    public static String getGreeting() {
        switch (getCurrentTimeOfDay()) {
            case MORNING:   return "Good morning!";
            case AFTERNOON: return "Good afternoon!";
            case EVENING:   return "Good evening!";
            case NIGHT:     return "Good night!";
            default:        return "Hello!";
        }
    }
}
