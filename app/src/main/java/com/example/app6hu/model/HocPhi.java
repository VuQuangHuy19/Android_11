package com.example.app6hu.model;

public class HocPhi {
    private String id;
    private String tenHocPhi;
    private long donGiaTin;
    private int soTinChi;
    private long soTien;   // ✅ tiền sau khi tính
    private String ngayDong;
    private String trangThai;
    private boolean noMon;   // ✅ có nợ môn hay không

    public HocPhi() {}

    public HocPhi(String id, String tenHocPhi, long donGiaTin, int soTinChi,
                  boolean noMon, long soTien, String ngayDong, String trangThai) {
        this.id = id;
        this.tenHocPhi = tenHocPhi;
        this.donGiaTin = donGiaTin;
        this.soTinChi = soTinChi;
        this.noMon = noMon;
        this.soTien = soTien;
        this.ngayDong = ngayDong;
        this.trangThai = trangThai;
    }

    public String getId() { return id; }
    public String getTenHocPhi() { return tenHocPhi; }
    public long getDonGiaTin() { return donGiaTin; }
    public int getSoTinChi() { return soTinChi; }
    public boolean isNoMon() { return noMon; }
    public long getSoTien() { return soTien; }
    public String getNgayDong() { return ngayDong; }
    public String getTrangThai() { return trangThai; }

    public void setId(String id) { this.id = id; }
    public void setTenHocPhi(String tenHocPhi) { this.tenHocPhi = tenHocPhi; }
    public void setDonGiaTin (long donGiaTin) { this.donGiaTin = donGiaTin; }
    public void setSoTinChi(int soTinChi) { this.soTinChi = soTinChi; }
    public void setNoMon(boolean noMon) { this.noMon = noMon; }
    public void setSoTien(long soTien) { this.soTien = soTien; }
    public void setNgayDong(String ngayDong) { this.ngayDong = ngayDong; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}
