package com.example.app6hu.utils;

import com.example.app6hu.model.Transaction;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseForecastUtils {

    /**
     * Dự báo chi tiêu tháng tiếp theo dựa trên dữ liệu các tháng trước
     * Sử dụng trung bình có trọng số (tháng gần nhất có trọng số cao hơn)
     * Có thể dự báo dựa trên tháng hiện tại nếu chưa có dữ liệu tháng trước
     */
    public static ForecastResult forecastNextMonth(List<Transaction> pastTransactions) {
        if (pastTransactions == null || pastTransactions.isEmpty()) {
            return new ForecastResult(0, new HashMap<>(), "Không đủ dữ liệu để dự báo. Vui lòng thêm giao dịch chi tiêu.");
        }

        // Tính trung bình chi tiêu theo tháng
        Map<Integer, Double> monthlyExpenses = new HashMap<>();
        Map<String, Double> categoryExpenses = new HashMap<>();
        Calendar cal = Calendar.getInstance();
        Calendar now = Calendar.getInstance();

        for (Transaction t : pastTransactions) {
            if (t.getType() != null && "EXPENSE".equals(t.getType()) && t.getDate() != null) {
                cal.setTime(t.getDate());
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);
                int monthKey = year * 12 + month;

                // Tổng chi tiêu theo tháng
                monthlyExpenses.put(monthKey, monthlyExpenses.getOrDefault(monthKey, 0.0) + t.getAmount());

                // Chi tiêu theo danh mục
                String category = t.getCategory() != null ? t.getCategory() : "Khác";
                categoryExpenses.put(category, categoryExpenses.getOrDefault(category, 0.0) + t.getAmount());
            }
        }

        if (monthlyExpenses.isEmpty()) {
            return new ForecastResult(0, new HashMap<>(), "Không có dữ liệu chi tiêu. Vui lòng thêm giao dịch chi tiêu.");
        }

        // Tách tháng hiện tại và các tháng trước
        int currentMonthKey = now.get(Calendar.YEAR) * 12 + now.get(Calendar.MONTH);
        double currentMonthExpense = monthlyExpenses.getOrDefault(currentMonthKey, 0.0);
        
        // Lọc các tháng trước (không tính tháng hiện tại)
        Map<Integer, Double> pastMonths = new HashMap<>();
        for (Map.Entry<Integer, Double> entry : monthlyExpenses.entrySet()) {
            if (entry.getKey() < currentMonthKey) {
                pastMonths.put(entry.getKey(), entry.getValue());
            }
        }

        double forecastAmount;
        String message;
        Map<String, Double> avgCategoryExpenses = new HashMap<>();

        if (pastMonths.isEmpty()) {
            // Chỉ có dữ liệu tháng hiện tại - dự báo dựa trên chi tiêu hiện tại
            int currentDay = now.get(Calendar.DAY_OF_MONTH);
            int daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH);
            
            if (currentDay > 0 && currentMonthExpense > 0) {
                // Tính trung bình chi tiêu mỗi ngày trong tháng hiện tại
                double dailyAverage = currentMonthExpense / currentDay;
                // Dự báo = trung bình ngày * số ngày trong tháng
                forecastAmount = dailyAverage * daysInMonth;
                message = String.format("Dựa trên chi tiêu %d ngày đầu tháng này (%.0fđ/ngày), dự báo tháng tới: %.0fđ", 
                        currentDay, dailyAverage, forecastAmount);
            } else {
                forecastAmount = 0;
                message = "Chưa có đủ dữ liệu chi tiêu trong tháng này để dự báo";
            }
        } else {
            // Có dữ liệu các tháng trước - dùng trung bình có trọng số
            List<Integer> sortedMonths = new java.util.ArrayList<>(pastMonths.keySet());
            java.util.Collections.sort(sortedMonths);
            
            double weightedSum = 0;
            double totalWeight = 0;
            int monthCount = sortedMonths.size();
            
            // Tháng gần nhất có trọng số cao nhất
            for (int i = 0; i < sortedMonths.size(); i++) {
                int monthKey = sortedMonths.get(i);
                double weight = (i + 1.0) / monthCount; // Tháng gần nhất có weight = 1.0
                weightedSum += pastMonths.get(monthKey) * weight;
                totalWeight += weight;
            }
            
            double avgMonthlyExpense = weightedSum / totalWeight;
            
            // Nếu có dữ liệu tháng hiện tại, kết hợp với trung bình các tháng trước
            if (currentMonthExpense > 0) {
                int currentDay = now.get(Calendar.DAY_OF_MONTH);
                int daysInMonth = now.getActualMaximum(Calendar.DAY_OF_MONTH);
                double dailyAverage = currentMonthExpense / currentDay;
                double currentMonthProjection = dailyAverage * daysInMonth;
                
                // Kết hợp: 60% từ tháng hiện tại (nếu có), 40% từ trung bình các tháng trước
                forecastAmount = (currentMonthProjection * 0.6) + (avgMonthlyExpense * 0.4);
                message = String.format("Dựa trên %d tháng trước + tháng này, dự báo tháng tới: %.0fđ", 
                        monthCount + 1, forecastAmount);
            } else {
                // Chỉ dùng trung bình các tháng trước
                forecastAmount = avgMonthlyExpense * 1.1; // +10% buffer
                message = String.format("Dựa trên %d tháng trước (ưu tiên tháng gần nhất), dự báo tháng tới: %.0fđ", 
                        monthCount, forecastAmount);
            }

            // Tính trung bình chi tiêu theo danh mục
            for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
                avgCategoryExpenses.put(entry.getKey(), entry.getValue() / (monthCount + (currentMonthExpense > 0 ? 1 : 0)));
            }
        }

        return new ForecastResult(forecastAmount, avgCategoryExpenses, message);
    }

    /**
     * Kiểm tra và cảnh báo nếu chi tiêu tháng hiện tại vượt quá trung bình
     */
    public static WarningResult checkExpenseWarning(List<Transaction> allTransactions, int currentMonth, int currentYear) {
        if (allTransactions == null || allTransactions.isEmpty()) {
            return new WarningResult(false, 0, 0, "Không có dữ liệu");
        }

        Calendar cal = Calendar.getInstance();
        Map<Integer, Double> monthlyExpenses = new HashMap<>();

        // Tính chi tiêu theo tháng
        for (Transaction t : allTransactions) {
            if (t.getType() != null && "EXPENSE".equals(t.getType()) && t.getDate() != null) {
                cal.setTime(t.getDate());
                int month = cal.get(Calendar.MONTH) + 1; // +1 vì Calendar.MONTH bắt đầu từ 0
                int year = cal.get(Calendar.YEAR);
                int monthKey = year * 12 + month;
                monthlyExpenses.put(monthKey, monthlyExpenses.getOrDefault(monthKey, 0.0) + t.getAmount());
            }
        }

        if (monthlyExpenses.size() < 2) {
            return new WarningResult(false, 0, 0, "Cần ít nhất 2 tháng dữ liệu để so sánh");
        }

        // Tính trung bình các tháng trước (không tính tháng hiện tại)
        int currentMonthKey = currentYear * 12 + currentMonth;
        double totalExpense = 0;
        int monthCount = 0;
        
        for (Map.Entry<Integer, Double> entry : monthlyExpenses.entrySet()) {
            if (entry.getKey() < currentMonthKey) { // Chỉ tính các tháng trước
                totalExpense += entry.getValue();
                monthCount++;
            }
        }

        if (monthCount == 0) {
            return new WarningResult(false, 0, 0, "Chưa có dữ liệu tháng trước");
        }

        double avgExpense = totalExpense / monthCount;
        double currentExpense = monthlyExpenses.getOrDefault(currentMonthKey, 0.0);
        
        boolean isOverLimit = currentExpense > avgExpense;
        double percentage = avgExpense > 0 ? ((currentExpense - avgExpense) / avgExpense) * 100 : 0;
        
        String message;
        if (isOverLimit) {
            message = String.format("⚠️ Chi tiêu tháng này (%.0fđ) vượt quá trung bình (%.0fđ) %.1f%%", 
                    currentExpense, avgExpense, percentage);
        } else {
            message = String.format("✓ Chi tiêu tháng này (%.0fđ) trong mức trung bình (%.0fđ)", 
                    currentExpense, avgExpense);
        }

        return new WarningResult(isOverLimit, currentExpense, avgExpense, message);
    }

    /**
     * Tính xu hướng chi tiêu (tăng/giảm)
     */
    public static String calculateTrend(List<Transaction> recentTransactions) {
        if (recentTransactions == null || recentTransactions.size() < 2) {
            return "Không đủ dữ liệu";
        }

        Calendar cal = Calendar.getInstance();
        Map<Integer, Double> monthlyExpenses = new HashMap<>();

        for (Transaction t : recentTransactions) {
            if (t.getType() != null && "EXPENSE".equals(t.getType()) && t.getDate() != null) {
                cal.setTime(t.getDate());
                int month = cal.get(Calendar.MONTH);
                int year = cal.get(Calendar.YEAR);
                int monthKey = year * 12 + month;
                monthlyExpenses.put(monthKey, monthlyExpenses.getOrDefault(monthKey, 0.0) + t.getAmount());
            }
        }

        if (monthlyExpenses.size() < 2) {
            return "Không đủ dữ liệu";
        }

        // So sánh 2 tháng gần nhất
        List<Integer> sortedMonths = new java.util.ArrayList<>(monthlyExpenses.keySet());
        java.util.Collections.sort(sortedMonths);

        if (sortedMonths.size() >= 2) {
            double lastMonth = monthlyExpenses.get(sortedMonths.get(sortedMonths.size() - 1));
            double prevMonth = monthlyExpenses.get(sortedMonths.get(sortedMonths.size() - 2));
            
            double change = ((lastMonth - prevMonth) / prevMonth) * 100;
            
            if (change > 5) {
                return String.format("Tăng %.1f%% so với tháng trước", change);
            } else if (change < -5) {
                return String.format("Giảm %.1f%% so với tháng trước", Math.abs(change));
            } else {
                return "Ổn định";
            }
        }

        return "Không đủ dữ liệu";
    }

    public static class ForecastResult {
        private final double forecastAmount;
        private final Map<String, Double> categoryForecasts;
        private final String message;

        public ForecastResult(double forecastAmount, Map<String, Double> categoryForecasts, String message) {
            this.forecastAmount = forecastAmount;
            this.categoryForecasts = categoryForecasts;
            this.message = message;
        }

        public double getForecastAmount() {
            return forecastAmount;
        }

        public Map<String, Double> getCategoryForecasts() {
            return categoryForecasts;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class WarningResult {
        private final boolean isOverLimit;
        private final double currentExpense;
        private final double avgExpense;
        private final String message;

        public WarningResult(boolean isOverLimit, double currentExpense, double avgExpense, String message) {
            this.isOverLimit = isOverLimit;
            this.currentExpense = currentExpense;
            this.avgExpense = avgExpense;
            this.message = message;
        }

        public boolean isOverLimit() {
            return isOverLimit;
        }

        public double getCurrentExpense() {
            return currentExpense;
        }

        public double getAvgExpense() {
            return avgExpense;
        }

        public String getMessage() {
            return message;
        }
    }
}

