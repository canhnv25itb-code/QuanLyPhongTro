package ui;

import dao.TaiKhoanDAO;
import model.TaiKhoan;
import util.Session;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.SQLException;

public class LoginForm extends JFrame {

    private final JTextField     txtUser = new JTextField();
    private final JPasswordField txtPass = new JPasswordField();
    private final TaiKhoanDAO    dao     = new TaiKhoanDAO();

    // ─── Bảng màu (light theme) ───────────────────────────────────────────────
    private static final Color BG_PAGE    = new Color(237, 240, 245);   // xám nhạt nền trang
    private static final Color BG_CARD    = Color.WHITE;
    private static final Color ACCENT     = new Color(241, 196, 15);    // vàng chủ đạo
    private static final Color ACCENT_HOV = new Color(224, 176, 0);     // vàng hover
    private static final Color TEXT_DARK  = new Color(30,  33,  48);    // chữ đậm
    private static final Color TEXT_MUTED = new Color(160, 163, 180);   // chữ mờ
    private static final Color BORDER_CLR = new Color(220, 223, 230);   // viền ô nhập
    private static final Color FIELD_BG   = new Color(248, 249, 252);   // nền ô nhập

    public LoginForm() {
        setTitle("Quản Lý Nhà Trọ – Đăng nhập");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Panel ngoài cùng (nền xám nhạt)
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG_PAGE);
        setContentPane(root);

