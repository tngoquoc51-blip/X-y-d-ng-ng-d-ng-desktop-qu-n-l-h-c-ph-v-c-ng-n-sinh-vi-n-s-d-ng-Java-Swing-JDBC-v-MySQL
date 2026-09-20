package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.bus.CongNoService;
import vn.edu.eaut.qlhocphi.bus.ViDienTuService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.dal.SinhVienDAO;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.gui.thanhtoan.ChonSoTienThanhToanDialog;
import vn.edu.eaut.qlhocphi.gui.thanhtoan.PaymentMethodDialog;
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
import java.util.stream.Collectors;

/**
 * Trang "Tổng quan" cho Sinh viên – thiết kế chuẩn hệ thống quản lý đại học.
 * Phong cách: Modern Sky University, card sạch, số liệu rõ, thao tác nhanh.
 */
public class SinhVienCongNoPanel extends JPanel {

    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CongNoService congNoService = new CongNoService();
    private final ViDienTuService viDienTuService = new ViDienTuService();
    private final SinhVienDAO sinhVienDAO = new SinhVienDAO();
    private final TaiKhoan taiKhoan;
    private final String maSV;
    private final Runnable moTroLyAI;

    // Banner
    private vn.edu.eaut.qlhocphi.gui.common.AvatarComponent avatarComponent;
    private JLabel lblTenSV;
    private JLabel lblPillNhacNo;

    // 4 thẻ số liệu
    private JLabel lblCardTong, lblCardDaDong, lblCardConNo, lblCardVi;

    // Donut + tiến độ
    private DonutRing donutRing;
    private ThanhTienDoBar thanhTienDo;
    private JLabel lblTienDoChuoi;
    private JLabel lblTenHocKyGanNhat;

    // Danh sách hóa đơn + lịch sử
    private JPanel hoaDonBox;
    private LichSuThanhToanPanel lichSuPanel;

    private boolean daHienThiToastCongNo = false;

    public SinhVienCongNoPanel(TaiKhoan taiKhoan, Runnable moTroLyAI) {
        this.taiKhoan = taiKhoan;
        this.maSV = taiKhoan.getMaSV();
        this.moTroLyAI = moTroLyAI;

        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        add(buildBanner(), BorderLayout.NORTH);

        JPanel noiDung = new JPanel();
        noiDung.setOpaque(false);
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setBorder(new EmptyBorder(18, 20, 24, 20));

        noiDung.add(buildHangTheSoLieu());
        noiDung.add(Box.createRigidArea(new Dimension(0, 16)));
        noiDung.add(buildHangDonutVaHanhDong());
        noiDung.add(Box.createRigidArea(new Dimension(0, 16)));
        noiDung.add(buildHaiCotDuoi());

        JScrollPane scroll = new JScrollPane(bocNgoai(noiDung));
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(18);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        taiDuLieu();
    }

    private JPanel bocNgoai(JPanel noiDung) {
        JPanel wrap = new KhungCuonToanChieuRong(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(noiDung, BorderLayout.NORTH);
        return wrap;
    }

    // ===================== BANNER =====================
    // ===================== BANNER =====================
    private JPanel buildBanner() {
        GradientBannerPanel banner = new GradientBannerPanel();
        banner.setLayout(new BorderLayout());
        banner.setPreferredSize(new Dimension(10, 120));
        banner.setMinimumSize(new Dimension(10, 110));
        banner.setBorder(BorderFactory.createEmptyBorder(18, 28, 18, 28));

        // Avatar to hơn, cân với 3 dòng chữ
        avatarComponent = new vn.edu.eaut.qlhocphi.gui.common.AvatarComponent(taiKhoan.getHoTen(), 72);

        JPanel textBox = new JPanel();
        textBox.setOpaque(false);
        textBox.setLayout(new BoxLayout(textBox, BoxLayout.Y_AXIS));
        textBox.setAlignmentY(Component.CENTER_ALIGNMENT);

        JLabel lblChao = new JLabel(loiChao());
        lblChao.setFont(UITheme.FONT_BASE);
        lblChao.setForeground(new Color(255, 255, 255, 210));
        lblChao.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTenSV = new JLabel(taiKhoan.getHoTen());
        lblTenSV.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTenSV.setForeground(Color.WHITE);
        lblTenSV.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTenSV.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));

