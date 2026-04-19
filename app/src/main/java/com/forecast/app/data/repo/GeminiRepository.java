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

    private static final String API_KEY = "AIzaSyA1QT6OZs_02LOTgvDdXtd_PlV3--gFmtk";

    // ✅ Stable working REST endpoint
    // We update the name to gemini-3-flash-preview as per the new documentation
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3-flash-preview:generateContent?key=" + API_KEY;

    private final ExecutorService executor;

    public GeminiRepository() {
        executor = Executors.newSingleThreadExecutor();
    }

    public void generateProductivityInsight(
            int tasksDone,
            int sessionsDone,
            String condition,
            InsightCallback callback
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

                // -------------------------
                // PROMPT
                // -------------------------
                String prompt =
                        "You are a brief, encouraging productivity coach inside an app called Forecast. " +
                                "Today the user completed " + tasksDone + " tasks and " +
                                sessionsDone + " focus sessions. Their productivity condition is " +
                                condition + ". Give a 1–2 sentence motivational insight.";

                // -------------------------
                // REQUEST JSON (CORRECT FORMAT)
                // -------------------------
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

                // -------------------------
                // SEND REQUEST
                // -------------------------
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

                // -------------------------
                // DEBUG LOG (IMPORTANT)
                // -------------------------
                Log.d("GeminiRAW", raw);

                JSONObject json = new JSONObject(raw);

                // -------------------------
                // HANDLE API ERROR SAFELY
                // -------------------------
                if (json.has("error")) {
                    JSONObject err = json.getJSONObject("error");
                    callback.onError(err.toString());
                    return;
                }

                // -------------------------
                // SAFE PARSING (NO CRASH)
                // -------------------------
                JSONArray candidates = json.optJSONArray("candidates");

                if (candidates == null || candidates.length() == 0) {
                    callback.onError("No candidates returned. Full response: " + raw);
                    return;
                }

                JSONObject contentObj = candidates
                        .getJSONObject(0)
                        .optJSONObject("content");

                if (contentObj == null) {
                    callback.onError("Missing content field. Raw: " + raw);
                    return;
                }

                JSONArray partsArray = contentObj.optJSONArray("parts");

                if (partsArray == null || partsArray.length() == 0) {
                    callback.onError("Missing parts field. Raw: " + raw);
                    return;
                }

                String aiText = partsArray
                        .getJSONObject(0)
                        .optString("text", "Keep going — you're doing great!");

                callback.onSuccess("AI Coach: " + aiText.trim());

            } catch (Exception e) {
                Log.e("GeminiRepo", "Error fetching AI insight", e);
                callback.onError(e.toString());
            }
        });
    }

    public interface InsightCallback {
        void onSuccess(String insight);
        void onError(String error);
    }
}