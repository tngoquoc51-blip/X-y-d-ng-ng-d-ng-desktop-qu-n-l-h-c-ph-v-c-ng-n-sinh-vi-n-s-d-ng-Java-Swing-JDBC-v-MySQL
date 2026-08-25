package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.bus.CongNoService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.dal.SinhVienDAO;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.gui.thanhtoan.PaymentMethodDialog;
import vn.edu.eaut.qlhocphi.gui.thanhtoan.ChonSoTienThanhToanDialog;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.SinhVien;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.TrangThaiHoaDon;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Trang "Tong quan" cong Sinh vien: banner chao mung + 4 the thong ke mau +
 * thanh tien do dong hoc phi + 2 the xem nhanh (hoa don gan nhat can dong / lich
 * su thanh toan gan day). Thiet ke phoi mau lai theo bo mau tint nhat (xem
 * UITheme: TINT_VIOLET/GREEN/RED/BLUE) cho dong bo, hien dai hon ban cu.
 *
 * Ghi chu sua loi: ban cu co nut "Lam moi" + icon mu tot nghiep dat canh nhau
 * trong 1 FlowLayout o goc phai banner; khi cua so bi thu hep hoac phong chu he
 * thong khac chuan, 2 thanh phan nay bi chen/chong len nhau va dan den hien
 * tuong hien thi sai (vd nhin giong 1 chuoi ngay/gio). Ban thiet ke moi bo hang
 * icon + nut do, thay bang 1 pill "X nhac no" duy nhat, vua gon vua bam duoc de
 * lam moi du lieu - khong con nguy co chong lap nua.
 */
public class SinhVienCongNoPanel extends JPanel {
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CongNoService congNoService = new CongNoService();
    private final SinhVienDAO sinhVienDAO = new SinhVienDAO();
    private final TaiKhoan taiKhoan;
    private final String maSV;

    private JLabel lblTenSV;
    private JLabel lblPillNhacNo;
    private JPanel theNhanh;
    private ThanhTienDoBar thanhTienDo;
    private JLabel lblTienDoChuoi;
    private JPanel hoaDonGanNhatBox;
    private LichSuThanhToanPanel lichSuPanel;
    private final Runnable moTroLyAI;
    private boolean daHienThiToastCongNo = false;

