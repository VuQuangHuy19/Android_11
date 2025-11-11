package com.example.app6hu.model;



public class ReportItem {
    private String name;
    private long amount;
    private String color;

    public ReportItem(String name, long amount, String color) {
        this.name = name;
        this.amount = amount;
        this.color = color;
    }

    // Getter
    public String getName() {
        return name;
    }

    public long getAmount() {
        return amount;
    }

    public String getColor() {
        return color;
    }

    // Setter (nếu cần)
    public void setName(String name) {
        this.name = name;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setColor(String color) {
        this.color = color;
    }
}



