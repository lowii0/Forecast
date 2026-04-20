package com.forecast.app.ui.summary;

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

import com.forecast.app.R;
import com.forecast.app.models.ProductivitySummary;
import com.forecast.app.util.DateTimeUtils;
import com.forecast.app.util.ProductivityCalculator;

public class SummaryFragment extends Fragment {

    private SummaryViewModel viewModel;

    private TextView tvDate, tvConditionLabel, tvConditionEmoji;
    private TextView tvScore, tvMotivation;
    private TextView tvTotalTasks, tvCompletedTasks, tvTaskRate;
    private TextView tvTotalSessions, tvCompletedSessions, tvFocusMinutes;
    private ProgressBar progressScore;

    // ✅ ADDED: Progress bars for tasks and sessions
    private ProgressBar progressTasks;
    private ProgressBar progressSessions;

    private View loadingLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_summary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SummaryViewModel.class);

        // Bind views
        tvDate             = view.findViewById(R.id.tvSummaryDate);
        tvConditionLabel   = view.findViewById(R.id.tvConditionLabel);
        tvConditionEmoji   = view.findViewById(R.id.tvConditionEmoji);
        tvScore            = view.findViewById(R.id.tvProductivityScore);
        tvMotivation       = view.findViewById(R.id.tvMotivationalMessage);
        tvTotalTasks       = view.findViewById(R.id.tvTotalTasks);
        tvCompletedTasks   = view.findViewById(R.id.tvCompletedTasks);
        tvTaskRate         = view.findViewById(R.id.tvTaskRate);
        tvTotalSessions    = view.findViewById(R.id.tvTotalSessions);
        tvCompletedSessions = view.findViewById(R.id.tvCompletedSessions);
        tvFocusMinutes     = view.findViewById(R.id.tvFocusMinutes);
        progressScore      = view.findViewById(R.id.progressScore);

        // ADDED: Bind new progress bars (make sure IDs match XML)
        progressTasks      = view.findViewById(R.id.progressTasks);
        progressSessions   = view.findViewById(R.id.progressSessions);

        loadingLayout      = view.findViewById(R.id.layoutLoading);

        // Observe
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (loadingLayout != null) {
                loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getSummary().observe(getViewLifecycleOwner(), this::bindSummary);

        // NEW: Observe AI Insight
        viewModel.getAiInsight().observe(getViewLifecycleOwner(), insight -> {
            if (tvMotivation != null) {
                tvMotivation.setText(insight);
            }
        });

        // Load data
        viewModel.loadTodaySummary();

        View btnRefresh = view.findViewById(R.id.btnRefreshSummary);
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> viewModel.refresh());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadTodaySummary();
    }

    private void bindSummary(ProductivitySummary summary) {
        if (summary == null) return;

        tvDate.setText(DateTimeUtils.formatDate(summary.getDate()));
        tvConditionLabel.setText(ProductivityCalculator.getConditionLabel(summary.getCondition()));

        int scoreInt = (int) summary.getProductivityScore();
        tvScore.setText(scoreInt + "%");
        progressScore.setProgress(scoreInt);

        switch (summary.getCondition()) {
            case SUNNY:        tvConditionEmoji.setText("☀️"); break;
            case PARTLY_SUNNY: tvConditionEmoji.setText("⛅"); break;
            case CLOUDY:       tvConditionEmoji.setText("☁️"); break;
            case RAINY:        tvConditionEmoji.setText("🌧️"); break;
            case STORMY:       tvConditionEmoji.setText("⛈️"); break;
        }

        tvTotalTasks.setText("Total tasks: " + summary.getTotalTasks());
        tvCompletedTasks.setText("Completed: " + summary.getCompletedTasks());
        tvTaskRate.setText(String.format("%.0f%% completion", summary.getTaskCompletionRate()));

        tvTotalSessions.setText("Total focus sessions: " + summary.getTotalFocusSessions());
        tvCompletedSessions.setText("Completed: " + summary.getCompletedFocusSessions());
        tvFocusMinutes.setText("Total focus time: " + summary.getTotalFocusMinutes() + " min");

        // Link task progress bar to task data
        int totalTasks = summary.getTotalTasks();
        int completedTasks = summary.getCompletedTasks();

        if (totalTasks > 0 && progressTasks != null) {
            progressTasks.setMax(totalTasks);
            progressTasks.setProgress(completedTasks);
        }

        // Link session progress bar to session data
        int totalSessions = summary.getTotalFocusSessions();
        int completedSessions = summary.getCompletedFocusSessions();

        if (totalSessions > 0 && progressSessions != null) {
            progressSessions.setMax(totalSessions);
            progressSessions.setProgress(completedSessions);
        }
    }
}