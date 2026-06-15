package ui;

import dao.PhongTroDAO;
import model.PhongTro;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.text.DecimalFormat;

public class PhongTroPanel extends JPanel {
    private final PhongTroDAO dao = new PhongTroDAO();
    private JPanel gridPanel;
    private final DecimalFormat formatter = new DecimalFormat("###,###,###");

    private String maPhongFilter = null;

    public PhongTroPanel() {
        this(null);
    }

    public PhongTroPanel(String maPhongFilter) {
        this.maPhongFilter = maPhongFilter;
        setBackground(new Color(24, 25, 28)); 
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        // 1. Tiêu đề
        JLabel lblTitle = new JLabel(maPhongFilter == null ? "SƠ ĐỒ PHÒNG TRỌ" : "THÔNG TIN PHÒNG THUÊ CỦA BẠN", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(lblTitle, BorderLayout.NORTH);

        // 2. Lưới phòng
        gridPanel = new JPanel(new GridLayout(0, 3, 20, 20));
        gridPanel.setBackground(new Color(24, 25, 28));
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 30, 30, 30));

        loadData();

        JScrollPane sp = new JScrollPane(gridPanel);
        sp.setBorder(null);
        sp.getViewport().setBackground(new Color(24, 25, 28));
        add(sp, BorderLayout.CENTER);

