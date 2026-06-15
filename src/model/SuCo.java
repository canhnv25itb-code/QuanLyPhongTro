package model;

import java.time.LocalDate;

public class SuCo {
    private int id;
    private String maPhong;
    private String moTa;
    private LocalDate ngayGui;
    private String trangThai;
    private String tenDN;

    public SuCo() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }
    public LocalDate getNgayGui() { return ngayGui; }
    public void setNgayGui(LocalDate ngayGui) { this.ngayGui = ngayGui; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }
    public String getTenDN() { return tenDN; }
    public void setTenDN(String tenDN) { this.tenDN = tenDN; }
}
