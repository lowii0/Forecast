package com.forecast.app.ui.timer;

import android.app.Application;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.forecast.app.data.repo.SessionRepository;
import com.forecast.app.enums.TimerState;
import com.forecast.app.models.PomodoroSession;
import com.forecast.app.util.Constants;
import com.forecast.app.util.DateTimeUtils;

import java.util.Date;

public class TimerViewModel extends AndroidViewModel {

    private final SessionRepository sessionRepository;

    // ── LiveData ──────────────────────────────────────────────────────────────

    private final MutableLiveData<Long> timeRemaining = new MutableLiveData<>();
    private final MutableLiveData<TimerState> timerState = new MutableLiveData<>(TimerState.IDLE);
    private final MutableLiveData<Integer> sessionCount = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> sessionCompleted = new MutableLiveData<>(false);
    private final MutableLiveData<String> currentLabel = new MutableLiveData<>("Ready");

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
        timeRemaining.setValue(Constants.FOCUS_DURATION_MS);
        currentDurationMs = Constants.FOCUS_DURATION_MS;
        remainingMs = Constants.FOCUS_DURATION_MS;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public LiveData<Long> getTimeRemaining()    { return timeRemaining; }
    public LiveData<TimerState> getTimerState() { return timerState; }
    public LiveData<Integer> getSessionCount()  { return sessionCount; }
    public LiveData<Boolean> getSessionCompleted() { return sessionCompleted; }
    public LiveData<String> getCurrentLabel()   { return currentLabel; }

    // ── Task Linking ──────────────────────────────────────────────────────────

    public void setLinkedTask(Integer taskId) {
        this.linkedTaskId = taskId;
    }

    public Integer getLinkedTaskId() {
        return linkedTaskId;
    }

    // ── Timer Controls ────────────────────────────────────────────────────────

    /**
     * Starts a new FOCUS session.
     */
    public void startFocus() {
        cancelCurrentTimer();
        currentDurationMs = Constants.FOCUS_DURATION_MS;
        remainingMs = Constants.FOCUS_DURATION_MS;
        timerState.setValue(TimerState.FOCUS);
        currentLabel.setValue("Focus");
        sessionStartTime = new Date();
        isPaused = false;
        sessionCompleted.setValue(false);
        startCountdown(remainingMs);
    }

    /**
     * Starts a SHORT or LONG break based on session count.
     */
    public void startBreak() {
        cancelCurrentTimer();
        int count = sessionCount.getValue() != null ? sessionCount.getValue() : 0;
        boolean isLongBreak = (count % Constants.SESSIONS_BEFORE_LONG_BREAK == 0) && count > 0;

        if (isLongBreak) {
            currentDurationMs = Constants.LONG_BREAK_DURATION_MS;
            remainingMs = Constants.LONG_BREAK_DURATION_MS;
            timerState.setValue(TimerState.LONG_BREAK);
            currentLabel.setValue("Long Break");
        } else {
            currentDurationMs = Constants.SHORT_BREAK_DURATION_MS;
            remainingMs = Constants.SHORT_BREAK_DURATION_MS;
            timerState.setValue(TimerState.SHORT_BREAK);
            currentLabel.setValue("Short Break");
        }

        isPaused = false;
        sessionCompleted.setValue(false);
        startCountdown(remainingMs);
    }

    /**
     * Pauses the current countdown.
     */
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

    /**
     * Resumes from a paused state.
     */
    public void resume() {
        if (timerState.getValue() == TimerState.PAUSED && remainingMs > 0) {
            isPaused = false;
            // Restore the original state label
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

    /**
     * Resets the timer back to IDLE state.
     */
    public void reset() {
        cancelCurrentTimer();
        isPaused = false;
        timerState.setValue(TimerState.IDLE);
        currentLabel.setValue("Ready");
        timeRemaining.setValue(Constants.FOCUS_DURATION_MS);
        remainingMs = Constants.FOCUS_DURATION_MS;
        currentDurationMs = Constants.FOCUS_DURATION_MS;
        sessionCompleted.setValue(false);
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

    /**
     * Called when countdown reaches zero. Saves session if it was a FOCUS session.
     */
    private void onTimerFinished() {
        TimerState state = timerState.getValue();

        if (state == TimerState.FOCUS) {
            // Increment session count
            int count = sessionCount.getValue() != null ? sessionCount.getValue() : 0;
            sessionCount.setValue(count + 1);

            // Save completed session to database
            saveSession(true);

            sessionCompleted.setValue(true);
            currentLabel.setValue("Focus Complete!");
        } else {
            // Break finished
            sessionCompleted.setValue(true);
            currentLabel.setValue("Break Over!");
        }

        timerState.setValue(TimerState.IDLE);
    }

    /**
     * Saves a PomodoroSession to the database.
     * @param completed whether the session was fully completed
     */
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
     * Manually save a partial (incomplete) session if the user quits mid-session.
     * Call this from the Fragment's onDestroyView or onStop if needed.
     */
    public void saveIncompleteSessionIfRunning() {
        TimerState state = timerState.getValue();
        if (state == TimerState.FOCUS || state == TimerState.PAUSED) {
            if (sessionStartTime != null && remainingMs < currentDurationMs) {
                saveSession(false);
            }
        }
    }

    // ── Formatting helper for UI ──────────────────────────────────────────────

    /**
     * Formats the given millisecond value into MM:SS for display.
     */
    public String formatTime(long millis) {
        return DateTimeUtils.formatCountdown(millis);
    }

    /**
     * Returns progress as a float from 0.0 (empty) to 1.0 (full) for a progress bar.
     */
    public float getProgress(long remainingMs) {
        if (currentDurationMs == 0) return 0f;
        return (float) remainingMs / currentDurationMs;
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    @Override
    protected void onCleared() {
        super.onCleared();
        cancelCurrentTimer();
    }
}
