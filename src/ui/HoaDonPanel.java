package ui;

import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import dao.HoaDonDAO;
import dao.CauHinhNganHangDAO;
import model.HoaDon;
import model.CauHinhNganHang;
import util.Session;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class HoaDonPanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private HoaDonDAO hdao = new HoaDonDAO();
    private DecimalFormat df = new DecimalFormat("#,### VNĐ");

    // Thêm 2 biến ComboBox để lọc tháng/năm
    private JComboBox<Integer> cboThang;
    private JComboBox<Integer> cboNam;

    // --- CẤU HÌNH MÀU SẮC ---
    private final Color COLOR_BG = new Color(33, 37, 41);
    private final Color COLOR_CELL_BG = new Color(43, 48, 53);
    private final Color COLOR_TEXT = new Color(220, 220, 220);
    private final Color COLOR_YELLOW_BTN = new Color(255, 193, 7);
    private final Color COLOR_BLUE_BTN = new Color(52, 152, 219);
    private final Color COLOR_GREEN_BTN = new Color(46, 204, 113);
    private final Color COLOR_RED_BTN = new Color(220, 53, 69);
    private final Color COLOR_TEXT_BLACK = new Color(0, 0, 0);

    private String maPhongFilter = null;

    public HoaDonPanel() {
        this(null);
    }

    public HoaDonPanel(String maPhongFilter) {
        this.maPhongFilter = maPhongFilter;
        setLayout(new BorderLayout(10, 10));
        setBackground(COLOR_BG);
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initComponents();
        loadData();
    }

    private void initComponents() {
        // --- 1. PHẦN TRÊN: THANH CÔNG CỤ (TOOLBAR) ---
        JPanel pnlToolbar = new JPanel(new BorderLayout());
        pnlToolbar.setBackground(COLOR_BG);
        pnlToolbar.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        // Nhóm Nút chức năng (Bên trái)
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlButtons.setBackground(COLOR_BG);
        JButton btnAdd = createStyledButton("➕ THÊM HÓA ĐƠN", COLOR_BLUE_BTN, Color.WHITE);
        JButton btnAutoCreate = createStyledButton("⚡ TẠO HĐ THÁNG TỚI", COLOR_YELLOW_BTN, COLOR_TEXT_BLACK);
        JButton btnAutoUpdate = createStyledButton("🔄 CẬP NHẬT TIỀN", COLOR_GREEN_BTN, Color.WHITE);
        JButton btnPay = createStyledButton("✅ THANH TOÁN", COLOR_YELLOW_BTN, COLOR_TEXT_BLACK);
        JButton btnDelete = createStyledButton("🗑️ XÓA", COLOR_RED_BTN, Color.WHITE);
        JButton btnQR = createStyledButton("🖼️ CẤU HÌNH QR", COLOR_BLUE_BTN, Color.WHITE);
        JButton btnShowQR = createStyledButton("🖼️ XEM QR THANH TOÁN", COLOR_GREEN_BTN, Color.WHITE);

        if (maPhongFilter != null) {
            btnAdd.setVisible(false);
            btnAutoCreate.setVisible(false);
            btnAutoUpdate.setVisible(false);
            btnPay.setVisible(false);
            btnDelete.setVisible(false);
            btnQR.setVisible(false);
            btnShowQR.setVisible(true);
        } else {
            btnAdd.setVisible(true);
            btnAutoCreate.setVisible(true);
            btnAutoUpdate.setVisible(true);
            btnPay.setVisible(true);
            btnDelete.setVisible(true);
            btnQR.setVisible(true);
            btnShowQR.setVisible(false);
        }

        pnlButtons.add(btnAdd);
        pnlButtons.add(btnAutoCreate);
        pnlButtons.add(btnAutoUpdate);
        pnlButtons.add(btnPay);
        pnlButtons.add(btnDelete);
        pnlButtons.add(btnQR);
        pnlButtons.add(btnShowQR);

        // Nhóm Lọc dữ liệu (Bên phải)
        JPanel pnlFilter = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlFilter.setBackground(COLOR_BG);
        
        JLabel lblThang = new JLabel("Tháng:");
        lblThang.setForeground(Color.WHITE);
        lblThang.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        Integer[] thangs = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
        cboThang = new JComboBox<>(thangs);
        cboThang.setSelectedItem(LocalDate.now().getMonthValue()); // Mặc định tháng hiện tại
        
        JLabel lblNam = new JLabel("Năm:");
        lblNam.setForeground(Color.WHITE);
        lblNam.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        Integer[] nams = new Integer[10];
        int currentYear = LocalDate.now().getYear();
        for(int i = 0; i < 10; i++) nams[i] = currentYear - 3 + i; // Lấy từ 3 năm trước đến 6 năm sau
        cboNam = new JComboBox<>(nams);
        cboNam.setSelectedItem(currentYear); // Mặc định năm hiện tại

        pnlFilter.add(lblThang);
        pnlFilter.add(cboThang);
        pnlFilter.add(lblNam);
        pnlFilter.add(cboNam);

        // Ghép 2 nhóm vào Toolbar
        pnlToolbar.add(pnlButtons, BorderLayout.WEST);
        pnlToolbar.add(pnlFilter, BorderLayout.EAST);
        add(pnlToolbar, BorderLayout.NORTH);

        // --- Sự kiện khi chọn Tháng/Năm khác thì tải lại bảng ---
        cboThang.addActionListener(e -> loadData());
        cboNam.addActionListener(e -> loadData());

        // --- 2. PHẦN GIỮA: BẢNG DỮ LIỆU ---
        String[] cols = {"Mã HD", "Phòng", "Ngày Lập", "Tiền Phòng", "Điện", "Nước", "Dịch Vụ", "Tổng Tiền", "Trạng Thái"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(model);
        table.setRowHeight(40); 
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setBackground(COLOR_CELL_BG);
        table.setForeground(COLOR_TEXT);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setGridColor(new Color(60, 60, 60)); 
        table.setSelectionBackground(new Color(73, 80, 87)); 

        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(52, 58, 64));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));

        setupTableRenderers();

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.getViewport().setBackground(COLOR_BG);
        add(scrollPane, BorderLayout.CENTER);

        // --- 3. XỬ LÝ SỰ KIỆN NÚT BẤM ---

        btnAdd.addActionListener(e -> {
            String tenPhong = JOptionPane.showInputDialog(this, "Nhập Tên Phòng:");
            if (tenPhong != null && !tenPhong.trim().isEmpty()) {
                if (hdao.addManualInvoice(tenPhong)) {
                    JOptionPane.showMessageDialog(this, "Thành công!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi: Không tìm thấy phòng hoặc phòng chưa thuê.");
                }
            }
        });

        btnAutoCreate.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Hệ thống sẽ quét khách đang thuê và tạo hóa đơn cho THÁNG TỚI. Tiếp tục?", 
                "Xác nhận", JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                int count = hdao.autoGenerateNextMonthInvoices(); 
                if (count > 0) {
                    JOptionPane.showMessageDialog(this, "Đã tạo thành công " + count + " hóa đơn cho tháng tới!");
                    // Tự động chuyển ComboBox sang tháng tới để xem hóa đơn vừa tạo
                    LocalDate nextMonth = LocalDate.now().plusMonths(1);
                    cboThang.setSelectedItem(nextMonth.getMonthValue());
                    cboNam.setSelectedItem(nextMonth.getYear());
                } else {
                    JOptionPane.showMessageDialog(this, "Không có hóa đơn mới (Đã tồn tại hóa đơn tháng tới hoặc không có khách thuê).");
                }
            }
        });

        // ĐÃ SỬA: Đồng bộ gọi đúng hàm lấy Tháng/Năm từ ComboBox thay vì hàm cũ
        btnAutoUpdate.addActionListener(e -> {
            int thang = (int) cboThang.getSelectedItem();
            int nam = (int) cboNam.getSelectedItem();
            
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Đồng bộ tiền điện nước từ bảng Điện Nước sang Hóa Đơn cho tháng " + String.format("%02d/%d", thang, nam) + "?", 
                "Xác nhận", JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                if (hdao.syncFromDienNuoc(thang, nam)) {
                    JOptionPane.showMessageDialog(this, "Đồng bộ thành công!");
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(this, "Không có dữ liệu mới để cập nhật hoặc hóa đơn đã thu tiền!");
                }
            }
        });

        btnPay.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                Object maObj = model.getValueAt(row, 0);
                int maHD = Integer.parseInt(maObj.toString());
                
                String trangThaiHienTai = model.getValueAt(row, 8).toString();
                
                if (trangThaiHienTai.equals("Đã thu")) {
                    JOptionPane.showMessageDialog(this, "Hóa đơn này đã được thanh toán rồi!");
                    return;
                }

                if (JOptionPane.showConfirmDialog(this, "Xác nhận khách đã thanh toán hóa đơn này?", 
                        "Xác nhận thanh toán", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    
                    if (hdao.updateStatus(maHD, "Đã thu")) {
                        JOptionPane.showMessageDialog(this, "Thanh toán thành công!");
                        
                        // --- ĐỒNG BỘ SANG THỐNG KÊ ---
                        Window parentWindow = SwingUtilities.getWindowAncestor(this);
                        if (parentWindow instanceof MainFrame) {
                            MainFrame main = (MainFrame) parentWindow;
                            if (main.getThongKePanel() != null) {
                                main.getThongKePanel().loadData(); 
                            }
                        }

                        loadData(); 
                    } else {
                        JOptionPane.showMessageDialog(this, "Có lỗi xảy ra khi cập nhật trạng thái!");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn để thanh toán!");
            }
        });

        btnDelete.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row != -1) {
                int maHD = Integer.parseInt(model.getValueAt(row, 0).toString());
                if (JOptionPane.showConfirmDialog(this, "Xóa hóa đơn này?", "Cảnh báo", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (hdao.delete(maHD)) loadData();
                }
            }
        });

        btnQR.addActionListener(e -> showQRDialog(true));
        btnShowQR.addActionListener(e -> showQRDialog(false));
    }

    private void showQRDialog(boolean isConfig) {
        String landlordUser = Session.getTenDN();
        if (landlordUser == null || landlordUser.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không xác định được chủ trọ!");
            return;
        }

        if (!isConfig) {
            // Kiểm tra chọn hóa đơn trước khi xem QR thanh toán
            int row = table.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Vui lòng chọn một hóa đơn trong bảng để thanh toán VietQR!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Object maObj = model.getValueAt(row, 0);
            int maHD = Integer.parseInt(maObj.toString());
            String tenPhong = model.getValueAt(row, 1).toString();
            String trangThai = model.getValueAt(row, 8).toString();
            String tongTienStr = model.getValueAt(row, 7).toString().replaceAll("[^\\d]", "");
            double amount = Double.parseDouble(tongTienStr);

            if ("Đã thu".equals(trangThai)) {
                JOptionPane.showMessageDialog(this, "Hóa đơn này đã được thanh toán rồi!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            // Màn hình quét mã QR của khách thuê
            JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Quét mã QR để thanh toán", Dialog.ModalityType.APPLICATION_MODAL);
            dlg.setSize(420, 600);
            dlg.setLocationRelativeTo(this);
            dlg.setResizable(false);

            JPanel pnl = new JPanel(new BorderLayout(10, 10));
            pnl.setBackground(COLOR_BG);
            pnl.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JLabel lblInfo = new JLabel("ĐANG TẠO MÃ VIETQR THANH TOÁN...", SwingConstants.CENTER);
            lblInfo.setForeground(Color.WHITE);
            lblInfo.setFont(new Font("Segoe UI", Font.BOLD, 13));
            pnl.add(lblInfo, BorderLayout.NORTH);

            JLabel lblImage = new JLabel("Đang tải...", SwingConstants.CENTER);
            lblImage.setForeground(Color.LIGHT_GRAY);
            lblImage.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
            pnl.add(lblImage, BorderLayout.CENTER);

            JPanel pnlDetails = new JPanel();
            pnlDetails.setLayout(new BoxLayout(pnlDetails, BoxLayout.Y_AXIS));
            pnlDetails.setOpaque(false);
            pnlDetails.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

            JLabel lblBankInfo = new JLabel("Đang truy vấn thông tin tài khoản chủ trọ...", SwingConstants.CENTER);
            lblBankInfo.setForeground(Color.WHITE);
            lblBankInfo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            lblBankInfo.setAlignmentX(Component.CENTER_ALIGNMENT);
            pnlDetails.add(lblBankInfo);
            pnl.add(pnlDetails, BorderLayout.SOUTH);

            // Nút điều khiển
            JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            pnlBtns.setOpaque(false);
            JButton btnPayConfirm = createStyledButton("✅ Xác nhận đã chuyển khoản", COLOR_GREEN_BTN, Color.WHITE);
            JButton btnClose = createStyledButton("Đóng", new Color(100, 100, 100), Color.WHITE);
            pnlBtns.add(btnPayConfirm);
            pnlBtns.add(btnClose);
            pnlDetails.add(Box.createVerticalStrut(10));
            pnlDetails.add(pnlBtns);

            btnClose.addActionListener(e -> dlg.dispose());
            btnPayConfirm.addActionListener(e -> {
                if (hdao.updateStatus(maHD, "Đã thu")) {
                    JOptionPane.showMessageDialog(dlg, "Thanh toán thành công! Hóa đơn phòng " + tenPhong + " đã chuyển sang trạng thái Đã thu.", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                    dlg.dispose();
                    loadData();
                } else {
                    JOptionPane.showMessageDialog(dlg, "Lỗi cập nhật trạng thái hóa đơn!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });

            // Tải thông tin tài khoản và sinh VietQR trong luồng nền
            new Thread(() -> {
                CauHinhNganHangDAO chDAO = new CauHinhNganHangDAO();
                CauHinhNganHang ch = chDAO.getByTenDN(landlordUser);
                if (ch != null) {
                    try {
                        String bank = ch.getTenNganHang();
                        String account = ch.getSoTaiKhoan();
                        String addInfo = "TT_HD_" + maHD + "_Phong_" + tenPhong.trim();
                        String encodedAddInfo = java.net.URLEncoder.encode(addInfo, "UTF-8");
                        String qrUrl = "https://img.vietqr.io/image/" + bank + "-" + account + "-compact.png?amount=" + (long)amount + "&addInfo=" + encodedAddInfo;

                        java.net.URL url = new java.net.URL(qrUrl);
                        ImageIcon tempIcon = new ImageIcon(url);
                        if (tempIcon.getIconWidth() > 0) {
                            Image img = tempIcon.getImage().getScaledInstance(280, 280, Image.SCALE_SMOOTH);
                            SwingUtilities.invokeLater(() -> {
                                lblImage.setIcon(new ImageIcon(img));
                                lblImage.setText("");
                                lblInfo.setText("QUÉT MÃ VIETQR ĐỂ CHUYỂN KHOẢN TỰ ĐỘNG");
                                lblBankInfo.setText("<html><center>"
                                    + "Ngân hàng: <b>" + ch.getTenNganHang().toUpperCase() + "</b> | Số tài khoản: <b>" + ch.getSoTaiKhoan() + "</b><br>"
                                    + "Chủ tài khoản: <b>" + ch.getTenChuTaiKhoan() + "</b><br>"
                                    + "Số tiền: <b style='color:#2ecc71;'>" + df.format(amount) + "</b> | Nội dung: <b>" + addInfo + "</b>"
                                    + "</center></html>");
                            });
                        } else {
                            throw new Exception("Kích thước ảnh bằng 0");
                        }
                    } catch (Exception ex) {
                        System.err.println("Lỗi tạo VietQR: " + ex.getMessage());
                        SwingUtilities.invokeLater(() -> {
                            lblImage.setText("<html><center>Không thể tạo VietQR động.<br>Đang tải mã QR tĩnh...</center></html>");
                            loadStaticQR(landlordUser, lblImage, lblInfo, lblBankInfo);
                        });
                    }
                } else {
                    SwingUtilities.invokeLater(() -> {
                        lblImage.setText("<html><center>Chủ trọ chưa cấu hình ngân hàng.<br>Đang tải mã QR tĩnh...</center></html>");
                        loadStaticQR(landlordUser, lblImage, lblInfo, lblBankInfo);
                    });
                }
            }).start();

            dlg.setContentPane(pnl);
            dlg.setVisible(true);
        } else {
            // Màn hình cấu hình của chủ trọ
            JDialog dlg = new JDialog(SwingUtilities.getWindowAncestor(this), "Cấu hình thanh toán", Dialog.ModalityType.APPLICATION_MODAL);
            dlg.setSize(480, 420);
            dlg.setLocationRelativeTo(this);
            dlg.setResizable(false);

            JTabbedPane tabPane = new JTabbedPane();

            // TAB 1: Cấu hình tài khoản VietQR động
            JPanel pnlDynamic = new JPanel(new GridBagLayout());
            pnlDynamic.setBackground(COLOR_BG);
            pnlDynamic.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(10, 5, 10, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            JLabel lblBank = new JLabel("Chọn Ngân hàng:");
            lblBank.setForeground(Color.WHITE);
            String[] banks = {"vcb", "mbbank", "tpb", "bidv", "vietinbank", "tcb", "acb", "shb", "vib", "vpb"};
            JComboBox<String> cbBanks = new JComboBox<>(banks);

            JLabel lblAcc = new JLabel("Số tài khoản:");
            lblAcc.setForeground(Color.WHITE);
            JTextField txtAcc = new JTextField(15);

            JLabel lblOwner = new JLabel("Tên chủ tài khoản:");
            lblOwner.setForeground(Color.WHITE);
            JTextField txtOwner = new JTextField(15);

            // Load existing config
            CauHinhNganHangDAO chDAO = new CauHinhNganHangDAO();
            CauHinhNganHang ch = chDAO.getByTenDN(landlordUser);
            if (ch != null) {
                cbBanks.setSelectedItem(ch.getTenNganHang());
                txtAcc.setText(ch.getSoTaiKhoan());
                txtOwner.setText(ch.getTenChuTaiKhoan());
            }

            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
            pnlDynamic.add(lblBank, gbc);
            gbc.gridx = 1; gbc.weightx = 0.7;
            pnlDynamic.add(cbBanks, gbc);

            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
            pnlDynamic.add(lblAcc, gbc);
            gbc.gridx = 1; gbc.weightx = 0.7;
            pnlDynamic.add(txtAcc, gbc);

            gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
            pnlDynamic.add(lblOwner, gbc);
            gbc.gridx = 1; gbc.weightx = 0.7;
            pnlDynamic.add(txtOwner, gbc);

            JButton btnSaveBank = createStyledButton("💾 Lưu thông tin tài khoản", COLOR_GREEN_BTN, Color.WHITE);
            gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
            gbc.insets = new Insets(20, 5, 10, 5);
            pnlDynamic.add(btnSaveBank, gbc);

            btnSaveBank.addActionListener(e -> {
                String selectedBank = cbBanks.getSelectedItem().toString();
                String acc = txtAcc.getText().trim();
                String owner = txtOwner.getText().trim();

                if (acc.isEmpty() || owner.isEmpty()) {
                    JOptionPane.showMessageDialog(dlg, "Vui lòng điền đầy đủ số tài khoản và tên chủ tài khoản!", "Thông báo", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                CauHinhNganHang newCh = new CauHinhNganHang();
                newCh.setTenDN(landlordUser);
                newCh.setTenNganHang(selectedBank);
                newCh.setSoTaiKhoan(acc);
                newCh.setTenChuTaiKhoan(owner);

                if (chDAO.save(newCh)) {
                    JOptionPane.showMessageDialog(dlg, "Cập nhật tài khoản ngân hàng thành công!", "Thành công", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(dlg, "Lưu thông tin thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            });

            tabPane.addTab("Cấu hình VietQR Động", pnlDynamic);

            // TAB 2: Upload mã QR tĩnh (thiết kế cũ)
            JPanel pnlStatic = new JPanel(new BorderLayout(10, 10));
            pnlStatic.setBackground(COLOR_BG);
            pnlStatic.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

            JLabel lblStaticInfo = new JLabel("Tải lên ảnh QR thanh toán cố định", SwingConstants.CENTER);
            lblStaticInfo.setForeground(Color.WHITE);
            pnlStatic.add(lblStaticInfo, BorderLayout.NORTH);

            JLabel lblStaticImage = new JLabel("", SwingConstants.CENTER);
            lblStaticImage.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 60)));
            pnlStatic.add(lblStaticImage, BorderLayout.CENTER);

            Runnable loadStaticImageHelper = () -> {
                File qrFile = new File("qrcodes/" + landlordUser + "_qr.png");
                if (qrFile.exists()) {
                    ImageIcon icon = new ImageIcon(qrFile.getAbsolutePath());
                    Image img = icon.getImage().getScaledInstance(260, 260, Image.SCALE_SMOOTH);
                    lblStaticImage.setIcon(new ImageIcon(img));
                    lblStaticImage.setText("");
                } else {
                    lblStaticImage.setIcon(null);
                    lblStaticImage.setText("Chưa tải ảnh QR lên");
                    lblStaticImage.setForeground(Color.GRAY);
                }
            };
            loadStaticImageHelper.run();

            JPanel pnlStaticBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
            pnlStaticBtns.setOpaque(false);
            JButton btnUpload = createStyledButton("📤 Tải ảnh lên", COLOR_BLUE_BTN, Color.WHITE);
            JButton btnDeleteQR = createStyledButton("🗑️ Xóa mã QR", COLOR_RED_BTN, Color.WHITE);
            pnlStaticBtns.add(btnUpload);
            pnlStaticBtns.add(btnDeleteQR);
            pnlStatic.add(pnlStaticBtns, BorderLayout.SOUTH);

            btnUpload.addActionListener(e -> {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Image files", "png", "jpg", "jpeg"));
                int res = fileChooser.showOpenDialog(dlg);
                if (res == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    File dir = new File("qrcodes");
                    if (!dir.exists()) dir.mkdirs();
                    File destFile = new File(dir, landlordUser + "_qr.png");
                    try {
                        Files.copy(selectedFile.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                        JOptionPane.showMessageDialog(dlg, "Tải lên thành công!");
                        loadStaticImageHelper.run();
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(dlg, "Lỗi tải ảnh lên: " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            });

            btnDeleteQR.addActionListener(e -> {
                File qrFile = new File("qrcodes/" + landlordUser + "_qr.png");
                if (qrFile.exists()) {
                    if (qrFile.delete()) {
                        JOptionPane.showMessageDialog(dlg, "Đã xóa thành công!");
                        loadStaticImageHelper.run();
                    } else {
                        JOptionPane.showMessageDialog(dlg, "Không thể xóa tệp tin!");
                    }
                }
            });

            tabPane.addTab("Tải QR Tĩnh lên", pnlStatic);

            dlg.setContentPane(tabPane);
            dlg.setVisible(true);
        }
    }

    private JButton createStyledButton(String text, Color bgColor, Color textColor) {
        JButton btn = new JButton(text);
        btn.setBackground(bgColor);
        btn.setForeground(textColor);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        return btn;
    }

    private void setupTableRenderers() {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        table.getColumnModel().getColumn(8).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean s, boolean f, int r, int c) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(t, v, s, f, r, c);
                lbl.setHorizontalAlignment(JLabel.CENTER);
                lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
                if (v != null) {
                    lbl.setForeground(v.toString().contains("Chưa") ? new Color(231, 76, 60) : new Color(46, 204, 113));
                }
                return lbl;
            }
        });
    }
 
    public void loadData() {
        if (model == null || cboThang == null || cboNam == null) return;
        
        model.setRowCount(0); 
        
        // Lấy tháng và năm từ giao diện
        int thangDuocChon = (int) cboThang.getSelectedItem();
        int namDuocChon = (int) cboNam.getSelectedItem();

        List<HoaDon> list = hdao.getHoaDonTheoThang(thangDuocChon, namDuocChon);
        
        for (HoaDon hd : list) {
            if (maPhongFilter != null && !hd.getTenPhong().equalsIgnoreCase(maPhongFilter)) {
                continue;
            }
            double tong = hd.getTienPhong() + hd.getTienDien() + hd.getTienNuoc() + hd.getTienDichVu();
            model.addRow(new Object[]{
                hd.getMaHoaDon(), hd.getTenPhong(), hd.getNgayLap(),
                df.format(hd.getTienPhong()), df.format(hd.getTienDien()),
                df.format(hd.getTienNuoc()), df.format(hd.getTienDichVu()),
                df.format(tong), hd.getTrangThai()
            });
        }
    }

    private void loadStaticQR(String landlordUser, JLabel lblImage, JLabel lblInfo, JLabel lblBankInfo) {
        File qrFile = new File("qrcodes/" + landlordUser + "_qr.png");
        if (qrFile.exists()) {
            ImageIcon icon = new ImageIcon(qrFile.getAbsolutePath());
            Image img = icon.getImage().getScaledInstance(280, 280, Image.SCALE_SMOOTH);
            lblImage.setIcon(new ImageIcon(img));
            lblImage.setText("");
            lblInfo.setText("QUÉT MÃ QR CỐ ĐỊNH CỦA CHỦ TRỌ");
            lblBankInfo.setText("<html><center>Chuyển khoản thủ công theo mã QR trên</center></html>");
        } else {
            lblImage.setIcon(null);
            lblImage.setText("<html><center>Chưa cấu hình QR thanh toán</center></html>");
            lblInfo.setText("KHÔNG THỂ THANH TOÁN");
            lblBankInfo.setText("<html><center>Chủ trọ chưa cấu hình tài khoản ngân hàng hoặc mã QR tĩnh.</center></html>");
        }
    }
}