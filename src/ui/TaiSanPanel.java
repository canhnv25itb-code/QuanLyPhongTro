package ui;

import dao.TaiSanDAO;
import dao.PhongTroDAO;
import model.TaiSan;
import model.PhongTro;
import util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class TaiSanPanel extends JPanel {
    private final TaiSanDAO dao = new TaiSanDAO();
    private final PhongTroDAO phongDao = new PhongTroDAO();

    private JComboBox<String> cbRoom;
    private JTable table;
    private DefaultTableModel tableModel;

    private JTextField txtName;
    private JSpinner spinQuantity;
    private JTextField txtCondition;
    private JButton btnAdd, btnUpdate, btnDelete;
    private TaiSan selectedAsset;

    public TaiSanPanel() {
        setBackground(new Color(18, 19, 24));
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Chọn phòng ở góc trên
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.setOpaque(false);
        JLabel lblRoom = new JLabel("Chọn phòng trọ: ");
        lblRoom.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRoom.setForeground(Color.WHITE);
        topPanel.add(lblRoom);

        cbRoom = new JComboBox<>();
        cbRoom.setPreferredSize(new Dimension(150, 30));
        cbRoom.addActionListener(e -> loadAssets());
        topPanel.add(cbRoom);
        add(topPanel, BorderLayout.NORTH);

        // Bảng danh mục tài sản
        String[] cols = {"ID", "Tên tài sản", "Số lượng", "Tình trạng bàn giao"};
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

        // Panel thông tin tài sản chi tiết (East)
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        controlPanel.setBackground(new Color(30, 31, 38));
        controlPanel.setPreferredSize(new Dimension(280, 500));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel lblFormTitle = new JLabel("CHI TIẾT TÀI SẢN");
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblFormTitle.setForeground(Color.WHITE);
        lblFormTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(lblFormTitle);
        controlPanel.add(Box.createVerticalStrut(20));

        // Tên tài sản
        JLabel lblName = new JLabel("Tên tài sản:");
        lblName.setForeground(new Color(180, 180, 180));
        lblName.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(lblName);
        controlPanel.add(Box.createVerticalStrut(4));
        txtName = new JTextField();
        txtName.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtName.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(txtName);
        controlPanel.add(Box.createVerticalStrut(15));

        // Số lượng
        JLabel lblQty = new JLabel("Số lượng:");
        lblQty.setForeground(new Color(180, 180, 180));
        lblQty.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(lblQty);
        controlPanel.add(Box.createVerticalStrut(4));
        spinQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 100, 1));
        spinQuantity.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        spinQuantity.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(spinQuantity);
        controlPanel.add(Box.createVerticalStrut(15));

        // Tình trạng bàn giao
        JLabel lblCond = new JLabel("Tình trạng bàn giao:");
        lblCond.setForeground(new Color(180, 180, 180));
        lblCond.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(lblCond);
        controlPanel.add(Box.createVerticalStrut(4));
        txtCondition = new JTextField("Hoạt động tốt");
        txtCondition.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtCondition.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlPanel.add(txtCondition);
        controlPanel.add(Box.createVerticalStrut(25));

        // Các nút CRUD
        btnAdd = new JButton("➕ Thêm tài sản");
        btnUpdate = new JButton("⚙️ Cập nhật");
        btnDelete = new JButton("❌ Xóa tài sản");

        for (JButton btn : new JButton[]{btnAdd, btnUpdate, btnDelete}) {
            btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btn.setForeground(Color.WHITE);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            btn.setAlignmentX(Component.LEFT_ALIGNMENT);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        btnAdd.setBackground(new Color(46, 204, 113));
        btnUpdate.setBackground(new Color(52, 152, 219));
        btnDelete.setBackground(new Color(231, 76, 60));

        btnAdd.addActionListener(e -> addAsset());
        btnUpdate.addActionListener(e -> updateAsset());
        btnDelete.addActionListener(e -> deleteAsset());

        controlPanel.add(btnAdd);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(btnUpdate);
        controlPanel.add(Box.createVerticalStrut(10));
        controlPanel.add(btnDelete);

        add(controlPanel, BorderLayout.EAST);

        // Sự kiện khi click chọn dòng
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) {
                int id = (int) tableModel.getValueAt(row, 0);
                selectedAsset = findAssetById(id);
                if (selectedAsset != null) {
                    txtName.setText(selectedAsset.getTenTaiSan());
                    spinQuantity.setValue(selectedAsset.getSoLuong());
                    txtCondition.setText(selectedAsset.getTinhTrang());
                }
            }
        });

        loadRooms();
    }

    private void loadRooms() {
        cbRoom.removeAllItems();
        String username = Session.getTenDN();
        if (username == null) username = "admin";
        List<PhongTro> list = phongDao.getAll();
        for (PhongTro pt : list) {
            cbRoom.addItem(pt.getMaPhong());
        }
        if (cbRoom.getItemCount() > 0) {
            cbRoom.setSelectedIndex(0);
        }
    }

    private void loadAssets() {
        tableModel.setRowCount(0);
        String selectedRoom = (String) cbRoom.getSelectedItem();
        if (selectedRoom == null) return;

        String username = Session.getTenDN();
        if (username == null) username = "admin";

        List<TaiSan> list = dao.getByMaPhong(selectedRoom, username);
        for (TaiSan ts : list) {
            tableModel.addRow(new Object[]{
                ts.getId(),
                ts.getTenTaiSan(),
                ts.getSoLuong(),
                ts.getTinhTrang()
            });
        }
        clearForm();
    }

    private TaiSan findAssetById(int id) {
        String selectedRoom = (String) cbRoom.getSelectedItem();
        String username = Session.getTenDN();
        if (selectedRoom == null || username == null) return null;
        List<TaiSan> list = dao.getByMaPhong(selectedRoom, username);
        for (TaiSan ts : list) {
            if (ts.getId() == id) return ts;
        }
        return null;
    }

    private void clearForm() {
        txtName.setText("");
        spinQuantity.setValue(1);
        txtCondition.setText("Hoạt động tốt");
        selectedAsset = null;
        table.clearSelection();
    }

    private void addAsset() {
        String selectedRoom = (String) cbRoom.getSelectedItem();
        String username = Session.getTenDN();
        if (selectedRoom == null || username == null) return;

        String name = txtName.getText().trim();
        int qty = (int) spinQuantity.getValue();
        String cond = txtCondition.getText().trim();

        if (name.isEmpty() || cond.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đủ thông tin tài sản!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        TaiSan ts = new TaiSan();
        ts.setMaPhong(selectedRoom);
        ts.setTenTaiSan(name);
        ts.setSoLuong(qty);
        ts.setTinhTrang(cond);
        ts.setTenDN(username);

        if (dao.insert(ts)) {
            JOptionPane.showMessageDialog(this, "Thêm tài sản mới thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadAssets();
        } else {
            JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi thêm tài sản!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateAsset() {
        if (selectedAsset == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài sản cần cập nhật!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String name = txtName.getText().trim();
        int qty = (int) spinQuantity.getValue();
        String cond = txtCondition.getText().trim();

        if (name.isEmpty() || cond.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng điền đủ thông tin!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        selectedAsset.setTenTaiSan(name);
        selectedAsset.setSoLuong(qty);
        selectedAsset.setTinhTrang(cond);

        if (dao.update(selectedAsset)) {
            JOptionPane.showMessageDialog(this, "Cập nhật tài sản thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
            loadAssets();
        } else {
            JOptionPane.showMessageDialog(this, "Lỗi xảy ra khi cập nhật tài sản!", "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteAsset() {
        if (selectedAsset == null) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn tài sản cần xóa!", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa tài sản '" + selectedAsset.getTenTaiSan() + "'?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.delete(selectedAsset.getId())) {
                JOptionPane.showMessageDialog(this, "Xóa tài sản thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadAssets();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi xảy ra khi xóa tài sản!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void loadData() {
        loadRooms();
    }
}
