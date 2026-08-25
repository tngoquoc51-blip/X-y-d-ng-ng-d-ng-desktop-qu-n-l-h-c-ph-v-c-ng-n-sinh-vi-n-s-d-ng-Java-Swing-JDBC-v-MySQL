package vn.edu.eaut.qlhocphi.gui.baocao;

import vn.edu.eaut.qlhocphi.bus.BaoCaoService;
import vn.edu.eaut.qlhocphi.bus.CongNoService;
import vn.edu.eaut.qlhocphi.config.UITheme;
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
 * Man hinh Dashboard "Thong ke & Bao cao" - ban nang cap day du cho do an tot
 * nghiep: banner dong bo, 5 the KPI rieng biet (khong nhoi chung), the ty le
 * thu hoc phi dang thanh tien do, 2 bieu do co nhan gia tri/% ro rang, va them
 * moi khoi "Top 5 sinh vien no nhieu nhat" - phan phan tich sau ma ban cu
 * chua co. Toan bo ve bang Java2D thuan, khong can them thu vien bieu do ngoai.
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
    private JPanel khoiTopNo;
    private JButton btnXuatBaoCao;

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
        giua.add(buildTyLeThuCard());
        giua.add(Box.createRigidArea(new Dimension(0, 16)));
        giua.add(buildBieuDo());
        giua.add(Box.createRigidArea(new Dimension(0, 16)));
        giua.add(buildTopNoCard());

        JScrollPane scroll = new JScrollPane(bocNgoai(giua));
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        taiDuLieu();
    }

    /** Bam sat chieu rong khung cuon, khong de trong khoang trang lech ben phai. */
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

    // ================== HEADER (banner + logo, dong bo cac trang khac) ==================

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
        JLabel tieuDe = new JLabel("Thong ke & Bao cao");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Tong quan tai chinh, ty le thu va phan tich cong no toan truong");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);

        btnXuatBaoCao = new JButton("Xuat bao cao");
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

    // ================== 5 THE KPI RIENG BIET (khong nhoi chung nhu ban cu) ==================

    private JPanel buildTheTongQuan() {
        theTongQuan = new JPanel(new GridLayout(1, 5, 14, 0));
        theTongQuan.setOpaque(false);
        theTongQuan.add(UITheme.statCard("Tong so hoa don", "0", UITheme.TINT_BLUE, UITheme.TEXT_BLUE));
        theTongQuan.add(UITheme.statCard("Tong hoc phi", "0 d", UITheme.TINT_VIOLET, UITheme.TEXT_VIOLET));
        theTongQuan.add(UITheme.statCard("Da thu", "0 d", UITheme.TINT_GREEN, UITheme.TEXT_GREEN));
        theTongQuan.add(UITheme.statCard("Con no", "0 d", UITheme.TINT_RED, UITheme.TEXT_RED));
        theTongQuan.add(UITheme.statCard("Hoa don qua han", "0", UITheme.TINT_RED, UITheme.TEXT_RED));
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

    // ================== TY LE THU HOC PHI (thanh tien do) ==================

    private JPanel buildTyLeThuCard() {
        theTyLeThu = UITheme.card();
        theTyLeThu.setLayout(new BorderLayout(0, 10));

        JPanel dongTren = new JPanel(new BorderLayout());
        dongTren.setOpaque(false);
        JLabel lblTieuDe = new JLabel("Ty le thu hoc phi toan truong");
        lblTieuDe.setFont(UITheme.FONT_BASE);
        lblTieuDe.setForeground(UITheme.TEXT_PRIMARY);
        dongTren.add(lblTieuDe, BorderLayout.WEST);

        lblTyLeThuChuoi = new JLabel("0%  (0 d / 0 d)");
        lblTyLeThuChuoi.setFont(UITheme.FONT_BOLD);
        lblTyLeThuChuoi.setForeground(UITheme.TEXT_MUTED);
        dongTren.add(lblTyLeThuChuoi, BorderLayout.EAST);
        theTyLeThu.add(dongTren, BorderLayout.NORTH);

        tienDoThu = new TienDoBar();
        tienDoThu.setPreferredSize(new Dimension(10, 12));
        theTyLeThu.add(tienDoThu, BorderLayout.CENTER);

        return theTyLeThu;
    }

    /** Thanh tien do bo tron ve tay - dung lai kieu da lam o SinhVienCongNoPanel. */
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

    // ================== 2 BIEU DO (co nhan gia tri/%) ==================

    private JPanel buildBieuDo() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);
        row.setPreferredSize(new Dimension(10, 300));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        JPanel cardCot = UITheme.card();
        cardCot.setLayout(new BorderLayout(0, 8));
        cardCot.add(UITheme.sectionLabel("Tong thu theo hoc ky"), BorderLayout.NORTH);
        bieuDoCot = new BarChartPanel();
        cardCot.add(bieuDoCot, BorderLayout.CENTER);
        row.add(cardCot);

        JPanel cardTron = UITheme.card();
        cardTron.setLayout(new BorderLayout(0, 8));
        cardTron.add(UITheme.sectionLabel("Trang thai hoa don"), BorderLayout.NORTH);
        bieuDoTron = new PieChartPanel();
        cardTron.add(bieuDoTron, BorderLayout.CENTER);
        row.add(cardTron);

        return row;
    }

    // ================== TOP 5 SINH VIEN NO NHIEU NHAT (moi hoan toan) ==================

    private JPanel buildTopNoCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UITheme.sectionLabel("Top 5 sinh vien con no hoc phi nhieu nhat"), BorderLayout.NORTH);

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
            JLabel trong = new JLabel("Khong co sinh vien nao con no hoc phi");
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

    /** 1 dong: avatar + ten + hoc ky, thanh mini the hien ti le so voi nguoi no cao nhat, so tien dang pill do. */
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

    // ================== TAI DU LIEU ==================

    private void taiDuLieu() {
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                BaoCaoService.BaoCaoTongQuan tongQuan = baoCaoService.layTongQuan();
                Map<String, BigDecimal> thuTheoHocKy = baoCaoService.thongKeThuTheoHocKy();
                Map<TrangThaiHoaDon, Long> theoTrangThai = baoCaoService.thongKeSoLuongTheoTrangThai();
                List<HoaDonHocPhi> danhSachNo = congNoService.layDanhSachConNo();
                return new Object[]{tongQuan, thuTheoHocKy, theoTrangThai, danhSachNo};
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

                    capNhatTheTongQuan(tongQuan);
                    capNhatTyLeThu(tongQuan);
                    bieuDoCot.setDuLieu(thuTheoHocKy);
                    bieuDoTron.setDuLieu(theoTrangThai);
                    capNhatTopNo(danhSachNo);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(DashboardPanel.this, "Khong the tai du lieu thong ke.\n" + rootMessage(ex));
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

    private String rootMessage(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }

    // ================== BIEU DO COT (co them nhan gia tri tren dinh cot) ==================

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
                veChuThongBao(g2, "Chua co du lieu");
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

                g2.setColor(UITheme.PRIMARY);
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

    // ================== BIEU DO TRON (co them % trong chu thich) ==================

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
                case DA_DONG_DU: return UITheme.SUCCESS;
                case DONG_MOT_PHAN: return UITheme.WARNING;
                case QUA_HAN: return UITheme.DANGER;
                default: return UITheme.TEXT_MUTED;
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
                String text = "Chua co du lieu";
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
}