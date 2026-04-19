package com.forecast.app.ui.ai;

import android.content.Context;
import android.content.SharedPreferences;
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
import androidx.fragment.app.Fragment;

import com.forecast.app.R;
import com.forecast.app.data.repo.GeminiRepository;

public class AiFragment extends Fragment {

    private Button btnAnalyze;
    private ProgressBar progressAi;
    private LinearLayout layoutAiReasoning;
    private TextView tvAiReasoning;
    private GeminiRepository geminiRepository;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ai, container, false);

        // Initialize Views
        btnAnalyze = view.findViewById(R.id.btnAnalyze);
        progressAi = view.findViewById(R.id.progressAi);
        layoutAiReasoning = view.findViewById(R.id.layoutAiReasoning);
        tvAiReasoning = view.findViewById(R.id.tvAiReasoning);

        // Initialize Repository
        geminiRepository = new GeminiRepository();

        // Set Click Listener
        btnAnalyze.setOnClickListener(v -> analyzeProductivity());

        return view;
    }

    private void analyzeProductivity() {
        // UI Updates: Show loading, hide previous results
        btnAnalyze.setEnabled(false);
        progressAi.setVisibility(View.VISIBLE);
        layoutAiReasoning.setVisibility(View.GONE);

        // 1. Read from SharedPreferences
        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("ForecastAppPrefs", Context.MODE_PRIVATE);

        int tasksDone = prefs.getInt("TASKS_DONE", 0);
        int sessionsDone = prefs.getInt("SESSIONS_DONE", 0);
        String condition = "productive";

        // 2. Safety fallback for fresh installs / demo
        if (tasksDone == 0 && sessionsDone == 0) {
            tasksDone = 4;
            sessionsDone = 2;
        }

        // 3. Send to Gemini
        geminiRepository.generateProductivityInsight(
                tasksDone,
                sessionsDone,
                condition,
                new GeminiRepository.InsightCallback() {
                    @Override
                    public void onSuccess(String insight) {
                        requireActivity().runOnUiThread(() -> {
                            progressAi.setVisibility(View.GONE);
                            layoutAiReasoning.setVisibility(View.VISIBLE);
                            tvAiReasoning.setText(insight);
                            btnAnalyze.setEnabled(true);
                        });
                    }

                    @Override
                    public void onError(String error) {
                        requireActivity().runOnUiThread(() -> {
                            progressAi.setVisibility(View.GONE);
                            btnAnalyze.setEnabled(true);
                            tvAiReasoning.setText("Oops, AI is sleeping: " + error);
                            layoutAiReasoning.setVisibility(View.VISIBLE);
                        });
                    }
                }
        );
    }
}