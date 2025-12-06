package com.example.app6hu.model;

public class Calendars {

    private int day;           // ngày trong tháng
    private String dayName; // "Thứ 2, 11/11"
    private String category;   // Danh mục
    private String description;// chi tiết
    private double amount;       // số tiền
    private boolean isExpense; // true = chi, false = thu
    private int iconResId;     // drawable icon

    public Calendars(int day, String category, String description, long amount, boolean isExpense, int iconResId) {
        this.day = day;
        this.category = category;
        this.description = description;
        this.amount = amount;
        this.isExpense = isExpense;
        this.iconResId = iconResId;
    }
    public Calendars() {
    }

    // Constructor đầy đủ

    public int getDay() { return day; }
    public void setDay(int day) { this.day = day; }

    public String getDayName() { return dayName; }
    public void setDayName(String dayName) { this.dayName = dayName; }

    public boolean isExpense() { return isExpense; }
    public void setExpense(boolean expense) { isExpense = expense; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getIconResId() { return iconResId; }
    public void setIconResId(int iconResId) { this.iconResId = iconResId; }
}

