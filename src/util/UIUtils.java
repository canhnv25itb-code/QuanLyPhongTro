package util;

import javax.swing.*;
import java.awt.*;

public class UIUtils {
    public static void setupLookAndFeel() {
        try {
            // Thử FlatLaf nếu có, nếu không dùng Nimbus
            try {
                Class.forName("com.formdev.flatlaf.FlatDarkLaf");
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatDarkLaf");
            } catch (Exception ex) {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if ("Nimbus".equals(info.getName())) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
            }
        } catch (Exception e) {
            // ignore, dùng default
        }
    }

    public static void showInfo(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showError(Component parent, String message, String title) {
        JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
    }

    public static JButton createButton(String text, Icon icon) {
        JButton btn = new JButton(text, icon);
        btn.setFocusPainted(false);
        btn.setBackground(new Color(60, 120, 200));
        btn.setForeground(Color.WHITE);
        btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        return btn;
    }
}