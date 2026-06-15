package ui;

import dao.KhachThueDAO;
import model.KhachThue;
import util.DataRefreshEvent;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Panel quản lý Khách thuê – dành cho Chủ trọ.
 * Cột "Tài khoản liên kết" đọc trực tiếp từ KhachThue.TenDN (không cần join thêm).
 */
public class KhachThuePanel extends JPanel {

    private JTable            table;
    private DefaultTableModel model;
    private JTextField        txtSearch;
    private final KhachThueDAO dao = new KhachThueDAO();

    public KhachThuePanel() {
        setBackground(new Color(24, 25, 28));
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        initComponents();
        loadData();
    }

    private void initComponents() {
        // ── Header Panel ────────────────────────────────────────────────────────
        JPanel pnlHeader = new JPanel(new BorderLayout(15, 10));
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Tiêu đề bên trái
        JLabel lblTitle = new JLabel("👥  Quản lý Khách thuê");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        // Thanh điều khiển & Tìm kiếm bên phải
        JPanel pnlControls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlControls.setOpaque(false);

        JLabel lblSearch = new JLabel("🔍");
        lblSearch.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblSearch.setForeground(Color.LIGHT_GRAY);

        txtSearch = new JTextField(15);
        txtSearch.setBackground(new Color(45, 46, 55));
        txtSearch.setForeground(Color.WHITE);
        txtSearch.setCaretColor(Color.WHITE);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 80)),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        txtSearch.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override public void keyReleased(java.awt.event.KeyEvent e) { handleSearch(); }
        });

        JButton btnAdd    = styledBtn("＋ Thêm khách",   new Color(46, 204, 113));  // Green accent
        JButton btnEdit   = styledBtn("✏️ Sửa",    new Color(52, 152, 219));   // Blue accent
        JButton btnDelete = styledBtn("🗑️ Xóa",   new Color(231, 76, 60));    // Red accent
        JButton btnReload = styledBtn("🔄 Tải lại", new Color(110, 112, 125));

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnReload.addActionListener(e -> loadData());

        pnlControls.add(lblSearch);
        pnlControls.add(txtSearch);
        pnlControls.add(btnAdd);
        pnlControls.add(btnEdit);
        pnlControls.add(btnDelete);
        pnlControls.add(btnReload);

        pnlHeader.add(pnlControls, BorderLayout.EAST);

        // ── Bảng dữ liệu ──────────────────────────────────────────────────────
        String[] cols = {"Mã", "Họ tên", "CCCD", "SĐT", "Quê quán",
                          "Phòng", "Trạng thái", "Tài khoản liên kết"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        setupTableUI();

        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEdit();
                }
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(new Color(30, 31, 36));
        sp.setBorder(BorderFactory.createLineBorder(new Color(45, 48, 56)));

        add(pnlHeader, BorderLayout.NORTH);
        add(sp,        BorderLayout.CENTER);
    }

    private void setupTableUI() {
        table.setRowHeight(42);
        table.setBackground(new Color(30, 31, 36));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(45, 48, 56));
        table.setShowVerticalLines(false);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        table.getTableHeader().setBackground(new Color(45, 48, 56));
        table.getTableHeader().setForeground(Color.LIGHT_GRAY);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.getTableHeader().setPreferredSize(new Dimension(0, 40));

        // Căn giữa cột 0 (Mã) và 5 (Phòng)
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(0).setCellRenderer(center);
        table.getColumnModel().getColumn(5).setCellRenderer(center);

        // Badge màu cho CCCD, SĐT, Tài khoản
        table.getColumnModel().getColumn(2).setCellRenderer(
                new BadgeRenderer(new Color(40, 50, 70),  new Color(130, 180, 255)));
        table.getColumnModel().getColumn(3).setCellRenderer(
                new BadgeRenderer(new Color(50, 40, 70),  new Color(190, 150, 255)));
        table.getColumnModel().getColumn(7).setCellRenderer(
                new BadgeRenderer(new Color(60, 50, 40),  new Color(241, 196, 15)));

        // Trạng thái – xanh/đỏ
        table.getColumnModel().getColumn(6).setCellRenderer(new BadgeRenderer(null, null) {
            @Override
            public Component getTableCellRendererComponent(
                    JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                if ("Đang ở".equals(v))
                    setColors(new Color(30, 60, 40), new Color(46, 204, 113));
                else
                    setColors(new Color(60, 30, 30), new Color(231, 76, 60));
                return super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            }
        });

        // Độ rộng cột hợp lý
        int[] widths = {40, 160, 130, 110, 120, 70, 90, 140};
        for (int i = 0; i < widths.length; i++)
            table.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    private void onAdd() {
        Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
        new KhachThueDialog(owner, true, null).setVisible(true);
        loadData();
        // Thông báo cho toàn bộ panel khác cập nhật
        DataRefreshEvent.get().notifyAll("KhachThuePanel.onAdd");
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn dòng cần sửa!"); return; }
        int id = (int) model.getValueAt(row, 0);
        KhachThue k = dao.findById(id);
        if (k != null) {
            Frame owner = (Frame) SwingUtilities.getWindowAncestor(this);
            new KhachThueDialog(owner, true, k).setVisible(true);
            loadData();
            // Thông báo cho toàn bộ panel khác cập nhật
            DataRefreshEvent.get().notifyAll("KhachThuePanel.onEdit");
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Chọn dòng cần xóa!"); return; }
        int id = (int) model.getValueAt(row, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Xóa khách thuê #" + id + "?\n(Tài khoản liên kết không bị xóa.)",
                "Xác nhận", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.delete(id)) {
                JOptionPane.showMessageDialog(this, "✅ Đã xóa! Trạng thái phòng đã được cập nhật.");
                loadData();
                // Thông báo cho toàn bộ panel khác cập nhật
                DataRefreshEvent.get().notifyAll("KhachThuePanel.onDelete");
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!");
            }
        }
    }

    // ── Load / Search ─────────────────────────────────────────────────────────
    public void loadData() {
        model.setRowCount(0);
        for (KhachThue k : dao.getAll()) {
            model.addRow(new Object[]{
                k.getMaKhach(),
                k.getHoTen(),
                k.getCccd(),
                k.getSdt(),
                k.getQueQuan() != null ? k.getQueQuan() : "",
                k.getMaPhong(),
                k.getTrangThai(),
                k.getTenDN() != null && !k.getTenDN().isEmpty() ? k.getTenDN() : "Chưa liên kết"
            });
        }
    }

    private void handleSearch() {
        String kw = txtSearch.getText().trim().toLowerCase();
        if (kw.isEmpty()) { loadData(); return; }
        model.setRowCount(0);
        for (KhachThue k : dao.getAll()) {
            String maKhach = String.valueOf(k.getMaKhach());
            if (k.getHoTen().toLowerCase().contains(kw)
                    || maKhach.contains(kw)
                    || k.getMaPhong().toLowerCase().contains(kw)
                    || k.getCccd().contains(kw)
                    || k.getSdt().contains(kw)) {
                model.addRow(new Object[]{
                    k.getMaKhach(),
                    k.getHoTen(),
                    k.getCccd(),
                    k.getSdt(),
                    k.getQueQuan() != null ? k.getQueQuan() : "",
                    k.getMaPhong(),
                    k.getTrangThai(),
                    k.getTenDN() != null && !k.getTenDN().isEmpty() ? k.getTenDN() : "Chưa liên kết"
                });
            }
        }
    }

    // ── Helper button ─────────────────────────────────────────────────────────
    private JButton styledBtn(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg); b.setForeground(Color.WHITE);
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setFocusPainted(false); b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(115, 35));
        b.putClientProperty("JButton.buttonType", "roundRect"); // FlatLaf round border
        return b;
    }

    // ── Badge Renderer ────────────────────────────────────────────────────────
    static class BadgeRenderer extends DefaultTableCellRenderer {
        private Color bgColor, fgColor;
        BadgeRenderer(Color bg, Color fg) { 
            this.bgColor = bg; 
            this.fgColor = fg; 
            setHorizontalAlignment(CENTER); 
            setOpaque(false); // Enable manual round drawing
        }
        void setColors(Color bg, Color fg) { this.bgColor = bg; this.fgColor = fg; }

        @Override
        public Component getTableCellRendererComponent(
                JTable t, Object v, boolean sel, boolean foc, int r, int c) {
            super.getTableCellRendererComponent(t, v, sel, foc, r, c);
            if (sel) {
                setForeground(Color.WHITE);
            } else {
                if (fgColor != null) setForeground(fgColor);
            }
            setFont(new Font("Segoe UI", Font.BOLD, 12));
            return this;
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (bgColor != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(bgColor);
                g2.fillRoundRect(6, 6, getWidth() - 12, getHeight() - 12, 8, 8);
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}