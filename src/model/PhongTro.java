package model;

public class PhongTro {
    private String maPhong;
    private int tang;
    private String trangThai;
    private double giaPhong;  // Thêm mới
    private double dienTich;  // Thêm mới

    public PhongTro() {}

    // Constructor cập nhật đầy đủ 5 tham số
    public PhongTro(String maPhong, int tang, String trangThai, double giaPhong, double dienTich) {
        this.maPhong = maPhong;
        this.tang = tang;
        this.trangThai = trangThai;
        this.giaPhong = giaPhong;
        this.dienTich = dienTich;
    }

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }
    
    public int getTang() { return tang; }
    public void setTang(int tang) { this.tang = tang; }
    
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public double getGiaPhong() { return giaPhong; }
    public void setGiaPhong(double giaPhong) { this.giaPhong = giaPhong; }

    public double getDienTich() { return dienTich; }
    public void setDienTich(double dienTich) { this.dienTich = dienTich; }
}