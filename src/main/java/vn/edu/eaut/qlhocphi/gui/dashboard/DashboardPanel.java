package vn.edu.eaut.qlhocphi.gui.dashboard;

import vn.edu.eaut.qlhocphi.bus.BaoCaoService;
import vn.edu.eaut.qlhocphi.bus.CongNoService;
import vn.edu.eaut.qlhocphi.bus.SinhVienService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.SinhVien;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

/**
 * Man hinh "Tong quan" (Dashboard quan ly) danh cho Admin/Ke toan.
 * Cau truc: banner chao mung -> hang KPI so lieu that (tu BaoCaoService) ->
 * hang truy cap nhanh -> 2 khoi danh sach co the bam vao de dieu huong ngay.
 * Day la "trung tam dieu khien" that su, khong con la trang thong tin tinh.
 */
public class DashboardPanel extends JPanel {
    private final CongNoService congNoService = new CongNoService();
    private final SinhVienService sinhVienService = new SinhVienService();
    private final BaoCaoService baoCaoService = new BaoCaoService();

    private final JPanel khoiCongNo = new JPanel();
    private final JPanel khoiSinhVienMoi = new JPanel();
    private JPanel kpiRow;
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

        JPanel quickActions = buildQuickActions(dieuHuong);
        quickActions.setAlignmentX(Component.LEFT_ALIGNMENT);
        giua.add(quickActions);
        giua.add(Box.createRigidArea(new Dimension(0, 18)));

        JPanel thongTinRow = buildThongTinRow();
        thongTinRow.setAlignmentX(Component.LEFT_ALIGNMENT);
        giua.add(thongTinRow);

        add(giua, BorderLayout.CENTER);

        taiDuLieuKpi();
        taiDuLieuCongNo();
        taiDuLieuSinhVienMoi();
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

