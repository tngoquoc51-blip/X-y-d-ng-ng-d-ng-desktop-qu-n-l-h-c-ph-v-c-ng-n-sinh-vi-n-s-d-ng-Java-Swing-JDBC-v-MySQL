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
import vn.edu.eaut.qlhocphi.gui.thanhtoan.DoiSoatNganHangPanel;
import vn.edu.eaut.qlhocphi.gui.thanhtoan.LichThuTuDongPanel;
import vn.edu.eaut.qlhocphi.gui.sinhvien.ViDienTuPanel;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.VaiTro;
import vn.edu.eaut.qlhocphi.gui.sinhvien.DoiMatKhauDialog;
import vn.edu.eaut.qlhocphi.gui.sinhvien.ThongTinCaNhanPanel;
import vn.edu.eaut.qlhocphi.bus.ThongBaoService;
import vn.edu.eaut.qlhocphi.gui.baocao.DuBaoCongNoPanel;
import vn.edu.eaut.qlhocphi.gui.common.AppLogo;

import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MainFrame extends JFrame {

    private final TaiKhoan taiKhoan;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel content = new JPanel(cardLayout);
    private final Map<String, MenuButtonCoBadge> menuButtons = new LinkedHashMap<>();
    private final ThongBaoService thongBaoService = new ThongBaoService();
    private javax.swing.Timer timerBadge;

    private SinhVienPanel sinhVienPanel;
    private CongNoPanel congNoPanel;

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
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(UITheme.BG_MAIN);

        add(buildSidebar(), BorderLayout.WEST);
        add(buildHeader(), BorderLayout.NORTH);

        content.setBackground(UITheme.BG_MAIN);
        content.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        if (taiKhoan.getVaiTro() == VaiTro.SINHVIEN) {
            LichSuThanhToanPanel lichSuRieng = new LichSuThanhToanPanel(taiKhoan.getHoTen(), taiKhoan.getMaSV(), true);
            content.add(new SinhVienCongNoPanel(taiKhoan, () -> chuyenMan("chatbot")), "tongquan");
            content.add(new HoaDonHocPhiSinhVienPanel(taiKhoan), "hoadon_sv");
            content.add(lichSuRieng, "lichsu_sv");
            content.add(new ViDienTuPanel(taiKhoan), "vidientu");
            content.add(new vn.edu.eaut.qlhocphi.gui.sinhvien.HocTapTinChiPanel(taiKhoan), "tinchi");
            content.add(new ChatbotPanel(), "chatbot");
            content.add(new ThongTinCaNhanPanel(taiKhoan), "thongtin");
            content.add(new vn.edu.eaut.qlhocphi.gui.sinhvien.ThongBaoSinhVienPanel(taiKhoan, this::capNhatBadgeThongBao), "thongbao_sv");
            add(content, BorderLayout.CENTER);
            chuyenMan("tongquan");
            lichSuRieng.tuTaiDuLieu();
            vn.edu.eaut.qlhocphi.gui.common.AutoRefreshTimer.gan(lichSuRieng, 20, lichSuRieng::tuTaiDuLieu);
            capNhatBadgeThongBao();
            batDauTuDongCapNhatBadge();
            return;
        }

        if (taiKhoan.getVaiTro() == VaiTro.ADMIN) {
            content.add(new vn.edu.eaut.qlhocphi.gui.admin.AdminHeThongPanel(taiKhoan, this::chuyenMan), "tongquan");
            content.add(new TaiKhoanPanel(taiKhoan), "taikhoan");
            content.add(new BackupRestorePanel(), "backup");
            content.add(new vn.edu.eaut.qlhocphi.gui.admin.NhatKyPanel(), "nhatky");
            content.add(new vn.edu.eaut.qlhocphi.gui.admin.CauHinhKyThuatPanel(taiKhoan), "cauhinhkythuat");
            content.add(new vn.edu.eaut.qlhocphi.gui.admin.CauHinhBaoMatPanel(), "cauhinhbaomat");
            content.add(new vn.edu.eaut.qlhocphi.gui.admin.SchedulerHealthPanel(), "scheduler");
            content.add(new vn.edu.eaut.qlhocphi.gui.admin.MauThongBaoPanel(), "mauthongbao");
            content.add(new vn.edu.eaut.qlhocphi.gui.admin.BroadcastPanel(taiKhoan), "broadcast");
            content.add(new vn.edu.eaut.qlhocphi.gui.admin.GuiThongBaoSinhVienPanel(taiKhoan), "gui_thongbao_sv");
            add(content, BorderLayout.CENTER);
            chuyenMan("tongquan");
            capNhatBadgeThongBao();
            batDauTuDongCapNhatBadge();
            return;
        }

        JPanel trangTongQuan = taiKhoan.getVaiTro() == VaiTro.KETOAN
                ? new vn.edu.eaut.qlhocphi.gui.dashboard.KeToanDashboardPanel(taiKhoan, this::chuyenMan)
                : new DashboardPanel(taiKhoan, this::chuyenMan);
        content.add(trangTongQuan, "tongquan");
        if (taiKhoan.getVaiTro() == VaiTro.KETOAN) {
            content.add(new vn.edu.eaut.qlhocphi.gui.dashboard.CaLamViecPanel(taiKhoan), "calamviec");
        }

        vn.edu.eaut.qlhocphi.bus.AuditContext.datNguoiDung(taiKhoan);

        sinhVienPanel = new SinhVienPanel(taiKhoan);
        congNoPanel = new CongNoPanel(dieuHuongTimKiem);

        content.add(sinhVienPanel, "sinhvien");
        content.add(new HocKyPanel(taiKhoan), "hocky");
        content.add(new HoaDonPanel(dieuHuongTimKiem, taiKhoan), "hocphi");
        content.add(new PhieuThuPanel(dieuHuongTimKiem), "thanhtoan");
        content.add(new DoiSoatNganHangPanel(taiKhoan), "doisoat");
        content.add(new LichThuTuDongPanel(taiKhoan), "lichthutudong");
        content.add(congNoPanel, "congno");
        content.add(new vn.edu.eaut.qlhocphi.gui.baocao.DashboardPanel(dieuHuongTimKiem), "baocao");
        content.add(new DuBaoCongNoPanel(dieuHuongTimKiem), "dubaocongno");
        content.add(new ChatbotPanel(), "chatbot");
        if (taiKhoan.getVaiTro() == VaiTro.PHONGDAOTAO) {
            content.add(new vn.edu.eaut.qlhocphi.gui.admin.BroadcastPanel(taiKhoan), "broadcast");
            content.add(new vn.edu.eaut.qlhocphi.gui.admin.GuiThongBaoSinhVienPanel(taiKhoan), "gui_thongbao_sv");
        }

        add(content, BorderLayout.CENTER);

        chuyenMan("tongquan");
        canhBaoQuaHanLucMoApp();
        capNhatBadgeThongBao();
        batDauTuDongCapNhatBadge();
    }

    private void canhBaoQuaHanLucMoApp() {
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return new vn.edu.eaut.qlhocphi.bus.CongNoService().layDanhSachQuaHan().size();
            }
            @Override
            protected void done() {
                try {
                    int soLuong = get();
                    if (soLuong > 0) {
                        JOptionPane.showMessageDialog(MainFrame.this,
                                "Hien co " + soLuong + " hoa don dang qua han thanh toan.\n"
                                        + "Vao trang Cong no de xem chi tiet va gui nhac no.",
                                "Canh bao cong no qua han", JOptionPane.WARNING_MESSAGE);
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private float wavePhase = 0f;
    private javax.swing.Timer waveTimer;

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = Math.max(getWidth(), 1), h = getHeight();
                float mid = 0.38f + 0.10f * (float) Math.sin(wavePhase);
                float[] fracs = {0f, mid, 1f};
                Color[] cols = {UITheme.SKY_DAM, UITheme.SKY_GIUA, UITheme.SKY_NHAT};
                LinearGradientPaint lgp = new LinearGradientPaint(0, 0, w, 0, fracs, cols);
                g2.setPaint(lgp);
                g2.fillRect(0, 0, w, h);
                g2.setColor(new Color(255, 255, 255, 40));
                g2.fillRect(0, h - 1, w, 1);
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(10, 64));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        if (waveTimer != null) waveTimer.stop();
        waveTimer = new javax.swing.Timer(50, e -> {
            wavePhase += 0.035f;
            if (wavePhase > Math.PI * 2) wavePhase -= (float) (Math.PI * 2);
            getContentPane().repaint();
        });
        waveTimer.start();
        addHierarchyListener(ev -> {
            if (!isDisplayable() && waveTimer != null) waveTimer.stop();
        });

        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        trai.setOpaque(false);
        // Không gắn logo hệ thống ở đây – logo chỉ hiện ở sidebar
        trai.add(UITheme.avatarTron(taiKhoan.getHoTen()));

        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));

        JLabel lblTieuDe = new JLabel("Hệ Thống Quản Lý Học Phí Và Công Nợ Sinh Viên");
        lblTieuDe.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTieuDe.setForeground(new Color(255, 255, 255, 245));
        lblTieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblUser = new JLabel("Xin Chào, " + taiKhoan.getHoTen() + "  (" + tenVaiTroHienThi(taiKhoan.getVaiTro()) + ")");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblUser.setForeground(Color.WHITE);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        chuText.add(lblTieuDe);
        chuText.add(lblUser);
        trai.add(chuText);
        header.add(trai, BorderLayout.WEST);

        JButton btnDangXuat = new JButton("Đăng Xuất");
        btnDangXuat.setFont(UITheme.FONT_BOLD);
        btnDangXuat.setForeground(UITheme.PRIMARY_DARK);
        btnDangXuat.setBackground(Color.WHITE);
        btnDangXuat.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btnDangXuat.setFocusPainted(false);
        btnDangXuat.setOpaque(true);
        btnDangXuat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDangXuat.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        JPanel phaiBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        phaiBox.setOpaque(false);
        phaiBox.add(new vn.edu.eaut.qlhocphi.gui.common.ThemeToggleButton(this::doiTheme));
        phaiBox.add(btnDangXuat);
        header.add(phaiBox, BorderLayout.EAST);

        return header;
    }

    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int h = Math.max(getHeight(), 1);
                // Gradient dọc dịu hơn: đậm trên → vừa dưới (không xuống quá nhạt)
                float mid = 0.50f + 0.06f * (float) Math.sin(wavePhase + 0.4f);
                float[] fracs = {0f, mid, 1f};
                // Giữ nền đủ đậm để chữ trắng luôn rõ, không xuống quá nhạt
                Color[] cols = {UITheme.SKY_DAM, UITheme.SKY_GIUA, UITheme.SKY_GIUA};
                LinearGradientPaint lgp = new LinearGradientPaint(0, 0, 0, h, fracs, cols);
                g2.setPaint(lgp);
                g2.fillRect(0, 0, getWidth(), h);
                g2.dispose();
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setOpaque(false);
        sidebar.setPreferredSize(new Dimension(248, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(125, 211, 252, 120)),
                BorderFactory.createEmptyBorder(14, 0, 14, 0)));

        // Logo to, can giua, chu ben duoi
        JPanel logoBox = new JPanel();
        logoBox.setOpaque(false);
        logoBox.setLayout(new BoxLayout(logoBox, BoxLayout.Y_AXIS));
        logoBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));
        logoBox.setBorder(BorderFactory.createEmptyBorder(8, 12, 12, 12));

        JComponent logoIcon = AppLogo.tao(72);  // to hon
        logoIcon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel logoText = new JLabel("Quản Lý Nợ Công Sinh Viên");
        logoText.setFont(new Font("Segoe UI", Font.BOLD, 17));
        logoText.setForeground(Color.WHITE);
        logoText.setAlignmentX(Component.CENTER_ALIGNMENT);
        logoText.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        logoBox.add(logoIcon);
        logoBox.add(logoText);
        sidebar.add(logoBox);
        sidebar.add(Box.createRigidArea(new Dimension(0, 8)));

        if (taiKhoan.getVaiTro() == VaiTro.SINHVIEN) {
            themMucMenu(sidebar, "tongquan", "Tổng Quan", UITheme.SIDEBAR_BLUE, "BAR");
            themMucMenu(sidebar, "hoadon_sv", "Hóa Đơn Học Phí", UITheme.SIDEBAR_ORANGE, "DOC");
            themMucMenu(sidebar, "lichsu_sv", "Lịch Sử Thanh Toán", UITheme.SIDEBAR_GREEN, "CARD");
            themMucMenu(sidebar, "vidientu", "Ví Học Phí", UITheme.SIDEBAR_ORANGE, "CARD");
            themMucMenu(sidebar, "tinchi", "Tiến độ & Tín chỉ", UITheme.SIDEBAR_GREEN, "BAR");
            themMucMenu(sidebar, "thongtin", "Thông Tin Cá Nhân", UITheme.SIDEBAR_GRAY, "USER");
            themMucMenu(sidebar, "thongbao_sv", "Thông Báo", UITheme.SIDEBAR_BLUE, "DOC");
            themMucMenu(sidebar, "chatbot", "Trợ Lý AI", UITheme.SIDEBAR_PURPLE, "BOT");
            themNutDoiMatKhau(sidebar);
            sidebar.add(Box.createVerticalGlue());
            return sidebar;
        }

        if (taiKhoan.getVaiTro() == VaiTro.ADMIN) {
            themMucMenu(sidebar, "tongquan", "Tổng Quan Hệ Thống", UITheme.SIDEBAR_BLUE, "BAR");

            JLabel nhanAdmin = new JLabel("  BẢO TRÌ & CẤU HÌNH");
            nhanAdmin.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nhanAdmin.setForeground(new Color(255, 255, 255, 240));
            nhanAdmin.setAlignmentX(Component.LEFT_ALIGNMENT);
            nhanAdmin.setBorder(BorderFactory.createEmptyBorder(14, 10, 6, 0));
            sidebar.add(nhanAdmin);

            themMucMenu(sidebar, "taikhoan", "Quản Lý Tài Khoản", UITheme.SIDEBAR_GRAY, "KEY");
            themMucMenu(sidebar, "backup", "Sao Lưu / Phục Hồi", UITheme.SIDEBAR_GRAY, "DISK");
            themMucMenu(sidebar, "nhatky", "Nhật Ký Hệ Thống", UITheme.SIDEBAR_GRAY, "LINE");
            themMucMenu(sidebar, "cauhinhkythuat", "Cấu Hình Kỹ Thuật", UITheme.SIDEBAR_PURPLE, "BOT");
            themMucMenu(sidebar, "cauhinhbaomat", "Cấu Hình Bảo Mật", UITheme.SIDEBAR_ORANGE, "KEY");
            themMucMenu(sidebar, "scheduler", "Scheduler Health", UITheme.SIDEBAR_GREEN, "BAR");
            themMucMenu(sidebar, "mauthongbao", "Mẫu Thông Báo", UITheme.SIDEBAR_BLUE, "DOC");
            themMucMenu(sidebar, "broadcast", "Broadcast Toàn Trường", UITheme.SIDEBAR_PURPLE, "BOT");
            themMucMenu(sidebar, "gui_thongbao_sv", "Gửi TB Sinh Viên", UITheme.SIDEBAR_BLUE, "DOC");

            sidebar.add(Box.createVerticalGlue());
            return sidebar;
        }

        if (taiKhoan.getVaiTro() == VaiTro.PHONGDAOTAO) {
            JLabel nhanPdt = new JLabel("  PHÒNG ĐÀO TẠO");
            nhanPdt.setFont(new Font("Segoe UI", Font.BOLD, 12));
            nhanPdt.setForeground(new Color(255, 255, 255, 245));
            nhanPdt.setAlignmentX(Component.LEFT_ALIGNMENT);
            nhanPdt.setBorder(BorderFactory.createEmptyBorder(6, 10, 8, 0));
            sidebar.add(nhanPdt);
        }

        themMucMenu(sidebar, "tongquan", "Tổng Quan", UITheme.SIDEBAR_BLUE, "BAR");
        if (taiKhoan.getVaiTro() == VaiTro.KETOAN) {
            themMucMenu(sidebar, "calamviec", "Ca Làm Việc", UITheme.SIDEBAR_ORANGE, "CARD");
        }
        themMucMenu(sidebar, "sinhvien", "Sinh Viên", UITheme.SIDEBAR_BLUE, "USER");
        themMucMenu(sidebar, "hocky", "Học Kỳ & Mức Phí", UITheme.SIDEBAR_ORANGE, "CAL");
        themMucMenu(sidebar, "hocphi", "Hóa Đơn Học Phí", UITheme.SIDEBAR_PURPLE, "DOC");
        themMucMenu(sidebar, "thanhtoan", "Thanh Toán", UITheme.SIDEBAR_GREEN, "CARD");
        themMucMenu(sidebar, "doisoat", "Đối Soát Ngân Hàng", UITheme.SIDEBAR_PURPLE, "BOT");
        themMucMenu(sidebar, "lichthutudong", "Thu Tự Động", UITheme.SIDEBAR_GREEN, "BOT");
        themMucMenu(sidebar, "congno", "Công Nợ", UITheme.SIDEBAR_ORANGE, "BAL");
        themMucMenu(sidebar, "baocao", "Thống Kê & Báo Cáo", UITheme.SIDEBAR_BLUE, "LINE");
        themMucMenu(sidebar, "dubaocongno", "Dự Báo AI", UITheme.SIDEBAR_PURPLE, "BOT");
        themMucMenu(sidebar, "chatbot", "Trợ Lý AI (Chatbot)", UITheme.SIDEBAR_PURPLE, "BOT");
        if (taiKhoan.getVaiTro() == VaiTro.PHONGDAOTAO) {
            themMucMenu(sidebar, "broadcast", "Thông Báo Toàn Trường", UITheme.SIDEBAR_PURPLE, "BOT");
            themMucMenu(sidebar, "gui_thongbao_sv", "Gửi TB Sinh Viên", UITheme.SIDEBAR_BLUE, "DOC");
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
                GradientPaint gp = new GradientPaint(0, 0, UITheme.PRIMARY_DARK, getWidth(), getHeight(), UITheme.PRIMARY);
                g2.setPaint(gp);
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

    private Icon taoIconMau(Color mau, String loaiIcon) {
        int w = 20, h = 20;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(mau);
        g2.fillRoundRect(0, 0, w, h, 6, 6);
        g2.setColor(Color.WHITE);
        switch (loaiIcon) {
            case "BAR":
                g2.fillRect(3, 10, 3, 5); g2.fillRect(7, 6, 3, 9); g2.fillRect(11, 3, 3, 12); break;
            case "USER":
                g2.fillOval(6, 3, 6, 6); g2.fillArc(3, 10, 12, 10, 0, 180); break;
            case "CAL":
                g2.drawRect(3, 4, 11, 10); g2.fillRect(3, 4, 11, 3); break;
            case "DOC":
                g2.drawRect(4, 3, 9, 11); g2.drawLine(6, 6, 11, 6); g2.drawLine(6, 9, 11, 9); break;
            case "CARD":
                g2.drawRect(2, 5, 13, 8); g2.fillRect(2, 7, 13, 2); break;
            case "BAL":
                g2.drawLine(3, 9, 14, 9); g2.drawLine(8, 4, 8, 13); break;
            case "LINE":
                g2.drawLine(3, 12, 7, 7); g2.drawLine(7, 7, 10, 10); g2.drawLine(10, 10, 14, 4); break;
            case "BOT":
                g2.drawRoundRect(4, 5, 9, 8, 3, 3); g2.fillOval(6, 7, 2, 2); g2.fillOval(10, 7, 2, 2); break;
            case "KEY":
                g2.drawOval(4, 4, 5, 5); g2.drawLine(8, 8, 13, 13); break;
            case "DISK":
                g2.drawRect(3, 3, 11, 11); g2.fillRect(6, 3, 5, 4); break;
        }
        g2.dispose();
        return new ImageIcon(img);
    }

    private void themMucMenu(JPanel sidebar, String key, String label, Color mauIcon, String loaiIcon) {
        MenuButtonCoBadge btn = new MenuButtonCoBadge(label, taoIconMau(mauIcon, loaiIcon));
        btn.setUI(new BasicButtonUI());
        btn.setIconTextGap(14);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        btn.setPreferredSize(new Dimension(240, 48));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        // Chữ trắng tinh, đậm – không mờ trên nền sky
        btn.setForeground(new Color(0xFF, 0xFF, 0xFF));
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 12));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setRolloverEnabled(false);
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
        btn.setBackground(new Color(0, 0, 0, 0));
        btn.setBorder(BorderFactory.createEmptyBorder(11, 22, 11, 12));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addActionListener(e -> new DoiMatKhauDialog(this, taiKhoan).setVisible(true));
        sidebar.add(btn);
    }

    private void chuyenMan(String key) {
        cardLayout.show(content, key);
        // Active: nền trắng mờ + chữ primary đậm; Idle: chữ trắng
        Color activeBg = new Color(255, 255, 255, 245);
        Color activeFg = UITheme.PRIMARY_DARK;
        Color idleFg = new Color(0xFF, 0xFF, 0xFF); // trắng tinh, không mờ
        for (Map.Entry<String, MenuButtonCoBadge> e : menuButtons.entrySet()) {
            boolean active = e.getKey().equals(key);
            e.getValue().setBackground(active ? activeBg : new Color(0, 0, 0, 0));
            e.getValue().setForeground(active ? activeFg : idleFg);
            e.getValue().setFont(new Font("Segoe UI", Font.BOLD, active ? 15 : 15));
            e.getValue().setOpaque(active);
        }
        danhDauDaDocVaXoaBadge(key);
    }

    private void danhDauDaDocVaXoaBadge(String key) {
        MenuButtonCoBadge btn = menuButtons.get(key);
        if (btn != null) btn.setSoBienDong(0);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                thongBaoService.danhDauDaDoc(taiKhoan, key);
                return null;
            }
            @Override
            protected void done() { }
        };
        worker.execute();
    }

    private void capNhatBadgeThongBao() {
        SwingWorker<Map<String, Integer>, Void> worker = new SwingWorker<>() {
            @Override
            protected Map<String, Integer> doInBackground() throws Exception {
                return thongBaoService.demChuaDocTheoManHinh(taiKhoan);
            }
            @Override
            protected void done() {
                try {
                    Map<String, Integer> demTheoManHinh = get();
                    for (Map.Entry<String, MenuButtonCoBadge> e : menuButtons.entrySet()) {
                        Integer soLuong = demTheoManHinh.get(e.getKey());
                        e.getValue().setSoBienDong(soLuong != null ? soLuong : 0);
                    }
                } catch (Exception ignored) {
                }
            }
        };
        worker.execute();
    }

    private void batDauTuDongCapNhatBadge() {
        timerBadge = new javax.swing.Timer(15_000, e -> capNhatBadgeThongBao());
        timerBadge.setRepeats(true);
        timerBadge.start();
        addHierarchyListener(e -> {
            if (!isDisplayable() && timerBadge != null) timerBadge.stop();
        });
    }

    private static String tenVaiTroHienThi(VaiTro vt) {
        if (vt == null) return "";
        return switch (vt) {
            case ADMIN -> "Admin Hệ Thống";
            case PHONGDAOTAO -> "Phòng Đào Tạo";
            case KETOAN -> "Kế Toán";
            case SINHVIEN -> "Sinh Viên";
        };
    }

    private static class MenuButtonCoBadge extends JButton {
        private int soBienDong = 0;

        MenuButtonCoBadge(String text, Icon icon) {
            super(text, icon);
            setForeground(new Color(0xFF, 0xFF, 0xFF));
            setFont(new Font("Segoe UI", Font.BOLD, 15));
            setHorizontalTextPosition(SwingConstants.RIGHT);
            setVerticalTextPosition(SwingConstants.CENTER);
        }

        void setSoBienDong(int soLuong) {
            this.soBienDong = soLuong;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg = getBackground();
            boolean active = isOpaque() && bg != null && bg.getAlpha() > 0;

            if (active) {
                g2.setColor(new Color(255, 255, 255, 235));
                g2.fillRoundRect(6, 4, getWidth() - 12, getHeight() - 8, 10, 10);
                g2.setColor(UITheme.PRIMARY_DARK);
                g2.fillRoundRect(6, 8, 4, getHeight() - 16, 3, 3);
            }
            g2.dispose();

            super.paintComponent(g);

            if (soBienDong <= 0) return;

            g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            String nhan = soBienDong > 9 ? "9+" : String.valueOf(soBienDong);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            int rongChu = fm.stringWidth(nhan);
            int duongKinh = Math.max(16, rongChu + 8);
            int x = getWidth() - duongKinh - 8;
            int y = 6;
            g2.setColor(UITheme.PRIMARY_DARK);
            g2.fillOval(x - 1, y - 1, duongKinh + 2, duongKinh + 2);
            g2.setColor(UITheme.DANGER);
            g2.fillOval(x, y, duongKinh, duongKinh);
            g2.setColor(Color.WHITE);
            g2.drawString(nhan, x + (duongKinh - rongChu) / 2, y + duongKinh - 5);
            g2.dispose();
        }
    }

    private void doiTheme() {
        vn.edu.eaut.qlhocphi.config.UITheme.apDungTheoCheDo();
        dispose();
        SwingUtilities.invokeLater(() -> new MainFrame(taiKhoan).setVisible(true));
    }
}