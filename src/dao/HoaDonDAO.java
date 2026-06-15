package dao;

import model.HoaDon;
import util.DatabaseConnection;
import util.Session;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public class HoaDonDAO {

    // ── 1. Lấy toàn bộ hóa đơn của chủ trọ ─────────────────────────────────
    public List<HoaDon> getAll() {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT MaHoaDon, TenPhong, NgayLap, TienPhong, TienDien, TienNuoc, TienDichVu, TrangThai " +
                     "FROM HoaDon WHERE TenDN = ? ORDER BY NgayLap DESC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── 2. Lọc theo tháng/năm ────────────────────────────────────────────────
    public List<HoaDon> getHoaDonTheoThang(int month, int year) {
        List<HoaDon> list = new ArrayList<>();
        String sql = "SELECT MaHoaDon, TenPhong, NgayLap, TienPhong, TienDien, TienNuoc, TienDichVu, TrangThai " +
                     "FROM HoaDon WHERE TenDN = ? AND MONTH(NgayLap) = ? AND YEAR(NgayLap) = ? " +
                     "ORDER BY NgayLap DESC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            ps.setInt(2, month);
            ps.setInt(3, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── 3. Cập nhật trạng thái (thu tiền / hoàn trả) ────────────────────────
    public boolean updateStatus(int maHoaDon, String trangThai) {
        String sql = "UPDATE HoaDon SET TrangThai = ? WHERE MaHoaDon = ? AND TenDN = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setNString(1, trangThai);
            ps.setInt(2, maHoaDon);
            ps.setString(3, Session.getTenDN());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── 4. Tạo hóa đơn tháng tiếp theo ─────────────────────────────────────
    public int autoGenerateNextMonthInvoices() {
        LocalDate next = LocalDate.now().plusMonths(1);
        int month = next.getMonthValue();
        int year  = next.getYear();
        java.sql.Date billingDate = java.sql.Date.valueOf(next.withDayOfMonth(1));
        String sql = "INSERT INTO HoaDon (TenPhong, NgayLap, TienPhong, TienDien, TienNuoc, TienDichVu, TrangThai, TenDN) " +
                     "SELECT p.MaPhong, ?, p.GiaPhong, 0, 0, 0, N'Chưa thu', ? " +
                     "FROM PhongTro p " +
                     "WHERE p.TenDN = ? AND p.TrangThai = N'Đang thuê' " +
                     "AND NOT EXISTS ( " +
                     "    SELECT 1 FROM HoaDon hd " +
                     "    WHERE LTRIM(RTRIM(hd.TenPhong)) = LTRIM(RTRIM(p.MaPhong)) " +
                     "    AND hd.TenDN = p.TenDN " +
                     "    AND MONTH(hd.NgayLap) = ? AND YEAR(hd.NgayLap) = ? " +
                     ")";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, billingDate);
            ps.setString(2, Session.getTenDN());
            ps.setString(3, Session.getTenDN());
            ps.setInt(4, month);
            ps.setInt(5, year);
            return ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    // ── 5. Đồng bộ từ DienNuoc ───────────────────────────────────────────────
    public boolean syncFromDienNuoc(int month, int year) {
        String thangNamStr = String.format("%02d/%d", month, year);
        String thangNamAlt = month + "/" + year;
        ensureHoaDonExists(thangNamStr, thangNamAlt, month, year);

        String sql = "UPDATE HoaDon SET " +
                     "TienDien = ISNULL((SELECT TOP 1 dn.TienDien FROM DienNuoc dn " +
                     "    WHERE LTRIM(RTRIM(dn.MaPhong)) = LTRIM(RTRIM(HoaDon.TenPhong)) " +
                     "    AND dn.TenDN = HoaDon.TenDN " +
                     "    AND (dn.ThangNam = ? OR dn.ThangNam = ?)), 0), " +
                     "TienNuoc = ISNULL((SELECT TOP 1 dn.TienNuoc FROM DienNuoc dn " +
                     "    WHERE LTRIM(RTRIM(dn.MaPhong)) = LTRIM(RTRIM(HoaDon.TenPhong)) " +
                     "    AND dn.TenDN = HoaDon.TenDN " +
                     "    AND (dn.ThangNam = ? OR dn.ThangNam = ?)), 0), " +
                     "TienDichVu = ISNULL((SELECT TOP 1 TongTienDV FROM DichVu " +
                     "    WHERE LTRIM(RTRIM(DichVu.MaPhong)) = LTRIM(RTRIM(HoaDon.TenPhong)) " +
                     "    AND DichVu.TenDN = HoaDon.TenDN), 0) " +
                     "WHERE HoaDon.TenDN = ? " +
                     "AND MONTH(HoaDon.NgayLap) = ? AND YEAR(HoaDon.NgayLap) = ? " +
                     "AND CONVERT(VARBINARY(100), HoaDon.TrangThai) = ?";

        byte[] chuaThuBytes = "Chưa thu".getBytes(java.nio.charset.StandardCharsets.UTF_16LE);
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, thangNamStr);
            ps.setString(2, thangNamAlt);
            ps.setString(3, thangNamStr);
            ps.setString(4, thangNamAlt);
            ps.setString(5, Session.getTenDN());
            ps.setInt(6, month);
            ps.setInt(7, year);
            ps.setBytes(8, chuaThuBytes);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private void ensureHoaDonExists(String thangNam, String thangNamAlt, int month, int year) {
        String sql = "INSERT INTO HoaDon (TenPhong, NgayLap, TienPhong, TienDien, TienNuoc, TienDichVu, TrangThai, TenDN) " +
                     "SELECT dn.MaPhong, ?, ISNULL(p.GiaPhong, 0), 0, 0, 0, N'Chưa thu', ? " +
                     "FROM DienNuoc dn " +
                     "JOIN PhongTro p ON LTRIM(RTRIM(p.MaPhong)) = LTRIM(RTRIM(dn.MaPhong)) AND p.TenDN = dn.TenDN " +
                     "WHERE dn.TenDN = ? " +
                     "AND (dn.ThangNam = ? OR dn.ThangNam = ?) " +
                     "AND NOT EXISTS ( " +
                     "    SELECT 1 FROM HoaDon hd " +
                     "    WHERE LTRIM(RTRIM(hd.TenPhong)) = LTRIM(RTRIM(dn.MaPhong)) " +
                     "    AND hd.TenDN = dn.TenDN " +
                     "    AND MONTH(hd.NgayLap) = ? AND YEAR(hd.NgayLap) = ? " +
                     ")";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(LocalDate.of(year, month, 1)));
            ps.setString(2, Session.getTenDN());
            ps.setString(3, Session.getTenDN());
            ps.setString(4, thangNam);
            ps.setString(5, thangNamAlt);
            ps.setInt(6, month);
            ps.setInt(7, year);
            ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Doanh thu tháng này ───────────────────────────────────────────────────
    public double getDoanhThuThangNay() {
        String sql = "SELECT SUM(TienPhong + TienDien + TienNuoc + TienDichVu) " +
                     "FROM HoaDon WHERE TenDN = ? " +
                     "AND MONTH(NgayLap)=MONTH(GETDATE()) AND YEAR(NgayLap)=YEAR(GETDATE())";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (Exception e) { e.printStackTrace(); }
        return 0;
    }

    // ── Doanh thu 3 tháng gần nhất ───────────────────────────────────────────
    public Map<String, Double> getDoanhThu3Thang() {
        Map<String, Double> map = new LinkedHashMap<>();
        String sql = "SELECT TOP 3 " +
                     "(RIGHT('0'+CAST(MONTH(NgayLap) AS VARCHAR),2)+'/'+CAST(YEAR(NgayLap) AS VARCHAR)) AS ThangNam, " +
                     "SUM(TienPhong+TienDien+TienNuoc+TienDichVu) AS Tong " +
                     "FROM HoaDon WHERE TenDN = ? " +
                     "GROUP BY YEAR(NgayLap), MONTH(NgayLap) " +
                     "ORDER BY YEAR(NgayLap) DESC, MONTH(NgayLap) DESC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) map.put(rs.getString("ThangNam"), rs.getDouble("Tong"));
        } catch (Exception e) { e.printStackTrace(); }
        return map;
    }

    // ── Xóa hóa đơn ─────────────────────────────────────────────────────────
    public boolean delete(int maHoaDon) {
        String sql = "DELETE FROM HoaDon WHERE MaHoaDon = ? AND TenDN = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maHoaDon);
            ps.setString(2, Session.getTenDN());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { return false; }
    }

    // ── Thêm thủ công hóa đơn cho 1 phòng ───────────────────────────────────
    public boolean addManualInvoice(String maPhong) {
        String sql = "INSERT INTO HoaDon (TenPhong, NgayLap, TienPhong, TienDien, TienNuoc, TienDichVu, TrangThai, TenDN) " +
                     "SELECT TOP 1 p.MaPhong, GETDATE(), p.GiaPhong, 0, 0, 0, N'Chưa thu', ? " +
                     "FROM PhongTro p " +
                     "WHERE LTRIM(RTRIM(p.MaPhong)) = LTRIM(RTRIM(?)) AND p.TenDN = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            ps.setString(2, maPhong);
            ps.setString(3, Session.getTenDN());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── Tạo hóa đơn tháng này cho tất cả phòng đang thuê ────────────────────
    public int autoGenerateMonthlyInvoices() {
        int month = LocalDate.now().getMonthValue();
        int year  = LocalDate.now().getYear();
        java.sql.Date billingDate = java.sql.Date.valueOf(LocalDate.now().withDayOfMonth(1));
        String sql = "INSERT INTO HoaDon (TenPhong, NgayLap, TienPhong, TienDien, TienNuoc, TienDichVu, TrangThai, TenDN) " +
                     "SELECT p.MaPhong, ?, p.GiaPhong, 0, 0, 0, N'Chưa thu', ? " +
                     "FROM PhongTro p " +
                     "WHERE p.TenDN = ? AND p.TrangThai = N'Đang thuê' " +
                     "AND NOT EXISTS ( " +
                     "    SELECT 1 FROM HoaDon hd " +
                     "    WHERE LTRIM(RTRIM(hd.TenPhong)) = LTRIM(RTRIM(p.MaPhong)) " +
                     "    AND hd.TenDN = p.TenDN " +
                     "    AND MONTH(hd.NgayLap) = ? AND YEAR(hd.NgayLap) = ? " +
                     ")";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDate(1, billingDate);
            ps.setString(2, Session.getTenDN());
            ps.setString(3, Session.getTenDN());
            ps.setInt(4, month);
            ps.setInt(5, year);
            return ps.executeUpdate();
        } catch (Exception e) { e.printStackTrace(); return 0; }
    }

    private HoaDon mapRow(ResultSet rs) throws SQLException {
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(rs.getInt("MaHoaDon"));
        hd.setTenPhong(rs.getString("TenPhong"));
        Date d = rs.getDate("NgayLap");
        if (d != null) hd.setNgayLap(d.toLocalDate());
        hd.setTienPhong(rs.getDouble("TienPhong"));
        hd.setTienDien(rs.getDouble("TienDien"));
        hd.setTienNuoc(rs.getDouble("TienNuoc"));
        hd.setTienDichVu(rs.getDouble("TienDichVu"));
        hd.setTrangThai(rs.getString("TrangThai"));
        return hd;
    }
}
