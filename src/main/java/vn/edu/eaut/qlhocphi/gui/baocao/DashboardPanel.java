package vn.edu.eaut.qlhocphi.gui.baocao;

import vn.edu.eaut.qlhocphi.bus.BaoCaoService;
import vn.edu.eaut.qlhocphi.bus.CongNoService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.AutoRefreshTimer;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.TrangThaiHoaDon;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Màn hình Dashboard "Thống kê & Báo cáo" - bản nâng cấp đầy đủ cho đồ án tốt
 * nghiệp: banner đồng bộ, 5 thẻ KPI riêng biệt (không nhồi chung), thẻ tỷ lệ
 * thu học phí dạng thanh tiến độ, 2 biểu đồ có nhãn giá trị/% rõ ràng, và thêm
 * mới khối "Top 5 sinh viên nợ nhiều nhất" - phần phân tích sâu mà bản cũ
 * chưa có. Toàn bộ vẽ bằng Java2D thuần, không cần thêm thư viện biểu đồ ngoài.
 */
public class DashboardPanel extends JPanel {
    private final BaoCaoService baoCaoService = new BaoCaoService();
    private final CongNoService congNoService = new CongNoService();
    private final java.util.function.BiConsumer<String, String> dieuHuongTimKiem;

    private JPanel theTongQuan;
    private JPanel theTyLeThu;
    private TienDoBar tienDoThu;
    private JLabel lblTyLeThuChuoi;
    private BarChartPanel bieuDoCot;
    private PieChartPanel bieuDoTron;
    private HinhThucPieChartPanel bieuDoHinhThuc;
    private LineChartPanel bieuDoXuHuong;
    private BarChartPanel bieuDoTheoNam;
    private JPanel khoiTopNo;
    private JButton btnXuatBaoCao;
    private JComboBox<String> cboNam;
    private JComboBox<String> cboThang;
    private JLabel lblLocMoTa;

