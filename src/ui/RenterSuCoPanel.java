package ui;

import dao.SuCoDAO;
import model.SuCo;
import util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Panel báo cáo sự cố & theo dõi tiến độ dành cho Khách thuê (Tenant Portal).
 * Thiết kế giao diện hiện đại đồng bộ màu sắc tối của Cloudzone.
 */
public class RenterSuCoPanel extends JPanel {

    private final String maPhong;
    private final SuCoDAO dao = new SuCoDAO();
    
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextArea txtMoTa;
    private JButton btnSubmit;

    public RenterSuCoPanel(String maPhong) {
        this.maPhong = maPhong;
        setBackground(new Color(18, 19, 24));
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 1. Tiêu đề
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("🛠️ BÁO CÁO & THEO DÕI SỰ CỐ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        titlePanel.add(lblTitle, BorderLayout.WEST);
        add(titlePanel, BorderLayout.NORTH);

        // 2. Bảng lịch sử báo sự cố (bên trái)
        String[] cols = {"ID", "Mô tả sự cố", "Ngày gửi", "Trạng thái"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(32);
        table.setBackground(new Color(30, 31, 38));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(50, 50, 60));
        table.getTableHeader().setBackground(new Color(40, 42, 50));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        // Căn lề và tô màu cho trạng thái
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(SwingConstants.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
                if (v != null) {
                    String status = v.toString();
                    if ("Chờ xử lý".equalsIgnoreCase(status)) {
                        lbl.setForeground(new Color(230, 126, 34)); // Cam
                    } else if ("Đang sửa".equalsIgnoreCase(status)) {
                        lbl.setForeground(new Color(52, 152, 219)); // Xanh dương
                    } else if ("Đã xong".equalsIgnoreCase(status)) {
                        lbl.setForeground(new Color(46, 204, 113)); // Xanh lá
                    }
                }
                return lbl;
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 60)));
        scroll.getViewport().setBackground(new Color(30, 31, 38));
        add(scroll, BorderLayout.CENTER);

        // 3. Form gửi báo cáo mới (bên phải)
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(new Color(30, 31, 38));
        formPanel.setPreferredSize(new Dimension(320, 500));
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel lblHeading = new JLabel("BÁO SỰ CỐ MỚI");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeading.setForeground(Color.WHITE);
        lblHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblHeading);
        formPanel.add(Box.createVerticalStrut(20));

        JLabel lblDesc = new JLabel("Mô tả chi tiết sự cố:");
        lblDesc.setForeground(new Color(180, 180, 180));
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        formPanel.add(lblDesc);
        formPanel.add(Box.createVerticalStrut(6));

        txtMoTa = new JTextArea();
        txtMoTa.setBackground(new Color(22, 23, 28));
        txtMoTa.setForeground(Color.WHITE);
        txtMoTa.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtMoTa.setCaretColor(Color.WHITE);
        txtMoTa.setLineWrap(true);
        txtMoTa.setWrapStyleWord(true);
        
        JScrollPane scrMoTa = new JScrollPane(txtMoTa);
        scrMoTa.setMaximumSize(new Dimension(Integer.MAX_VALUE, 160));
        scrMoTa.setPreferredSize(new Dimension(Integer.MAX_VALUE, 160));
        scrMoTa.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrMoTa.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 100)));
        formPanel.add(scrMoTa);
        formPanel.add(Box.createVerticalStrut(20));

        btnSubmit = new JButton("Gửi yêu cầu sửa chữa");
        btnSubmit.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSubmit.setBackground(new Color(46, 204, 113));
        btnSubmit.setForeground(Color.WHITE);
        btnSubmit.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnSubmit.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnSubmit.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnSubmit.addActionListener(e -> submitSuCo());
        formPanel.add(btnSubmit);

        add(formPanel, BorderLayout.EAST);

        loadData();
    }

    public void loadData() {
        tableModel.setRowCount(0);
        if (maPhong == null) return;
        
        List<SuCo> list = dao.getByMaPhong(maPhong);
        for (SuCo sc : list) {
            tableModel.addRow(new Object[]{
                    sc.getId(),
                    sc.getMoTa(),
                    sc.getNgayGui().toString(),
                    sc.getTrangThai()
            });
        }
    }

    private void submitSuCo() {
        String moTa = txtMoTa.getText().trim();
        if (moTa.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập mô tả sự cố!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String landlordUser = Session.getTenDN(); // Lấy tên landlord để quản lý
        if (landlordUser == null || landlordUser.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy tài khoản quản lý phòng này!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        SuCo sc = new SuCo();
        sc.setMaPhong(maPhong);
        sc.setMoTa(moTa);
        sc.setNgayGui(LocalDate.now());
        sc.setTrangThai("Chờ xử lý");
        sc.setTenDN(landlordUser);

        if (dao.insert(sc)) {
            JOptionPane.showMessageDialog(this, "Gửi báo cáo sự cố thành công! Ban quản lý sẽ sớm tiếp nhận và xử lý.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            txtMoTa.setText("");
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi gửi báo cáo sự cố!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
