package model;

public class KhachThue {
    private int maKhach;           // int - khớp với DB (identity)
    private String hoTen;
    private String cccd;
    private String sdt;
    private String queQuan;        // thêm trường queQuan có trong DB
    private String maPhong;
    private double phiDichVu;      // thêm trường phiDichVu có trong DB
    private String trangThai;
    private String tenDN;          // liên kết với TaiKhoan.TenDN

    public KhachThue() {}

    // Constructor đầy đủ (dùng khi đọc từ DB)
    public KhachThue(int maKhach, String hoTen, String cccd, String sdt,
                     String queQuan, String maPhong, double phiDichVu,
                     String trangThai, String tenDN) {
        this.maKhach   = maKhach;
        this.hoTen     = hoTen;
        this.cccd      = cccd;
        this.sdt       = sdt;
        this.queQuan   = queQuan;
        this.maPhong   = maPhong;
        this.phiDichVu = phiDichVu;
        this.trangThai = trangThai;
        this.tenDN     = tenDN;
    }

    // Getters & Setters
    public int getMaKhach()                    { return maKhach; }
    public void setMaKhach(int maKhach)        { this.maKhach = maKhach; }

    public String getHoTen()                   { return hoTen; }
    public void setHoTen(String hoTen)         { this.hoTen = hoTen; }

    public String getCccd()                    { return cccd; }
    public void setCccd(String cccd)           { this.cccd = cccd; }

    public String getSdt()                     { return sdt; }
    public void setSdt(String sdt)             { this.sdt = sdt; }

    public String getQueQuan()                 { return queQuan; }
    public void setQueQuan(String queQuan)     { this.queQuan = queQuan; }

    public String getMaPhong()                 { return maPhong; }
    public void setMaPhong(String maPhong)     { this.maPhong = maPhong; }

    public double getPhiDichVu()               { return phiDichVu; }
    public void setPhiDichVu(double phiDichVu) { this.phiDichVu = phiDichVu; }

    public String getTrangThai()               { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getTenDN()                   { return tenDN; }
    public void setTenDN(String tenDN)         { this.tenDN = tenDN; }

    @Override
    public String toString() {
        return "[" + maKhach + "] " + hoTen + " - Phòng: " + maPhong;
    }
}