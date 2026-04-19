package com.forecast.app.data.repo;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.forecast.app.data.local.AppDatabase;
import com.forecast.app.data.local.SessionDao;
import com.forecast.app.models.PomodoroSession;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SessionRepository {

    private final SessionDao sessionDao;
    private final ExecutorService executor;

    public SessionRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        sessionDao = db.sessionDao();
        executor = Executors.newSingleThreadExecutor();
    }

    // ── Write Operations ──────────────────────────────────────────────────────

    public void insertSession(PomodoroSession session) {
        executor.execute(() -> sessionDao.insertSession(session));
    }

    public void insertSessionWithCallback(PomodoroSession session, InsertCallback callback) {
        executor.execute(() -> {
            long id = sessionDao.insertSession(session);
            if (callback != null) callback.onInserted((int) id);
        });
    }

    public void updateSession(PomodoroSession session) {
        executor.execute(() -> sessionDao.updateSession(session));
    }

    public void deleteSession(PomodoroSession session) {
        executor.execute(() -> sessionDao.deleteSession(session));
    }

    // ── Read Operations ───────────────────────────────────────────────────────

    public LiveData<List<PomodoroSession>> getAllSessions() {
        return sessionDao.getAllSessions();
    }

    public LiveData<List<PomodoroSession>> getSessionsForTask(int taskId) {
        return sessionDao.getSessionsForTask(taskId);
    }

    public LiveData<List<PomodoroSession>> getSessionsForDay(long startOfDay, long endOfDay) {
        return sessionDao.getSessionsForDay(startOfDay, endOfDay);
    }

    // ── Sync reads ────────────────────────────────────────────────────────────

    public List<PomodoroSession> getSessionsForDaySync(long startOfDay, long endOfDay) {
        return sessionDao.getSessionsForDaySync(startOfDay, endOfDay);
    }

    public int getTotalFocusSessionsForDay(long startOfDay, long endOfDay) {
        return sessionDao.getTotalFocusSessionsForDay(startOfDay, endOfDay);
    }

    public int getCompletedFocusSessionsForDay(long startOfDay, long endOfDay) {
        return sessionDao.getCompletedFocusSessionsForDay(startOfDay, endOfDay);
    }

    public int getTotalFocusMinutesForDay(long startOfDay, long endOfDay) {
        return sessionDao.getTotalFocusMinutesForDay(startOfDay, endOfDay);
    }

    public int getCompletedFocusCountForTask(int taskId) {
        return sessionDao.getCompletedFocusCountForTask(taskId);
    }

    // ── Sync single fetch ─────────────────────────────────────────────────────

    public PomodoroSession getSessionByIdSync(int sessionId) {
        return sessionDao.getSessionByIdSync(sessionId);
    }

    // ── Callback interface ────────────────────────────────────────────────────

    public interface InsertCallback {
        void onInserted(int newId);
    }
}
