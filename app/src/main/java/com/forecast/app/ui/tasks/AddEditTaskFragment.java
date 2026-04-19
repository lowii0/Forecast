package com.forecast.app.ui.tasks;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.forecast.app.R;
import com.forecast.app.enums.Category;
import com.forecast.app.enums.Priority;
import com.forecast.app.models.Task;
import com.forecast.app.util.DateTimeUtils;

import java.util.Calendar;
import java.util.Date;

public class AddEditTaskFragment extends Fragment {

    private TasksViewModel viewModel;

    // ── UI references ─────────────────────────────────────────────────────────
    private EditText etTitle, etDescription, etPomodoros;
    private Spinner spinnerPriority, spinnerCategory;
    private TextView tvDueDate;
    private Button btnSave;

    // ── State ─────────────────────────────────────────────────────────────────
    private boolean isEditMode = false;
    private int editTaskId = -1;
    private Task existingTask = null;
    private Date selectedDueDate = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_edit_task, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(TasksViewModel.class);

        // ── Bind views ────────────────────────────────────────────────────────
        etTitle         = view.findViewById(R.id.etTaskTitle);
        etDescription   = view.findViewById(R.id.etTaskDescription);
        etPomodoros     = view.findViewById(R.id.etEstimatedPomodoros);
        spinnerPriority = view.findViewById(R.id.spinnerPriority);
        spinnerCategory = view.findViewById(R.id.spinnerCategory);
        tvDueDate       = view.findViewById(R.id.tvDueDate);
        btnSave         = view.findViewById(R.id.btnSaveTask);
        TextView tvTitle = view.findViewById(R.id.tvFormTitle);

        // ── Setup spinners ────────────────────────────────────────────────────
        ArrayAdapter<Priority> priorityAdapter = new ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, Priority.values());
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPriority.setAdapter(priorityAdapter);
        spinnerPriority.setSelection(1); // Default: MEDIUM

        ArrayAdapter<Category> categoryAdapter = new ArrayAdapter<>(
            requireContext(), android.R.layout.simple_spinner_item, Category.values());
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(categoryAdapter);

        // ── Due date picker ───────────────────────────────────────────────────
        tvDueDate.setOnClickListener(v -> showDatePicker());

        // ── Check mode from arguments ─────────────────────────────────────────
        Bundle args = getArguments();
        if (args != null) {
            isEditMode = args.getBoolean("is_edit", false);
            editTaskId = args.getInt("task_id", -1);
        }

        if (isEditMode && editTaskId != -1) {
            tvTitle.setText("Edit Task");
            btnSave.setText("Update Task");
            loadExistingTask();
        } else {
            tvTitle.setText("New Task");
            btnSave.setText("Save Task");
        }

        // ── Save button ───────────────────────────────────────────────────────
        btnSave.setOnClickListener(v -> saveTask(view));
    }

    private void loadExistingTask() {
        viewModel.loadTaskForEdit(editTaskId);
        viewModel.getSelectedTask().observe(getViewLifecycleOwner(), task -> {
            if (task != null && existingTask == null) {
                existingTask = task;
                populateForm(task);
            }
        });
    }

    private void populateForm(Task task) {
        etTitle.setText(task.getTitle());
        etDescription.setText(task.getDescription());
        etPomodoros.setText(String.valueOf(task.getEstimatedPomodoros()));

        // Set spinner selections
        Priority[] priorities = Priority.values();
        for (int i = 0; i < priorities.length; i++) {
            if (priorities[i] == task.getPriority()) {
                spinnerPriority.setSelection(i);
                break;
            }
        }

        Category[] categories = Category.values();
        for (int i = 0; i < categories.length; i++) {
            if (categories[i] == task.getCategory()) {
                spinnerCategory.setSelection(i);
                break;
            }
        }

        if (task.getDueDate() != null) {
            selectedDueDate = task.getDueDate();
            tvDueDate.setText(DateTimeUtils.formatDate(selectedDueDate));
        }
    }

    private void saveTask(View view) {
        String title = etTitle.getText().toString().trim();

        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }

        String description = etDescription.getText().toString().trim();
        Priority priority = (Priority) spinnerPriority.getSelectedItem();
        Category category = (Category) spinnerCategory.getSelectedItem();

        int pomodoros = 1;
        try {
            String pomStr = etPomodoros.getText().toString().trim();
            if (!pomStr.isEmpty()) pomodoros = Integer.parseInt(pomStr);
        } catch (NumberFormatException ignored) {}

        if (isEditMode && existingTask != null) {
            existingTask.setTitle(title);
            existingTask.setDescription(description);
            existingTask.setPriority(priority);
            existingTask.setCategory(category);
            existingTask.setDueDate(selectedDueDate);
            existingTask.setEstimatedPomodoros(pomodoros);
            viewModel.update(existingTask);
            Toast.makeText(getContext(), "Task updated!", Toast.LENGTH_SHORT).show();
        } else {
            Task newTask = new Task(title, description, priority, category, selectedDueDate, pomodoros);
            viewModel.insert(newTask);
            Toast.makeText(getContext(), "Task saved!", Toast.LENGTH_SHORT).show();
        }

        // Navigate back
        Navigation.findNavController(view).popBackStack();
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        if (selectedDueDate != null) cal.setTime(selectedDueDate);

        DatePickerDialog picker = new DatePickerDialog(
            requireContext(),
            (datePicker, year, month, day) -> {
                cal.set(year, month, day);
                selectedDueDate = cal.getTime();
                tvDueDate.setText(DateTimeUtils.formatDate(selectedDueDate));
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        );
        picker.show();
    }
}
