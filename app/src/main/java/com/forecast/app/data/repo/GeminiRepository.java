package com.forecast.app.data.repo;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GeminiRepository {

    // TODO: Replace with your actual Gemini API Key securely
    private static final String API_KEY = "YOUR_GEMINI_API_KEY_HERE";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + API_KEY;
    private final ExecutorService executor;

    public GeminiRepository() {
        executor = Executors.newSingleThreadExecutor();
    }

    public void generateProductivityInsight(int tasksDone, int sessionsDone, String condition, InsightCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                // Build prompt
                String prompt = "You are a brief, encouraging productivity coach inside an app called Forecast. " +
                        "Today, the user completed " + tasksDone + " tasks and " + sessionsDone + " focus sessions. " +
                        "Their overall productivity condition is " + condition + ". " +
                        "Give them a 1 to 2 sentence personalized motivational insight.";

                // Build JSON payload
                JSONObject part = new JSONObject();
                part.put("text", prompt);
                JSONObject partsObj = new JSONObject();
                partsObj.put("parts", new JSONArray().put(part));
                JSONObject payload = new JSONObject();
                payload.put("contents", new JSONArray().put(partsObj));

                // Send request
                try(OutputStream os = conn.getOutputStream()) {
                    byte[] input = payload.toString().getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                // Read response
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(response.toString());
                String aiText = jsonResponse.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                callback.onSuccess("AI Coach: " + aiText.trim());

            } catch (Exception e) {
                Log.e("GeminiRepo", "Error fetching AI insight", e);
                callback.onError(e.getMessage());
            }
        });
    }

    public interface InsightCallback {
        void onSuccess(String insight);
        void onError(String error);
    }
}