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
 * Thống kê & Báo cáo – khung 5 năm vận hành.
 * Lọc năm/tháng → biểu đồ doanh thu từ phiếu thu sinh viên đóng học phí.
 */
public class DashboardPanel extends JPanel {
    private final BaoCaoService baoCaoService = new BaoCaoService();
    private final CongNoService congNoService = new CongNoService();
    private final java.util.function.BiConsumer<String, String> dieuHuongTimKiem;

    private JPanel theTongQuan;
    private TienDoBar tienDoThu;
    private JLabel lblTyLeThuChuoi;
    private BarChartPanel bieuDoCot;
    private PieChartPanel bieuDoTron;
    private HinhThucPieChartPanel bieuDoHinhThuc;
    private BarChartPanel bieuDoThang;      // T1–T12 hoặc tháng có dữ liệu
    private BarChartPanel bieuDoTheoNam;   // 5 năm
    private JLabel lblTieuDeThang;
    private JPanel khoiTopNo;
    private JComboBox<String> cboNam;
    private JComboBox<String> cboThang;
    private JLabel lblLocMoTa;

    private static final Color[] PALETTE = {
            new Color(0x1D, 0x4E, 0xD8), new Color(0x0E, 0xA5, 0xE9),
            new Color(0x10, 0xB9, 0x81), new Color(0xF5, 0x9E, 0x0B),
            new Color(0x8B, 0x5C, 0xF6), new Color(0xEF, 0x44, 0x44),
            new Color(0x14, 0xB8, 0xA6), new Color(0x63, 0x66, 0xF1),
            new Color(0xF9, 0x73, 0x16), new Color(0x06, 0xB6, 0xD4),
            new Color(0x84, 0xCC, 0x16), new Color(0xEC, 0x48, 0x99)
    };

