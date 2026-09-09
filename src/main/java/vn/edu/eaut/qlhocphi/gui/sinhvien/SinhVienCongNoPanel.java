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
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Trang "Tong quan" cho Sinh vien - phien ban thiet ke lai voi vong tron tien do
 * (donut ring) ve tay bang Arc2D thay cho o phan tram phang, dong bo phong cach
 * phan tich truc quan voi Dashboard Admin. Giu nguyen ten sinh vien + anh dai dien
 * (AvatarComponent) o banner theo dung yeu cau.
 */
public class SinhVienCongNoPanel extends JPanel {
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CongNoService congNoService = new CongNoService();
    private final SinhVienDAO sinhVienDAO = new SinhVienDAO();
    private final TaiKhoan taiKhoan;
    private final String maSV;

    private vn.edu.eaut.qlhocphi.gui.common.AvatarComponent avatarComponent;
    private JLabel lblTenSV;
    private JLabel lblPillNhacNo;
    private DonutRing donutRing;
    private JLabel lblDonutPhanTram;
    private JLabel lblHocPhi, lblDaDong, lblConNo;
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
        giua.add(buildHangTongQuan());
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

    // ===== Banner chao mung - GIU NGUYEN avatar + ten sinh vien =====
    private JPanel buildBanner() {
        GradientBannerPanel banner = new GradientBannerPanel();
        banner.setLayout(new BorderLayout());
        banner.setPreferredSize(new Dimension(10, 100));
        banner.setBorder(BorderFactory.createEmptyBorder(18, 24, 18, 24));

        avatarComponent = new vn.edu.eaut.qlhocphi.gui.common.AvatarComponent(taiKhoan.getHoTen(), 52);

        JPanel textBox = new JPanel();
        textBox.setOpaque(false);
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));

        JLabel lblChao = new JLabel(loiChao());
        lblChao.setFont(UITheme.FONT_BASE);
        lblChao.setForeground(new Color(255, 255, 255, 210));
        lblChao.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTenSV = new JLabel(taiKhoan.getHoTen());
        lblTenSV.setFont(new Font("Segoe UI", Font.BOLD, 23));
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

        JPanel traiBox = new JPanel(new BorderLayout(16, 0));
        traiBox.setOpaque(false);
        traiBox.add(avatarComponent, BorderLayout.WEST);
        traiBox.add(textBox, BorderLayout.CENTER);
        banner.add(traiBox, BorderLayout.WEST);

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

    // ===== Hang tong quan: 1 the donut lon (Tien do tong) + 3 dong so lieu ben canh =====
    // Thay the cho 4 the phang xep ngang kieu cu - day la diem khac biet chinh so voi
    // cac dashboard thong thuong: dung 1 vong tron truc quan thay vi chi hien so %.
    private JPanel buildHangTongQuan() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(24, 0));

        // ----- Ben trai: vong tron tien do -----
        JPanel khoiDonut = new JPanel(new BorderLayout(0, 6));
        khoiDonut.setOpaque(false);
        JLabel tieuDeDonut = new JLabel("Tổng tiến độ đóng học phí");
        tieuDeDonut.setFont(UITheme.FONT_BOLD);
        tieuDeDonut.setForeground(UITheme.TEXT_PRIMARY);
        khoiDonut.add(tieuDeDonut, BorderLayout.NORTH);

        donutRing = new DonutRing();
        donutRing.setPreferredSize(new Dimension(130, 130));
        JPanel donutWrap = new JPanel(new GridBagLayout());
        donutWrap.setOpaque(false);
        donutWrap.add(donutRing);
        khoiDonut.add(donutWrap, BorderLayout.CENTER);
        khoiDonut.setPreferredSize(new Dimension(170, 170));

        card.add(khoiDonut, BorderLayout.WEST);

        // ----- Duong phan cach doc -----
        JPanel duongKe = new JPanel();
        duongKe.setBackground(UITheme.BORDER);
        duongKe.setPreferredSize(new Dimension(1, 10));
        card.add(duongKe, BorderLayout.CENTER);

        // ----- Ben phai: 3 dong so lieu chi tiet -----
        JPanel khoiSoLieu = new JPanel();
        khoiSoLieu.setOpaque(false);
        khoiSoLieu.setLayout(new BoxLayout(khoiSoLieu, BoxLayout.Y_AXIS));
        khoiSoLieu.setBorder(BorderFactory.createEmptyBorder(4, 24, 4, 0));

        lblHocPhi = dongSoLieuChiTiet("Tổng học phí", "…", UITheme.TEXT_VIOLET);
        lblDaDong = dongSoLieuChiTiet("Đã đóng", "…", UITheme.TEXT_GREEN);
        lblConNo = dongSoLieuChiTiet("Còn nợ", "…", UITheme.TEXT_RED);

        khoiSoLieu.add(lblHocPhi);
        khoiSoLieu.add(Box.createRigidArea(new Dimension(0, 14)));
        khoiSoLieu.add(lblDaDong);
        khoiSoLieu.add(Box.createRigidArea(new Dimension(0, 14)));
        khoiSoLieu.add(lblConNo);

        card.add(khoiSoLieu, BorderLayout.EAST);

        return card;
    }

    /** 1 dong so lieu: cham mau + nhan + gia tri lon, tra ve JLabel de sau nay setText truc tiep vao gia tri. */
    private JLabel dongSoLieuChiTiet(String nhan, String giaTriBanDau, Color mau) {
        JLabel l = new JLabel(dinhDangHtmlSoLieu(nhan, giaTriBanDau, mau));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private String dinhDangHtmlSoLieu(String nhan, String giaTri, Color mau) {
        String hex = String.format("#%02x%02x%02x", mau.getRed(), mau.getGreen(), mau.getBlue());
        return "<html><span style='color:#8a90a6;font-size:12px'>&#9679; " + nhan + "</span><br>"
                + "<span style='color:" + hex + ";font-size:19px;font-weight:bold'>" + giaTri + "</span></html>";
    }

    // ===== Thanh tien do dong hoc phi (hoc ky gan nhat) =====
    private JPanel buildTienDoCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel dongTren = new JPanel(new BorderLayout());
        dongTren.setOpaque(false);
        JLabel lblTieuDe = new JLabel("Tiến độ đóng học phí học kỳ gần nhất");
        lblTieuDe.setFont(UITheme.FONT_BASE);
        lblTieuDe.setForeground(UITheme.TEXT_PRIMARY);
        dongTren.add(lblTieuDe, BorderLayout.WEST);

        lblTienDoChuoi = new JLabel("0 / 0đ");
        lblTienDoChuoi.setFont(UITheme.FONT_BOLD);
        lblTienDoChuoi.setForeground(UITheme.TEXT_MUTED);
        dongTren.add(lblTienDoChuoi, BorderLayout.EAST);

        card.add(dongTren, BorderLayout.NORTH);

        thanhTienDo = new ThanhTienDoBar();
        thanhTienDo.setPreferredSize(new Dimension(10, 12));
        card.add(thanhTienDo, BorderLayout.CENTER);

        return card;
    }

    // ===== 2 the xem nhanh: Hoa don gan nhat can dong / Lich su thanh toan =====
    private JPanel buildHaiCotXemNhanh() {
        // Dung GridBagLayout voi weightx=0.5 + fill=BOTH cho CA 2 O - day la cach CHUAN
        // va DANG TIN CAY nhat trong Swing de ep 2 card luon chia dung 50/50 va LAP DAY
        // TOAN BO chieu rong duoc cap phat, khac phuc trieu de loi "co lai giua man
        // hinh" cua FlowLayout (FlowLayout khong bao gio keo gian component).
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 320));

        final int CAO_MOI_THE = 300;

        JPanel cardHoaDon = UITheme.card();
        cardHoaDon.setLayout(new BorderLayout(0, 14));
        cardHoaDon.add(nhanTieuDeCoVach("HÓA ĐƠN HỌC PHÍ", UITheme.TEXT_RED), BorderLayout.NORTH);
        hoaDonGanNhatBox = new JPanel();
        hoaDonGanNhatBox.setOpaque(false);
        hoaDonGanNhatBox.setLayout(new BoxLayout(hoaDonGanNhatBox, BoxLayout.Y_AXIS));
        cardHoaDon.add(hoaDonGanNhatBox, BorderLayout.CENTER);

        JPanel cardLichSu = UITheme.card();
        cardLichSu.setLayout(new BorderLayout());
        lichSuPanel = new LichSuThanhToanPanel(taiKhoan.getHoTen(), maSV);
        cardLichSu.add(lichSuPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipady = CAO_MOI_THE - 40; // giu chieu cao toi thieu on dinh cho ca 2 card

        gbc.gridx = 0;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, 10);
        row.add(cardHoaDon, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 10, 0, 0);
        row.add(cardLichSu, gbc);

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
                        avatarComponent.capNhatTen(sv.getHoTen());
                        avatarComponent.taiAnh(sv.getAnhDaiDien());
                    }
                    capNhatTongQuan(dsHoaDon);
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

    private void capNhatTongQuan(List<HoaDonHocPhi> dsHoaDon) {
        BigDecimal tongHocPhi = dsHoaDon.stream().map(HoaDonHocPhi::getSoTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tongDaNop = dsHoaDon.stream().map(HoaDonHocPhi::getDaNop)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tongConNo = dsHoaDon.stream().map(HoaDonHocPhi::tinhConNo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        float tiLe = 0f;
        if (tongHocPhi.compareTo(BigDecimal.ZERO) > 0) {
            tiLe = tongDaNop.multiply(BigDecimal.valueOf(1000))
                    .divide(tongHocPhi, 0, java.math.RoundingMode.HALF_UP)
                    .floatValue() / 1000f;
        }
        donutRing.setTiLe(tiLe);

        lblHocPhi.setText(dinhDangHtmlSoLieu("Tổng học phí", MoneyUtils.format(tongHocPhi), UITheme.TEXT_VIOLET));
        lblDaDong.setText(dinhDangHtmlSoLieu("Đã đóng", MoneyUtils.format(tongDaNop), UITheme.TEXT_GREEN));
        lblConNo.setText(dinhDangHtmlSoLieu("Còn nợ", MoneyUtils.format(tongConNo), UITheme.TEXT_RED));

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
            JPanel wrap = new JPanel();
            wrap.setOpaque(false);
            wrap.setLayout(new BoxLayout(wrap, BoxLayout.Y_AXIS));
            wrap.setAlignmentX(Component.CENTER_ALIGNMENT);
            wrap.setBorder(BorderFactory.createEmptyBorder(24, 0, 0, 0));

            JLabel iconTron = new JLabel("\u2705", SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    GradientPaint gp = new GradientPaint(0, 0, UITheme.TINT_GREEN, getWidth(), getHeight(),
                            new Color(0xC8, 0xEF, 0xD8));
                    g2.setPaint(gp);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            iconTron.setOpaque(false);
            iconTron.setForeground(UITheme.SUCCESS);
            iconTron.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
            iconTron.setPreferredSize(new Dimension(76, 76));
            iconTron.setMaximumSize(new Dimension(76, 76));
            iconTron.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblOk = new JLabel("Bạn đã đóng đủ học phí", SwingConstants.CENTER);
            lblOk.setFont(new Font("Segoe UI", Font.BOLD, 16));
            lblOk.setForeground(UITheme.TEXT_PRIMARY);
            lblOk.setAlignmentX(Component.CENTER_ALIGNMENT);
            lblOk.setBorder(BorderFactory.createEmptyBorder(16, 0, 6, 0));

            JLabel lblPhu = new JLabel("Không có hóa đơn nào cần thanh toán", SwingConstants.CENTER);
            lblPhu.setFont(UITheme.FONT_BASE);
            lblPhu.setForeground(UITheme.TEXT_MUTED);
            lblPhu.setAlignmentX(Component.CENTER_ALIGNMENT);

            wrap.add(iconTron);
            wrap.add(lblOk);
            wrap.add(lblPhu);

            JPanel outer = new JPanel(new GridBagLayout());
            outer.setOpaque(false);
            outer.add(wrap);
            hoaDonGanNhatBox.add(outer);
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
                        new PaymentMethodDialog(chaMe, maSV, conNoGanNhat.getMaHoaDon(), soTienDaChon, this::taiDuLieu).setVisible(true)
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

    /** Vong tron tien do (donut ring) - ve tay bang Arc2D, dong bo phong cach voi Dashboard Admin. */
    private static class DonutRing extends JComponent {
        private float tiLe = 0f;

        void setTiLe(float tiLe) {
            this.tiLe = Math.max(0f, Math.min(1f, tiLe));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int duongKinh = Math.min(w, h) - 8;
            int x = (w - duongKinh) / 2, y = (h - duongKinh) / 2;
            int doDayVanh = Math.max(11, duongKinh / 9);

            g2.setStroke(new BasicStroke(doDayVanh, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
            g2.setColor(UITheme.TINT_VIOLET);
            g2.draw(new Ellipse2D.Float(x, y, duongKinh, duongKinh));

            float goc = tiLe * 360f;
            GradientPaint gp = new GradientPaint(x, y, UITheme.PRIMARY.brighter(), x + duongKinh, y + duongKinh, UITheme.PRIMARY);
            g2.setPaint(gp);
            g2.draw(new Arc2D.Float(x, y, duongKinh, duongKinh, 90, -goc, Arc2D.OPEN));
            g2.dispose();

            String phanTram = Math.round(tiLe * 100) + "%";
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g3.setFont(new Font("Segoe UI", Font.BOLD, duongKinh / 4));
            g3.setColor(UITheme.TEXT_PRIMARY);
            FontMetrics fm = g3.getFontMetrics();
            int tx = w / 2 - fm.stringWidth(phanTram) / 2;
            int ty = h / 2 + fm.getAscent() / 2 - 4;
            g3.drawString(phanTram, tx, ty);
            g3.dispose();
        }
    }

    /** Thanh tien do bo tron ve thu cong, co gradient nhe thay vi mau phang. */
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
                GradientPaint gp = new GradientPaint(0, 0, UITheme.PRIMARY_DARK, w, 0, UITheme.PRIMARY);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, Math.max(w, h), h, h, h);
            }
            g2.dispose();
        }
    }

    private static class KhungCuonToanChieuRong extends JPanel implements Scrollable {
        KhungCuonToanChieuRong(LayoutManager lm) { super(lm); }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int huong, int dir) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int huong, int dir) { return 120; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}