        JLabel lblNgay = new JLabel(dinhDangNgayHomNay());
        lblNgay.setFont(UITheme.FONT_BASE);
        lblNgay.setForeground(new Color(255, 255, 255, 230));
        lblNgay.setAlignmentX(Component.LEFT_ALIGNMENT);

        textBox.add(lblChao);
        textBox.add(Box.createVerticalStrut(2));
        textBox.add(lblTenSV);
        textBox.add(Box.createVerticalStrut(2));
        textBox.add(lblNgay);

        // Hàng ngang: avatar + chữ, căn giữa theo chiều dọc
        JPanel trai = new JPanel();
        trai.setOpaque(false);
        trai.setLayout(new BoxLayout(trai, BoxLayout.X_AXIS));

        JPanel bocAvatar = new JPanel(new BorderLayout());
        bocAvatar.setOpaque(false);
        bocAvatar.setPreferredSize(new Dimension(72, 72));
        bocAvatar.setMaximumSize(new Dimension(72, 72));
        bocAvatar.setMinimumSize(new Dimension(72, 72));
        bocAvatar.setAlignmentY(Component.CENTER_ALIGNMENT);
        bocAvatar.add(avatarComponent, BorderLayout.CENTER);

        trai.add(bocAvatar);
        trai.add(Box.createRigidArea(new Dimension(18, 0)));
        trai.add(textBox);

        banner.add(trai, BorderLayout.WEST);

