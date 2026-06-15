package ui;

import dao.TaiKhoanDAO;
import model.TaiKhoan;

import javax.swing.*;
import java.awt.*;

/**
 * Form đăng ký tài khoản mới.
 * Người dùng chọn vai trò: "Chủ trọ" hoặc "Khách thuê".
 */
public class RegisterForm extends JFrame {

    // ─── UI ──────────────────────────────────────────────────────────────────
    private final JTextField     txtUser    = new JTextField(20);
    private final JPasswordField txtPass    = new JPasswordField(20);
    private final JPasswordField txtConfirm = new JPasswordField(20);
    private final JTextField     txtEmail   = new JTextField(20);
    private final JComboBox<String> cboRole =
            new JComboBox<>(new String[]{"Chủ trọ", "Khách thuê"});

    private final TaiKhoanDAO dao;
    private final JFrame      prevFrame;   // quay lại LoginForm khi hủy

    // ─── Màu sắc ─────────────────────────────────────────────────────────────
    private static final Color BG        = new Color(30, 31, 38);
    private static final Color ACCENT    = new Color(46, 204, 113);  // xanh lá
    private static final Color BACK_CLR  = new Color(100, 100, 110);
    private static final Color FG_LABEL  = new Color(180, 180, 190);
    private static final Font  FONT_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    private static final Font  FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_BTN   = new Font("Segoe UI", Font.BOLD, 13);

    // ─── Constructor ─────────────────────────────────────────────────────────
    public RegisterForm(JFrame prevFrame) {
        this.prevFrame = prevFrame;
        this.dao       = new TaiKhoanDAO();

        setTitle("ĐĂNG KÝ TÀI KHOẢN");
        setSize(450, 420);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
        getContentPane().setBackground(BG);

        buildUI();

        // Khi đóng cửa sổ này thì hiện lại LoginForm
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                returnToLogin();
            }
        });
    }

    // ─── Xây dựng giao diện ──────────────────────────────────────────────────
    private void buildUI() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        // Tiêu đề
        JLabel lblTitle = new JLabel("TẠO TÀI KHOẢN MỚI", SwingConstants.CENTER);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ACCENT);
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(lblTitle, gbc);

        // Vai trò
        gbc.gridwidth = 1; gbc.gridy = 1; gbc.gridx = 0;
        add(makeLabel("Vai trò:"), gbc);
        gbc.gridx = 1;
        styleCombo(cboRole);
        add(cboRole, gbc);

        // Tên đăng nhập
        gbc.gridx = 0; gbc.gridy = 2;
        add(makeLabel("Tên đăng nhập:"), gbc);
        gbc.gridx = 1; add(styleField(txtUser), gbc);

        // Email
        gbc.gridx = 0; gbc.gridy = 3;
        add(makeLabel("Email:"), gbc);
        gbc.gridx = 1; add(styleField(txtEmail), gbc);

        // Mật khẩu
        gbc.gridx = 0; gbc.gridy = 4;
        add(makeLabel("Mật khẩu:"), gbc);
        gbc.gridx = 1; add(styleField(txtPass), gbc);

        // Xác nhận mật khẩu
        gbc.gridx = 0; gbc.gridy = 5;
        add(makeLabel("Xác nhận MK:"), gbc);
        gbc.gridx = 1; add(styleField(txtConfirm), gbc);

        // Nút Đăng ký
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2;
        JButton btnRegister = makeButton("ĐĂNG KÝ", ACCENT, Color.BLACK);
        btnRegister.addActionListener(e -> handleRegister());
        add(btnRegister, gbc);

        // Nút Quay lại
        gbc.gridy = 7;
        JButton btnBack = makeButton("← QUAY LẠI ĐĂNG NHẬP", BACK_CLR, Color.WHITE);
        btnBack.addActionListener(e -> returnToLogin());
        add(btnBack, gbc);

        getRootPane().setDefaultButton(btnRegister);
    }

    // ─── Xử lý đăng ký ───────────────────────────────────────────────────────
    private void handleRegister() {
        String username = txtUser.getText().trim();
        String email    = txtEmail.getText().trim();
        String pass     = new String(txtPass.getPassword());
        String confirm  = new String(txtConfirm.getPassword());
        String role     = (String) cboRole.getSelectedItem();

        // ── Validate ──────────────────────────────────────────────────────────
        if (username.isEmpty() || pass.isEmpty() || email.isEmpty()) {
            showError("Vui lòng điền đầy đủ thông tin!");
            return;
        }
        if (username.length() < 4) {
            showError("Tên đăng nhập phải có ít nhất 4 ký tự.");
            return;
        }
        if (pass.length() < 6) {
            showError("Mật khẩu phải có ít nhất 6 ký tự.");
            return;
        }
        if (!pass.equals(confirm)) {
            showError("Mật khẩu xác nhận không khớp!");
            txtConfirm.setText("");
            return;
        }
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showError("Địa chỉ email không hợp lệ.");
            return;
        }

        // ── Gọi DAO ──────────────────────────────────────────────────────────
        TaiKhoan newAcc = new TaiKhoan();
        newAcc.setTenDangNhap(username);
        newAcc.setMatKhau(pass);
        newAcc.setVaiTro(role);
        newAcc.setEmail(email);

        int result = dao.register(newAcc);
        switch (result) {
            case 0:
                JOptionPane.showMessageDialog(this,
                        "Đăng ký thành công!\nVai trò: " + role + "\nBạn có thể đăng nhập ngay bây giờ.",
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                returnToLogin();
                break;
            case -1:
                showError("Tên đăng nhập \"" + username + "\" đã tồn tại. Vui lòng chọn tên khác.");
                txtUser.requestFocus();
                break;
            default:
                showError("Đăng ký thất bại do lỗi hệ thống. Vui lòng thử lại sau.");
        }
    }

    // ─── Quay lại màn hình đăng nhập ─────────────────────────────────────────
    private void returnToLogin() {
        if (prevFrame != null) prevFrame.setVisible(true);
        this.dispose();
    }

    // ─── Helpers UI ──────────────────────────────────────────────────────────
    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(FG_LABEL);
        return l;
    }

    private JComponent styleField(JTextField field) {
        field.setBackground(new Color(45, 46, 55));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 80)),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
        return field;
    }

    private void styleCombo(JComboBox<String> combo) {
        combo.setBackground(new Color(45, 46, 55));
        combo.setForeground(Color.WHITE);
        combo.setFont(FONT_LABEL);
    }

    private JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BTN);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}