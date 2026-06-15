package dao;

import model.KhachThue;
import util.DatabaseConnection;
import util.Session;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * KhachThueDAO – lọc theo MaChuTro = Session.getTenDN().
 * MaChuTro là chủ trọ sở hữu khách thuê này.
 * TenDN trong KhachThue là tài khoản đăng nhập của KHÁCH THUÊ (riêng biệt).
 */
public class KhachThueDAO {

    private final PhongTroDAO phongDao = new PhongTroDAO();

    private KhachThue map(ResultSet rs) throws SQLException {
        return new KhachThue(
            rs.getInt("MaKhach"),
            rs.getString("HoTen"),
            rs.getString("CCCD"),
            rs.getString("SDT"),
            rs.getString("QueQuan"),
            rs.getString("MaPhong"),
            rs.getDouble("PhiDichVu"),
            rs.getString("TrangThai"),
            rs.getString("TenDN")
        );
    }

    // ── Lấy tất cả khách của chủ trọ đang đăng nhập ──────────────────────────
    public List<KhachThue> getAll() {
        List<KhachThue> list = new ArrayList<>();
        String sql = "SELECT * FROM KhachThue WHERE MaChuTro = ? ORDER BY MaKhach";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, Session.getTenDN());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (Exception e) { e.printStackTrace(); }
        return list;
    }

    // ── Tìm theo MaKhach (phải thuộc chủ trọ hiện tại) ──────────────────────
    public KhachThue findById(int id) {
        String sql = "SELECT * FROM KhachThue WHERE MaKhach = ? AND MaChuTro = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, Session.getTenDN());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ── Tìm theo TenDN (tài khoản khách thuê – không lọc theo chủ trọ) ──────
    public KhachThue findByTenDN(String tenDN) {
        String sql = "SELECT * FROM KhachThue WHERE TenDN = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tenDN);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return map(rs);
        } catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    // ── Thêm mới ─────────────────────────────────────────────────────────────
    public boolean insert(KhachThue k) throws SQLException {
        String sql = "INSERT INTO KhachThue " +
                     "(HoTen, CCCD, SDT, QueQuan, MaPhong, PhiDichVu, TrangThai, TenDN, MaChuTro) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k.getHoTen());
            ps.setString(2, k.getCccd());
            ps.setString(3, k.getSdt());
            ps.setString(4, k.getQueQuan() != null ? k.getQueQuan() : "");
            ps.setString(5, k.getMaPhong());
            ps.setDouble(6, k.getPhiDichVu());
            ps.setString(7, k.getTrangThai());
            ps.setString(8, k.getTenDN()); // tài khoản đăng nhập của khách thuê
            ps.setString(9, Session.getTenDN()); // chủ trọ sở hữu
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                syncPhongTrangThai(k.getMaPhong(), k.getTrangThai());
                if ("Đang ở".equalsIgnoreCase(k.getTrangThai())) {
                    syncContract(k, null, null);
                    util.DatabaseMigration.ensureBillingRecords(k.getMaPhong(), Session.getTenDN(), c);
                }
            }
            return ok;
        }
    }

    // ── Cập nhật ─────────────────────────────────────────────────────────────
    public boolean update(KhachThue k) throws SQLException {
        KhachThue old = findById(k.getMaKhach());
        String oldMaPhong = (old != null) ? old.getMaPhong() : null;
        String oldHoTen = (old != null) ? old.getHoTen() : null;

        String sql = "UPDATE KhachThue " +
                     "SET HoTen=?, CCCD=?, SDT=?, QueQuan=?, MaPhong=?, PhiDichVu=?, TrangThai=?, TenDN=? " +
                     "WHERE MaKhach=? AND MaChuTro=?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, k.getHoTen());
            ps.setString(2, k.getCccd());
            ps.setString(3, k.getSdt());
            ps.setString(4, k.getQueQuan() != null ? k.getQueQuan() : "");
            ps.setString(5, k.getMaPhong());
            ps.setDouble(6, k.getPhiDichVu());
            ps.setString(7, k.getTrangThai());
            ps.setString(8, k.getTenDN());
            ps.setInt(9, k.getMaKhach());
            ps.setString(10, Session.getTenDN());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                if (oldMaPhong != null && !oldMaPhong.equals(k.getMaPhong())) {
                    phongDao.updateTrangThai(oldMaPhong, "Trống");
                }
                syncPhongTrangThai(k.getMaPhong(), k.getTrangThai());
                syncContract(k, oldMaPhong, oldHoTen);
                if ("Đang ở".equalsIgnoreCase(k.getTrangThai())) {
                    util.DatabaseMigration.ensureBillingRecords(k.getMaPhong(), Session.getTenDN(), c);
                }
            }
            return ok;
        }
    }

    // ── Xóa ─────────────────────────────────────────────────────────────────
    public boolean delete(int id) {
        KhachThue k = findById(id);
        String maPhong = (k != null) ? k.getMaPhong() : null;
        String sql = "DELETE FROM KhachThue WHERE MaKhach = ? AND MaChuTro = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, Session.getTenDN());
            boolean ok = ps.executeUpdate() > 0;
            if (ok) {
                if (maPhong != null) {
                    phongDao.updateTrangThai(maPhong, "Trống");
                }
                if (k != null) {
                    // Cập nhật hợp đồng của khách này thành "Hết hạn"
                    String sqlUpdateHd = "UPDATE HopDong SET TrangThai = N'Hết hạn' WHERE MaPhong = ? AND TenKhachHang = ? AND TenDN = ? AND TrangThai = N'Đang hiệu lực'";
                    try (PreparedStatement psHd = c.prepareStatement(sqlUpdateHd)) {
                        psHd.setString(1, k.getMaPhong());
                        psHd.setString(2, k.getHoTen());
                        psHd.setString(3, Session.getTenDN());
                        psHd.executeUpdate();
                    }
                }
            }
            return ok;
        } catch (Exception e) { e.printStackTrace(); return false; }
    }

    private void syncPhongTrangThai(String maPhong, String trangThaiKhach) {
        if (maPhong == null || maPhong.isEmpty()) return;
        String s = "Đang ở".equalsIgnoreCase(trangThaiKhach) ? "Đang thuê" : "Trống";
        phongDao.updateTrangThai(maPhong, s);
    }

    private void syncContract(KhachThue k, String oldMaPhong, String oldHoTen) {
        if (!"Đang ở".equalsIgnoreCase(k.getTrangThai())) {
            // Nếu khách không còn ở, chuyển các hợp đồng cũ thành "Hết hạn"
            String sqlUpdateHd = "UPDATE HopDong SET TrangThai = N'Hết hạn' WHERE MaPhong = ? AND TenKhachHang = ? AND TenDN = ? AND TrangThai = N'Đang hiệu lực'";
            try (Connection c = DatabaseConnection.getConnection();
                 PreparedStatement ps = c.prepareStatement(sqlUpdateHd)) {
                ps.setString(1, k.getMaPhong());
                ps.setString(2, k.getHoTen());
                ps.setString(3, Session.getTenDN());
                ps.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        // Kiểm tra xem đã có hợp đồng đang hiệu lực cho khách này và phòng này chưa
        boolean hasContract = false;
        String sqlCheck = "SELECT COUNT(*) FROM HopDong WHERE MaPhong = ? AND TenKhachHang = ? AND TenDN = ? AND TrangThai = N'Đang hiệu lực'";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlCheck)) {
            ps.setString(1, k.getMaPhong());
            ps.setString(2, k.getHoTen());
            ps.setString(3, Session.getTenDN());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasContract = true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!hasContract) {
            boolean updatedOld = false;
            // Nếu đổi phòng hoặc đổi tên khách, ta cập nhật hợp đồng hiện có
            if (oldMaPhong != null && oldHoTen != null) {
                String sqlUpdateOld = "UPDATE HopDong SET MaPhong = ?, TenKhachHang = ? WHERE MaPhong = ? AND TenKhachHang = ? AND TenDN = ? AND TrangThai = N'Đang hiệu lực'";
                try (Connection c = DatabaseConnection.getConnection();
                     PreparedStatement ps = c.prepareStatement(sqlUpdateOld)) {
                    ps.setString(1, k.getMaPhong());
                    ps.setString(2, k.getHoTen());
                    ps.setString(3, oldMaPhong);
                    ps.setString(4, oldHoTen);
                    ps.setString(5, Session.getTenDN());
                    if (ps.executeUpdate() > 0) {
                        updatedOld = true;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!updatedOld) {
                // Tạo mới hoàn toàn nếu chưa được cập nhật từ cái cũ
                createAutomaticContract(k);
            }
        }
    }

    private void createAutomaticContract(KhachThue k) {
        double defaultDeposit = 0;
        String sqlPrice = "SELECT GiaPhong FROM PhongTro WHERE MaPhong = ? AND TenDN = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlPrice)) {
            ps.setString(1, k.getMaPhong());
            ps.setString(2, Session.getTenDN());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    defaultDeposit = rs.getDouble("GiaPhong");
                }
            }
        } catch (Exception e) {
            System.err.println("Lỗi lấy giá phòng cho hợp đồng tự động: " + e.getMessage());
        }

        String sqlIns = "INSERT INTO HopDong (TenKhachHang, MaPhong, NgayBatDau, NgayKetThuc, TienCoc, TrangThai, TenDN) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sqlIns)) {
            ps.setString(1, k.getHoTen());
            ps.setString(2, k.getMaPhong());
            ps.setDate(3, java.sql.Date.valueOf(java.time.LocalDate.now()));
            ps.setDate(4, java.sql.Date.valueOf(java.time.LocalDate.now().plusYears(1)));
            ps.setDouble(5, defaultDeposit);
            ps.setString(6, "Đang hiệu lực");
            ps.setString(7, Session.getTenDN());
            ps.executeUpdate();
            System.out.println("[ContractSync] Tự động tạo hợp đồng cho khách: " + k.getHoTen() + " phòng: " + k.getMaPhong());
        } catch (Exception e) {
            System.err.println("Lỗi tạo hợp đồng tự động: " + e.getMessage());
        }
    }

    public boolean hasDuplicateCccd(String cccd, Integer excludeMaKhach) {
        if (cccd == null || cccd.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM KhachThue WHERE LTRIM(RTRIM(CCCD)) = LTRIM(RTRIM(?)) AND MaChuTro = ?" + 
                     (excludeMaKhach != null ? " AND MaKhach <> ?" : "");
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, cccd.trim());
            ps.setString(2, Session.getTenDN());
            if (excludeMaKhach != null) {
                ps.setInt(3, excludeMaKhach);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }

    public boolean hasDuplicateSdt(String sdt, Integer excludeMaKhach) {
        if (sdt == null || sdt.trim().isEmpty()) return false;
        String sql = "SELECT COUNT(*) FROM KhachThue WHERE LTRIM(RTRIM(SDT)) = LTRIM(RTRIM(?)) AND MaChuTro = ?" + 
                     (excludeMaKhach != null ? " AND MaKhach <> ?" : "");
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, sdt.trim());
            ps.setString(2, Session.getTenDN());
            if (excludeMaKhach != null) {
                ps.setInt(3, excludeMaKhach);
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return false;
    }
}