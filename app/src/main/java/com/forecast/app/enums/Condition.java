package com.forecast.app.enums;

/**
 * Productivity "weather" condition system.
 * Displayed on the Summary screen to represent daily performance.
 */
public enum Condition {
    STORMY,     // Very low productivity (0–20%)
    RAINY,      // Low productivity (21–40%)
    CLOUDY,     // Moderate productivity (41–60%)
    PARTLY_SUNNY, // Good productivity (61–80%)
    SUNNY       // Excellent productivity (81–100%)
}
