package vn.edu.eaut.qlhocphi.gui.dashboard;

import vn.edu.eaut.qlhocphi.bus.BaoCaoService;
import vn.edu.eaut.qlhocphi.bus.CongNoService;
import vn.edu.eaut.qlhocphi.bus.SinhVienService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.AutoRefreshTimer;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.SinhVien;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Màn hình "Tổng quan" (Dashboard quản lý) dành cho Admin/Kế toán - phiên bản
 * nâng cấp với biểu đồ vòng tròn (donut) thể hiện tỷ lệ thu học phí, vẽ tay bằng
 * Graphics2D/Arc2D (không dùng thư viện ngoài) - điểm nhấn phân tích trực quan
 * hiếm gặp trong đồ án quản lý học phí thông thường (đa số chỉ dùng bảng số/thẻ KPI).
 */
public class DashboardPanel extends JPanel {
    private final CongNoService congNoService = new CongNoService();
    private final SinhVienService sinhVienService = new SinhVienService();
    private final BaoCaoService baoCaoService = new BaoCaoService();

    private final JPanel khoiCongNo = new JPanel();
    private final JPanel khoiSinhVienMoi = new JPanel();
    private JPanel kpiRow;
    private DonutChart donutChart;
    private JLabel lblDonutTamGiua, lblChuThichDaThu, lblChuThichConNo;
    private final Consumer<String> dieuHuong;

