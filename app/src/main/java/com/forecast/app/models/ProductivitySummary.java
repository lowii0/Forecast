package com.forecast.app.models;

import com.forecast.app.enums.Condition;

import java.util.Date;

/**
 * NOT a Room entity – computed at runtime from Task + Session data.
 * Passed as a data object to SummaryFragment via SummaryViewModel.
 */
public class ProductivitySummary {

    private Date date;
    private int totalTasks;
    private int completedTasks;
    private int totalFocusSessions;
    private int completedFocusSessions;
    private int totalFocusMinutes;
    private float productivityScore;   // 0.0 – 100.0
    private Condition condition;       // Weather metaphor

    // ── Constructors ──────────────────────────────────────────────────────────

    public ProductivitySummary() {
        this.date = new Date();
    }

    public ProductivitySummary(Date date, int totalTasks, int completedTasks,
                               int totalFocusSessions, int completedFocusSessions,
                               int totalFocusMinutes, float productivityScore,
                               Condition condition) {
        this.date = date;
        this.totalTasks = totalTasks;
        this.completedTasks = completedTasks;
        this.totalFocusSessions = totalFocusSessions;
        this.completedFocusSessions = completedFocusSessions;
        this.totalFocusMinutes = totalFocusMinutes;
        this.productivityScore = productivityScore;
        this.condition = condition;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    public int getTotalFocusSessions() { return totalFocusSessions; }
    public void setTotalFocusSessions(int totalFocusSessions) {
        this.totalFocusSessions = totalFocusSessions;
    }

    public int getCompletedFocusSessions() { return completedFocusSessions; }
    public void setCompletedFocusSessions(int completedFocusSessions) {
        this.completedFocusSessions = completedFocusSessions;
    }

    public int getTotalFocusMinutes() { return totalFocusMinutes; }
    public void setTotalFocusMinutes(int totalFocusMinutes) {
        this.totalFocusMinutes = totalFocusMinutes;
    }

    public float getProductivityScore() { return productivityScore; }
    public void setProductivityScore(float productivityScore) {
        this.productivityScore = productivityScore;
    }

    public Condition getCondition() { return condition; }
    public void setCondition(Condition condition) { this.condition = condition; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    public float getTaskCompletionRate() {
        if (totalTasks == 0) return 0f;
        return (float) completedTasks / totalTasks * 100f;
    }

    public float getSessionCompletionRate() {
        if (totalFocusSessions == 0) return 0f;
        return (float) completedFocusSessions / totalFocusSessions * 100f;
    }
}
