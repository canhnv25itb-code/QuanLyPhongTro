package ui;

import dao.HoaDonDAO;
import dao.HopDongDAO;
import dao.TaiSanDAO;
import dao.ThongBaoDAO;
import model.HoaDon;
import model.HopDong;
import model.TaiSan;
import model.ThongBao;
import util.Session;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Panel trang chủ / Dashboard dành riêng cho Khách Thuê.
 * Hiển thị tổng quan hóa đơn, thời hạn hợp đồng, bảng tin thông báo và tài sản bàn giao.
 */
public class RenterDashboardPanel extends JPanel {

    private final String maPhong;
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final HopDongDAO hopDongDAO = new HopDongDAO();
    private final ThongBaoDAO thongBaoDAO = new ThongBaoDAO();
    private final TaiSanDAO taiSanDAO = new TaiSanDAO();
    private final DecimalFormat formatter = new DecimalFormat("###,###,### VNĐ");

    // UI Components
    private JPanel pnlCards;
    private JList<String> lstAnnouncements;
    private DefaultListModel<String> listModelAnnouncements;
    private JTable tblAssets;
    private DefaultTableModel modelAssets;

    public RenterDashboardPanel(String maPhong) {
        this.maPhong = maPhong;
        setBackground(new Color(18, 19, 24));
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        // 1. Header
        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("🚀 BẢNG ĐIỀU KHIỂN PHÒNG " + (maPhong != null ? maPhong : ""));
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(Color.WHITE);

        JButton btnRefresh = new JButton("🔄 Làm mới");
        btnRefresh.setBackground(new Color(52, 152, 219));
        btnRefresh.setForeground(Color.WHITE);
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRefresh.setFocusPainted(false);
        btnRefresh.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnRefresh.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRefresh.addActionListener(e -> loadData());

        pnlHeader.add(lblTitle, BorderLayout.WEST);
        pnlHeader.add(btnRefresh, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // 2. Main Content Container
        JPanel pnlMain = new JPanel(new BorderLayout(20, 20));
        pnlMain.setOpaque(false);

        // 2.1. Top Stats Cards
        pnlCards = new JPanel(new GridLayout(1, 3, 20, 0));
        pnlCards.setOpaque(false);
        pnlMain.add(pnlCards, BorderLayout.NORTH);

        // 2.2. Bottom Section (Split Left / Right)
        JPanel pnlBottom = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlBottom.setOpaque(false);

        // Left Column: Announcements
        JPanel pnlAnnounce = new JPanel(new BorderLayout(10, 10));
        pnlAnnounce.setBackground(new Color(30, 31, 38));
        pnlAnnounce.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblAnnounceTitle = new JLabel("📢 BẢNG TIN KHU TRỌ");
        lblAnnounceTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAnnounceTitle.setForeground(new Color(241, 196, 15));
        pnlAnnounce.add(lblAnnounceTitle, BorderLayout.NORTH);

        listModelAnnouncements = new DefaultListModel<>();
        lstAnnouncements = new JList<>(listModelAnnouncements);
        lstAnnouncements.setBackground(new Color(30, 31, 38));
        lstAnnouncements.setForeground(Color.WHITE);
        lstAnnouncements.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lstAnnouncements.setCellRenderer(new AnnouncementCellRenderer());
        
        JScrollPane scrAnnounce = new JScrollPane(lstAnnouncements);
        scrAnnounce.setBorder(null);
        scrAnnounce.getViewport().setBackground(new Color(30, 31, 38));
        pnlAnnounce.add(scrAnnounce, BorderLayout.CENTER);

        // Right Column: Assets
        JPanel pnlAssets = new JPanel(new BorderLayout(10, 10));
        pnlAssets.setBackground(new Color(30, 31, 38));
        pnlAssets.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel lblAssetsTitle = new JLabel("🛋️ TÀI SẢN / TRANG THIẾT BỊ BÀN GIAO");
        lblAssetsTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblAssetsTitle.setForeground(new Color(46, 204, 113));
        pnlAssets.add(lblAssetsTitle, BorderLayout.NORTH);

        modelAssets = new DefaultTableModel(new String[]{"Tên tài sản", "SL", "Tình trạng bàn giao"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        tblAssets = new JTable(modelAssets);
        tblAssets.setRowHeight(32);
        tblAssets.setBackground(new Color(30, 31, 38));
        tblAssets.setForeground(Color.WHITE);
        tblAssets.setGridColor(new Color(50, 50, 60));
        tblAssets.getTableHeader().setBackground(new Color(40, 42, 50));
        tblAssets.getTableHeader().setForeground(Color.WHITE);
        tblAssets.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tblAssets.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);

        JScrollPane scrAssets = new JScrollPane(tblAssets);
        scrAssets.setBorder(null);
        scrAssets.getViewport().setBackground(new Color(30, 31, 38));
        pnlAssets.add(scrAssets, BorderLayout.CENTER);

        pnlBottom.add(pnlAnnounce);
        pnlBottom.add(pnlAssets);
        pnlMain.add(pnlBottom, BorderLayout.CENTER);

        add(pnlMain, BorderLayout.CENTER);

        loadData();
    }

    public void loadData() {
        if (maPhong == null) return;

        // Clear UI
        pnlCards.removeAll();
        listModelAnnouncements.clear();
        modelAssets.setRowCount(0);

        String landlordUser = Session.getTenDN(); // Lấy landlord username qua Session

        // --- 1. Tính toán thẻ Hóa đơn đóng tháng này ---
        double unpaidAmount = 0;
        int currentMonth = LocalDate.now().getMonthValue();
        int currentYear = LocalDate.now().getYear();
        List<HoaDon> hoaDons = hoaDonDAO.getHoaDonTheoThang(currentMonth, currentYear);
        boolean hasInvoice = false;
        for (HoaDon hd : hoaDons) {
            if (hd.getTenPhong().equalsIgnoreCase(maPhong)) {
                hasInvoice = true;
                if ("Chưa thu".equalsIgnoreCase(hd.getTrangThai())) {
                    unpaidAmount += (hd.getTienPhong() + hd.getTienDien() + hd.getTienNuoc() + hd.getTienDichVu());
                }
            }
        }

        String labelHoadon = "TIỀN PHÒNG THÁNG " + currentMonth;
        String valHoadon = hasInvoice ? (unpaidAmount > 0 ? formatter.format(unpaidAmount) : "Đã thanh toán ✅") : "Chưa xuất HĐ";
        Color colorHoadon = unpaidAmount > 0 ? new Color(231, 76, 60) : new Color(46, 204, 113);

        // --- 2. Tính toán thẻ Thời hạn hợp đồng ---
        List<HopDong> hopDongs = hopDongDAO.getAll();
        HopDong activeHd = null;
        for (HopDong hd : hopDongs) {
            if (hd.getMaPhong().equalsIgnoreCase(maPhong) && "Đang hiệu lực".equalsIgnoreCase(hd.getTrangThai())) {
                activeHd = hd;
                break;
            }
        }

        String valHopdong = "Chưa có HĐ";
        Color colorHopdong = new Color(155, 89, 182);
        double activeDeposit = 0;

        if (activeHd != null) {
            activeDeposit = activeHd.getTienCoc();
            LocalDate today = LocalDate.now();
            LocalDate end = activeHd.getNgayKetThuc();
            if (end != null) {
                if (today.isAfter(end)) {
                    valHopdong = "Đã hết hạn";
                    colorHopdong = new Color(231, 76, 60);
                } else {
                    long remainingDays = ChronoUnit.DAYS.between(today, end);
                    valHopdong = remainingDays + " ngày còn lại";
                    if (remainingDays <= 30) {
                        colorHopdong = new Color(230, 126, 34); // Màu cam cảnh báo
                    } else {
                        colorHopdong = new Color(52, 152, 219); // Xanh dương bình thường
                    }
                }
            }
        }

        // --- 3. Tiền đặt cọc ---
        String valDeposit = formatter.format(activeDeposit);

        // Thêm các thẻ vào pnlCards
        pnlCards.add(createStatCard(labelHoadon, valHoadon, colorHoadon));
        pnlCards.add(createStatCard("THỜI HẠN HỢP ĐỒNG", valHopdong, colorHopdong));
        pnlCards.add(createStatCard("TIỀN ĐẶT CỌC HĐ", valDeposit, new Color(155, 89, 182)));

        // --- 4. Tải danh sách thông báo mới nhất ---
        List<ThongBao> thongBaos = thongBaoDAO.getByTenDN(landlordUser);
        if (thongBaos.isEmpty()) {
            listModelAnnouncements.addElement("Không có thông báo nào từ chủ trọ.");
        } else {
            for (ThongBao tb : thongBaos) {
                // Định dạng hiển thị: TieuDe | NgayDang | NoiDung
                String formatted = String.format("<html><b>%s</b> <font color='#bdc3c7' size='3'>(%s)</font><br><p style='margin-top: 4px; color: #ecf0f1;'>%s</p><hr size='1' color='#454550'></html>",
                        tb.getTieuDe(), tb.getNgayDang().toString(), tb.getNoiDung().replace("\n", "<br>"));
                listModelAnnouncements.addElement(formatted);
            }
        }

        // --- 5. Tải danh sách tài sản phòng ---
        List<TaiSan> taiSans = taiSanDAO.getByMaPhong(maPhong, landlordUser);
        for (TaiSan ts : taiSans) {
            modelAssets.addRow(new Object[]{
                    ts.getTenTaiSan(),
                    ts.getSoLuong(),
                    ts.getTinhTrang()
            });
        }

        pnlCards.revalidate();
        pnlCards.repaint();
    }

    private JPanel createStatCard(String title, String value, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(30, 31, 38));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, accentColor),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(new Color(150, 150, 150));
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        
        JLabel lblValue = new JLabel(value);
        lblValue.setForeground(Color.WHITE);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 20));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }

    // Custom cell renderer to render HTML formatted list elements correctly with padding
    private static class AnnouncementCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            JLabel lbl = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            lbl.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
            lbl.setBackground(new Color(30, 31, 38));
            lbl.setForeground(Color.WHITE);
            return lbl;
        }
    }
}
