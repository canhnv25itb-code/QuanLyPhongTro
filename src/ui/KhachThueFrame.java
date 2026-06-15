package ui;

import dao.KhachThueDAO;
import model.KhachThue;
import model.TaiKhoan;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

/**
 * Màn hình chính dành cho KHÁCH THUÊ.
 * Thiết kế giao diện gradient sidebar và header hiện đại đồng bộ với MainFrame (Ảnh 2).
 */
public class KhachThueFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JLabel lblHeaderIcon;

    public KhachThueFrame(TaiKhoan taiKhoan) {
        setTitle("Hệ thống Phòng trọ Pro – KHÁCH THUÊ: " + taiKhoan.getTenDangNhap());
        setSize(1300, 850);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Tra cứu khách thuê theo TenDN ---
        KhachThueDAO ktDao = new KhachThueDAO();
        KhachThue kt = ktDao.findByTenDN(taiKhoan.getTenDangNhap());
        String maPhong = (kt != null) ? kt.getMaPhong() : null;

        // Layout tổng thể
        setLayout(new BorderLayout());

        // --- 1. SIDEBAR PANEL GRADIENT ---
        GradientSidebar sidebar = new GradientSidebar();
        sidebar.setPreferredSize(new Dimension(240, getHeight()));
        sidebar.setLayout(new BorderLayout());

        // Logo phần đầu Sidebar
        JPanel logoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 22));
        logoPanel.setOpaque(false);

        JLabel lblLogoIcon = new JLabel("🚀", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 60)); // Trắng mờ tròn làm logo background
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblLogoIcon.setPreferredSize(new Dimension(36, 36));
        lblLogoIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        lblLogoIcon.setForeground(Color.WHITE);

        JLabel lblLogoText = new JLabel("Cloudzone");
        lblLogoText.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblLogoText.setForeground(Color.WHITE);

        logoPanel.add(lblLogoIcon);
        logoPanel.add(lblLogoText);
        sidebar.add(logoPanel, BorderLayout.NORTH);

        // Menu danh sách các mục chọn
        JPanel menuContainer = new JPanel();
        menuContainer.setOpaque(false);
        menuContainer.setLayout(new BoxLayout(menuContainer, BoxLayout.Y_AXIS));
        menuContainer.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // Tải các icon gốc đồng bộ
        Icon imgPhongTro  = getScaledIcon("icons/phong_tro.png");
        Icon imgHopDong   = getScaledIcon("icons/hop_dong.png");
        Icon imgHoaDon    = getScaledIcon("icons/hoa_don.png");
        Icon imgDienNuoc  = getScaledIcon("icons/dien_nuoc.png");
        Icon imgDichVu    = getScaledIcon("icons/dich_vu.png");
        Icon imgSuCo      = getScaledIcon("icons/dich_vu.png");

        // Thiết kế icon Dashboard grid bằng đồ họa vector
        Icon iconDashboard = new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRect(x, y, 8, 8);
                g2.fillRect(x + 10, y, 8, 8);
                g2.fillRect(x, y + 10, 8, 8);
                g2.fillRect(x + 10, y + 10, 8, 8);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 18; }
            @Override public int getIconHeight() { return 18; }
        };

        // Thiết kế icon Dashboard grid màu hồng đỏ cho header
        Icon iconDashboardColor = new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(244, 67, 54));
                g2.fillRect(x, y, 8, 8);
                g2.fillRect(x + 10, y, 8, 8);
                g2.fillRect(x, y + 10, 8, 8);
                g2.fillRect(x + 10, y + 10, 8, 8);
                g2.dispose();
            }
            @Override public int getIconWidth() { return 18; }
            @Override public int getIconHeight() { return 18; }
        };

        SidebarItem btnDashboard = new SidebarItem("Trang chủ",      iconDashboard, false);
        SidebarItem btnPhongTro  = new SidebarItem("Phòng của tôi", imgPhongTro,  true);
        SidebarItem btnHopDong   = new SidebarItem("Hợp đồng thuê", imgHopDong,   true);
        SidebarItem btnHoaDon    = new SidebarItem("Hóa đơn",       imgHoaDon,    true);
        SidebarItem btnDienNuoc  = new SidebarItem("Điện nước",     imgDienNuoc,  true);
        SidebarItem btnDichVu    = new SidebarItem("Dịch vụ",       imgDichVu,    true);
        SidebarItem btnSuCo      = new SidebarItem("Báo sự cố",      imgSuCo,      true);

        java.util.List<SidebarItem> menuItems = new java.util.ArrayList<>();
        if (maPhong != null) {
            menuItems.add(btnDashboard);
            menuItems.add(btnPhongTro);
            menuItems.add(btnHopDong);
            menuItems.add(btnHoaDon);
            menuItems.add(btnDienNuoc);
            menuItems.add(btnDichVu);
            menuItems.add(btnSuCo);
        }

        for (SidebarItem item : menuItems) {
            menuContainer.add(item);
            menuContainer.add(Box.createVerticalStrut(4));
        }

        JScrollPane scrollMenu = new JScrollPane(menuContainer);
        scrollMenu.setOpaque(false);
        scrollMenu.getViewport().setOpaque(false);
        scrollMenu.setBorder(null);
        scrollMenu.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        sidebar.add(scrollMenu, BorderLayout.CENTER);

        add(sidebar, BorderLayout.WEST);

        // --- 2. WORKSPACE PANEL (HEADER TRẮNG + CONTENT CARDLAYOUT) ---
        JPanel workspacePanel = new JPanel(new BorderLayout());
        
        // Header Panel màu trắng thanh lịch
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(230, 230, 230)));
        headerPanel.setPreferredSize(new Dimension(1000, 60));

        // Phần bên trái của Header
        JPanel headerLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 11));
        headerLeft.setOpaque(false);

        lblHeaderIcon = new JLabel("", SwingConstants.CENTER) {
            private Icon currentIcon;
            @Override
            public void setIcon(Icon icon) {
                this.currentIcon = icon;
                repaint();
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // 1. Vẽ nền bo góc màu hồng nhạt
                g2.setColor(new Color(255, 235, 238));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                
                // 2. Vẽ viền hồng đỏ mờ
                g2.setColor(new Color(244, 67, 54, 100));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 8, 8);
                
                // 3. Vẽ Icon hiện tại ở giữa
                if (currentIcon != null) {
                    int ix = (getWidth() - currentIcon.getIconWidth()) / 2;
                    int iy = (getHeight() - currentIcon.getIconHeight()) / 2;
                    currentIcon.paintIcon(this, g2, ix, iy);
                }
                
                g2.dispose();
            }
        };
        lblHeaderIcon.setPreferredSize(new Dimension(38, 38));

        JLabel lblScreenTitle = new JLabel(maPhong != null ? "Trang chủ" : "Cảnh báo");
        lblScreenTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblScreenTitle.setForeground(new Color(60, 65, 80));

        headerLeft.add(lblHeaderIcon);
        headerLeft.add(lblScreenTitle);
        headerPanel.add(headerLeft, BorderLayout.WEST);

        // Phần bên phải của Header (Thông báo + Profile)
        JPanel headerRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 11));
        headerRight.setOpaque(false);

        // Chuông thông báo
        JPanel pnlNotif = new JPanel(null) {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(244, 67, 54)); // Chấm đỏ tròn
                g2.fillOval(18, 0, 15, 15);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 9));
                FontMetrics fm = g2.getFontMetrics();
                String text = "3";
                int tx = 18 + (15 - fm.stringWidth(text)) / 2;
                int ty = 0 + (15 - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, tx, ty);
                g2.dispose();
            }
        };
        pnlNotif.setOpaque(false);
        pnlNotif.setPreferredSize(new Dimension(38, 38));
        JLabel lblBell = new JLabel("🔔");
        lblBell.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
        lblBell.setBounds(2, 6, 24, 24);
        lblBell.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        pnlNotif.add(lblBell);

        // Avatar người dùng
        JLabel lblAvatar = new JLabel("👤", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(230, 235, 240));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblAvatar.setPreferredSize(new Dimension(38, 38));
        lblAvatar.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        lblAvatar.setForeground(new Color(120, 130, 140));
        lblAvatar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Tích hợp Menu Dropdown cho Avatar
        JPopupMenu userMenu = new JPopupMenu();
        String userName = taiKhoan.getTenDangNhap();
        String details = (kt != null)
                ? "Họ tên: " + kt.getHoTen() + " | Phòng: " + maPhong
                : "Chưa liên kết phòng";
        
        JMenuItem miProfile = new JMenuItem("Khách thuê: " + userName + " (" + details + ")");
        miProfile.setEnabled(false);
        JMenuItem miLogoutUser = new JMenuItem("Đăng xuất");
        miLogoutUser.addActionListener(e -> doLogout());
        JMenuItem miExitUser = new JMenuItem("Thoát hệ thống");
        miExitUser.addActionListener(e -> doExit());
        userMenu.add(miProfile);
        userMenu.addSeparator();
        userMenu.add(miLogoutUser);
        userMenu.add(miExitUser);

        lblAvatar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                userMenu.show(lblAvatar, 0, lblAvatar.getHeight());
            }
        });

        headerRight.add(pnlNotif);
        headerRight.add(lblAvatar);
        headerPanel.add(headerRight, BorderLayout.EAST);

        workspacePanel.add(headerPanel, BorderLayout.NORTH);

        // Khởi tạo các panel nội dung (CardLayout)
        if (maPhong != null) {
            cardLayout = new CardLayout();
            contentPanel = new JPanel(cardLayout);
            contentPanel.setBackground(new Color(245, 246, 250));

            JPanel dashboardPnl = new RenterDashboardPanel(maPhong);
            JPanel phongTroPnl  = new PhongTroPanel(maPhong);
            JPanel hopDongPnl   = new HopDongPanel(maPhong);
            JPanel hoaDonPnl    = new HoaDonPanel(maPhong);
            JPanel dienNuocPnl  = new DienNuocPanel(maPhong);
            JPanel dichVuPnl    = new DichVuPanel(maPhong);
            JPanel suCoPnl      = new RenterSuCoPanel(maPhong);

            contentPanel.add(dashboardPnl, "Trang chủ");
            contentPanel.add(phongTroPnl, "Phòng của tôi");
            contentPanel.add(hopDongPnl,  "Hợp đồng thuê");
            contentPanel.add(hoaDonPnl,   "Hóa đơn");
            contentPanel.add(dienNuocPnl, "Điện nước");
            contentPanel.add(dichVuPnl,   "Dịch vụ");
            contentPanel.add(suCoPnl,      "Báo sự cố");

            workspacePanel.add(contentPanel, BorderLayout.CENTER);

            // Cài đặt sự kiện click menu sidebar
            btnDashboard.addActionListener(e -> {
                setActiveItem(menuItems, btnDashboard);
                lblScreenTitle.setText("Trang chủ");
                lblHeaderIcon.setIcon(iconDashboardColor);
                cardLayout.show(contentPanel, "Trang chủ");
            });
            btnPhongTro.addActionListener(e -> {
                setActiveItem(menuItems, btnPhongTro);
                lblScreenTitle.setText("Phòng của tôi");
                lblHeaderIcon.setIcon(imgPhongTro);
                cardLayout.show(contentPanel, "Phòng của tôi");
            });
            btnHopDong.addActionListener(e -> {
                setActiveItem(menuItems, btnHopDong);
                lblScreenTitle.setText("Hợp đồng thuê");
                lblHeaderIcon.setIcon(imgHopDong);
                cardLayout.show(contentPanel, "Hợp đồng thuê");
            });
            btnHoaDon.addActionListener(e -> {
                setActiveItem(menuItems, btnHoaDon);
                lblScreenTitle.setText("Hóa đơn");
                lblHeaderIcon.setIcon(imgHoaDon);
                cardLayout.show(contentPanel, "Hóa đơn");
            });
            btnDienNuoc.addActionListener(e -> {
                setActiveItem(menuItems, btnDienNuoc);
                lblScreenTitle.setText("Điện nước");
                lblHeaderIcon.setIcon(imgDienNuoc);
                cardLayout.show(contentPanel, "Điện nước");
            });
            btnDichVu.addActionListener(e -> {
                setActiveItem(menuItems, btnDichVu);
                lblScreenTitle.setText("Dịch vụ");
                lblHeaderIcon.setIcon(imgDichVu);
                cardLayout.show(contentPanel, "Dịch vụ");
            });
            btnSuCo.addActionListener(e -> {
                setActiveItem(menuItems, btnSuCo);
                lblScreenTitle.setText("Báo sự cố");
                lblHeaderIcon.setIcon(imgSuCo);
                cardLayout.show(contentPanel, "Báo sự cố");
            });

            // Mặc định chọn Trang chủ
            setActiveItem(menuItems, btnDashboard);
            lblHeaderIcon.setIcon(iconDashboardColor);
            lblScreenTitle.setText("Trang chủ");
            cardLayout.show(contentPanel, "Trang chủ");
        } else {
            // Mặc định hiển thị icon cảnh báo ⚠️
            Icon warnIcon = new Icon() {
                @Override
                public void paintIcon(Component c, Graphics g, int x, int y) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(244, 67, 54));
                    g2.setFont(new Font("Segoe UI Emoji", Font.BOLD, 18));
                    FontMetrics fm = g2.getFontMetrics();
                    String warnStr = "⚠️";
                    int tx = x + (18 - fm.stringWidth(warnStr)) / 2;
                    int ty = y + (18 - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(warnStr, tx, ty);
                    g2.dispose();
                }
                @Override public int getIconWidth() { return 18; }
                @Override public int getIconHeight() { return 18; }
            };
            lblHeaderIcon.setIcon(warnIcon);

            // Hiển thị panel cảnh báo thiết kế mới
            JPanel warn = new JPanel(new GridBagLayout());
            warn.setBackground(new Color(245, 246, 250));
            JLabel lbl = new JLabel(
                "<html><center>"
                + "<font size='6' color='#e74c3c'>⚠️</font><br><br>"
                + "<b style='font-size:16px;color:#333;'>Tài khoản chưa được liên kết với phòng nào!</b><br>"
                + "<span style='color:#7f8c8d;font-size:13px;'>"
                + "Tên đăng nhập <b>" + taiKhoan.getTenDangNhap() + "</b> chưa được Chủ trọ liên kết.<br>"
                + "Vui lòng liên hệ Chủ trọ để gán phòng cho tài khoản của bạn."
                + "</span></center></html>", SwingConstants.CENTER);
            warn.add(lbl);
            workspacePanel.add(warn, BorderLayout.CENTER);
        }

        add(workspacePanel, BorderLayout.CENTER);
    }

    private void setActiveItem(java.util.List<SidebarItem> items, SidebarItem activeItem) {
        for (SidebarItem item : items) {
            item.setActive(item == activeItem);
        }
    }

    private ImageIcon getScaledIcon(String path) {
        try {
            ImageIcon icon = null;
            java.io.File file = new java.io.File(path);
            if (!file.exists()) {
                file = new java.io.File("src/" + path);
            }
            if (file.exists()) {
                icon = new ImageIcon(file.getAbsolutePath());
            } else {
                java.net.URL url = getClass().getResource("/" + path);
                if (url != null) {
                    icon = new ImageIcon(url);
                }
            }
            if (icon != null && icon.getImage() != null) {
                Image img = icon.getImage();
                // Tải hình ảnh đồng bộ bằng MediaTracker
                MediaTracker tracker = new MediaTracker(this);
                tracker.addImage(img, 0);
                tracker.waitForID(0);

                // Vẽ co giãn đồng bộ lên BufferedImage kích thước 20x20
                BufferedImage scaled = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = scaled.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.drawImage(img, 0, 0, 20, 20, null);
                g2.dispose();
                return new ImageIcon(scaled);
            }
        } catch (Exception e) {
            System.err.println("Lỗi tải icon " + path + ": " + e.getMessage());
        }
        return null;
    }

    private void doLogout() {
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc chắn muốn đăng xuất?", "Xác nhận", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            util.Session.logout();
            new LoginForm().setVisible(true);
            this.dispose();
        }
    }

    private void doExit() {
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc chắn muốn thoát hệ thống?", "Xác nhận", 
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    // --- SIDEBAR TÙY CHỈNH GRADIENT (ẢNH 2) ---
    private static class GradientSidebar extends JPanel {
        public GradientSidebar() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Vẽ gradient chuyển sắc dọc từ xanh tím sang hồng đỏ nhạt
            GradientPaint gp = new GradientPaint(
                0, 0, new Color(74, 96, 214), 
                0, getHeight(), new Color(220, 115, 125)
            );
            g2.setPaint(gp);
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // --- SIDEBAR ITEM TÙY CHỈNH (ẢNH 2) ---
    private static class SidebarItem extends JButton {
        private final String title;
        private final boolean showChevron;
        private boolean active = false;

        public SidebarItem(String title, Icon icon, boolean showChevron) {
            this.title = title;
            this.showChevron = showChevron;
            setIcon(icon);

            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            setHorizontalAlignment(SwingConstants.LEFT);
            setVerticalAlignment(SwingConstants.CENTER);
            
            // Lề trong
            setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));
            setMaximumSize(new Dimension(320, 46));
            setPreferredSize(new Dimension(220, 46));
        }

        public void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            // Hiệu ứng hover và active bo tròn mờ sang trọng
            if (active) {
                g2.setColor(new Color(255, 255, 255, 45)); 
                g2.fillRoundRect(8, 2, w - 16, h - 4, 10, 10);
            } else if (getModel().isRollover()) {
                g2.setColor(new Color(255, 255, 255, 20)); 
                g2.fillRoundRect(8, 2, w - 16, h - 4, 10, 10);
            }

            // Vẽ Icon (sử dụng icon gốc đầy màu sắc sinh động)
            int iconX = 22;
            int iconY = (h - 20) / 2;
            Icon icon = getIcon();
            if (icon != null) {
                icon.paintIcon(this, g2, iconX, iconY);
            }

            // Vẽ nhãn văn bản chữ trắng
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 14));
            FontMetrics fm = g2.getFontMetrics();
            int textX = icon != null ? 52 : 22;
            int textY = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(title, textX, textY);

            // Vẽ chevron '>' mờ sang trọng bên phải
            if (showChevron) {
                g2.setColor(new Color(255, 255, 255, 140));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
                String chevron = "›"; 
                FontMetrics fmChevron = g2.getFontMetrics();
                int chevX = w - 24 - fmChevron.stringWidth(chevron);
                int chevY = (h - fmChevron.getHeight()) / 2 + fmChevron.getAscent();
                g2.drawString(chevron, chevX, chevY);
            }

            g2.dispose();
        }
    }
}