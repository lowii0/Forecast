package com.forecast.app.util;

import com.forecast.app.enums.Condition;

public final class ProductivityCalculator {

    private ProductivityCalculator() {}

    /**
     * Calculates a productivity score between 0.0 and 100.0.
     *
     * Formula:
     *   score = (taskCompletionRate * WEIGHT_TASK) + (sessionCompletionRate * WEIGHT_SESSION)
     *
     * If there are no tasks or sessions for the day, that component is treated as 0.
     */
    public static float calculateScore(int totalTasks, int completedTasks,
                                       int totalSessions, int completedSessions) {
        float taskRate = 0f;
        if (totalTasks > 0) {
            taskRate = (float) completedTasks / totalTasks * 100f;
        }

        float sessionRate = 0f;
        if (totalSessions > 0) {
            sessionRate = (float) completedSessions / totalSessions * 100f;
        }

        // If there are no tasks AND no sessions, return 0
        if (totalTasks == 0 && totalSessions == 0) return 0f;

        // If only one type of data exists, weight it fully
        if (totalTasks == 0) return sessionRate;
        if (totalSessions == 0) return taskRate;

        return (taskRate * Constants.WEIGHT_TASK_COMPLETION)
             + (sessionRate * Constants.WEIGHT_SESSION_COMPLETION);
    }

    /**
     * Maps a productivity score to a weather Condition.
     */
    public static Condition getCondition(float score) {
        if (score >= Constants.SCORE_SUNNY)        return Condition.SUNNY;
        if (score >= Constants.SCORE_PARTLY_SUNNY) return Condition.PARTLY_SUNNY;
        if (score >= Constants.SCORE_CLOUDY)       return Condition.CLOUDY;
        if (score >= Constants.SCORE_RAINY)        return Condition.RAINY;
        return Condition.STORMY;
    }

    /**
     * Returns a human-readable label for a Condition.
     */
    public static String getConditionLabel(Condition condition) {
        switch (condition) {
            case SUNNY:        return "Excellent Day! ☀️";
            case PARTLY_SUNNY: return "Good Progress! ⛅";
            case CLOUDY:       return "Keep Going! ☁️";
            case RAINY:        return "Slow Day 🌧️";
            case STORMY:       return "Rough Day ⛈️";
            default:           return "Unknown";
        }
    }

    /**
     * Returns a motivational message based on the condition.
     */
    public static String getMotivationalMessage(Condition condition) {
        switch (condition) {
            case SUNNY:        return "Outstanding work! You crushed today's goals!";
            case PARTLY_SUNNY: return "Great job! You're building strong habits.";
            case CLOUDY:       return "Decent progress. A little more push tomorrow!";
            case RAINY:        return "Tough day, but you showed up. That counts.";
            case STORMY:       return "Every storm runs out of rain. Try again tomorrow.";
            default:           return "";
        }
    }
}
