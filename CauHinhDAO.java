package dao;

import util.DatabaseConnection;
import util.Session;
import java.sql.*;

/**
 * CauHinhDAO – Quản lý cài đặt giá điện/nước của từng chủ trọ.
 * Tự động tạo bảng CauHinh nếu chưa tồn tại.
 */
public class CauHinhDAO {

    // Giá mặc định nếu chưa cấu hình
    public static final int DEFAULT_GIA_DIEN = 3500;   // VNĐ / kWh
    public static final int DEFAULT_GIA_NUOC = 15000;  // VNĐ / m³

    public CauHinhDAO() {
        ensureTableExists();
    }

    /** Tự động tạo bảng CauHinh nếu chưa có */
    private void ensureTableExists() {
        String sql = "IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'CauHinh') " +
                     "CREATE TABLE CauHinh (" +
                     "  TenDN     VARCHAR(50)  NOT NULL PRIMARY KEY, " +
                     "  GiaDien   FLOAT        NOT NULL DEFAULT 3500, " +
                     "  GiaNuoc   FLOAT        NOT NULL DEFAULT 15000" +
                     ")";
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement()) {
            st.executeUpdate(sql);
        } catch (Exception e) {
            System.err.println("[CauHinhDAO] Lỗi tạo bảng CauHinh: " + e.getMessage());
        }
    }

    /** Lấy giá điện (VNĐ/kWh) của chủ trọ hiện tại */
    public double getGiaDien() {
        return getConfig("GiaDien", DEFAULT_GIA_DIEN);
    }

    /** Lấy giá nước (VNĐ/m³) của chủ trọ hiện tại */
    public double getGiaNuoc() {
        return getConfig("GiaNuoc", DEFAULT_GIA_NUOC);
    }

    private double getConfig(String column, double defaultVal) {
        String sql = "SELECT " + column + " FROM CauHinh WHERE TenDN = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (Exception e) {
            System.err.println("[CauHinhDAO] getConfig(" + column + "): " + e.getMessage());
        }
        return defaultVal;
    }

    /**
     * Lưu hoặc cập nhật giá điện + giá nước cho chủ trọ hiện tại.
     * Dùng MERGE (UPSERT) để tránh duplicate key.
     */
    public boolean saveGia(double giaDien, double giaNuoc) {
        String sql =
            "MERGE CauHinh AS target " +
            "USING (SELECT ? AS TenDN, ? AS GiaDien, ? AS GiaNuoc) AS src " +
            "  ON target.TenDN = src.TenDN " +
            "WHEN MATCHED THEN " +
            "  UPDATE SET GiaDien = src.GiaDien, GiaNuoc = src.GiaNuoc " +
            "WHEN NOT MATCHED THEN " +
            "  INSERT (TenDN, GiaDien, GiaNuoc) VALUES (src.TenDN, src.GiaDien, src.GiaNuoc);";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            ps.setDouble(2, giaDien);
            ps.setDouble(3, giaNuoc);
            ps.executeUpdate();
            return true;
        } catch (Exception e) {
            System.err.println("[CauHinhDAO] saveGia: " + e.getMessage());
            return false;
        }
    }
}
