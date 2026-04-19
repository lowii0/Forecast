package com.forecast.app.data.repo;

import android.app.Application;
import com.forecast.app.models.PomodoroSession;
import com.forecast.app.models.ProductivitySummary;
import com.forecast.app.models.Task;
import com.forecast.app.util.DateTimeUtils;
import com.forecast.app.util.ProductivityCalculator;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Combines TaskRepository + SessionRepository to build ProductivitySummary objects.
 * All data fetching is done synchronously on a background thread.
 */
public class ProductivityRepository {

    private final TaskRepository taskRepository;
    private final SessionRepository sessionRepository;
    private final ExecutorService executor;

    public ProductivityRepository(Application application) {
        taskRepository = new TaskRepository(application);
        sessionRepository = new SessionRepository(application);
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Builds today's ProductivitySummary and delivers it via callback.
     */
    public void getTodaySummary(SummaryCallback callback) {
        getSummaryForDate(new Date(), callback);
    }

    /**
     * Builds a ProductivitySummary for a specific date and delivers it via callback.
     */
    public void getSummaryForDate(Date date, SummaryCallback callback) {
        executor.execute(() -> {
            long startOfDay = DateTimeUtils.getStartOfDay(date);
            long endOfDay = DateTimeUtils.getEndOfDay(date);

            // Fetch raw data
            List<Task> tasks = taskRepository.getTasksForDaySync(startOfDay, endOfDay);
            List<PomodoroSession> sessions = sessionRepository.getSessionsForDaySync(startOfDay, endOfDay);

            int totalTasks = tasks.size();
            int completedTasks = (int) tasks.stream().filter(Task::isCompleted).count();

            int totalFocusSessions = sessionRepository.getTotalFocusSessionsForDay(startOfDay, endOfDay);
            int completedFocusSessions = sessionRepository.getCompletedFocusSessionsForDay(startOfDay, endOfDay);
            int totalFocusMinutes = sessionRepository.getTotalFocusMinutesForDay(startOfDay, endOfDay);

            // Calculate score and determine condition
            float score = ProductivityCalculator.calculateScore(
                    totalTasks, completedTasks,
                    totalFocusSessions, completedFocusSessions
            );

            ProductivitySummary summary = new ProductivitySummary(
                    date,
                    totalTasks,
                    completedTasks,
                    totalFocusSessions,
                    completedFocusSessions,
                    totalFocusMinutes,
                    score,
                    ProductivityCalculator.getCondition(score)
            );

            if (callback != null) callback.onSummaryReady(summary);
        });
    }

    // ── Callback interface ────────────────────────────────────────────────────

    public interface SummaryCallback {
        void onSummaryReady(ProductivitySummary summary);
    }
}
