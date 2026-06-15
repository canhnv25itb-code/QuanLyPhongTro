package model;

import java.time.LocalDate;

public class HopDong {
    private int maHopDong;      // Khớp với cột MaHD trong SQL
    private String maPhong;     // ĐÃ ĐỔI THÀNH STRING để chứa các giá trị như "P101", "P102"
    private String tenKhach;    // Khớp với cột TenKhachHang trong SQL
    
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private double tienCoc;
    private String trangThai;

    public HopDong() {}

    // --- GETTER & SETTER ---
    public int getMaHopDong() { return maHopDong; }
    public void setMaHopDong(int maHopDong) { this.maHopDong = maHopDong; }
    
    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }
    
    public String getTenKhach() { return tenKhach; }
    public void setTenKhach(String tenKhach) { this.tenKhach = tenKhach; }
    
    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }
    
    public LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDate ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }
    
    public double getTienCoc() { return tienCoc; }
    public void setTienCoc(double tienCoc) { this.tienCoc = tienCoc; }
    
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
}