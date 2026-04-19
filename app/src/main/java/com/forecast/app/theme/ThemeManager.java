package com.forecast.app.theme;

import com.forecast.app.R;
import com.forecast.app.enums.TimeOfDay;
import com.forecast.app.util.DateTimeUtils;

public final class ThemeManager {

    private ThemeManager() {}

    /**
     * Returns the appropriate theme style resource based on the current time of day.
     * Call this in MainActivity.onCreate() BEFORE setContentView().
     *
     * Usage:
     *   setTheme(ThemeManager.getCurrentTheme());
     *   setContentView(R.layout.activity_main);
     */
    public static int getCurrentTheme() {
        TimeOfDay timeOfDay = DateTimeUtils.getCurrentTimeOfDay();
        switch (timeOfDay) {
            case MORNING:   return R.style.Theme_Morning;
            case AFTERNOON: return R.style.Theme_Afternoon;
            case EVENING:   return R.style.Theme_Evening;
            case NIGHT:     return R.style.Theme_Night;
            default:        return R.style.Theme_Afternoon;
        }
    }

    /**
     * Returns the theme for a specific TimeOfDay.
     * Useful for previewing or testing themes manually.
     */
    public static int getThemeFor(TimeOfDay timeOfDay) {
        switch (timeOfDay) {
            case MORNING:   return R.style.Theme_Morning;
            case AFTERNOON: return R.style.Theme_Afternoon;
            case EVENING:   return R.style.Theme_Evening;
            case NIGHT:     return R.style.Theme_Night;
            default:        return R.style.Theme_Afternoon;
        }
    }

    /**
     * Returns a display name for the current time period.
     */
    public static String getCurrentPeriodName() {
        switch (DateTimeUtils.getCurrentTimeOfDay()) {
            case MORNING:   return "Morning";
            case AFTERNOON: return "Afternoon";
            case EVENING:   return "Evening";
            case NIGHT:     return "Night";
            default:        return "Day";
        }
    }
}
