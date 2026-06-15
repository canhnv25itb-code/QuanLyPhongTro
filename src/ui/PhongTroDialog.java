package ui;

import model.PhongTro;
import javax.swing.*;
import java.awt.*;

public class PhongTroDialog extends JDialog {
    private JTextField txtMa, txtTang;
    private JComboBox<String> cbStatus;
    private boolean saved = false;
    private PhongTro phongTro;

    public PhongTroDialog(Frame owner, PhongTro p) {
        super(owner, true);
        setTitle(p == null ? "Thêm phòng mới" : "Cập nhật phòng");
        setSize(400, 220); // Đã thu gọn chiều cao cho vừa vặn với 3 trường dữ liệu
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        // Form nhập liệu (Chỉ còn 3 hàng: Mã, Tầng, Trạng thái)
        JPanel form = new JPanel(new GridLayout(3, 2, 10, 10));
        form.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        form.add(new JLabel("Mã phòng (VD: P101):"));
        txtMa = new JTextField();
        form.add(txtMa);

        form.add(new JLabel("Tầng (1-5):"));
        txtTang = new JTextField();
        form.add(txtTang);

        form.add(new JLabel("Trạng thái:"));
        cbStatus = new JComboBox<>(new String[]{"Trống", "Đã thuê"});
        form.add(cbStatus);

        add(form, BorderLayout.CENTER);

        // Nút điều khiển
        JButton btnSave = new JButton("Lưu dữ liệu");
        JButton btnCancel = new JButton("Hủy bỏ");
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.add(btnSave);
        bottom.add(btnCancel);
        add(bottom, BorderLayout.SOUTH);

        // Đổ dữ liệu cũ vào nếu là chế độ Sửa
        if (p != null) {
            this.phongTro = p;
            txtMa.setText(p.getMaPhong());
            txtMa.setEditable(false); // Không cho sửa mã phòng (Primary Key)
            txtTang.setText(String.valueOf(p.getTang()));
            cbStatus.setSelectedItem(p.getTrangThai());
        }

        // Sự kiện nút Lưu
        btnSave.addActionListener(e -> {
            if (validateForm()) {
                if (phongTro == null) phongTro = new PhongTro();
                
                phongTro.setMaPhong(txtMa.getText().trim());
                phongTro.setTang(Integer.parseInt(txtTang.getText().trim()));
                phongTro.setTrangThai(cbStatus.getSelectedItem().toString());
                
                saved = true;
                dispose();
            }
        });

        btnCancel.addActionListener(e -> dispose());
    }

    private boolean validateForm() {
        if (txtMa.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Mã phòng không được để trống!");
            return false;
        }
        try {
            int tang = Integer.parseInt(txtTang.getText().trim());
            if (tang < 1 || tang > 5) throw new Exception();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Tầng phải là số từ 1 đến 5!");
            return false;
        }
        return true;
    }

    public boolean isSaved() { return saved; }
    public PhongTro getPhongTro() { return phongTro; }
}