package com.example.app6hu.model;

public class Truong {
    private String tenTruong;
    private String link;
    private int logo; // drawable resource

    public Truong(String tenTruong, String link, int logo) {
        this.tenTruong = tenTruong;
        this.link = link;
        this.logo = logo;
    }

    public String getTenTruong() {
        return tenTruong;
    }

    public String getLink() {
        return link;
    }

    public int getLogo() {
        return logo;
    }
}
