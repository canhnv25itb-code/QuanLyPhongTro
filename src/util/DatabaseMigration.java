package util;

import java.sql.*;

/**
 * DatabaseMigration – Thiết lập schema multi-tenancy hoàn chỉnh.
 *
 * Mỗi lần khởi động sẽ tự động:
 *   1. Thêm cột TenDN vào PhongTro (nếu chưa có)
 *   2. Gán phòng TenDN=NULL cho Chủ trọ đầu tiên
 *   3. Đổi PK PhongTro → (MaPhong, TenDN) nếu vẫn là PK đơn
 *   4. Thêm cột MaChuTro vào KhachThue (nếu chưa có) + gán dữ liệu cũ
 *   5. Thêm cột TenDN vào HopDong, HoaDon, DienNuoc, DichVu (nếu chưa có) + gán dữ liệu cũ
 */
public class DatabaseMigration {

    public static void run() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String firstChuTro = findFirstChuTro(conn);
            String owner = firstChuTro != null ? firstChuTro : "unknown";

            // ── BƯỚC 1: PhongTro.TenDN ──────────────────────────────────────
            if (!columnExists(conn, "PhongTro", "TenDN")) {
                runSql(conn, "ALTER TABLE PhongTro ADD TenDN VARCHAR(50) NULL");
                System.out.println("[Migration] Thêm cột TenDN vào PhongTro OK");
            }
            int fixed = runSqlUpdate(conn,
                "UPDATE PhongTro SET TenDN = '" + owner + "' WHERE TenDN IS NULL");
            if (fixed > 0) System.out.println("[Migration] Gán " + fixed + " phòng NULL → " + owner);

            // ── BƯỚC 2: Đổi PK PhongTro → (MaPhong, TenDN) ─────────────────
            if (pkIsSingleColumn(conn, "PhongTro", "MaPhong")) {
                System.out.println("[Migration] Đổi PK PhongTro → (MaPhong, TenDN)...");
                dropForeignKeysReferencing(conn, "PhongTro");
                String pkName = getPrimaryKeyName(conn, "PhongTro");
                if (pkName != null)
                    runSql(conn, "ALTER TABLE PhongTro DROP CONSTRAINT " + pkName);
                runSql(conn, "UPDATE PhongTro SET TenDN = '" + owner + "' WHERE TenDN IS NULL");
                runSql(conn, "ALTER TABLE PhongTro ALTER COLUMN TenDN VARCHAR(50) NOT NULL");
                runSql(conn, "ALTER TABLE PhongTro ADD CONSTRAINT PK_PhongTro PRIMARY KEY (MaPhong, TenDN)");
                System.out.println("[Migration] PK mới: (MaPhong, TenDN) ✓");
            }

            // ── BƯỚC 3: KhachThue.MaChuTro ──────────────────────────────────
            if (!columnExists(conn, "KhachThue", "MaChuTro")) {
                runSql(conn, "ALTER TABLE KhachThue ADD MaChuTro VARCHAR(50) NULL");
                System.out.println("[Migration] Thêm cột MaChuTro vào KhachThue OK");
            }
            // Gán khách thuê theo phòng → chủ trọ sở hữu phòng đó
            runSqlUpdate(conn,
                "UPDATE kt SET kt.MaChuTro = p.TenDN " +
                "FROM KhachThue kt " +
                "JOIN PhongTro p ON LTRIM(RTRIM(p.MaPhong)) = LTRIM(RTRIM(kt.MaPhong)) " +
                "WHERE kt.MaChuTro IS NULL");

            // Fallback: nếu vẫn còn NULL (phòng bị xóa/không khớp)
            runSqlUpdate(conn,
                "UPDATE KhachThue SET MaChuTro = '" + owner + "' WHERE MaChuTro IS NULL");

            // ── BƯỚC 4: HopDong.TenDN ────────────────────────────────────────
            migrateChildTable(conn, "HopDong",  "MaPhong", "TenDN", owner);

            // ── BƯỚC 5: HoaDon.TenDN ─────────────────────────────────────────
            // HoaDon dùng cột TenPhong (= MaPhong)
            if (!columnExists(conn, "HoaDon", "TenDN")) {
                runSql(conn, "ALTER TABLE HoaDon ADD TenDN VARCHAR(50) NULL");
                System.out.println("[Migration] Thêm cột TenDN vào HoaDon OK");
            }
            runSqlUpdate(conn,
                "UPDATE hd SET hd.TenDN = p.TenDN " +
                "FROM HoaDon hd " +
                "JOIN PhongTro p ON LTRIM(RTRIM(p.MaPhong)) = LTRIM(RTRIM(hd.TenPhong)) " +
                "WHERE hd.TenDN IS NULL");
            runSqlUpdate(conn,
                "UPDATE HoaDon SET TenDN = '" + owner + "' WHERE TenDN IS NULL");

