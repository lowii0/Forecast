package com.forecast.app.data.repo;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.forecast.app.data.local.AppDatabase;
import com.forecast.app.data.local.TaskDao;
import com.forecast.app.models.Task;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskRepository {

    private final TaskDao taskDao;
    private final ExecutorService executor;

    public TaskRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        taskDao = db.taskDao();
        executor = Executors.newSingleThreadExecutor();
    }

    // ── Write Operations ──────────────────────────────────────────────────────

    public void insertTask(Task task) {
        executor.execute(() -> taskDao.insertTask(task));
    }

    public void insertTaskWithCallback(Task task, InsertCallback callback) {
        executor.execute(() -> {
            long id = taskDao.insertTask(task);
            if (callback != null) callback.onInserted((int) id);
        });
    }

    public void updateTask(Task task) {
        executor.execute(() -> taskDao.updateTask(task));
    }

    public void deleteTask(Task task) {
        executor.execute(() -> taskDao.deleteTask(task));
    }

    public void deleteTaskById(int taskId) {
        executor.execute(() -> taskDao.deleteTaskById(taskId));
    }

    public void setTaskCompleted(int taskId, boolean completed) {
        executor.execute(() -> taskDao.setTaskCompleted(taskId, completed));
    }

    // ── Read Operations (LiveData) ────────────────────────────────────────────

    public LiveData<List<Task>> getAllTasks() {
        return taskDao.getAllTasks();
    }

    public LiveData<Task> getTaskById(int taskId) {
        return taskDao.getTaskById(taskId);
    }

    public LiveData<List<Task>> getIncompleteTasks() {
        return taskDao.getIncompleteTasks();
    }

    public LiveData<List<Task>> getCompletedTasks() {
        return taskDao.getCompletedTasks();
    }

    public LiveData<List<Task>> getTasksByCategory(String category) {
        return taskDao.getTasksByCategory(category);
    }

    public LiveData<List<Task>> getTasksByPriority(String priority) {
        return taskDao.getTasksByPriority(priority);
    }

    public LiveData<List<Task>> getTasksForDay(long startOfDay, long endOfDay) {
        return taskDao.getTasksForDay(startOfDay, endOfDay);
    }

    // ── Sync reads ────────────────────────────────────────────────────────────

    public List<Task> getAllTasksSync() {
        return taskDao.getAllTasksSync();
    }

    public List<Task> getTasksForDaySync(long startOfDay, long endOfDay) {
        return taskDao.getTasksForDaySync(startOfDay, endOfDay);
    }

    public int getTotalTaskCountForDay(long startOfDay, long endOfDay) {
        return taskDao.getTotalTaskCountForDay(startOfDay, endOfDay);
    }

    public int getCompletedTaskCountForDay(long startOfDay, long endOfDay) {
        return taskDao.getCompletedTaskCountForDay(startOfDay, endOfDay);
    }

    // ── NEW (Phase 3): sync single-task fetch for TimerViewModel ─────────────

    /**
     * Fetches a single Task synchronously. Must be called from a background thread.
     * Used by TimerViewModel to check if a linked task is ready to complete.
     */
    public Task getTaskByIdSync(int taskId) {
        return taskDao.getTaskByIdSync(taskId);
    }

    // ── Callback interface ────────────────────────────────────────────────────

    public interface InsertCallback {
        void onInserted(int newId);
    }
}