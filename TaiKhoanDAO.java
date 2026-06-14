package dao;

import model.TaiKhoan;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaiKhoanDAO {

    // ─── Lấy toàn bộ danh sách tài khoản ────────────────────────────────────

    public List<TaiKhoan> getAll() {
        List<TaiKhoan> list = new ArrayList<>();
        String sql = "SELECT TenDN, MatKhau, Quyen, Email FROM TaiKhoan";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                TaiKhoan t = new TaiKhoan();
                t.setTenDangNhap(rs.getString("TenDN"));
                t.setMatKhau(rs.getString("MatKhau"));
                t.setVaiTro(rs.getString("Quyen"));
                t.setEmail(rs.getString("Email"));
                list.add(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ─── Tìm theo tên đăng nhập (khóa chính) ────────────────────────────────

    public TaiKhoan findById(String tenDN) {
        String sql = "SELECT TenDN, MatKhau, Quyen, Email FROM TaiKhoan WHERE TenDN = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return null;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tenDN);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TaiKhoan t = new TaiKhoan();
                    t.setTenDangNhap(rs.getString("TenDN"));
                    t.setMatKhau(rs.getString("MatKhau"));
                    t.setVaiTro(rs.getString("Quyen"));
                    t.setEmail(rs.getString("Email"));
                    return t;
                }
            }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // ─── Xác thực đăng nhập ─────────────────────────────────────────────────

    public TaiKhoan authenticate(String username, String password) throws SQLException {
        String sql = "SELECT TenDN, MatKhau, Quyen, Email FROM TaiKhoan WHERE TenDN = ? AND MatKhau = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TaiKhoan t = new TaiKhoan();
                    t.setTenDangNhap(rs.getString("TenDN"));
                    t.setMatKhau(rs.getString("MatKhau"));
                    t.setVaiTro(rs.getString("Quyen"));
                    t.setEmail(rs.getString("Email"));
                    return t;
                }
            }
        }
        return null;
    }

    // ─── Kiểm tra tên đăng nhập đã tồn tại chưa ────────────────────────────

    public boolean existsByUsername(String tenDN) {
        String sql = "SELECT 1 FROM TaiKhoan WHERE TenDN = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tenDN);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ─── Đăng ký tài khoản mới ──────────────────────────────────────────────

    /**
     * Đăng ký tài khoản mới với vai trò "Chủ trọ" hoặc "Khách thuê".
     * Trả về:
     *   0 – thành công
     *  -1 – tên đăng nhập đã tồn tại
     *  -2 – lỗi SQL
     */
    public int register(TaiKhoan t) {
        if (existsByUsername(t.getTenDangNhap())) {
            return -1; // username đã tồn tại
        }
        String sql = "INSERT INTO TaiKhoan (TenDN, MatKhau, Quyen, Email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getTenDangNhap());
            ps.setString(2, t.getMatKhau());
            ps.setString(3, t.getVaiTro());
            ps.setString(4, t.getEmail());
            return ps.executeUpdate() > 0 ? 0 : -2;
        } catch (SQLException e) {
            e.printStackTrace();
            return -2;
        }
    }

    // ─── Thêm tài khoản (dùng nội bộ / admin) ──────────────────────────────

    public boolean insert(TaiKhoan t) {
        String sql = "INSERT INTO TaiKhoan (TenDN, MatKhau, Quyen, Email) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getTenDangNhap());
            ps.setString(2, t.getMatKhau());
            ps.setString(3, t.getVaiTro());
            ps.setString(4, t.getEmail());
            return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─── Cập nhật tài khoản ─────────────────────────────────────────────────

    public boolean update(TaiKhoan t) {
        String sql = "UPDATE TaiKhoan SET MatKhau = ?, Quyen = ?, Email = ? WHERE TenDN = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn == null) return false;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, t.getMatKhau());
            ps.setString(2, t.getVaiTro());
            ps.setString(3, t.getEmail());
            ps.setString(4, t.getTenDangNhap());
            return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ─── Xóa tài khoản ──────────────────────────────────────────────────────

    public boolean delete(String tenDN) {
        String sql = "DELETE FROM TaiKhoan WHERE TenDN = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tenDN);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getUnlinkedKhachThueAccounts(String currentLinkedUser) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT TenDN FROM TaiKhoan " +
                     "WHERE Quyen = N'Khách thuê' " +
                     "AND (TenDN NOT IN (SELECT DISTINCT TenDN FROM KhachThue WHERE TenDN IS NOT NULL AND TenDN <> ''))";
        if (currentLinkedUser != null && !currentLinkedUser.isEmpty()) {
            sql += " OR TenDN = ?";
        }
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            if (currentLinkedUser != null && !currentLinkedUser.isEmpty()) {
                ps.setString(1, currentLinkedUser);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(rs.getString("TenDN"));
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }
}