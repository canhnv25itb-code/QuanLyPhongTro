package dao;

import model.PhongTro;
import util.DatabaseConnection;
import util.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PhongTroDAO {

    /** Lấy tất cả phòng của chủ trọ đang đăng nhập */
    public List<PhongTro> getAll() {
        List<PhongTro> list = new ArrayList<>();
        String sql = "SELECT MaPhong, Tang, TrangThai, GiaPhong, DienTich FROM PhongTro " +
                     "WHERE TenDN = ? ORDER BY MaPhong ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new PhongTro(
                        rs.getString("MaPhong"),
                        rs.getInt("Tang"),
                        rs.getString("TrangThai"),
                        rs.getDouble("GiaPhong"),
                        rs.getDouble("DienTich")
                    ));
                }
            }
        } catch (SQLException e) {
            System.out.println("LỖI getAll PhongTro: " + e.getMessage());
        }
        return list;
    }

    /** Đếm tổng số phòng của chủ trọ hiện tại */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM PhongTro WHERE TenDN = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    /** Đếm theo trạng thái của chủ trọ hiện tại */
    public int countByStatus(String status) {
        String sql = "SELECT COUNT(*) FROM PhongTro WHERE TrangThai = ? AND TenDN = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, Session.getTenDN());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    /** Thêm phòng mới – tự gán TenDN theo Session */
    public boolean insert(PhongTro p) throws SQLException {
        String sql = "INSERT INTO PhongTro (MaPhong, Tang, TrangThai, GiaPhong, DienTich, TenDN) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getMaPhong());
            ps.setInt(2, p.getTang());
            ps.setString(3, p.getTrangThai());
            ps.setDouble(4, p.getGiaPhong());
            ps.setDouble(5, p.getDienTich());
            ps.setString(6, Session.getTenDN());
            System.out.println("[Insert] MaPhong=" + p.getMaPhong() + " TenDN=" + Session.getTenDN());
            return ps.executeUpdate() > 0;
        }
        // Không bắt ngoại lệ ở đây – để UI hiển thị đúng lý do lỗi
    }

    /** Cập nhật trạng thái phòng */
    public boolean updateTrangThai(String maPhong, String trangThai) {
        String sql = "UPDATE PhongTro SET TrangThai = ? WHERE MaPhong = ? AND TenDN = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setString(2, maPhong);
            ps.setString(3, Session.getTenDN());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("LỖI updateTrangThai: " + e.getMessage());
            return false;
        }
    }

    /** Cập nhật thông tin phòng */
    public boolean update(PhongTro p) {
        String sql = "UPDATE PhongTro SET Tang=?, TrangThai=?, GiaPhong=?, DienTich=? " +
                     "WHERE MaPhong=? AND TenDN=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getTang());
            ps.setString(2, p.getTrangThai());
            ps.setDouble(3, p.getGiaPhong());
            ps.setDouble(4, p.getDienTich());
            ps.setString(5, p.getMaPhong());
            ps.setString(6, Session.getTenDN());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace(); return false;
        }
    }

    /** Xóa phòng (chỉ xóa được phòng của mình) */
    public boolean delete(String maPhong) {
        String sql = "DELETE FROM PhongTro WHERE MaPhong = ? AND TenDN = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            ps.setString(2, Session.getTenDN());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace(); return false;
        }
    }
}