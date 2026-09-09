package vn.edu.eaut.qlhocphi.gui.thanhtoan;

import vn.edu.eaut.qlhocphi.bus.CongNoService;
import vn.edu.eaut.qlhocphi.bus.DoiSoatNganHangService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.AutoRefreshTimer;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.GiaoDichNganHang;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * "Đối soát thanh toán thông minh" - màn hình trung tâm của tính năng tự động
 * thu học phí qua chuyển khoản: nhập sao kê -> engine tự động khớp -> Admin/Kế
 * toán duyệt (từng dòng hoặc hàng loạt cho nhóm tin cậy cao). 3 tab theo trạng
 * thái: Tự động khớp / Nghi vấn / Chưa xử lý, đồng bộ phong cách với CongNoPanel.
 */
public class DoiSoatNganHangPanel extends JPanel {
    private final DoiSoatNganHangService doiSoatService = new DoiSoatNganHangService();
    private final CongNoService congNoService = new CongNoService();
    private final TaiKhoan taiKhoan;

    private JTabbedPane tabs;
    private DefaultTableModel modelTuDongKhop, modelNghiVan, modelChuaXuLy;
    private JTable tableTuDongKhop, tableNghiVan, tableChuaXuLy;
    private JLabel lblTongGiaoDich, lblTuDongKhop, lblNghiVan;
    private JButton btnNhapSaoKe, btnQuetDoiSoat, btnDuyetHangLoat;
    private List<GiaoDichNganHang> danhSachTuDongKhop = new java.util.ArrayList<>();
    private List<GiaoDichNganHang> danhSachNghiVan = new java.util.ArrayList<>();
    private List<GiaoDichNganHang> danhSachChuaXuLy = new java.util.ArrayList<>();
    private List<HoaDonHocPhi> danhSachConNoHienTai = new java.util.ArrayList<>();

    public DoiSoatNganHangPanel(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(buildHeader());
        north.add(Box.createRigidArea(new Dimension(0, 16)));
        north.add(buildKpiRow());
        add(north, BorderLayout.NORTH);

        add(buildTabs(), BorderLayout.CENTER);

        taiDuLieu();
        AutoRefreshTimer.gan(this, 20, this::taiDuLieu);
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
        JLabel tieuDe = new JLabel("Đối Soát Thanh Toán Thông Minh");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Tự động khớp giao dịch chuyển khoản với hóa đơn học phí còn nợ");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.setOpaque(false);

        btnNhapSaoKe = new JButton("Nhập Sao Kê Ngân Hàng");
        styleNutTrang(btnNhapSaoKe);
        btnNhapSaoKe.addActionListener(e -> nhapSaoKe());

        btnQuetDoiSoat = new JButton("Quét Đối Soát");
        styleNutTrang(btnQuetDoiSoat);
        btnQuetDoiSoat.addActionListener(e -> chayQuetDoiSoat());

        actions.add(btnNhapSaoKe);
        actions.add(btnQuetDoiSoat);
        banner.add(actions, BorderLayout.EAST);

        return banner;
    }

    private void styleNutTrang(JButton btn) {
        btn.setFont(UITheme.FONT_BOLD);
        btn.setBackground(Color.WHITE);
        btn.setForeground(UITheme.PRIMARY_DARK);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
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
                String icon = "\uD83E\uDD16"; // robot - the hien "tu dong/AI"
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
        lblTongGiaoDich = new JLabel("0");
        lblTuDongKhop = new JLabel("0");
        lblNghiVan = new JLabel("0");
        row.add(thongKeCard("Tổng Giao Dịch Chưa Xử Lý", lblTongGiaoDich, UITheme.TEXT_MUTED));
        row.add(thongKeCard("Tự Động Khớp (Sẵn Sàng Duyệt)", lblTuDongKhop, UITheme.TEXT_GREEN));
        row.add(thongKeCard("Nghi Vấn - Cần Xem Lại", lblNghiVan, UITheme.WARNING));
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

    // ================== 3 TAB ==================

    private JTabbedPane buildTabs() {
        tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);

        modelTuDongKhop = taoTableModel();
        tableTuDongKhop = taoTable(modelTuDongKhop);
        tabs.addTab("Tự Động Khớp", boiTrongCard(tableTuDongKhop, true, () -> danhSachTuDongKhop));

        modelNghiVan = taoTableModel();
        tableNghiVan = taoTable(modelNghiVan);
        tabs.addTab("Nghi Vấn", boiTrongCard(tableNghiVan, false, () -> danhSachNghiVan));

        modelChuaXuLy = taoTableModel();
        tableChuaXuLy = taoTable(modelChuaXuLy);
        tabs.addTab("Chưa Khớp Được", boiTrongCard(tableChuaXuLy, false, () -> danhSachChuaXuLy));

        return tabs;
    }

