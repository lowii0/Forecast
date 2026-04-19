package com.forecast.app.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.forecast.app.R;
import com.forecast.app.enums.TimerState;
import com.forecast.app.models.ProductivitySummary;
import com.forecast.app.ui.summary.SummaryViewModel;
import com.forecast.app.ui.tasks.TaskAdapter;
import com.forecast.app.ui.tasks.TasksViewModel;
import com.forecast.app.ui.timer.TimerViewModel;
import com.forecast.app.util.DateTimeUtils;
import com.forecast.app.util.ProductivityCalculator;

public class HomeFragment extends Fragment {

    private TasksViewModel tasksViewModel;
    private SummaryViewModel summaryViewModel;
    private TimerViewModel timerViewModel;
    private TaskAdapter taskAdapter;

    // ── UI references ─────────────────────────────────────────────────────────
    private TextView tvConditionLabel, tvMotivationalMessage, tvConditionEmoji, tvProductivityScore;
    private TextView tvCompletedTasks, tvCompletedSessions, tvTaskCount, tvHomeEmptyTasks;
    private ProgressBar progressScore;
    private RecyclerView recyclerViewTasks;

    // Mini Timer references
    private TextView tvMiniTimerCountdown, tvMiniTimerLabel, tvMiniTimerStatus;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // ── Initialize ViewModels ─────────
        tasksViewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);
        summaryViewModel = new ViewModelProvider(requireActivity()).get(SummaryViewModel.class);
        timerViewModel = new ViewModelProvider(requireActivity()).get(TimerViewModel.class);

        // ── Bind views ─────────
        TextView tvGreeting    = view.findViewById(R.id.tvGreeting);
        TextView tvDate        = view.findViewById(R.id.tvHomeDate);
        View btnGoToTasks      = view.findViewById(R.id.btnGoToTasks);
        View btnGoToTimer      = view.findViewById(R.id.btnGoToTimer);

        tvConditionLabel       = view.findViewById(R.id.tvConditionLabel);
        tvMotivationalMessage  = view.findViewById(R.id.tvMotivationalMessage);
        tvConditionEmoji       = view.findViewById(R.id.tvConditionEmoji);
        tvProductivityScore    = view.findViewById(R.id.tvProductivityScore);
        progressScore          = view.findViewById(R.id.progressScore);
        tvCompletedTasks       = view.findViewById(R.id.tvCompletedTasks);
        tvCompletedSessions    = view.findViewById(R.id.tvCompletedSessions);
        tvTaskCount            = view.findViewById(R.id.tvIncompleteTaskCount);

        recyclerViewTasks      = view.findViewById(R.id.recyclerViewHomeTasks);
        tvHomeEmptyTasks       = view.findViewById(R.id.tvHomeEmptyTasks);

        tvMiniTimerCountdown   = view.findViewById(R.id.tvMiniTimerCountdown);
        tvMiniTimerLabel       = view.findViewById(R.id.tvMiniTimerLabel);
        tvMiniTimerStatus      = view.findViewById(R.id.tvMiniTimerStatus);

        // ── Header ─────────
        tvGreeting.setText(DateTimeUtils.getGreeting());
        tvDate.setText(DateTimeUtils.formatDate(new java.util.Date()));

        // ── RecyclerView ─────────
        taskAdapter = new TaskAdapter();
        recyclerViewTasks.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerViewTasks.setAdapter(taskAdapter);

        taskAdapter.setOnTaskCheckedListener((task, isChecked) -> {
            tasksViewModel.setCompleted(task.getId(), isChecked);
            summaryViewModel.loadTodaySummary();
        });

        taskAdapter.setOnTaskClickListener(task -> {
            Bundle args = new Bundle();
            args.putInt("task_id", task.getId());
            args.putBoolean("is_edit", true);
            Navigation.findNavController(view)
                    .navigate(R.id.action_global_addEditTaskFragment, args); // FIXED
        });

        // ── Observe tasks ─────────
        tasksViewModel.getIncompleteTasks().observe(getViewLifecycleOwner(), tasks -> {
            int count = tasks != null ? tasks.size() : 0;
            tvTaskCount.setText(String.valueOf(count));

            taskAdapter.submitList(tasks);

            if (tasks == null || tasks.isEmpty()) {
                recyclerViewTasks.setVisibility(View.GONE);
                tvHomeEmptyTasks.setVisibility(View.VISIBLE);
            } else {
                recyclerViewTasks.setVisibility(View.VISIBLE);
                tvHomeEmptyTasks.setVisibility(View.GONE);
            }
        });

        // ── Summary observer ─────────
        summaryViewModel.getSummary().observe(getViewLifecycleOwner(), summary -> {
            if (summary != null) {
                bindSummaryData(summary);
            }
        });

        // ── Mini timer observers ─────────
        timerViewModel.getTimeRemaining().observe(getViewLifecycleOwner(), millis -> {
            if (timerViewModel.getTimerState().getValue() != TimerState.IDLE) {
                tvMiniTimerCountdown.setText(timerViewModel.formatTime(millis));
            }
        });

        timerViewModel.getTimerState().observe(getViewLifecycleOwner(), state -> {
            if (state == TimerState.IDLE) {
                tvMiniTimerCountdown.setText("—");
                tvMiniTimerLabel.setText("Focus Timer");
                tvMiniTimerStatus.setText("Tap to open timer");
            } else if (state == TimerState.PAUSED) {
                tvMiniTimerStatus.setText("Paused");
            } else {
                tvMiniTimerStatus.setText("Running...");
            }
        });

        timerViewModel.getCurrentLabel().observe(getViewLifecycleOwner(), label -> {
            if (timerViewModel.getTimerState().getValue() != TimerState.IDLE) {
                tvMiniTimerLabel.setText(label);
            }
        });

        // ── Quick navigation ─────────
        btnGoToTasks.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.tasksFragment));

        btnGoToTimer.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.timerFragment));
    }

    @Override
    public void onResume() {
        super.onResume();
        summaryViewModel.loadTodaySummary();
    }

    private void bindSummaryData(ProductivitySummary summary) {
        tvConditionLabel.setText(ProductivityCalculator.getConditionLabel(summary.getCondition()));
        tvMotivationalMessage.setText(ProductivityCalculator.getMotivationalMessage(summary.getCondition()));

        int scoreInt = (int) summary.getProductivityScore();
        tvProductivityScore.setText(scoreInt + "%");
        progressScore.setProgress(scoreInt);

        tvCompletedTasks.setText(String.valueOf(summary.getCompletedTasks()));
        tvCompletedSessions.setText(String.valueOf(summary.getCompletedFocusSessions()));

        switch (summary.getCondition()) {
            case SUNNY:        tvConditionEmoji.setText("☀️"); break;
            case PARTLY_SUNNY: tvConditionEmoji.setText("⛅"); break;
            case CLOUDY:       tvConditionEmoji.setText("☁️"); break;
            case RAINY:        tvConditionEmoji.setText("🌧️"); break;
            case STORMY:       tvConditionEmoji.setText("⛈️"); break;
        }
    }
}