package util;

import model.TaiKhoan;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Session – Singleton lưu thông tin tài khoản đang đăng nhập.
 *
 * Cách dùng:
 *   - Login thành công: Session.login(taiKhoan);
 *   - Lấy TenDN để lọc DB: Session.getTenDN();
 *   - Kiểm tra vai trò: Session.isAdmin();
 *   - Đăng xuất: Session.logout();
 */
public class Session {

    private static TaiKhoan currentUser = null;
    private static String ownerDN = null;

    /** Không cho khởi tạo instance */
    private Session() {}

    /** Gọi sau khi đăng nhập thành công */
    public static void login(TaiKhoan user) {
        currentUser = user;
        ownerDN = null;
        if (user != null) {
            if ("Khách thuê".equalsIgnoreCase(user.getVaiTro())) {
                String sql = "SELECT MaChuTro FROM KhachThue WHERE TenDN = ?";
                try (Connection c = DatabaseConnection.getConnection();
                     PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, user.getTenDangNhap());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            ownerDN = rs.getString("MaChuTro");
                            System.out.println("[Session] Resolved landlord's username (MaChuTro) for renter: " + ownerDN);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("[Session] Error querying MaChuTro: " + e.getMessage());
                }
            } else {
                ownerDN = user.getTenDangNhap();
            }
        }
        System.out.println("[Session] Đăng nhập: " + (user != null ? user.getTenDangNhap() : "null")
                + " (" + (user != null ? user.getVaiTro() : "null") + ")");
    }

    /** Gọi khi đăng xuất */
    public static void logout() {
        System.out.println("[Session] Đăng xuất: "
                + (currentUser != null ? currentUser.getTenDangNhap() : "?"));
        currentUser = null;
        ownerDN = null;
    }

    /** Tên đăng nhập hiện tại – dùng trong WHERE TenDN = ? */
    public static String getTenDN() {
        if (currentUser != null && "Khách thuê".equalsIgnoreCase(currentUser.getVaiTro()) && ownerDN != null) {
            return ownerDN;
        }
        return currentUser != null ? currentUser.getTenDangNhap() : "";
    }

    /** Lấy tên đăng nhập thực tế của tài khoản (không đổi thành chủ trọ) */
    public static String getRealTenDN() {
        return currentUser != null ? currentUser.getTenDangNhap() : "";
    }

    /** Lấy toàn bộ object TaiKhoan */
    public static TaiKhoan getCurrentUser() {
        return currentUser;
    }

    /** Vai trò của user hiện tại */
    public static String getVaiTro() {
        return currentUser != null ? currentUser.getVaiTro() : "";
    }

    /** Kiểm tra có phải Admin không */
    public static boolean isAdmin() {
        return "Admin".equalsIgnoreCase(getVaiTro());
    }

    /** Kiểm tra đã đăng nhập chưa */
    public static boolean isLoggedIn() {
        return currentUser != null;
    }
}
