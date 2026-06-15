package model;

public class DichVu {
    private int id;
    private String maPhong;
    private double wifi;
    private double thangMay;
    private double giuXe;
    private double pcccVeSinh;
    private double tongTien; // Cột này SQL tự tính qua công thức (Wifi + ThangMay + GiuXe + PCCC_VeSinh)
    private String ghiChu;   // Chứa "Đã thuê" hoặc "Trống" từ bảng PhongTro

    // Constructor mặc định
    public DichVu() {}

    // Constructor đầy đủ tham số (Hữu ích khi cần tạo nhanh đối tượng để Add/Update)
    public DichVu(int id, String maPhong, double wifi, double thangMay, double giuXe, double pcccVeSinh) {
        this.id = id;
        this.maPhong = maPhong;
        this.wifi = wifi;
        this.thangMay = thangMay;
        this.giuXe = giuXe;
        this.pcccVeSinh = pcccVeSinh;
    }

    // Getter và Setter
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getMaPhong() { return maPhong; }
    public void setMaPhong(String maPhong) { this.maPhong = maPhong; }

    public double getWifi() { return wifi; }
    public void setWifi(double wifi) { this.wifi = wifi; }

    public double getThangMay() { return thangMay; }
    public void setThangMay(double thangMay) { this.thangMay = thangMay; }

    public double getGiuXe() { return giuXe; }
    public void setGiuXe(double giuXe) { this.giuXe = giuXe; }

    public double getPcccVeSinh() { return pcccVeSinh; }
    public void setPcccVeSinh(double pcccVeSinh) { this.pcccVeSinh = pcccVeSinh; }

    // Lưu ý: Cột này lấy giá trị trực tiếp từ cột TongTienDV trong Database
    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    // Biến ghiChu dùng để lưu "Trạng thái" giúp Renderer tô màu Vàng/Trắng
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    // Phương thức toString (Tùy chọn - giúp bạn debug nhanh khi In ra màn hình)
    @Override
    public String toString() {
        return "DichVu{" + "maPhong=" + maPhong + ", trangThai=" + ghiChu + ", tong=" + tongTien + '}';
    }
}