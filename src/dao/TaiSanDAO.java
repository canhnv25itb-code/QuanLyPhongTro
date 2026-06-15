package dao;

import model.TaiSan;
import util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaiSanDAO {
    public List<TaiSan> getByMaPhong(String maPhong, String tenDN) {
        List<TaiSan> list = new ArrayList<>();
        String sql = "SELECT * FROM TaiSan WHERE MaPhong = ? AND TenDN = ? ORDER BY ID ASC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maPhong);
            ps.setString(2, tenDN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TaiSan ts = new TaiSan();
                    ts.setId(rs.getInt("ID"));
                    ts.setMaPhong(rs.getString("MaPhong"));
                    ts.setTenTaiSan(rs.getString("TenTaiSan"));
                    ts.setSoLuong(rs.getInt("SoLuong"));
                    ts.setTinhTrang(rs.getString("TinhTrang"));
                    ts.setTenDN(rs.getString("TenDN"));
                    list.add(ts);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean insert(TaiSan ts) {
        String sql = "INSERT INTO TaiSan (MaPhong, TenTaiSan, SoLuong, TinhTrang, TenDN) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ts.getMaPhong());
            ps.setString(2, ts.getTenTaiSan());
            ps.setInt(3, ts.getSoLuong());
            ps.setString(4, ts.getTinhTrang());
            ps.setString(5, ts.getTenDN());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean update(TaiSan ts) {
        String sql = "UPDATE TaiSan SET TenTaiSan = ?, SoLuong = ?, TinhTrang = ? WHERE ID = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ts.getTenTaiSan());
            ps.setInt(2, ts.getSoLuong());
            ps.setString(3, ts.getTinhTrang());
            ps.setInt(4, ts.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM TaiSan WHERE ID = ?";
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
