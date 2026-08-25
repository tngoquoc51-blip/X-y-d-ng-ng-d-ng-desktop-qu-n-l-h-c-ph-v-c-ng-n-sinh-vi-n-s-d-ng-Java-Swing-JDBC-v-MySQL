package vn.edu.eaut.qlhocphi.gui;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.admin.BackupRestorePanel;
import vn.edu.eaut.qlhocphi.gui.admin.TaiKhoanPanel;
import vn.edu.eaut.qlhocphi.gui.dashboard.DashboardPanel;
import vn.edu.eaut.qlhocphi.gui.chatbot.ChatbotPanel;
import vn.edu.eaut.qlhocphi.gui.congno.CongNoPanel;
import vn.edu.eaut.qlhocphi.gui.hocky.HocKyPanel;
import vn.edu.eaut.qlhocphi.gui.hocphi.HoaDonPanel;
import vn.edu.eaut.qlhocphi.gui.sinhvien.HoaDonHocPhiSinhVienPanel;
import vn.edu.eaut.qlhocphi.gui.sinhvien.LichSuThanhToanPanel;
import vn.edu.eaut.qlhocphi.gui.sinhvien.SinhVienCongNoPanel;
import vn.edu.eaut.qlhocphi.gui.sinhvien.SinhVienPanel;
import vn.edu.eaut.qlhocphi.gui.thanhtoan.PhieuThuPanel;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.VaiTro;
import vn.edu.eaut.qlhocphi.gui.sinhvien.DoiMatKhauDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MainFrame extends JFrame {
    private static final Color MAU_THANH_NGUOI_DUNG = new Color(0x12, 0x18, 0x26);

    private final TaiKhoan taiKhoan;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final Map<String, JButton> menuButtons = new LinkedHashMap<>();

    /** Tham chieu toi cac panel can duoc "dieu khien tu xa" (goi timKiem() tu panel khac). */
    private SinhVienPanel sinhVienPanel;
    private CongNoPanel congNoPanel;

    /** Ham dieu huong dung chung: chuyen tab (key) VA tim kiem san (tuKhoa, co the null). */
    private final BiConsumer<String, String> dieuHuongTimKiem = (key, tuKhoa) -> {
        chuyenMan(key);
        if (tuKhoa == null) return;
        if ("sinhvien".equals(key) && sinhVienPanel != null) sinhVienPanel.timKiem(tuKhoa);
        if ("congno".equals(key) && congNoPanel != null) congNoPanel.timKiem(tuKhoa);
    };

    public MainFrame(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;

        setTitle("Hệ Thống Quản Lý Học Phí Và Công Nợ Sinh Viên");
        setSize(1250, 780);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BG_MAIN);

        add(buildSidebar(), BorderLayout.WEST);

        JPanel northStack = new JPanel();
        northStack.setLayout(new BoxLayout(northStack, BoxLayout.Y_AXIS));
        northStack.add(buildTopBanner());
        northStack.add(buildHeader());
        add(northStack, BorderLayout.NORTH);

        content.setBackground(UITheme.BG_MAIN);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (taiKhoan.getVaiTro() == VaiTro.SINHVIEN) {
            LichSuThanhToanPanel lichSuRieng = new LichSuThanhToanPanel(taiKhoan.getHoTen(), taiKhoan.getMaSV(), true);
            content.add(new SinhVienCongNoPanel(taiKhoan, () -> chuyenMan("chatbot")), "tongquan");
            content.add(new HoaDonHocPhiSinhVienPanel(taiKhoan), "hoadon_sv");
            content.add(lichSuRieng, "lichsu_sv");
            content.add(new ChatbotPanel(), "chatbot");
            add(content, BorderLayout.CENTER);
            chuyenMan("tongquan");
            lichSuRieng.tuTaiDuLieu();
            return;
        }

        JPanel trangTongQuan = taiKhoan.getVaiTro() == VaiTro.KETOAN
                ? new vn.edu.eaut.qlhocphi.gui.dashboard.KeToanDashboardPanel(taiKhoan, this::chuyenMan)
                : new DashboardPanel(taiKhoan, this::chuyenMan);
        content.add(trangTongQuan, "tongquan");

        sinhVienPanel = new SinhVienPanel();
        congNoPanel = new CongNoPanel(dieuHuongTimKiem);

        content.add(sinhVienPanel, "sinhvien");
        content.add(new HocKyPanel(), "hocky");
        content.add(new HoaDonPanel(dieuHuongTimKiem), "hocphi");
        content.add(new PhieuThuPanel(dieuHuongTimKiem), "thanhtoan");
        content.add(congNoPanel, "congno");
        content.add(new vn.edu.eaut.qlhocphi.gui.baocao.DashboardPanel(dieuHuongTimKiem), "baocao");
        content.add(new ChatbotPanel(), "chatbot");

        if (taiKhoan.getVaiTro() == VaiTro.ADMIN) {
            content.add(new TaiKhoanPanel(taiKhoan), "taikhoan");
            content.add(new BackupRestorePanel(), "backup");
        }

        add(content, BorderLayout.CENTER);

        chuyenMan("tongquan");
    }

    private JPanel buildTopBanner() {
        JPanel banner = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UITheme.HEADER_TEAL_1, getWidth(), 0, UITheme.HEADER_TEAL_2);
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        banner.setPreferredSize(new Dimension(10, 54));
        banner.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        trai.setOpaque(false);
        trai.add(oIconTron("H", 34));
        JLabel tieuDe = new JLabel("Hệ Thống Quản Lý Học Phí Và Công Nợ Sinh Viên");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 17));
        tieuDe.setForeground(Color.WHITE);
        trai.add(tieuDe);
        banner.add(trai, BorderLayout.WEST);

        return banner;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(MAU_THANH_NGUOI_DUNG);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0x24, 0x2C, 0x3D)),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));

        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        trai.setOpaque(false);
        trai.add(UITheme.avatarTron(taiKhoan.getHoTen()));

        JLabel lblUser = new JLabel(
                "Xin Chào, " + taiKhoan.getHoTen() + "  (" + taiKhoan.getVaiTro() + ")"
        );
        lblUser.setFont(UITheme.FONT_BOLD);
        lblUser.setForeground(Color.WHITE);
        trai.add(lblUser);
        header.add(trai, BorderLayout.WEST);


        JButton btnDangXuat = UITheme.secondaryButton("Đăng Xuất ");
        btnDangXuat.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        header.add(btnDangXuat, BorderLayout.EAST);

        return header;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.BG_SIDEBAR);
        sidebar.setPreferredSize(new Dimension(240, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoRow.setOpaque(false);
        logoRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        logoRow.setBorder(BorderFactory.createEmptyBorder(0, 10, 24, 0));
        logoRow.add(oIconTron("QL", 30));
        JLabel logo = new JLabel("QL Học Phí");
        logo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        logo.setForeground(Color.WHITE);
        logoRow.add(logo);
        sidebar.add(logoRow);

        if (taiKhoan.getVaiTro() == VaiTro.SINHVIEN) {
            themMucMenu(sidebar, "tongquan", "Tổng Quan", UITheme.SIDEBAR_BLUE, "BAR");
            themMucMenu(sidebar, "hoadon_sv", "Hóa Đơn Học Phí", UITheme.SIDEBAR_ORANGE, "DOC");
            themMucMenu(sidebar, "lichsu_sv", "Lịch Sử Thanh Toán", UITheme.SIDEBAR_GREEN, "CARD");
            themMucMenu(sidebar, "chatbot", "Trợ Lý AI", UITheme.SIDEBAR_PURPLE, "BOT");
            themNutDoiMatKhau(sidebar);
            sidebar.add(Box.createVerticalGlue());
            return sidebar;
        }

        themMucMenu(sidebar, "tongquan", "Tổng Quan", UITheme.SIDEBAR_BLUE, "BAR");
        themMucMenu(sidebar, "sinhvien", "Sinh Viên", UITheme.SIDEBAR_GRAY, "USER");
        themMucMenu(sidebar, "hocky", "Học Kỳ & Mức Phí", UITheme.SIDEBAR_ORANGE, "CAL");
        themMucMenu(sidebar, "hocphi", "Hóa Đơn Học Phí", UITheme.SIDEBAR_PURPLE, "DOC");
        themMucMenu(sidebar, "thanhtoan", "Thanh Toán", UITheme.SIDEBAR_GREEN, "CARD");
        themMucMenu(sidebar, "congno", "Công Nợ", UITheme.SIDEBAR_GRAY, "BAL");
        themMucMenu(sidebar, "baocao", "Thống Kê & Báo Cáo", UITheme.SIDEBAR_BLUE, "LINE");
        themMucMenu(sidebar, "chatbot", "Trợ Lý AI (Chatbot)", UITheme.SIDEBAR_PURPLE, "BOT");

        if (taiKhoan.getVaiTro() == VaiTro.ADMIN) {
            JLabel nhanAdmin = new JLabel("  QUẢN TRỊ");
            nhanAdmin.setFont(new Font("Segoe UI", Font.BOLD, 11));
            nhanAdmin.setForeground(new Color(0x8A, 0x93, 0xA8));
            nhanAdmin.setAlignmentX(Component.LEFT_ALIGNMENT);
            nhanAdmin.setBorder(BorderFactory.createEmptyBorder(16, 10, 6, 0));
            sidebar.add(nhanAdmin);

            themMucMenu(sidebar, "taikhoan", "Quản Lý Tài Khoản", UITheme.SIDEBAR_GRAY, "KEY");
            themMucMenu(sidebar, "backup", "Sao Lưu / Phục Hồi", UITheme.SIDEBAR_GRAY, "DISK");
        }

        sidebar.add(Box.createVerticalGlue());
        return sidebar;
    }

    private JComponent oIconTron(String chu, int size) {
        JComponent icon = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 55));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, size <= 30 ? 12 : 14));
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(chu)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(chu, x, y);
                g2.dispose();
            }
        };
        icon.setPreferredSize(new Dimension(size, size));
        icon.setOpaque(false);
        return icon;
    }

    /** Tạo Icon màu dạng vector đồ họa đẹp mắt */
    private Icon taoIconMau(Color mau, String loaiIcon) {
        int w = 18, h = 18;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Nền bo góc mềm mại chứa Icon
        g2.setColor(mau);
        g2.fillRoundRect(0, 0, w, h, 6, 6);

        // Vẽ biểu tượng màu trắng bên trong
        g2.setColor(Color.WHITE);
        switch (loaiIcon) {
            case "BAR": // Biểu đồ cột
                g2.fillRect(3, 10, 3, 5);
                g2.fillRect(7, 6, 3, 9);
                g2.fillRect(11, 3, 3, 12);
                break;
            case "USER": // Sinh viên
                g2.fillOval(6, 3, 6, 6);
                g2.fillArc(3, 10, 12, 10, 0, 180);
                break;
            case "CAL": // Học kỳ
                g2.drawRect(3, 4, 11, 10);
                g2.fillRect(3, 4, 11, 3);
                break;
            case "DOC": // Hóa đơn
                g2.drawRect(4, 3, 9, 11);
                g2.drawLine(6, 6, 11, 6);
                g2.drawLine(6, 9, 11, 9);
                break;
            case "CARD": // Thanh toán
                g2.drawRect(2, 5, 13, 8);
                g2.fillRect(2, 7, 13, 2);
                break;
            case "BAL": // Công nợ (Cán cân)
                g2.drawLine(3, 9, 14, 9);
                g2.drawLine(8, 4, 8, 13);
                break;
            case "LINE": // Báo cáo
                g2.drawLine(3, 12, 7, 7);
                g2.drawLine(7, 7, 10, 10);
                g2.drawLine(10, 10, 14, 4);
                break;
            case "BOT": // AI Chatbot
                g2.drawRoundRect(4, 5, 9, 8, 3, 3);
                g2.fillOval(6, 7, 2, 2);
                g2.fillOval(10, 7, 2, 2);
                break;
            case "KEY": // Tài khoản
                g2.drawOval(4, 4, 5, 5);
                g2.drawLine(8, 8, 13, 13);
                break;
            case "DISK": // Sao lưu
                g2.drawRect(3, 3, 11, 11);
                g2.fillRect(6, 3, 5, 4);
                break;
        }
        g2.dispose();
        return new ImageIcon(img);
    }

    private void themMucMenu(JPanel sidebar, String key, String label, Color mauIcon, String loaiIcon) {
        JButton btn = new JButton(label, taoIconMau(mauIcon, loaiIcon));
        btn.setIconTextGap(12);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(UITheme.FONT_BASE);
        btn.setForeground(Color.WHITE);
        btn.setBackground(UITheme.BG_SIDEBAR);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> chuyenMan(key));

        sidebar.add(btn);
        menuButtons.put(key, btn);
    }

    private void themNutDoiMatKhau(JPanel sidebar) {
        JButton btn = new JButton("Đổi Mật Khẩu", taoIconMau(UITheme.SIDEBAR_GRAY, "KEY"));
        btn.setIconTextGap(12);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(UITheme.FONT_BASE);
        btn.setForeground(Color.WHITE);
        btn.setBackground(UITheme.BG_SIDEBAR);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 10));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> new DoiMatKhauDialog(this, taiKhoan).setVisible(true));
        sidebar.add(btn);
    }

    private void chuyenMan(String key) {
        cardLayout.show(content, key);
        for (Map.Entry<String, JButton> e : menuButtons.entrySet()) {
            boolean active = e.getKey().equals(key);
            e.getValue().setBackground(active ? UITheme.SIDEBAR_ACTIVE : UITheme.BG_SIDEBAR);
        }
    }
}