import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import ui.LoginForm;
import util.DatabaseMigration;

public class Main {
    public static void main(String[] args) {

        // ── Tự động cập nhật cấu trúc database khi cần (chạy 1 lần) ──
        DatabaseMigration.run();

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                util.UIUtils.setupLookAndFeel();

                // Gọi đúng tên class LoginForm của bạn (Viết hoa L và F)
                LoginForm login = new LoginForm();
                login.setLocationRelativeTo(null);
                login.setVisible(true);
            }
        });
    }
}