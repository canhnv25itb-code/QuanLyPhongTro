package dao;

import model.HopDong;
import util.DatabaseConnection;
import util.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HopDongDAO {

    // ── Lấy hợp đồng của chủ trọ đang đăng nhập ─────────────────────────────
    public List<HopDong> getAll() {
        List<HopDong> list = new ArrayList<>();
        String sql = "SELECT MaHD, TenKhachHang, MaPhong, NgayBatDau, NgayKetThuc, TienCoc, TrangThai " +
                     "FROM HopDong WHERE TenDN = ? ORDER BY MaHD DESC";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (Exception e) {
            System.err.println("LỖI HopDongDAO.getAll(): " + e.getMessage());
        }
        return list;
    }

    // ── Thêm hợp đồng mới ────────────────────────────────────────────────────
    public boolean them(HopDong hd) {
        String sql = "INSERT INTO HopDong (TenKhachHang, MaPhong, NgayBatDau, NgayKetThuc, TienCoc, TrangThai, TenDN) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hd.getTenKhach());
            ps.setString(2, hd.getMaPhong());
            ps.setDate(3, hd.getNgayBatDau() != null ? Date.valueOf(hd.getNgayBatDau()) : null);
            ps.setDate(4, hd.getNgayKetThuc() != null ? Date.valueOf(hd.getNgayKetThuc()) : null);
            ps.setDouble(5, hd.getTienCoc());
            ps.setString(6, hd.getTrangThai() != null ? hd.getTrangThai() : "Đang hiệu lực");
            ps.setString(7, Session.getTenDN()); // gán chủ trọ
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("LỖI HopDongDAO.them(): " + e.getMessage());
            return false;
        }
    }

    // ── Cập nhật hợp đồng ────────────────────────────────────────────────────
    public boolean sua(HopDong hd) {
        String sql = "UPDATE HopDong SET TenKhachHang=?, MaPhong=?, NgayBatDau=?, " +
                     "NgayKetThuc=?, TienCoc=?, TrangThai=? WHERE MaHD=? AND TenDN=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, hd.getTenKhach());
            ps.setString(2, hd.getMaPhong());
            ps.setDate(3, hd.getNgayBatDau() != null ? Date.valueOf(hd.getNgayBatDau()) : null);
            ps.setDate(4, hd.getNgayKetThuc() != null ? Date.valueOf(hd.getNgayKetThuc()) : null);
            ps.setDouble(5, hd.getTienCoc());
            ps.setString(6, hd.getTrangThai());
            ps.setInt(7, hd.getMaHopDong());
            ps.setString(8, Session.getTenDN());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("LỖI HopDongDAO.sua(): " + e.getMessage());
            return false;
        }
    }

    // ── Xóa hợp đồng ─────────────────────────────────────────────────────────
    public boolean xoa(int maHD) {
        String sql = "DELETE FROM HopDong WHERE MaHD = ? AND TenDN = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maHD);
            ps.setString(2, Session.getTenDN());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("LỖI HopDongDAO.xoa(): " + e.getMessage());
            return false;
        }
    }

    // ── Hủy hợp đồng (đổi trạng thái) ───────────────────────────────────────
    public boolean huyHopDong(int maHD) {
        String sql = "UPDATE HopDong SET TrangThai = N'Đã hủy' WHERE MaHD = ? AND TenDN = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maHD);
            ps.setString(2, Session.getTenDN());
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("LỖI HopDongDAO.huyHopDong(): " + e.getMessage());
            return false;
        }
    }

    // ── Lấy theo MaHD ────────────────────────────────────────────────────────
    public HopDong getById(int maHD) {
        String sql = "SELECT MaHD, TenKhachHang, MaPhong, NgayBatDau, NgayKetThuc, TienCoc, TrangThai " +
                     "FROM HopDong WHERE MaHD = ? AND TenDN = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, maHD);
            ps.setString(2, Session.getTenDN());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (Exception e) {
            System.err.println("LỖI HopDongDAO.getById(): " + e.getMessage());
        }
        return null;
    }

    private HopDong mapRow(ResultSet rs) throws SQLException {
        HopDong hd = new HopDong();
        hd.setMaHopDong(rs.getInt("MaHD"));
        hd.setTenKhach(rs.getString("TenKhachHang"));
        hd.setMaPhong(rs.getString("MaPhong"));
        Date d1 = rs.getDate("NgayBatDau");
        if (d1 != null) hd.setNgayBatDau(d1.toLocalDate());
        Date d2 = rs.getDate("NgayKetThuc");
        if (d2 != null) hd.setNgayKetThuc(d2.toLocalDate());
        hd.setTienCoc(rs.getDouble("TienCoc"));
        hd.setTrangThai(rs.getString("TrangThai"));
        return hd;
    }
}