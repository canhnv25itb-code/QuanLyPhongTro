package ui;

import dao.HopDongDAO;
import model.HopDong;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class HopDongPanel extends JPanel {

    // ── Bảng & Model ──────────────────────────────────────────
    private JTable table;
    private DefaultTableModel tableModel;
    private HopDongDAO hdDao = new HopDongDAO();

    // ── Màu chủ đề ────────────────────────────────────────────
    private static final Color BG_DARK    = new Color(18, 19, 24);
    private static final Color BG_CARD    = new Color(30, 31, 38);
    private static final Color BG_HEADER  = new Color(40, 41, 48);
    private static final Color ACCENT     = new Color(241, 196, 15);
    private static final Color GREEN      = new Color(46, 204, 113);
    private static final Color RED        = new Color(231, 76, 60);
    private static final Color BLUE       = new Color(52, 152, 219);
    private static final Color TEXT_WHITE = Color.WHITE;
    private static final Color TEXT_GRAY  = new Color(160, 160, 160);
    private static final Color BORDER     = new Color(55, 55, 65);

    private static final String[] COLS = {
        "#", "PHÒNG", "KHÁCH THUÊ", "NGÀY BẮT ĐẦU", "NGÀY KẾT THÚC",
        "ĐẶT CỌC (VNĐ)", "TRẠNG THÁI", "HÀNH ĐỘNG"
    };

    private String maPhongFilter = null;

    public HopDongPanel() {
        this(null);
    }

    public HopDongPanel(String maPhongFilter) {
        this.maPhongFilter = maPhongFilter;
        setBackground(BG_DARK);
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        loadData();
    }

    // ═══════════════════════════════════════════════════════════
    //  UI BUILDERS
    // ═══════════════════════════════════════════════════════════

    private JPanel buildHeader() {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setOpaque(false);
        pnl.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));

        JLabel lbl = new JLabel(maPhongFilter == null ? "📋  Quản lý Hợp đồng thuê" : "📋  Thông tin Hợp đồng của bạn");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(TEXT_WHITE);

        JButton btnAdd = makeButton("＋  Tạo hợp đồng", ACCENT, Color.BLACK);
        btnAdd.addActionListener(e -> openDialog(null));

        pnl.add(lbl, BorderLayout.WEST);
        if (maPhongFilter == null) {
            pnl.add(btnAdd, BorderLayout.EAST);
        }
        return pnl;
    }

    private JScrollPane buildTablePanel() {
        String[] cols;
        if (maPhongFilter != null) {
            cols = new String[]{"#", "PHÒNG", "KHÁCH THUÊ", "NGÀY BẮT ĐẦU", "NGÀY KẾT THÚC", "ĐẶT CỌC (VNĐ)", "TRẠNG THÁI"};
        } else {
            cols = COLS;
        }

        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return maPhongFilter == null && c == 7; }
        };

        table = new JTable(tableModel);
        table.setRowHeight(46);
        table.setBackground(BG_CARD);
        table.setForeground(TEXT_WHITE);
        table.setGridColor(BORDER);
        table.setSelectionBackground(new Color(60, 62, 75));
        table.setSelectionForeground(TEXT_WHITE);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader header = table.getTableHeader();
        header.setBackground(BG_HEADER);
        header.setForeground(TEXT_GRAY);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setReorderingAllowed(false);
        header.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));

        // Độ rộng cột
        if (maPhongFilter == null) {
            int[] widths = {40, 70, 160, 115, 115, 130, 110, 140};
            for (int i = 0; i < widths.length; i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }
            table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
            table.getColumnModel().getColumn(7).setCellRenderer(new ActionRenderer());
            table.getColumnModel().getColumn(7).setCellEditor(new ActionEditor());
        } else {
            int[] widths = {40, 70, 160, 120, 120, 140, 120};
            for (int i = 0; i < widths.length; i++) {
                table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
            }
            table.getColumnModel().getColumn(6).setCellRenderer(new StatusRenderer());
        }

        // Căn giữa cột số
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        centerRenderer.setBackground(BG_CARD);
        centerRenderer.setForeground(TEXT_WHITE);
        table.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        table.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        // Row renderer nền xen kẽ
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(BG_CARD);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.getVerticalScrollBar().setBackground(BG_DARK);
        return sp;
    }

    // ═══════════════════════════════════════════════════════════
    //  DATA
    // ═══════════════════════════════════════════════════════════

    public void loadData() {
        tableModel.setRowCount(0);
        try {
            List<HopDong> list = hdDao.getAll();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (HopDong hd : list) {
                if (maPhongFilter != null && !hd.getMaPhong().equalsIgnoreCase(maPhongFilter)) {
                    continue;
                }
                if (maPhongFilter != null) {
                    tableModel.addRow(new Object[]{
                        hd.getMaHopDong(),
                        hd.getMaPhong()    != null ? hd.getMaPhong()    : "—",
                        hd.getTenKhach()   != null ? hd.getTenKhach()   : "—",
                        hd.getNgayBatDau() != null ? hd.getNgayBatDau().format(fmt) : "—",
                        hd.getNgayKetThuc()!= null ? hd.getNgayKetThuc().format(fmt): "—",
                        String.format("%,.0f", hd.getTienCoc()),
                        hd.getTrangThai()  != null ? hd.getTrangThai()  : "—"
                    });
                } else {
                    tableModel.addRow(new Object[]{
                        hd.getMaHopDong(),
                        hd.getMaPhong()    != null ? hd.getMaPhong()    : "—",
                        hd.getTenKhach()   != null ? hd.getTenKhach()   : "—",
                        hd.getNgayBatDau() != null ? hd.getNgayBatDau().format(fmt) : "—",
                        hd.getNgayKetThuc()!= null ? hd.getNgayKetThuc().format(fmt): "—",
                        String.format("%,.0f", hd.getTienCoc()),
                        hd.getTrangThai()  != null ? hd.getTrangThai()  : "—",
                        "actions"
                    });
                }
            }
        } catch (Exception e) {
            showError("Lỗi tải dữ liệu: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  DIALOG THÊM / SỬA
    // ═══════════════════════════════════════════════════════════

    /**
     * @param hd null → chế độ Thêm mới; != null → chế độ Sửa
     */
    private void openDialog(HopDong hd) {
        boolean isEdit = (hd != null);
        JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this),
                isEdit ? "✏️  Sửa hợp đồng" : "➕  Tạo hợp đồng mới",
                Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(460, 440);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(BG_CARD);
        root.setBorder(BorderFactory.createLineBorder(BORDER));
        dlg.setContentPane(root);

        // ── Title bar ──
        JLabel title = new JLabel(isEdit ? "  ✏️  Sửa hợp đồng" : "  ➕  Tạo hợp đồng mới");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(TEXT_WHITE);
        title.setOpaque(true);
        title.setBackground(BG_HEADER);
        title.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 0));
        root.add(title, BorderLayout.NORTH);

        // ── Form ──
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG_CARD);
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 12, 24));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 4, 7, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField tfMaPhong    = formField(isEdit ? (hd.getMaPhong()    != null ? hd.getMaPhong()    : "") : "");
        JTextField tfTenKhach   = formField(isEdit ? (hd.getTenKhach()   != null ? hd.getTenKhach()   : "") : "");
        JTextField tfNgayBD     = formField(isEdit && hd.getNgayBatDau()  != null ? hd.getNgayBatDau().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))  : "");
        JTextField tfNgayKT     = formField(isEdit && hd.getNgayKetThuc() != null ? hd.getNgayKetThuc().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "");
        JTextField tfTienCoc    = formField(isEdit ? String.valueOf((long) hd.getTienCoc()) : "");

        String[] trangThaiOptions = {"Đang hiệu lực", "Đã hủy", "Hết hạn"};
        JComboBox<String> cbTrangThai = new JComboBox<>(trangThaiOptions);
        styleCombo(cbTrangThai);
        if (isEdit && hd.getTrangThai() != null) cbTrangThai.setSelectedItem(hd.getTrangThai());

        String[][] rows = {
            {"Mã phòng:", null},
            {"Tên khách:", null},
            {"Ngày bắt đầu (dd/MM/yyyy):", null},
            {"Ngày kết thúc (dd/MM/yyyy):", null},
            {"Tiền cọc (VNĐ):", null},
            {"Trạng thái:", null}
        };
        Component[] fields = {tfMaPhong, tfTenKhach, tfNgayBD, tfNgayKT, tfTienCoc, cbTrangThai};

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.35;
            JLabel lbl = new JLabel(rows[i][0]);
            lbl.setForeground(TEXT_GRAY);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            form.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 0.65;
            form.add(fields[i], gbc);
        }

        root.add(form, BorderLayout.CENTER);

        // ── Buttons ──
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 12));
        btnPanel.setBackground(BG_HEADER);
        btnPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER));

        JButton btnCancel = makeButton("Hủy bỏ", new Color(70, 72, 82), TEXT_WHITE);
        JButton btnSave   = makeButton(isEdit ? "💾  Lưu thay đổi" : "✅  Tạo hợp đồng", ACCENT, Color.BLACK);

        btnCancel.addActionListener(e -> dlg.dispose());

        btnSave.addActionListener(e -> {
            // ── Validate ──
            String maPhong  = tfMaPhong.getText().trim();
            String tenKhach = tfTenKhach.getText().trim();
            String ngayBDStr= tfNgayBD.getText().trim();
            String ngayKTStr= tfNgayKT.getText().trim();
            String tienCocStr = tfTienCoc.getText().trim();

            if (maPhong.isEmpty() || tenKhach.isEmpty() || ngayBDStr.isEmpty() || ngayKTStr.isEmpty() || tienCocStr.isEmpty()) {
                showError("Vui lòng điền đầy đủ thông tin!"); return;
            }

            LocalDate ngayBD, ngayKT;
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                ngayBD = LocalDate.parse(ngayBDStr, fmt);
                ngayKT = LocalDate.parse(ngayKTStr, fmt);
            } catch (DateTimeParseException ex) {
                showError("Ngày không đúng định dạng dd/MM/yyyy!"); return;
            }

            if (!ngayKT.isAfter(ngayBD)) {
                showError("Ngày kết thúc phải sau ngày bắt đầu!"); return;
            }

            double tienCoc;
            try { tienCoc = Double.parseDouble(tienCocStr.replaceAll("[^\\d.]", "")); }
            catch (NumberFormatException ex) { showError("Tiền cọc không hợp lệ!"); return; }

            // ── Build object ──
            HopDong obj = isEdit ? hd : new HopDong();
            obj.setMaPhong(maPhong);
            obj.setTenKhach(tenKhach);
            obj.setNgayBatDau(ngayBD);
            obj.setNgayKetThuc(ngayKT);
            obj.setTienCoc(tienCoc);
            obj.setTrangThai((String) cbTrangThai.getSelectedItem());

            boolean ok = isEdit ? hdDao.sua(obj) : hdDao.them(obj);

            if (ok) {
                dlg.dispose();
                loadData();
                showInfo(isEdit ? "Cập nhật hợp đồng thành công!" : "Tạo hợp đồng mới thành công!");
            } else {
                showError(isEdit ? "Cập nhật thất bại!" : "Tạo hợp đồng thất bại!");
            }
        });

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        root.add(btnPanel, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }

    // ═══════════════════════════════════════════════════════════
    //  RENDERERS & EDITORS
    // ═══════════════════════════════════════════════════════════

    /** Render màu xen kẽ cho các hàng */
    class AlternatingRowRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            setForeground(TEXT_WHITE);
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
            if (!sel) setBackground(r % 2 == 0 ? BG_CARD : new Color(35, 36, 44));
            else       setBackground(new Color(60, 62, 78));
            return this;
        }
    }

    /** Badge màu cho cột Trạng thái */
    class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            String text = v != null ? v.toString() : "";
            JLabel lbl = new JLabel(text, SwingConstants.CENTER);
            lbl.setOpaque(true);
            lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
            lbl.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            lbl.setBackground(r % 2 == 0 ? BG_CARD : new Color(35, 36, 44));

            if (text.contains("hiệu lực")) {
                lbl.setForeground(GREEN);
            } else if (text.contains("hủy") || text.contains("Hủy")) {
                lbl.setForeground(RED);
            } else {
                lbl.setForeground(TEXT_GRAY);
            }
            return lbl;
        }
    }

    /** Renderer: hiển thị 3 nút Sửa / Xóa / Hủy */
    class ActionRenderer implements TableCellRenderer {
        private final JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));

        public ActionRenderer() {
            pnl.setOpaque(true);
            pnl.add(makeSmallButton("✏️", BLUE));
            pnl.add(makeSmallButton("🗑", RED));
            pnl.add(makeSmallButton("🚫", new Color(150, 80, 200)));
        }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            pnl.setBackground(r % 2 == 0 ? BG_CARD : new Color(35, 36, 44));
            return pnl;
        }

        private JButton makeSmallButton(String text, Color bg) {
            JButton b = new JButton(text);
            b.setBackground(bg);
            b.setForeground(Color.WHITE);
            b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setPreferredSize(new Dimension(36, 28));
            return b;
        }
    }

    /** Editor: xử lý click vào 3 nút Sửa / Xóa / Hủy */
    class ActionEditor extends AbstractCellEditor implements TableCellEditor {
        private final JPanel pnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 6));
        private int currentRow;

        public ActionEditor() {
            pnl.setOpaque(true);
            pnl.setBackground(BG_CARD);

            JButton btnSua  = makeSmallButton("✏️", BLUE);
            JButton btnXoa  = makeSmallButton("🗑", RED);
            JButton btnHuy  = makeSmallButton("🚫", new Color(150, 80, 200));

            // ── SỬA ──
            btnSua.addActionListener(e -> {
                fireEditingStopped();
                int maHD = Integer.parseInt(tableModel.getValueAt(currentRow, 0).toString());
                HopDong hd = hdDao.getById(maHD);
                if (hd != null) openDialog(hd);
                else showError("Không tìm thấy hợp đồng!");
            });

            // ── XÓA ──
            btnXoa.addActionListener(e -> {
                fireEditingStopped();
                int maHD = Integer.parseInt(tableModel.getValueAt(currentRow, 0).toString());
                int confirm = JOptionPane.showConfirmDialog(
                        HopDongPanel.this,
                        "Bạn có chắc muốn XÓA VĨNH VIỄN hợp đồng #" + maHD + "?",
                        "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (hdDao.xoa(maHD)) { loadData(); showInfo("Đã xóa hợp đồng #" + maHD); }
                    else showError("Xóa thất bại!");
                }
            });

            // ── HỦY (đổi trạng thái) ──
            btnHuy.addActionListener(e -> {
                fireEditingStopped();
                int maHD = Integer.parseInt(tableModel.getValueAt(currentRow, 0).toString());
                int confirm = JOptionPane.showConfirmDialog(
                        HopDongPanel.this,
                        "Hủy hợp đồng #" + maHD + "? (Trạng thái sẽ đổi thành \"Đã hủy\")",
                        "Xác nhận hủy", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (hdDao.huyHopDong(maHD)) { loadData(); showInfo("Đã hủy hợp đồng #" + maHD); }
                    else showError("Hủy thất bại!");
                }
            });

            pnl.add(btnSua);
            pnl.add(btnXoa);
            pnl.add(btnHuy);
        }

        @Override
        public Component getTableCellEditorComponent(JTable t, Object v, boolean sel, int r, int c) {
            currentRow = r;
            return pnl;
        }

        @Override public Object getCellEditorValue() { return "actions"; }

        private JButton makeSmallButton(String text, Color bg) {
            JButton b = new JButton(text);
            b.setBackground(bg);
            b.setForeground(Color.WHITE);
            b.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
            b.setBorderPainted(false);
            b.setFocusPainted(false);
            b.setPreferredSize(new Dimension(36, 28));
            return b;
        }
    }

    // ═══════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════

    private JButton makeButton(String text, Color bg, Color fg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        return b;
    }

    private JTextField formField(String val) {
        JTextField tf = new JTextField(val);
        tf.setBackground(new Color(22, 23, 30));
        tf.setForeground(TEXT_WHITE);
        tf.setCaretColor(TEXT_WHITE);
        tf.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return tf;
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setBackground(new Color(22, 23, 30));
        cb.setForeground(TEXT_WHITE);
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cb.setBorder(BorderFactory.createLineBorder(BORDER));
        cb.setFocusable(false);
    }

    private void showInfo(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Thành công", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Lỗi", JOptionPane.ERROR_MESSAGE);
    }
}