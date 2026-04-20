package com.forecast.app.data.repo;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiRepository {

    private static final String API_KEY = "AIzaSyDjGoaQOLipOGXBE0BONI8ympjUgxX6ecY";
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + API_KEY;
    private final ExecutorService executor;

    public GeminiRepository() {
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Analyzes and prioritizes a list of tasks using Gemini AI
     */
    public void prioritizeTasks(
            List<TaskItem> tasks,
            PrioritizeCallback callback
    ) {
        executor.execute(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                // Build task list for prompt
                StringBuilder taskList = new StringBuilder();
                for (int i = 0; i < tasks.size(); i++) {
                    TaskItem task = tasks.get(i);
                    taskList.append((i + 1))
                            .append(". ")
                            .append(task.getTitle());

                    if (task.getDescription() != null && !task.getDescription().isEmpty()) {
                        taskList.append(" - ").append(task.getDescription());
                    }

                    if (task.getDueDate() != null && !task.getDueDate().isEmpty()) {
                        taskList.append(" (Due: ").append(task.getDueDate()).append(")");
                    }

                    taskList.append("\n");
                }

                // Create prompt for Gemini
                String prompt = "You are an expert productivity assistant. Analyze these tasks and provide:\n\n" +
                        "1. A brief insight about the overall workload (2-3 sentences)\n" +
                        "2. Prioritized order of tasks (just the numbers in priority order, comma-separated)\n" +
                        "3. A brief reason for the prioritization (1-2 sentences)\n\n" +
                        "Tasks:\n" + taskList.toString() + "\n" +
                        "Respond ONLY in this exact JSON format:\n" +
                        "{\n" +
                        "  \"insight\": \"your insight here\",\n" +
                        "  \"priorityOrder\": [1, 3, 2, 4],\n" +
                        "  \"reasoning\": \"your reasoning here\"\n" +
                        "}";

                // Build request JSON
                JSONObject textPart = new JSONObject();
                textPart.put("text", prompt);

                JSONArray parts = new JSONArray();
                parts.put(textPart);

                JSONObject content = new JSONObject();
                content.put("parts", parts);

                JSONArray contents = new JSONArray();
                contents.put(content);

                JSONObject payload = new JSONObject();
                payload.put("contents", contents);

                // Send request
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload.toString().getBytes("utf-8"));
                }

                int code = conn.getResponseCode();
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(
                                (code >= 200 && code < 300)
                                        ? conn.getInputStream()
                                        : conn.getErrorStream(),
                                "utf-8"
                        )
                );

                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }

                String raw = response.toString();
                Log.d("GeminiRAW", raw);

                JSONObject json = new JSONObject(raw);

                // Handle API errors
                if (json.has("error")) {
                    JSONObject err = json.getJSONObject("error");
                    callback.onError(err.toString());
                    return;
                }

                // Parse response
                JSONArray candidates = json.optJSONArray("candidates");
                if (candidates == null || candidates.length() == 0) {
                    callback.onError("No candidates returned");
                    return;
                }

                JSONObject contentObj = candidates.getJSONObject(0).optJSONObject("content");
                if (contentObj == null) {
                    callback.onError("Missing content field");
                    return;
                }

                JSONArray partsArray = contentObj.optJSONArray("parts");
                if (partsArray == null || partsArray.length() == 0) {
                    callback.onError("Missing parts field");
                    return;
                }

                String aiText = partsArray.getJSONObject(0).optString("text", "");

                // Parse AI response JSON
                String cleanedText = aiText.trim();
                if (cleanedText.startsWith("```json")) {
                    cleanedText = cleanedText.substring(7);
                }
                if (cleanedText.startsWith("```")) {
                    cleanedText = cleanedText.substring(3);
                }
                if (cleanedText.endsWith("```")) {
                    cleanedText = cleanedText.substring(0, cleanedText.length() - 3);
                }
                cleanedText = cleanedText.trim();

                JSONObject aiResult = new JSONObject(cleanedText);

                String insight = aiResult.optString("insight", "");
                String reasoning = aiResult.optString("reasoning", "");
                JSONArray priorityOrder = aiResult.optJSONArray("priorityOrder");

                // Build prioritized task list
                List<TaskItem> prioritizedTasks = new ArrayList<>();
                if (priorityOrder != null) {
                    for (int i = 0; i < priorityOrder.length(); i++) {
                        int taskIndex = priorityOrder.getInt(i) - 1;
                        if (taskIndex >= 0 && taskIndex < tasks.size()) {
                            TaskItem task = tasks.get(taskIndex);
                            task.setPriority(i + 1);
                            prioritizedTasks.add(task);
                        }
                    }
                }

                // Add any remaining tasks that weren't prioritized
                for (TaskItem task : tasks) {
                    if (!prioritizedTasks.contains(task)) {
                        prioritizedTasks.add(task);
                    }
                }

                callback.onSuccess(insight, reasoning, prioritizedTasks);

            } catch (Exception e) {
                Log.e("GeminiRepo", "Error prioritizing tasks", e);
                callback.onError(e.toString());
            }
        });
    }

    public interface PrioritizeCallback {
        void onSuccess(String insight, String reasoning, List<TaskItem> prioritizedTasks);
        void onError(String error);
    }

    /**
     * Simple task model for prioritization
     */
    public static class TaskItem {
        private String id;
        private String title;
        private String description;
        private String dueDate;
        private int priority;
        private boolean isCompleted;

        public TaskItem(String id, String title, String description, String dueDate) {
            this.id = id;
            this.title = title;
            this.description = description;
            this.dueDate = dueDate;
            this.priority = 0;
            this.isCompleted = false;
        }

        public String getId() { return id; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getDueDate() { return dueDate; }
        public int getPriority() { return priority; }
        public boolean isCompleted() { return isCompleted; }

        public void setPriority(int priority) { this.priority = priority; }
        public void setCompleted(boolean completed) { isCompleted = completed; }
    }
}