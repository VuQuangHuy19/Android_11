package com.example.app6hu.utils;

import com.example.app6hu.model.Transaction;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpenseForecastUtils {

    /**
     * Dự báo chi tiêu tháng tiếp theo dựa trên dữ liệu các tháng trước
     */
    public static ForecastResult forecastNextMonth(List<Transaction> pastTransactions) {
        if (pastTransactions == null || pastTransactions.isEmpty()) {
            return new ForecastResult(0, new HashMap<>(), "Không đủ dữ liệu để dự báo");
        }

        // Tính trung bình chi tiêu theo tháng
        Map<Integer, Double> monthlyExpenses = new HashMap<>();
        Map<String, Double> categoryExpenses = new HashMap<>();
        Calendar cal = Calendar.getInstance();

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
            return new ForecastResult(0, new HashMap<>(), "Không có dữ liệu chi tiêu");
        }

        // Tính trung bình chi tiêu hàng tháng
        double totalExpense = 0;
        for (Double expense : monthlyExpenses.values()) {
            totalExpense += expense;
        }
        double avgMonthlyExpense = totalExpense / monthlyExpenses.size();

        // Tính trung bình chi tiêu theo danh mục
        Map<String, Double> avgCategoryExpenses = new HashMap<>();
        int monthCount = monthlyExpenses.size();
        for (Map.Entry<String, Double> entry : categoryExpenses.entrySet()) {
            avgCategoryExpenses.put(entry.getKey(), entry.getValue() / monthCount);
        }

        // Dự báo: sử dụng trung bình + 10% buffer
        double forecastAmount = avgMonthlyExpense * 1.1;

        String message = String.format("Dựa trên %d tháng trước, dự báo chi tiêu tháng tới: %.0fđ", 
                monthCount, forecastAmount);

        return new ForecastResult(forecastAmount, avgCategoryExpenses, message);
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
        private double forecastAmount;
        private Map<String, Double> categoryForecasts;
        private String message;

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
}

