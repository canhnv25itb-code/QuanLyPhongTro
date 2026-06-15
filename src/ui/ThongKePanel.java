package ui;

import dao.ThongKeDAO;
import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class ThongKePanel extends JPanel {
    // 1. Khai báo các thành phần giao diện
    private JLabel lblDoanhThu, lblKhachThue, lblPhongTrong, lblHopDong;
    private DefaultCategoryDataset dataset;
    private ThongKeDAO thongKeDAO = new ThongKeDAO();
    private DecimalFormat formatter = new DecimalFormat("###,###,###");

    public ThongKePanel() {
        // Gọi hàm khởi tạo giao diện
        initUI();
        // Nạp dữ liệu lần đầu
        loadData();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        setBackground(new Color(18, 19, 24)); // Màu tối cho chuyên nghiệp

        // --- 1. Phần tổng quan dạng card ---
        JPanel overviewPanel = new JPanel(new GridLayout(1, 4, 15, 15));
        overviewPanel.setOpaque(false); // Để lộ nền tối của Panel chính
        overviewPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        lblDoanhThu = createCard("Tổng doanh thu", "0 đ", new Color(52, 152, 219));
        lblKhachThue = createCard("Khách thuê", "0", new Color(46, 204, 113));
        lblPhongTrong = createCard("Phòng trống", "0", new Color(241, 196, 15));
        lblHopDong = createCard("Hợp đồng HĐ", "0", new Color(231, 76, 60));

        overviewPanel.add(lblDoanhThu);
        overviewPanel.add(lblKhachThue);
        overviewPanel.add(lblPhongTrong);
        overviewPanel.add(lblHopDong);

        add(overviewPanel, BorderLayout.NORTH);

        // --- 2. Biểu đồ doanh thu theo tháng ---
        dataset = new DefaultCategoryDataset();
        int currentYear = LocalDate.now().getYear();
        JFreeChart chart = ChartFactory.createBarChart(
                "BIỂU ĐỒ DOANH THU NĂM " + currentYear,
                "Tháng",
                "Số tiền (VNĐ)",
                dataset
        );
        
        // Tùy chỉnh màu sắc biểu đồ cho đẹp
        chart.setBackgroundPaint(Color.WHITE);
        
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 15, 15));
        chartPanel.setOpaque(false);
        add(chartPanel, BorderLayout.CENTER);
    }

    private JLabel createCard(String title, String value, Color color) {
        JLabel card = new JLabel("<html><div style='text-align: center; font-family: Segoe UI;'>" 
                + "<p style='font-size: 12px;'>" + title + "</p>" 
                + "<p style='font-size: 18px; font-weight: bold;'>" + value + "</p></div></html>");
        card.setOpaque(true);
        card.setBackground(color);
        card.setForeground(Color.WHITE);
        card.setHorizontalAlignment(SwingConstants.CENTER);
        card.setBorder(BorderFactory.createLineBorder(color.darker(), 1));
        return card;
    }

    public void loadData() {
        try {
            // Lấy dữ liệu từ DAO (Nhớ kiểm tra SQL lọc 'Đã thu' trong DAO nhé)
            double doanhThu = thongKeDAO.getTongDoanhThu();
            int soKhach = thongKeDAO.getSoKhachThue();
            int phongTrong = thongKeDAO.getSoPhongTrong();
            int hopDong = thongKeDAO.getSoHopDongDangHoatDong();

            // Cập nhật 4 thẻ thông tin
            lblDoanhThu.setText("<html><div style='text-align: center;'><h3>Tổng doanh thu</h3><h2>" + formatter.format(doanhThu) + " đ</h2></div></html>");
            lblKhachThue.setText("<html><div style='text-align: center;'><h3>Khách thuê</h3><h2>" + soKhach + "</h2></div></html>");
            lblPhongTrong.setText("<html><div style='text-align: center;'><h3>Phòng trống</h3><h2>" + phongTrong + "</h2></div></html>");
            lblHopDong.setText("<html><div style='text-align: center;'><h3>Hợp đồng HĐ</h3><h2>" + hopDong + "</h2></div></html>");

            // Cập nhật Biểu đồ
            int currentYear = LocalDate.now().getYear();
            List<Double> listDoanhThu = thongKeDAO.getDoanhThuTheoThang(currentYear);
            
            dataset.clear(); 
            if (listDoanhThu != null) {
                for (int i = 0; i < listDoanhThu.size(); i++) {
                    dataset.addValue(listDoanhThu.get(i), "Doanh thu", "Tháng " + (i + 1));
                }
            }
            
            this.revalidate();
            this.repaint();
        } catch (Exception e) {
            System.err.println("Lỗi load dữ liệu thống kê: " + e.getMessage());
        }
    }
}