    public SinhVienCongNoPanel(TaiKhoan taiKhoan, Runnable moTroLyAI) {
        this.taiKhoan = taiKhoan;
        this.maSV = taiKhoan.getMaSV();
        this.moTroLyAI = moTroLyAI;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        add(buildBanner(), BorderLayout.NORTH);

        JPanel giua = new JPanel();
        giua.setOpaque(false);
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));
        giua.add(buildTheThongKe());
        giua.add(Box.createRigidArea(new Dimension(0, 16)));
        giua.add(buildTienDoCard());
        giua.add(Box.createRigidArea(new Dimension(0, 16)));
        giua.add(buildHaiCotXemNhanh());

        JScrollPane scroll = new JScrollPane(bocNgoai(giua));
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        taiDuLieu();
    }

    private JPanel bocNgoai(JPanel noiDung) {
        JPanel wrap = new KhungCuonToanChieuRong(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(noiDung, BorderLayout.NORTH);
        return wrap;
    }

    // ===== Banner chao mung =====
    private JPanel buildBanner() {
        GradientBannerPanel banner = new GradientBannerPanel();
        banner.setLayout(new BorderLayout());
        banner.setPreferredSize(new Dimension(10, 100));
        banner.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        JPanel textBox = new JPanel();
        textBox.setOpaque(false);
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));

        JLabel lblChao = new JLabel(loiChao());
        lblChao.setFont(UITheme.FONT_BASE);
        lblChao.setForeground(new Color(255, 255, 255, 210));
        lblChao.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTenSV = new JLabel(taiKhoan.getHoTen());
        lblTenSV.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTenSV.setForeground(Color.WHITE);
        lblTenSV.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTenSV.setBorder(BorderFactory.createEmptyBorder(2, 0, 6, 0));

        JLabel lblNgay = new JLabel(dinhDangNgayHomNay());
        lblNgay.setFont(UITheme.FONT_BASE);
        lblNgay.setForeground(new Color(255, 255, 255, 210));
        lblNgay.setAlignmentX(Component.LEFT_ALIGNMENT);

        textBox.add(lblChao);
        textBox.add(lblTenSV);
        textBox.add(lblNgay);
        banner.add(textBox, BorderLayout.WEST);

        lblPillNhacNo = UITheme.pill("0 nhắc nợ", new Color(255, 255, 255, 40), Color.WHITE);
        lblPillNhacNo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblPillNhacNo.setToolTipText("Nhấn để làm mới dữ liệu");
        lblPillNhacNo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) { taiDuLieu(); }
        });

        JPanel khungPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        khungPhai.setOpaque(false);
        khungPhai.add(lblPillNhacNo);
        banner.add(khungPhai, BorderLayout.EAST);

        return banner;
    }

    private String loiChao() {
        int gio = LocalTime.now().getHour();
        if (gio < 11) return "Chào buổi sáng,";
        if (gio < 18) return "Chào buổi chiều,";
        return "Chào buổi tối,";
    }

    private String dinhDangNgayHomNay() {
        LocalDate now = LocalDate.now();
        String thu = tenThuTiengViet(now.getDayOfWeek());
        return thu + ", ngày " + now.format(DMY);
    }

    private String tenThuTiengViet(DayOfWeek d) {
        switch (d) {
            case MONDAY: return "Thứ Hai";
            case TUESDAY: return "Thứ Ba";
            case WEDNESDAY: return "Thứ Tư";
            case THURSDAY: return "Thứ Năm";
            case FRIDAY: return "Thứ Sáu";
            case SATURDAY: return "Thứ Bảy";
            default: return "Chủ Nhật";
        }
    }

    // ===== 4 the thong ke mau (Hoc phi / Da dong / Con no / Tien do dong) =====
    private JPanel buildTheThongKe() {
        theNhanh = new JPanel(new GridLayout(1, 4, 16, 0));
        theNhanh.setOpaque(false);
        theNhanh.add(UITheme.statCard("Học phí", "…", UITheme.TINT_VIOLET, UITheme.TEXT_VIOLET));
        theNhanh.add(UITheme.statCard("Đã đóng", "…", UITheme.TINT_GREEN, UITheme.TEXT_GREEN));
        theNhanh.add(UITheme.statCard("Còn nợ", "…", UITheme.TINT_RED, UITheme.TEXT_RED));
        theNhanh.add(UITheme.statCard("Tiến độ đóng", "…", UITheme.TINT_BLUE, UITheme.TEXT_BLUE));
        return theNhanh;
    }

    // ===== Thanh tien do dong hoc phi (hoc ky gan nhat) =====
    private JPanel buildTienDoCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel dongTren = new JPanel(new BorderLayout());
        dongTren.setOpaque(false);
        JLabel lblTieuDe = new JLabel("Tiến độ đóng học phí học kỳ 1");
        lblTieuDe.setFont(UITheme.FONT_BASE);
        lblTieuDe.setForeground(UITheme.TEXT_PRIMARY);
        dongTren.add(lblTieuDe, BorderLayout.WEST);

        lblTienDoChuoi = new JLabel("0 / 0đ");
        lblTienDoChuoi.setFont(UITheme.FONT_BOLD);
        lblTienDoChuoi.setForeground(UITheme.TEXT_MUTED);
        dongTren.add(lblTienDoChuoi, BorderLayout.EAST);

        card.add(dongTren, BorderLayout.NORTH);

        thanhTienDo = new ThanhTienDoBar();
        thanhTienDo.setPreferredSize(new Dimension(10, 10));
        card.add(thanhTienDo, BorderLayout.CENTER);

        return card;
    }

    // ===== 2 the xem nhanh: Hoa don gan nhat can dong / Lich su thanh toan =====
    // SUA LOI: ban cu dung GridLayout(1,2,16,0) - GridLayout tinh preferredSize cua
    // ca dong dua theo do rong "tu nhien" ma tung the con muon co, nen chi can 1 trong
    // 2 the (vd Lich su thanh toan) co noi dung tinh sai chieu rong (vd do JScrollPane
    // ben trong bi tran ngang) la ca dong nay bi lech, khong con chia deu 50/50 va
    // khong con trai het chieu rong thuc te nua (dung la loi ban gap - the bi don ve
    // ben phai, ho mot khoang trong lon ben trai).
    // Doi sang GridBagLayout voi weightx = 0.5 cho ca 2 cot + fill = BOTH: day la cach
    // CHUAN va DANG TIN CAY nhat trong Swing de ep 2 o luon chia dung 50/50 va luon
    // lap day toan bo chieu rong duoc cap, hoan toan khong phu thuoc vao noi dung ben
    // trong tung the muon rong bao nhieu.
    // ===== 2 the xem nhanh: Hoa don gan nhat can dong / Lich su thanh toan =====
