package ui;

import dao.KhachThueDAO;
import dao.TaiKhoanDAO;
import model.KhachThue;
import model.TaiKhoan;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

/**
 * Dialog thêm/sửa Khách Thuê.
 * - MaKhach là IDENTITY (tự tăng trong DB) → không hiển thị trường nhập.
 * - Có ô "Tài khoản liên kết" (TenDN) để gán thẳng vào bảng KhachThue.
 */
public class KhachThueDialog extends JDialog {

    // ── Form fields ────────────────────────────────────────────────────────────
    private final JTextField txtTen    = new JTextField();
    private final JTextField txtCCCD   = new JTextField();
    private final JTextField txtSDT    = new JTextField();
    private final JTextField txtQueQuan= new JTextField();
    private final JTextField txtPhong  = new JTextField();
    private final JComboBox<String> cbStatus =
            new JComboBox<>(new String[]{"Đang ở", "Đã rời"});

    // ── Phần tài khoản liên kết ────────────────────────────────────────────────
    private final JCheckBox  chkAccount = new JCheckBox("Tạo / Cập nhật tài khoản liên kết");
    private final JComboBox<String> cbUser = new JComboBox<>();
    private final JPasswordField txtPass= new JPasswordField();

    // ── DAO ────────────────────────────────────────────────────────────────────
    private final KhachThueDAO ktDao = new KhachThueDAO();
    private final TaiKhoanDAO  tkDao = new TaiKhoanDAO();
    private final KhachThue existingData;
    private final boolean    isEdit;

    // ── Màu chủ đề ────────────────────────────────────────────────────────────
    private static final Color BG_DARK    = new Color(30, 31, 38);
    private static final Color BG_CARD    = new Color(45, 46, 55);
    private static final Color ACCENT     = new Color(52, 152, 219);
    private static final Color ACCENT_GRN = new Color(46, 204, 113);
    private static final Color FG_LABEL   = new Color(180, 180, 190);
    private static final Font  FONT_TITLE = new Font("Segoe UI", Font.BOLD, 18);
    private static final Font  FONT_LABEL = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font  FONT_BTN   = new Font("Segoe UI", Font.BOLD, 13);

