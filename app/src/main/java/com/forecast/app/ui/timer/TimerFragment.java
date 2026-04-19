package com.forecast.app.ui.timer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.forecast.app.R;
import com.forecast.app.enums.TimerState;
import com.forecast.app.models.Task;
import com.forecast.app.ui.tasks.TasksViewModel;

import java.util.ArrayList;
import java.util.List;

public class TimerFragment extends Fragment {

    private TimerViewModel timerViewModel;
    private TasksViewModel tasksViewModel;

    // ── UI references ─────────────────────────────────────────────────────────
    private TextView tvCountdown, tvStateLabel, tvSessionCount, tvLinkedTask;
    private Button btnStart, btnPause, btnResume, btnReset, btnBreak;
    private Spinner spinnerTaskSelector;

    // ── State ─────────────────────────────────────────────────────────────────
    private List<Task> taskList = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_timer, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        timerViewModel = new ViewModelProvider(requireActivity()).get(TimerViewModel.class);
        tasksViewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);

        // ── Bind views ────────────────────────────────────────────────────────
        tvCountdown      = view.findViewById(R.id.tvCountdown);
        tvStateLabel     = view.findViewById(R.id.tvTimerStateLabel);
        tvSessionCount   = view.findViewById(R.id.tvSessionCount);
        tvLinkedTask     = view.findViewById(R.id.tvLinkedTask);
        btnStart         = view.findViewById(R.id.btnStartFocus);
        btnPause         = view.findViewById(R.id.btnPause);
        btnResume        = view.findViewById(R.id.btnResume);
        btnReset         = view.findViewById(R.id.btnReset);
        btnBreak         = view.findViewById(R.id.btnStartBreak);
        spinnerTaskSelector = view.findViewById(R.id.spinnerTaskSelector);

        // ── Button listeners ──────────────────────────────────────────────────
        btnStart.setOnClickListener(v -> timerViewModel.startFocus());
        btnPause.setOnClickListener(v -> timerViewModel.pause());
        btnResume.setOnClickListener(v -> timerViewModel.resume());
        btnReset.setOnClickListener(v -> timerViewModel.reset());
        btnBreak.setOnClickListener(v -> timerViewModel.startBreak());

        // ── Task selector ─────────────────────────────────────────────────────
        tasksViewModel.getIncompleteTasks().observe(getViewLifecycleOwner(), tasks -> {
            taskList.clear();
            // Add a "No task" placeholder
            Task placeholder = new Task();
            placeholder.setId(-1);
            placeholder.setTitle("No task selected");
            taskList.add(placeholder);
            if (tasks != null) taskList.addAll(tasks);

            List<String> taskTitles = new ArrayList<>();
            for (Task t : taskList) taskTitles.add(t.getTitle());

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, taskTitles);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerTaskSelector.setAdapter(adapter);
        });

        spinnerTaskSelector.setOnItemSelectedListener(
            new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent,
                                           View v, int position, long id) {
                    if (position < taskList.size()) {
                        Task selected = taskList.get(position);
                        if (selected.getId() == -1) {
                            timerViewModel.setLinkedTask(null);
                            tvLinkedTask.setText("No task linked");
                        } else {
                            timerViewModel.setLinkedTask(selected.getId());
                            tvLinkedTask.setText("Linked: " + selected.getTitle());
                        }
                    }
                }

                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            }
        );

        // ── Observe ViewModel ─────────────────────────────────────────────────

        timerViewModel.getTimeRemaining().observe(getViewLifecycleOwner(), millis -> {
            tvCountdown.setText(timerViewModel.formatTime(millis));
        });

        timerViewModel.getTimerState().observe(getViewLifecycleOwner(), state -> {
            updateButtonVisibility(state);
        });

        timerViewModel.getSessionCount().observe(getViewLifecycleOwner(), count -> {
            tvSessionCount.setText("Sessions today: " + count);
        });

        timerViewModel.getCurrentLabel().observe(getViewLifecycleOwner(), label -> {
            tvStateLabel.setText(label);
        });

        timerViewModel.getSessionCompleted().observe(getViewLifecycleOwner(), completed -> {
            if (Boolean.TRUE.equals(completed)) {
                // Could show a dialog or animation here
                tvStateLabel.setText("✓ Session complete!");
            }
        });
    }

    private void updateButtonVisibility(TimerState state) {
        // Reset all
        btnStart.setVisibility(View.GONE);
        btnPause.setVisibility(View.GONE);
        btnResume.setVisibility(View.GONE);
        btnReset.setVisibility(View.GONE);
        btnBreak.setVisibility(View.GONE);

        switch (state) {
            case IDLE:
                btnStart.setVisibility(View.VISIBLE);
                btnBreak.setVisibility(View.VISIBLE);
                break;
            case FOCUS:
            case SHORT_BREAK:
            case LONG_BREAK:
                btnPause.setVisibility(View.VISIBLE);
                btnReset.setVisibility(View.VISIBLE);
                break;
            case PAUSED:
                btnResume.setVisibility(View.VISIBLE);
                btnReset.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Save partial session if the user navigates away mid-session
        timerViewModel.saveIncompleteSessionIfRunning();
    }
}