    public DashboardPanel(java.util.function.BiConsumer<String, String> dieuHuongTimKiem) {
        this.dieuHuongTimKiem = dieuHuongTimKiem;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        add(buildHeader(), BorderLayout.NORTH);

        JPanel giua = new JPanel();
        giua.setOpaque(false);
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));
        giua.add(buildTheTongQuan());
        giua.add(Box.createRigidArea(new Dimension(0, 16)));
        giua.add(buildBoLocThangNam());
        giua.add(Box.createRigidArea(new Dimension(0, 16)));
        giua.add(buildTyLeThuCard());
        giua.add(Box.createRigidArea(new Dimension(0, 16)));
        giua.add(buildBieuDo());
        giua.add(Box.createRigidArea(new Dimension(0, 16)));
        giua.add(buildBieuDoNangCao());
        giua.add(Box.createRigidArea(new Dimension(0, 16)));
        giua.add(buildBieuDoTheoNam());
        giua.add(Box.createRigidArea(new Dimension(0, 16)));
        giua.add(buildTopNoCard());

        JScrollPane scroll = new JScrollPane(bocNgoai(giua));
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        taiDuLieu();
        // 45s vì màn hình này tính biểu đồ, nên độ tải CSDL hơn các màn còn lại.
        AutoRefreshTimer.gan(this, 45, this::taiDuLieu);
    }

    /** Bám sát chiều rộng khung cuộn, không để trống khoảng trắng lệch bên phải. */
    private JPanel bocNgoai(JPanel noiDung) {
        JPanel wrap = new KhungCuonToanChieuRong(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(noiDung, BorderLayout.NORTH);
        return wrap;
    }

    private static class KhungCuonToanChieuRong extends JPanel implements Scrollable {
        KhungCuonToanChieuRong(LayoutManager lm) { super(lm); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int huong, int dir) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int huong, int dir) { return 120; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    // ================== HEADER (banner + logo, đồng bộ các trang khác) ==================

    private JPanel buildHeader() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 90));

        JPanel trai = new JPanel(new BorderLayout(14, 0));
        trai.setOpaque(false);
        trai.add(logoBadge(), BorderLayout.WEST);

        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));
        JLabel tieuDe = new JLabel("Thống kê & Báo cáo");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Tổng quan tài chính, tỷ lệ thu và phân tích công nợ toàn trường");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);

        btnXuatBaoCao = new JButton("Xuất báo cáo");
        btnXuatBaoCao.setFont(UITheme.FONT_BOLD);
        btnXuatBaoCao.setBackground(Color.WHITE);
        btnXuatBaoCao.setForeground(UITheme.PRIMARY_DARK);
        btnXuatBaoCao.setFocusPainted(false);
        btnXuatBaoCao.setBorderPainted(false);
        btnXuatBaoCao.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnXuatBaoCao.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXuatBaoCao.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            ExportReportDialog dialog = new ExportReportDialog((Frame) owner);
            dialog.setVisible(true);
        });
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(btnXuatBaoCao);
        banner.add(actions, BorderLayout.EAST);

        return banner;
    }

    private JComponent logoBadge() {
        JComponent badge = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 55));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
                FontMetrics fm = g2.getFontMetrics();
                String icon = "\uD83D\uDCCA";
                int x = (getWidth() - fm.stringWidth(icon)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(icon, x, y);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(52, 52));
        badge.setOpaque(false);
        return badge;
    }

    // ================== 5 THẺ KPI RIÊNG BIỆT (không nhồi chung như bản cũ) ==================

    private JPanel buildTheTongQuan() {
        theTongQuan = new JPanel(new GridLayout(1, 5, 14, 0));
        theTongQuan.setOpaque(false);
        theTongQuan.add(UITheme.statCard("Tổng số hóa đơn", "0", UITheme.TINT_BLUE, UITheme.TEXT_BLUE));
        theTongQuan.add(UITheme.statCard("Tổng học phí", "0 đ", UITheme.TINT_VIOLET, UITheme.TEXT_VIOLET));
        theTongQuan.add(UITheme.statCard("Đã thu", "0 đ", UITheme.TINT_GREEN, UITheme.TEXT_GREEN));
        theTongQuan.add(UITheme.statCard("Còn nợ", "0 đ", UITheme.TINT_RED, UITheme.TEXT_RED));
        theTongQuan.add(UITheme.statCard("Hóa đơn quá hạn", "0", UITheme.TINT_RED, UITheme.TEXT_RED));
        return theTongQuan;
    }

    private void capNhatTheTongQuan(BaoCaoService.BaoCaoTongQuan tq) {
        capNhatGiaTriThe(theTongQuan, 0, String.valueOf(tq.getTongSoHoaDon()));
        capNhatGiaTriThe(theTongQuan, 1, MoneyUtils.format(tq.getTongHocPhi()));
        capNhatGiaTriThe(theTongQuan, 2, MoneyUtils.format(tq.getTongDaThu()));
        capNhatGiaTriThe(theTongQuan, 3, MoneyUtils.format(tq.getTongConNo()));
        capNhatGiaTriThe(theTongQuan, 4, String.valueOf(tq.getSoHoaDonQuaHan()));
    }

    private void capNhatGiaTriThe(JPanel row, int index, String giaTriMoi) {
        JPanel the = (JPanel) row.getComponent(index);
        for (Component c : the.getComponents()) {
            if (c instanceof JLabel && "giaTri".equals(c.getName())) ((JLabel) c).setText(giaTriMoi);
        }
    }

    // ================== TỶ LỆ THU HỌC PHÍ (thanh tiến độ) ==================

    private JPanel buildTyLeThuCard() {
        theTyLeThu = UITheme.card();
        theTyLeThu.setLayout(new BorderLayout(0, 10));

        JPanel dongTren = new JPanel(new BorderLayout());
        dongTren.setOpaque(false);
        JLabel lblTieuDe = new JLabel("Tỷ lệ thu học phí toàn trường");
        lblTieuDe.setFont(UITheme.FONT_BASE);
        lblTieuDe.setForeground(UITheme.TEXT_PRIMARY);
        dongTren.add(lblTieuDe, BorderLayout.WEST);

        lblTyLeThuChuoi = new JLabel("0%  (0 đ / 0 đ)");
        lblTyLeThuChuoi.setFont(UITheme.FONT_BOLD);
        lblTyLeThuChuoi.setForeground(UITheme.TEXT_MUTED);
        dongTren.add(lblTyLeThuChuoi, BorderLayout.EAST);
        theTyLeThu.add(dongTren, BorderLayout.NORTH);

        tienDoThu = new TienDoBar();
        tienDoThu.setPreferredSize(new Dimension(10, 12));
        theTyLeThu.add(tienDoThu, BorderLayout.CENTER);

        return theTyLeThu;
    }

    /** Thanh tiến độ bo tròn vẽ tay - dùng lại kiểu đã làm ở SinhVienCongNoPanel. */
    private static class TienDoBar extends JComponent {
        private int phanTram = 0;
        void setPhanTram(int p) { this.phanTram = p; repaint(); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int h = getHeight();
            g2.setColor(UITheme.BORDER);
            g2.fillRoundRect(0, 0, getWidth(), h, h, h);
            int w = (int) (getWidth() * (phanTram / 100.0));
            if (w > 0) {
                g2.setColor(phanTram >= 80 ? UITheme.SUCCESS : phanTram >= 40 ? UITheme.WARNING : UITheme.DANGER);
                g2.fillRoundRect(0, 0, Math.max(w, h), h, h, h);
            }
            g2.dispose();
        }
    }

    // ================== 2 BIỂU ĐỒ (có nhãn giá trị/%) ==================

    private JPanel buildBieuDo() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(10, 300));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JPanel cardCot = UITheme.card();
        cardCot.setLayout(new BorderLayout(0, 8));
        cardCot.add(UITheme.sectionLabel("Tổng thu theo học kỳ"), BorderLayout.NORTH);
        bieuDoCot = new BarChartPanel();
        cardCot.add(bieuDoCot, BorderLayout.CENTER);
        row.add(cardCot);

        JPanel cardTron = UITheme.card();
        cardTron.setLayout(new BorderLayout(0, 8));
        cardTron.add(UITheme.sectionLabel("Trạng thái hóa đơn"), BorderLayout.NORTH);
        bieuDoTron = new PieChartPanel();
        cardTron.add(bieuDoTron, BorderLayout.CENTER);
        row.add(cardTron);

        return row;
    }

    /** Hàng biểu đồ thứ 2 (mới thêm): tỷ lệ theo hình thức thanh toán + xu hướng thu theo tháng. */
    private JPanel buildBieuDoNangCao() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(10, 300));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JPanel cardHinhThuc = UITheme.card();
        cardHinhThuc.setLayout(new BorderLayout(0, 8));
        cardHinhThuc.add(UITheme.sectionLabel("Tỷ lệ theo hình thức thanh toán"), BorderLayout.NORTH);
        bieuDoHinhThuc = new HinhThucPieChartPanel();
        cardHinhThuc.add(bieuDoHinhThuc, BorderLayout.CENTER);
        row.add(cardHinhThuc);

        JPanel cardXuHuong = UITheme.card();
        cardXuHuong.setLayout(new BorderLayout(0, 8));
        cardXuHuong.add(UITheme.sectionLabel("Thu theo tháng (chọn năm để xem T1–T12)"), BorderLayout.NORTH);
        bieuDoXuHuong = new LineChartPanel();
        cardXuHuong.add(bieuDoXuHuong, BorderLayout.CENTER);
        row.add(cardXuHuong);

        return row;
    }

    /** Hàng biểu đồ năm – tổng thu từng năm. */
    private JPanel buildBieuDoTheoNam() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 8));
        card.setPreferredSize(new Dimension(10, 280));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        card.add(UITheme.sectionLabel("Tổng thu theo năm (5 năm vận hành)"), BorderLayout.NORTH);
        bieuDoTheoNam = new BarChartPanel();
        card.add(bieuDoTheoNam, BorderLayout.CENTER);
        return card;
    }

    /** Thanh lọc Năm / Tháng – Phòng Đào Tạo chọn để xem biểu đồ. */
    private JPanel buildBoLocThangNam() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(12, 8));

        JLabel tieuDe = new JLabel("Báo cáo 5 năm vận hành – theo tháng / năm");
        tieuDe.setFont(UITheme.FONT_BOLD);
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);

        JPanel loc = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        loc.setOpaque(false);

        loc.add(new JLabel("Năm:"));
        cboNam = new JComboBox<>();
        cboNam.addItem("Tất cả năm");
        cboNam.setPreferredSize(new Dimension(120, 34));
        cboNam.setFont(UITheme.FONT_BASE);
        loc.add(cboNam);

        loc.add(new JLabel("Tháng:"));
        cboThang = new JComboBox<>();
        cboThang.addItem("Tất cả tháng");
        for (int m = 1; m <= 12; m++) cboThang.addItem("Tháng " + m);
        cboThang.setPreferredSize(new Dimension(130, 34));
        cboThang.setFont(UITheme.FONT_BASE);
        loc.add(cboThang);

        JButton btnXem = UITheme.primaryButton("Xem báo cáo");
        btnXem.addActionListener(e -> taiBieuDoTheoLoc());
        loc.add(btnXem);

        JButton btnReset = UITheme.secondaryButton("Tất cả");
        btnReset.addActionListener(e -> {
            cboNam.setSelectedIndex(0);
            cboThang.setSelectedIndex(0);
            taiBieuDoTheoLoc();
        });
        loc.add(btnReset);

        lblLocMoTa = new JLabel("Đang xem: 5 năm vận hành");
        lblLocMoTa.setFont(UITheme.FONT_BASE);
        lblLocMoTa.setForeground(UITheme.TEXT_MUTED);

        JPanel north = new JPanel(new BorderLayout());
        north.setOpaque(false);
        north.add(tieuDe, BorderLayout.WEST);
        north.add(lblLocMoTa, BorderLayout.EAST);

        card.add(north, BorderLayout.NORTH);
        card.add(loc, BorderLayout.CENTER);
        return card;
    }

    // ================== TOP 5 SINH VIÊN NỢ NHIỀU NHẤT (mới hoàn toàn) ==================

    private JPanel buildTopNoCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UITheme.sectionLabel("Top 5 sinh viên còn nợ học phí nhiều nhất"), BorderLayout.NORTH);

        khoiTopNo = new JPanel();
        khoiTopNo.setOpaque(false);
        khoiTopNo.setLayout(new BoxLayout(khoiTopNo, BoxLayout.Y_AXIS));
        card.add(khoiTopNo, BorderLayout.CENTER);

        return card;
    }

    private void capNhatTopNo(List<HoaDonHocPhi> danhSachNo) {
        khoiTopNo.removeAll();
        List<HoaDonHocPhi> top5 = danhSachNo.stream()
                .sorted(Comparator.comparing(HoaDonHocPhi::tinhConNo).reversed())
                .limit(5)
                .toList();

        if (top5.isEmpty()) {
            JLabel trong = new JLabel("Không có sinh viên nào còn nợ học phí");
            trong.setFont(UITheme.FONT_BASE);
            trong.setForeground(UITheme.TEXT_MUTED);
            khoiTopNo.add(trong);
        } else {
            BigDecimal noCaoNhat = top5.get(0).tinhConNo();
            for (HoaDonHocPhi hd : top5) {
                khoiTopNo.add(dongTopNo(hd, noCaoNhat));
            }
        }
        khoiTopNo.revalidate();
        khoiTopNo.repaint();
    }

    /** 1 dòng: avatar + tên + học kỳ, thanh mini thể hiện tỷ lệ so với người nợ cao nhất, số tiền dạng pill đỏ. */
    private JPanel dongTopNo(HoaDonHocPhi hd, BigDecimal noCaoNhat) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (dieuHuongTimKiem != null) dieuHuongTimKiem.accept("congno", hd.getMaSV());
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) { row.setBackground(new Color(0xF7,0xF9,0xFC)); row.setOpaque(true); row.repaint(); }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) { row.setOpaque(false); row.repaint(); }
        });
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF0, 0xF2, 0xF6)),
                BorderFactory.createEmptyBorder(10, 4, 10, 4)));

        JPanel trai = new JPanel(new BorderLayout(12, 0));
        trai.setOpaque(false);
        trai.add(UITheme.avatarTron(hd.getTenSV()), BorderLayout.WEST);

        JPanel giua = new JPanel();
        giua.setOpaque(false);
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));
        JLabel lblTen = new JLabel(hd.getTenSV() + "  (" + hd.getMaSV() + ")");
        lblTen.setFont(UITheme.FONT_BOLD);
        lblTen.setForeground(UITheme.TEXT_PRIMARY);
        lblTen.setAlignmentX(Component.LEFT_ALIGNMENT);

        double tiLe = noCaoNhat.compareTo(BigDecimal.ZERO) > 0
                ? hd.tinhConNo().doubleValue() / noCaoNhat.doubleValue() : 0;
        MiniBar miniBar = new MiniBar(tiLe);
        miniBar.setPreferredSize(new Dimension(180, 8));
        miniBar.setMaximumSize(new Dimension(220, 8));
        miniBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        giua.add(lblTen);
        giua.add(Box.createRigidArea(new Dimension(0, 5)));
        giua.add(miniBar);
        trai.add(giua, BorderLayout.CENTER);
        row.add(trai, BorderLayout.CENTER);

        JLabel pill = UITheme.pill(MoneyUtils.format(hd.tinhConNo()), UITheme.TINT_RED, UITheme.TEXT_RED);
        JPanel phaiWrap = new JPanel(new GridBagLayout());
        phaiWrap.setOpaque(false);
        phaiWrap.add(pill);
        row.add(phaiWrap, BorderLayout.EAST);

        return row;
    }

    private static class MiniBar extends JComponent {
        private final double tiLe;
        MiniBar(double tiLe) { this.tiLe = Math.max(0, Math.min(1, tiLe)); setOpaque(false); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int h = getHeight();
            g2.setColor(UITheme.BORDER);
            g2.fillRoundRect(0, 0, getWidth(), h, h, h);
            int w = (int) (getWidth() * tiLe);
            if (w > 0) {
                g2.setColor(UITheme.DANGER);
                g2.fillRoundRect(0, 0, Math.max(w, h), h, h, h);
            }
            g2.dispose();
        }
    }

    // ================== TẢI DỮ LIỆU ==================

    private void taiDuLieu() {
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                BaoCaoService.BaoCaoTongQuan tongQuan = baoCaoService.layTongQuan();
                Map<String, BigDecimal> thuTheoHocKy = baoCaoService.thongKeThuTheoHocKy();
                Map<TrangThaiHoaDon, Long> theoTrangThai = baoCaoService.thongKeSoLuongTheoTrangThai();
                List<HoaDonHocPhi> danhSachNo = congNoService.layDanhSachConNo();
                Map<String, BigDecimal> thuTheoHinhThuc = baoCaoService.thongKeThuTheoHinhThuc();
                Map<String, BigDecimal> thuTheoThang = baoCaoService.thongKeThuTheoThang();
                Map<String, BigDecimal> thuTheoNam = baoCaoService.thongKeThuTheoNam();
                List<Integer> dsNam = baoCaoService.layDanhSachNamCoDuLieu();
                return new Object[]{tongQuan, thuTheoHocKy, theoTrangThai, danhSachNo, thuTheoHinhThuc, thuTheoThang, thuTheoNam, dsNam};
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] ketQua = get();
                    BaoCaoService.BaoCaoTongQuan tongQuan = (BaoCaoService.BaoCaoTongQuan) ketQua[0];
                    Map<String, BigDecimal> thuTheoHocKy = (Map<String, BigDecimal>) ketQua[1];
                    Map<TrangThaiHoaDon, Long> theoTrangThai = (Map<TrangThaiHoaDon, Long>) ketQua[2];
                    List<HoaDonHocPhi> danhSachNo = (List<HoaDonHocPhi>) ketQua[3];
                    Map<String, BigDecimal> thuTheoHinhThuc = (Map<String, BigDecimal>) ketQua[4];
                    Map<String, BigDecimal> thuTheoThang = (Map<String, BigDecimal>) ketQua[5];
                    Map<String, BigDecimal> thuTheoNam = (Map<String, BigDecimal>) ketQua[6];
                    List<Integer> dsNam = (List<Integer>) ketQua[7];

                    capNhatTheTongQuan(tongQuan);
                    capNhatTyLeThu(tongQuan);
                    bieuDoCot.setDuLieu(thuTheoHocKy);
                    bieuDoTron.setDuLieu(theoTrangThai);
                    bieuDoHinhThuc.setDuLieu(thuTheoHinhThuc);
                    bieuDoXuHuong.setDuLieu(thuTheoThang);
                    if (bieuDoTheoNam != null) bieuDoTheoNam.setDuLieu(thuTheoNam);
                    capNhatTopNo(danhSachNo);
                    napComboNam(dsNam);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(DashboardPanel.this, "Không thể tải dữ liệu thống kê.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void capNhatTyLeThu(BaoCaoService.BaoCaoTongQuan tq) {
        BigDecimal tongHocPhi = tq.getTongHocPhi();
        int phanTram = tongHocPhi != null && tongHocPhi.compareTo(BigDecimal.ZERO) > 0
                ? tq.getTongDaThu().multiply(BigDecimal.valueOf(100))
                .divide(tongHocPhi, 0, RoundingMode.HALF_UP).intValue()
                : 0;
        tienDoThu.setPhanTram(Math.min(100, phanTram));
        lblTyLeThuChuoi.setText(phanTram + "%   (" + MoneyUtils.format(tq.getTongDaThu())
                + " / " + MoneyUtils.format(tongHocPhi) + ")");
    }

    private void napComboNam(List<Integer> dsNam) {
        if (cboNam == null) return;
        String chon = (String) cboNam.getSelectedItem();
        cboNam.removeAllItems();
        cboNam.addItem("Tất cả năm (5 năm)");
        // Luôn đủ 5 năm vận hành (kể cả năm chưa có dữ liệu)
        List<Integer> namVH = dsNam != null && !dsNam.isEmpty()
                ? dsNam
                : baoCaoService.layDanhSachNamVanHanh();
        for (Integer n : namVH) cboNam.addItem(String.valueOf(n));
        if (chon != null) cboNam.setSelectedItem(chon);
        else cboNam.setSelectedIndex(0);
    }

    /** Tải lại biểu đồ tháng / năm theo bộ lọc (SwingWorker). */
    private void taiBieuDoTheoLoc() {
        // Gán biến thường trước, rồi copy sang final cho SwingWorker (tránh lỗi "might already have been assigned")
        Integer namTmp = null;
        String sNam = cboNam != null ? (String) cboNam.getSelectedItem() : "Tất cả năm";
        if (sNam != null && !sNam.startsWith("Tất cả")) {
            try { namTmp = Integer.parseInt(sNam.trim()); } catch (NumberFormatException ignored) { namTmp = null; }
        }
        int thangTmp = 0;
        String sThang = cboThang != null ? (String) cboThang.getSelectedItem() : "Tất cả tháng";
        if (sThang != null && !sThang.startsWith("Tất cả")) {
            try { thangTmp = Integer.parseInt(sThang.replace("Tháng ", "").trim()); }
            catch (NumberFormatException ignored) { thangTmp = 0; }
        }
        final Integer nam = namTmp;
        final int thang = thangTmp;

        SwingWorker<Map<String, BigDecimal>[], Void> w = new SwingWorker<>() {
            @Override
            protected Map<String, BigDecimal>[] doInBackground() throws Exception {
                Map<String, BigDecimal> theoThang = baoCaoService.thongKeThuTheoThang(nam);
                // Nếu chọn 1 tháng cụ thể trong năm → chỉ giữ tháng đó
                if (nam != null && thang >= 1 && thang <= 12) {
                    String key = "T" + thang;
                    Map<String, BigDecimal> one = new LinkedHashMap<>();
                    one.put(key, theoThang.getOrDefault(key, BigDecimal.ZERO));
                    theoThang = one;
                }
                Map<String, BigDecimal> theoNam = baoCaoService.thongKeThuTheoNam();
                @SuppressWarnings("unchecked")
                Map<String, BigDecimal>[] arr = new Map[]{theoThang, theoNam};
                return arr;
            }

            @Override
            protected void done() {
                try {
                    Map<String, BigDecimal>[] arr = get();
                    bieuDoXuHuong.setDuLieu(arr[0]);
                    if (bieuDoTheoNam != null) bieuDoTheoNam.setDuLieu(arr[1]);
                    String moTa;
                    if (nam == null) moTa = "Đang xem: 5 năm vận hành (theo tháng)";
                    else if (thang == 0) moTa = "Đang xem: cả năm " + nam + " (12 tháng)";
                    else moTa = "Đang xem: tháng " + thang + "/" + nam;
                    if (lblLocMoTa != null) lblLocMoTa.setText(moTa);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(DashboardPanel.this, rootMessage(ex));
                }
            }
        };
        w.execute();
    }

    private String rootMessage(Exception ex) {

        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }

    // ================== BIỂU ĐỒ CỘT (có thêm nhãn giá trị trên đỉnh cột) ==================

    private static class BarChartPanel extends JPanel {
        private Map<String, BigDecimal> duLieu = new LinkedHashMap<>();

        BarChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(400, 260));
        }

        void setDuLieu(Map<String, BigDecimal> duLieu) {
            this.duLieu = duLieu == null ? new LinkedHashMap<>() : duLieu;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int leTrai = 20, leDuoi = 46, leTren = 30, lePhai = 16;
            int rongVe = w - leTrai - lePhai;
            int caoVe = h - leTren - leDuoi;
            if (rongVe <= 0 || caoVe <= 0) return;

            if (duLieu.isEmpty()) {
                veChuThongBao(g2, "Chưa có dữ liệu");
                return;
            }

            BigDecimal max = duLieu.values().stream().reduce(BigDecimal.ZERO, BigDecimal::max);
            if (max.compareTo(BigDecimal.ZERO) == 0) max = BigDecimal.ONE;

            g2.setColor(UITheme.BORDER);
            g2.drawLine(leTrai, leTren + caoVe, leTrai + rongVe, leTren + caoVe);

            int soCot = duLieu.size();
            int rongMoiCot = rongVe / soCot;
            int rongThanhCot = Math.max(18, Math.min(60, rongMoiCot - 24));

            g2.setFont(UITheme.FONT_BASE.deriveFont(11f));
            int i = 0;
            for (Map.Entry<String, BigDecimal> e : duLieu.entrySet()) {
                double tiLe = e.getValue().doubleValue() / max.doubleValue();
                int caoThanh = (int) Math.round(tiLe * (caoVe - 20));
                int x = leTrai + i * rongMoiCot + (rongMoiCot - rongThanhCot) / 2;
                int y = leTren + caoVe - caoThanh;

                // Bảng màu học thuật – không trùng, chuẩn hệ thống quản lý
                Color[] palette = {
                        new Color(0x1D, 0x4E, 0xD8), // indigo
                        new Color(0x0E, 0xA5, 0xE9), // sky
                        new Color(0x10, 0xB9, 0x81), // emerald
                        new Color(0xF5, 0x9E, 0x0B), // amber
                        new Color(0x8B, 0x5C, 0xF6), // violet
                        new Color(0xEF, 0x44, 0x44), // red soft
                        new Color(0x14, 0xB8, 0xA6), // teal
                        new Color(0x63, 0x66, 0xF1)  // indigo light
                };
                g2.setColor(palette[i % palette.length]);
                g2.fillRoundRect(x, y, rongThanhCot, caoThanh, 6, 6);

                String nhanGiaTri = MoneyUtils.format(e.getValue());
                FontMetrics fmGT = g2.getFontMetrics();
                int wGT = fmGT.stringWidth(nhanGiaTri);
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.drawString(nhanGiaTri, x + (rongThanhCot - wGT) / 2, Math.max(leTren - 6, y - 6));

                String nhan = e.getKey();
                int wNhan = fmGT.stringWidth(nhan);
                g2.setColor(UITheme.TEXT_MUTED);
                g2.drawString(nhan, leTrai + i * rongMoiCot + (rongMoiCot - wNhan) / 2, leTren + caoVe + 18);
                i++;
            }
        }

        private void veChuThongBao(Graphics2D g2, String text) {
            g2.setColor(UITheme.TEXT_MUTED);
            g2.setFont(UITheme.FONT_BASE);
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(text, (getWidth() - fm.stringWidth(text)) / 2, getHeight() / 2);
        }
    }

    // ================== BIỂU ĐỒ TRÒN (có thêm % trong chú thích) ==================

    private static class PieChartPanel extends JPanel {
        private Map<TrangThaiHoaDon, Long> duLieu = new LinkedHashMap<>();

        PieChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(400, 260));
        }

        void setDuLieu(Map<TrangThaiHoaDon, Long> duLieu) {
            this.duLieu = duLieu == null ? new LinkedHashMap<>() : duLieu;
            repaint();
        }

        private Color mauCuaTrangThai(TrangThaiHoaDon tt) {
            switch (tt) {
                case DA_DONG_DU: return new Color(0x10, 0xB9, 0x81);
                case DONG_MOT_PHAN: return new Color(0xF5, 0x9E, 0x0B);
                case QUA_HAN: return new Color(0xEF, 0x44, 0x44);
                case CHUA_DONG: return new Color(0x64, 0x74, 0x8B);
                default: return new Color(0x94, 0xA3, 0xB8);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            long tong = duLieu.values().stream().mapToLong(Long::longValue).sum();
            int w = getWidth(), h = getHeight();

            if (tong == 0) {
                g2.setColor(UITheme.TEXT_MUTED);
                g2.setFont(UITheme.FONT_BASE);
                String text = "Chưa có dữ liệu";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, (w - fm.stringWidth(text)) / 2, h / 2);
                return;
            }

            int duongKinh = Math.min(w, h) - 40;
            int x = (w - duongKinh) / 2 - 60;
            int y = (h - duongKinh) / 2;
            if (x < 10) x = 10;

            double goc = 90;
            for (Map.Entry<TrangThaiHoaDon, Long> e : duLieu.entrySet()) {
                double phan = 360.0 * e.getValue() / tong;
                g2.setColor(mauCuaTrangThai(e.getKey()));
                g2.fill(new Arc2D.Double(x, y, duongKinh, duongKinh, goc, -phan, Arc2D.PIE));
                goc -= phan;
            }

            int chuThichX = x + duongKinh + 30;
            int chuThichY = y + 10;
            g2.setFont(UITheme.FONT_BASE.deriveFont(12f));
            for (Map.Entry<TrangThaiHoaDon, Long> e : duLieu.entrySet()) {
                double phanTram = 100.0 * e.getValue() / tong;
                g2.setColor(mauCuaTrangThai(e.getKey()));
                g2.fillRect(chuThichX, chuThichY, 12, 12);
                g2.setColor(UITheme.TEXT_PRIMARY);
                String nhan = String.format("%s (%d - %.0f%%)", e.getKey().getNhan(), e.getValue(), phanTram);
                g2.drawString(nhan, chuThichX + 18, chuThichY + 11);
                chuThichY += 22;
            }
        }
    }

    // ================== BIỂU ĐỒ TRÒN THEO HÌNH THỨC THANH TOÁN (mới thêm) ==================

    private static class HinhThucPieChartPanel extends JPanel {
        private Map<String, BigDecimal> duLieu = new LinkedHashMap<>();

        HinhThucPieChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(400, 260));
        }

        void setDuLieu(Map<String, BigDecimal> duLieu) {
            this.duLieu = duLieu == null ? new LinkedHashMap<>() : duLieu;
            repaint();
        }

        private Color mauTheoHinhThuc(String hinhThuc) {
            switch (hinhThuc) {
                case "TIEN_MAT": return new Color(0x10, 0xB9, 0x81);
                case "CHUYEN_KHOAN": return new Color(0x1D, 0x4E, 0xD8);
                case "THANH_TOAN_ONLINE": return new Color(0xF5, 0x9E, 0x0B);
                case "VI_DIEN_TU": return new Color(0x8B, 0x5C, 0xF6);
                default: return new Color(0x94, 0xA3, 0xB8);
            }
        }

        private String tenHienThi(String hinhThuc) {
            switch (hinhThuc) {
                case "TIEN_MAT": return "Tiền mặt";
                case "CHUYEN_KHOAN": return "Chuyển khoản";
                case "THANH_TOAN_ONLINE": return "Online (VNPay/MoMo)";
                case "VI_DIEN_TU": return "Ví điện tử";
                default: return hinhThuc;
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            BigDecimal tong = duLieu.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            int w = getWidth(), h = getHeight();

            if (tong.compareTo(BigDecimal.ZERO) == 0) {
                g2.setColor(UITheme.TEXT_MUTED);
                g2.setFont(UITheme.FONT_BASE);
                String text = "Chưa có dữ liệu";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, (w - fm.stringWidth(text)) / 2, h / 2);
                return;
            }

            int duongKinh = Math.min(w, h) - 40;
            int x = (w - duongKinh) / 2 - 60;
            int y = (h - duongKinh) / 2;
            if (x < 10) x = 10;

            double goc = 90;
            for (Map.Entry<String, BigDecimal> e : duLieu.entrySet()) {
                double phan = 360.0 * e.getValue().doubleValue() / tong.doubleValue();
                g2.setColor(mauTheoHinhThuc(e.getKey()));
                g2.fill(new Arc2D.Double(x, y, duongKinh, duongKinh, goc, -phan, Arc2D.PIE));
                goc -= phan;
            }

            int chuThichX = x + duongKinh + 30;
            int chuThichY = y + 10;
            g2.setFont(UITheme.FONT_BASE.deriveFont(12f));
            for (Map.Entry<String, BigDecimal> e : duLieu.entrySet()) {
                double phanTram = 100.0 * e.getValue().doubleValue() / tong.doubleValue();
                g2.setColor(mauTheoHinhThuc(e.getKey()));
                g2.fillRect(chuThichX, chuThichY, 12, 12);
                g2.setColor(UITheme.TEXT_PRIMARY);
                String nhan = String.format("%s (%.0f%%)", tenHienThi(e.getKey()), phanTram);
                g2.drawString(nhan, chuThichX + 18, chuThichY + 11);
                chuThichY += 22;
            }
        }
    }

    // ================== BIỂU ĐỒ ĐƯỜNG XU HƯỚNG THU THEO THÁNG (mới thêm) ==================

    private static class LineChartPanel extends JPanel {
        private Map<String, BigDecimal> duLieu = new LinkedHashMap<>();

        LineChartPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(400, 260));
        }

        void setDuLieu(Map<String, BigDecimal> duLieu) {
            this.duLieu = duLieu == null ? new LinkedHashMap<>() : duLieu;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int leTrai = 70, leDuoi = 40, leTren = 30, lePhai = 20;
            int rongVe = w - leTrai - lePhai;
            int caoVe = h - leTren - leDuoi;
            if (rongVe <= 0 || caoVe <= 0) return;

            if (duLieu.isEmpty()) {
                g2.setColor(UITheme.TEXT_MUTED);
                g2.setFont(UITheme.FONT_BASE);
                String text = "Chưa có dữ liệu";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(text, (w - fm.stringWidth(text)) / 2, h / 2);
                return;
            }

            BigDecimal max = duLieu.values().stream().reduce(BigDecimal.ZERO, BigDecimal::max);
            if (max.compareTo(BigDecimal.ZERO) == 0) max = BigDecimal.ONE;

            g2.setColor(UITheme.BORDER);
            g2.drawLine(leTrai, leTren + caoVe, leTrai + rongVe, leTren + caoVe);
            g2.drawLine(leTrai, leTren, leTrai, leTren + caoVe);

            int soDiem = duLieu.size();
            int khoangCach = soDiem > 1 ? rongVe / (soDiem - 1) : 0;

            int[] xs = new int[soDiem];
            int[] ys = new int[soDiem];
            String[] nhans = duLieu.keySet().toArray(new String[0]);
            BigDecimal[] giaTris = duLieu.values().toArray(new BigDecimal[0]);

            g2.setFont(UITheme.FONT_BASE.deriveFont(10f));
            for (int i = 0; i < soDiem; i++) {
                double tiLe = giaTris[i].doubleValue() / max.doubleValue();
                xs[i] = soDiem == 1 ? leTrai + rongVe / 2 : leTrai + i * khoangCach;
                ys[i] = leTren + caoVe - (int) Math.round(tiLe * caoVe);

                FontMetrics fm = g2.getFontMetrics();
                String nhan = nhans[i];
                int wNhan = fm.stringWidth(nhan);
                g2.setColor(UITheme.TEXT_MUTED);
                g2.drawString(nhan, xs[i] - wNhan / 2, leTren + caoVe + 16);
            }

            g2.setColor(new Color(0x0E, 0xA5, 0xE9)); // sky
            g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (int i = 0; i < soDiem - 1; i++) {
                g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
            }

            for (int i = 0; i < soDiem; i++) {
                g2.setColor(UITheme.PRIMARY);
                g2.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
                g2.setColor(Color.WHITE);
                g2.fillOval(xs[i] - 2, ys[i] - 2, 4, 4);

                String nhanGiaTri = MoneyUtils.format(giaTris[i]);
                FontMetrics fm = g2.getFontMetrics();
                int wGT = fm.stringWidth(nhanGiaTri);
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.drawString(nhanGiaTri, xs[i] - wGT / 2, ys[i] - 10);
            }
        }
    }
}