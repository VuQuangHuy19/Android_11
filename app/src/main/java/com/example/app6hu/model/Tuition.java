package com.example.app6hu.model;

public class Tuition {
    private String subjectName;
    private int credit;
    private long feePerCredit;
    private long totalFee;
    private boolean isRetake;
    private boolean isPaid;
    // Constructor mặc định
    public Tuition() {}

    // Constructor đầy đủ
    public Tuition(String subjectName, int credit, long feePerCredit, long totalFee, boolean isRetake, boolean isPaid) {
        this.subjectName = subjectName;
        this.credit = credit;
        this.feePerCredit = feePerCredit;
        this.totalFee = totalFee;
        this.isRetake = isRetake;
        this.isPaid = isPaid;
    }
    // Getters và Setters
    public String getSubjectName() {
        return subjectName;
    }
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
    public int getCredit() {
        return credit;
    }
    public void setCredit(int credit) {
        this.credit = credit;
    }
    public long getFeePerCredit() {
        return feePerCredit;
    }
    public void setFeePerCredit(long feePerCredit) {
        this.feePerCredit = feePerCredit;
    }
    public long getTotalFee() {
        return totalFee;
    }
    public void setTotalFee(long totalFee) {
        this.totalFee = totalFee;
    }
    public boolean isRetake() {
        return isRetake;
    }
    public void setRetake(boolean retake) {
        isRetake = retake;
    }
    public boolean isPaid() {
        return isPaid;
    }
    public void setPaid(boolean paid) {
        isPaid = paid;
    }
    // Phương thức toString để debug
    @Override
    public String toString() {
        return "Tuition{" +
                "subjectName='" + subjectName + '\'' +
                ", credit=" + credit +
                ", feePerCredit=" + feePerCredit +
                ", totalFee=" + totalFee +
                ", isRetake=" + isRetake +
                ", isPaid=" + isPaid +
                '}';
    }
}