            // ── BƯỚC 6: DienNuoc.TenDN ───────────────────────────────────────
            migrateChildTable(conn, "DienNuoc", "MaPhong", "TenDN", owner);

            // ── BƯỚC 7: DichVu.TenDN ─────────────────────────────────────────
            migrateChildTable(conn, "DichVu",   "MaPhong", "TenDN", owner);

            // ── BƯỚC 8: Tự động đồng bộ điện nước, hóa đơn, dịch vụ cho khách thuê hiện tại ──
            System.out.println("[Migration] Kiểm tra và tự động tạo điện nước, hóa đơn, dịch vụ cho tất cả khách thuê hiện tại...");
            int autoRecordsCreated = 0;
            String sqlActiveRenters = "SELECT MaPhong, MaChuTro FROM KhachThue WHERE TrangThai = N'Đang ở'";
            try (Statement st = conn.createStatement();
                 ResultSet rs = st.executeQuery(sqlActiveRenters)) {
                while (rs.next()) {
                    String maPhong = rs.getString("MaPhong");
                    String maChuTro = rs.getString("MaChuTro");
                    ensureBillingRecords(maPhong, maChuTro, conn);
                    autoRecordsCreated++;
                }
            }
            System.out.println("[Migration] Đã đồng bộ dữ liệu cho " + autoRecordsCreated + " phòng đang ở.");

            // ── BƯỚC 9: Loại bỏ ràng buộc UNIQUE trên KhachThue ──
            dropUniqueConstraintIfExists(conn, "KhachThue");

            // ── BƯỚC 10: Bảng Sự cố & Bảo trì ──
            if (!tableExists(conn, "SuCo")) {
                runSql(conn, "CREATE TABLE SuCo (" +
                             "  ID INT IDENTITY(1,1) PRIMARY KEY," +
                             "  MaPhong VARCHAR(50) NOT NULL," +
                             "  MoTa NVARCHAR(500) NOT NULL," +
                             "  NgayGui DATE NOT NULL," +
                             "  TrangThai NVARCHAR(50) NOT NULL DEFAULT N'Chờ xử lý'," +
                             "  TenDN VARCHAR(50) NOT NULL" +
                             ")");
                System.out.println("[Migration] Tạo bảng SuCo OK");
            }

            // ── BƯỚC 11: Bảng Quản lý Tài sản ──
            if (!tableExists(conn, "TaiSan")) {
                runSql(conn, "CREATE TABLE TaiSan (" +
                             "  ID INT IDENTITY(1,1) PRIMARY KEY," +
                             "  MaPhong VARCHAR(50) NOT NULL," +
                             "  TenTaiSan NVARCHAR(100) NOT NULL," +
                             "  SoLuong INT NOT NULL DEFAULT 1," +
                             "  TinhTrang NVARCHAR(100) NOT NULL," +
                             "  TenDN VARCHAR(50) NOT NULL" +
                             ")");
                System.out.println("[Migration] Tạo bảng TaiSan OK");
            }

            // ── BƯỚC 12: Bảng Thông báo ──
            if (!tableExists(conn, "ThongBao")) {
                runSql(conn, "CREATE TABLE ThongBao (" +
                             "  ID INT IDENTITY(1,1) PRIMARY KEY," +
                             "  TieuDe NVARCHAR(200) NOT NULL," +
                             "  NoiDung NVARCHAR(MAX) NOT NULL," +
                             "  NgayDang DATE NOT NULL," +
                             "  TenDN VARCHAR(50) NOT NULL" +
                             ")");
                System.out.println("[Migration] Tạo bảng ThongBao OK");
            }

            // ── BƯỚC 13: Bảng Cấu hình Ngân hàng ──
            if (!tableExists(conn, "CauHinhNganHang")) {
                runSql(conn, "CREATE TABLE CauHinhNganHang (" +
                             "  TenDN VARCHAR(50) PRIMARY KEY," +
                             "  TenNganHang VARCHAR(50) NOT NULL," +
                             "  SoTaiKhoan VARCHAR(50) NOT NULL," +
                             "  TenChuTaiKhoan NVARCHAR(100) NOT NULL" +
                             ")");
                System.out.println("[Migration] Tạo bảng CauHinhNganHang OK");
                
                // Chèn dữ liệu mẫu cho chủ trọ đầu tiên
                if (firstChuTro != null) {
                    runSql(conn, "INSERT INTO CauHinhNganHang (TenDN, TenNganHang, SoTaiKhoan, TenChuTaiKhoan) " +
                                 "VALUES ('" + firstChuTro + "', 'vietcombank', '0011004123456', N'NGUYEN VAN A')");
                }
            }

