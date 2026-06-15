package dao;

import model.ThongBao;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ThongBaoDAO {
    public List<ThongBao> getByTenDN(String tenDN) {
        List<ThongBao> list = new ArrayList<>();
        String sql = "SELECT * FROM ThongBao WHERE TenDN = ? ORDER BY ID DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ThongBao tb = new ThongBao();
                    tb.setId(rs.getInt("ID"));
                    tb.setTieuDe(rs.getString("TieuDe"));
                    tb.setNoiDung(rs.getString("NoiDung"));
                    tb.setNgayDang(rs.getDate("NgayDang").toLocalDate());
                    tb.setTenDN(rs.getString("TenDN"));
                    list.add(tb);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<ThongBao> getLatestAnnouncements(String maChuTro, int limit) {
        List<ThongBao> list = new ArrayList<>();
        String sql = "SELECT TOP (?) * FROM ThongBao WHERE TenDN = ? ORDER BY ID DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setString(2, maChuTro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ThongBao tb = new ThongBao();
                    tb.setId(rs.getInt("ID"));
                    tb.setTieuDe(rs.getString("TieuDe"));
                    tb.setNoiDung(rs.getString("NoiDung"));
                    tb.setNgayDang(rs.getDate("NgayDang").toLocalDate());
                    tb.setTenDN(rs.getString("TenDN"));
                    list.add(tb);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(ThongBao tb) {
        String sql = "INSERT INTO ThongBao (TieuDe, NoiDung, NgayDang, TenDN) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tb.getTieuDe());
            ps.setString(2, tb.getNoiDung());
            ps.setDate(3, Date.valueOf(tb.getNgayDang()));
            ps.setString(4, tb.getTenDN());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM ThongBao WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
