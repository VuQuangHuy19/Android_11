package com.example.app6hu.api;

import android.util.Log;

import com.example.app6hu.model.Transaction;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ExpensePredictionApi {
    private static final String TAG = "ExpensePredictionApi";
    private static final String BASE_URL = "http://10.0.2.2:5000"; // Android emulator localhost
    // Đối với thiết bị thật, thay bằng IP máy tính: "http://192.168.x.x:5000"
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final SimpleDateFormat dateFormat;

    public ExpensePredictionApi() {
        this.client = new OkHttpClient();
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
    }

    /**
     * Lấy dự đoán chi tiêu từ API
     * @param transactions Danh sách giao dịch để gửi lên server
     * @param callback Callback để xử lý kết quả
     */
    public void getPrediction(List<Transaction> transactions, PredictionCallback callback) {
        try {
            // Chuyển đổi transactions sang JSON
            JSONArray transactionsJson = new JSONArray();
            for (Transaction t : transactions) {
                JSONObject transJson = new JSONObject();
                transJson.put("type", t.getType());
                transJson.put("amount", t.getAmount());
                transJson.put("category", t.getCategory() != null ? t.getCategory() : "Khác");
                transJson.put("detail", t.getDetail() != null ? t.getDetail() : "");
                if (t.getDate() != null) {
                    transJson.put("date", dateFormat.format(t.getDate()));
                } else {
                    transJson.put("date", dateFormat.format(new Date()));
                }
                transactionsJson.put(transJson);
            }

            JSONObject requestBody = new JSONObject();
            requestBody.put("transactions", transactionsJson);

            RequestBody body = RequestBody.create(requestBody.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + "/api/predict")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "Lỗi khi gọi API: " + e.getMessage());
                    if (callback != null) {
                        callback.onFailure(e);
                    }
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if (!response.isSuccessful()) {
                        Log.e(TAG, "API trả về lỗi: " + response.code());
                        if (callback != null) {
                            callback.onFailure(new IOException("API error: " + response.code()));
                        }
                        return;
                    }

                    try {
                        String responseBody = response.body().string();
                        JSONObject json = new JSONObject(responseBody);
                        
                        PredictionResult result = parsePredictionResult(json);
                        if (callback != null) {
                            callback.onSuccess(result);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi khi parse response: " + e.getMessage());
                        if (callback != null) {
                            callback.onFailure(e);
                        }
                    }
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "Lỗi khi tạo request: " + e.getMessage());
            if (callback != null) {
                callback.onFailure(e);
            }
        }
    }

    /**
     * Mở web interface trong browser
     * @return URL của web interface
     */
    public String getWebInterfaceUrl() {
        return BASE_URL;
    }

    private PredictionResult parsePredictionResult(JSONObject json) throws Exception {
        PredictionResult result = new PredictionResult();
        
        result.currentMonthTotal = json.getDouble("current_month_total");
        result.predictedNextMonth = json.getDouble("predicted_next_month");
        result.averageExpense = json.getDouble("average_expense");
        result.trend = json.getString("trend");
        
        if (json.has("trend_percentage") && !json.isNull("trend_percentage")) {
            result.trendPercentage = json.getDouble("trend_percentage");
        }

        // Parse monthly data
        JSONArray monthlyData = json.getJSONArray("monthly_data");
        for (int i = 0; i < monthlyData.length(); i++) {
            JSONObject monthData = monthlyData.getJSONObject(i);
            MonthlyData data = new MonthlyData();
            data.month = monthData.getString("month");
            data.amount = monthData.getDouble("amount");
            result.monthlyData.add(data);
        }

        // Parse predicted data
        JSONArray predictedData = json.getJSONArray("predicted_data");
        for (int i = 0; i < predictedData.length(); i++) {
            JSONObject predData = predictedData.getJSONObject(i);
            MonthlyData data = new MonthlyData();
            data.month = predData.getString("month");
            data.amount = predData.getDouble("amount");
            result.predictedData.add(data);
        }

        // Parse category predictions
        JSONArray categoryPredictions = json.getJSONArray("category_predictions");
        for (int i = 0; i < categoryPredictions.length(); i++) {
            JSONObject catPred = categoryPredictions.getJSONObject(i);
            CategoryPrediction pred = new CategoryPrediction();
            pred.category = catPred.getString("category");
            pred.predictedAmount = catPred.getDouble("predicted_amount");
            result.categoryPredictions.add(pred);
        }

        // Parse warnings
        if (json.has("warnings")) {
            JSONArray warnings = json.getJSONArray("warnings");
            for (int i = 0; i < warnings.length(); i++) {
                JSONObject warning = warnings.getJSONObject(i);
                WarningItem warningItem = new WarningItem();
                warningItem.message = warning.getString("message");
                warningItem.severity = warning.getString("severity");
                warningItem.type = warning.getString("type");
                result.warnings.add(warningItem);
            }
        }
        if (json.has("warning_level")) {
            result.warningLevel = json.getString("warning_level");
        }

        return result;
    }

    public interface PredictionCallback {
        void onSuccess(PredictionResult result);
        void onFailure(Exception e);
    }

    public static class PredictionResult {
        public double currentMonthTotal;
        public double predictedNextMonth;
        public double averageExpense;
        public String trend;
        public double trendPercentage;
        public List<MonthlyData> monthlyData = new ArrayList<>();
        public List<MonthlyData> predictedData = new ArrayList<>();
        public List<CategoryPrediction> categoryPredictions = new ArrayList<>();
        public List<WarningItem> warnings = new ArrayList<>();
        public String warningLevel = "none";
    }

    public static class MonthlyData {
        public String month;
        public double amount;
    }

    public static class CategoryPrediction {
        public String category;
        public double predictedAmount;
    }

    public static class WarningItem {
        public String message;
        public String severity;
        public String type;
    }
}

