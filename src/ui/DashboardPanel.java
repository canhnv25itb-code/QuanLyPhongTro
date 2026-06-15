package ui;

import java.awt.*;
import javax.swing.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import dao.HoaDonDAO;
import dao.PhongTroDAO;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

public class DashboardPanel extends JPanel {
    private final PhongTroDAO phongDao = new PhongTroDAO();
    private final HoaDonDAO hoadonDao = new HoaDonDAO();

    // Khai báo các Panel chứa để dễ dàng xóa và vẽ lại khi Refresh
    private JPanel pnlCards;
    private JPanel pnlCharts;
    private JPanel pnlAlerts;

    public DashboardPanel() {
        setBackground(new Color(18, 19, 24)); 
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        pnlCards = new JPanel(new GridLayout(1, 4, 20, 0));
        pnlCards.setOpaque(false);
        add(pnlCards, BorderLayout.NORTH);

        pnlCharts = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlCharts.setOpaque(false);
        add(pnlCharts, BorderLayout.CENTER);

        // Khởi tạo Panel cảnh báo tự động ở dưới cùng
        pnlAlerts = new JPanel(new GridLayout(1, 2, 20, 0));
        pnlAlerts.setOpaque(false);
        pnlAlerts.setPreferredSize(new Dimension(800, 140));
        add(pnlAlerts, BorderLayout.SOUTH);

        // Gọi lần đầu tiên khi mở app
        refreshData();
    }

    // --- CẬP NHẬT REAL-TIME ---
    public void refreshData() {
        pnlCards.removeAll();
        pnlCharts.removeAll();
        pnlAlerts.removeAll();

        // 1. Lấy dữ liệu mới nhất từ DB
        double doanhThu = hoadonDao.getDoanhThuThangNay(); 
        int daThue = phongDao.countByStatus("Đang thuê"); 
        int tongPhong = phongDao.countAll();
        String trangThai = (tongPhong > 0 && daThue == tongPhong) ? "Full phòng" : "Ổn định";

        // 2. Vẽ lại 4 thẻ thống kê
        pnlCards.add(createStatCard("TỔNG SỐ PHÒNG", String.valueOf(tongPhong), new Color(155, 89, 182)));
        pnlCards.add(createStatCard("PHÒNG ĐÃ THUÊ", String.valueOf(daThue), new Color(46, 204, 113)));
        pnlCards.add(createStatCard("DOANH THU THÁNG", String.format("%,.0f đ", doanhThu), new Color(241, 196, 15)));
        pnlCards.add(createStatCard("TRẠNG THÁI", trangThai, new Color(52, 152, 219)));

        // 3. Vẽ lại 2 biểu đồ
        pnlCharts.add(createPieChartPanel(daThue, tongPhong - daThue));
        pnlCharts.add(createBarChartPanel());

        // 4. Quét và tải các cảnh báo tự động
        loadAlerts();

        // Cập nhật lại giao diện
        revalidate();
        repaint();
    }

    private void loadAlerts() {
        String username = util.Session.getTenDN();
        if (username == null || username.isEmpty()) {
            username = "admin"; // Fallback mẫu
        }

        // 4.1 Quét hợp đồng sắp hết hạn (30 ngày)
        java.util.List<String> listExpiring = new java.util.ArrayList<>();
        String sqlExp = "SELECT MaPhong, TenKhachHang, NgayKetThuc FROM HopDong " +
                        "WHERE TenDN = ? AND NgayKetThuc >= CAST(GETDATE() AS DATE) " +
                        "AND NgayKetThuc <= DATEADD(day, 30, CAST(GETDATE() AS DATE))";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlExp)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String p = rs.getString("MaPhong");
                    String k = rs.getString("TenKhachHang");
                    java.sql.Date d = rs.getDate("NgayKetThuc");
                    listExpiring.add("Phòng " + p + " (" + k + ") hết hạn vào " + d.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 4.2 Quét hóa đơn quá hạn chưa đóng
        java.util.List<String> listOverdue = new java.util.ArrayList<>();
        String sqlOve = "SELECT TenPhong, NgayLap FROM HoaDon " +
                        "WHERE TenDN = ? AND TrangThai = N'Chưa thu' " +
                        "AND NgayLap < DATEADD(month, DATEDIFF(month, 0, GETDATE()), 0)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlOve)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String p = rs.getString("TenPhong");
                    java.sql.Date d = rs.getDate("NgayLap");
                    java.time.LocalDate ld = d.toLocalDate();
                    listOverdue.add("Phòng " + p + " chưa đóng hóa đơn tháng " + ld.getMonthValue() + "/" + ld.getYear());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Tạo Panel cảnh báo hợp đồng
        JPanel pnlExp = new JPanel(new BorderLayout(5, 5));
        pnlExp.setBackground(new Color(30, 31, 38));
        pnlExp.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(230, 126, 34)), // Orange accent
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        JLabel lblTitleExp = new JLabel("⚠️ HỢP ĐỒNG SẮP HẾT HẠN (30 NGÀY)");
        lblTitleExp.setForeground(new Color(230, 126, 34));
        lblTitleExp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlExp.add(lblTitleExp, BorderLayout.NORTH);
        
