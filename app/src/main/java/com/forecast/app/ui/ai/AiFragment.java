package com.forecast.app.ui.ai;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.forecast.app.R;
import com.forecast.app.data.repo.GeminiRepository;
import com.forecast.app.data.repo.TaskRepository;
import com.forecast.app.models.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AiFragment extends Fragment {

    private Button btnAnalyze;
    private ProgressBar progressAi;
    private LinearLayout layoutAiInsight;
    private LinearLayout layoutAiReasoning;
    private LinearLayout layoutAiTasks;
    private TextView tvAiInsight;
    private TextView tvAiReasoning;
    private TextView tvTaskStatus;
    private TextView tvSuggestedOrderHeader;

    private GeminiRepository geminiRepository;
    private TaskRepository taskRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai, container, false);

        btnAnalyze = view.findViewById(R.id.btnAnalyze);
        progressAi = view.findViewById(R.id.progressAi);
        layoutAiInsight = view.findViewById(R.id.layoutAiInsight);
        layoutAiReasoning = view.findViewById(R.id.layoutAiReasoning);
        layoutAiTasks = view.findViewById(R.id.layoutAiTasks);
        tvAiInsight = view.findViewById(R.id.tvAiInsight);
        tvAiReasoning = view.findViewById(R.id.tvAiReasoning);
        tvTaskStatus = view.findViewById(R.id.tvTaskStatus);
        tvSuggestedOrderHeader = view.findViewById(R.id.tvSuggestedOrderHeader);

        geminiRepository = new GeminiRepository();
        taskRepository = new TaskRepository(requireActivity().getApplication());

        // Disable button until tasks are loaded
        btnAnalyze.setEnabled(false);
        tvTaskStatus.setText("Loading your tasks...");
        tvTaskStatus.setVisibility(View.VISIBLE);

        loadTasksFromDatabase();

        btnAnalyze.setOnClickListener(v -> analyzeTasks());

        return view;
    }

    /**
     * Fetches real tasks from Room DB on a background thread,
     * then updates the UI on the main thread.
     */
    private void loadTasksFromDatabase() {
        executor.execute(() -> {
            // getAllTasksSync() runs on this background thread — safe to call
            List<Task> dbTasks = taskRepository.getAllTasksSync();

            // Convert Task → GeminiRepository.TaskItem, skip completed ones
            List<GeminiRepository.TaskItem> geminiTasks = new ArrayList<>();
            for (Task t : dbTasks) {
                if (!t.isCompleted()) {
                    // Format the due date — adjust if getDueDate() returns a long timestamp
                    String dueDateStr = "";
                    if (t.getDueDate() != null) {
                        dueDateStr = new java.text.SimpleDateFormat(
                                "MMM d, yyyy", java.util.Locale.getDefault()
                        ).format(t.getDueDate());
                    }

                    geminiTasks.add(new GeminiRepository.TaskItem(
                            String.valueOf(t.getId()),
                            t.getTitle(),
                            t.getDescription() != null ? t.getDescription() : "",
                            dueDateStr
                    ));
                }
            }

            // Back to main thread to update UI
            requireActivity().runOnUiThread(() -> updateTaskStatus(geminiTasks));
        });
    }

    private List<GeminiRepository.TaskItem> currentTasks = new ArrayList<>();

    private void updateTaskStatus(List<GeminiRepository.TaskItem> tasks) {
        currentTasks = tasks;

        if (currentTasks.isEmpty()) {
            tvTaskStatus.setText("No incomplete tasks found. Add some tasks first!");
            tvTaskStatus.setVisibility(View.VISIBLE);
            btnAnalyze.setEnabled(false);
        } else {
            tvTaskStatus.setText("Found " + currentTasks.size() + " task(s) ready to prioritize");
            tvTaskStatus.setVisibility(View.VISIBLE);
            btnAnalyze.setEnabled(true);
        }
    }

    private void analyzeTasks() {
        if (currentTasks.isEmpty()) {
            Toast.makeText(requireContext(), "No tasks to analyze", Toast.LENGTH_SHORT).show();
            return;
        }

        btnAnalyze.setEnabled(false);
        progressAi.setVisibility(View.VISIBLE);
        layoutAiInsight.setVisibility(View.GONE);
        layoutAiReasoning.setVisibility(View.GONE);
        layoutAiTasks.removeAllViews();
        tvSuggestedOrderHeader.setVisibility(View.GONE);

        geminiRepository.prioritizeTasks(currentTasks, new GeminiRepository.PrioritizeCallback() {
            @Override
            public void onSuccess(String insight, String reasoning,
                                  List<GeminiRepository.TaskItem> prioritizedTasks) {
                requireActivity().runOnUiThread(() -> {
                    progressAi.setVisibility(View.GONE);
                    btnAnalyze.setEnabled(true);

                    if (insight != null && !insight.isEmpty()) {
                        tvAiInsight.setText(insight);
                        layoutAiInsight.setVisibility(View.VISIBLE);
                    }

                    if (reasoning != null && !reasoning.isEmpty()) {
                        tvAiReasoning.setText(reasoning);
                        layoutAiReasoning.setVisibility(View.VISIBLE);
                    }

                    displayPrioritizedTasks(prioritizedTasks);
                    tvSuggestedOrderHeader.setVisibility(View.VISIBLE);
                });
            }

            @Override
            public void onError(String error) {
                requireActivity().runOnUiThread(() -> {
                    progressAi.setVisibility(View.GONE);
                    btnAnalyze.setEnabled(true);
                    tvAiInsight.setText("AI analysis failed. Please try again.");
                    layoutAiInsight.setVisibility(View.VISIBLE);
                    Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void displayPrioritizedTasks(List<GeminiRepository.TaskItem> tasks) {
        layoutAiTasks.removeAllViews();
        for (int i = 0; i < tasks.size(); i++) {
            layoutAiTasks.addView(createTaskCard(tasks.get(i), i + 1));
        }
    }

    private View createTaskCard(GeminiRepository.TaskItem task, int position) {
        CardView cardView = new CardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 12);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(12);
        cardView.setCardElevation(2);
        cardView.setUseCompatPadding(true);

        LinearLayout mainLayout = new LinearLayout(requireContext());
        mainLayout.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.setPadding(16, 16, 16, 16);

        TextView priorityBadge = new TextView(requireContext());
        LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(40, 40);
        badgeParams.setMargins(0, 4, 16, 0);
        priorityBadge.setLayoutParams(badgeParams);
        priorityBadge.setText(String.valueOf(position));
        priorityBadge.setTextSize(14);
        priorityBadge.setTextColor(Color.WHITE);
        priorityBadge.setGravity(android.view.Gravity.CENTER);
        priorityBadge.setTypeface(null, android.graphics.Typeface.BOLD);
        priorityBadge.setBackgroundResource(R.drawable.bg_priority_badge);
        int badgeColor = position <= 2 ? Color.parseColor("#EF4444")
                : position <= 4 ? Color.parseColor("#F59E0B")
                : Color.parseColor("#10B981");
        priorityBadge.getBackground().setTint(badgeColor);

        LinearLayout contentLayout = new LinearLayout(requireContext());
        contentLayout.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        contentLayout.setOrientation(LinearLayout.VERTICAL);

        TextView titleView = new TextView(requireContext());
        titleView.setText(task.getTitle());
        titleView.setTextSize(14);
        titleView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_primary));
        titleView.setTypeface(null, android.graphics.Typeface.BOLD);
        contentLayout.addView(titleView);

        if (task.getDescription() != null && !task.getDescription().isEmpty()) {
            TextView descView = new TextView(requireContext());
            LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            descParams.setMargins(0, 4, 0, 0);
            descView.setLayoutParams(descParams);
            descView.setText(task.getDescription());
            descView.setTextSize(12);
            descView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            contentLayout.addView(descView);
        }

        if (task.getDueDate() != null && !task.getDueDate().isEmpty()) {
            TextView dueView = new TextView(requireContext());
            LinearLayout.LayoutParams dueParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            dueParams.setMargins(0, 8, 0, 0);
            dueView.setLayoutParams(dueParams);
            dueView.setText("📅 " + task.getDueDate());
            dueView.setTextSize(11);
            dueView.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
            contentLayout.addView(dueView);
        }

        mainLayout.addView(priorityBadge);
        mainLayout.addView(contentLayout);
        cardView.addView(mainLayout);
        return cardView;
    }
}