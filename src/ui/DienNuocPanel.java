package ui;

import dao.DienNuocDAO;
import model.DienNuoc;
import util.DataRefreshEvent;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

public class DienNuocPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private DienNuocDAO dnDao = new DienNuocDAO();
    private JComboBox<String> cboThangNam;
    private JLabel lblGiaHienTai;
    private String maPhongFilter = null;


    public DienNuocPanel() {
        this(null);
    }

    public DienNuocPanel(String maPhongFilter) {
        this.maPhongFilter = maPhongFilter;
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(18, 19, 24));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // --- Header & Công cụ ---
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("⚡ Quản lý Điện & Nước");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(Color.WHITE);

        // --- COMBOBOX LỌC THÁNG ---
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlFilter.setOpaque(false);
        JLabel lblLoc = new JLabel("Chọn tháng:");
        lblLoc.setForeground(Color.WHITE);
        cboThangNam = new JComboBox<>();
        loadComboboxData();
        cboThangNam.addActionListener(e -> loadData());

        pnlFilter.add(lblLoc);
        pnlFilter.add(cboThangNam);

        JButton btnAddMonth = new JButton("➕ Tạo tháng mới");
        btnAddMonth.setBackground(new Color(46, 204, 113));
        btnAddMonth.setForeground(Color.WHITE);
        btnAddMonth.setFocusPainted(false);
        btnAddMonth.addActionListener(e -> handleAddMonth());

        JButton btnSync = new JButton("🔄 Đồng bộ Hóa Đơn");
        btnSync.setBackground(new Color(52, 152, 219));
        btnSync.setForeground(Color.WHITE);
        btnSync.setFocusPainted(false);
        btnSync.addActionListener(e -> handleSync());

        JButton btnGia = new JButton("⚙️ Cài đặt giá điện/nước");
        btnGia.setBackground(new Color(155, 89, 182));
        btnGia.setForeground(Color.WHITE);
        btnGia.setOpaque(true);
        btnGia.setBorderPainted(false);
        btnGia.setFocusPainted(false);
        btnGia.addActionListener(e -> showGiaDialog());

        // Nhãn hiển thị giá đang áp dụng
        lblGiaHienTai = new JLabel();
        lblGiaHienTai.setForeground(new Color(255, 200, 100));
        lblGiaHienTai.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        updateGiaLabel();

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRight.setOpaque(false);
        pnlRight.add(pnlFilter);

        if (maPhongFilter != null) {
            btnAddMonth.setVisible(false);
            btnSync.setVisible(false);
            btnGia.setVisible(false);
        } else {
            pnlRight.add(lblGiaHienTai);
            pnlRight.add(btnGia);
            pnlRight.add(btnAddMonth);
            pnlRight.add(btnSync);
        }

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(pnlRight, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);


        // --- Danh sách cột ---
        String[] cols;
        if (maPhongFilter != null) {
            cols = new String[]{"ID", "PHÒNG", "THÁNG/NĂM", "SỐ ĐIỆN", "TIỀN ĐIỆN", "SỐ NƯỚC", "TIỀN NƯỚC", "TỔNG TIỀN"};
        } else {
            cols = new String[]{"ID", "PHÒNG", "THÁNG/NĂM", "SỐ ĐIỆN", "TIỀN ĐIỆN", "SỐ NƯỚC", "TIỀN NƯỚC", "TỔNG TIỀN", "HÀNH ĐỘNG"};
        }

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return maPhongFilter == null && c == 8; }
        };

        table = new JTable(model);
        setupTableUI();

        table.getColumnModel().getColumn(3).setCellRenderer(new CustomValueRenderer(new Color(255, 153, 51), "kWh"));
        table.getColumnModel().getColumn(5).setCellRenderer(new CustomValueRenderer(new Color(51, 153, 255), "m³"));
        table.getColumnModel().getColumn(7).setCellRenderer(new CustomValueRenderer(new Color(46, 204, 113), ""));

        if (maPhongFilter == null) {
            table.getColumnModel().getColumn(8).setCellRenderer(new ActionButtonRenderer());
            table.getColumnModel().getColumn(8).setCellEditor(new ActionButtonEditor(new JCheckBox()));
        }

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(new Color(18, 19, 24));
        add(sp, BorderLayout.CENTER);

        loadData();
    }

    private void setupTableUI() {
        table.setRowHeight(45);
        table.setBackground(new Color(30, 31, 38));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(50, 50, 50));
        table.getTableHeader().setBackground(new Color(40, 41, 48));
        table.getTableHeader().setForeground(Color.LIGHT_GRAY);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    private void loadComboboxData() {
        cboThangNam.removeAllItems();
        List<String> listThang = dnDao.getDanhSachThangNam();
        for (String t : listThang) {
            cboThangNam.addItem(t);
        }
    }

    public void loadData() {
        if (cboThangNam.getSelectedItem() == null) return;
        String thangDuocChon = cboThangNam.getSelectedItem().toString();

        model.setRowCount(0);
        try {
            List<DienNuoc> list = dnDao.getTheoThangNam(thangDuocChon);
            for (DienNuoc dn : list) {
                if (maPhongFilter != null && !dn.getTenPhong().equalsIgnoreCase(maPhongFilter)) {
                    continue;
                }
                int soDien = dn.getDienCuoi() - dn.getDienDau();
                int soNuoc = dn.getNuocCuoi() - dn.getNuocDau();
                if (maPhongFilter != null) {
                    model.addRow(new Object[]{
                        dn.getId(), dn.getTenPhong(), dn.getThangNam(),
                        soDien, String.format("%,.0f đ", dn.getTienDien()),
                        soNuoc, String.format("%,.0f đ", dn.getTienNuoc()),
                        String.format("%,.0f đ", dn.getTienDien() + dn.getTienNuoc())
                    });
                } else {
                    model.addRow(new Object[]{
                        dn.getId(), dn.getTenPhong(), dn.getThangNam(),
                        soDien, String.format("%,.0f đ", dn.getTienDien()),
                        soNuoc, String.format("%,.0f đ", dn.getTienNuoc()),
                        String.format("%,.0f đ", dn.getTienDien() + dn.getTienNuoc()),
                        "Sửa/Xóa"
                    });
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void handleAddMonth() {
        if (cboThangNam.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Chưa có dữ liệu tháng cũ làm gốc!");
            return;
        }

        String thangNamCu = cboThangNam.getSelectedItem().toString();
        String thangNamMoi = JOptionPane.showInputDialog(this,
            "Bạn đang chốt số liệu từ tháng [" + thangNamCu + "].\nNhập Tháng/Năm mới (VD: 06/2026):");

        if (thangNamMoi != null && !thangNamMoi.trim().isEmpty()) {
            // Chuẩn hóa format: "5/2026" → "05/2026"
            String normalized = normalizeThangNam(thangNamMoi.trim());
            if (normalized == null) {
                JOptionPane.showMessageDialog(this, "Định dạng sai! Vui lòng nhập theo dạng MM/YYYY (VD: 06/2026).");
                return;
            }
            if (dnDao.chuyenThangMoi(normalized, thangNamCu)) {
                JOptionPane.showMessageDialog(this, "Đã khởi tạo thành công tháng " + normalized);
                loadComboboxData();
                cboThangNam.setSelectedItem(normalized);
            } else {
                JOptionPane.showMessageDialog(this, "Có lỗi xảy ra (Tháng này có thể đã tồn tại)!");
            }
        }
    }

    /**
     * Chuẩn hóa "5/2026" → "05/2026" để tránh lỗi khớp ThangNam.
     * Trả về null nếu format không hợp lệ.
     */
    private String normalizeThangNam(String input) {
        try {
            String[] parts = input.split("/");
            if (parts.length != 2) return null;
            int thang = Integer.parseInt(parts[0].trim());
            int nam   = Integer.parseInt(parts[1].trim());
            if (thang < 1 || thang > 12 || nam < 2000) return null;
            return String.format("%02d/%d", thang, nam);
        } catch (Exception e) {
            return null;
        }
    }

    // ─── Cập nhật nhãn giá hiện tại ───────────────────────────────────────────
    private void updateGiaLabel() {
        if (lblGiaHienTai == null) return;
        double gDien = dnDao.getGiaDien();
        double gNuoc = dnDao.getGiaNuoc();
        lblGiaHienTai.setText(String.format(
            "⚡ %,.0f đ/kWh   💧 %,.0f đ/m³", gDien, gNuoc));
    }

    // ─── Dialog cài đặt giá điện/nước ────────────────────────────────────────
    private void showGiaDialog() {
        double curDien = dnDao.getGiaDien();
        double curNuoc = dnDao.getGiaNuoc();

        // --- Panel form ---
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(30, 31, 38));
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;

        Font fontLabel = new Font("Segoe UI", Font.PLAIN, 14);
        Font fontField = new Font("Segoe UI", Font.BOLD, 14);
        Color colorField = new Color(45, 46, 55);

        // Giá điện
        g.gridx = 0; g.gridy = 0; g.weightx = 0.4;
        JLabel lDien = new JLabel("⚡ Giá điện (VNĐ/kWh):");
        lDien.setForeground(new Color(255, 153, 51)); lDien.setFont(fontLabel);
        form.add(lDien, g);

        g.gridx = 1; g.weightx = 0.6;
        JTextField txtDien = new JTextField(String.valueOf((long) curDien));
        txtDien.setBackground(colorField); txtDien.setForeground(Color.WHITE);
        txtDien.setFont(fontField); txtDien.setCaretColor(Color.WHITE);
        txtDien.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 100)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        form.add(txtDien, g);

        // Giá nước
        g.gridx = 0; g.gridy = 1; g.weightx = 0.4;
        JLabel lNuoc = new JLabel("💧 Giá nước (VNĐ/m³):");
        lNuoc.setForeground(new Color(51, 153, 255)); lNuoc.setFont(fontLabel);
        form.add(lNuoc, g);

        g.gridx = 1; g.weightx = 0.6;
        JTextField txtNuoc = new JTextField(String.valueOf((long) curNuoc));
        txtNuoc.setBackground(colorField); txtNuoc.setForeground(Color.WHITE);
        txtNuoc.setFont(fontField); txtNuoc.setCaretColor(Color.WHITE);
        txtNuoc.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(80, 80, 100)),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        form.add(txtNuoc, g);

        // Ghi chú
        g.gridx = 0; g.gridy = 2; g.gridwidth = 2;
        JLabel lNote = new JLabel("<html><i>* Giá mới sẽ được áp dụng ngay khi cập nhật số liệu điện/nước.</i></html>");
        lNote.setForeground(new Color(150, 150, 160));
        lNote.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        form.add(lNote, g);

        // --- Tạo dialog ---
        JDialog dlg = new JDialog(
            SwingUtilities.getWindowAncestor(this),
            "⚙️ Cài đặt Giá Điện / Nước",
            java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(440, 260);
        dlg.setLocationRelativeTo(this);
        dlg.setResizable(false);

        // Title
        JLabel lblTitle = new JLabel("⚙️ CÀI ĐẶT GIÁ ĐIỆN / NƯỚC", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(new Color(155, 89, 182));
        lblTitle.setOpaque(true);
        lblTitle.setBackground(new Color(22, 23, 28));
        lblTitle.setBorder(BorderFactory.createEmptyBorder(14, 0, 14, 0));

        // Buttons
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        pnlBtn.setBackground(new Color(22, 23, 28));

        JButton btnCancel = new JButton("Hủy bỏ");
        btnCancel.setBackground(new Color(100, 100, 110));
        btnCancel.setForeground(Color.WHITE); btnCancel.setFocusPainted(false);
        btnCancel.setBorderPainted(false); btnCancel.setOpaque(true);
        btnCancel.addActionListener(e -> dlg.dispose());

        JButton btnSave = new JButton("💾 Lưu cấu hình");
        btnSave.setBackground(new Color(46, 204, 113));
        btnSave.setForeground(Color.WHITE); btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnSave.setFocusPainted(false); btnSave.setBorderPainted(false); btnSave.setOpaque(true);
        btnSave.addActionListener(e -> {
            try {
                double newDien = Double.parseDouble(txtDien.getText().trim().replace(",", ""));
                double newNuoc = Double.parseDouble(txtNuoc.getText().trim().replace(",", ""));
                if (newDien <= 0 || newNuoc <= 0) {
                    JOptionPane.showMessageDialog(dlg, "Giá phải lớn hơn 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (dnDao.saveGia(newDien, newNuoc)) {
                    JOptionPane.showMessageDialog(dlg,
                        String.format("✅ Đã lưu:\n" +
                            "⚡ Giá điện: %,.0f VNĐ/kWh\n" +
                            "💧 Giá nước: %,.0f VNĐ/m³", newDien, newNuoc),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    updateGiaLabel();
                    dlg.dispose();
                } else {
                    JOptionPane.showMessageDialog(dlg, "Lưu thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dlg, "Vui lòng nhập số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        pnlBtn.add(btnCancel);
        pnlBtn.add(btnSave);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(30, 31, 38));
        root.add(lblTitle, BorderLayout.NORTH);
        root.add(form, BorderLayout.CENTER);
        root.add(pnlBtn, BorderLayout.SOUTH);

        dlg.setContentPane(root);
        dlg.setVisible(true);
    }


    private void handleSync() {
        if (model.getRowCount() == 0 || cboThangNam.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Không có dữ liệu để đồng bộ!");
            return;
        }

        String thangNam = cboThangNam.getSelectedItem().toString();

        try {
            String[] parts = thangNam.split("/");
            int thang = Integer.parseInt(parts[0].trim());
            int nam   = Integer.parseInt(parts[1].trim());

            int confirm = JOptionPane.showConfirmDialog(this,
                "Chuyển tiền Điện/Nước tháng " + thangNam + " sang Hóa đơn chưa thu. Tiếp tục?",
                "Xác nhận Đồng bộ", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (dnDao.dongBoSangHoaDon(thang, nam)) {
                    JOptionPane.showMessageDialog(this, "✅ Đồng bộ thành công! Hóa Đơn đã được cập nhật.");
                    // Thông báo HoaDonPanel tự reload
                    DataRefreshEvent.get().notifyAll("DienNuocPanel.handleSync");
                } else {
                    JOptionPane.showMessageDialog(this,
                        "⚠️ Không có hóa đơn nào được cập nhật.\n"
                        + "Kiểm tra lại trạng thái 'Chưa thu' và tháng/năm tương ứng.");
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi đọc định dạng Tháng/Năm. Vui lòng kiểm tra lại (VD: 06/2026).");
        }
    }

    // ─── Inner Classes ─────────────────────────────────────────────────────────

    class ActionButtonRenderer extends JPanel implements TableCellRenderer {
        public ActionButtonRenderer() {
            setOpaque(false);
            setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
            JButton btnEdit = new JButton("📝");
            JButton btnDel  = new JButton("🗑️");
            add(btnEdit); add(btnDel);
        }
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            return this;
        }
    }

    class ActionButtonEditor extends DefaultCellEditor {
        private JPanel panel;
        private int currentRow;

        public ActionButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
            JButton btnEdit = new JButton("📝");
            JButton btnDel  = new JButton("🗑️");

            btnEdit.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) table.getValueAt(currentRow, 0);

                String moiDienStr = JOptionPane.showInputDialog(null,
                    "Nhập SỐ ĐIỆN ĐÃ DÙNG trong tháng này (kWh):");
                if (moiDienStr == null) return;

                String moiNuocStr = JOptionPane.showInputDialog(null,
                    "Nhập SỐ NƯỚC ĐÃ DÙNG trong tháng này (m³):");
                if (moiNuocStr == null) return;

                try {
                    int moiDien = Integer.parseInt(moiDienStr.trim());
                    int moiNuoc = Integer.parseInt(moiNuocStr.trim());

                    if (dnDao.update(id, moiDien, moiNuoc)) {
                        if (dnDao.getLastSyncRows() > 0) {
                            JOptionPane.showMessageDialog(null,
                                "✅ Thành công! Đã lưu số liệu và cập nhật vào Hóa Đơn.");
                        } else {
                            JOptionPane.showMessageDialog(null,
                                "⚠️ Đã lưu số liệu Điện/Nước, nhưng chưa cập nhật Hóa Đơn nào.\n"
                                + "Kiểm tra hóa đơn tháng này đã tồn tại và còn trạng thái 'Chưa thu'.");
                        }
                        SwingUtilities.invokeLater(() -> loadData());
                        // Thông báo toàn bộ panel khác (đặc biệt HoaDonPanel) tự reload
                        DataRefreshEvent.get().notifyAll("DienNuocPanel.update");
                    } else {
                        JOptionPane.showMessageDialog(null,
                            "❌ Lưu thất bại! Kiểm tra lại kết nối CSDL hoặc ID bản ghi.");
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(null, "Lỗi: Vui lòng nhập số nguyên hợp lệ!");
                }
            });

            btnDel.addActionListener(e -> {
                fireEditingStopped();
                int id = (int) table.getValueAt(currentRow, 0);
                int confirm = JOptionPane.showConfirmDialog(null,
                    "Xóa bản ghi này?", "Xác nhận xóa", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    if (dnDao.delete(id)) {
                        JOptionPane.showMessageDialog(null, "Đã xóa thành công!");
                    } else {
                        JOptionPane.showMessageDialog(null, "Xóa thất bại!");
                    }
                    loadData();
                }
            });

            panel.add(btnEdit); panel.add(btnDel);
        }

        public Component getTableCellEditorComponent(JTable t, Object v, boolean s, int r, int c) {
            this.currentRow = r;
            return panel;
        }
    }

    class CustomValueRenderer extends DefaultTableCellRenderer {
        private Color color;
        private String unit;

        public CustomValueRenderer(Color c, String u) { color = c; unit = u; }

        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
            JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
            lbl.setForeground(color);
            lbl.setText(v != null ? v.toString() + (unit.isEmpty() ? "" : " " + unit) : "");
            lbl.setHorizontalAlignment(SwingConstants.CENTER);
            return lbl;
        }
    }
}
