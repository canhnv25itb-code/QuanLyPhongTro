package ui;

import dao.SuCoDAO;
import model.SuCo;
import util.Session;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class SuCoPanel extends JPanel {
    private final SuCoDAO dao = new SuCoDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    private JComboBox<String> cbStatus;
    private JButton btnUpdate;
    private SuCo selectedSuCo;

    public SuCoPanel() {
        setBackground(new Color(18, 19, 24));
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Tiêu đề Panel
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setOpaque(false);
        JLabel lblTitle = new JLabel("🛠️ QUẢN LÝ SỰ CỐ & BẢO TRÌ");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(Color.WHITE);
        titlePanel.add(lblTitle, BorderLayout.WEST);
        add(titlePanel, BorderLayout.NORTH);

        // Bảng danh sách sự cố
        String[] cols = {"ID", "Phòng", "Mô tả sự cố", "Ngày báo", "Trạng thái"};
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

        // Panel điều khiển (East)
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(new Color(30, 31, 38));
        controlPanel.setPreferredSize(new Dimension(280, 500));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel lblHeading = new JLabel("CẬP NHẬT TIẾN ĐỘ");
        lblHeading.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblHeading.setForeground(Color.WHITE);
        lblHeading.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(lblHeading);
        controlPanel.add(Box.createVerticalStrut(20));

        JLabel lblStatus = new JLabel("Trạng thái xử lý:");
        lblStatus.setForeground(new Color(180, 180, 180));
        lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(lblStatus);
        controlPanel.add(Box.createVerticalStrut(6));

        cbStatus = new JComboBox<>(new String[]{"Chờ xử lý", "Đang sửa", "Đã xong"});
        cbStatus.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        cbStatus.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(cbStatus);
        controlPanel.add(Box.createVerticalStrut(20));

        btnUpdate = new JButton("Cập nhật trạng thái");
        btnUpdate.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnUpdate.setBackground(new Color(52, 152, 219));
        btnUpdate.setForeground(Color.WHITE);
        btnUpdate.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnUpdate.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnUpdate.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnUpdate.addActionListener(e -> updateStatus());
        controlPanel.add(btnUpdate);

        add(controlPanel, BorderLayout.EAST);

        // Sự kiện click chọn dòng trên bảng
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                selectedSuCo = findById(id);
                if (selectedSuCo != null) {
                    cbStatus.setSelectedItem(selectedSuCo.getTrangThai());
                }
            }
        });

        loadData();
    }

    public void loadData() {
        tableModel.setRowCount(0);
        String username = Session.getTenDN();
        if (username == null) username = "admin";
        List<SuCo> list = dao.getByTenDN(username);
        for (SuCo sc : list) {
            tableModel.addRow(new Object[]{
                sc.getId(),
                sc.getMaPhong(),
                sc.getMoTa(),
                sc.getNgayGui().toString(),
                sc.getTrangThai()
            });
        }
    }

    private SuCo findById(int id) {
        String username = Session.getTenDN();
        if (username == null) username = "admin";
        List<SuCo> list = dao.getByTenDN(username);
        for (SuCo sc : list) {
            if (sc.getId() == id) return sc;
        }
        return null;
    }

    private void updateStatus() {
        if (selectedSuCo == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn sự cố để cập nhật!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String status = (String) cbStatus.getSelectedItem();
        if (dao.updateTrangThai(selectedSuCo.getId(), status)) {
            JOptionPane.showMessageDialog(this, "Cập nhật trạng thái sự cố thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadData();
            selectedSuCo = null;
            table.clearSelection();
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi cập nhật!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}