        // 3. Thanh công cụ bên dưới
        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setBackground(new Color(30, 31, 36));
        pnlBottom.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel pnlNote = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 0));
        pnlNote.setOpaque(false);
        pnlNote.add(createNoteLabel("Trống", new Color(30, 70, 45), new Color(46, 204, 113)));
        pnlNote.add(createNoteLabel("Đã thuê", new Color(80, 30, 30), new Color(231, 76, 60)));

        JButton btnAdd = new JButton("+ Thêm Phòng");
        btnAdd.setPreferredSize(new Dimension(150, 40));
        btnAdd.setBackground(new Color(41, 128, 185));
        btnAdd.setForeground(Color.WHITE);
        btnAdd.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnAdd.addActionListener(e -> onAdd());

        pnlBottom.add(pnlNote, BorderLayout.CENTER);
        
        if (maPhongFilter != null) {
            btnAdd.setVisible(false);
        } else {
            pnlBottom.add(btnAdd, BorderLayout.EAST);
        }
        add(pnlBottom, BorderLayout.SOUTH);
    }

    public void loadData() {
        gridPanel.removeAll();
        List<PhongTro> ds = dao.getAll();
        if (ds != null) {
            for (PhongTro p : ds) {
                if (maPhongFilter != null && !p.getMaPhong().equalsIgnoreCase(maPhongFilter)) {
                    continue;
                }
                gridPanel.add(createRoomButton(p));
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private JButton createRoomButton(PhongTro p) {
        // Khớp với chữ "Đang thuê" trong database của bạn
        boolean daThue = p.getTrangThai().equalsIgnoreCase("Đang thuê") || p.getTrangThai().equalsIgnoreCase("Đã thuê");
        String colorHex = daThue ? "#ff6b6b" : "#2ecc71";
        
        String giaPhong = formatter.format(p.getGiaPhong()) + "đ";
        String dienTich = p.getDienTich() + " m²";
        
        String htmlContent = "<html><center>"
                + "<font color='" + colorHex + "' size='5'><b>" + p.getMaPhong() + "</b></font><br>"
                + "<font color='#bdc3c7' size='3'>" + p.getTrangThai() + "</font><br>"
                + "<hr color='#454838'>" 
                + "<font color='#f1c40f'><b>" + giaPhong + "</b></font><br>"
                + "<font color='#ecf0f1'>" + dienTich + "</font>"
                + "</center></html>";

        JButton btn = new JButton(htmlContent);
        btn.setPreferredSize(new Dimension(150, 140)); 
        btn.setFocusPainted(false);
        btn.setBackground(daThue ? new Color(60, 30, 30) : new Color(30, 55, 40));
        btn.setBorder(BorderFactory.createLineBorder(daThue ? new Color(231, 76, 60) : new Color(46, 204, 113), 2));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (maPhongFilter == null) {
            btn.addActionListener(e -> {
                String[] options = {"Sửa thông tin", "Xóa phòng", "Hủy"};
                int choice = JOptionPane.showOptionDialog(this, "Thao tác với phòng " + p.getMaPhong() + "?", 
                        "Quản lý", JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
                if (choice == 0) onEdit(p);
                else if (choice == 1) onDelete(p);
            });
        }
        return btn;
    }

    // --- PHẦN XỬ LÝ LOGIC ---

    private double parsePrice(String str) throws NumberFormatException {
        if (str == null) throw new NumberFormatException();
        // Loại bỏ mọi ký tự không phải chữ số (dấu chấm, phẩy, đ, VND...)
        String clean = str.trim().replaceAll("[^0-9]", "");
        if (clean.isEmpty()) throw new NumberFormatException();
        return Double.parseDouble(clean);
    }

    private double parseArea(String str) throws NumberFormatException {
        if (str == null) throw new NumberFormatException();
        // Loại bỏ ký tự đơn vị như m2, m², khoảng trắng... chỉ giữ chữ số, dấu phẩy và dấu chấm
        String clean = str.trim().replaceAll("[^0-9,.]", "");
        // Nếu có cả dấu chấm và phẩy (kiểu 1.234,5) -> bỏ dấu chấm phân cách hàng nghìn, chuyển phẩy thập phân thành chấm
        if (clean.contains(",") && clean.contains(".")) {
            clean = clean.replace(".", "").replace(',', '.');
        } else {
            clean = clean.replace(',', '.');
        }
        if (clean.isEmpty()) throw new NumberFormatException();
        return Double.parseDouble(clean);
    }

    private void onAdd() {
        JTextField txtMa = new JTextField();
        JTextField txtGia = new JTextField();
        JTextField txtDT = new JTextField();
        Object[] message = { "Mã phòng (VD: P101):", txtMa, "Giá thuê (VND):", txtGia, "Diện tích (m²):", txtDT };

        int option = JOptionPane.showConfirmDialog(this, message, "Thêm phòng mới", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String ma = txtMa.getText().trim();
            if (ma.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Mã phòng không được để trống!");
                return;
            }
            try {
                double gia = parsePrice(txtGia.getText());
                double dt = parseArea(txtDT.getText());
                PhongTro p = new PhongTro(ma, 1, "Trống", gia, dt);
                if (dao.insert(p)) {
                    JOptionPane.showMessageDialog(this, "✅ Thêm phòng thành công!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Thêm phòng thất bại!");
                }
            } catch (java.sql.SQLException ex) {
                // Hiển thị lý do lỗi thực tế từ SQL Server
                String msg = ex.getMessage();
                if (msg != null && msg.contains("PRIMARY KEY") || msg != null && msg.contains("Violation")) {
                    JOptionPane.showMessageDialog(this,
                        "❌ Mã phòng '" + ma + "' đã tồn tại trong cơ sở dữ liệu!\n"
                        + "Hãy đổi sang mã phòng khác.");
                } else {
                    JOptionPane.showMessageDialog(this, "❌ Lỗi CSDL: " + msg);
                }
                System.err.println("[Insert LỖI] " + msg);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi định dạng số! Vui lòng kiểm tra lại giá thuê và diện tích.");
            }
        }
    }

    private void onEdit(PhongTro p) {
        String[] statuses = {"Trống", "Đang thuê", "Bảo trì"};
        JComboBox<String> cbStatus = new JComboBox<>(statuses);
        cbStatus.setSelectedItem(p.getTrangThai());
        
        JTextField txtGia = new JTextField(formatter.format(p.getGiaPhong()));
        JTextField txtDT = new JTextField(String.valueOf(p.getDienTich()));

        Object[] message = {
            "Trạng thái:", cbStatus,
            "Giá thuê (VND):", txtGia,
            "Diện tích (m²):", txtDT
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Sửa phòng " + p.getMaPhong(), JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                double gia = parsePrice(txtGia.getText());
                double dt = parseArea(txtDT.getText());
                p.setTrangThai(cbStatus.getSelectedItem().toString());
                p.setGiaPhong(gia);
                p.setDienTich(dt);
                
                if (dao.update(p)) {
                    JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Lỗi định dạng số! Vui lòng kiểm tra lại giá thuê và diện tích.");
            }
        }
    }

    private void onDelete(PhongTro p) {
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa phòng " + p.getMaPhong() + "?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.delete(p.getMaPhong())) {
                JOptionPane.showMessageDialog(this, "Đã xóa phòng thành công!");
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa phòng thất bại! Lưu ý: Không thể xóa phòng đang có Hợp đồng hoặc Hóa đơn.");
            }
        }
    }

    private JLabel createNoteLabel(String text, Color bgColor, Color fgColor) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setOpaque(true);
        lbl.setBackground(bgColor);
        lbl.setForeground(fgColor);
        lbl.setPreferredSize(new Dimension(90, 30));
        lbl.setBorder(BorderFactory.createLineBorder(fgColor));
        return lbl;
    }
}