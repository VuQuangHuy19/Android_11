package com.example.app6hu.model;

public class MonthlySummary {
    public  String id;
    public String month;
    public long amount;

    public MonthlySummary(String month, long amount) {
        this.month = month;
        this.amount = amount;
    }
}
