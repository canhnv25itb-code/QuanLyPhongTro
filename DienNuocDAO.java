package dao;

import util.DatabaseConnection;
import util.Session;
import model.DienNuoc;

import java.sql.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * DienNuocDAO – quản lý điện nước + cấu hình giá điện/nước theo từng chủ trọ.
 */
public class DienNuocDAO {

    private int lastSyncRows = -1;

    // Giá mặc định nếu chưa cấu hình
    private static final double DEFAULT_GIA_DIEN = 3500;
    private static final double DEFAULT_GIA_NUOC = 15000;

    // Bytes UTF-16LE của "Chưa thu" – bypass collation tiếng Việt
    private static final byte[] BYTES_CHUA_THU =
            "Chưa thu".getBytes(StandardCharsets.UTF_16LE);

    public int getLastSyncRows() { return lastSyncRows; }

    // ─────────────────────────────────────────────────────────────────────────
    // PHẦN CẤU HÌNH GIÁ ĐIỆN / NƯỚC
    // ─────────────────────────────────────────────────────────────────────────

    /** Đảm bảo bảng CauHinh tồn tại (tự tạo nếu chưa có) */
    private void ensureConfigTable() {
        String sql =
            "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name='CauHinh') " +
            "CREATE TABLE CauHinh (" +
            "  TenDN   VARCHAR(50) NOT NULL PRIMARY KEY, " +
            "  GiaDien FLOAT       NOT NULL DEFAULT 3500, " +
            "  GiaNuoc FLOAT       NOT NULL DEFAULT 15000)";
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println("[CauHinh] ensureConfigTable: " + e.getMessage());
        }
    }

    /** Lấy giá điện (VNĐ/kWh) của chủ trọ hiện tại */
    public double getGiaDien() {
        ensureConfigTable();
        String sql = "SELECT GiaDien FROM CauHinh WHERE TenDN = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (Exception e) {
            System.err.println("[CauHinh] getGiaDien: " + e.getMessage());
        }
        return DEFAULT_GIA_DIEN;
    }

    /** Lấy giá nước (VNĐ/m³) của chủ trọ hiện tại */
    public double getGiaNuoc() {
        ensureConfigTable();
        String sql = "SELECT GiaNuoc FROM CauHinh WHERE TenDN = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (Exception e) {
            System.err.println("[CauHinh] getGiaNuoc: " + e.getMessage());
        }
        return DEFAULT_GIA_NUOC;
    }

    /** Lưu giá điện + giá nước cho chủ trọ hiện tại (UPSERT) */
    public boolean saveGia(double giaDien, double giaNuoc) {
        ensureConfigTable();
        String sql =
            "MERGE CauHinh AS target " +
            "USING (SELECT ? AS TenDN, ? AS GiaDien, ? AS GiaNuoc) AS src " +
            "  ON target.TenDN = src.TenDN " +
            "WHEN MATCHED THEN " +
            "  UPDATE SET GiaDien = src.GiaDien, GiaNuoc = src.GiaNuoc " +
            "WHEN NOT MATCHED THEN " +
            "  INSERT (TenDN, GiaDien, GiaNuoc) VALUES (src.TenDN, src.GiaDien, src.GiaNuoc);";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            ps.setDouble(2, giaDien);
            ps.setDouble(3, giaNuoc);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("[CauHinh] saveGia: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PHẦN ĐIỆN NƯỚC
    // ─────────────────────────────────────────────────────────────────────────

    // Chuẩn hóa "5/2026" → "05/2026"
    private String normalize(String s) {
        if (s == null) return "";
        String[] p = s.trim().split("/");
        if (p.length != 2) return s.trim();
        try {
            return String.format("%02d/%d",
                    Integer.parseInt(p[0].trim()), Integer.parseInt(p[1].trim()));
        } catch (Exception e) { return s.trim(); }
    }

    private static final String NORM =
        "CASE WHEN LEN(LEFT(ThangNam,CHARINDEX('/',ThangNam)-1))=1 " +
        "     THEN '0'+ThangNam ELSE ThangNam END";

    private static String normDn(String alias) {
        String c = alias + ".ThangNam";
        return "CASE WHEN LEN(LEFT(" + c + ",CHARINDEX('/'," + c + ")-1))=1 " +
               "     THEN '0'+" + c + " ELSE " + c + " END";
    }

    // 1. Danh sách tháng/năm
    public List<String> getDanhSachThangNam() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT DISTINCT " + NORM + " AS TN FROM DienNuoc WHERE TenDN = ? ORDER BY TN DESC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(rs.getString(1));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 2. Lấy theo tháng/năm
    public List<DienNuoc> getTheoThangNam(String thangNam) {
        List<DienNuoc> list = new ArrayList<>();
        String sql = "SELECT dn.* FROM DienNuoc dn " +
                     "WHERE dn.TenDN = ? AND " + NORM + " = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            ps.setString(2, normalize(thangNam));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DienNuoc dn = new DienNuoc();
                    dn.setId(rs.getInt("ID"));
                    dn.setTenPhong(rs.getString("MaPhong"));
                    dn.setThangNam(normalize(rs.getString("ThangNam")));
                    dn.setDienDau(rs.getInt("DienDau"));
                    dn.setDienCuoi(rs.getInt("DienCuoi"));
                    dn.setNuocDau(rs.getInt("NuocDau"));
                    dn.setNuocCuoi(rs.getInt("NuocCuoi"));
                    dn.setTienDien(rs.getDouble("TienDien"));
                    dn.setTienNuoc(rs.getDouble("TienNuoc"));
                    list.add(dn);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // 3. Xóa
    public boolean delete(int id) {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM DienNuoc WHERE ID = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 4. Cập nhật số điện nước → tính tiền → đồng bộ HoaDon
    public boolean update(int id, int soDienDung, int soNuocDung) {
        lastSyncRows = -1;
        int dienDau = 0, nuocDau = 0;
        String thangNam = "", maPhong = "";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT DienDau, NuocDau, ThangNam, MaPhong FROM DienNuoc WHERE ID=?")) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) { System.out.println("==> [update] ID=" + id + " không tìm thấy"); return false; }
            dienDau  = rs.getInt("DienDau");
            nuocDau  = rs.getInt("NuocDau");
            thangNam = normalize(rs.getString("ThangNam"));
            maPhong  = rs.getString("MaPhong");
        } catch (Exception e) { e.printStackTrace(); return false; }

        int    dienCuoi = dienDau + soDienDung;
        int    nuocCuoi = nuocDau + soNuocDung;
        double tienDien = soDienDung * getGiaDien();
        double tienNuoc = soNuocDung * getGiaNuoc();

        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "UPDATE DienNuoc SET DienCuoi=?,NuocCuoi=?,TienDien=?,TienNuoc=?,ThangNam=? WHERE ID=?")) {
            ps.setInt(1, dienCuoi); ps.setInt(2, nuocCuoi);
            ps.setDouble(3, tienDien); ps.setDouble(4, tienNuoc);
            ps.setString(5, thangNam); ps.setInt(6, id);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                lastSyncRows = syncHoaDonByRoom(thangNam);
                System.out.println("==> [update] synced " + lastSyncRows + " HoaDon.");
            }
            return rows > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // 5. Đồng bộ toàn bộ tháng sang HoaDon
    public boolean dongBoSangHoaDon(int thang, int nam) {
        String thangNam = String.format("%02d/%d", thang, nam);
        System.out.println("==> [dongBo] " + thangNam);
        fixThangNamFormat();
        autoCalculateTien(thangNam);
        int rows = syncHoaDonByRoom(thangNam);
        System.out.println("==> [dongBo] Cập nhật " + rows + " HoaDon.");
        return rows > 0;
    }

    // Tự tính TienDien/TienNuoc khi NULL
    private void autoCalculateTien(String thangNam) {
        String sql =
            "UPDATE DienNuoc SET " +
            "  TienDien = (DienCuoi - DienDau) * ?, " +
            "  TienNuoc = (NuocCuoi - NuocDau) * ? " +
            "WHERE TenDN = ? AND " + NORM + " = ? AND (TienDien IS NULL OR TienNuoc IS NULL)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, getGiaDien());
            ps.setDouble(2, getGiaNuoc());
            ps.setString(3, Session.getTenDN());
            ps.setString(4, thangNam);
            int r = ps.executeUpdate();
            if (r > 0) System.out.println("==> autoCalculateTien: " + r + " phòng.");
        } catch (Exception e) {
            System.out.println("==> autoCalculateTien LỖI: " + e.getMessage());
        }
    }

    // Sync HoaDon "Chưa thu" – dùng VARBINARY bypass collation
    private int syncHoaDonByRoom(String thangNam) {
        String sql =
            "UPDATE HoaDon SET " +
            "  TienDien = ISNULL((" +
            "      SELECT TOP 1 dn.TienDien FROM DienNuoc dn " +
            "      WHERE LTRIM(RTRIM(dn.MaPhong)) = LTRIM(RTRIM(HoaDon.TenPhong)) " +
            "        AND dn.TenDN = ? AND " + normDn("dn") + " = ?" +
            "  ), 0), " +
            "  TienNuoc = ISNULL((" +
            "      SELECT TOP 1 dn.TienNuoc FROM DienNuoc dn " +
            "      WHERE LTRIM(RTRIM(dn.MaPhong)) = LTRIM(RTRIM(HoaDon.TenPhong)) " +
            "        AND dn.TenDN = ? AND " + normDn("dn") + " = ?" +
            "  ), 0), " +
            "  TienDichVu = ISNULL((" +
            "      SELECT TOP 1 TongTienDV FROM DichVu " +
            "      WHERE LTRIM(RTRIM(DichVu.MaPhong)) = LTRIM(RTRIM(HoaDon.TenPhong)) AND TenDN = ?" +
            "  ), 0) " +
            "WHERE TenDN = ? AND CONVERT(VARBINARY(100), TrangThai) = ? " +
            "  AND EXISTS (" +
            "      SELECT 1 FROM DienNuoc dn " +
            "      WHERE LTRIM(RTRIM(dn.MaPhong)) = LTRIM(RTRIM(HoaDon.TenPhong)) " +
            "        AND dn.TenDN = ? AND " + normDn("dn") + " = ?" +
            "  )";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            ps.setString(2, thangNam);
            ps.setString(3, Session.getTenDN());
            ps.setString(4, thangNam);
            ps.setString(5, Session.getTenDN());
            ps.setString(6, Session.getTenDN());
            ps.setBytes(7, BYTES_CHUA_THU);
            ps.setString(8, Session.getTenDN());
            ps.setString(9, thangNam);
            int rows = ps.executeUpdate();
            System.out.println("==> syncHoaDonByRoom [" + thangNam + "]: " + rows + " rows.");
            return rows;
        } catch (Exception e) {
            System.out.println("==> syncHoaDonByRoom LỖI: " + e.getMessage());
            e.printStackTrace(); return 0;
        }
    }

    // Chuyển tháng mới
    public boolean chuyenThangMoi(String thangNamMoi, String thangNamCu) {
        String sql =
            "INSERT INTO DienNuoc (MaPhong, ThangNam, DienDau, DienCuoi, NuocDau, NuocCuoi, TenDN) " +
            "SELECT MaPhong, ?, DienCuoi, DienCuoi, NuocCuoi, NuocCuoi, TenDN " +
            "FROM DienNuoc WHERE TenDN = ? AND " + NORM + " = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, normalize(thangNamMoi));
            ps.setString(2, Session.getTenDN());
            ps.setString(3, normalize(thangNamCu));
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // Chuẩn hóa ThangNam trong DB
    public void fixThangNamFormat() {
        String sql =
            "UPDATE DienNuoc " +
            "SET ThangNam = RIGHT('0'+LEFT(ThangNam,CHARINDEX('/',ThangNam)-1),2)" +
            "             + SUBSTRING(ThangNam,CHARINDEX('/',ThangNam),LEN(ThangNam)) " +
            "WHERE LEN(LEFT(ThangNam,CHARINDEX('/',ThangNam)-1)) = 1";
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement()) {
            int r = st.executeUpdate(sql);
            if (r > 0) System.out.println("==> fixThangNam: " + r + " bản ghi.");
        } catch (Exception e) { System.out.println("==> fixThangNam LỖI: " + e.getMessage()); }
    }
}
