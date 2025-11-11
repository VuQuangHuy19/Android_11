package com.example.app6hu.model;

import java.io.Serializable;
import java.util.Date;

public class Transaction implements Serializable {
    private int id;
    private String type;      // INCOME hoặc EXPENSE
    private double amount;
    private String category;
    private String detail;
    private Date date;
    private String icon;      // emoji hoặc tên icon

    public Transaction() {
    }

    public Transaction(int id, String type, double amount, String category, String detail, Date date, String icon) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.detail = detail;
        this.date = date;
        this.icon = icon;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %.0f", icon, category, detail, amount);
    }
}
