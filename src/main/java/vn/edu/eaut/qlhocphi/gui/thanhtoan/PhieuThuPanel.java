package vn.edu.eaut.qlhocphi.gui.thanhtoan;

import vn.edu.eaut.qlhocphi.bus.CongNoService;
import vn.edu.eaut.qlhocphi.bus.HocPhiService;
import vn.edu.eaut.qlhocphi.bus.ThanhToanService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.dal.PhieuThuDAO;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.PhieuThu;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Man hinh ghi nhan thanh toan hoc phi - ban "desktop quan ly" day du:
 * banner + KPI so lieu that, tra cuu hoa don truoc khi thu (hien Ten SV/con no),
 * canh bao khi so tien vuot qua cong no, va lich su thu gan day toan truong.
 */
public class PhieuThuPanel extends JPanel {
    private static final DateTimeFormatter DMY_HM = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final java.util.function.BiConsumer<String, String> dieuHuongTimKiem;
    private final ThanhToanService thanhToanService = new ThanhToanService();
    private final HocPhiService hocPhiService = new HocPhiService();
    private final CongNoService congNoService = new CongNoService();
    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();

    private JTextField txtMaHoaDon, txtSoTien, txtNguoiThu;
    private JComboBox<String> cboHinhThuc;
    private JLabel lblKetQua;

    private JPanel boxThongTinHoaDon;
    private JLabel lblTenSVTraCuu, lblHocKyTraCuu, lblConNoTraCuu;
    private JButton btnDienDuNo, btnXemHoSoSV;

    private JLabel lblTongDaThu, lblTongConNo, lblSoQuaHan;

    private JPanel khoiLichSuThu;

    /** Hoa don dang duoc tra cuu/chon de thu tien - dung de validate so tien nhap vao. */
    private HoaDonHocPhi hoaDonDangTraCuu;

    public PhieuThuPanel(java.util.function.BiConsumer<String, String> dieuHuongTimKiem) {
        this.dieuHuongTimKiem = dieuHuongTimKiem;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(buildHeader());
        north.add(Box.createRigidArea(new Dimension(0, 16)));
        north.add(buildKpiRow());
        add(north, BorderLayout.NORTH);

        JPanel giua = new JPanel();
        giua.setOpaque(false);
        giua.setLayout(new BoxLayout(giua, BoxLayout.Y_AXIS));

        JPanel haiCot = new JPanel(new GridLayout(1, 2, 16, 0));
        haiCot.setOpaque(false);
        haiCot.setAlignmentX(Component.LEFT_ALIGNMENT);
        haiCot.add(buildFormCard());
        haiCot.add(buildOnlineCard());
        giua.add(haiCot);
        giua.add(Box.createRigidArea(new Dimension(0, 16)));

        JPanel lichSuCard = buildLichSuThuCard();
        lichSuCard.setAlignmentX(Component.LEFT_ALIGNMENT);
        giua.add(lichSuCard);

        JScrollPane scroll = new JScrollPane(giua);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        add(scroll, BorderLayout.CENTER);

        taiKpi();
        taiLichSuThu();
    }

    // ================== HEADER ==================

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
        JLabel tieuDe = new JLabel("Thanh toan hoc phi");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Tra cuu hoa don, ghi nhan thu tien va xem lich su thanh toan");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);

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
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
                FontMetrics fm = g2.getFontMetrics();
                String icon = "\uD83D\uDCB3";
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

