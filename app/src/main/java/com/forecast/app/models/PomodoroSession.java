package com.forecast.app.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.forecast.app.data.local.Converters;
import com.forecast.app.enums.TimerState;

import java.util.Date;

@Entity(
    tableName = "pomodoro_sessions",
    foreignKeys = @ForeignKey(
        entity = Task.class,
        parentColumns = "id",
        childColumns = "taskId",
        onDelete = ForeignKey.SET_NULL
    ),
    indices = {@Index("taskId")}
)
@TypeConverters(Converters.class)
public class PomodoroSession {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private Integer taskId;       // Nullable – session may not be linked to a task
    private TimerState type;      // FOCUS or SHORT_BREAK or LONG_BREAK
    private Date startTime;
    private Date endTime;
    private int durationMinutes;  // Actual completed duration
    private boolean completed;    // Was the session completed without interruption?

    // ── Constructors ──────────────────────────────────────────────────────────

    public PomodoroSession() {}

    public PomodoroSession(Integer taskId, TimerState type, int durationMinutes) {
        this.taskId = taskId;
        this.type = type;
        this.durationMinutes = durationMinutes;
        this.startTime = new Date();
        this.completed = false;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getTaskId() { return taskId; }
    public void setTaskId(Integer taskId) { this.taskId = taskId; }

    public TimerState getType() { return type; }
    public void setType(TimerState type) { this.type = type; }

    public Date getStartTime() { return startTime; }
    public void setStartTime(Date startTime) { this.startTime = startTime; }

    public Date getEndTime() { return endTime; }
    public void setEndTime(Date endTime) { this.endTime = endTime; }

    public int getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