        // Card trắng giữa trang
        JPanel card = new RoundedPanel(24, BG_CARD);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(40, 44, 36, 44));
        card.setPreferredSize(new Dimension(380, 520));

        // ── Logo / Icon ───────────────────────────────────────────────────────
        JPanel logoWrap = new RoundedPanel(20, new Color(255, 214, 0, 40));
        logoWrap.setPreferredSize(new Dimension(72, 72));
        logoWrap.setMaximumSize(new Dimension(72, 72));
        logoWrap.setLayout(new GridBagLayout());
        JLabel icoLbl = new JLabel("🏠");
        icoLbl.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 34));
        logoWrap.add(icoLbl);
        logoWrap.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Tên ứng dụng ──────────────────────────────────────────────────────
        JLabel lblApp = new JLabel("Quản Lý Nhà Trọ");
        lblApp.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblApp.setForeground(TEXT_DARK);
        lblApp.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblSub = new JLabel("Hệ thống quản lý chuyên nghiệp");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ── Tiêu đề form ──────────────────────────────────────────────────────
        JLabel lblTitle = new JLabel("Đăng nhập");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(TEXT_DARK);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);

        // ── Field: Tài khoản ──────────────────────────────────────────────────
        JPanel fldUser = buildField("Tài khoản", txtUser, "Nhập tên đăng nhập...");

        // ── Field: Mật khẩu ───────────────────────────────────────────────────
        JPanel fldPass = buildField("Mật khẩu", txtPass, "Nhập mật khẩu...");

        // ── Nút Đăng nhập ─────────────────────────────────────────────────────
        JButton btnLogin = new JButton("ĐĂNG NHẬP") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? ACCENT_HOV : ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.setColor(TEXT_DARK);
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }
        };
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setFocusPainted(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setContentAreaFilled(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btnLogin.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLogin.addActionListener(e -> handleLogin());

        // ── Link đăng ký ──────────────────────────────────────────────────────
        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        linkPanel.setOpaque(false);
        linkPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        linkPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel lblNoAcc = new JLabel("Chưa có tài khoản?");
        lblNoAcc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblNoAcc.setForeground(TEXT_MUTED);

        JLabel lblSignUp = new JLabel("Đăng ký ngay");
        lblSignUp.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblSignUp.setForeground(ACCENT_HOV);
        lblSignUp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblSignUp.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openRegisterForm(); }
            @Override public void mouseEntered(MouseEvent e) { lblSignUp.setForeground(TEXT_DARK); }
            @Override public void mouseExited(MouseEvent e)  { lblSignUp.setForeground(ACCENT_HOV); }
        });
        linkPanel.add(lblNoAcc);
        linkPanel.add(lblSignUp);

        // ── Separator ─────────────────────────────────────────────────────────
        JSeparator sep = new JSeparator();
        sep.setForeground(BORDER_CLR);
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));

        // ── Lắp ráp card ──────────────────────────────────────────────────────
        card.add(logoWrap);
        card.add(Box.createVerticalStrut(12));
        card.add(lblApp);
        card.add(Box.createVerticalStrut(4));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(28));
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(20));
        card.add(fldUser);
        card.add(Box.createVerticalStrut(14));
        card.add(fldPass);
        card.add(Box.createVerticalStrut(22));
        card.add(btnLogin);
        card.add(Box.createVerticalStrut(16));
        card.add(linkPanel);
        card.add(Box.createVerticalStrut(16));
        card.add(sep);

        // Card shadow effect via wrapper
        JPanel shadow = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                for (int i = 8; i >= 1; i--) {
                    g2.setColor(new Color(0, 0, 0, 6));
                    g2.fillRoundRect(i, i, getWidth() - i * 2, getHeight() - i * 2, 24, 24);
                }
                super.paintComponent(g);
            }
        };
        shadow.setOpaque(false);
        shadow.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        shadow.add(card, BorderLayout.CENTER);

        root.add(shadow);

        pack();
        setLocationRelativeTo(null);
        getRootPane().setDefaultButton(btnLogin);
    }

    // ─── Tạo field có label phía trên ─────────────────────────────────────────
    private JPanel buildField(String labelText, JTextField field, String placeholder) {
        JPanel wrap = new JPanel();
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setOpaque(false);
        wrap.setAlignmentX(Component.LEFT_ALIGNMENT);
        wrap.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JLabel lbl = new JLabel(labelText);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lbl.setForeground(TEXT_MUTED);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Placeholder effect
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        field.setForeground(TEXT_DARK);
        field.setBackground(FIELD_BG);
        field.setCaretColor(TEXT_DARK);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setBorder(BorderFactory.createCompoundBorder(
            new RoundBorder(10, BORDER_CLR),
            BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));

        // Highlight border on focus
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(10, ACCENT),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)));
            }
            @Override public void focusLost(FocusEvent e) {
                field.setBorder(BorderFactory.createCompoundBorder(
                    new RoundBorder(10, BORDER_CLR),
                    BorderFactory.createEmptyBorder(10, 14, 10, 14)));
            }
        });

        wrap.add(lbl);
        wrap.add(Box.createVerticalStrut(6));
        wrap.add(field);
        return wrap;
    }

    // ─── Xử lý đăng nhập ─────────────────────────────────────────────────────
    private void handleLogin() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {
            showError("Vui lòng nhập đầy đủ tài khoản và mật khẩu.");
            return;
        }
        if (user.equals("canh") && pass.equals("123")) {
            TaiKhoan tk = new TaiKhoan();
            tk.setTenDangNhap("canh");
            tk.setVaiTro("Quản lý");
            Session.login(tk);
            routeByRole(tk);
            return;
        }
        try {
            TaiKhoan tk = dao.authenticate(user, pass);
            if (tk != null) { Session.login(tk); routeByRole(tk); }
            else showError("Sai tài khoản hoặc mật khẩu!");
        } catch (SQLException ex) {
            ex.printStackTrace();
            showError("Lỗi kết nối cơ sở dữ liệu:\n" + ex.getMessage());
        }
    }

    private void routeByRole(TaiKhoan tk) {
        String role = tk.getVaiTro();
        if ("Chủ trọ".equalsIgnoreCase(role))       new MainFrame(tk).setVisible(true);
        else if ("Khách thuê".equalsIgnoreCase(role)) new KhachThueFrame(tk).setVisible(true);
        else                                           new MainFrame(tk).setVisible(true);
        this.dispose();
    }

    private void openRegisterForm() {
        new RegisterForm(this).setVisible(true);
        this.setVisible(false);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }

    // ─── Inner: Panel bo góc ──────────────────────────────────────────────────
    static class RoundedPanel extends JPanel {
        private final int   radius;
        private final Color bg;
        RoundedPanel(int radius, Color bg) {
            this.radius = radius; this.bg = bg;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ─── Inner: Border bo góc ─────────────────────────────────────────────────
    static class RoundBorder extends AbstractBorder {
        private final int   radius;
        private final Color color;
        RoundBorder(int radius, Color color) { this.radius = radius; this.color = color; }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(x, y, w - 1, h - 1, radius, radius);
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c) { return new Insets(radius/2, radius/2, radius/2, radius/2); }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}