package dao;

import util.DatabaseConnection;
import util.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ThongKeDAO {
    private static final Logger LOGGER = Logger.getLogger(ThongKeDAO.class.getName());

    // ── 1. Tổng doanh thu đã thu của chủ trọ ────────────────────────────────
    public double getTongDoanhThu() {
        String sql = "SELECT SUM(ISNULL(TienPhong,0)+ISNULL(TienDien,0)+ISNULL(TienNuoc,0)+ISNULL(TienDichVu,0)) " +
                     "FROM HoaDon WHERE TenDN = ? AND CONVERT(VARBINARY(100), TrangThai) = ?";
        byte[] daThuBytes = "Đã thu".getBytes(java.nio.charset.StandardCharsets.UTF_16LE);
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            ps.setBytes(2, daThuBytes);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Lỗi getTongDoanhThu", e); }
        return 0;
    }

    // ── 2. Doanh thu theo tháng ──────────────────────────────────────────────
    public List<Double> getDoanhThuTheoThang(int nam) {
        List<Double> result = new ArrayList<>(Collections.nCopies(12, 0.0));
        String sql = "SELECT MONTH(NgayLap) AS thang, " +
                     "SUM(ISNULL(TienPhong,0)+ISNULL(TienDien,0)+ISNULL(TienNuoc,0)+ISNULL(TienDichVu,0)) AS tong " +
                     "FROM HoaDon WHERE TenDN = ? AND YEAR(NgayLap) = ? " +
                     "AND CONVERT(VARBINARY(100), TrangThai) = ? " +
                     "GROUP BY MONTH(NgayLap)";
        byte[] daThuBytes = "Đã thu".getBytes(java.nio.charset.StandardCharsets.UTF_16LE);
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            ps.setInt(2, nam);
            ps.setBytes(3, daThuBytes);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int thang = rs.getInt("thang");
                    if (thang >= 1 && thang <= 12) result.set(thang - 1, rs.getDouble("tong"));
                }
            }
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Lỗi getDoanhThuTheoThang", e); }
        return result;
    }

    // ── 3. Số khách thuê của chủ trọ ────────────────────────────────────────
    public int getSoKhachThue() {
        String sql = "SELECT COUNT(*) FROM KhachThue WHERE MaChuTro = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Lỗi getSoKhachThue", e); }
        return 0;
    }

    // ── 4. Số phòng trống của chủ trọ ───────────────────────────────────────
    public int getSoPhongTrong() {
        String sql = "SELECT COUNT(*) FROM PhongTro WHERE TenDN = ? AND TrangThai = N'Trống'";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Lỗi getSoPhongTrong", e); }
        return 0;
    }

    // ── 5. Số hợp đồng đang hoạt động ───────────────────────────────────────
    public int getSoHopDongDangHoatDong() {
        String sql = "SELECT COUNT(*) FROM HopDong WHERE TenDN = ? AND TrangThai = N'Đang hiệu lực'";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { LOGGER.log(Level.SEVERE, "Lỗi getSoHopDongDangHoatDong", e); }
        return 0;
    }
}