    // ── Constructor ───────────────────────────────────────────────────────────
    public KhachThueDialog(Frame parent, boolean modal, KhachThue data) {
        super(parent, modal);
        this.existingData = data;
        this.isEdit       = (data != null);

        setTitle(isEdit ? "✏️ CẬP NHẬT KHÁCH THUÊ" : "➕ THÊM KHÁCH THUÊ MỚI");
        setSize(480, 610);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 70)));
        setContentPane(root);

        // Title bar
        JLabel lblTitle = new JLabel(
                isEdit ? "✏️ CẬP NHẬT THÔNG TIN KHÁCH THUÊ" : "➕ THÊM KHÁCH THUÊ MỚI",
                SwingConstants.CENTER);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(ACCENT);
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new Color(22, 23, 28));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        root.add(lblTitle, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_DARK);
        form.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 25));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(6, 6, 6, 6);
        g.fill   = GridBagConstraints.HORIZONTAL;
        int row  = 0;

        // Họ tên
        addRow(form, g, row++, "Họ và tên (*):", txtTen);
        // CCCD
        addRow(form, g, row++, "Số CCCD:", txtCCCD);
        // SĐT
        addRow(form, g, row++, "Số điện thoại:", txtSDT);
        // Quê quán
        addRow(form, g, row++, "Quê quán:", txtQueQuan);
        // Mã phòng
        addRow(form, g, row++, "Mã phòng (*):", txtPhong);
        // Trạng thái
        g.gridx = 0; g.gridy = row; g.weightx = 0.35;
        form.add(makeLabel("Trạng thái:"), g);
        g.gridx = 1; g.weightx = 0.65;
        styleCombo(cbStatus);
        form.add(cbStatus, g);
        row++;

        // ── Phần tài khoản ──────────────────────────────────────────────────
        g.gridx = 0; g.gridy = row; g.gridwidth = 2;
        g.insets = new Insets(14, 6, 6, 6);
        chkAccount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkAccount.setForeground(ACCENT_GRN);
        chkAccount.setOpaque(false);
        form.add(chkAccount, g);
        g.gridwidth = 1;
        g.insets = new Insets(6, 6, 6, 6);
        row++;

        addRow(form, g, row++, "Tên đăng nhập:", cbUser);
        addRow(form, g, row++, "Mật khẩu:", txtPass);

        root.add(form, BorderLayout.CENTER);

        // Buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 12));
        actions.setBackground(new Color(22, 23, 28));
        actions.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(60, 60, 70)));
        JButton btnCancel = makeBtn("HỦY BỎ", new Color(100, 100, 110), Color.WHITE);
        JButton btnSave   = makeBtn("LƯU DỮ LIỆU", ACCENT, Color.WHITE);
        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> handleSave());
        actions.add(btnCancel);
        actions.add(btnSave);
        root.add(actions, BorderLayout.SOUTH);

        // Toggle account fields
        chkAccount.addActionListener(e -> toggleAccountFields(chkAccount.isSelected()));

        prefillData();
    }

    // ── Điền dữ liệu (khi Edit) ──────────────────────────────────────────────
    private void loadAccountsList() {
        cbUser.removeAllItems();
        String currentLinked = (existingData != null) ? existingData.getTenDN() : null;
        java.util.List<String> list = tkDao.getUnlinkedKhachThueAccounts(currentLinked);
        
        cbUser.addItem(""); 
        for (String acc : list) {
            cbUser.addItem(acc);
        }
        
        if (currentLinked != null && !currentLinked.isEmpty()) {
            cbUser.setSelectedItem(currentLinked);
        } else {
            cbUser.setSelectedItem("");
        }
    }

    private void prefillData() {
        cbUser.setEditable(true);
        loadAccountsList();
        styleField(cbUser);

        if (isEdit && existingData != null) {
            txtTen.setText(existingData.getHoTen());
            txtCCCD.setText(existingData.getCccd());
            txtSDT.setText(existingData.getSdt());
            txtQueQuan.setText(existingData.getQueQuan() != null ? existingData.getQueQuan() : "");
            txtPhong.setText(existingData.getMaPhong());
            cbStatus.setSelectedItem(existingData.getTrangThai());

            // Điền thông tin tài khoản đã liên kết (nếu có)
            String tenDN = existingData.getTenDN();
            if (tenDN != null && !tenDN.isEmpty()) {
                TaiKhoan tk = tkDao.findById(tenDN);
                if (tk != null) {
                    chkAccount.setSelected(true);
                    cbUser.setSelectedItem(tk.getTenDangNhap());
                    txtPass.setText(tk.getMatKhau());
                    toggleAccountFields(true);
                    return;
                }
            }
            chkAccount.setSelected(false);
            toggleAccountFields(false);
        } else {
            // Khi thêm mới: mặc định bật checkbox tạo tài khoản
            chkAccount.setSelected(true);
            toggleAccountFields(true);
        }
    }

    private void toggleAccountFields(boolean enabled) {
        cbUser.setEnabled(enabled);
        txtPass.setEnabled(enabled);
        Color bg = enabled ? BG_CARD : new Color(35, 36, 42);
        cbUser.setBackground(bg);
        txtPass.setBackground(bg);
        if (cbUser.isEditable()) {
            Component editor = cbUser.getEditor().getEditorComponent();
            if (editor instanceof JTextField) {
                editor.setBackground(bg);
            }
        }
    }

    // ── Lưu dữ liệu ──────────────────────────────────────────────────────────
    private void handleSave() {
        String ten   = txtTen.getText().trim();
        String cccd  = txtCCCD.getText().trim();
        String sdt   = txtSDT.getText().trim();
        String queQuan = txtQueQuan.getText().trim();
        String phong = txtPhong.getText().trim();
        String status= (String) cbStatus.getSelectedItem();

        if (ten.isEmpty() || phong.isEmpty()) {
            showError("Vui lòng nhập ít nhất Họ tên và Mã phòng!");
            return;
        }

        // Kiểm tra trùng CCCD trong danh sách của chủ trọ hiện tại
        if (!cccd.isEmpty() && ktDao.hasDuplicateCccd(cccd, isEdit ? existingData.getMaKhach() : null)) {
            showError("Trùng số CCCD!");
            return;
        }

        // Kiểm tra trùng SĐT trong danh sách của chủ trọ hiện tại
        if (!sdt.isEmpty() && ktDao.hasDuplicateSdt(sdt, isEdit ? existingData.getMaKhach() : null)) {
            showError("Trùng số điện thoại!");
            return;
        }

        // Xác định TenDN từ ô tài khoản
        String tenDN = "";
        if (chkAccount.isSelected()) {
            Object selectedObj = cbUser.getSelectedItem();
            tenDN = (selectedObj != null) ? selectedObj.toString().trim() : "";
            String pass = new String(txtPass.getPassword());
            if (tenDN.isEmpty() || pass.isEmpty()) {
                showError("Vui lòng điền đầy đủ Tên đăng nhập và Mật khẩu!");
                return;
            }
        }

        // Tạo object KhachThue
        KhachThue kt = new KhachThue();
        if (isEdit) kt.setMaKhach(existingData.getMaKhach());
        kt.setHoTen(ten);
        kt.setCccd(cccd);
        kt.setSdt(sdt);
        kt.setQueQuan(queQuan);
        kt.setMaPhong(phong);
        kt.setPhiDichVu(existingData != null ? existingData.getPhiDichVu() : 0);
        kt.setTrangThai(status);
        kt.setTenDN(tenDN);

        // Lưu vào DB
        try {
            boolean ok = isEdit ? ktDao.update(kt) : ktDao.insert(kt);
            if (!ok) { showError("Lưu thông tin Khách thuê thất bại!"); return; }
        } catch (SQLException ex) {
            showError("Lỗi CSDL:\n" + ex.getMessage());
            return;
        }

        // Tạo / cập nhật tài khoản liên kết
        if (chkAccount.isSelected()) {
            try {
                Object selectedObj = cbUser.getSelectedItem();
                String user = (selectedObj != null) ? selectedObj.toString().trim() : "";
                String pass = new String(txtPass.getPassword());
                if (!user.isEmpty() && !pass.isEmpty()) {
                    TaiKhoan tk = new TaiKhoan();
                    tk.setTenDangNhap(user);
                    tk.setMatKhau(pass);
                    tk.setVaiTro("Khách thuê");
                    tk.setEmail(user + "@phongtro.com");

                    TaiKhoan exist = tkDao.findById(user);
                    boolean accountOk = false;
                    if (exist != null) {
                        accountOk = tkDao.update(tk);
                    } else {
                        accountOk = tkDao.insert(tk);
                    }
                    if (!accountOk) {
                        JOptionPane.showMessageDialog(this, 
                            "⚠️ Cảnh báo: Đã lưu khách thuê nhưng tạo tài khoản liên kết thất bại!",
                            "Cảnh báo", JOptionPane.WARNING_MESSAGE);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, 
                    "⚠️ Cảnh báo: Đã lưu khách thuê nhưng lỗi tạo tài khoản liên kết: " + ex.getMessage(),
                    "Cảnh báo", JOptionPane.WARNING_MESSAGE);
            }
        }

        JOptionPane.showMessageDialog(this, "✅ Lưu thành công!", "Thành công",
                JOptionPane.INFORMATION_MESSAGE);
        dispose();
    }

    // ── Helpers UI ────────────────────────────────────────────────────────────
    private void addRow(JPanel form, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.weightx = 0.35;
        form.add(makeLabel(label), g);
        g.gridx = 1; g.weightx = 0.65;
        styleField(field);
        form.add(field, g);
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_LABEL);
        l.setForeground(FG_LABEL);
        return l;
    }

    private void styleField(JComponent f) {
        f.setBackground(BG_CARD);
        f.setForeground(Color.WHITE);
        if (f instanceof JTextField) {
            ((JTextField) f).setCaretColor(Color.WHITE);
            f.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(70, 70, 80)),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        } else if (f instanceof JComboBox) {
            JComboBox<?> cb = (JComboBox<?>) f;
            cb.setFont(FONT_LABEL);
            if (cb.isEditable()) {
                Component editor = cb.getEditor().getEditorComponent();
                if (editor instanceof JTextField) {
                    editor.setBackground(BG_CARD);
                    editor.setForeground(Color.WHITE);
                    ((JTextField) editor).setCaretColor(Color.WHITE);
                    ((JTextField) editor).setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
                }
            }
        }
        f.setFont(FONT_LABEL);
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setBackground(BG_CARD);
        cb.setForeground(Color.WHITE);
        cb.setFont(FONT_LABEL);
    }

    private JButton makeBtn(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setFont(FONT_BTN);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(140, 36));
        return b;
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}