// FlowLayout(CENTER) - 2 the giu kich thuoc co dinh (khong con phu thuoc GridBagLayout
// hay tinh bounds thu cong nua), ca khoi tu dong can giua theo chieu rong man hinh.
    private JPanel buildHaiCotXemNhanh() {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));

        final int RONG_MOI_THE = 380;
        final int CAO_MOI_THE = 260;

        JPanel cardHoaDon = UITheme.card();
        cardHoaDon.setLayout(new BorderLayout(0, 10));
        cardHoaDon.setPreferredSize(new Dimension(RONG_MOI_THE, CAO_MOI_THE));
        cardHoaDon.add(nhanTieuDeCoVach("HÓA ĐƠN HỌC PHÍ", UITheme.TEXT_RED), BorderLayout.NORTH);
        hoaDonGanNhatBox = new JPanel();
        hoaDonGanNhatBox.setOpaque(false);
        hoaDonGanNhatBox.setLayout(new BoxLayout(hoaDonGanNhatBox, BoxLayout.Y_AXIS));
        cardHoaDon.add(hoaDonGanNhatBox, BorderLayout.CENTER);

        JPanel cardLichSu = UITheme.card();
        cardLichSu.setLayout(new BorderLayout());
        cardLichSu.setPreferredSize(new Dimension(RONG_MOI_THE, CAO_MOI_THE));
        lichSuPanel = new LichSuThanhToanPanel(taiKhoan.getHoTen(), maSV);
        cardLichSu.add(lichSuPanel, BorderLayout.CENTER);

        row.add(cardHoaDon);
        row.add(cardLichSu);

        return row;
    }

    private JPanel nhanTieuDeCoVach(String text, Color mauVach) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        JPanel vach = new JPanel();
        vach.setBackground(mauVach);
        vach.setPreferredSize(new Dimension(4, 18));
        p.add(vach, BorderLayout.WEST);
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    // ===== Tai du lieu =====
    private void taiDuLieu() {
        if (maSV == null || maSV.isBlank()) {
            lblTenSV.setText(taiKhoan.getHoTen() + "  (Chưa gán Mã sinh viên - liên hệ Admin)");
            return;
        }

        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                SinhVien sv = sinhVienDAO.timTheoMa(maSV);
                List<HoaDonHocPhi> dsHoaDon = congNoService.layTheoSinhVien(maSV);
                return new Object[]{sv, dsHoaDon};
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] ketQua = get();
                    SinhVien sv = (SinhVien) ketQua[0];
                    List<HoaDonHocPhi> dsHoaDon = (List<HoaDonHocPhi>) ketQua[1];

                    if (sv != null) {
                        lblTenSV.setText(sv.getHoTen() + "  (" + sv.getMaSV() + " - " + sv.getLop() + ")");
                    }
                    capNhatTheThongKe(dsHoaDon);
                    capNhatTienDo(dsHoaDon);
                    capNhatHoaDonGanNhat(dsHoaDon);
                    lichSuPanel.taiDuLieu(dsHoaDon);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(SinhVienCongNoPanel.this,
                            "Không thể tải dữ liệu công nợ.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void capNhatTheThongKe(List<HoaDonHocPhi> dsHoaDon) {
        BigDecimal tongHocPhi = dsHoaDon.stream().map(HoaDonHocPhi::getSoTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tongDaNop = dsHoaDon.stream().map(HoaDonHocPhi::getDaNop)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tongConNo = dsHoaDon.stream().map(HoaDonHocPhi::tinhConNo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int phanTram = tongHocPhi.compareTo(BigDecimal.ZERO) > 0
                ? tongDaNop.multiply(BigDecimal.valueOf(100)).divide(tongHocPhi, 0, java.math.RoundingMode.HALF_UP).intValue()
                : 0;

        capNhatGiaTriThe(0, MoneyUtils.format(tongHocPhi));
        capNhatGiaTriThe(1, MoneyUtils.format(tongDaNop));
        capNhatGiaTriThe(2, MoneyUtils.format(tongConNo));
        capNhatGiaTriThe(3, phanTram + "%");

        long soHoaDonChuaDong = dsHoaDon.stream()
                .filter(hd -> hd.tinhConNo().compareTo(BigDecimal.ZERO) > 0)
                .count();
        lblPillNhacNo.setText(soHoaDonChuaDong + " nhắc nợ");

        hienThongBaoCongNoNeuCan(tongConNo, dsHoaDon);
    }

    private void capNhatTienDo(List<HoaDonHocPhi> dsHoaDon) {
        if (dsHoaDon.isEmpty()) {
            thanhTienDo.setPhanTram(0);
            lblTienDoChuoi.setText("0đ / 0đ");
            return;
        }
        HoaDonHocPhi hd = dsHoaDon.get(0);
        BigDecimal tong = hd.getSoTien();
        BigDecimal daNop = hd.getDaNop();
        int phanTram = tong.compareTo(BigDecimal.ZERO) > 0
                ? daNop.multiply(BigDecimal.valueOf(100)).divide(tong, 0, java.math.RoundingMode.HALF_UP).intValue()
                : 0;
        thanhTienDo.setPhanTram(Math.min(100, phanTram));
        lblTienDoChuoi.setText(MoneyUtils.format(daNop) + " / " + MoneyUtils.format(tong));
    }

    private void capNhatHoaDonGanNhat(List<HoaDonHocPhi> dsHoaDon) {
        hoaDonGanNhatBox.removeAll();
        HoaDonHocPhi conNoGanNhat = dsHoaDon.stream()
                .filter(hd -> hd.tinhConNo().compareTo(BigDecimal.ZERO) > 0)
                .findFirst().orElse(null);

        if (conNoGanNhat == null) {
            JLabel lblOk = new JLabel("Bạn đã đóng đủ học phí. Không có hóa đơn nào cần thanh toán.");
            lblOk.setFont(UITheme.FONT_BASE);
            lblOk.setForeground(UITheme.TEXT_MUTED);
            hoaDonGanNhatBox.add(lblOk);
        } else {
            RoundedTintPanel dong = new RoundedTintPanel(UITheme.TINT_RED, 14);
            dong.setLayout(new GridBagLayout());
            dong.setAlignmentX(Component.LEFT_ALIGNMENT);
            dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
            dong.setBorder(new EmptyBorder(14, 16, 14, 16));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridy = 0;
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.weighty = 1;

            JLabel lblIcon = new JLabel("\u26A0");
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
            lblIcon.setForeground(UITheme.TEXT_RED);
            gbc.gridx = 0;
            gbc.weightx = 0;
            gbc.insets = new Insets(0, 0, 0, 12);
            dong.add(lblIcon, gbc);

            JPanel giua = new JPanel();
            giua.setOpaque(false);
            giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));
            JLabel lblTen = new JLabel(conNoGanNhat.getTenHocKy());
            lblTen.setFont(UITheme.FONT_BOLD);
            lblTen.setForeground(UITheme.TEXT_RED);
            lblTen.setAlignmentX(Component.LEFT_ALIGNMENT);
            JLabel lblTrangThai = new JLabel(conNoGanNhat.tinhTrangThai().getNhan());
            lblTrangThai.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblTrangThai.setForeground(new Color(0xC6, 0x39, 0x39, 190));
            lblTrangThai.setAlignmentX(Component.LEFT_ALIGNMENT);
            giua.add(lblTen);
            giua.add(lblTrangThai);
            gbc.gridx = 1;
            gbc.weightx = 1;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(0, 0, 0, 12);
            dong.add(giua, gbc);

            JLabel lblTien = new JLabel(MoneyUtils.format(conNoGanNhat.tinhConNo()));
            lblTien.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblTien.setForeground(UITheme.TEXT_RED);
            gbc.gridx = 2;
            gbc.weightx = 0;
            gbc.fill = GridBagConstraints.NONE;
            gbc.insets = new Insets(0, 0, 0, 0);
            dong.add(lblTien, gbc);

            hoaDonGanNhatBox.add(dong);
            hoaDonGanNhatBox.add(Box.createRigidArea(new Dimension(0, 12)));

            JButton btnThanhToan = nutBoTron("\uD83D\uDCB3  Thanh toán ngay", UITheme.PRIMARY);
            btnThanhToan.setAlignmentX(Component.LEFT_ALIGNMENT);
            btnThanhToan.setMaximumSize(new Dimension(220, 42));
            btnThanhToan.addActionListener(e -> {
                Window chaMe = SwingUtilities.getWindowAncestor(this);
                new ChonSoTienThanhToanDialog(chaMe, conNoGanNhat.tinhConNo(), soTienDaChon ->
                        new PaymentMethodDialog(chaMe, conNoGanNhat.getMaHoaDon(), soTienDaChon, this::taiDuLieu).setVisible(true)
                ).setVisible(true);
            });
            hoaDonGanNhatBox.add(btnThanhToan);
        }
        hoaDonGanNhatBox.revalidate();
        hoaDonGanNhatBox.repaint();
    }

    /** Nut bo goc tron, nen mau dac, dung cho "Thanh toan ngay". */
    private JButton nutBoTron(String text, Color mauNen) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? mauNen.darker() : mauNen);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(UITheme.FONT_BOLD);
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Panel bo goc tron, nen 1 mau nhat (tint) - dung lam khung canh bao/nhac no. */
    private static class RoundedTintPanel extends JPanel {
        private final Color mauNen;
        private final int boGoc;

        RoundedTintPanel(Color mauNen, int boGoc) {
            this.mauNen = mauNen;
            this.boGoc = boGoc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(mauNen);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), boGoc, boGoc);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Hien toast canh bao cong no ngay lan dau tai du lieu thanh cong (moi lan dang nhap), neu con no > 0. */
    private void hienThongBaoCongNoNeuCan(BigDecimal tongConNo, List<HoaDonHocPhi> dsHoaDon) {
        if (daHienThiToastCongNo) return;
        daHienThiToastCongNo = true;
        if (tongConNo == null || tongConNo.compareTo(BigDecimal.ZERO) <= 0) return;

        long soHoaDonChuaDong = dsHoaDon.stream()
                .filter(hd -> hd.tinhConNo().compareTo(BigDecimal.ZERO) > 0)
                .count();

        SwingUtilities.invokeLater(() -> {
            Window chaMe = SwingUtilities.getWindowAncestor(SinhVienCongNoPanel.this);
            new ThongBaoCongNoToast(chaMe, taiKhoan.getHoTen(), tongConNo, (int) soHoaDonChuaDong).hienThi();
        });
    }

    private void capNhatGiaTriThe(int index, String giaTriMoi) {
        JPanel the = (JPanel) theNhanh.getComponent(index);
        JLabel lbl = timNhanTheoTen(the, "giaTri");
        if (lbl != null) lbl.setText(giaTriMoi);
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

    private String rootMessage(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }

    /** Panel nen gradient xanh duong, dung lam banner chao mung o tren cung. */
    private static class GradientBannerPanel extends JPanel {
        GradientBannerPanel() { setOpaque(true); }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // Ve nen cha (mau BG_MAIN) truoc de khong bao gio de lo pixel trong suot
            // o vung goc - day chinh la nguyen nhan gay hien tuong "chong hinh" o ban cu.
            g2.setColor(UITheme.BG_MAIN);
            g2.fillRect(0, 0, getWidth(), getHeight());
            GradientPaint gp = new GradientPaint(
                    0, 0, UITheme.PRIMARY_DARK,
                    getWidth(), getHeight(), UITheme.PRIMARY);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
        }
    }

    /** Thanh tien do bo tron ve thu cong (khong dung JTable/JProgressBar de dong bo mau phang). */
    private static class ThanhTienDoBar extends JComponent {
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
                g2.setColor(UITheme.PRIMARY);
                g2.fillRoundRect(0, 0, Math.max(w, h), h, h, h);
            }
            g2.dispose();
        }
    }
    /** JPanel biet "bam sat" chieu rong khung cuon (Scrollable), tranh de trong
     *  khoang trang ben phai khi noi dung hep hon vung hien thi thuc te. */
    private static class KhungCuonToanChieuRong extends JPanel implements Scrollable {
        KhungCuonToanChieuRong(LayoutManager lm) { super(lm); }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int huong, int dir) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int huong, int dir) { return 120; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}