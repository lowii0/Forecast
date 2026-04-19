package com.forecast.app.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.forecast.app.R;
import com.forecast.app.ui.tasks.TasksViewModel;
import com.forecast.app.util.DateTimeUtils;

public class HomeFragment extends Fragment {

    private TasksViewModel tasksViewModel;

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

        tasksViewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);

        // ── Bind views ────────────────────────────────────────────────────────
        TextView tvGreeting    = view.findViewById(R.id.tvGreeting);
        TextView tvDate        = view.findViewById(R.id.tvHomeDate);
        TextView tvTaskCount   = view.findViewById(R.id.tvIncompleteTaskCount);
        View btnGoToTasks      = view.findViewById(R.id.btnGoToTasks);
        View btnGoToTimer      = view.findViewById(R.id.btnGoToTimer);

        // ── Set greeting & date ───────────────────────────────────────────────
        tvGreeting.setText(DateTimeUtils.getGreeting());
        tvDate.setText(DateTimeUtils.formatDate(new java.util.Date()));

        // ── Observe incomplete tasks ──────────────────────────────────────────
        tasksViewModel.getIncompleteTasks().observe(getViewLifecycleOwner(), tasks -> {
            int count = tasks != null ? tasks.size() : 0;
            tvTaskCount.setText(count + " task" + (count != 1 ? "s" : "") + " remaining today");
        });

        // ── Quick navigation ──────────────────────────────────────────────────
        btnGoToTasks.setOnClickListener(v ->
            Navigation.findNavController(view).navigate(R.id.tasksFragment));

        btnGoToTimer.setOnClickListener(v ->
            Navigation.findNavController(view).navigate(R.id.timerFragment));
    }
}