    // ================== KPI ==================

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        lblTongDaThu = new JLabel("0 d");
        lblTongConNo = new JLabel("0 d");
        lblSoQuaHan = new JLabel("0");
        row.add(thongKeCard("Tong da thu toan truong", lblTongDaThu, UITheme.SUCCESS));
        row.add(thongKeCard("Tong cong no con lai", lblTongConNo, UITheme.WARNING));
        row.add(thongKeCard("Hoa don qua han", lblSoQuaHan, UITheme.DANGER));
        return row;
    }

    private JPanel thongKeCard(String tieuDe, JLabel giaTri, Color mauNhan) {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        JLabel l1 = new JLabel(tieuDe);
        l1.setFont(UITheme.FONT_BASE);
        l1.setForeground(UITheme.TEXT_MUTED);
        l1.setAlignmentX(Component.LEFT_ALIGNMENT);
        giaTri.setFont(new Font("Segoe UI", Font.BOLD, 22));
        giaTri.setForeground(mauNhan);
        giaTri.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(l1);
        card.add(Box.createRigidArea(new Dimension(0, 6)));
        card.add(giaTri);
        return card;
    }

    private void taiKpi() {
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                BigDecimal daThu = congNoService.tongDaThuTatCa();
                BigDecimal conNo = congNoService.tongConNoToanTruong();
                int quaHan = congNoService.layDanhSachQuaHan().size();
                return new Object[]{daThu, conNo, quaHan};
            }

            @Override
            protected void done() {
                try {
                    Object[] kq = get();
                    lblTongDaThu.setText(MoneyUtils.format((BigDecimal) kq[0]));
                    lblTongConNo.setText(MoneyUtils.format((BigDecimal) kq[1]));
                    lblSoQuaHan.setText(String.valueOf(kq[2]));
                } catch (Exception ex) {
                    lblTongDaThu.setText("--");
                    lblTongConNo.setText("--");
                    lblSoQuaHan.setText("--");
                }
            }
        };
        worker.execute();
    }

    // ================== FORM NOP TRUC TIEP (co tra cuu) ==================

    private JPanel buildFormCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(UITheme.sectionLabel("Nop truc tiep (tien mat / chuyen khoan)"));
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        // Ma hoa don + nut tra cuu tren cung 1 hang
        JLabel lblMaHD = UIUtils.formLabel("Ma hoa don");
        lblMaHD.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblMaHD);
        card.add(Box.createRigidArea(new Dimension(0, 4)));

        JPanel hangMaHD = new JPanel(new BorderLayout(8, 0));
        hangMaHD.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangMaHD.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtMaHoaDon = UIUtils.textField(10);
        JButton btnTraCuu = UITheme.secondaryButton("Tra cuu");
        btnTraCuu.addActionListener(e -> traCuuHoaDon());
        hangMaHD.add(txtMaHoaDon, BorderLayout.CENTER);
        hangMaHD.add(btnTraCuu, BorderLayout.EAST);
        card.add(hangMaHD);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        // Khung thong tin hoa don sau khi tra cuu (mac dinh an noi dung, chi hien khung)
        boxThongTinHoaDon = new JPanel();
        boxThongTinHoaDon.setLayout(new BoxLayout(boxThongTinHoaDon, BoxLayout.Y_AXIS));
        boxThongTinHoaDon.setBackground(UITheme.TINT_BLUE);
        boxThongTinHoaDon.setOpaque(true);
        boxThongTinHoaDon.setAlignmentX(Component.LEFT_ALIGNMENT);
        boxThongTinHoaDon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        boxThongTinHoaDon.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        lblTenSVTraCuu = dongThongTin("Nhap ma hoa don va bam Tra cuu de xem thong tin");
        lblHocKyTraCuu = dongThongTin("");
        lblConNoTraCuu = dongThongTin("");
        boxThongTinHoaDon.add(lblTenSVTraCuu);
        boxThongTinHoaDon.add(lblHocKyTraCuu);
        boxThongTinHoaDon.add(lblConNoTraCuu);
        card.add(boxThongTinHoaDon);
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        // So tien + nut dien du no
        JLabel lblSoTien = UIUtils.formLabel("So tien nop (VND)");
        lblSoTien.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblSoTien);
        card.add(Box.createRigidArea(new Dimension(0, 4)));

        JPanel hangSoTien = new JPanel(new BorderLayout(8, 0));
        hangSoTien.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangSoTien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtSoTien = UIUtils.textField(10);
        btnDienDuNo = UITheme.secondaryButton("Dien du no");
        btnDienDuNo.setEnabled(false);
        btnDienDuNo.addActionListener(e -> dienDuSoNo());

        btnXemHoSoSV = UITheme.secondaryButton("Xem ho so SV");
        btnXemHoSoSV.setEnabled(false);
        btnXemHoSoSV.addActionListener(e -> {
            if (hoaDonDangTraCuu != null && dieuHuongTimKiem != null) {
                dieuHuongTimKiem.accept("sinhvien", hoaDonDangTraCuu.getMaSV());
            }
        });

        JPanel hangNutPhu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        hangNutPhu.setOpaque(false);
        hangNutPhu.add(btnDienDuNo);
        hangNutPhu.add(btnXemHoSoSV);

        hangSoTien.add(txtSoTien, BorderLayout.CENTER);
        hangSoTien.add(hangNutPhu, BorderLayout.EAST);
        card.add(hangSoTien);
        card.add(Box.createRigidArea(new Dimension(0, 14)));

        cboHinhThuc = new JComboBox<>(new String[]{"TIEN_MAT", "CHUYEN_KHOAN"});
        cboHinhThuc.setFont(UITheme.FONT_BASE);
        txtNguoiThu = UIUtils.textField(18);

        themDongCombo(card, "Hinh thuc", cboHinhThuc);
        themDong(card, "Nguoi thu", txtNguoiThu);

        JButton btnGhiNhan = UITheme.primaryButton("Ghi nhan thanh toan");
        btnGhiNhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnGhiNhan.addActionListener(e -> ghiNhanThanhToan());
        card.add(btnGhiNhan);

        card.add(Box.createRigidArea(new Dimension(0, 10)));
        lblKetQua = new JLabel(" ");
        lblKetQua.setFont(UITheme.FONT_BASE);
        lblKetQua.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblKetQua);

        return card;
    }

    private JLabel dongThongTin(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BASE);
        l.setForeground(UITheme.TEXT_BLUE);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    /** Tra cuu hoa don theo ma, hien Ten SV / Hoc ky / Con no ngay tren form truoc khi ghi nhan. */
    private void traCuuHoaDon() {
        String text = txtMaHoaDon.getText().trim();
        int maHoaDon;
        try {
            maHoaDon = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            UIUtils.thongBaoLoi(this, "Ma hoa don phai la so nguyen");
            return;
        }

        SwingWorker<HoaDonHocPhi, Void> worker = new SwingWorker<>() {
            @Override
            protected HoaDonHocPhi doInBackground() throws Exception {
                return hocPhiService.timTheoMa(maHoaDon);
            }

            @Override
            protected void done() {
                try {
                    HoaDonHocPhi hd = get();
                    hoaDonDangTraCuu = hd;
                    if (hd == null) {
                        lblTenSVTraCuu.setText("Khong tim thay hoa don so #" + maHoaDon);
                        lblHocKyTraCuu.setText("");
                        lblConNoTraCuu.setText("");
                        btnDienDuNo.setEnabled(false);
                        btnXemHoSoSV.setEnabled(false);
                        boxThongTinHoaDon.setBackground(UITheme.TINT_RED);
                    } else {
                        lblTenSVTraCuu.setText(hd.getTenSV() + "  (" + hd.getMaSV() + ")");
                        lblHocKyTraCuu.setText("Hoc ky: " + hd.getTenHocKy()
                                + "   -   Tong hoc phi: " + MoneyUtils.format(hd.getSoTien()));
                        BigDecimal conNo = hd.tinhConNo();
                        lblConNoTraCuu.setText("Con no: " + MoneyUtils.format(conNo)
                                + "   -   Trang thai: " + hd.tinhTrangThai().getNhan());
                        btnDienDuNo.setEnabled(conNo.compareTo(BigDecimal.ZERO) > 0);
                        btnXemHoSoSV.setEnabled(true);
                        boxThongTinHoaDon.setBackground(
                                conNo.compareTo(BigDecimal.ZERO) > 0 ? UITheme.TINT_BLUE : UITheme.TINT_GREEN);
                        Color mauChu = conNo.compareTo(BigDecimal.ZERO) > 0 ? UITheme.TEXT_BLUE : UITheme.TEXT_GREEN;
                        lblTenSVTraCuu.setForeground(mauChu);
                        lblHocKyTraCuu.setForeground(mauChu);
                        lblConNoTraCuu.setForeground(mauChu);
                    }
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(PhieuThuPanel.this, "Khong the tra cuu hoa don.");
                }
            }
        };
        worker.execute();
    }

    private void dienDuSoNo() {
        if (hoaDonDangTraCuu == null) return;
        txtSoTien.setText(hoaDonDangTraCuu.tinhConNo().toBigInteger().toString());
    }

    // ================== THANH TOAN ONLINE ==================

    private JPanel buildOnlineCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(UITheme.sectionLabel("Thanh toan online"));
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel moTaBox = new JPanel();
        moTaBox.setLayout(new BoxLayout(moTaBox, BoxLayout.Y_AXIS));
        moTaBox.setBackground(UITheme.TINT_VIOLET);
        moTaBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        moTaBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        moTaBox.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JLabel mota = new JLabel("<html>Mo phong thanh toan qua cong thanh toan online "
                + "(VNPay/MoMo). He thong tao giao dich, xac nhan va tu dong ghi phieu thu.</html>");
        mota.setFont(UITheme.FONT_BASE);
        mota.setForeground(UITheme.TEXT_VIOLET);
        moTaBox.add(mota);
        card.add(moTaBox);
        card.add(Box.createRigidArea(new Dimension(0, 16)));

        JButton btnMoOnline = UITheme.primaryButton("Mo thanh toan online");
        btnMoOnline.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnMoOnline.addActionListener(e -> {
            ThanhToanOnlineDialog dialog = new ThanhToanOnlineDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), thanhToanService);
            dialog.setVisible(true);
            // Sau khi dong dialog (du thanh cong hay khong), lam moi KPI + lich su
            // vi co the da co giao dich moi duoc ghi nhan qua cong online.
            taiKpi();
            taiLichSuThu();
        });
        card.add(btnMoOnline);
        card.add(Box.createVerticalGlue());

        return card;
    }

    // ================== LICH SU THU GAN DAY ==================

    private JPanel buildLichSuThuCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 12));
        card.add(UITheme.sectionLabel("Lich su thu gan day (5 giao dich moi nhat)"), BorderLayout.NORTH);

        khoiLichSuThu = new JPanel();
        khoiLichSuThu.setOpaque(false);
        khoiLichSuThu.setLayout(new BoxLayout(khoiLichSuThu, BoxLayout.Y_AXIS));
        card.add(khoiLichSuThu, BorderLayout.CENTER);

        return card;
    }

    private void taiLichSuThu() {
        SwingWorker<List<PhieuThu>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PhieuThu> doInBackground() throws Exception {
                List<HoaDonHocPhi> tatCaHoaDon = hocPhiService.layTatCaHoaDon();
                List<PhieuThu> tatCaPhieuThu = new ArrayList<>();
                for (HoaDonHocPhi hd : tatCaHoaDon) {
                    tatCaPhieuThu.addAll(phieuThuDAO.layTheoHoaDon(hd.getMaHoaDon()));
                }
                tatCaPhieuThu.sort(Comparator.comparing(PhieuThu::getNgayNop,
                        Comparator.nullsLast(Comparator.reverseOrder())));
                return tatCaPhieuThu.size() > 5 ? tatCaPhieuThu.subList(0, 5) : tatCaPhieuThu;
            }

            @Override
            protected void done() {
                khoiLichSuThu.removeAll();
                try {
                    List<PhieuThu> list = get();
                    if (list.isEmpty()) {
                        JLabel trong = new JLabel("Chua co giao dich thu tien nao");
                        trong.setFont(UITheme.FONT_BASE);
                        trong.setForeground(UITheme.TEXT_MUTED);
                        khoiLichSuThu.add(trong);
                    } else {
                        for (PhieuThu pt : list) {
                            khoiLichSuThu.add(dongLichSuThu(pt));
                        }
                    }
                } catch (Exception ex) {
                    JLabel loi = new JLabel("Khong the tai lich su thu");
                    loi.setForeground(UITheme.TEXT_MUTED);
                    khoiLichSuThu.add(loi);
                }
                khoiLichSuThu.revalidate();
                khoiLichSuThu.repaint();
            }
        };
        worker.execute();
    }

    private JPanel dongLichSuThu(PhieuThu pt) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF0, 0xF2, 0xF6)),
                BorderFactory.createEmptyBorder(8, 4, 8, 4)));

        JPanel trai = new JPanel();
        trai.setOpaque(false);
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));
        JLabel lblMaHD = new JLabel("Hoa don #" + pt.getMaHoaDon() + "  -  " + pt.getHinhThuc());
        lblMaHD.setFont(UITheme.FONT_BOLD);
        lblMaHD.setForeground(UITheme.TEXT_PRIMARY);
        String ngay = pt.getNgayNop() != null ? pt.getNgayNop().format(DMY_HM) : "";
        JLabel lblPhu = new JLabel(ngay + "  -  Nguoi thu: " + pt.getNguoiThu());
        lblPhu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPhu.setForeground(UITheme.TEXT_MUTED);
        trai.add(lblMaHD);
        trai.add(lblPhu);
        row.add(trai, BorderLayout.CENTER);

        JLabel lblTien = UITheme.pill(MoneyUtils.format(pt.getSoTienNop()), UITheme.TINT_GREEN, UITheme.TEXT_GREEN);
        JPanel phaiWrap = new JPanel(new GridBagLayout());
        phaiWrap.setOpaque(false);
        phaiWrap.add(lblTien);
        row.add(phaiWrap, BorderLayout.EAST);

        return row;
    }

    // ================== HELPER FORM ==================

    private void themDong(JPanel card, String label, JComponent field) {
        JLabel l = UIUtils.formLabel(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        card.add(l);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(field);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
    }

    private void themDongCombo(JPanel card, String label, JComboBox<String> combo) {
        JLabel l = UIUtils.formLabel(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setAlignmentX(Component.LEFT_ALIGNMENT);
        combo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        card.add(l);
        card.add(Box.createRigidArea(new Dimension(0, 4)));
        card.add(combo);
        card.add(Box.createRigidArea(new Dimension(0, 14)));
    }

    // ================== GHI NHAN THANH TOAN ==================

    private void ghiNhanThanhToan() {
        try {
            int maHoaDon = Integer.parseInt(txtMaHoaDon.getText().trim());
            BigDecimal soTien = new BigDecimal(txtSoTien.getText().trim());
            String hinhThuc = (String) cboHinhThuc.getSelectedItem();
            String nguoiThu = txtNguoiThu.getText().trim();

            if (soTien.compareTo(BigDecimal.ZERO) <= 0) {
                UIUtils.thongBaoLoi(this, "So tien nop phai lon hon 0");
                return;
            }

            // Canh bao neu so tien vuot qua cong no con lai cua hoa don da tra cuu
            if (hoaDonDangTraCuu != null && hoaDonDangTraCuu.getMaHoaDon() == maHoaDon) {
                BigDecimal conNo = hoaDonDangTraCuu.tinhConNo();
                if (soTien.compareTo(conNo) > 0) {
                    int xacNhan = JOptionPane.showConfirmDialog(this,
                            "So tien nop (" + MoneyUtils.format(soTien) + ") vuot qua cong no con lai ("
                                    + MoneyUtils.format(conNo) + ").\nBan co chac muon tiep tuc?",
                            "Canh bao thu vuot cong no", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (xacNhan != JOptionPane.YES_OPTION) return;
                }
            }

            lblKetQua.setForeground(UITheme.TEXT_MUTED);
            lblKetQua.setText("Dang xu ly...");

            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    thanhToanService.ghiNhanThanhToan(maHoaDon, soTien, hinhThuc, nguoiThu);
                    return null;
                }

                @Override
                protected void done() {
                    try {
                        get();
                        lblKetQua.setForeground(UITheme.SUCCESS);
                        lblKetQua.setText("Ghi nhan thanh cong!");
                        txtSoTien.setText("");
                        taiKpi();
                        taiLichSuThu();
                        if (hoaDonDangTraCuu != null && hoaDonDangTraCuu.getMaHoaDon() == maHoaDon) {
                            traCuuHoaDon(); // lam moi lai thong tin con no vua thay doi
                        }
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        lblKetQua.setForeground(UITheme.DANGER);
                        lblKetQua.setText(cause.getMessage());
                    }
                }
            };
            worker.execute();
        } catch (NumberFormatException ex) {
            UIUtils.thongBaoLoi(this, "Ma hoa don phai la so nguyen, so tien phai la so hop le");
        }
    }
}