package com.forecast.app.data.local;

import androidx.room.TypeConverter;

import com.forecast.app.enums.Category;
import com.forecast.app.enums.Priority;
import com.forecast.app.enums.TimerState;

import java.util.Date;

public class Converters {

    // ── Date ──────────────────────────────────────────────────────────────────

    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    // ── Priority ──────────────────────────────────────────────────────────────

    @TypeConverter
    public static Priority fromPriorityString(String value) {
        return value == null ? null : Priority.valueOf(value);
    }

    @TypeConverter
    public static String priorityToString(Priority priority) {
        return priority == null ? null : priority.name();
    }

    // ── Category ──────────────────────────────────────────────────────────────

    @TypeConverter
    public static Category fromCategoryString(String value) {
        return value == null ? null : Category.valueOf(value);
    }

    @TypeConverter
    public static String categoryToString(Category category) {
        return category == null ? null : category.name();
    }

    // ── TimerState ────────────────────────────────────────────────────────────

    @TypeConverter
    public static TimerState fromTimerStateString(String value) {
        return value == null ? null : TimerState.valueOf(value);
    }

    @TypeConverter
    public static String timerStateToString(TimerState state) {
        return state == null ? null : state.name();
    }
}