        JLabel iconTron = new JLabel("\uD83D\uDCCA", SwingConstants.CENTER); // icon bieu do - dung cho "quan ly"
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
        if (gio < 11) return "Chao buoi sang";
        if (gio < 13) return "Chao buoi trua";
        if (gio < 18) return "Chao buoi chieu";
        return "Chao buoi toi";
    }

    // ================== KPI SO LIEU THAT ==================

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.add(UITheme.statCard("Tong sinh vien", "…", UITheme.TINT_BLUE, UITheme.TEXT_BLUE));
        row.add(UITheme.statCard("Tong da thu", "…", UITheme.TINT_GREEN, UITheme.TEXT_GREEN));
        row.add(UITheme.statCard("Tong cong no", "…", UITheme.TINT_RED, UITheme.TEXT_RED));
        row.add(UITheme.statCard("Hoa don qua han", "…", UITheme.TINT_VIOLET, UITheme.TEXT_VIOLET));
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

    // ================== TRUY CAP NHANH ==================

    private JPanel buildQuickActions(Consumer<String> dieuHuong) {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);

        JButton the1 = UITheme.quickActionCard("\uD83D\uDC65", "Ho so", "Sinh vien", UITheme.PRIMARY);
        JButton the2 = UITheme.quickActionCard("\uD83D\uDCC5", "Cau hinh", "Hoc ky & muc phi", UITheme.WARNING);
        JButton the3 = UITheme.quickActionCard("\uD83D\uDCB3", "Tai chinh", "Thanh toan", UITheme.SUCCESS);
        JButton the4 = UITheme.quickActionCard("\u26A0", "Canh bao", "Cong no", UITheme.DANGER);

        the1.addActionListener(e -> dieuHuong.accept("sinhvien"));
        the2.addActionListener(e -> dieuHuong.accept("hocky"));
        the3.addActionListener(e -> dieuHuong.accept("thanhtoan"));
        the4.addActionListener(e -> dieuHuong.accept("congno"));

        row.add(the1);
        row.add(the2);
        row.add(the3);
        row.add(the4);
        return row;
    }

    // ================== 2 KHOI DANH SACH - CO THE BAM VAO ==================

    private JPanel buildThongTinRow() {
        JPanel row = new JPanel(new GridLayout(1, 2, 16, 0));
        row.setOpaque(false);

        JPanel congNoCard = richCard("Sinh vien con no hoc phi", "\u26A0", UITheme.DANGER, "congno");
        khoiCongNo.setOpaque(false);
        khoiCongNo.setLayout(new BoxLayout(khoiCongNo, BoxLayout.Y_AXIS));
        congNoCard.add(khoiCongNo, BorderLayout.CENTER);

        JPanel sinhVienCard = richCard("Sinh vien moi cap nhat", "\uD83D\uDC65", UITheme.PRIMARY, "sinhvien");
        khoiSinhVienMoi.setOpaque(false);
        khoiSinhVienMoi.setLayout(new BoxLayout(khoiSinhVienMoi, BoxLayout.Y_AXIS));
        sinhVienCard.add(khoiSinhVienMoi, BorderLayout.CENTER);

        row.add(congNoCard);
        row.add(sinhVienCard);
        return row;
    }

    /** The trang bo goc, co header icon mau + tieu de + link "Xem tat ca" dieu huong sang tab tuong ung. */
    private JPanel richCard(String tieuDe, String icon, Color mauNhan, String tabDieuHuong) {
        JPanel card = new JPanel(new BorderLayout(0, 12)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 20, 10, 20));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel headerTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        headerTrai.setOpaque(false);

        JLabel iconBadge = new JLabel(icon, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mauNhan);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        iconBadge.setOpaque(false);
        iconBadge.setForeground(Color.WHITE);
        iconBadge.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        iconBadge.setPreferredSize(new Dimension(36, 36));

        JLabel title = new JLabel(tieuDe);
        title.setFont(UITheme.FONT_H2);
        title.setForeground(UITheme.TEXT_PRIMARY);

        headerTrai.add(iconBadge);
        headerTrai.add(title);
        header.add(headerTrai, BorderLayout.WEST);

        JLabel lblXemTatCa = new JLabel("Xem tat ca \u2192");
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

    /** 1 dong thong tin: avatar tron + ten + phu chu, ben phai la 1 the mau (pill). Bam vao ca dong se dieu huong. */
    private JPanel richInfoRow(String ten, String phu, String giaTri, Color nenNhat, Color chuDam, String tabDieuHuong) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF0, 0xF2, 0xF6)),
                BorderFactory.createEmptyBorder(10, 4, 10, 4)));
        row.setCursor(new Cursor(Cursor.HAND_CURSOR));
        row.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dieuHuong.accept(tabDieuHuong); }
            @Override public void mouseEntered(MouseEvent e) { row.setOpaque(true); row.setBackground(new Color(0xF7, 0xF9, 0xFC)); row.repaint(); }
            @Override public void mouseExited(MouseEvent e) { row.setOpaque(false); row.repaint(); }
        });

        JPanel trai = new JPanel(new BorderLayout(12, 0));
        trai.setOpaque(false);
        trai.add(UITheme.avatarTron(ten), BorderLayout.WEST);

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
        textCol.add(tenLabel);
        textCol.add(phuLabel);
        trai.add(textCol, BorderLayout.CENTER);
        row.add(trai, BorderLayout.CENTER);

        if (giaTri != null) {
            JLabel giaTriPill = UITheme.pill(giaTri, nenNhat, chuDam);
            JPanel phaiWrap = new JPanel(new GridBagLayout());
            phaiWrap.setOpaque(false);
            phaiWrap.add(giaTriPill);
            row.add(phaiWrap, BorderLayout.EAST);
        }
        return row;
    }

    // ================== TAI DU LIEU 2 KHOI DANH SACH ==================

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
                        themDongTrong(khoiCongNo, "Khong co sinh vien nao con no hoc phi");
                    } else {
                        int gioiHan = Math.min(list.size(), 5);
                        for (int i = 0; i < gioiHan; i++) {
                            HoaDonHocPhi hd = list.get(i);
                            khoiCongNo.add(richInfoRow(
                                    hd.getTenSV(), "Ma SV: " + hd.getMaSV(),
                                    MoneyUtils.format(hd.tinhConNo()),
                                    UITheme.TINT_RED, UITheme.TEXT_RED, "congno"));
                        }
                        if (list.size() > gioiHan) {
                            themDongTrong(khoiCongNo, "... va " + (list.size() - gioiHan) + " sinh vien khac");
                        }
                    }
                } catch (Exception ex) {
                    themDongTrong(khoiCongNo, "Khong the tai du lieu cong no");
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
                        themDongTrong(khoiSinhVienMoi, "Chua co sinh vien nao trong he thong");
                    } else {
                        int gioiHan = Math.min(list.size(), 5);
                        for (int i = 0; i < gioiHan; i++) {
                            SinhVien sv = list.get(i);
                            khoiSinhVienMoi.add(richInfoRow(
                                    sv.getHoTen(), "Ma SV: " + sv.getMaSV(),
                                    sv.getLop(), UITheme.TINT_BLUE, UITheme.TEXT_BLUE, "sinhvien"));
                        }
                        if (list.size() > gioiHan) {
                            themDongTrong(khoiSinhVienMoi, "... va " + (list.size() - gioiHan) + " sinh vien khac");
                        }
                    }
                } catch (Exception ex) {
                    themDongTrong(khoiSinhVienMoi, "Khong the tai danh sach sinh vien");
                }
                khoiSinhVienMoi.revalidate();
                khoiSinhVienMoi.repaint();
            }
        };
        worker.execute();
    }

    private void themDongTrong(JPanel khoi, String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(UITheme.FONT_BASE);
        l.setForeground(UITheme.TEXT_MUTED);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(16, 0, 16, 0));
        khoi.add(l);
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