package com.forecast.app.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.forecast.app.data.local.Converters;
import com.forecast.app.enums.Category;
import com.forecast.app.enums.Priority;

import java.util.Date;

@Entity(tableName = "tasks")
@TypeConverters(Converters.class)
public class Task {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String title;
    private String description;
    private Priority priority;
    private Category category;
    private boolean isCompleted;
    private Date createdAt;
    private Date dueDate;
    private int estimatedPomodoros; // How many focus sessions estimated

    // ── Constructors ──────────────────────────────────────────────────────────

    public Task() {
        this.createdAt = new Date();
        this.isCompleted = false;
        this.priority = Priority.MEDIUM;
        this.category = Category.OTHER;
        this.estimatedPomodoros = 1;
    }

    public Task(String title, String description, Priority priority,
                Category category, Date dueDate, int estimatedPomodoros) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.category = category;
        this.dueDate = dueDate;
        this.estimatedPomodoros = estimatedPomodoros;
        this.isCompleted = false;
        this.createdAt = new Date();
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public int getEstimatedPomodoros() { return estimatedPomodoros; }
    public void setEstimatedPomodoros(int estimatedPomodoros) {
        this.estimatedPomodoros = estimatedPomodoros;
    }
}
