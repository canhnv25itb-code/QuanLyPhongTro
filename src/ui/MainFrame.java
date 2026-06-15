package ui;

import model.TaiKhoan;
import util.DataRefreshEvent;
import util.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

public class MainFrame extends JFrame {
    private TaiKhoan user;
    private JLabel lblStatus;
    private JLabel lblHeaderIcon;

    // --- PANEL THÀNH VIÊN ---
    private DashboardPanel dashboardPnl;
    private ThongKePanel   thongKePnl;
    private PhongTroPanel  phongTroPnl;
    private KhachThuePanel khachThuePnl;
    private HopDongPanel   hopDongPnl;
    private DienNuocPanel  dienNuocPnl;
    private HoaDonPanel    hoaDonPnl;
    private DichVuPanel    dichVuPnl;
    private SuCoPanel      suCoPnl;
    private TaiSanPanel    taiSanPnl;
    private ThongBaoPanel  thongBaoPnl;

    // --- DI CHUYỂN TABS CHỨC NĂNG SANG CARDLAYOUT ---
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private JPanel currentPanel;

    public MainFrame(TaiKhoan user) {
        this.user = user;
        
        try {
            UIUtils.setupLookAndFeel();
        } catch (Exception e) {
            try { 
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); 
            } catch (Exception ex) {}
        }
        