    public DashboardPanel(java.util.function.BiConsumer<String, String> dieuHuongTimKiem) {
        this.dieuHuongTimKiem = dieuHuongTimKiem;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        add(buildHeader(), BorderLayout.NORTH);

        JPanel giua = new JPanel();
        giua.setOpaque(false);
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));
        giua.add(buildTheTongQuan());
        giua.add(Box.createRigidArea(new Dimension(0, 14)));
        giua.add(buildBoLocThangNam());
        giua.add(Box.createRigidArea(new Dimension(0, 14)));
        giua.add(buildTyLeThuCard());
        giua.add(Box.createRigidArea(new Dimension(0, 14)));
        giua.add(buildBieuDo());
        giua.add(Box.createRigidArea(new Dimension(0, 14)));
        giua.add(buildBieuDoNangCao());
        giua.add(Box.createRigidArea(new Dimension(0, 14)));
        giua.add(buildBieuDoTheoNamVaThang());
        giua.add(Box.createRigidArea(new Dimension(0, 14)));
        giua.add(buildTopNoCard());

        JScrollPane scroll = new JScrollPane(bocNgoai(giua));
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        taiDuLieu();
        AutoRefreshTimer.gan(this, 45, this::taiDuLieu);
    }

    private JPanel bocNgoai(JPanel noiDung) {
        JPanel outer = new KhungCuon(new BorderLayout());
        outer.setOpaque(false);
        outer.add(noiDung, BorderLayout.NORTH);
        return outer;
    }

    private static class KhungCuon extends JPanel implements Scrollable {
        KhungCuon(LayoutManager lm) { super(lm); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 120; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }

    private JPanel buildHeader() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 90));

        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));
        JLabel tieuDe = new JLabel("Thống kê & Báo cáo (5 năm vận hành)");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Doanh thu học phí theo tháng / năm – dữ liệu từ phiếu thu sinh viên");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        banner.add(chuText, BorderLayout.WEST);

        JButton btnXuat = new JButton("Xuất báo cáo");
        btnXuat.setFont(UITheme.FONT_BOLD);
        btnXuat.setBackground(Color.WHITE);
        btnXuat.setForeground(UITheme.PRIMARY_DARK);
        btnXuat.setFocusPainted(false);
        btnXuat.setBorderPainted(false);
        btnXuat.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnXuat.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnXuat.addActionListener(e -> {
            Window owner = SwingUtilities.getWindowAncestor(this);
            new ExportReportDialog((Frame) owner).setVisible(true);
        });
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(btnXuat);
        banner.add(actions, BorderLayout.EAST);
        return banner;
    }

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
        capNhatGiaTriThe(0, String.valueOf(tq.getTongSoHoaDon()));
        capNhatGiaTriThe(1, MoneyUtils.format(tq.getTongHocPhi()));
        capNhatGiaTriThe(2, MoneyUtils.format(tq.getTongDaThu()));
        capNhatGiaTriThe(3, MoneyUtils.format(tq.getTongConNo()));
        capNhatGiaTriThe(4, String.valueOf(tq.getSoHoaDonQuaHan()));
    }

    private void capNhatGiaTriThe(int index, String giaTriMoi) {
        JPanel the = (JPanel) theTongQuan.getComponent(index);
        for (Component c : the.getComponents()) {
            if (c instanceof JLabel && "giaTri".equals(c.getName())) ((JLabel) c).setText(giaTriMoi);
        }
    }

    private JPanel buildBoLocThangNam() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(12, 8));

        JLabel tieuDe = new JLabel("Bộ lọc báo cáo 5 năm – chọn năm / tháng để xem doanh thu");
        tieuDe.setFont(UITheme.FONT_BOLD);
        tieuDe.setForeground(UITheme.TEXT_PRIMARY);

        JPanel loc = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        loc.setOpaque(false);
        loc.add(new JLabel("Năm:"));
        cboNam = new JComboBox<>();
        cboNam.addItem("Tất cả năm (5 năm)");
        cboNam.setPreferredSize(new Dimension(150, 34));
        cboNam.setFont(UITheme.FONT_BASE);
        loc.add(cboNam);

        loc.add(new JLabel("Tháng:"));
        cboThang = new JComboBox<>();
        cboThang.addItem("Tất cả tháng");
        for (int m = 1; m <= 12; m++) cboThang.addItem("Tháng " + m);
        cboThang.setPreferredSize(new Dimension(130, 34));
        cboThang.setFont(UITheme.FONT_BASE);
        loc.add(cboThang);

        JButton btnXem = UITheme.primaryButton("Xem biểu đồ");
        btnXem.addActionListener(e -> taiBieuDoTheoLoc());
        loc.add(btnXem);

        JButton btnReset = UITheme.secondaryButton("Làm mới");
        btnReset.addActionListener(e -> {
            cboNam.setSelectedIndex(0);
            cboThang.setSelectedIndex(0);
            taiDuLieu();
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

    private JPanel buildTyLeThuCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        JPanel dongTren = new JPanel(new BorderLayout());
        dongTren.setOpaque(false);
        JLabel lbl = new JLabel("Tỷ lệ thu học phí toàn trường");
        lbl.setFont(UITheme.FONT_BASE);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        dongTren.add(lbl, BorderLayout.WEST);
        lblTyLeThuChuoi = new JLabel("0%");
        lblTyLeThuChuoi.setFont(UITheme.FONT_BOLD);
        lblTyLeThuChuoi.setForeground(UITheme.TEXT_MUTED);
        dongTren.add(lblTyLeThuChuoi, BorderLayout.EAST);
        card.add(dongTren, BorderLayout.NORTH);
        tienDoThu = new TienDoBar();
        tienDoThu.setPreferredSize(new Dimension(10, 12));
        card.add(tienDoThu, BorderLayout.CENTER);
        return card;
    }

    private static class TienDoBar extends JComponent {
        private int phanTram;
        void setPhanTram(int p) { phanTram = p; repaint(); }
        @Override protected void paintComponent(Graphics g) {
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

    private JPanel buildBieuDoNangCao() {
        JPanel row = new JPanel(new GridLayout(1, 1, 16, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(10, 280));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 280));
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 8));
        card.add(UITheme.sectionLabel("Tỷ lệ theo hình thức thanh toán (tiền mặt / CK / Online / Ví)"), BorderLayout.NORTH);
        bieuDoHinhThuc = new HinhThucPieChartPanel();
        card.add(bieuDoHinhThuc, BorderLayout.CENTER);
        row.add(card);
        return row;
    }

    /** Biểu đồ 5 năm + biểu đồ tháng (lọc được). */
    private JPanel buildBieuDoTheoNamVaThang() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(10, 320));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        JPanel cardNam = UITheme.card();
        cardNam.setLayout(new BorderLayout(0, 8));
        cardNam.add(UITheme.sectionLabel("Doanh thu theo năm (5 năm vận hành)"), BorderLayout.NORTH);
        bieuDoTheoNam = new BarChartPanel();
        cardNam.add(bieuDoTheoNam, BorderLayout.CENTER);
        row.add(cardNam);

        JPanel cardThang = UITheme.card();
        cardThang.setLayout(new BorderLayout(0, 8));
        lblTieuDeThang = UITheme.sectionLabel("Doanh thu theo tháng – chọn năm ở bộ lọc");
        cardThang.add(lblTieuDeThang, BorderLayout.NORTH);
        bieuDoThang = new BarChartPanel();
        cardThang.add(bieuDoThang, BorderLayout.CENTER);
        row.add(cardThang);
        return row;
    }

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
                .limit(5).toList();
        if (top5.isEmpty()) {
            JLabel trong = new JLabel("Không có sinh viên còn nợ");
            trong.setFont(UITheme.FONT_BASE);
            trong.setForeground(UITheme.TEXT_MUTED);
            khoiTopNo.add(trong);
        } else {
            BigDecimal max = top5.get(0).tinhConNo();
            for (HoaDonHocPhi hd : top5) khoiTopNo.add(dongTopNo(hd, max));
        }
        khoiTopNo.revalidate();
        khoiTopNo.repaint();
    }

    private JPanel dongTopNo(HoaDonHocPhi hd, BigDecimal noCaoNhat) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        row.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (dieuHuongTimKiem != null) dieuHuongTimKiem.accept("congno", hd.getMaSV());
            }
        });
        JLabel lbl = new JLabel(hd.getTenSV() + " (" + hd.getMaSV() + ")");
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        row.add(lbl, BorderLayout.WEST);
        row.add(UITheme.pill(MoneyUtils.format(hd.tinhConNo()), UITheme.TINT_RED, UITheme.TEXT_RED), BorderLayout.EAST);
        return row;
    }

    private void taiDuLieu() {
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                return new Object[]{
                        baoCaoService.layTongQuan(),
                        baoCaoService.thongKeThuTheoHocKy(),
                        baoCaoService.thongKeSoLuongTheoTrangThai(),
                        congNoService.layDanhSachConNo(),
                        baoCaoService.thongKeThuTheoHinhThuc(),
                        baoCaoService.thongKeThuTheoNam(),
                        baoCaoService.thongKeThuTheoThang(LocalDateYear()),
                        baoCaoService.layDanhSachNamVanHanh()
                };
            }
            private Integer LocalDateYear() {
                return java.time.LocalDate.now().getYear();
            }
            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] kq = get();
                    BaoCaoService.BaoCaoTongQuan tq = (BaoCaoService.BaoCaoTongQuan) kq[0];
                    capNhatTheTongQuan(tq);
                    capNhatTyLeThu(tq);
                    bieuDoCot.setDuLieu((Map<String, BigDecimal>) kq[1]);
                    bieuDoTron.setDuLieu((Map<TrangThaiHoaDon, Long>) kq[2]);
                    capNhatTopNo((List<HoaDonHocPhi>) kq[3]);
                    bieuDoHinhThuc.setDuLieu((Map<String, BigDecimal>) kq[4]);
                    bieuDoTheoNam.setDuLieu((Map<String, BigDecimal>) kq[5]);
                    bieuDoThang.setDuLieu((Map<String, BigDecimal>) kq[6]);
                    napComboNam((List<Integer>) kq[7]);
                    int namHt = java.time.LocalDate.now().getYear();
                    lblTieuDeThang.setText("Doanh thu theo tháng – năm " + namHt + " (T1–T12)");
                    lblLocMoTa.setText("Đang xem: năm " + namHt + " (mặc định) + 5 năm cột");
                    // chọn sẵn năm hiện tại trên combo
                    cboNam.setSelectedItem(String.valueOf(namHt));
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(DashboardPanel.this, "Không tải được thống kê.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void capNhatTyLeThu(BaoCaoService.BaoCaoTongQuan tq) {
        BigDecimal tong = tq.getTongHocPhi();
        int pt = tong != null && tong.compareTo(BigDecimal.ZERO) > 0
                ? tq.getTongDaThu().multiply(BigDecimal.valueOf(100)).divide(tong, 0, RoundingMode.HALF_UP).intValue()
                : 0;
        tienDoThu.setPhanTram(Math.min(100, pt));
        lblTyLeThuChuoi.setText(pt + "%  (" + MoneyUtils.format(tq.getTongDaThu()) + " / " + MoneyUtils.format(tong) + ")");
    }

    private void napComboNam(List<Integer> dsNam) {
        if (cboNam == null) return;
        String chon = (String) cboNam.getSelectedItem();
        cboNam.removeAllItems();
        cboNam.addItem("Tất cả năm (5 năm)");
        List<Integer> namVH = (dsNam != null && !dsNam.isEmpty()) ? dsNam : baoCaoService.layDanhSachNamVanHanh();
        for (Integer n : namVH) cboNam.addItem(String.valueOf(n));
        if (chon != null) cboNam.setSelectedItem(chon);
    }

    private void taiBieuDoTheoLoc() {
        Integer namTmp = null;
        String sNam = cboNam != null ? (String) cboNam.getSelectedItem() : null;
        if (sNam != null && !sNam.startsWith("Tất cả")) {
            try { namTmp = Integer.parseInt(sNam.trim()); } catch (NumberFormatException ignored) {}
        }
        int thangTmp = 0;
        String sThang = cboThang != null ? (String) cboThang.getSelectedItem() : null;
        if (sThang != null && !sThang.startsWith("Tất cả")) {
            try { thangTmp = Integer.parseInt(sThang.replace("Tháng ", "").trim()); } catch (NumberFormatException ignored) {}
        }
        final Integer nam = namTmp;
        final int thang = thangTmp;

        SwingWorker<Object[], Void> w = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                Map<String, BigDecimal> theoNam = baoCaoService.thongKeThuTheoNam();
                Map<String, BigDecimal> theoThang;
                if (nam != null) {
                    theoThang = baoCaoService.thongKeThuTheoThang(nam);
                    if (thang >= 1 && thang <= 12) {
                        Map<String, BigDecimal> one = new LinkedHashMap<>();
                        String key = "T" + thang;
                        one.put(key, theoThang.getOrDefault(key, BigDecimal.ZERO));
                        theoThang = one;
                    }
                } else {
                    // Tất cả năm: hiển thị tổng theo năm trên cột tháng? → dùng 5 năm
                    theoThang = theoNam;
                }
                return new Object[]{theoNam, theoThang};
            }
            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] arr = get();
                    bieuDoTheoNam.setDuLieu((Map<String, BigDecimal>) arr[0]);
                    bieuDoThang.setDuLieu((Map<String, BigDecimal>) arr[1]);
                    String moTa;
                    if (nam == null) {
                        moTa = "Đang xem: so sánh 5 năm";
                        lblTieuDeThang.setText("Doanh thu theo năm (chế độ tất cả năm)");
                    } else if (thang == 0) {
                        moTa = "Đang xem: cả năm " + nam + " (12 tháng)";
                        lblTieuDeThang.setText("Doanh thu tháng T1–T12 năm " + nam);
                    } else {
                        moTa = "Đang xem: tháng " + thang + "/" + nam;
                        lblTieuDeThang.setText("Doanh thu tháng " + thang + " năm " + nam);
                    }
                    lblLocMoTa.setText(moTa);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(DashboardPanel.this, rootMessage(ex));
                }
            }
        };
        w.execute();
    }

    private String rootMessage(Exception ex) {
        Throwable c = ex.getCause() != null ? ex.getCause() : ex;
        return c.getMessage() != null ? c.getMessage() : c.toString();
    }

    // ---------- Biểu đồ cột ----------
    private static class BarChartPanel extends JPanel {
        private Map<String, BigDecimal> duLieu = new LinkedHashMap<>();
        BarChartPanel() { setOpaque(false); setPreferredSize(new Dimension(400, 260)); }
        void setDuLieu(Map<String, BigDecimal> d) {
            this.duLieu = d == null ? new LinkedHashMap<>() : d;
            repaint();
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int leTrai = 28, leDuoi = 48, leTren = 28, lePhai = 12;
            int rongVe = w - leTrai - lePhai, caoVe = h - leTren - leDuoi;
            if (rongVe <= 0 || caoVe <= 0) return;

            if (duLieu.isEmpty()) {
                g2.setColor(UITheme.TEXT_MUTED);
                g2.setFont(UITheme.FONT_BASE);
                String t = "Chưa có dữ liệu";
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(t, (w - fm.stringWidth(t)) / 2, h / 2);
                return;
            }

            BigDecimal max = duLieu.values().stream().reduce(BigDecimal.ZERO, BigDecimal::max);
            if (max.compareTo(BigDecimal.ZERO) == 0) max = BigDecimal.ONE;

            g2.setColor(UITheme.BORDER);
            g2.drawLine(leTrai, leTren + caoVe, leTrai + rongVe, leTren + caoVe);

            int soCot = Math.max(1, duLieu.size());
            int rongMoi = rongVe / soCot;
            int rongCot = Math.max(14, Math.min(56, rongMoi - 16));
            g2.setFont(UITheme.FONT_BASE.deriveFont(10f));
            int i = 0;
            for (Map.Entry<String, BigDecimal> e : duLieu.entrySet()) {
                double tiLe = e.getValue().doubleValue() / max.doubleValue();
                int cao = (int) Math.round(tiLe * (caoVe - 18));
                int x = leTrai + i * rongMoi + (rongMoi - rongCot) / 2;
                int y = leTren + caoVe - cao;
                g2.setColor(PALETTE[i % PALETTE.length]);
                g2.fillRoundRect(x, Math.max(y, leTren), rongCot, Math.max(cao, 2), 6, 6);

                String gt = MoneyUtils.format(e.getValue());
                FontMetrics fm = g2.getFontMetrics();
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.drawString(gt, x + (rongCot - fm.stringWidth(gt)) / 2, Math.max(leTren + 10, y - 4));

                String nhan = e.getKey();
                g2.setColor(UITheme.TEXT_MUTED);
                g2.drawString(nhan, leTrai + i * rongMoi + (rongMoi - fm.stringWidth(nhan)) / 2, leTren + caoVe + 16);
                i++;
            }
        }
    }

    private static class PieChartPanel extends JPanel {
        private Map<TrangThaiHoaDon, Long> duLieu = new LinkedHashMap<>();
        PieChartPanel() { setOpaque(false); }
        void setDuLieu(Map<TrangThaiHoaDon, Long> d) { duLieu = d == null ? new LinkedHashMap<>() : d; repaint(); }
        private Color mau(TrangThaiHoaDon tt) {
            return switch (tt) {
                case DA_DONG_DU -> new Color(0x10, 0xB9, 0x81);
                case DONG_MOT_PHAN -> new Color(0xF5, 0x9E, 0x0B);
                case QUA_HAN -> new Color(0xEF, 0x44, 0x44);
                case CHUA_DONG -> new Color(0x64, 0x74, 0x8B);
            };
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            long tong = duLieu.values().stream().mapToLong(Long::longValue).sum();
            int w = getWidth(), h = getHeight();
            if (tong == 0) {
                g2.setColor(UITheme.TEXT_MUTED);
                g2.drawString("Chưa có dữ liệu", w / 2 - 40, h / 2);
                return;
            }
            int dk = Math.min(w, h) - 40;
            int x = Math.max(10, (w - dk) / 2 - 50);
            int y = (h - dk) / 2;
            double goc = 90;
            for (var e : duLieu.entrySet()) {
                double phan = 360.0 * e.getValue() / tong;
                g2.setColor(mau(e.getKey()));
                g2.fill(new Arc2D.Double(x, y, dk, dk, goc, -phan, Arc2D.PIE));
                goc -= phan;
            }
            int cx = x + dk + 24, cy = y + 8;
            g2.setFont(UITheme.FONT_BASE.deriveFont(12f));
            for (var e : duLieu.entrySet()) {
                double pct = 100.0 * e.getValue() / tong;
                g2.setColor(mau(e.getKey()));
                g2.fillRect(cx, cy, 12, 12);
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.drawString(String.format("%s (%d - %.0f%%)", e.getKey().getNhan(), e.getValue(), pct), cx + 18, cy + 11);
                cy += 22;
            }
        }
    }

    private static class HinhThucPieChartPanel extends JPanel {
        private Map<String, BigDecimal> duLieu = new LinkedHashMap<>();
        HinhThucPieChartPanel() { setOpaque(false); }
        void setDuLieu(Map<String, BigDecimal> d) { duLieu = d == null ? new LinkedHashMap<>() : d; repaint(); }
        private Color mau(String ht) {
            return switch (ht) {
                case "TIEN_MAT" -> new Color(0x10, 0xB9, 0x81);
                case "CHUYEN_KHOAN" -> new Color(0x1D, 0x4E, 0xD8);
                case "THANH_TOAN_ONLINE" -> new Color(0xF5, 0x9E, 0x0B);
                case "VI_DIEN_TU" -> new Color(0x8B, 0x5C, 0xF6);
                default -> new Color(0x94, 0xA3, 0xB8);
            };
        }
        private String ten(String ht) {
            return switch (ht) {
                case "TIEN_MAT" -> "Tiền mặt";
                case "CHUYEN_KHOAN" -> "Chuyển khoản";
                case "THANH_TOAN_ONLINE" -> "Online (VNPay/MoMo)";
                case "VI_DIEN_TU" -> "Ví điện tử";
                default -> ht;
            };
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            BigDecimal tong = duLieu.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            int w = getWidth(), h = getHeight();
            if (tong.compareTo(BigDecimal.ZERO) == 0) {
                g2.setColor(UITheme.TEXT_MUTED);
                g2.drawString("Chưa có phiếu thu", w / 2 - 50, h / 2);
                return;
            }
            int dk = Math.min(w, h) - 40;
            int x = Math.max(10, (w - dk) / 2 - 50);
            int y = (h - dk) / 2;
            double goc = 90;
            for (var e : duLieu.entrySet()) {
                double phan = 360.0 * e.getValue().doubleValue() / tong.doubleValue();
                g2.setColor(mau(e.getKey()));
                g2.fill(new Arc2D.Double(x, y, dk, dk, goc, -phan, Arc2D.PIE));
                goc -= phan;
            }
            int cx = x + dk + 24, cy = y + 8;
            g2.setFont(UITheme.FONT_BASE.deriveFont(12f));
            for (var e : duLieu.entrySet()) {
                double pct = 100.0 * e.getValue().doubleValue() / tong.doubleValue();
                g2.setColor(mau(e.getKey()));
                g2.fillRect(cx, cy, 12, 12);
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.drawString(String.format("%s (%.0f%%)", ten(e.getKey()), pct), cx + 18, cy + 11);
                cy += 22;
            }
        }
    }
}