package model;

import java.time.LocalDate;

public class ThongBao {
    private int id;
    private String tieuDe;
    private String noiDung;
    private LocalDate ngayDang;
    private String tenDN;

    public ThongBao() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }
    public LocalDate getNgayDang() { return ngayDang; }
    public void setNgayDang(LocalDate ngayDang) { this.ngayDang = ngayDang; }
    public String getTenDN() { return tenDN; }
    public void setTenDN(String tenDN) { this.tenDN = tenDN; }
}
