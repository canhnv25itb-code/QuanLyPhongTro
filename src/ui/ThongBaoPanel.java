package ui;

import dao.ThongBaoDAO;
import model.ThongBao;
import util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ThongBaoPanel extends JPanel {
    private final ThongBaoDAO dao = new ThongBaoDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    
    private JTextField txtTitle;
    private JTextArea txtContent;
    private JButton btnAdd, btnDelete;
    private ThongBao selectedTb;

    public ThongBaoPanel() {
        setBackground(new Color(18, 19, 24));
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Tiêu đề Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("📢 HỆ THỐNG THÔNG BÁO KHU TRỌ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        titlePanel.add(lblTitle, BorderLayout.WEST);
        add(titlePanel, BorderLayout.NORTH);

        // Bảng các thông báo đã đăng
        String[] cols = {"ID", "Tiêu đề thông báo", "Ngày đăng"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setRowHeight(30);
        table.setBackground(new Color(30, 31, 38));
        table.setForeground(Color.WHITE);
        table.setGridColor(new Color(50, 50, 60));
        table.getTableHeader().setBackground(new Color(40, 42, 50));
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(null);
        add(scroll, BorderLayout.CENTER);

        // Panel soạn thảo (East)
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(new Color(30, 31, 38));
        controlPanel.setPreferredSize(new Dimension(320, 500));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel lblFormTitle = new JLabel("SOẠN THẢO THÔNG BÁO");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFormTitle.setForeground(Color.WHITE);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(lblFormTitle);
        controlPanel.add(Box.createVerticalStrut(20));

        // Tiêu đề thông báo
        JLabel lblT = new JLabel("Tiêu đề:");
        lblT.setForeground(new Color(180, 180, 180));
        lblT.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(lblT);
        controlPanel.add(Box.createVerticalStrut(4));
        txtTitle = new JTextField();
        txtTitle.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(txtTitle);
        controlPanel.add(Box.createVerticalStrut(15));

        // Nội dung thông báo
        JLabel lblC = new JLabel("Nội dung:");
        lblC.setForeground(new Color(180, 180, 180));
        lblC.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(lblC);
        controlPanel.add(Box.createVerticalStrut(4));
        
        txtContent = new JTextArea();
        txtContent.setLineWrap(true);
        txtContent.setWrapStyleWord(true);
        JScrollPane scrContent = new JScrollPane(txtContent);
        scrContent.setPreferredSize(new Dimension(280, 150));
        scrContent.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        scrContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(scrContent);
        controlPanel.add(Box.createVerticalStrut(20));

        // Nút bấm đăng/xóa
        btnAdd = new JButton("📤 Đăng thông báo");
        btnDelete = new JButton("❌ Xóa thông báo");

        for (JButton btn : new JButton[]{btnAdd, btnDelete}) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(Color.WHITE);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        btnAdd.setBackground(new Color(46, 204, 113));
        btnDelete.setBackground(new Color(231, 76, 60));

        btnAdd.addActionListener(e -> postAnnouncement());
        btnDelete.addActionListener(e -> deleteAnnouncement());

        controlPanel.add(btnAdd);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(btnDelete);

        add(controlPanel, BorderLayout.EAST);

        // Lắng nghe sự kiện click dòng chọn trên bảng
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                selectedTb = findTbById(id);
                if (selectedTb != null) {
                    txtTitle.setText(selectedTb.getTieuDe());
                    txtContent.setText(selectedTb.getNoiDung());
                }
            }
        });

        loadData();
    }

    public void loadData() {
        tableModel.setRowCount(0);
        String username = Session.getTenDN();
        if (username == null) username = "admin";
        List<ThongBao> list = dao.getByTenDN(username);
        for (ThongBao tb : list) {
            tableModel.addRow(new Object[]{
                tb.getId(),
                tb.getTieuDe(),
                tb.getNgayDang().toString()
            });
        }
        clearForm();
    }

    private ThongBao findTbById(int id) {
        String username = Session.getTenDN();
        if (username == null) username = "admin";
        List<ThongBao> list = dao.getByTenDN(username);
        for (ThongBao tb : list) {
            if (tb.getId() == id) return tb;
        }
        return null;
    }

    private void clearForm() {
        txtTitle.setText("");
        txtContent.setText("");
        selectedTb = null;
        table.clearSelection();
    }

    private void postAnnouncement() {
        String username = Session.getTenDN();
        if (username == null) username = "admin";

        String title = txtTitle.getText().trim();
        String content = txtContent.getText().trim();

        if (title.isEmpty() || content.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tiêu đề và nội dung thông báo!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ThongBao tb = new ThongBao();
        tb.setTieuDe(title);
        tb.setNoiDung(content);
        tb.setNgayDang(LocalDate.now());
        tb.setTenDN(username);

        if (dao.insert(tb)) {
            JOptionPane.showMessageDialog(this, "Đăng thông báo khu trọ thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadData();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi xảy ra khi đăng thông báo!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAnnouncement() {
        if (selectedTb == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn thông báo cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa thông báo '" + selectedTb.getTieuDe() + "'?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.delete(selectedTb.getId())) {
                JOptionPane.showMessageDialog(this, "Xóa thông báo thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi xảy ra khi xóa thông báo!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