        lblPillNhacNo = UITheme.pill("0 nhắc nợ", new Color(255, 255, 255, 45), Color.WHITE);
        lblPillNhacNo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblPillNhacNo.setToolTipText("Nhấn để làm mới dữ liệu");
        lblPillNhacNo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) { taiDuLieu(); }
        });

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        phai.setOpaque(false);
        phai.add(lblPillNhacNo);
        banner.add(phai, BorderLayout.EAST);

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
        return tenThuTiengViet(now.getDayOfWeek()) + ", ngày " + now.format(DMY);
    }

    private String tenThuTiengViet(DayOfWeek d) {
        return switch (d) {
            case MONDAY -> "Thứ Hai";
            case TUESDAY -> "Thứ Ba";
            case WEDNESDAY -> "Thứ Tư";
            case THURSDAY -> "Thứ Năm";
            case FRIDAY -> "Thứ Sáu";
            case SATURDAY -> "Thứ Bảy";
            default -> "Chủ Nhật";
        };
    }

    // ===================== 4 THẺ SỐ LIỆU =====================
    private JPanel buildHangTheSoLieu() {
        JPanel row = new JPanel(new GridLayout(1, 4, 14, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        lblCardTong    = taoTheSoLieu("Tổng học phí", "…", UITheme.TEXT_VIOLET, UITheme.TINT_VIOLET);
        lblCardDaDong  = taoTheSoLieu("Đã đóng", "…", UITheme.TEXT_GREEN, UITheme.TINT_GREEN);
        lblCardConNo   = taoTheSoLieu("Còn nợ", "…", UITheme.TEXT_RED, UITheme.TINT_RED);
        lblCardVi      = taoTheSoLieu("Số dư ví", "…", UITheme.TEXT_BLUE, UITheme.TINT_BLUE);

        row.add(lblCardTong.getParent());
        row.add(lblCardDaDong.getParent());
        row.add(lblCardConNo.getParent());
        row.add(lblCardVi.getParent());
        return row;
    }

    private JLabel taoTheSoLieu(String nhan, String giaTri, Color mauChu, Color mauNen) {
        JPanel card = new RoundedCard(12);
        card.setLayout(new BorderLayout(0, 6));
        card.setBorder(new EmptyBorder(14, 16, 14, 16));
        card.setBackground(UITheme.BG_CARD);

        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNhan.setForeground(UITheme.TEXT_MUTED);

        JLabel lblGiaTri = new JLabel(giaTri);
        lblGiaTri.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblGiaTri.setForeground(mauChu);

        // chấm màu nhỏ
        JPanel cham = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(mauChu);
                g2.fillOval(0, 2, 8, 8);
                g2.dispose();
            }
        };
        cham.setPreferredSize(new Dimension(12, 12));
        cham.setOpaque(false);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        top.setOpaque(false);
        top.add(cham);
        top.add(lblNhan);

        card.add(top, BorderLayout.NORTH);
        card.add(lblGiaTri, BorderLayout.CENTER);

        // lưu reference để setText sau
        lblGiaTri.putClientProperty("card", card);
        return lblGiaTri;
    }

    // ===================== DONUT + HÀNH ĐỘNG NHANH =====================
    private JPanel buildHangDonutVaHanhDong() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));

        // ---- Card trái: Donut + tiến độ học kỳ ----
        JPanel cardTrai = UITheme.card();
        cardTrai.setLayout(new BorderLayout(20, 0));
        cardTrai.setBorder(new EmptyBorder(16, 20, 16, 20));

        // Donut
        JPanel khoiDonut = new JPanel(new BorderLayout(0, 4));
        khoiDonut.setOpaque(false);
        JLabel tieuDeDonut = new JLabel("Tiến độ tổng");
        tieuDeDonut.setFont(new Font("Segoe UI", Font.BOLD, 13));
        tieuDeDonut.setForeground(UITheme.TEXT_PRIMARY);
        tieuDeDonut.setHorizontalAlignment(SwingConstants.CENTER);
        khoiDonut.add(tieuDeDonut, BorderLayout.NORTH);

        donutRing = new DonutRing();
        donutRing.setPreferredSize(new Dimension(120, 120));
        JPanel wrapDonut = new JPanel(new GridBagLayout());
        wrapDonut.setOpaque(false);
        wrapDonut.add(donutRing);
        khoiDonut.add(wrapDonut, BorderLayout.CENTER);
        cardTrai.add(khoiDonut, BorderLayout.WEST);

        // Tiến độ học kỳ gần nhất
        JPanel khoiTienDo = new JPanel();
        khoiTienDo.setOpaque(false);
        khoiTienDo.setLayout(new BoxLayout(khoiTienDo, BoxLayout.Y_AXIS));

        lblTenHocKyGanNhat = new JLabel("Học kỳ gần nhất");
        lblTenHocKyGanNhat.setFont(UITheme.FONT_BOLD);
        lblTenHocKyGanNhat.setForeground(UITheme.TEXT_PRIMARY);
        lblTenHocKyGanNhat.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblTienDoChuoi = new JLabel("0đ / 0đ");
        lblTienDoChuoi.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblTienDoChuoi.setForeground(UITheme.TEXT_MUTED);
        lblTienDoChuoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTienDoChuoi.setBorder(new EmptyBorder(4, 0, 10, 0));

        thanhTienDo = new ThanhTienDoBar();
        thanhTienDo.setPreferredSize(new Dimension(10, 10));
        thanhTienDo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        thanhTienDo.setAlignmentX(Component.LEFT_ALIGNMENT);

        khoiTienDo.add(lblTenHocKyGanNhat);
        khoiTienDo.add(lblTienDoChuoi);
        khoiTienDo.add(thanhTienDo);
        khoiTienDo.add(Box.createVerticalGlue());

        cardTrai.add(khoiTienDo, BorderLayout.CENTER);

        // ---- Card phải: Quick Actions ----
        JPanel cardPhai = UITheme.card();
        cardPhai.setLayout(new BorderLayout(0, 12));
        cardPhai.setBorder(new EmptyBorder(16, 18, 16, 18));

        JLabel lblHanhDong = new JLabel("Thao tác nhanh");
        lblHanhDong.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHanhDong.setForeground(UITheme.TEXT_PRIMARY);
        cardPhai.add(lblHanhDong, BorderLayout.NORTH);

        JPanel nutBox = new JPanel(new GridLayout(2, 2, 10, 10));
        nutBox.setOpaque(false);

        nutBox.add(taoNutHanhDong("💳  Thanh toán", UITheme.PRIMARY, e -> moThanhToanNhanh()));
        nutBox.add(taoNutHanhDong("👛  Nạp ví", UITheme.ACCENT_TEAL, e -> {
            // mở trang ví nếu có callback, tạm thời thông báo
            UIUtils.thongBao(this, "Chuyển sang mục Ví Học Phí để nạp tiền.");
        }));
        nutBox.add(taoNutHanhDong("🤖  Trợ lý AI", UITheme.TEXT_VIOLET, e -> {
            if (moTroLyAI != null) moTroLyAI.run();
        }));
        nutBox.add(taoNutHanhDong("🔄  Làm mới", UITheme.TEXT_MUTED, e -> taiDuLieu()));

        cardPhai.add(nutBox, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 8);
        gbc.gridx = 0;
        gbc.weightx = 0.62;
        row.add(cardTrai, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.38;
        gbc.insets = new Insets(0, 8, 0, 0);
        row.add(cardPhai, gbc);

        return row;
    }

    private JButton taoNutHanhDong(String text, Color mau, java.awt.event.ActionListener al) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = getModel().isRollover() ? mau.darker() : mau;
                if (mau.equals(UITheme.TEXT_MUTED)) {
                    g2.setColor(UITheme.BORDER);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(UITheme.TEXT_PRIMARY);
                } else {
                    g2.setColor(bg);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(Color.WHITE);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(10, 40));
        b.addActionListener(al);
        return b;
    }

    private void moThanhToanNhanh() {
        // tìm hóa đơn còn nợ đầu tiên
        try {
            List<HoaDonHocPhi> ds = congNoService.layTheoSinhVien(maSV);
            HoaDonHocPhi hd = ds.stream()
                    .filter(h -> h.tinhConNo().compareTo(BigDecimal.ZERO) > 0)
                    .findFirst().orElse(null);
            if (hd == null) {
                UIUtils.thongBao(this, "Bạn không còn hóa đơn nào cần thanh toán.");
                return;
            }
            Window cha = SwingUtilities.getWindowAncestor(this);
            new ChonSoTienThanhToanDialog(cha, hd.tinhConNo(), soTien ->
                    new PaymentMethodDialog(cha, maSV, hd.getMaHoaDon(), soTien, this::taiDuLieu).setVisible(true)
            ).setVisible(true);
        } catch (Exception ex) {
            UIUtils.thongBaoLoi(this, "Không thể mở thanh toán.\n" + ex.getMessage());
        }
    }

    // ===================== HAI CỘT DƯỚI =====================
    private JPanel buildHaiCotDuoi() {
        JPanel row = new JPanel(new GridBagLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));

        // Cột trái: Hóa đơn chưa đóng
        JPanel cardHoaDon = UITheme.card();
        cardHoaDon.setLayout(new BorderLayout(0, 12));
        cardHoaDon.setBorder(new EmptyBorder(16, 16, 16, 16));
        cardHoaDon.add(nhanTieuDeCoVach("HÓA ĐƠN CẦN THANH TOÁN", UITheme.TEXT_RED), BorderLayout.NORTH);

        hoaDonBox = new JPanel();
        hoaDonBox.setOpaque(false);
        hoaDonBox.setLayout(new BoxLayout(hoaDonBox, BoxLayout.Y_AXIS));
        JScrollPane scrollHD = new JScrollPane(hoaDonBox);
        scrollHD.setBorder(null);
        scrollHD.setOpaque(false);
        scrollHD.getViewport().setOpaque(false);
        scrollHD.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        cardHoaDon.add(scrollHD, BorderLayout.CENTER);

        // Cột phải: Lịch sử
        JPanel cardLichSu = UITheme.card();
        cardLichSu.setLayout(new BorderLayout());
        cardLichSu.setBorder(new EmptyBorder(8, 8, 8, 8));
        lichSuPanel = new LichSuThanhToanPanel(taiKhoan.getHoTen(), maSV);
        cardLichSu.add(lichSuPanel, BorderLayout.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.ipady = 280;

        gbc.gridx = 0;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 0, 0, 8);
        row.add(cardHoaDon, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.5;
        gbc.insets = new Insets(0, 8, 0, 0);
        row.add(cardLichSu, gbc);

        return row;
    }

    private JPanel nhanTieuDeCoVach(String text, Color mauVach) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        JPanel vach = new JPanel();
        vach.setBackground(mauVach);
        vach.setPreferredSize(new Dimension(4, 16));
        p.add(vach, BorderLayout.WEST);
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    // ===================== TẢI DỮ LIỆU =====================
    private void taiDuLieu() {
        if (maSV == null || maSV.isBlank()) {
            lblTenSV.setText(taiKhoan.getHoTen() + "  (Chưa gán Mã SV - liên hệ Admin)");
            return;
        }

        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                SinhVien sv = sinhVienDAO.timTheoMa(maSV);
                List<HoaDonHocPhi> dsHoaDon = congNoService.layTheoSinhVien(maSV);
                BigDecimal soDuVi = viDienTuService.laySoDu(maSV);
                return new Object[]{sv, dsHoaDon, soDuVi};
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] kq = get();
                    SinhVien sv = (SinhVien) kq[0];
                    List<HoaDonHocPhi> ds = (List<HoaDonHocPhi>) kq[1];
                    BigDecimal soDu = (BigDecimal) kq[2];

                    if (sv != null) {
                        lblTenSV.setText(sv.getHoTen() + "  (" + sv.getMaSV() + " - " + sv.getLop() + ")");
                        avatarComponent.capNhatTen(sv.getHoTen());
                        avatarComponent.taiAnh(sv.getAnhDaiDien());
                    }
                    capNhatTheSoLieu(ds, soDu);
                    capNhatDonutVaTienDo(ds);
                    capNhatDanhSachHoaDon(ds);
                    lichSuPanel.taiDuLieu(ds);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(SinhVienCongNoPanel.this,
                            "Không thể tải dữ liệu công nợ.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void capNhatTheSoLieu(List<HoaDonHocPhi> ds, BigDecimal soDuVi) {
        BigDecimal tong = ds.stream().map(HoaDonHocPhi::getSoTien).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal daNop = ds.stream().map(HoaDonHocPhi::getDaNop).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal conNo = ds.stream().map(HoaDonHocPhi::tinhConNo).reduce(BigDecimal.ZERO, BigDecimal::add);

        lblCardTong.setText(MoneyUtils.format(tong));
        lblCardDaDong.setText(MoneyUtils.format(daNop));
        lblCardConNo.setText(MoneyUtils.format(conNo));
        lblCardVi.setText(MoneyUtils.format(soDuVi != null ? soDuVi : BigDecimal.ZERO));

        // Đổi màu "Còn nợ" khi = 0
        if (conNo.compareTo(BigDecimal.ZERO) <= 0) {
            lblCardConNo.setForeground(UITheme.TEXT_GREEN);
        } else {
            lblCardConNo.setForeground(UITheme.TEXT_RED);
        }

        long soChuaDong = ds.stream().filter(hd -> hd.tinhConNo().compareTo(BigDecimal.ZERO) > 0).count();
        lblPillNhacNo.setText(soChuaDong + " nhắc nợ");

        hienThongBaoCongNoNeuCan(conNo, ds);
    }

    private void capNhatDonutVaTienDo(List<HoaDonHocPhi> ds) {
        BigDecimal tong = ds.stream().map(HoaDonHocPhi::getSoTien).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal daNop = ds.stream().map(HoaDonHocPhi::getDaNop).reduce(BigDecimal.ZERO, BigDecimal::add);

        float tiLe = 0f;
        if (tong.compareTo(BigDecimal.ZERO) > 0) {
            tiLe = daNop.multiply(BigDecimal.valueOf(1000))
                    .divide(tong, 0, java.math.RoundingMode.HALF_UP)
                    .floatValue() / 1000f;
        }
        donutRing.setTiLe(tiLe);

        if (ds.isEmpty()) {
            thanhTienDo.setPhanTram(0);
            lblTienDoChuoi.setText("0đ / 0đ");
            lblTenHocKyGanNhat.setText("Chưa có học kỳ");
            return;
        }
        HoaDonHocPhi hd = ds.get(0);
        lblTenHocKyGanNhat.setText(hd.getTenHocKy() != null ? hd.getTenHocKy() : "Học kỳ gần nhất");
        BigDecimal tongHd = hd.getSoTien();
        BigDecimal da = hd.getDaNop();
        int pt = tongHd.compareTo(BigDecimal.ZERO) > 0
                ? da.multiply(BigDecimal.valueOf(100)).divide(tongHd, 0, java.math.RoundingMode.HALF_UP).intValue()
                : 0;
        thanhTienDo.setPhanTram(Math.min(100, pt));
        lblTienDoChuoi.setText(MoneyUtils.format(da) + " / " + MoneyUtils.format(tongHd));
    }

    private void capNhatDanhSachHoaDon(List<HoaDonHocPhi> ds) {
        hoaDonBox.removeAll();

        List<HoaDonHocPhi> chuaDong = ds.stream()
                .filter(hd -> hd.tinhConNo().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());

        if (chuaDong.isEmpty()) {
            JPanel empty = new JPanel();
            empty.setOpaque(false);
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
            empty.setBorder(new EmptyBorder(30, 0, 0, 0));

            JLabel icon = new JLabel("✅", SwingConstants.CENTER);
            icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 36));
            icon.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel tieuDe = new JLabel("Bạn đã đóng đủ học phí", SwingConstants.CENTER);
            tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 15));
            tieuDe.setForeground(UITheme.TEXT_PRIMARY);
            tieuDe.setAlignmentX(Component.CENTER_ALIGNMENT);
            tieuDe.setBorder(new EmptyBorder(12, 0, 4, 0));

            JLabel phu = new JLabel("Không có hóa đơn nào cần thanh toán", SwingConstants.CENTER);
            phu.setFont(UITheme.FONT_BASE);
            phu.setForeground(UITheme.TEXT_MUTED);
            phu.setAlignmentX(Component.CENTER_ALIGNMENT);

            empty.add(icon);
            empty.add(tieuDe);
            empty.add(phu);

            JPanel outer = new JPanel(new GridBagLayout());
            outer.setOpaque(false);
            outer.add(empty);
            hoaDonBox.add(outer);
        } else {
            for (HoaDonHocPhi hd : chuaDong) {
                hoaDonBox.add(taoDongHoaDon(hd));
                hoaDonBox.add(Box.createRigidArea(new Dimension(0, 10)));
            }
        }
        hoaDonBox.revalidate();
        hoaDonBox.repaint();
    }

    private JPanel taoDongHoaDon(HoaDonHocPhi hd) {
        RoundedTintPanel dong = new RoundedTintPanel(UITheme.TINT_RED, 12);
        dong.setLayout(new BorderLayout(12, 0));
        dong.setBorder(new EmptyBorder(12, 14, 12, 14));
        dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        dong.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel trai = new JPanel();
        trai.setOpaque(false);
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));

        JLabel lblTen = new JLabel(hd.getTenHocKy() != null ? hd.getTenHocKy() : "Hóa đơn #" + hd.getMaHoaDon());
        lblTen.setFont(UITheme.FONT_BOLD);
        lblTen.setForeground(UITheme.TEXT_RED);
        lblTen.setAlignmentX(Component.LEFT_ALIGNMENT);

        String han = hd.getHanThanhToan() != null ? "Hạn: " + hd.getHanThanhToan().format(DMY) : "";
        JLabel lblHan = new JLabel(han + "  •  " + hd.tinhTrangThai().getNhan());
        lblHan.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHan.setForeground(new Color(0xC6, 0x39, 0x39, 180));
        lblHan.setAlignmentX(Component.LEFT_ALIGNMENT);

        trai.add(lblTen);
        trai.add(Box.createRigidArea(new Dimension(0, 3)));
        trai.add(lblHan);

        JLabel lblTien = new JLabel(MoneyUtils.format(hd.tinhConNo()));
        lblTien.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTien.setForeground(UITheme.TEXT_RED);

        JButton btn = nutBoTron("Thanh toán", UITheme.PRIMARY);
        btn.setPreferredSize(new Dimension(110, 34));
        btn.addActionListener(e -> {
            Window cha = SwingUtilities.getWindowAncestor(this);
            new ChonSoTienThanhToanDialog(cha, hd.tinhConNo(), soTien ->
                    new PaymentMethodDialog(cha, maSV, hd.getMaHoaDon(), soTien, this::taiDuLieu).setVisible(true)
            ).setVisible(true);
        });

        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        phai.setOpaque(false);
        phai.add(lblTien);
        phai.add(btn);

        dong.add(trai, BorderLayout.CENTER);
        dong.add(phai, BorderLayout.EAST);
        return dong;
    }

    private JButton nutBoTron(String text, Color mauNen) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover() ? mauNen.darker() : mauNen);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void hienThongBaoCongNoNeuCan(BigDecimal tongConNo, List<HoaDonHocPhi> ds) {
        if (daHienThiToastCongNo) return;
        daHienThiToastCongNo = true;
        if (tongConNo == null || tongConNo.compareTo(BigDecimal.ZERO) <= 0) return;

        long so = ds.stream().filter(hd -> hd.tinhConNo().compareTo(BigDecimal.ZERO) > 0).count();
        SwingUtilities.invokeLater(() -> {
            Window cha = SwingUtilities.getWindowAncestor(SinhVienCongNoPanel.this);
            new ThongBaoCongNoToast(cha, taiKhoan.getHoTen(), tongConNo, (int) so).hienThi();
        });
    }

    private String rootMessage(Exception ex) {
        Throwable c = ex.getCause() != null ? ex.getCause() : ex;
        return c.getMessage() != null ? c.getMessage() : c.toString();
    }

    // ===================== INNER CLASSES =====================
    private static class GradientBannerPanel extends JPanel {
        GradientBannerPanel() { setOpaque(true); }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(UITheme.BG_MAIN);
            g2.fillRect(0, 0, getWidth(), getHeight());
            GradientPaint gp = new GradientPaint(0, 0, UITheme.PRIMARY_DARK, getWidth(), getHeight(), UITheme.PRIMARY);
            g2.setPaint(gp);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
        }
    }

    private static class DonutRing extends JComponent {
        private float tiLe = 0f;
        void setTiLe(float t) {
            this.tiLe = Math.max(0f, Math.min(1f, t));
            repaint();
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            int d = Math.min(w, h) - 6;
            int x = (w - d) / 2, y = (h - d) / 2;
            int day = Math.max(10, d / 9);

            g2.setStroke(new BasicStroke(day, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(UITheme.TINT_VIOLET);
            g2.draw(new Ellipse2D.Float(x, y, d, d));

            float goc = tiLe * 360f;
            GradientPaint gp = new GradientPaint(x, y, UITheme.PRIMARY.brighter(), x + d, y + d, UITheme.PRIMARY);
            g2.setPaint(gp);
            g2.draw(new Arc2D.Float(x, y, d, d, 90, -goc, Arc2D.OPEN));
            g2.dispose();

            String pt = Math.round(tiLe * 100) + "%";
            Graphics2D g3 = (Graphics2D) g.create();
            g3.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g3.setFont(new Font("Segoe UI", Font.BOLD, d / 4));
            g3.setColor(UITheme.TEXT_PRIMARY);
            FontMetrics fm = g3.getFontMetrics();
            g3.drawString(pt, w / 2 - fm.stringWidth(pt) / 2, h / 2 + fm.getAscent() / 2 - 3);
            g3.dispose();
        }
    }

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

    private static class RoundedTintPanel extends JPanel {
        private final Color mau;
        private final int radius;
        RoundedTintPanel(Color mau, int radius) {
            this.mau = mau;
            this.radius = radius;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(mau);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class RoundedCard extends JPanel {
        private final int radius;
        RoundedCard(int radius) {
            this.radius = radius;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(UITheme.BG_CARD);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.setColor(UITheme.BORDER);
            g2.setStroke(new BasicStroke(1f));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    private static class KhungCuonToanChieuRong extends JPanel implements Scrollable {
        KhungCuonToanChieuRong(LayoutManager lm) { super(lm); }
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int o, int d) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int o, int d) { return 120; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}