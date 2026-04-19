package com.forecast.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.forecast.app.models.PomodoroSession;

import java.util.List;

@Dao
public interface SessionDao {

    // ── Insert / Update / Delete ───────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSession(PomodoroSession session);

    @Update
    void updateSession(PomodoroSession session);

    @Delete
    void deleteSession(PomodoroSession session);

    // ── Queries – All Sessions ────────────────────────────────────────────────

    @Query("SELECT * FROM pomodoro_sessions ORDER BY startTime DESC")
    LiveData<List<PomodoroSession>> getAllSessions();

    @Query("SELECT * FROM pomodoro_sessions WHERE id = :sessionId")
    PomodoroSession getSessionByIdSync(int sessionId);

    // ── Queries – By Task ─────────────────────────────────────────────────────

    @Query("SELECT * FROM pomodoro_sessions WHERE taskId = :taskId ORDER BY startTime DESC")
    LiveData<List<PomodoroSession>> getSessionsForTask(int taskId);

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE taskId = :taskId AND type = 'FOCUS' AND completed = 1")
    int getCompletedFocusCountForTask(int taskId);

    // ── Queries – Today ───────────────────────────────────────────────────────

    @Query("SELECT * FROM pomodoro_sessions WHERE startTime >= :startOfDay AND startTime <= :endOfDay")
    LiveData<List<PomodoroSession>> getSessionsForDay(long startOfDay, long endOfDay);

    @Query("SELECT * FROM pomodoro_sessions WHERE startTime >= :startOfDay AND startTime <= :endOfDay")
    List<PomodoroSession> getSessionsForDaySync(long startOfDay, long endOfDay);

    // ── Aggregates ────────────────────────────────────────────────────────────

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE type = 'FOCUS' AND startTime >= :startOfDay AND startTime <= :endOfDay")
    int getTotalFocusSessionsForDay(long startOfDay, long endOfDay);

    @Query("SELECT COUNT(*) FROM pomodoro_sessions WHERE type = 'FOCUS' AND completed = 1 AND startTime >= :startOfDay AND startTime <= :endOfDay")
    int getCompletedFocusSessionsForDay(long startOfDay, long endOfDay);

    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM pomodoro_sessions WHERE type = 'FOCUS' AND completed = 1 AND startTime >= :startOfDay AND startTime <= :endOfDay")
    int getTotalFocusMinutesForDay(long startOfDay, long endOfDay);
}