    private DefaultTableModel taoTableModel() {
        return new DefaultTableModel(new Object[]{
                "Ngày Giao Dịch", "Nội Dung Chuyển Khoản", "Số Tiền", "Mã SV Gợi Ý", "Độ Tin Cậy", "Mã Giao Dịch Ngân Hàng"
        }, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    private JTable taoTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(4).setCellRenderer(doTinCayRenderer());
        return table;
    }

    private JPanel boiTrongCard(JTable table, boolean coNutDuyetHangLoat, java.util.function.Supplier<List<GiaoDichNganHang>> layDanhSach) {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        footer.setOpaque(false);

        if (coNutDuyetHangLoat) {
            btnDuyetHangLoat = UITheme.primaryButton("Duyệt Tất Cả (Tin Cậy Cao)");
            btnDuyetHangLoat.addActionListener(e -> duyetHangLoat());
            footer.add(btnDuyetHangLoat);
        }

        JButton btnXemChiTiet = UITheme.secondaryButton("Xem & Xử Lý Dòng Đã Chọn");
        btnXemChiTiet.addActionListener(e -> xuLyDongDaChon(table, layDanhSach.get()));
        footer.add(btnXemChiTiet);

        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private DefaultTableCellRenderer doTinCayRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setOpaque(true);
                label.setFont(UITheme.FONT_BOLD);
                label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
                String text = value == null ? "" : value.toString().replace("%", "");
                int diem = 0;
                try { diem = Integer.parseInt(text); } catch (Exception ignored) {}
                Color bg, fg;
                if (diem >= 85) { bg = UITheme.TINT_GREEN; fg = UITheme.TEXT_GREEN; }
                else if (diem >= 55) { bg = new Color(0xFD, 0xF3, 0xDA); fg = UITheme.WARNING; }
                else { bg = new Color(0xEC, 0xEE, 0xF2); fg = UITheme.TEXT_MUTED; }
                if (!isSelected) { label.setBackground(bg); label.setForeground(fg); }
                return label;
            }
        };
    }

    // ================== HANH DONG ==================

    private void nhapSaoKe() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn file sao kê ngân hàng (.xlsx, .xls hoặc .csv)");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "File sao kê (*.xlsx, *.xls, *.csv)", "xlsx", "xls", "csv"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();

        btnNhapSaoKe.setEnabled(false);
        SwingWorker<DoiSoatNganHangService.KetQuaNhap, Void> worker = new SwingWorker<>() {
            @Override
            protected DoiSoatNganHangService.KetQuaNhap doInBackground() throws Exception {
                DoiSoatNganHangService.KetQuaNhap kq = doiSoatService.nhapSaoKe(file);
                doiSoatService.chayDoiSoatTuDong(); // tu dong quet luon sau khi nhap xong
                return kq;
            }

            @Override
            protected void done() {
                btnNhapSaoKe.setEnabled(true);
                try {
                    DoiSoatNganHangService.KetQuaNhap kq = get();
                    UIUtils.thongBao(DoiSoatNganHangPanel.this,
                            "Đã đọc " + kq.tongDong + " dòng.\nThêm mới: " + kq.themMoi
                                    + " | Trùng lặp (bỏ qua): " + kq.trungLap
                                    + (kq.loi > 0 ? " | Lỗi: " + kq.loi : "")
                                    + "\n\nHệ thống đã tự động quét đối soát xong.");
                    taiDuLieu();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(DoiSoatNganHangPanel.this, "Nhập sao kê thất bại: " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void chayQuetDoiSoat() {
        btnQuetDoiSoat.setEnabled(false);
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                doiSoatService.chayDoiSoatTuDong();
                return null;
            }
            @Override
            protected void done() {
                btnQuetDoiSoat.setEnabled(true);
                try {
                    get();
                    UIUtils.thongBao(DoiSoatNganHangPanel.this, "Đã quét đối soát xong.");
                    taiDuLieu();
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(DoiSoatNganHangPanel.this, "Quét đối soát thất bại.");
                }
            }
        };
        worker.execute();
    }

    private void duyetHangLoat() {
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Duyệt TẤT CẢ giao dịch đang ở mục \"Tự động khớp\"?\nHệ thống sẽ tự tạo phiếu thu cho từng hóa đơn tương ứng.",
                "Xác nhận duyệt hàng loạt", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) return;

        btnDuyetHangLoat.setEnabled(false);
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return doiSoatService.duyetHangLoatTuDongKhop(taiKhoan);
            }
            @Override
            protected void done() {
                btnDuyetHangLoat.setEnabled(true);
                try {
                    int soLuong = get();
                    UIUtils.thongBao(DoiSoatNganHangPanel.this, "Đã duyệt và thu thành công " + soLuong + " giao dịch.");
                    taiDuLieu();
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(DoiSoatNganHangPanel.this, "Duyệt hàng loạt thất bại.");
                }
            }
        };
        worker.execute();
    }

    private void xuLyDongDaChon(JTable table, List<GiaoDichNganHang> danhSach) {
        int row = table.getSelectedRow();
        if (row < 0 || row >= danhSach.size()) {
            UIUtils.thongBaoLoi(this, "Vui lòng chọn 1 dòng trong bảng.");
            return;
        }
        GiaoDichNganHang gd = danhSach.get(row);
        GiaoDichXuLyDialog dialog = new GiaoDichXuLyDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), gd, danhSachConNoHienTai, taiKhoan);
        dialog.setVisible(true);
        if (dialog.daXuLy()) {
            taiDuLieu();
        }
    }

    // ================== TAI DU LIEU ==================

    private void taiDuLieu() {
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                List<GiaoDichNganHang> tuDongKhop = doiSoatService.layTheoTrangThai("TU_DONG_KHOP");
                List<GiaoDichNganHang> nghiVan = doiSoatService.layTheoTrangThai("NGHI_VAN");
                List<GiaoDichNganHang> chuaXuLy = doiSoatService.layTheoTrangThai("CHUA_XU_LY");
                List<HoaDonHocPhi> danhSachConNo = congNoService.layDanhSachConNo();
                return new Object[]{tuDongKhop, nghiVan, chuaXuLy, danhSachConNo};
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] kq = get();
                    danhSachTuDongKhop = (List<GiaoDichNganHang>) kq[0];
                    danhSachNghiVan = (List<GiaoDichNganHang>) kq[1];
                    danhSachChuaXuLy = (List<GiaoDichNganHang>) kq[2];
                    danhSachConNoHienTai = (List<HoaDonHocPhi>) kq[3];

                    dienBang(modelTuDongKhop, danhSachTuDongKhop, danhSachConNoHienTai);
                    dienBang(modelNghiVan, danhSachNghiVan, danhSachConNoHienTai);
                    dienBang(modelChuaXuLy, danhSachChuaXuLy, danhSachConNoHienTai);

                    lblTuDongKhop.setText(String.valueOf(danhSachTuDongKhop.size()));
                    lblNghiVan.setText(String.valueOf(danhSachNghiVan.size()));
                    lblTongGiaoDich.setText(String.valueOf(danhSachTuDongKhop.size() + danhSachNghiVan.size() + danhSachChuaXuLy.size()));
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(DoiSoatNganHangPanel.this, "Không thể tải dữ liệu đối soát.");
                }
            }
        };
        worker.execute();
    }

    private static final DateTimeFormatter DMY_HM = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private void dienBang(DefaultTableModel model, List<GiaoDichNganHang> list, List<HoaDonHocPhi> danhSachConNo) {
        model.setRowCount(0);
        for (GiaoDichNganHang gd : list) {
            String maSVGoiY = "-";
            if (gd.getMaHoaDonKhop() != null) {
                maSVGoiY = danhSachConNo.stream()
                        .filter(hd -> hd.getMaHoaDon() == gd.getMaHoaDonKhop())
                        .map(hd -> hd.getMaSV() + " - " + hd.getTenSV())
                        .findFirst().orElse("HD #" + gd.getMaHoaDonKhop());
            }
            model.addRow(new Object[]{
                    gd.getThoiGianGiaoDich() != null ? gd.getThoiGianGiaoDich().format(DMY_HM) : "",
                    gd.getNoiDung(),
                    MoneyUtils.format(gd.getSoTien()),
                    maSVGoiY,
                    gd.getDoTinCay() != null ? String.valueOf(gd.getDoTinCay()) : "-",
                    String.valueOf(gd.getMaGiaoDich())
            });
        }
    }
}