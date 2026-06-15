package dao;

import model.CauHinhNganHang;
import util.DatabaseConnection;

import java.sql.*;

public class CauHinhNganHangDAO {
    public CauHinhNganHang getByTenDN(String tenDN) {
        String sql = "SELECT * FROM CauHinhNganHang WHERE TenDN = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDN);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    CauHinhNganHang ch = new CauHinhNganHang();
                    ch.setTenDN(rs.getString("TenDN"));
                    ch.setTenNganHang(rs.getString("TenNganHang"));
                    ch.setSoTaiKhoan(rs.getString("SoTaiKhoan"));
                    ch.setTenChuTaiKhoan(rs.getString("TenChuTaiKhoan"));
                    return ch;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean save(CauHinhNganHang ch) {
        String sqlCheck = "SELECT COUNT(*) FROM CauHinhNganHang WHERE TenDN = ?";
        boolean exists = false;
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlCheck)) {
            ps.setString(1, ch.getTenDN());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    exists = true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (exists) {
            String sqlUpdate = "UPDATE CauHinhNganHang SET TenNganHang = ?, SoTaiKhoan = ?, TenChuTaiKhoan = ? WHERE TenDN = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setString(1, ch.getTenNganHang());
                ps.setString(2, ch.getSoTaiKhoan());
                ps.setString(3, ch.getTenChuTaiKhoan());
                ps.setString(4, ch.getTenDN());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            String sqlInsert = "INSERT INTO CauHinhNganHang (TenDN, TenNganHang, SoTaiKhoan, TenChuTaiKhoan) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sqlInsert)) {
                ps.setString(1, ch.getTenDN());
                ps.setString(2, ch.getTenNganHang());
                ps.setString(3, ch.getSoTaiKhoan());
                ps.setString(4, ch.getTenChuTaiKhoan());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
    }
}
