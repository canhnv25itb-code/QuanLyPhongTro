package dao;

import model.SuCo;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SuCoDAO {
    public List<SuCo> getByTenDN(String tenDN) {
        List<SuCo> list = new ArrayList<>();
        String sql = "SELECT * FROM SuCo WHERE TenDN = ? ORDER BY ID DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SuCo sc = new SuCo();
                    sc.setId(rs.getInt("ID"));
                    sc.setMaPhong(rs.getString("MaPhong"));
                    sc.setMoTa(rs.getString("MoTa"));
                    sc.setNgayGui(rs.getDate("NgayGui").toLocalDate());
                    sc.setTrangThai(rs.getString("TrangThai"));
                    sc.setTenDN(rs.getString("TenDN"));
                    list.add(sc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<SuCo> getByMaPhong(String maPhong) {
        List<SuCo> list = new ArrayList<>();
        String sql = "SELECT * FROM SuCo WHERE MaPhong = ? ORDER BY ID DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    SuCo sc = new SuCo();
                    sc.setId(rs.getInt("ID"));
                    sc.setMaPhong(rs.getString("MaPhong"));
                    sc.setMoTa(rs.getString("MoTa"));
                    sc.setNgayGui(rs.getDate("NgayGui").toLocalDate());
                    sc.setTrangThai(rs.getString("TrangThai"));
                    sc.setTenDN(rs.getString("TenDN"));
                    list.add(sc);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(SuCo sc) {
        String sql = "INSERT INTO SuCo (MaPhong, MoTa, NgayGui, TrangThai, TenDN) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sc.getMaPhong());
            ps.setString(2, sc.getMoTa());
            ps.setDate(3, Date.valueOf(sc.getNgayGui()));
            ps.setString(4, sc.getTrangThai());
            ps.setString(5, sc.getTenDN());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTrangThai(int id, String trangThai) {
        String sql = "UPDATE SuCo SET TrangThai = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
