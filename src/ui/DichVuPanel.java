package ui;

import dao.DichVuDAO;
import model.DichVu;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DichVuPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private DichVuDAO dvDao = new DichVuDAO();
    private String maPhongFilter = null;

    public DichVuPanel() {
        this(null);
    }

    public DichVuPanel(String maPhongFilter) {
        this.maPhongFilter = maPhongFilter;
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentShown(java.awt.event.ComponentEvent evt) {
                loadData();
            }
        });

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(18, 19, 24)); 
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel toolBar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolBar.setOpaque(false);
        
        JButton btnAdd = new JButton("➕ Thêm mới");
        JButton btnEdit = new JButton("📝 Sửa");
        JButton btnDelete = new JButton("🗑️ Xóa");
        JButton btnReload = new JButton("🔄 Làm mới");
        
        if (maPhongFilter != null) {
            btnAdd.setVisible(false);
            btnEdit.setVisible(false);
            btnDelete.setVisible(false);
        }
        
        toolBar.add(btnAdd); toolBar.add(btnEdit); toolBar.add(btnDelete); toolBar.add(btnReload);
        add(toolBar, BorderLayout.NORTH);

        String[] cols = {"ID", "PHÒNG", "WIFI", "THANG MÁY", "GIỮ XE", "VỆ SINH/PCCC", "TỔNG TIỀN", "TRẠNG THÁI"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        
        table = new JTable(model);
        table.setRowHeight(35);
        table.setBackground(new Color(30, 31, 38)); 
        table.setForeground(Color.WHITE);           
        table.setGridColor(new Color(50, 50, 60));  
        table.getTableHeader().setBackground(new Color(45, 45, 55)); 
        table.getTableHeader().setForeground(Color.WHITE);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                Object val = table.getModel().getValueAt(row, 7);
                String trangThai = (val != null) ? val.toString() : "Trống";
                
                if (trangThai.equalsIgnoreCase("Đã thuê")) {
                    c.setBackground(new Color(255, 215, 0)); // Màu vàng
                    c.setForeground(Color.BLACK);            
                } else {
                    c.setBackground(new Color(30, 31, 38));  // Màu đen mặc định
                    c.setForeground(Color.WHITE);
                }
                
                if (isSelected) {
                    c.setBackground(new Color(75, 110, 175)); 
                    c.setForeground(Color.WHITE);
                }
                return c;
            }
        });

        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(7).setMinWidth(100);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(new Color(18, 19, 24)); 
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 50, 60)));
        add(scrollPane, BorderLayout.CENTER);

        // Sự kiện
        btnReload.addActionListener(e -> loadData());
        btnEdit.addActionListener(e -> handleEdit());
        btnAdd.addActionListener(e -> handleAdd());
        btnDelete.addActionListener(e -> handleDelete());

        loadData();
    }

    public void loadData() {
        model.setRowCount(0);
        List<DichVu> list = dvDao.getAll();
        for (DichVu dv : list) {
            if (maPhongFilter != null && !dv.getMaPhong().equalsIgnoreCase(maPhongFilter)) {
                continue;
            }
            model.addRow(new Object[]{
                dv.getId(), 
                dv.getMaPhong(),
                dv.getWifi(),
                dv.getThangMay(),
                dv.getGiuXe(),
                dv.getPcccVeSinh(),
                String.format("%,.0f đ", dv.getTongTien()),
                dv.getGhiChu() != null ? dv.getGhiChu() : "Trống"
            });
        }
    }

    private void handleEdit() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn một phòng trong bảng để sửa!");
            return;
        }

        try {
            // 1. Lấy thông tin hiện tại từ dòng đang chọn
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            String maPhong = model.getValueAt(row, 1).toString();
            double w = Double.parseDouble(model.getValueAt(row, 2).toString());
            double t = Double.parseDouble(model.getValueAt(row, 3).toString());
            double x = Double.parseDouble(model.getValueAt(row, 4).toString());
            double p = Double.parseDouble(model.getValueAt(row, 5).toString());

            // 2. Hiện hộp thoại nhập liệu (Dùng bảng nhập liệu tùy chỉnh hoặc input đơn giản)
            String inputW = JOptionPane.showInputDialog(this, "Cập nhật tiền WIFI cho phòng " + maPhong, w);
            if (inputW == null) return; // Nhấn Cancel thì thoát

            // 3. Tạo đối tượng và gửi xuống DAO
            DichVu dv = new DichVu();
            dv.setId(id);
            dv.setWifi(Double.parseDouble(inputW));
            dv.setThangMay(t); // Giữ nguyên các giá trị cũ hoặc hiện thêm Input cho các giá trị này
            dv.setGiuXe(x);
            dv.setPcccVeSinh(p);

            if (dvDao.update(dv)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadData(); // Load lại để SQL tự tính lại Tổng Tiền và Renderer tô lại màu
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật thất bại!");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi: Vui lòng nhập số hợp lệ!");
        }
    }
    private void handleAdd() {
        String phong = JOptionPane.showInputDialog(this, "Nhập mã phòng:");
        if (phong != null && !phong.trim().isEmpty()) {
            DichVu dv = new DichVu();
            dv.setMaPhong(phong);
            dv.setWifi(50000); 
            dv.setThangMay(50000);
            dv.setGiuXe(100000);
            dv.setPcccVeSinh(50000);
            if (dvDao.add(dv)) {
                loadData();
            } else {
                JOptionPane.showMessageDialog(this, "Lỗi: Có thể phòng đã tồn tại!");
            }
        }
    }

    private void handleDelete() {
        int row = table.getSelectedRow();
        if (row == -1) return;
        int id = (int) model.getValueAt(row, 0);
        int conf = JOptionPane.showConfirmDialog(this, "Xóa dịch vụ của phòng này?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (conf == JOptionPane.YES_OPTION) {
            if (dvDao.delete(id)) {
                loadData();
            }
        }
    }
}