        initComponents();
    }

    // --- TẠO CÁC HÀM GETTER ĐỂ CÁC BÊN KHÁC GỌI ĐỒNG BỘ ---
    public ThongKePanel getThongKePanel() {
        return thongKePnl;
    }

    public HoaDonPanel getHoaDonPanel() {
        return hoaDonPnl;
    }

    private void initComponents() {
        setTitle("Hệ thống Quản lý Nhà trọ Pro - Dashboard");
        setSize(1300, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // --- MENU BAR ---
        JMenuBar menuBar = new JMenuBar();
        JMenu mSystem = new JMenu("Hệ thống");
        JMenuItem miLogout = new JMenuItem("Đăng xuất");
        miLogout.addActionListener(e -> doLogout());
        mSystem.add(miLogout);
        JMenuItem miExit = new JMenuItem("Thoát ứng dụng");
        miExit.addActionListener(e -> doExit());
        mSystem.add(miExit);
        menuBar.add(mSystem);
        setJMenuBar(menuBar);

        // --- KHỞI TẠO CÁC PANEL ---
        dashboardPnl = new DashboardPanel();
        thongKePnl   = new ThongKePanel();
        phongTroPnl  = new PhongTroPanel();
        khachThuePnl = new KhachThuePanel();
        hopDongPnl   = new HopDongPanel();
        dienNuocPnl  = new DienNuocPanel();
        hoaDonPnl    = new HoaDonPanel();
        dichVuPnl    = new DichVuPanel();
        suCoPnl      = new SuCoPanel();
        taiSanPnl    = new TaiSanPanel();
        thongBaoPnl  = new ThongBaoPanel();

        // Layout tổng thể của MainFrame
        setLayout(new BorderLayout());

        // --- 1. SIDEBAR PANEL GRADIENT (ẢNH 2) ---
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
        Icon imgThongKe   = getScaledIcon("icons/thong_ke.png");
        Icon imgPhongTro  = getScaledIcon("icons/phong_tro.png");
        Icon imgKhachThue = getScaledIcon("icons/khach_thue.png");
        Icon imgHopDong   = getScaledIcon("icons/hop_dong.png");
        Icon imgDienNuoc  = getScaledIcon("icons/dien_nuoc.png");
        Icon imgHoaDon    = getScaledIcon("icons/hoa_don.png");
        Icon imgDichVu    = getScaledIcon("icons/dich_vu.png");

        // Thiết kế icon Dashboard grid bằng đồ họa vector (màu trắng cho sidebar)
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

        Icon imgThongBao  = getScaledIcon("icons/thong_ke.png");
        Icon imgTaiSan    = getScaledIcon("icons/phong_tro.png");
        Icon imgSuCo      = getScaledIcon("icons/dich_vu.png");

        SidebarItem btnDashboard = new SidebarItem("Dashboard", iconDashboard, false);
        SidebarItem btnThongKe   = new SidebarItem("Thống kê",   imgThongKe,   true);
        SidebarItem btnPhongTro  = new SidebarItem("Phòng trọ",  imgPhongTro,  true);
        SidebarItem btnKhachThue = new SidebarItem("Khách thuê", imgKhachThue, true);
        SidebarItem btnHopDong   = new SidebarItem("Hợp đồng",   imgHopDong,   true);
        SidebarItem btnDienNuoc  = new SidebarItem("Điện nước",  imgDienNuoc,  true);
        SidebarItem btnHoaDon    = new SidebarItem("Hóa đơn",    imgHoaDon,    true);
        SidebarItem btnDichVu    = new SidebarItem("Dịch vụ",    imgDichVu,    true);
        SidebarItem btnSuCo      = new SidebarItem("Sự cố",      imgSuCo,      true);
        SidebarItem btnTaiSan    = new SidebarItem("Tài sản",    imgTaiSan,    true);
        SidebarItem btnThongBao  = new SidebarItem("Thông báo",  imgThongBao,  true);

        java.util.List<SidebarItem> menuItems = new java.util.ArrayList<>();
        menuItems.add(btnDashboard);
        menuItems.add(btnThongKe);
        menuItems.add(btnPhongTro);
        menuItems.add(btnKhachThue);
        menuItems.add(btnHopDong);
        menuItems.add(btnDienNuoc);
        menuItems.add(btnHoaDon);
        menuItems.add(btnDichVu);
        menuItems.add(btnSuCo);
        menuItems.add(btnTaiSan);
        menuItems.add(btnThongBao);

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

        JLabel lblScreenTitle = new JLabel("Dashboard");
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
        String userName = (user != null) ? user.getTenDangNhap() : "Admin_Root";
        String role     = (user != null) ? user.getVaiTro()      : "Quản trị viên";
        JMenuItem miProfile = new JMenuItem("Chủ trọ: " + userName + " (" + role + ")");
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

        // CardLayout Panel chính chứa các panel con
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(245, 246, 250));

        contentPanel.add(dashboardPnl, "Dashboard");
        contentPanel.add(thongKePnl,   "Thống kê");
        contentPanel.add(phongTroPnl,  "Phòng trọ");
        contentPanel.add(khachThuePnl, "Khách thuê");
        contentPanel.add(hopDongPnl,   "Hợp đồng");
        contentPanel.add(dienNuocPnl,  "Điện nước");
        contentPanel.add(hoaDonPnl,    "Hóa đơn");
        contentPanel.add(dichVuPnl,    "Dịch vụ");
        contentPanel.add(suCoPnl,      "Sự cố");
        contentPanel.add(taiSanPnl,    "Tài sản");
        contentPanel.add(thongBaoPnl,  "Thông báo");

        workspacePanel.add(contentPanel, BorderLayout.CENTER);
        add(workspacePanel, BorderLayout.CENTER);

        // --- ĐĂNG KÝ LISTENER SỰ KIỆN VÀO EVENT BUS (Lazy loading) ---
        DataRefreshEvent bus = DataRefreshEvent.get();
        bus.addListener(() -> loadTabAsync(dashboardPnl, () -> dashboardPnl.refreshData()));
        bus.addListener(() -> loadTabAsync(thongKePnl,   () -> thongKePnl.loadData()));
        bus.addListener(() -> loadTabAsync(phongTroPnl,  () -> phongTroPnl.loadData()));
        bus.addListener(() -> loadTabAsync(khachThuePnl, () -> khachThuePnl.loadData()));
        bus.addListener(() -> loadTabAsync(hopDongPnl,   () -> hopDongPnl.loadData()));
        bus.addListener(() -> loadTabAsync(dienNuocPnl,  () -> dienNuocPnl.loadData()));
        bus.addListener(() -> loadTabAsync(hoaDonPnl,    () -> hoaDonPnl.loadData()));
        bus.addListener(() -> loadTabAsync(dichVuPnl,    () -> dichVuPnl.loadData()));
        bus.addListener(() -> loadTabAsync(suCoPnl,      () -> suCoPnl.loadData()));
        bus.addListener(() -> loadTabAsync(taiSanPnl,    () -> taiSanPnl.loadData()));
        bus.addListener(() -> loadTabAsync(thongBaoPnl,  () -> thongBaoPnl.loadData()));

        // --- CẤU HÌNH SỰ KIỆN CLICK SIDEBAR MENU ---
        btnDashboard.addActionListener(e -> {
            setActiveItem(menuItems, btnDashboard);
            lblScreenTitle.setText("Dashboard");
            lblHeaderIcon.setIcon(iconDashboardColor);
            switchTab("Dashboard", dashboardPnl, () -> dashboardPnl.refreshData());
        });
        btnThongKe.addActionListener(e -> {
            setActiveItem(menuItems, btnThongKe);
            lblScreenTitle.setText("Thống kê");
            lblHeaderIcon.setIcon(imgThongKe);
            switchTab("Thống kê", thongKePnl, () -> thongKePnl.loadData());
        });
        btnPhongTro.addActionListener(e -> {
            setActiveItem(menuItems, btnPhongTro);
            lblScreenTitle.setText("Phòng trọ");
            lblHeaderIcon.setIcon(imgPhongTro);
            switchTab("Phòng trọ", phongTroPnl, () -> phongTroPnl.loadData());
        });
        btnKhachThue.addActionListener(e -> {
            setActiveItem(menuItems, btnKhachThue);
            lblScreenTitle.setText("Khách thuê");
            lblHeaderIcon.setIcon(imgKhachThue);
            switchTab("Khách thuê", khachThuePnl, () -> khachThuePnl.loadData());
        });
        btnHopDong.addActionListener(e -> {
            setActiveItem(menuItems, btnHopDong);
            lblScreenTitle.setText("Hợp đồng");
            lblHeaderIcon.setIcon(imgHopDong);
            switchTab("Hợp đồng", hopDongPnl, () -> hopDongPnl.loadData());
        });
        btnDienNuoc.addActionListener(e -> {
            setActiveItem(menuItems, btnDienNuoc);
            lblScreenTitle.setText("Điện nước");
            lblHeaderIcon.setIcon(imgDienNuoc);
            switchTab("Điện nước", dienNuocPnl, () -> dienNuocPnl.loadData());
        });
        btnHoaDon.addActionListener(e -> {
            setActiveItem(menuItems, btnHoaDon);
            lblScreenTitle.setText("Hóa đơn");
            lblHeaderIcon.setIcon(imgHoaDon);
            switchTab("Hóa đơn", hoaDonPnl, () -> hoaDonPnl.loadData());
        });
        btnDichVu.addActionListener(e -> {
            setActiveItem(menuItems, btnDichVu);
            lblScreenTitle.setText("Dịch vụ");
            lblHeaderIcon.setIcon(imgDichVu);
            switchTab("Dịch vụ", dichVuPnl, () -> dichVuPnl.loadData());
        });
        btnSuCo.addActionListener(e -> {
            setActiveItem(menuItems, btnSuCo);
            lblScreenTitle.setText("Sự cố");
            lblHeaderIcon.setIcon(imgSuCo);
            switchTab("Sự cố", suCoPnl, () -> suCoPnl.loadData());
        });
        btnTaiSan.addActionListener(e -> {
            setActiveItem(menuItems, btnTaiSan);
            lblScreenTitle.setText("Tài sản");
            lblHeaderIcon.setIcon(imgTaiSan);
            switchTab("Tài sản", taiSanPnl, () -> taiSanPnl.loadData());
        });
        btnThongBao.addActionListener(e -> {
            setActiveItem(menuItems, btnThongBao);
            lblScreenTitle.setText("Thông báo");
            lblHeaderIcon.setIcon(imgThongBao);
            switchTab("Thông báo", thongBaoPnl, () -> thongBaoPnl.loadData());
        });

        // Thiết lập trạng thái mặc định ban đầu là hiển thị Dashboard
        setActiveItem(menuItems, btnDashboard);
        lblHeaderIcon.setIcon(iconDashboardColor);
        currentPanel = dashboardPnl;
        cardLayout.show(contentPanel, "Dashboard");

        // --- THANH TRẠNG THÁI ---
        setupStatusBar();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                doExit();
            }
        });
    }

    private void setActiveItem(java.util.List<SidebarItem> items, SidebarItem activeItem) {
        for (SidebarItem item : items) {
            item.setActive(item == activeItem);
        }
    }

    private void switchTab(String title, JPanel panel, Runnable loadAction) {
        currentPanel = panel;
        cardLayout.show(contentPanel, title);
        new SwingWorker<Void, Void>() {
            protected Void doInBackground() {
                return null;
            }
            protected void done() {
                loadAction.run();
            }
        }.execute();
    }

    private void loadTabAsync(JPanel panel, Runnable loadAction) {
        if (currentPanel == panel) {
            loadAction.run();
        }
    }

    private void setupStatusBar() {
        String userName = (user != null) ? user.getTenDangNhap() : "Admin_Root";
        String role = (user != null) ? user.getVaiTro() : "Quản trị viên";
        
        lblStatus = new JLabel(" 🟢 Đang hoạt động: " + userName + " | Quyền hạn: " + role);
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        
        JPanel statusPnl = new JPanel(new BorderLayout());
        statusPnl.setPreferredSize(new Dimension(this.getWidth(), 28));
        statusPnl.setBackground(new Color(40, 42, 50));
        statusPnl.setOpaque(true);
        lblStatus.setForeground(new Color(180, 180, 190));
        statusPnl.add(lblStatus, BorderLayout.WEST);
        
        add(statusPnl, BorderLayout.SOUTH);
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