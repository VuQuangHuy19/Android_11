package com.example.app6hu.model;

public class Calendars {

    private int day;           // ngày trong tháng
    private String category;   // Danh mục
    private String description;// chi tiết
    private long amount;       // số tiền
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

    public int getDay() { return day; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public long getAmount() { return amount; }
    public boolean isExpense() { return isExpense; }
    public int getIconResId() { return iconResId; }
}
