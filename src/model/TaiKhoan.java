package model;

public class TaiKhoan {
    private int taiKhoanID;
    private String tenDangNhap;
    private String matKhau;
    private String vaiTro;   // "Chủ trọ" hoặc "Khách thuê"
    private String email;

    // ─── Constructors ────────────────────────────────────────────────────────

    public TaiKhoan() {}

    public TaiKhoan(int taiKhoanID, String tenDangNhap, String matKhau,
                    String vaiTro, String email) {
        this.taiKhoanID  = taiKhoanID;
        this.tenDangNhap = tenDangNhap;
        this.matKhau     = matKhau;
        this.vaiTro      = vaiTro;
        this.email       = email;
    }

    // Giữ lại constructor cũ (4 tham số, không có email) để tương thích ngược
    public TaiKhoan(int taiKhoanID, String tenDangNhap, String matKhau, String vaiTro) {
        this(taiKhoanID, tenDangNhap, matKhau, vaiTro, null);
    }

    // ─── Getters & Setters ───────────────────────────────────────────────────

    public int getTaiKhoanID()                   { return taiKhoanID; }
    public void setTaiKhoanID(int taiKhoanID)    { this.taiKhoanID = taiKhoanID; }

    public String getTenDangNhap()               { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }

    public String getMatKhau()                   { return matKhau; }
    public void setMatKhau(String matKhau)       { this.matKhau = matKhau; }

    public String getVaiTro()                    { return vaiTro; }
    public void setVaiTro(String vaiTro)         { this.vaiTro = vaiTro; }

    public String getEmail()                     { return email; }
    public void setEmail(String email)           { this.email = email; }
}