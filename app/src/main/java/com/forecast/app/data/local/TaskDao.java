package com.forecast.app.data.local;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.forecast.app.models.Task;

import java.util.List;

@Dao
public interface TaskDao {

    // ── Insert / Update / Delete ───────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertTask(Task task);

    @Update
    void updateTask(Task task);

    @Delete
    void deleteTask(Task task);

    @Query("DELETE FROM tasks WHERE id = :taskId")
    void deleteTaskById(int taskId);

    // ── Queries – All Tasks ───────────────────────────────────────────────────

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    LiveData<List<Task>> getAllTasks();

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    List<Task> getAllTasksSync();

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    LiveData<Task> getTaskById(int taskId);

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    Task getTaskByIdSync(int taskId);

    // ── Queries – By Status ───────────────────────────────────────────────────

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY priority DESC, createdAt ASC")
    LiveData<List<Task>> getIncompleteTasks();

    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY createdAt DESC")
    LiveData<List<Task>> getCompletedTasks();

    // ── Queries – By Category / Priority ─────────────────────────────────────

    @Query("SELECT * FROM tasks WHERE category = :category ORDER BY priority DESC")
    LiveData<List<Task>> getTasksByCategory(String category);

    @Query("SELECT * FROM tasks WHERE priority = :priority ORDER BY createdAt DESC")
    LiveData<List<Task>> getTasksByPriority(String priority);

    // ── Queries – Today's Tasks ───────────────────────────────────────────────

    @Query("SELECT * FROM tasks WHERE createdAt >= :startOfDay AND createdAt <= :endOfDay")
    LiveData<List<Task>> getTasksForDay(long startOfDay, long endOfDay);

    @Query("SELECT * FROM tasks WHERE createdAt >= :startOfDay AND createdAt <= :endOfDay")
    List<Task> getTasksForDaySync(long startOfDay, long endOfDay);

    // ── Counts ────────────────────────────────────────────────────────────────

    @Query("SELECT COUNT(*) FROM tasks WHERE createdAt >= :startOfDay AND createdAt <= :endOfDay")
    int getTotalTaskCountForDay(long startOfDay, long endOfDay);

    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1 AND createdAt >= :startOfDay AND createdAt <= :endOfDay")
    int getCompletedTaskCountForDay(long startOfDay, long endOfDay);

    // ── Update Completion ─────────────────────────────────────────────────────

    @Query("UPDATE tasks SET isCompleted = :completed WHERE id = :taskId")
    void setTaskCompleted(int taskId, boolean completed);
}