            System.out.println("[Migration] Hoàn tất schema multi-tenancy!");

        } catch (Exception e) {
            System.err.println("[Migration] Lỗi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Thêm cột TenDN vào bảng, gán theo JOIN PhongTro, fallback sang owner nếu NULL.
     * @param roomCol  tên cột MaPhong trong bảng con (thường là "MaPhong")
     */
    private static void migrateChildTable(Connection conn, String table,
                                           String roomCol, String tenDNCol,
                                           String owner) throws SQLException {
        if (!columnExists(conn, table, tenDNCol)) {
            runSql(conn, "ALTER TABLE " + table + " ADD " + tenDNCol + " VARCHAR(50) NULL");
            System.out.println("[Migration] Thêm cột " + tenDNCol + " vào " + table + " OK");
        }
        // Gán theo phòng
        runSqlUpdate(conn,
            "UPDATE t SET t." + tenDNCol + " = p.TenDN " +
            "FROM " + table + " t " +
            "JOIN PhongTro p ON LTRIM(RTRIM(p.MaPhong)) = LTRIM(RTRIM(t." + roomCol + ")) " +
            "WHERE t." + tenDNCol + " IS NULL");
        // Fallback
        runSqlUpdate(conn,
            "UPDATE " + table + " SET " + tenDNCol + " = '" + owner + "' WHERE " + tenDNCol + " IS NULL");
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

    private static boolean columnExists(Connection conn, String table, String column) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getColumns(null, null, table, column)) {
            return rs.next();
        }
    }

    private static boolean pkIsSingleColumn(Connection conn, String table, String colName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getPrimaryKeys(null, null, table)) {
            boolean found = false; int count = 0;
            while (rs.next()) { count++; if (colName.equalsIgnoreCase(rs.getString("COLUMN_NAME"))) found = true; }
            return found && count == 1;
        }
    }

    private static String getPrimaryKeyName(Connection conn, String table) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getPrimaryKeys(null, null, table)) {
            if (rs.next()) return rs.getString("PK_NAME");
        }
        return null;
    }

    private static void dropForeignKeysReferencing(Connection conn, String pkTable) throws SQLException {
        String sql = "SELECT fk.name AS fk_name, OBJECT_NAME(fk.parent_object_id) AS child_table " +
                     "FROM sys.foreign_keys fk " +
                     "JOIN sys.tables pt ON pt.object_id = fk.referenced_object_id " +
                     "WHERE pt.name = '" + pkTable + "'";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String fkName = rs.getString("fk_name");
                String child  = rs.getString("child_table");
                try { runSql(conn, "ALTER TABLE " + child + " DROP CONSTRAINT " + fkName);
                      System.out.println("[Migration] Xóa FK '" + fkName + "' trên " + child);
                } catch (SQLException ex) {
                    System.err.println("[Migration] Không xóa được FK '" + fkName + "': " + ex.getMessage());
                }
            }
        }
    }

    private static void runSql(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement()) { st.executeUpdate(sql); }
    }

    private static int runSqlUpdate(Connection conn, String sql) throws SQLException {
        try (Statement st = conn.createStatement()) { return st.executeUpdate(sql); }
    }

    private static String findFirstChuTro(Connection conn) {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT TOP 1 TenDN FROM TaiKhoan WHERE Quyen = N'Chủ trọ' ORDER BY TenDN");
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return rs.getString("TenDN");
        } catch (Exception e) { System.err.println("[Migration] findFirstChuTro: " + e.getMessage()); }
        return null;
    }

    public static void ensureBillingRecords(String maPhong, String maChuTro, Connection conn) throws SQLException {
        if (maPhong == null || maPhong.isEmpty() || maChuTro == null || maChuTro.isEmpty()) return;

        // 1. Đảm bảo có dịch vụ (DichVu) cho phòng này
        boolean hasDichVu = false;
        String sqlCheckDv = "SELECT COUNT(*) FROM DichVu WHERE MaPhong = ? AND TenDN = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlCheckDv)) {
            ps.setString(1, maPhong);
            ps.setString(2, maChuTro);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasDichVu = true;
                }
            }
        }

        if (!hasDichVu) {
            String sqlInsDv = "INSERT INTO DichVu (MaPhong, Wifi, ThangMay, GiuXe, PCCC_VeSinh, TenDN) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsDv)) {
                ps.setString(1, maPhong);
                ps.setDouble(2, 100000);
                ps.setDouble(3, 50000);
                ps.setDouble(4, 100000);
                ps.setDouble(5, 50000);
                ps.setString(6, maChuTro);
                ps.executeUpdate();
            }
        }

        // Lấy tháng năm hiện tại dạng "MM/yyyy"
        java.time.LocalDate today = java.time.LocalDate.now();
        String currentThangNam = String.format("%02d/%d", today.getMonthValue(), today.getYear());
        String currentThangNamAlt = today.getMonthValue() + "/" + today.getYear();

        // 2. Đảm bảo có chỉ số điện nước (DienNuoc) cho tháng hiện tại
        boolean hasDienNuoc = false;
        String sqlCheckDn = "SELECT COUNT(*) FROM DienNuoc WHERE MaPhong = ? AND TenDN = ? AND (ThangNam = ? OR ThangNam = ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlCheckDn)) {
            ps.setString(1, maPhong);
            ps.setString(2, maChuTro);
            ps.setString(3, currentThangNam);
            ps.setString(4, currentThangNamAlt);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasDienNuoc = true;
                }
            }
        }

        if (!hasDienNuoc) {
            int dienDau = 0;
            int nuocDau = 0;
            String sqlLastDn = "SELECT TOP 1 DienCuoi, NuocCuoi FROM DienNuoc WHERE MaPhong = ? AND TenDN = ? ORDER BY ID DESC";
            try (PreparedStatement ps = conn.prepareStatement(sqlLastDn)) {
                ps.setString(1, maPhong);
                ps.setString(2, maChuTro);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        dienDau = rs.getInt("DienCuoi");
                        nuocDau = rs.getInt("NuocCuoi");
                    }
                }
            }

            String sqlInsDn = "INSERT INTO DienNuoc (MaPhong, ThangNam, DienDau, DienCuoi, NuocDau, NuocCuoi, TienDien, TienNuoc, TenDN) " +
                              "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsDn)) {
                ps.setString(1, maPhong);
                ps.setString(2, currentThangNam);
                ps.setInt(3, dienDau);
                ps.setInt(4, dienDau);
                ps.setInt(5, nuocDau);
                ps.setInt(6, nuocDau);
                ps.setDouble(7, 0);
                ps.setDouble(8, 0);
                ps.setString(9, maChuTro);
                ps.executeUpdate();
            }
        }

        // 3. Đảm bảo có hóa đơn (HoaDon) cho tháng hiện tại
        boolean hasHoaDon = false;
        String sqlCheckHd = "SELECT COUNT(*) FROM HoaDon WHERE TenPhong = ? AND TenDN = ? AND MONTH(NgayLap) = ? AND YEAR(NgayLap) = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlCheckHd)) {
            ps.setString(1, maPhong);
            ps.setString(2, maChuTro);
            ps.setInt(3, today.getMonthValue());
            ps.setInt(4, today.getYear());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    hasHoaDon = true;
                }
            }
        }

        if (!hasHoaDon) {
            double giaPhong = 0;
            String sqlGiaPhong = "SELECT GiaPhong FROM PhongTro WHERE MaPhong = ? AND TenDN = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlGiaPhong)) {
                ps.setString(1, maPhong);
                ps.setString(2, maChuTro);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        giaPhong = rs.getDouble("GiaPhong");
                    }
                }
            }

            double tongTienDv = 0;
            String sqlTongDv = "SELECT TongTienDV FROM DichVu WHERE MaPhong = ? AND TenDN = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlTongDv)) {
                ps.setString(1, maPhong);
                ps.setString(2, maChuTro);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        tongTienDv = rs.getDouble("TongTienDV");
                    }
                }
            }

            String sqlInsHd = "INSERT INTO HoaDon (TenPhong, NgayLap, TienPhong, TienDien, TienNuoc, TienDichVu, TrangThai, TenDN) " +
                              "VALUES (?, ?, ?, 0, 0, ?, N'Chưa thu', ?)";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsHd)) {
                ps.setString(1, maPhong);
                ps.setDate(2, java.sql.Date.valueOf(today.withDayOfMonth(1)));
                ps.setDouble(3, giaPhong);
                ps.setDouble(4, tongTienDv);
                ps.setString(5, maChuTro);
                ps.executeUpdate();
            }
        }
    }

    private static void dropUniqueConstraintIfExists(Connection conn, String table) {
        try {
            String sqlFind = "SELECT name FROM sys.objects WHERE parent_object_id = OBJECT_ID('" + table + "') AND type = 'UQ'";
            String constraintName = null;
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlFind)) {
                if (rs.next()) {
                    constraintName = rs.getString("name");
                }
            }
            if (constraintName != null) {
                runSql(conn, "ALTER TABLE " + table + " DROP CONSTRAINT " + constraintName);
                System.out.println("[Migration] Đã xóa ràng buộc UNIQUE '" + constraintName + "' trên bảng " + table);
            }
        } catch (Exception e) {
            System.err.println("[Migration] Lỗi khi xóa UNIQUE trên " + table + ": " + e.getMessage());
        }
    }

    private static boolean tableExists(Connection conn, String tableName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        }
    }
}
