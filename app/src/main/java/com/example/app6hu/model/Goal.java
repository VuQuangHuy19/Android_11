package com.example.app6hu.model;



public class Goal {
    private String id; // Document ID từ Firestore
    private String name;
    private long targetAmount;
    private long savedAmount;
    private String deadline;
    private String note;
    private String status;
    private String createdAt;
    private String color;

    // Constructor mặc định
    public Goal() {}

    // Constructor đầy đủ
    public Goal(String name, long targetAmount, long savedAmount, String deadline, String note, String status, String createdAt, String color) {
        this.name = name;
        this.targetAmount = targetAmount;
        this.savedAmount = savedAmount;
        this.deadline = deadline;
        this.note = note;
        this.status = status;
        this.createdAt = createdAt;
        this.color = color;
    }

    // Getters và Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(long targetAmount) {
        this.targetAmount = targetAmount;
    }

    public long getSavedAmount() {
        return savedAmount;
    }

    public void setSavedAmount(long savedAmount) {
        this.savedAmount = savedAmount;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    // Phương thức toString để debug
    @Override
    public String toString() {
        return "Goal{" +
                "name='" + name + '\'' +
                ", targetAmount=" + targetAmount +
                ", savedAmount=" + savedAmount +
                ", deadline='" + deadline + '\'' +
                ", note='" + note + '\'' +
                ", status='" + status + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", color='" + color + '\'' +
                '}';
    }
}
