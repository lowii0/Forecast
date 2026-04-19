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

    // ── UI references ─────────────────────────────────────────────────────────
    private TextView tvDate, tvConditionLabel, tvConditionEmoji;
    private TextView tvScore, tvMotivation;
    private TextView tvTotalTasks, tvCompletedTasks, tvTaskRate;
    private TextView tvTotalSessions, tvCompletedSessions, tvFocusMinutes;
    private ProgressBar progressScore;
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

        // ── Bind views ────────────────────────────────────────────────────────
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
        loadingLayout      = view.findViewById(R.id.layoutLoading);

        // ── Observe ───────────────────────────────────────────────────────────

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (loadingLayout != null) {
                loadingLayout.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getSummary().observe(getViewLifecycleOwner(), this::bindSummary);

        // ── Load data ─────────────────────────────────────────────────────────
        viewModel.loadTodaySummary();

        // Refresh button (optional)
        View btnRefresh = view.findViewById(R.id.btnRefreshSummary);
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> viewModel.refresh());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Always refresh when screen becomes visible
        viewModel.loadTodaySummary();
    }

    private void bindSummary(ProductivitySummary summary) {
        if (summary == null) return;

        // Date
        tvDate.setText(DateTimeUtils.formatDate(summary.getDate()));

        // Condition
        tvConditionLabel.setText(
            ProductivityCalculator.getConditionLabel(summary.getCondition()));
        tvMotivation.setText(
            ProductivityCalculator.getMotivationalMessage(summary.getCondition()));

        // Score
        int scoreInt = (int) summary.getProductivityScore();
        tvScore.setText(scoreInt + "%");
        progressScore.setProgress(scoreInt);

        // Condition emoji
        switch (summary.getCondition()) {
            case SUNNY:        tvConditionEmoji.setText("☀️"); break;
            case PARTLY_SUNNY: tvConditionEmoji.setText("⛅"); break;
            case CLOUDY:       tvConditionEmoji.setText("☁️"); break;
            case RAINY:        tvConditionEmoji.setText("🌧️"); break;
            case STORMY:       tvConditionEmoji.setText("⛈️"); break;
        }

        // Task stats
        tvTotalTasks.setText("Total tasks: " + summary.getTotalTasks());
        tvCompletedTasks.setText("Completed: " + summary.getCompletedTasks());
        tvTaskRate.setText(String.format("%.0f%% completion", summary.getTaskCompletionRate()));

        // Session stats
        tvTotalSessions.setText("Total focus sessions: " + summary.getTotalFocusSessions());
        tvCompletedSessions.setText("Completed: " + summary.getCompletedFocusSessions());
        tvFocusMinutes.setText("Total focus time: " + summary.getTotalFocusMinutes() + " min");
    }
}
