package com.forecast.app.ui.tasks;

import android.annotation.SuppressLint;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.forecast.app.R;
import com.forecast.app.models.Task;

public class TaskAdapter extends ListAdapter<Task, TaskAdapter.TaskViewHolder> {

    // ── Listener interfaces ───────────────────────────────────────────────────

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    public interface OnTaskCheckedListener {
        void onTaskChecked(Task task, boolean isChecked);
    }

    public interface OnTaskDeleteListener {
        void onTaskDelete(Task task);
    }

    // ── Fields ────────────────────────────────────────────────────────────────

    private OnTaskClickListener clickListener;
    private OnTaskCheckedListener checkedListener;
    private OnTaskDeleteListener deleteListener;

    // ── Constructor ───────────────────────────────────────────────────────────

    public TaskAdapter() {
        super(DIFF_CALLBACK);
    }

    // ── DiffUtil ──────────────────────────────────────────────────────────────

    private static final DiffUtil.ItemCallback<Task> DIFF_CALLBACK =
        new DiffUtil.ItemCallback<Task>() {
            @Override
            public boolean areItemsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
                return oldItem.getId() == newItem.getId();
            }

            @SuppressLint("DiffUtilEquals")
            @Override
            public boolean areContentsTheSame(@NonNull Task oldItem, @NonNull Task newItem) {
                return oldItem.getTitle().equals(newItem.getTitle())
                    && oldItem.isCompleted() == newItem.isCompleted()
                    && oldItem.getPriority() == newItem.getPriority()
                    && oldItem.getCategory() == newItem.getCategory();
            }
        };

    // ── Listener setters ──────────────────────────────────────────────────────

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnTaskCheckedListener(OnTaskCheckedListener listener) {
        this.checkedListener = listener;
    }

    public void setOnTaskDeleteListener(OnTaskDeleteListener listener) {
        this.deleteListener = listener;
    }

    // ── RecyclerView.Adapter ──────────────────────────────────────────────────

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = getItem(position);
        holder.bind(task);
    }

    // ── ViewHolder ────────────────────────────────────────────────────────────

    class TaskViewHolder extends RecyclerView.ViewHolder {

        private final CheckBox checkBoxCompleted;
        private final TextView tvTitle;
        private final TextView tvDescription;
        private final TextView tvPriority;
        private final TextView tvCategory;
        private final ImageButton btnDelete;

        TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBoxCompleted = itemView.findViewById(R.id.checkBoxCompleted);
            tvTitle           = itemView.findViewById(R.id.tvTaskTitle);
            tvDescription     = itemView.findViewById(R.id.tvTaskDescription);
            tvPriority        = itemView.findViewById(R.id.tvPriority);
            tvCategory        = itemView.findViewById(R.id.tvCategory);
            btnDelete         = itemView.findViewById(R.id.btnDeleteTask);
        }

        void bind(Task task) {
            // Title & strike-through if completed
            tvTitle.setText(task.getTitle());
            if (task.isCompleted()) {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setAlpha(0.5f);
            } else {
                tvTitle.setPaintFlags(tvTitle.getPaintFlags() & ~Paint.STRIKE_THRU_TEXT_FLAG);
                tvTitle.setAlpha(1.0f);
            }

            // Description
            if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setText(task.getDescription());
            } else {
                tvDescription.setVisibility(View.GONE);
            }

            // Priority badge
            tvPriority.setText(task.getPriority().name());

            // Category badge
            tvCategory.setText(task.getCategory().name());

            // Checkbox (avoid infinite loop by removing listener first)
            checkBoxCompleted.setOnCheckedChangeListener(null);
            checkBoxCompleted.setChecked(task.isCompleted());
            checkBoxCompleted.setOnCheckedChangeListener((btn, isChecked) -> {
                if (checkedListener != null) checkedListener.onTaskChecked(task, isChecked);
            });

            // Click to edit
            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onTaskClick(task);
            });

            // Delete button
            btnDelete.setOnClickListener(v -> {
                if (deleteListener != null) deleteListener.onTaskDelete(task);
            });
        }
    }
}
