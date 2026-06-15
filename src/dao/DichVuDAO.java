package dao;

import model.DichVu;
import util.DatabaseConnection;
import util.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DichVuDAO {

    // ── Lấy dịch vụ của chủ trọ đang đăng nhập ─────────────────────────────────
    public List<DichVu> getAll() {
        List<DichVu> list = new ArrayList<>();
        String sql = "SELECT dv.*, p.TrangThai FROM DichVu dv " +
                     "LEFT JOIN PhongTro p ON LTRIM(RTRIM(p.MaPhong)) = LTRIM(RTRIM(dv.MaPhong)) AND p.TenDN = dv.TenDN " +
                     "WHERE dv.TenDN = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    DichVu dv = new DichVu();
                    dv.setId(rs.getInt("MaDV"));
                    dv.setMaPhong(rs.getString("MaPhong"));
                    dv.setWifi(rs.getDouble("Wifi"));
                    dv.setThangMay(rs.getDouble("ThangMay"));
                    dv.setGiuXe(rs.getDouble("GiuXe"));
                    dv.setPcccVeSinh(rs.getDouble("PCCC_VeSinh"));
                    dv.setTongTien(rs.getDouble("TongTienDV"));
                    String status = rs.getString("TrangThai");
                    dv.setGhiChu(status != null ? status : "Trống");
                    list.add(dv);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── Thêm mới ───────────────────────────────────────────────────
    public boolean add(DichVu dv) {
        String sql = "INSERT INTO DichVu (MaPhong, Wifi, ThangMay, GiuXe, PCCC_VeSinh, TenDN) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, dv.getMaPhong());
            ps.setDouble(2, dv.getWifi());
            ps.setDouble(3, dv.getThangMay());
            ps.setDouble(4, dv.getGiuXe());
            ps.setDouble(5, dv.getPcccVeSinh());
            ps.setString(6, Session.getTenDN());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── Cập nhật ─────────────────────────────────────────────────────────────
    public boolean update(DichVu dv) {
        String sql = "UPDATE DichVu SET Wifi=?, ThangMay=?, GiuXe=?, PCCC_VeSinh=? WHERE MaDV=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setDouble(1, dv.getWifi());
            ps.setDouble(2, dv.getThangMay());
            ps.setDouble(3, dv.getGiuXe());
            ps.setDouble(4, dv.getPcccVeSinh());
            ps.setInt(5, dv.getId());
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    // ── Xóa ─────────────────────────────────────────────────────────────────
    public boolean delete(int id) {
        String sql = "DELETE FROM DichVu WHERE MaDV=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }
}