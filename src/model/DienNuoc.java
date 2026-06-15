package model;

public class DienNuoc {
    private int id;
    private String tenPhong;
    private String thangNam;
    private int dienDau, dienCuoi, nuocDau, nuocCuoi;
    private double tienDien, tienNuoc; // Thêm 2 cột này

    public DienNuoc() {}

    // Getter & Setter cho tất cả (Nhớ tạo đầy đủ nhé)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }
    public String getThangNam() { return thangNam; }
    public void setThangNam(String thangNam) { this.thangNam = thangNam; }
    public int getDienDau() { return dienDau; }
    public void setDienDau(int dienDau) { this.dienDau = dienDau; }
    public int getDienCuoi() { return dienCuoi; }
    public void setDienCuoi(int dienCuoi) { this.dienCuoi = dienCuoi; }
    public int getNuocDau() { return nuocDau; }
    public void setNuocDau(int nuocDau) { this.nuocDau = nuocDau; }
    public int getNuocCuoi() { return nuocCuoi; }
    public void setNuocCuoi(int nuocCuoi) { this.nuocCuoi = nuocCuoi; }
    
    public double getTienDien() { return tienDien; }
    public void setTienDien(double tienDien) { this.tienDien = tienDien; }
    public double getTienNuoc() { return tienNuoc; }
    public void setTienNuoc(double tienNuoc) { this.tienNuoc = tienNuoc; }

    public double getTongTien() { return tienDien + tienNuoc; }
}