package com.forecast.app.ui.summary;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.forecast.app.data.repo.GeminiRepository;
import com.forecast.app.data.repo.ProductivityRepository;
import com.forecast.app.models.ProductivitySummary;

public class SummaryViewModel extends AndroidViewModel {

    private final ProductivityRepository repository;
    private final GeminiRepository geminiRepository;

    private final MutableLiveData<ProductivitySummary> summary = new MutableLiveData<>();
    private final MutableLiveData<String> aiInsight = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public SummaryViewModel(@NonNull Application application) {
        super(application);
        repository = new ProductivityRepository(application);
        geminiRepository = new GeminiRepository(); // NEW
    }

    public LiveData<ProductivitySummary> getSummary() { return summary; }
    public LiveData<String> getAiInsight()            { return aiInsight; }
    public LiveData<Boolean> getIsLoading()           { return isLoading; }
    public LiveData<String> getError()                { return error; }

    public void loadTodaySummary() {
        isLoading.setValue(true);
        error.setValue(null);
        aiInsight.setValue("Asking AI coach for insights..."); // Loading state for AI

        repository.getTodaySummary(result -> {
            mainHandler.post(() -> {
                summary.setValue(result);
                isLoading.setValue(false);

                // Fetch AI Insight once summary is ready
                fetchAiInsight(result);
            });
        });
    }

    private void fetchAiInsight(ProductivitySummary result) {
        geminiRepository.generateProductivityInsight(
                result.getCompletedTasks(),
                result.getCompletedFocusSessions(),
                result.getCondition().name(),
                new GeminiRepository.InsightCallback() {
                    @Override
                    public void onSuccess(String insight) {
                        mainHandler.post(() -> aiInsight.setValue(insight));
                    }

                    @Override
                    public void onError(String errorMsg) {
                        mainHandler.post(() -> aiInsight.setValue("Failed to load AI coach. Keep up the good work!"));
                    }
                }
        );
    }

    public void refresh() {
        loadTodaySummary();
    }
}