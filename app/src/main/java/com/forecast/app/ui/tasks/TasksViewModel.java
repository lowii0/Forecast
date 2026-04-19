package com.forecast.app.ui.tasks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.forecast.app.data.repo.TaskRepository;
import com.forecast.app.enums.Category;
import com.forecast.app.enums.Priority;
import com.forecast.app.models.Task;

import java.util.List;

public class TasksViewModel extends AndroidViewModel {

    private final TaskRepository repository;

    // ── LiveData streams ──────────────────────────────────────────────────────

    private final LiveData<List<Task>> allTasks;
    private final LiveData<List<Task>> incompleteTasks;
    private final LiveData<List<Task>> completedTasks;

    // Active filter state
    private final MutableLiveData<String> categoryFilter = new MutableLiveData<>(null);
    private final MutableLiveData<String> priorityFilter = new MutableLiveData<>(null);

    // Filtered tasks (switches based on active filter)
    private final MutableLiveData<LiveData<List<Task>>> filteredTasksSource = new MutableLiveData<>();
    private final LiveData<List<Task>> filteredTasks;

    // Single task for edit screen
    private final MutableLiveData<Task> selectedTask = new MutableLiveData<>();

    // ── Constructor ───────────────────────────────────────────────────────────

    public TasksViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        allTasks = repository.getAllTasks();
        incompleteTasks = repository.getIncompleteTasks();
        completedTasks = repository.getCompletedTasks();

        // Default: show all tasks
        filteredTasksSource.setValue(allTasks);
        filteredTasks = Transformations.switchMap(filteredTasksSource, source -> source);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public LiveData<List<Task>> getAllTasks()       { return allTasks; }
    public LiveData<List<Task>> getIncompleteTasks() { return incompleteTasks; }
    public LiveData<List<Task>> getCompletedTasks() { return completedTasks; }
    public LiveData<List<Task>> getFilteredTasks()  { return filteredTasks; }
    public LiveData<Task> getSelectedTask()         { return selectedTask; }

    // ── CRUD Operations ───────────────────────────────────────────────────────

    public void insert(Task task) {
        repository.insertTask(task);
    }

    public void insertWithCallback(Task task, TaskRepository.InsertCallback callback) {
        repository.insertTaskWithCallback(task, callback);
    }

    public void update(Task task) {
        repository.updateTask(task);
    }

    public void delete(Task task) {
        repository.deleteTask(task);
    }

    public void deleteById(int taskId) {
        repository.deleteTaskById(taskId);
    }

    public void setCompleted(int taskId, boolean completed) {
        repository.setTaskCompleted(taskId, completed);
    }

    // ── Task Loading for Edit ─────────────────────────────────────────────────

    /**
     * Load a task by ID to populate the edit form.
     * Observe getSelectedTask() in the fragment.
     */
    public void loadTaskForEdit(int taskId) {
        repository.getTaskById(taskId).observeForever(task -> {
            if (task != null) selectedTask.setValue(task);
        });
    }

    // ── Filtering ─────────────────────────────────────────────────────────────

    public void filterByCategory(Category category) {
        if (category == null) {
            filteredTasksSource.setValue(allTasks);
        } else {
            filteredTasksSource.setValue(
                repository.getTasksByCategory(category.name())
            );
        }
    }

    public void filterByPriority(Priority priority) {
        if (priority == null) {
            filteredTasksSource.setValue(allTasks);
        } else {
            filteredTasksSource.setValue(
                repository.getTasksByPriority(priority.name())
            );
        }
    }

    public void showAllTasks() {
        filteredTasksSource.setValue(allTasks);
    }

    public void showIncompleteTasks() {
        filteredTasksSource.setValue(incompleteTasks);
    }

    public void showCompletedTasks() {
        filteredTasksSource.setValue(completedTasks);
    }
}
