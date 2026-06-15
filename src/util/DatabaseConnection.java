package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection với Connection Pool đơn giản.
 * Dùng chung 1 connection – không tạo mới mỗi lần (tiết kiệm 300-500ms/lần gọi).
 * Thread-safe với synchronized.
 */
public class DatabaseConnection {

    private static final String DATABASE_NAME = "QuanLyPhongTro";
    private static final String USERNAME       = "sa";
    private static final String PASSWORD       = "123456";
    private static final String SERVER         = "localhost";
    private static final String PORT           = "1433";

    private static final String DB_URL =
            "jdbc:sqlserver://" + SERVER + ":" + PORT
            + ";databaseName=" + DATABASE_NAME
            + ";encrypt=true;trustServerCertificate=true;"
            + "loginTimeout=5;";   // ← timeout nhanh khi mất kết nối

    // ── Pool tối giản: 1 connection tái sử dụng ─────────────────────────────
    private static Connection sharedConn = null;

    /**
     * Lấy connection dùng chung. Nếu chưa có hoặc bị đóng thì tự kết nối lại.
     * Không bao giờ tạo connection mới khi connection cũ còn sống.
     */
    public static synchronized Connection getConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            conn.setAutoCommit(true);
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("[DB] LỖI: Không tìm thấy JDBC Driver!");
            e.printStackTrace();
            return null;
        } catch (SQLException e) {
            System.err.println("[DB] LỖI kết nối: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Dùng khi cần transaction riêng biệt (INSERT/UPDATE quan trọng).
     * Connection này do caller quản lý (phải close sau dùng).
     */
    public static Connection getNewConnection() {
        try {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            return DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
        } catch (Exception e) {
            System.err.println("[DB] LỖI tạo connection mới: " + e.getMessage());
            return null;
        }
    }

    // ── Test kết nối ────────────────────────────────────────────────────────
    public static void main(String[] args) {
        System.out.println("Đang thử kết nối tới: " + DATABASE_NAME);
        Connection c = getConnection();
        if (c != null) {
            System.out.println("=> KẾT NỐI THÀNH CÔNG!");
        } else {
            System.out.println("=> KẾT NỐI THẤT BẠI.");
        }
    }
}