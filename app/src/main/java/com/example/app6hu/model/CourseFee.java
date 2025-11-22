package com.example.app6hu.model;

public class CourseFee {
    private String name;
    private long price; // Sử dụng long để lưu trữ số tiền lớn

    public CourseFee(String name, long price) {
        this.name = name;
        this.price = price;
    }

    // Getters
    public String getName() {
        return name;
    }

    public long getPrice() {
        return price;
    }
}