    public DashboardPanel(TaiKhoan taiKhoan, Consumer<String> dieuHuong) {
        this.dieuHuong = dieuHuong;
        setLayout(new BorderLayout(0, 18));
        setOpaque(false);

        add(buildBanner(taiKhoan), BorderLayout.NORTH);

        JPanel giua = new JPanel();
        giua.setOpaque(false);
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));

        kpiRow = buildKpiRow();
        kpiRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        giua.add(kpiRow);
        giua.add(Box.createRigidArea(new Dimension(0, 18)));

        JPanel phanTichRow = buildPhanTichRow(dieuHuong);
        phanTichRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        giua.add(phanTichRow);
        giua.add(Box.createRigidArea(new Dimension(0, 18)));

        JPanel thongTinRow = buildThongTinRow();
        thongTinRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        giua.add(thongTinRow);

        add(giua, BorderLayout.CENTER);

        taiDuLieuKpi();
        taiDuLieuCongNo();
        taiDuLieuSinhVienMoi();
        AutoRefreshTimer.gan(this, 30, () -> {
            taiDuLieuKpi();
            taiDuLieuCongNo();
            taiDuLieuSinhVienMoi();
        });
    }

    // ================== BANNER ==================

    private JPanel buildBanner(TaiKhoan taiKhoan) {
        JPanel banner = UITheme.gradientBanner();
        banner.setPreferredSize(new Dimension(0, 120));
        banner.setBorder(BorderFactory.createEmptyBorder(20, 26, 20, 26));

        JPanel traiPanel = new JPanel();
        traiPanel.setOpaque(false);
        traiPanel.setLayout(new BoxLayout(traiPanel, BoxLayout.Y_AXIS));

        JLabel loiChao = new JLabel(loiChaoTheoGio() + ",");
        loiChao.setFont(UITheme.FONT_BASE);
        loiChao.setForeground(new Color(0xDB, 0xE6, 0xFF));

        JLabel ten = new JLabel(taiKhoan.getHoTen());
        ten.setFont(new Font("Segoe UI", Font.BOLD, 24));
        ten.setForeground(Color.WHITE);

        Locale locVi = new Locale.Builder().setLanguage("vi").build();
        String ngayText = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", locVi));
        ngayText = Character.toUpperCase(ngayText.charAt(0)) + ngayText.substring(1);
        JLabel ngay = new JLabel(ngayText);
        ngay.setFont(UITheme.FONT_BASE);
        ngay.setForeground(new Color(0xDB, 0xE6, 0xFF));

        traiPanel.add(loiChao);
        traiPanel.add(Box.createRigidArea(new Dimension(0, 4)));
        traiPanel.add(ten);
        traiPanel.add(Box.createRigidArea(new Dimension(0, 8)));
        traiPanel.add(ngay);

        banner.add(traiPanel, BorderLayout.WEST);

        JLabel iconTron = new JLabel("\uD83D\uDCCA", SwingConstants.CENTER);
        iconTron.setOpaque(true);
        iconTron.setBackground(new Color(255, 255, 255, 40));
        iconTron.setForeground(Color.WHITE);
        iconTron.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        iconTron.setPreferredSize(new Dimension(58, 58));
        JPanel phaiWrap = new JPanel(new GridBagLayout());
        phaiWrap.setOpaque(false);
        phaiWrap.add(iconTron);
        banner.add(phaiWrap, BorderLayout.EAST);

        return banner;
    }

    private String loiChaoTheoGio() {
        int gio = java.time.LocalTime.now().getHour();
        if (gio < 11) return "Chào buổi sáng";
        if (gio < 13) return "Chào buổi trưa";
        if (gio < 18) return "Chào buổi chiều";
        return "Chào buổi tối";
    }

    // ================== KPI SỐ LIỆU THẬT ==================

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.add(UITheme.statCard("Tổng sinh viên", "…", UITheme.TINT_BLUE, UITheme.TEXT_BLUE));
        row.add(UITheme.statCard("Tổng đã thu", "…", UITheme.TINT_GREEN, UITheme.TEXT_GREEN));
        row.add(UITheme.statCard("Tổng công nợ", "…", UITheme.TINT_RED, UITheme.TEXT_RED));
        row.add(UITheme.statCard("Hóa đơn quá hạn", "…", UITheme.TINT_VIOLET, UITheme.TEXT_VIOLET));
        return row;
    }

    private void taiDuLieuKpi() {
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                int tongSV = sinhVienService.layTatCa().size();
                BaoCaoService.BaoCaoTongQuan tq = baoCaoService.layTongQuan();
                return new Object[]{tongSV, tq};
            }

            @Override
            protected void done() {
                try {
                    Object[] kq = get();
                    int tongSV = (int) kq[0];
                    BaoCaoService.BaoCaoTongQuan tq = (BaoCaoService.BaoCaoTongQuan) kq[1];

                    capNhatKpi(0, String.valueOf(tongSV));
                    capNhatKpi(1, MoneyUtils.format(tq.getTongDaThu()));
                    capNhatKpi(2, MoneyUtils.format(tq.getTongConNo()));
                    capNhatKpi(3, String.valueOf(tq.getSoHoaDonQuaHan()));

                    capNhatDonut(tq.getTongDaThu(), tq.getTongConNo());
                } catch (Exception ex) {
                    for (int i = 0; i < 4; i++) capNhatKpi(i, "--");
                }
            }
        };
        worker.execute();
    }

    private void capNhatKpi(int index, String giaTriMoi) {
        JPanel the = (JPanel) kpiRow.getComponent(index);
        JLabel lbl = timNhanTheoTen(the, "giaTri");
        if (lbl != null) lbl.setText(giaTriMoi);
    }

    // ================== KHỐI PHÂN TÍCH: DONUT CHART + TRUY CẬP NHANH ==================

    private JPanel buildPhanTichRow(Consumer<String> dieuHuong) {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.add(buildDonutCard());
        row.add(buildQuickActionsCard(dieuHuong));
        return row;
    }

    /** Thẻ chứa biểu đồ vòng tròn tỷ lệ đã thu / còn nợ - điểm nhấn phân tích trực quan. */
    private JPanel buildDonutCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 4));

        JLabel tieuDe = new JLabel("Tỷ lệ thu học phí");
        tieuDe.setFont(UITheme.FONT_H2);
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);
        card.add(tieuDe, BorderLayout.NORTH);

        JPanel giua = new JPanel(new BorderLayout(20, 0));
        giua.setOpaque(false);
        giua.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        donutChart = new DonutChart();
        donutChart.setPreferredSize(new Dimension(140, 140));
        JPanel donutWrap = new JPanel(new GridBagLayout());
        donutWrap.setOpaque(false);
        donutWrap.add(donutChart);
        giua.add(donutWrap, BorderLayout.WEST);

        JPanel chuThich = new JPanel();
        chuThich.setOpaque(false);
        chuThich.setLayout(new BoxLayout(chuThich, BoxLayout.Y_AXIS));
        chuThich.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        lblChuThichDaThu = dongChuThich(UITheme.SUCCESS, "Đã thu", "…");
        lblChuThichConNo = dongChuThich(UITheme.DANGER, "Còn nợ", "…");
        chuThich.add(lblChuThichDaThu);
        chuThich.add(Box.createRigidArea(new Dimension(0, 10)));
        chuThich.add(lblChuThichConNo);
        chuThich.add(Box.createVerticalGlue());
        giua.add(chuThich, BorderLayout.CENTER);

        card.add(giua, BorderLayout.CENTER);
        return card;
    }

    private JLabel dongChuThich(Color mauCham, String nhan, String giaTri) {
        JLabel l = new JLabel("<html>&#9679; " + nhan + " &nbsp; <b>" + giaTri + "</b></html>");
        l.setForeground(mauCham);
        l.setFont(UITheme.FONT_BASE);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void capNhatDonut(BigDecimal daThu, BigDecimal conNo) {
        BigDecimal tong = daThu.add(conNo);
        float tiLe = 0f;
        if (tong.compareTo(BigDecimal.ZERO) > 0) {
            tiLe = daThu.multiply(BigDecimal.valueOf(100))
                    .divide(tong, 1, RoundingMode.HALF_UP)
                    .floatValue() / 100f;
        }
        donutChart.setTiLe(tiLe);
        lblChuThichDaThu.setText("<html>&#9679; Đã thu &nbsp; <b>" + MoneyUtils.format(daThu) + "</b></html>");
        lblChuThichConNo.setText("<html>&#9679; Còn nợ &nbsp; <b>" + MoneyUtils.format(conNo) + "</b></html>");
    }

    /** Biểu đồ vòng tròn vẽ tay bằng Arc2D - không dùng thư viện biểu đồ ngoài. */
    private static class DonutChart extends JPanel {
        private float tiLe = 0f; // 0.0 - 1.0

        DonutChart() {
            setOpaque(false);
        }

        void setTiLe(float tiLe) {
            this.tiLe = Math.max(0f, Math.min(1f, tiLe));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int duongKinh = Math.min(w, h) - 10;
            int x = (w - duongKinh) / 2, y = (h - duongKinh) / 2;
            int doDayVanh = Math.max(12, duongKinh / 8);

            // Vòng nền (còn nợ) - màu nhạt
            g2.setStroke(new BasicStroke(doDayVanh, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2.setColor(UITheme.TINT_RED);
            g2.draw(new Ellipse2D.Float(x, y, duongKinh, duongKinh));

            // Vòng tỷ lệ đã thu - màu xanh, bắt đầu từ 12h (90 độ), vẽ theo chiều kim đồng hồ
            float goc = tiLe * 360f;
            g2.setColor(UITheme.SUCCESS);
            g2.draw(new Arc2D.Float(x, y, duongKinh, duongKinh, 90, -goc, Arc2D.OPEN));

            g2.dispose();

            // % ở giữa
            String phanTram = Math.round(tiLe * 100) + "%";
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g3.setFont(new Font("Segoe UI", Font.BOLD, duongKinh / 5));
            g3.setColor(UITheme.TEXT_PRIMARY);
            FontMetrics fm = g3.getFontMetrics();
            int tx = w / 2 - fm.stringWidth(phanTram) / 2;
            int ty = h / 2 + fm.getAscent() / 2 - 4;
            g3.drawString(phanTram, tx, ty);
            g3.dispose();
        }
    }

    // ================== TRUY CẬP NHANH ==================

    private JPanel buildQuickActionsCard(Consumer<String> dieuHuong) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        JLabel tieuDe = new JLabel("Truy cập nhanh");
        tieuDe.setFont(UITheme.FONT_H2);
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);
        card.add(tieuDe, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 10, 10));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JButton the1 = UITheme.quickActionCard("\uD83D\uDC65", "Hồ sơ", "Sinh viên", UITheme.PRIMARY);
        JButton the2 = UITheme.quickActionCard("\uD83D\uDCC5", "Cấu hình", "Học kỳ & mức phí", UITheme.WARNING);
        JButton the3 = UITheme.quickActionCard("\uD83D\uDCB3", "Tài chính", "Thanh toán", UITheme.SUCCESS);
        JButton the4 = UITheme.quickActionCard("\u26A0", "Cảnh báo", "Công nợ", UITheme.DANGER);

        the1.addActionListener(e -> dieuHuong.accept("sinhvien"));
        the2.addActionListener(e -> dieuHuong.accept("hocky"));
        the3.addActionListener(e -> dieuHuong.accept("thanhtoan"));
        the4.addActionListener(e -> dieuHuong.accept("congno"));

        grid.add(the1);
        grid.add(the2);
        grid.add(the3);
        grid.add(the4);

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    // ================== 2 KHỐI DANH SÁCH - CÓ THỂ BẤM VÀO ==================

    private JPanel buildThongTinRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);

        JPanel congNoCard = richCard("Sinh viên còn nợ học phí", "\u26A0", UITheme.DANGER, "congno");
        khoiCongNo.setOpaque(false);
        khoiCongNo.setLayout(new BoxLayout(khoiCongNo, BoxLayout.Y_AXIS));
        congNoCard.add(khoiCongNo, BorderLayout.CENTER);

        JPanel sinhVienCard = richCard("Sinh viên mới cập nhật", "\uD83D\uDC65", UITheme.PRIMARY, "sinhvien");
        khoiSinhVienMoi.setOpaque(false);
        khoiSinhVienMoi.setLayout(new BoxLayout(khoiSinhVienMoi, BoxLayout.Y_AXIS));
        sinhVienCard.add(khoiSinhVienMoi, BorderLayout.CENTER);

        row.add(congNoCard);
        row.add(sinhVienCard);
        return row;
    }

    /** Thẻ trắng bo góc, có bóng đổ đồng bộ với UITheme.card()/statCard(), header icon + tiêu đề + link điều hướng. */
    private JLabel richCardTitleRef;

    private JPanel richCard(String tieuDe, String icon, Color mauNhan, String tabDieuHuong) {
        JPanel card = new JPanel(new BorderLayout(0, 14)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(getParent() != null ? getParent().getBackground() : UITheme.BG_MAIN);
                g2.fillRect(0, 0, w, h);
                g2.setColor(UITheme.SHADOW_TONE);
                g2.fillRoundRect(3, 4, w - 6, h - 6, 16, 16);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w - 6, h - 6, 16, 16);
                // Vạch màu nhấn mỏng phía trên
                g2.setColor(mauNhan);
                g2.fillRoundRect(0, 0, w - 6, 4, 16, 16);
                g2.fillRect(0, 2, w - 6, 2);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, w - 7, h - 7, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(true);
        card.setBackground(UITheme.BG_MAIN);
        card.setBorder(BorderFactory.createEmptyBorder(22, 22, 18, 24));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel headerTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        headerTrai.setOpaque(false);

        JLabel iconBadge = new JLabel(icon, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, mauNhan.brighter(), getWidth(), getHeight(), mauNhan);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBadge.setOpaque(false);
        iconBadge.setForeground(Color.WHITE);
        iconBadge.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconBadge.setPreferredSize(new Dimension(40, 40));

        JLabel title = new JLabel(tieuDe);
        title.setFont(UITheme.FONT_H2);
        title.setForeground(UITheme.TEXT_PRIMARY);
        title.setName("tieuDeRichCard");

        headerTrai.add(iconBadge);
        headerTrai.add(title);
        header.add(headerTrai, BorderLayout.WEST);

        JLabel lblXemTatCa = new JLabel("Xem tất cả \u2192");
        lblXemTatCa.setFont(UITheme.FONT_BOLD);
        lblXemTatCa.setForeground(mauNhan);
        lblXemTatCa.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblXemTatCa.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) { dieuHuong.accept(tabDieuHuong); }
        });
        header.add(lblXemTatCa, BorderLayout.EAST);

        card.add(header, BorderLayout.NORTH);
        return card;
    }

    /** 1 dòng thông tin: avatar tròn + tên + phụ chú, bên phải là 1 thẻ màu (pill). Bấm vào cả dòng sẽ điều hướng. */
    private static final Color[] MAU_AVATAR_XOAY_VONG = {
            new Color(0x5B, 0x4F, 0xE8), new Color(0x0D, 0x9B, 0x93), new Color(0xE1, 0x5D, 0x8F),
            new Color(0xF5, 0x9E, 0x0B), new Color(0x22, 0xA0, 0x6B), new Color(0x38, 0xBD, 0xF8)
    };
    private int demSoDongAvatar = 0;

    private JPanel richInfoRow(String ten, String phu, String giaTri, Color nenNhat, Color chuDam, String tabDieuHuong) {
        Color mauAvatar = MAU_AVATAR_XOAY_VONG[demSoDongAvatar % MAU_AVATAR_XOAY_VONG.length];
        demSoDongAvatar++;
        final boolean[] dangHover = {false};
        final int CHIEU_CAO_DONG = 62;

        JPanel row = new JPanel(new BorderLayout(14, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(dangHover[0] ? UITheme.BG_MAIN : Color.WHITE);
                g2.fillRoundRect(0, 0, w, h, 12, 12);
                if (dangHover[0]) {
                    g2.setColor(chuDam);
                    g2.fillRoundRect(0, 6, 3, h - 12, 3, 3);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 14));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        row.setPreferredSize(new Dimension(10, CHIEU_CAO_DONG));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, CHIEU_CAO_DONG));
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dieuHuong.accept(tabDieuHuong); }
            @Override public void mouseEntered(MouseEvent e) { dangHover[0] = true; row.repaint(); }
            @Override public void mouseExited(MouseEvent e) { dangHover[0] = false; row.repaint(); }
        });

        JPanel khungAvatar = new JPanel(new GridBagLayout());
        khungAvatar.setOpaque(false);
        khungAvatar.setPreferredSize(new Dimension(42, 42));
        khungAvatar.add(avatarTronMauRieng(ten, mauAvatar));
        row.add(khungAvatar, BorderLayout.WEST);

        JPanel textCol = new JPanel();
        textCol.setOpaque(false);
        textCol.setLayout(new BoxLayout(textCol, BoxLayout.Y_AXIS));
        JLabel tenLabel = new JLabel(ten);
        tenLabel.setFont(UITheme.FONT_BOLD);
        tenLabel.setForeground(UITheme.TEXT_PRIMARY);
        tenLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel phuLabel = new JLabel(phu);
        phuLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        phuLabel.setForeground(UITheme.TEXT_MUTED);
        phuLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel khungText = new JPanel(new GridBagLayout());
        khungText.setOpaque(false);
        JPanel textColCanGiua = new JPanel();
        textColCanGiua.setOpaque(false);
        textColCanGiua.setLayout(new BoxLayout(textColCanGiua, BoxLayout.Y_AXIS));
        textColCanGiua.add(tenLabel);
        textColCanGiua.add(Box.createRigidArea(new Dimension(0, 2)));
        textColCanGiua.add(phuLabel);
        khungText.add(textColCanGiua);
        row.add(khungText, BorderLayout.CENTER);

        if (giaTri != null) {
            JLabel giaTriPill = UITheme.pill(giaTri, nenNhat, chuDam);
            JPanel phaiWrap = new JPanel(new GridBagLayout());
            phaiWrap.setOpaque(false);
            phaiWrap.add(giaTriPill);
            row.add(phaiWrap, BorderLayout.EAST);
        }

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(true);
        wrapper.setBackground(Color.WHITE);
        wrapper.add(row, BorderLayout.CENTER);
        wrapper.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        wrapper.setMaximumSize(new Dimension(Integer.MAX_VALUE, CHIEU_CAO_DONG + 4));
        return wrapper;
    }

    // ================== TẢI DỮ LIỆU 2 KHỐI DANH SÁCH ==================

    private void taiDuLieuCongNo() {
        SwingWorker<List<HoaDonHocPhi>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<HoaDonHocPhi> doInBackground() throws Exception {
                return congNoService.layDanhSachConNo();
            }

            @Override
            protected void done() {
                khoiCongNo.removeAll();
                try {
                    List<HoaDonHocPhi> list = get();
                    if (list.isEmpty()) {
                        themDongTrong(khoiCongNo, "Không có sinh viên nào còn nợ học phí");
                    } else {
                        int gioiHan = Math.min(list.size(), 5);
                        for (int i = 0; i < gioiHan; i++) {
                            HoaDonHocPhi hd = list.get(i);
                            khoiCongNo.add(richInfoRow(
                                    hd.getTenSV(), "Mã SV: " + hd.getMaSV(),
                                    MoneyUtils.format(hd.tinhConNo()),
                                    UITheme.TINT_RED, UITheme.TEXT_RED, "congno"));
                        }
                        if (list.size() > gioiHan) {
                            themDongTrong(khoiCongNo, "... Và " + (list.size() - gioiHan) + " sinh viên khác");
                        }
                    }
                } catch (Exception ex) {
                    themDongTrong(khoiCongNo, "Không thể tải dữ liệu công nợ");
                }
                khoiCongNo.revalidate();
                khoiCongNo.repaint();
            }
        };
        worker.execute();
    }

    private void taiDuLieuSinhVienMoi() {
        SwingWorker<List<SinhVien>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SinhVien> doInBackground() throws Exception {
                return sinhVienService.layTatCa();
            }

            @Override
            protected void done() {
                khoiSinhVienMoi.removeAll();
                try {
                    List<SinhVien> list = get();
                    if (list.isEmpty()) {
                        themDongTrong(khoiSinhVienMoi, "Chưa có sinh viên nào trong hệ thống");
                    } else {
                        int gioiHan = Math.min(list.size(), 5);
                        for (int i = 0; i < gioiHan; i++) {
                            SinhVien sv = list.get(i);
                            khoiSinhVienMoi.add(richInfoRow(
                                    sv.getHoTen(), "Mã SV: " + sv.getMaSV(),
                                    sv.getLop(), UITheme.TINT_BLUE, UITheme.TEXT_BLUE, "sinhvien"));
                        }
                        if (list.size() > gioiHan) {
                            themDongTrong(khoiSinhVienMoi, "... Và " + (list.size() - gioiHan) + " sinh viên khác");
                        }
                    }
                } catch (Exception ex) {
                    themDongTrong(khoiSinhVienMoi, "Không thể tải danh sách sinh viên");
                }
                khoiSinhVienMoi.revalidate();
                khoiSinhVienMoi.repaint();
            }
        };
        worker.execute();
    }

    private void themDongTrong(JPanel khoi, String text) {
        themTrangThaiRong(khoi, "\u2705", text);
    }

    /** Trạng thái rỗng đẹp: icon lớn trong vòng tròn nhạt màu, tiêu đề phụ rồi đến text chính. */
    private void themTrangThaiRong(JPanel khoi, String icon, String text) {
        JPanel wrap = new JPanel();
        wrap.setOpaque(false);
        wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
        wrap.setBorder(BorderFactory.createEmptyBorder(36, 0, 36, 0));
        wrap.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel iconTron = new JLabel(icon, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_MAIN);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconTron.setOpaque(false);
        iconTron.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        iconTron.setPreferredSize(new Dimension(64, 64));
        iconTron.setMaximumSize(new Dimension(64, 64));
        iconTron.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblText = new JLabel(text, SwingConstants.CENTER);
        lblText.setFont(UITheme.FONT_BOLD);
        lblText.setForeground(UITheme.TEXT_MUTED);
        lblText.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblText.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        wrap.add(iconTron);
        wrap.add(lblText);

        JPanel outer = new JPanel(new GridBagLayout());
        outer.setOpaque(false);
        outer.add(wrap);
        khoi.add(outer);
    }

    private JLabel avatarTronMauRieng(String hoTen, Color mau) {
        String chuCai = (hoTen == null || hoTen.isBlank()) ? "?" : hoTen.trim().substring(0, 1).toUpperCase();
        JLabel avatar = new JLabel(chuCai, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, mau.brighter(), getWidth(), getHeight(), mau.darker());
                g2.setPaint(gp);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        avatar.setOpaque(false);
        avatar.setForeground(Color.WHITE);
        avatar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        avatar.setPreferredSize(new Dimension(36, 36));
        return avatar;
    }

    private JLabel timNhanTheoTen(Container container, String name) {
        for (Component c : container.getComponents()) {
            if (name.equals(c.getName()) && c instanceof JLabel) return (JLabel) c;
            if (c instanceof Container) {
                JLabel ket = timNhanTheoTen((Container) c, name);
                if (ket != null) return ket;
            }
        }
        return null;
    }
}