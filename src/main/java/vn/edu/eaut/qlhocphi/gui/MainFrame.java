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



import javax.swing.*;
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
            content.add(new ChatbotPanel(), "chatbot");
            content.add(new ThongTinCaNhanPanel(taiKhoan), "thongtin");
            add(content, BorderLayout.CENTER);
            chuyenMan("tongquan");
            lichSuRieng.tuTaiDuLieu();
            vn.edu.eaut.qlhocphi.gui.common.AutoRefreshTimer.gan(lichSuRieng, 20, lichSuRieng::tuTaiDuLieu);
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

        if (taiKhoan.getVaiTro() == VaiTro.ADMIN) {
            content.add(new TaiKhoanPanel(taiKhoan), "taikhoan");
            content.add(new BackupRestorePanel(), "backup");   // ✅ bỏ tham số taiKhoan
            content.add(new vn.edu.eaut.qlhocphi.gui.admin.NhatKyPanel(), "nhatky");
        }

        add(content, BorderLayout.CENTER);

        chuyenMan("tongquan");
        canhBaoQuaHanLucMoApp();
        capNhatBadgeThongBao();
        batDauTuDongCapNhatBadge();
    }

    /** Tu dong quet va canh bao ngay khi Admin/Ke toan vua dang nhap, neu co hoa don qua han. */
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
                    // Khong lam gian doan trai nghiem dang nhap neu buoc canh bao nay loi
                }
            }
        };
        worker.execute();
    }


    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout()) {
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
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(10, 68));
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        trai.setOpaque(false);
        trai.add(oIconTron("H", 38));
        trai.add(UITheme.avatarTron(taiKhoan.getHoTen()));

        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));

        JLabel lblTieuDe = new JLabel("Hệ Thống Quản Lý Học Phí Và Công Nợ Sinh Viên");
        lblTieuDe.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTieuDe.setForeground(new Color(255, 255, 255, 190));
        lblTieuDe.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblUser = new JLabel("Xin Chào, " + taiKhoan.getHoTen() + "  (" + taiKhoan.getVaiTro() + ")");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblUser.setForeground(Color.WHITE);
        lblUser.setAlignmentX(Component.LEFT_ALIGNMENT);

        chuText.add(lblTieuDe);
        chuText.add(lblUser);
        trai.add(chuText);

        header.add(trai, BorderLayout.WEST);

        JButton btnDangXuat = new JButton("Đăng Xuất");
        btnDangXuat.setFont(UITheme.FONT_BOLD);
        btnDangXuat.setForeground(UITheme.HEADER_TEAL_2);
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
            themMucMenu(sidebar, "vidientu", "Ví Học Phí", UITheme.SIDEBAR_ORANGE, "CARD");
            themMucMenu(sidebar, "thongtin", "Thông Tin Cá Nhân", UITheme.SIDEBAR_GRAY, "USER");
            themMucMenu(sidebar, "chatbot", "Trợ Lý AI", UITheme.SIDEBAR_PURPLE, "BOT");
            themNutDoiMatKhau(sidebar);
            sidebar.add(Box.createVerticalGlue());
            return sidebar;
        }

        themMucMenu(sidebar, "tongquan", "Tổng Quan", UITheme.SIDEBAR_BLUE, "BAR");
        if (taiKhoan.getVaiTro() == VaiTro.KETOAN) {
            themMucMenu(sidebar, "calamviec", "Ca Làm Việc", UITheme.SIDEBAR_GREEN, "CARD");
        }
        themMucMenu(sidebar, "sinhvien", "Sinh Viên", UITheme.SIDEBAR_GRAY, "USER");
        themMucMenu(sidebar, "hocky", "Học Kỳ & Mức Phí", UITheme.SIDEBAR_ORANGE, "CAL");
        themMucMenu(sidebar, "hocphi", "Hóa Đơn Học Phí", UITheme.SIDEBAR_PURPLE, "DOC");
        themMucMenu(sidebar, "thanhtoan", "Thanh Toán", UITheme.SIDEBAR_GREEN, "CARD");
        themMucMenu(sidebar, "doisoat", "Đối Soát Ngân Hàng", UITheme.SIDEBAR_PURPLE, "BOT");
        themMucMenu(sidebar, "lichthutudong", "Thu Tự Động", UITheme.SIDEBAR_GREEN, "BOT");
        themMucMenu(sidebar, "congno", "Công Nợ", UITheme.SIDEBAR_GRAY, "BAL");
        themMucMenu(sidebar, "baocao", "Thống Kê & Báo Cáo", UITheme.SIDEBAR_BLUE, "LINE");
        themMucMenu(sidebar, "dubaocongno", "Dự Báo AI", UITheme.SIDEBAR_PURPLE, "BOT");
        themMucMenu(sidebar, "chatbot", "Trợ Lý AI (Chatbot)", UITheme.SIDEBAR_PURPLE, "BOT");

        if (taiKhoan.getVaiTro() == VaiTro.ADMIN) {
            JLabel nhanAdmin = new JLabel("  QUẢN TRỊ");
            nhanAdmin.setFont(new Font("Segoe UI", Font.BOLD, 11));
            nhanAdmin.setForeground(new Color(0x8A, 0x93, 0xA8));
            nhanAdmin.setAlignmentX(Component.LEFT_ALIGNMENT);
            nhanAdmin.setBorder(BorderFactory.createEmptyBorder(16, 10, 6, 0));
            sidebar.add(nhanAdmin);

            themMucMenu(sidebar, "taikhoan", "Quan ly tai khoan", UITheme.SIDEBAR_GRAY, "KEY");
            themMucMenu(sidebar, "backup", "Sao luu / Phuc hoi", UITheme.SIDEBAR_GRAY, "DISK");
            themMucMenu(sidebar, "nhatky", "Nhat ky he thong", UITheme.SIDEBAR_GRAY, "LINE");
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
        MenuButtonCoBadge btn = new MenuButtonCoBadge(label, taoIconMau(mauIcon, loaiIcon));
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
        for (Map.Entry<String, MenuButtonCoBadge> e : menuButtons.entrySet()) {
            boolean active = e.getKey().equals(key);
            e.getValue().setBackground(active ? UITheme.SIDEBAR_ACTIVE : UITheme.BG_SIDEBAR);
        }
        danhDauDaDocVaXoaBadge(key);
    }

    /** Khi nguoi dung mo 1 man hinh: danh dau da doc duoi CSDL (chay nen) VA xoa badge NGAY LAP TUC
     *  tren giao dien (khong doi CSDL xong moi an) de trai nghiem muot, khong bi giat. */
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
            protected void done() { /* khong can lam gi them, UI da cap nhat truoc do */ }
        };
        worker.execute();
    }

    /** Doc so luong thong bao chua doc cho TAT CA muc menu 1 lan, ve badge tuong ung. */
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
                    // Loi tam thoi khong lam gian doan trai nghiem, lan quet sau se tu cap nhat lai
                }
            }
        };
        worker.execute();
    }

    /** Quet lai badge dinh ky moi 15 giay, de admin/ke toan/sinh vien thay thong bao moi
     *  gan nhu ngay lap tuc ma khong can dang xuat/dang nhap lai. */
    private void batDauTuDongCapNhatBadge() {
        timerBadge = new javax.swing.Timer(15_000, e -> capNhatBadgeThongBao());
        timerBadge.setRepeats(true);
        timerBadge.start();
        addHierarchyListener(e -> {
            if (!isDisplayable() && timerBadge != null) timerBadge.stop();
        });
    }

    /** JButton menu sidebar, co the ve them "huy hieu" (badge) so mau do o goc tren-phai
     *  khi co thong bao chua doc. soBienDong = 0 thi khong ve gi ca. */
    private static class MenuButtonCoBadge extends JButton {
        private int soBienDong = 0;

        MenuButtonCoBadge(String text, Icon icon) {
            super(text, icon);
        }

        void setSoBienDong(int soLuong) {
            this.soBienDong = soLuong;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (soBienDong <= 0) return;

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            String nhan = soBienDong > 9 ? "9+" : String.valueOf(soBienDong);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            FontMetrics fm = g2.getFontMetrics();
            int rongChu = fm.stringWidth(nhan);
            int duongKinh = Math.max(16, rongChu + 8);

            int x = getWidth() - duongKinh - 6;
            int y = 4;

            g2.setColor(new Color(0x14, 0x16, 0x2E));
            g2.fillOval(x - 2, y - 2, duongKinh + 4, duongKinh + 4);

            g2.setColor(new Color(0xE1, 0x1D, 0x48));
            g2.fillOval(x, y, duongKinh, duongKinh);

            g2.setColor(Color.WHITE);
            g2.drawString(nhan, x + (duongKinh - rongChu) / 2, y + duongKinh - 5);
            g2.dispose();
        }
    }

    /** Goi khi nguoi dung bam nut chuyen Sang/Toi: ap dung bang mau moi roi RE-BUILD lai
     *  toan bo cua so (vi nhieu component da setBackground(...) 1 lan luc khoi tao, khong
     *  tu doi mau neu chi doi field UITheme suong). Dong cua so cu, mo cua so moi voi
     *  cung tai khoan dang dang nhap - nguoi dung khong bi dang xuat. */
    private void doiTheme() {
        vn.edu.eaut.qlhocphi.config.UITheme.apDungTheoCheDo();
        dispose();
        SwingUtilities.invokeLater(() -> new MainFrame(taiKhoan).setVisible(true));
    }
}