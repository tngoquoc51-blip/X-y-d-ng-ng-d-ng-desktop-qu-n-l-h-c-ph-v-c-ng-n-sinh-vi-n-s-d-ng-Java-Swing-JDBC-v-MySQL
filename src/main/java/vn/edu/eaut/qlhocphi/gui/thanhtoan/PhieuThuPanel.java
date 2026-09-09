package vn.edu.eaut.qlhocphi.gui.thanhtoan;

import vn.edu.eaut.qlhocphi.bus.CongNoService;
import vn.edu.eaut.qlhocphi.bus.HocPhiService;
import vn.edu.eaut.qlhocphi.bus.ThanhToanService;
import vn.edu.eaut.qlhocphi.ai.KetQuaOcrBienLai;
import vn.edu.eaut.qlhocphi.ai.OcrBienLaiService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.dal.PhieuThuDAO;
import vn.edu.eaut.qlhocphi.gui.common.AutoRefreshTimer;
import vn.edu.eaut.qlhocphi.gui.common.ChupAnhDialog;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.PhieuThu;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;
import vn.edu.eaut.qlhocphi.util.PDFExporter;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PhieuThuPanel extends JPanel {
    private static final DateTimeFormatter DMY_HM = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ===== Bảng màu riêng cho từng nút - không nút nào trùng màu =====
    // LƯU Ý: 3 màu lấy từ UITheme (PRIMARY/WARNING/SUCCESS) KHÔNG được khai báo "static final"
    // ở đây, vì static final sẽ "đóng băng" giá trị màu ngay lần đầu class được nạp (lúc đó
    // đang chế độ SÁNG) và không bao giờ đổi theo chế độ TỐI nữa dù panel có được tạo lại.
    // Nên gọi thẳng UITheme.PRIMARY / UITheme.WARNING / UITheme.SUCCESS ở nơi sử dụng.
    private static final Color MAU_XEM_HO_SO    = new Color(0x0E, 0xA5, 0xE9);
    private static final Color MAU_LAM_MOI      = new Color(0x64, 0x74, 0x8B);
    private static final Color MAU_ONLINE       = new Color(0x0D, 0x94, 0x88);
    private static final Color MAU_IN_BIEN_LAI  = new Color(0x7C, 0x3A, 0xED);
    private static final Color MAU_XEM_THEM     = new Color(0xDB, 0x27, 0x77);

    private final java.util.function.BiConsumer<String, String> dieuHuongTimKiem;
    private final ThanhToanService thanhToanService = new ThanhToanService();
    private final HocPhiService hocPhiService = new HocPhiService();
    private final CongNoService congNoService = new CongNoService();
    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();
    private final OcrBienLaiService ocrBienLaiService = new OcrBienLaiService();

    private JTextField txtMaHoaDon, txtSoTien, txtNguoiThu;
    private JComboBox<String> cboHinhThuc;
    private JLabel lblKetQua;

    private JPanel boxThongTinHoaDon;
    private JLabel lblTenSVTraCuu, lblHocKyTraCuu, lblConNoTraCuu;
    private JButton btnDienDuNo, btnXemHoSoSV, btnInBienLai;

    private JLabel lblTongDaThu, lblTongConNo, lblSoQuaHan;

    private JPanel khoiLichSuThu;
    private JComboBox<String> cboLocLichSu;
    private JButton btnXemThemLichSu;
    private boolean dangXemNhieuLichSu = false;

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
        AutoRefreshTimer.gan(this, 15, () -> { taiKpi(); taiLichSuThu(); });
    }

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
        JLabel tieuDe = new JLabel("Thanh toán học phí");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Quy trình 3 bước: Tra cứu → Nhập thông tin → Xác nhận thu tiền");
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

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);
        lblTongDaThu = new JLabel("0 đ");
        lblTongConNo = new JLabel("0 đ");
        lblSoQuaHan = new JLabel("0");
        row.add(thongKeCard("Tổng đã thu toàn trường", lblTongDaThu, UITheme.SUCCESS));
        row.add(thongKeCard("Tổng công nợ còn lại", lblTongConNo, UITheme.WARNING));
        row.add(thongKeCard("Hóa đơn quá hạn", lblSoQuaHan, UITheme.DANGER));
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

    private JPanel buildFormCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(UITheme.sectionLabel("Nộp trực tiếp (tiền mặt / chuyển khoản)"));
        card.add(Box.createRigidArea(new Dimension(0, 16)));

        card.add(dongTieuDeBuoc("1", "Tra cứu hóa đơn"));
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel hangMaHD = new JPanel(new BorderLayout(8, 0));
        hangMaHD.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangMaHD.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtMaHoaDon = UIUtils.textField(10);
        txtMaHoaDon.setToolTipText("Nhập mã hóa đơn cần thu tiền");
        JButton btnTraCuu = mauButton("Tra cứu", UITheme.PRIMARY);
        btnTraCuu.addActionListener(e -> traCuuHoaDon());
        JButton btnQuetBienLai = mauButton("Quét biên lai (AI)", MAU_ONLINE);
        btnQuetBienLai.setToolTipText("Chụp hoặc tải ảnh biên lai giấy lên, AI tự đọc số hóa đơn / số tiền để điền sẵn");
        btnQuetBienLai.addActionListener(e -> quetBienLaiOCR());
        JPanel hangNutTraCuu = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        hangNutTraCuu.setOpaque(false);
        hangNutTraCuu.add(btnQuetBienLai);
        hangNutTraCuu.add(btnTraCuu);
        hangMaHD.add(txtMaHoaDon, BorderLayout.CENTER);
        hangMaHD.add(hangNutTraCuu, BorderLayout.EAST);
        card.add(hangMaHD);
        card.add(Box.createRigidArea(new Dimension(0, 10)));

        boxThongTinHoaDon = new JPanel();
        boxThongTinHoaDon.setLayout(new BoxLayout(boxThongTinHoaDon, BoxLayout.Y_AXIS));
        boxThongTinHoaDon.setBackground(UITheme.TINT_BLUE);
        boxThongTinHoaDon.setOpaque(true);
        boxThongTinHoaDon.setAlignmentX(Component.LEFT_ALIGNMENT);
        boxThongTinHoaDon.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        boxThongTinHoaDon.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        lblTenSVTraCuu = dongThongTin("Nhập mã hóa đơn và bấm Tra cứu để xem thông tin");
        lblHocKyTraCuu = dongThongTin("");
        lblConNoTraCuu = dongThongTin("");
        boxThongTinHoaDon.add(lblTenSVTraCuu);
        boxThongTinHoaDon.add(lblHocKyTraCuu);
        boxThongTinHoaDon.add(lblConNoTraCuu);
        card.add(boxThongTinHoaDon);
        card.add(Box.createRigidArea(new Dimension(0, 18)));

        card.add(dongTieuDeBuoc("2", "Nhập thông tin thu tiền"));
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        JLabel lblSoTien = UIUtils.formLabel("Số tiền nộp (VND)");
        lblSoTien.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblSoTien);
        card.add(Box.createRigidArea(new Dimension(0, 4)));

        JPanel hangSoTien = new JPanel(new BorderLayout(8, 0));
        hangSoTien.setAlignmentX(Component.LEFT_ALIGNMENT);
        hangSoTien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtSoTien = UIUtils.textField(10);
        btnDienDuNo = mauButton("Điền đủ nợ", UITheme.WARNING);
        btnDienDuNo.setEnabled(false);
        btnDienDuNo.addActionListener(e -> dienDuSoNo());

        btnXemHoSoSV = mauButton("Xem hồ sơ SV", MAU_XEM_HO_SO);
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

        themDongCombo(card, "Hình thức", cboHinhThuc);
        themDong(card, "Người thu", txtNguoiThu);

        card.add(dongTieuDeBuoc("3", "Xác nhận"));
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel hangNutCuoi = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        hangNutCuoi.setOpaque(false);
        hangNutCuoi.setAlignmentX(Component.LEFT_ALIGNMENT);
        JButton btnGhiNhan = mauButton("Ghi nhận thanh toán", UITheme.SUCCESS);
        btnGhiNhan.addActionListener(e -> ghiNhanThanhToan());
        JButton btnLamMoi = mauButton("Làm mới form", MAU_LAM_MOI);
        btnLamMoi.addActionListener(e -> lamMoiForm());
        btnInBienLai = mauButton("In biên lai", MAU_IN_BIEN_LAI);
        btnInBienLai.setEnabled(false);
        btnInBienLai.addActionListener(e -> inBienLai());
        hangNutCuoi.add(btnGhiNhan);
        hangNutCuoi.add(btnLamMoi);
        hangNutCuoi.add(btnInBienLai);
        card.add(hangNutCuoi);

        card.add(Box.createRigidArea(new Dimension(0, 10)));
        lblKetQua = new JLabel(" ");
        lblKetQua.setFont(UITheme.FONT_BASE);
        lblKetQua.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(lblKetQua);

        return card;
    }

    private JPanel dongTieuDeBuoc(String so, String tieuDe) {
        JPanel dong = new JPanel(new BorderLayout(10, 0));
        dong.setOpaque(false);
        dong.setAlignmentX(Component.LEFT_ALIGNMENT);
        dong.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JLabel lblSo = new JLabel(so, SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblSo.setPreferredSize(new Dimension(22, 22));
        lblSo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblSo.setForeground(Color.WHITE);

        JLabel lblText = new JLabel(tieuDe);
        lblText.setFont(UITheme.FONT_H2);
        lblText.setForeground(UITheme.TEXT_PRIMARY);

        dong.add(lblSo, BorderLayout.WEST);
        dong.add(lblText, BorderLayout.CENTER);
        return dong;
    }

    private JLabel dongThongTin(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BASE);
        l.setForeground(UITheme.TEXT_BLUE);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    /**
     * MỚI: Quét biên lai giấy bằng AI (Gemini Vision) để giảm nhập tay cho kế toán.
     * Luồng: (1) chụp/tải ảnh biên lai → (2) AI đọc → (3) hiện form xác nhận (có thể
     * sửa tay) → (4) tự động điền vào form và tự động tra cứu nếu đọc được số hóa đơn.
     * Không bao giờ tự động ghi thẳng vào CSDL từ kết quả AI.
     */
    private void quetBienLaiOCR() {
        Window chaMe = SwingUtilities.getWindowAncestor(this);
        ChupAnhDialog dlgChup = new ChupAnhDialog(chaMe, "Chụp / tải ảnh biên lai giấy");
        dlgChup.setVisible(true);
        BufferedImage anh = dlgChup.layAnhDaChon();
        if (anh == null) return;

        lblKetQua.setForeground(UITheme.TEXT_MUTED);
        lblKetQua.setText("Đang phân tích ảnh bằng AI, vui lòng đợi...");

        SwingWorker<KetQuaOcrBienLai, Void> worker = new SwingWorker<>() {
            @Override
            protected KetQuaOcrBienLai doInBackground() throws Exception {
                return ocrBienLaiService.docBienLai(anh);
            }

            @Override
            protected void done() {
                try {
                    KetQuaOcrBienLai ocr = get();
                    lblKetQua.setText(" ");
                    XacNhanOcrBienLaiDialog dlgXn = new XacNhanOcrBienLaiDialog(chaMe, ocr);
                    dlgXn.setVisible(true);
                    if (!dlgXn.daXacNhan()) return;

                    Integer maHD = dlgXn.layMaHoaDon();
                    BigDecimal soTien = dlgXn.laySoTien();
                    String hinhThuc = dlgXn.layHinhThuc();

                    if (soTien != null) txtSoTien.setText(soTien.toBigInteger().toString());
                    if (hinhThuc != null) cboHinhThuc.setSelectedItem(hinhThuc);

                    if (maHD != null) {
                        txtMaHoaDon.setText(String.valueOf(maHD));
                        traCuuHoaDon();
                    } else {
                        UIUtils.thongBao(PhieuThuPanel.this,
                                "AI chưa đọc được Số hóa đơn. Đã điền Số tiền (nếu có) - vui lòng nhập tay Mã hóa đơn rồi bấm Tra cứu.");
                    }
                } catch (Exception ex) {
                    lblKetQua.setText(" ");
                    UIUtils.thongBaoLoi(PhieuThuPanel.this, "Lỗi phân tích ảnh bằng AI: " + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void traCuuHoaDon() {
        String text = txtMaHoaDon.getText().trim();
        int maHoaDon;
        try {
            maHoaDon = Integer.parseInt(text);
        } catch (NumberFormatException ex) {
            UIUtils.thongBaoLoi(this, "Mã hóa đơn phải là số nguyên");
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
                        lblTenSVTraCuu.setText("Không tìm thấy hóa đơn số #" + maHoaDon);
                        lblHocKyTraCuu.setText("");
                        lblConNoTraCuu.setText("");
                        btnDienDuNo.setEnabled(false);
                        btnXemHoSoSV.setEnabled(false);
                        boxThongTinHoaDon.setBackground(UITheme.TINT_RED);
                    } else {
                        lblTenSVTraCuu.setText(hd.getTenSV() + "  (" + hd.getMaSV() + ")");
                        lblHocKyTraCuu.setText("Học kỳ: " + hd.getTenHocKy()
                                + "   -   Tổng học phí: " + MoneyUtils.format(hd.getSoTien()));
                        BigDecimal conNo = hd.tinhConNo();
                        lblConNoTraCuu.setText("Còn nợ: " + MoneyUtils.format(conNo)
                                + "   -   Trạng thái: " + hd.tinhTrangThai().getNhan());
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
                    UIUtils.thongBaoLoi(PhieuThuPanel.this, "Không thể tra cứu hóa đơn.");
                }
            }
        };
        worker.execute();
    }

    private void dienDuSoNo() {
        if (hoaDonDangTraCuu == null) return;
        txtSoTien.setText(hoaDonDangTraCuu.tinhConNo().toBigInteger().toString());
    }

    private void lamMoiForm() {
        txtMaHoaDon.setText("");
        txtSoTien.setText("");
        txtNguoiThu.setText("");
        cboHinhThuc.setSelectedIndex(0);
        hoaDonDangTraCuu = null;
        lblTenSVTraCuu.setText("Nhập mã hóa đơn và bấm Tra cứu để xem thông tin");
        lblHocKyTraCuu.setText("");
        lblConNoTraCuu.setText("");
        boxThongTinHoaDon.setBackground(UITheme.TINT_BLUE);
        lblTenSVTraCuu.setForeground(UITheme.TEXT_BLUE);
        btnDienDuNo.setEnabled(false);
        btnXemHoSoSV.setEnabled(false);
        btnInBienLai.setEnabled(false);
        lblKetQua.setText(" ");
    }

    private void inBienLai() {
        if (hoaDonDangTraCuu == null) return;
        int maHoaDon = hoaDonDangTraCuu.getMaHoaDon();

        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("bien_lai_hoa_don_" + maHoaDon + ".pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".pdf")) path += ".pdf";
        String duongDan = path;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                List<PhieuThu> danhSach = phieuThuDAO.layTheoHoaDon(maHoaDon);
                if (danhSach.isEmpty()) throw new IllegalStateException("Hóa đơn này chưa có giao dịch thu tiền nào.");
                PhieuThu ptGanNhat = danhSach.stream()
                        .max(Comparator.comparing(PhieuThu::getNgayNop, Comparator.nullsLast(Comparator.naturalOrder())))
                        .orElse(danhSach.get(danhSach.size() - 1));

                HoaDonHocPhi hd = hocPhiService.timTheoMa(maHoaDon);
                PDFExporter.xuatBienLaiChuyenNghiep(duongDan, "TRƯỜNG ĐẠI HỌC EAUT", ptGanNhat.getMaPhieuThu(),
                        hd.getTenSV(), hd.getMaSV(), hd.getTenHocKy(),
                        hd.getSoTien(), ptGanNhat.getSoTienNop(), hd.tinhConNo(),
                        ptGanNhat.getHinhThuc(), ptGanNhat.getMaGiaoDich(), ptGanNhat.getNguoiThu(),
                        ptGanNhat.getNgayNop() != null ? ptGanNhat.getNgayNop().format(DMY_HM) : "");
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UIUtils.thongBao(PhieuThuPanel.this, "Đã xuất biên lai:\n" + duongDan);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(PhieuThuPanel.this, "Xuất biên lai thất bại: " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private JPanel buildOnlineCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(UITheme.sectionLabel("Thanh toán online"));
        card.add(Box.createRigidArea(new Dimension(0, 8)));

        JPanel moTaBox = new JPanel();
        moTaBox.setLayout(new BoxLayout(moTaBox, BoxLayout.Y_AXIS));
        moTaBox.setBackground(UITheme.TINT_VIOLET);
        moTaBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        moTaBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));
        moTaBox.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));
        JLabel mota = new JLabel("<html>Mô phỏng thanh toán qua cổng thanh toán online "
                + "(VNPay/MoMo). Hệ thống tạo giao dịch, xác nhận và tự động ghi phiếu thu.</html>");
        mota.setFont(UITheme.FONT_BASE);
        mota.setForeground(UITheme.TEXT_VIOLET);
        moTaBox.add(mota);
        card.add(moTaBox);
        card.add(Box.createRigidArea(new Dimension(0, 16)));

        JButton btnMoOnline = mauButton("Mở thanh toán online", MAU_ONLINE);
        btnMoOnline.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnMoOnline.addActionListener(e -> {
            ThanhToanOnlineDialog dialog = new ThanhToanOnlineDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), thanhToanService);
            dialog.setVisible(true);
            taiKpi();
            taiLichSuThu();
        });
        card.add(btnMoOnline);
        card.add(Box.createVerticalGlue());

        return card;
    }

    private JPanel buildLichSuThuCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 12));

        JPanel dongTieuDe = new JPanel(new BorderLayout());
        dongTieuDe.setOpaque(false);
        dongTieuDe.add(UITheme.sectionLabel("Lịch sử thu gần đây"), BorderLayout.WEST);

        JPanel hangLoc = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        hangLoc.setOpaque(false);
        cboLocLichSu = new JComboBox<>(new String[]{"Tất cả hình thức", "TIEN_MAT", "CHUYEN_KHOAN", "THANH_TOAN_ONLINE"});
        cboLocLichSu.setFont(UITheme.FONT_BASE);
        cboLocLichSu.addActionListener(e -> taiLichSuThu());
        btnXemThemLichSu = mauButton("Xem thêm (5 → 20)", MAU_XEM_THEM);
        btnXemThemLichSu.addActionListener(e -> {
            dangXemNhieuLichSu = !dangXemNhieuLichSu;
            btnXemThemLichSu.setText(dangXemNhieuLichSu ? "Thu gọn (20 → 5)" : "Xem thêm (5 → 20)");
            taiLichSuThu();
        });
        hangLoc.add(cboLocLichSu);
        hangLoc.add(btnXemThemLichSu);
        dongTieuDe.add(hangLoc, BorderLayout.EAST);

        card.add(dongTieuDe, BorderLayout.NORTH);

        khoiLichSuThu = new JPanel();
        khoiLichSuThu.setOpaque(false);
        khoiLichSuThu.setLayout(new BoxLayout(khoiLichSuThu, BoxLayout.Y_AXIS));
        card.add(khoiLichSuThu, BorderLayout.CENTER);

        return card;
    }

    private void taiLichSuThu() {
        String hinhThucLoc = cboLocLichSu == null ? "Tất cả hình thức" : (String) cboLocLichSu.getSelectedItem();
        int gioiHan = dangXemNhieuLichSu ? 20 : 5;

        SwingWorker<List<PhieuThu>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PhieuThu> doInBackground() throws Exception {
                List<HoaDonHocPhi> tatCaHoaDon = hocPhiService.layTatCaHoaDon();
                List<PhieuThu> tatCaPhieuThu = new ArrayList<>();
                for (HoaDonHocPhi hd : tatCaHoaDon) {
                    tatCaPhieuThu.addAll(phieuThuDAO.layTheoHoaDon(hd.getMaHoaDon()));
                }
                if (hinhThucLoc != null && !hinhThucLoc.equals("Tất cả hình thức")) {
                    tatCaPhieuThu.removeIf(pt -> !hinhThucLoc.equals(pt.getHinhThuc()));
                }
                tatCaPhieuThu.sort(Comparator.comparing(PhieuThu::getNgayNop,
                        Comparator.nullsLast(Comparator.reverseOrder())));
                return tatCaPhieuThu.size() > gioiHan ? tatCaPhieuThu.subList(0, gioiHan) : tatCaPhieuThu;
            }

            @Override
            protected void done() {
                khoiLichSuThu.removeAll();
                try {
                    List<PhieuThu> list = get();
                    if (list.isEmpty()) {
                        JLabel trong = new JLabel("Không có giao dịch nào phù hợp");
                        trong.setFont(UITheme.FONT_BASE);
                        trong.setForeground(UITheme.TEXT_MUTED);
                        khoiLichSuThu.add(trong);
                    } else {
                        for (PhieuThu pt : list) {
                            khoiLichSuThu.add(dongLichSuThu(pt));
                        }
                    }
                } catch (Exception ex) {
                    JLabel loi = new JLabel("Không thể tải lịch sử thu");
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
        JLabel lblMaHD = new JLabel("Hóa đơn #" + pt.getMaHoaDon() + "  -  " + pt.getHinhThuc());
        lblMaHD.setFont(UITheme.FONT_BOLD);
        lblMaHD.setForeground(UITheme.TEXT_PRIMARY);
        String ngay = pt.getNgayNop() != null ? pt.getNgayNop().format(DMY_HM) : "";
        JLabel lblPhu = new JLabel(ngay + "  -  Người thu: " + pt.getNguoiThu());
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

    private JButton mauButton(String text, Color mau) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(mau);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void ghiNhanThanhToan() {
        try {
            int maHoaDon = Integer.parseInt(txtMaHoaDon.getText().trim());
            BigDecimal soTien = new BigDecimal(txtSoTien.getText().trim());
            String hinhThuc = (String) cboHinhThuc.getSelectedItem();
            String nguoiThu = txtNguoiThu.getText().trim();

            if (soTien.compareTo(BigDecimal.ZERO) <= 0) {
                UIUtils.thongBaoLoi(this, "Số tiền nộp phải lớn hơn 0");
                return;
            }

            if (hoaDonDangTraCuu != null && hoaDonDangTraCuu.getMaHoaDon() == maHoaDon) {
                BigDecimal conNo = hoaDonDangTraCuu.tinhConNo();
                if (soTien.compareTo(conNo) > 0) {
                    int xacNhan = JOptionPane.showConfirmDialog(this,
                            "Số tiền nộp (" + MoneyUtils.format(soTien) + ") vượt quá công nợ còn lại ("
                                    + MoneyUtils.format(conNo) + ").\nBạn có chắc muốn tiếp tục?",
                            "Cảnh báo thu vượt công nợ", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (xacNhan != JOptionPane.YES_OPTION) return;
                }
            }

            lblKetQua.setForeground(UITheme.TEXT_MUTED);
            lblKetQua.setText("Đang xử lý...");

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
                        lblKetQua.setText("Ghi nhận thành công!");
                        txtSoTien.setText("");
                        btnInBienLai.setEnabled(true);
                        taiKpi();
                        taiLichSuThu();
                        if (hoaDonDangTraCuu != null && hoaDonDangTraCuu.getMaHoaDon() == maHoaDon) {
                            traCuuHoaDon();
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
            UIUtils.thongBaoLoi(this, "Mã hóa đơn phải là số nguyên, số tiền phải là số hợp lệ");
        }
    }

    private String rootMessage(Exception ex) {
        Throwable t = ex.getCause() != null ? ex.getCause() : ex;
        return t.getMessage() != null ? t.getMessage() : t.toString();
    }
}