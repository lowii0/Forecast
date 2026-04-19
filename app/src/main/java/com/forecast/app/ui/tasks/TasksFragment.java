package com.forecast.app.ui.tasks;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.forecast.app.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

public class TasksFragment extends Fragment {

    private TasksViewModel viewModel;
    private TaskAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tasks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);

        // ── RecyclerView setup ────────────────────────────────────────────────
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewTasks);
        adapter = new TaskAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // ── Adapter listeners ─────────────────────────────────────────────────

        // Click to edit
        adapter.setOnTaskClickListener(task -> {
            Bundle args = new Bundle();
            args.putInt("task_id", task.getId());
            args.putBoolean("is_edit", true);
            Navigation.findNavController(view)
                    .navigate(R.id.action_global_addEditTaskFragment, args); // FIXED
        });

        // Check / uncheck
        adapter.setOnTaskCheckedListener((task, isChecked) -> {
            viewModel.setCompleted(task.getId(), isChecked);
        });

        // Delete with Snackbar undo
        adapter.setOnTaskDeleteListener(task -> {
            viewModel.delete(task);
            Snackbar.make(view, "Task deleted", Snackbar.LENGTH_LONG)
                    .setAction("UNDO", v -> viewModel.insert(task))
                    .show();
        });

        // ── Observe tasks ─────────────────────────────────────────────────────
        viewModel.getAllTasks().observe(getViewLifecycleOwner(), tasks -> {
            adapter.submitList(tasks);

            // Show/hide empty state
            View emptyState = view.findViewById(R.id.layoutEmptyTasks);
            if (emptyState != null) {
                emptyState.setVisibility(tasks == null || tasks.isEmpty()
                        ? View.VISIBLE : View.GONE);
            }
        });

        // ── FAB ───────────────────────────────────────────────────────────────
        FloatingActionButton fab = view.findViewById(R.id.fabAddTask);
        fab.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("is_edit", false);
            Navigation.findNavController(view)
                    .navigate(R.id.action_global_addEditTaskFragment, args); // FIXED
        });
    }
}