        DefaultListModel<String> modelExp = new DefaultListModel<>();
        for (String s : listExpiring) modelExp.addElement(s);
        if (listExpiring.isEmpty()) modelExp.addElement("Không có hợp đồng nào sắp hết hạn.");
        JList<String> lstExp = new JList<>(modelExp);
        lstExp.setBackground(new Color(30, 31, 38));
        lstExp.setForeground(Color.WHITE);
        lstExp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scrExp = new JScrollPane(lstExp);
        scrExp.setBorder(null);
        pnlExp.add(scrExp, BorderLayout.CENTER);

        // Tạo Panel cảnh báo nợ đọng
        JPanel pnlOve = new JPanel(new BorderLayout(5, 5));
        pnlOve.setBackground(new Color(30, 31, 38));
        pnlOve.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 5, 0, 0, new Color(231, 76, 60)), // Red accent
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));
        JLabel lblTitleOve = new JLabel("🚨 CẢNH BÁO NỢ ĐỌNG HÓA ĐƠN");
        lblTitleOve.setForeground(new Color(231, 76, 60));
        lblTitleOve.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlOve.add(lblTitleOve, BorderLayout.NORTH);
        
        DefaultListModel<String> modelOve = new DefaultListModel<>();
        for (String s : listOverdue) modelOve.addElement(s);
        if (listOverdue.isEmpty()) modelOve.addElement("Không có hóa đơn quá hạn chưa đóng.");
        JList<String> lstOve = new JList<>(modelOve);
        lstOve.setBackground(new Color(30, 31, 38));
        lstOve.setForeground(Color.WHITE);
        lstOve.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane scrOve = new JScrollPane(lstOve);
        scrOve.setBorder(null);
        pnlOve.add(scrOve, BorderLayout.CENTER);

        pnlAlerts.add(pnlExp);
        pnlAlerts.add(pnlOve);
    }

    private JPanel createPieChartPanel(int daThue, int trong) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("Đã thuê", daThue);
        dataset.setValue("Trống", trong);

        // Bật Tooltips (true ở tham số thứ 3) để rê chuột vào biểu đồ hiện số
        JFreeChart chart = ChartFactory.createPieChart("TÌNH TRẠNG PHÒNG", dataset, true, true, false);
        styleChart(chart);
        
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setSectionPaint("Đã thuê", new Color(231, 76, 60));
        plot.setSectionPaint("Trống", new Color(46, 204, 113));
        
        return new ChartPanel(chart);
    }

    private JPanel createBarChartPanel() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Double> data = hoadonDao.getDoanhThu3Thang();
        
        if (data != null && !data.isEmpty()) {
            java.util.List<String> keys = new java.util.ArrayList<>(data.keySet());
            java.util.Collections.reverse(keys); 
            
            for (String key : keys) {
                dataset.addValue(data.get(key), "Doanh thu", key);
            }
        } else {
            dataset.addValue(0, "Doanh thu", "Tháng này");
        }

        // Bật Tooltips (true ở tham số thứ 4)
        JFreeChart chart = ChartFactory.createBarChart("DOANH THU 3 THÁNG GẦN NHẤT", "Tháng", "Số tiền (VNĐ)", dataset, org.jfree.chart.plot.PlotOrientation.VERTICAL, false, true, false);
        styleChart(chart);
        
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.getRenderer().setSeriesPaint(0, new Color(52, 152, 219));
        
        return new ChartPanel(chart);
    }

    private void styleChart(JFreeChart chart) {
        chart.setBackgroundPaint(new Color(18, 19, 24));
        chart.getTitle().setPaint(Color.WHITE);
        chart.getPlot().setBackgroundPaint(new Color(30, 31, 38));
        chart.getPlot().setOutlineVisible(false);
        if (chart.getLegend() != null) {
            chart.getLegend().setBackgroundPaint(new Color(30, 31, 38));
            chart.getLegend().setItemPaint(Color.WHITE);
        }
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
        JLabel lblValue = new JLabel(value);
        lblValue.setForeground(Color.WHITE);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 22));
        
        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        return card;
    }
}