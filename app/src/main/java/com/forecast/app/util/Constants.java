package com.forecast.app.util;

public final class Constants {

    private Constants() {} // Prevent instantiation

    // ── Timer Durations (milliseconds) ────────────────────────────────────────

    public static final long FOCUS_DURATION_MS        = 25 * 60 * 1000L; // 25 minutes
    public static final long SHORT_BREAK_DURATION_MS  =  5 * 60 * 1000L; //  5 minutes
    public static final long LONG_BREAK_DURATION_MS   = 15 * 60 * 1000L; // 15 minutes

    // ── Timer Durations (minutes) ─────────────────────────────────────────────

    public static final int FOCUS_DURATION_MIN       = 25;
    public static final int SHORT_BREAK_DURATION_MIN =  5;
    public static final int LONG_BREAK_DURATION_MIN  = 15;

    // ── Pomodoro Cycle ────────────────────────────────────────────────────────

    /** Number of focus sessions before a long break */
    public static final int SESSIONS_BEFORE_LONG_BREAK = 4;

    // ── SharedPreferences Keys ────────────────────────────────────────────────

    public static final String PREF_FILE_NAME        = "productivity_prefs";
    public static final String PREF_SELECTED_TASK_ID = "selected_task_id";
    public static final String PREF_SESSION_COUNT    = "session_count_today";

    // ── Navigation / Bundle Keys ──────────────────────────────────────────────

    public static final String KEY_TASK_ID = "task_id";
    public static final String KEY_IS_EDIT = "is_edit";

    // ── Notification ──────────────────────────────────────────────────────────

    public static final String NOTIFICATION_CHANNEL_ID   = "pomodoro_channel";
    public static final String NOTIFICATION_CHANNEL_NAME = "Pomodoro Timer";
    public static final int    NOTIFICATION_ID            = 1001;

    // ── Score Thresholds (%) ──────────────────────────────────────────────────

    public static final float SCORE_SUNNY        = 81f;
    public static final float SCORE_PARTLY_SUNNY = 61f;
    public static final float SCORE_CLOUDY       = 41f;
    public static final float SCORE_RAINY        = 21f;
    // Below SCORE_RAINY → STORMY

    // ── Productivity Score Weights ─────────────────────────────────────────────

    public static final float WEIGHT_TASK_COMPLETION    = 0.5f;
    public static final float WEIGHT_SESSION_COMPLETION = 0.5f;
}
