package com.forecast.app.ui.timer;

import android.app.Application;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.forecast.app.data.repo.SessionRepository;
import com.forecast.app.data.repo.TaskRepository;
import com.forecast.app.enums.TimerState;
import com.forecast.app.models.PomodoroSession;
import com.forecast.app.models.Task;
import com.forecast.app.util.Constants;
import com.forecast.app.util.DateTimeUtils;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TimerViewModel extends AndroidViewModel {

    private final SessionRepository sessionRepository;
    private final TaskRepository taskRepository;        // NEW
    private final ExecutorService executor;             // NEW
    private final Handler mainHandler;                  // NEW

    // ── LiveData ──────────────────────────────────────────────────────────────

    private final MutableLiveData<Long> timeRemaining = new MutableLiveData<>();
    private final MutableLiveData<TimerState> timerState = new MutableLiveData<>(TimerState.IDLE);
    private final MutableLiveData<Integer> sessionCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> sessionCompleted = new MutableLiveData<>(false);
    private final MutableLiveData<String> currentLabel = new MutableLiveData<>("Ready");

    // NEW (Phase 3): emits a Task when its session quota is met
    private final MutableLiveData<Task> taskCompletionSuggestion = new MutableLiveData<>(null);

    // ── Internal state ────────────────────────────────────────────────────────

    private CountDownTimer countDownTimer;
    private long currentDurationMs;
    private long remainingMs;
    private Integer linkedTaskId = null;
    private Date sessionStartTime;
    private boolean isPaused = false;

    // ── Constructor ───────────────────────────────────────────────────────────

    public TimerViewModel(@NonNull Application application) {
        super(application);
        sessionRepository = new SessionRepository(application);
        taskRepository    = new TaskRepository(application);   // NEW
        executor          = Executors.newSingleThreadExecutor(); // NEW
        mainHandler       = new Handler(Looper.getMainLooper()); // NEW

        timeRemaining.setValue(Constants.FOCUS_DURATION_MS);
        currentDurationMs = Constants.FOCUS_DURATION_MS;
        remainingMs       = Constants.FOCUS_DURATION_MS;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public LiveData<Long> getTimeRemaining()              { return timeRemaining; }
    public LiveData<TimerState> getTimerState()           { return timerState; }
    public LiveData<Integer> getSessionCount()            { return sessionCount; }
    public LiveData<Boolean> getSessionCompleted()        { return sessionCompleted; }
    public LiveData<String> getCurrentLabel()             { return currentLabel; }
    public LiveData<Task> getTaskCompletionSuggestion()   { return taskCompletionSuggestion; } // NEW

    // ── Task Linking ──────────────────────────────────────────────────────────

    public void setLinkedTask(Integer taskId) {
        this.linkedTaskId = taskId;
    }

    public Integer getLinkedTaskId() {
        return linkedTaskId;
    }

    // ── Timer Controls ────────────────────────────────────────────────────────

    public void startFocus() {
        cancelCurrentTimer();
        currentDurationMs = Constants.FOCUS_DURATION_MS;
        remainingMs       = Constants.FOCUS_DURATION_MS;
        timerState.setValue(TimerState.FOCUS);
        currentLabel.setValue("Focus");
        sessionStartTime = new Date();
        isPaused = false;
        sessionCompleted.setValue(false);
        startCountdown(remainingMs);
    }

    public void startBreak() {
        cancelCurrentTimer();
        int count = sessionCount.getValue() != null ? sessionCount.getValue() : 0;
        boolean isLongBreak = (count % Constants.SESSIONS_BEFORE_LONG_BREAK == 0) && count > 0;

        if (isLongBreak) {
            currentDurationMs = Constants.LONG_BREAK_DURATION_MS;
            remainingMs       = Constants.LONG_BREAK_DURATION_MS;
            timerState.setValue(TimerState.LONG_BREAK);
            currentLabel.setValue("Long Break");
        } else {
            currentDurationMs = Constants.SHORT_BREAK_DURATION_MS;
            remainingMs       = Constants.SHORT_BREAK_DURATION_MS;
            timerState.setValue(TimerState.SHORT_BREAK);
            currentLabel.setValue("Short Break");
        }

        isPaused = false;
        sessionCompleted.setValue(false);
        startCountdown(remainingMs);
    }

    public void pause() {
        if (timerState.getValue() == TimerState.FOCUS
                || timerState.getValue() == TimerState.SHORT_BREAK
                || timerState.getValue() == TimerState.LONG_BREAK) {
            cancelCurrentTimer();
            isPaused = true;
            timerState.setValue(TimerState.PAUSED);
            currentLabel.setValue("Paused");
        }
    }

    public void resume() {
        if (timerState.getValue() == TimerState.PAUSED && remainingMs > 0) {
            isPaused = false;
            if (currentDurationMs == Constants.FOCUS_DURATION_MS) {
                timerState.setValue(TimerState.FOCUS);
                currentLabel.setValue("Focus");
            } else if (currentDurationMs == Constants.LONG_BREAK_DURATION_MS) {
                timerState.setValue(TimerState.LONG_BREAK);
                currentLabel.setValue("Long Break");
            } else {
                timerState.setValue(TimerState.SHORT_BREAK);
                currentLabel.setValue("Short Break");
            }
            startCountdown(remainingMs);
        }
    }

    public void reset() {
        cancelCurrentTimer();
        isPaused = false;
        timerState.setValue(TimerState.IDLE);
        currentLabel.setValue("Ready");
        timeRemaining.setValue(Constants.FOCUS_DURATION_MS);
        remainingMs       = Constants.FOCUS_DURATION_MS;
        currentDurationMs = Constants.FOCUS_DURATION_MS;
        sessionCompleted.setValue(false);
    }

    // ── NEW (Phase 3): clear the suggestion after the dialog is handled ───────

    public void clearTaskCompletionSuggestion() {
        taskCompletionSuggestion.setValue(null);
    }

    // ── Internal countdown logic ──────────────────────────────────────────────

    private void startCountdown(long durationMs) {
        countDownTimer = new CountDownTimer(durationMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingMs = millisUntilFinished;
                timeRemaining.setValue(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                remainingMs = 0;
                timeRemaining.setValue(0L);
                onTimerFinished();
            }
        }.start();
    }

    private void cancelCurrentTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    private void onTimerFinished() {
        TimerState state = timerState.getValue();

        if (state == TimerState.FOCUS) {
            int count = sessionCount.getValue() != null ? sessionCount.getValue() : 0;
            sessionCount.setValue(count + 1);

            saveSession(true);

            // NEW (Phase 3): check if the linked task is now fully done
            checkIfLinkedTaskIsComplete();

            sessionCompleted.setValue(true);
            currentLabel.setValue("Focus Complete!");
        } else {
            sessionCompleted.setValue(true);
            currentLabel.setValue("Break Over!");
        }

        timerState.setValue(TimerState.IDLE);
    }

    private void saveSession(boolean completed) {
        TimerState state = timerState.getValue();
        if (state == null) return;

        int durationMin = (int) (currentDurationMs / 60000);
        PomodoroSession session = new PomodoroSession(linkedTaskId, state, durationMin);
        session.setStartTime(sessionStartTime != null ? sessionStartTime : new Date());
        session.setEndTime(new Date());
        session.setCompleted(completed);

        sessionRepository.insertSession(session);
    }

    /**
     * NEW (Phase 3):
     * Runs in background. Checks if the linked task's completed focus sessions
     * have now reached (or exceeded) its estimatedPomodoros goal.
     * If yes, posts the Task to taskCompletionSuggestion so the Fragment can
     * show a "Did you finish this task?" dialog.
     */
    private void checkIfLinkedTaskIsComplete() {
        if (linkedTaskId == null) return;

        executor.execute(() -> {
            Task task = taskRepository.getTaskByIdSync(linkedTaskId);
            if (task == null || task.isCompleted()) return;

            int completedSessions = sessionRepository.getCompletedFocusCountForTask(linkedTaskId);
            if (completedSessions >= task.getEstimatedPomodoros()) {
                mainHandler.post(() -> taskCompletionSuggestion.setValue(task));
            }
        });
    }

    public void saveIncompleteSessionIfRunning() {
        TimerState state = timerState.getValue();
        if (state == TimerState.FOCUS || state == TimerState.PAUSED) {
            if (sessionStartTime != null && remainingMs < currentDurationMs) {
                saveSession(false);
            }
        }
    }

    // ── Formatting ────────────────────────────────────────────────────────────

    public String formatTime(long millis) {
        return DateTimeUtils.formatCountdown(millis);
    }

    public float getProgress(long remainingMs) {
        if (currentDurationMs == 0) return 0f;
        return (float) remainingMs / currentDurationMs;
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelCurrentTimer();
        executor.shutdownNow();
    }
}