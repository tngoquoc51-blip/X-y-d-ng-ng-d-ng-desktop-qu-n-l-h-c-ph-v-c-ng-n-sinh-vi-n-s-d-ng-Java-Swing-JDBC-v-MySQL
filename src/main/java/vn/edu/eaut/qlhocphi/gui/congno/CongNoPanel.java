package vn.edu.eaut.qlhocphi.gui.congno;

import vn.edu.eaut.qlhocphi.bus.CongNoService;
import vn.edu.eaut.qlhocphi.bus.NhacNoTuDongService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.dal.SinhVienDAO;
import vn.edu.eaut.qlhocphi.gui.common.QuetQRDialog;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.SinhVien;
import vn.edu.eaut.qlhocphi.model.TrangThaiHoaDon;
import vn.edu.eaut.qlhocphi.util.EmailUtils;
import vn.edu.eaut.qlhocphi.util.ExcelExporter;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;
import vn.edu.eaut.qlhocphi.util.PDFExporter;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CongNoPanel extends JPanel {
    private final CongNoService congNoService = new CongNoService();
    private final SinhVienDAO sinhVienDAO = new SinhVienDAO();
    private final java.util.function.BiConsumer<String, String> dieuHuongTimKiem;

    private static final int CHU_KY_TU_DONG_CAP_NHAT_MS = 20_000;
    private javax.swing.Timer timerTuDongCapNhat;

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblTongNo, lblTongThu, lblSoHoaDonNo;
    private JTextField txtTimKiem;
    private JComboBox<String> cboTrangThai;
    private JLabel lblSoLuong;
    private JLabel lblTrangHienTai;
    private JButton btnTrangTruoc, btnTrangSau;
    private int trangHienTai = 0;
    private static final int SO_DONG_MOI_TRANG = 20;
    private List<HoaDonHocPhi> danhSachDaLoc = new ArrayList<>();

    private List<HoaDonHocPhi> danhSachHienTai = new ArrayList<>();

    public CongNoPanel(java.util.function.BiConsumer<String, String> dieuHuongTimKiem) {
        this.dieuHuongTimKiem = dieuHuongTimKiem;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(buildHeader());
        north.add(Box.createRigidArea(new Dimension(0, 16)));
        north.add(buildThongKeCards());
        north.add(Box.createRigidArea(new Dimension(0, 16)));
        north.add(buildToolbar());
        add(north, BorderLayout.NORTH);

        add(buildTableCard(), BorderLayout.CENTER);

        taiDuLieu();
        batDauTuDongCapNhat();
    }

    private void batDauTuDongCapNhat() {
        timerTuDongCapNhat = new javax.swing.Timer(CHU_KY_TU_DONG_CAP_NHAT_MS, e -> {
            if (isShowing()) {
                taiDuLieu();
            }
        });
        timerTuDongCapNhat.setRepeats(true);
        timerTuDongCapNhat.start();

        addHierarchyListener(e -> {
            if (!isDisplayable() && timerTuDongCapNhat != null) {
                timerTuDongCapNhat.stop();
            }
        });
    }

    /** Nut chuc nang to mau dam rieng biet - moi nut 1 mau, khong con dung
     *  "secondaryButton" (vien trang nhat, de bi mo) nhu truoc. */
    private JButton nutMauSac(String text, Color mauNen) {
        JButton b = new JButton(text);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        b.setBackground(mauNen);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(9, 16, 9, 16));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Nut "nguy hiem" (hanh dong hang loat/canh bao) tu ve bang Graphics2D thay
     *  vi dua vao setBackground() cua JButton thuong - tranh bi LookAndFeel he
     *  thong (Windows) de them lop gradient bong len tren lam mau bi "loa" sang,
     *  giam do tuong phan voi chu trang, kho doc. */
    private JButton nutNguyHiemToVe(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color mau = getModel().isRollover() ? UITheme.DANGER.darker() : UITheme.DANGER;
                g2.setColor(mau);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(UITheme.FONT_BOLD);
        b.setForeground(Color.WHITE);
        b.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
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
        JLabel tieuDe = new JLabel("Công nợ học phí");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Tra cứu, cảnh báo và nhắc nợ sinh viên còn nợ học phí");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);

        JButton btnLamMoi = new JButton("Làm mới");
        btnLamMoi.setFont(UITheme.FONT_BOLD);
        btnLamMoi.setBackground(Color.WHITE);
        btnLamMoi.setForeground(UITheme.PRIMARY_DARK);
        btnLamMoi.setFocusPainted(false);
        btnLamMoi.setBorderPainted(false);
        btnLamMoi.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnLamMoi.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLamMoi.addActionListener(e -> taiDuLieu());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(btnLamMoi);
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
                String icon = "\u26A0";
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

    private JPanel buildThongKeCards() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);

        lblSoHoaDonNo = new JLabel("0");
        lblTongNo = new JLabel("0 đ");
        lblTongThu = new JLabel("0 đ");

        row.add(thongKeCard("Số hóa đơn còn nợ", lblSoHoaDonNo, UITheme.WARNING));
        row.add(thongKeCard("Tổng công nợ toàn trường", lblTongNo, UITheme.DANGER));
        row.add(thongKeCard("Tổng đã thu", lblTongThu, UITheme.SUCCESS));
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

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new GridBagLayout());
        toolbar.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 8);
        int col = 0;

        gbc.gridx = col++;
        toolbar.add(UIUtils.formLabel("Tìm kiếm:"), gbc);

        txtTimKiem = UIUtils.textField(15);
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { apDungBoLoc(); }
            @Override public void removeUpdate(DocumentEvent e) { apDungBoLoc(); }
            @Override public void changedUpdate(DocumentEvent e) { apDungBoLoc(); }
        });
        gbc.gridx = col++;
        toolbar.add(txtTimKiem, gbc);

        JButton btnQuetQR = UITheme.accentButton("Quét QR thẻ SV");
        btnQuetQR.setToolTipText("Quét mã QR trên thẻ sinh viên bằng camera để tra cứu công nợ tức thì, khỏi gõ tay mã SV");
        btnQuetQR.addActionListener(e -> quetQRTimKiem());
        gbc.gridx = col++;
        toolbar.add(btnQuetQR, gbc);

        gbc.gridx = col++;
        toolbar.add(UIUtils.formLabel("Trạng thái:"), gbc);

        cboTrangThai = new JComboBox<>(new String[]{"Tất cả trạng thái", "Đóng một phần", "Quá hạn"});
        cboTrangThai.setFont(UITheme.FONT_BASE);
        cboTrangThai.addActionListener(e -> apDungBoLoc());
        gbc.gridx = col++;
        toolbar.add(cboTrangThai, gbc);

        gbc.gridx = col++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        toolbar.add(Box.createHorizontalGlue(), gbc);
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        JButton btnNhacNo = nutMauSac("Gửi nhắc nợ", UITheme.SUCCESS);
        btnNhacNo.addActionListener(e -> guiNhacNoDaChon());
        gbc.gridx = col++;
        toolbar.add(btnNhacNo, gbc);

        JButton btnNhacNoHangLoat = nutNguyHiemToVe("Gửi nhắc nợ TẤT CẢ quá hạn");
        btnNhacNoHangLoat.addActionListener(e -> guiNhacNoHangLoat());
        gbc.gridx = col++;
        toolbar.add(btnNhacNoHangLoat, gbc);

        JButton btnCauHinh = nutMauSac("Cấu hình khung thu", UITheme.SIDEBAR_PURPLE);
        btnCauHinh.addActionListener(e -> new CauHinhNhacNoDialog(
                (Window) SwingUtilities.getWindowAncestor(this), null, this::taiDuLieu).setVisible(true));
        gbc.gridx = col++;
        toolbar.add(btnCauHinh, gbc);

        JButton btnQuetTuDong = nutMauSac("Quét nhắc nợ tự động ngay", UITheme.PRIMARY);
        btnQuetTuDong.addActionListener(e -> quetNhacNoTuDongNgay());
        gbc.gridx = col++;
        toolbar.add(btnQuetTuDong, gbc);

        JButton btnXuatExcel = nutMauSac("Xuất Excel", UITheme.SIDEBAR_BLUE);
        btnXuatExcel.addActionListener(e -> xuatExcel());
        gbc.gridx = col++;
        toolbar.add(btnXuatExcel, gbc);

        JButton btnXuatPDF = nutMauSac("Xuất PDF", UITheme.PRIMARY_DARK);
        btnXuatPDF.addActionListener(e -> xuatPDF());
        gbc.gridx = col++;
        gbc.insets = new Insets(0, 0, 0, 0);
        toolbar.add(btnXuatPDF, gbc);

        return toolbar;
    }

    private JPanel buildTableCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UITheme.sectionLabel("Danh sách sinh viên còn nợ"), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{
                "Mã HD", "Mã SV", "Họ tên", "Học kỳ", "Còn nợ", "Trạng thái"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setToolTipText("Nhấp đúp (double-click) 1 dòng để xem chi tiết công nợ");
        table.putClientProperty("phimTat", "doubleclick=chi tiết, alt+click=xem sinh viên");

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        DefaultTableCellRenderer stripedRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF7, 0xF9, 0xFC));
                    c.setForeground(UITheme.TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(stripedRenderer);
        }
        table.getColumnModel().getColumn(5).setCellRenderer(new TrangThaiCellRenderer());

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) xemChiTietDongDangChon();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        card.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        lblSoLuong = new JLabel("Hiển thị 0 / 0 hóa đơn");
        lblSoLuong.setFont(UITheme.FONT_BASE);
        lblSoLuong.setForeground(UITheme.TEXT_MUTED);
        footer.add(lblSoLuong, BorderLayout.WEST);

        JPanel phanTrang = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        phanTrang.setOpaque(false);
        btnTrangTruoc = UITheme.secondaryButton("< Trang trước");
        lblTrangHienTai = new JLabel("Trang 1 / 1");
        lblTrangHienTai.setFont(UITheme.FONT_BOLD);
        btnTrangSau = UITheme.secondaryButton("Trang sau >");
        btnTrangTruoc.addActionListener(e -> { if (trangHienTai > 0) { trangHienTai--; hienThiTrangHienTai(); } });
        btnTrangSau.addActionListener(e -> { trangHienTai++; hienThiTrangHienTai(); });
        phanTrang.add(btnTrangTruoc);
        phanTrang.add(lblTrangHienTai);
        phanTrang.add(btnTrangSau);
        footer.add(phanTrang, BorderLayout.EAST);

        card.add(footer, BorderLayout.SOUTH);
        return card;
    }

    private class TrangThaiCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
            String text = value == null ? "" : value.toString();
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setFont(UITheme.FONT_BOLD);
            label.setOpaque(true);
            label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

            Color bg, fg;
            if (text.equals(TrangThaiHoaDon.QUA_HAN.getNhan())) {
                bg = new Color(0xFC, 0xE4, 0xE4); fg = UITheme.DANGER;
            } else if (text.equals(TrangThaiHoaDon.DONG_MOT_PHAN.getNhan())) {
                bg = new Color(0xFD, 0xF3, 0xDA); fg = UITheme.WARNING;
            } else {
                bg = new Color(0xEC, 0xEE, 0xF2); fg = UITheme.TEXT_MUTED;
            }
            if (!isSelected) {
                label.setBackground(bg);
                label.setForeground(fg);
            }
            return label;
        }
    }

    private void apDungBoLoc() {
        List<RowFilter<Object, Object>> danhSachLoc = new ArrayList<>();

        String tuKhoa = txtTimKiem.getText().trim();
        if (!tuKhoa.isEmpty()) {
            danhSachLoc.add(RowFilter.regexFilter("(?i)" + Pattern.quote(tuKhoa), 1, 2));
        }
        String trangThai = (String) cboTrangThai.getSelectedItem();
        if (trangThai != null && !trangThai.equals("Tất cả trạng thái")) {
            danhSachLoc.add(RowFilter.regexFilter("^" + Pattern.quote(trangThai) + "$", 5));
        }

        sorter.setRowFilter(danhSachLoc.isEmpty() ? null : RowFilter.andFilter(danhSachLoc));
        trangHienTai = 0;
        hienThiTrangHienTai();
    }

    private void hienThiTrangHienTai() {
        int tongSoDongDaLoc = table.getRowCount();
        int tongSoTrang = Math.max(1, (int) Math.ceil(tongSoDongDaLoc / (double) SO_DONG_MOI_TRANG));
        trangHienTai = Math.max(0, Math.min(trangHienTai, tongSoTrang - 1));

        int batDau = trangHienTai * SO_DONG_MOI_TRANG;
        int ketThuc = Math.min(batDau + SO_DONG_MOI_TRANG, tongSoDongDaLoc);

        RowFilter<Object, Object> locTrang = new RowFilter<>() {
            @Override
            public boolean include(Entry<?, ?> entry) {
                return true;
            }
        };

        lblTrangHienTai.setText("Trang " + (trangHienTai + 1) + " / " + tongSoTrang);
        btnTrangTruoc.setEnabled(trangHienTai > 0);
        btnTrangSau.setEnabled(trangHienTai < tongSoTrang - 1);
        capNhatSoLuongHienThi();
    }

    private void capNhatSoLuongHienThi() {
        lblSoLuong.setText("Hiển thị " + table.getRowCount() + " / " + tableModel.getRowCount() + " hóa đơn");
    }

    public void timKiem(String tuKhoa) {
        txtTimKiem.setText(tuKhoa);
    }

    /** MỚI: mở camera quét mã QR trên thẻ sinh viên, tự động điền vào ô tìm kiếm
     *  ngay khi quét được - kế toán khỏi phải gõ tay mã SV. */
    private void quetQRTimKiem() {
        Window chaMe = SwingUtilities.getWindowAncestor(this);
        QuetQRDialog dlg = new QuetQRDialog(chaMe, "Quét thẻ sinh viên - tra cứu công nợ");
        dlg.setVisible(true);
        String maSV = dlg.layKetQua();
        if (maSV != null && !maSV.isBlank()) {
            timKiem(maSV.trim());
        }
    }

    private void taiDuLieu() {
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                List<HoaDonHocPhi> danhSachNo = congNoService.layDanhSachConNo();
                BigDecimal tongNo = congNoService.tongConNoToanTruong();
                BigDecimal tongThu = congNoService.tongDaThuTatCa();
                return new Object[]{danhSachNo, tongNo, tongThu};
            }

            @Override
            @SuppressWarnings("unchecked")
            protected void done() {
                try {
                    Object[] result = get();
                    List<HoaDonHocPhi> danhSachNo = (List<HoaDonHocPhi>) result[0];
                    BigDecimal tongNo = (BigDecimal) result[1];
                    BigDecimal tongThu = (BigDecimal) result[2];

                    danhSachHienTai = danhSachNo;
                    tableModel.setRowCount(0);
                    for (HoaDonHocPhi hd : danhSachNo) {
                        tableModel.addRow(new Object[]{
                                hd.getMaHoaDon(), hd.getMaSV(), hd.getTenSV(), hd.getTenHocKy(),
                                MoneyUtils.format(hd.tinhConNo()), hd.tinhTrangThai().getNhan()
                        });
                    }
                    lblSoHoaDonNo.setText(String.valueOf(danhSachNo.size()));
                    lblTongNo.setText(MoneyUtils.format(tongNo));
                    lblTongThu.setText(MoneyUtils.format(tongThu));

                    apDungBoLoc();
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(CongNoPanel.this, "Không thể tải dữ liệu công nợ.");
                }
            }
        };
        worker.execute();
    }

    private HoaDonHocPhi layHoaDonDangChon() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        int maHoaDon = (int) tableModel.getValueAt(modelRow, 0);
        return danhSachHienTai.stream()
                .filter(hd -> hd.getMaHoaDon() == maHoaDon)
                .findFirst().orElse(null);
    }

    private void xemChiTietDongDangChon() {
        HoaDonHocPhi hd = layHoaDonDangChon();
        if (hd == null) return;

        JPanel noiDung = new JPanel();
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        dongChiTiet(noiDung, "Mã hóa đơn", "#" + hd.getMaHoaDon());
        dongChiTiet(noiDung, "Sinh viên", hd.getTenSV() + " (" + hd.getMaSV() + ")");
        dongChiTiet(noiDung, "Học kỳ", hd.getTenHocKy());
        noiDung.add(Box.createRigidArea(new Dimension(0, 8)));
        dongChiTiet(noiDung, "Tổng học phí", MoneyUtils.format(hd.getSoTien()));
        dongChiTiet(noiDung, "Đã nộp", MoneyUtils.format(hd.getDaNop()));
        dongChiTiet(noiDung, "Còn nợ", MoneyUtils.format(hd.tinhConNo()));
        dongChiTiet(noiDung, "Trạng thái", hd.tinhTrangThai().getNhan());

        JButton btnXemSV = UITheme.primaryButton("Xem hồ sơ sinh viên");
        btnXemSV.setAlignmentX(Component.LEFT_ALIGNMENT);
        noiDung.add(Box.createRigidArea(new Dimension(0, 10)));
        noiDung.add(btnXemSV);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết công nợ - Hóa đơn #" + hd.getMaHoaDon());
        dialog.setModal(true);
        dialog.getContentPane().add(noiDung);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        btnXemSV.addActionListener(e -> {
            dialog.dispose();
            if (dieuHuongTimKiem != null) dieuHuongTimKiem.accept("sinhvien", hd.getMaSV());
        });

        dialog.setVisible(true);
    }

    private void dongChiTiet(JPanel container, String nhan, String giaTri) {
        JPanel dong = new JPanel(new BorderLayout(20, 0));
        dong.setAlignmentX(Component.LEFT_ALIGNMENT);
        dong.setMaximumSize(new Dimension(360, 26));
        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(UITheme.FONT_BASE);
        lblNhan.setForeground(UITheme.TEXT_MUTED);
        JLabel lblGiaTri = new JLabel(giaTri);
        lblGiaTri.setFont(UITheme.FONT_BOLD);
        lblGiaTri.setForeground(UITheme.TEXT_PRIMARY);
        dong.add(lblNhan, BorderLayout.WEST);
        dong.add(lblGiaTri, BorderLayout.EAST);
        container.add(dong);
    }

    private void guiNhacNoDaChon() {
        HoaDonHocPhi hd = layHoaDonDangChon();
        if (hd == null) {
            UIUtils.thongBaoLoi(this, "Vui lòng chọn 1 dòng trong bảng để gửi nhắc nợ.");
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                SinhVien sv = sinhVienDAO.timTheoMa(hd.getMaSV());
                if (sv == null || sv.getEmail() == null || sv.getEmail().isBlank()) {
                    throw new IllegalStateException("Sinh viên chưa có email trong hệ thống.");
                }
                return EmailUtils.guiNhacHocPhi(sv.getEmail(), hd.getTenSV(), hd.getTenHocKy(),
                        MoneyUtils.format(hd.tinhConNo()));
            }

            @Override
            protected void done() {
                try {
                    boolean thanhCong = get();
                    if (thanhCong) {
                        UIUtils.thongBao(CongNoPanel.this, "Đã gửi email nhắc nợ cho sinh viên " + hd.getTenSV());
                    } else {
                        UIUtils.thongBaoLoi(CongNoPanel.this,
                                "Gửi email thất bại. Kiểm tra lại cấu hình mail trong application.properties.");
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(CongNoPanel.this, cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void guiNhacNoHangLoat() {
        List<HoaDonHocPhi> quaHan = danhSachHienTai.stream()
                .filter(hd -> hd.tinhTrangThai() == TrangThaiHoaDon.QUA_HAN)
                .toList();

        if (quaHan.isEmpty()) {
            UIUtils.thongBao(this, "Hiện không có hóa đơn nào quá hạn.");
            return;
        }

        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Gửi email nhắc nợ cho " + quaHan.size() + " hóa đơn đang quá hạn?",
                "Xác nhận gửi hàng loạt", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) return;

        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() {
                int thanhCong = 0, thatBai = 0;
                for (HoaDonHocPhi hd : quaHan) {
                    try {
                        SinhVien sv = sinhVienDAO.timTheoMa(hd.getMaSV());
                        if (sv == null || sv.getEmail() == null || sv.getEmail().isBlank()) {
                            thatBai++;
                            continue;
                        }
                        boolean ok = EmailUtils.guiNhacHocPhi(sv.getEmail(), hd.getTenSV(), hd.getTenHocKy(),
                                MoneyUtils.format(hd.tinhConNo()));
                        if (ok) thanhCong++; else thatBai++;
                    } catch (Exception ex) {
                        thatBai++;
                    }
                }
                return new int[]{thanhCong, thatBai};
            }

            @Override
            protected void done() {
                try {
                    int[] ketQua = get();
                    UIUtils.thongBao(CongNoPanel.this, "Đã gửi thành công " + ketQua[0]
                            + " email. Thất bại/không có email: " + ketQua[1] + ".");
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(CongNoPanel.this, "Có lỗi khi gửi hàng loạt.");
                }
            }
        };
        worker.execute();
    }

    private void quetNhacNoTuDongNgay() {
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Hệ thống sẽ quét TOÀN BỘ hóa đơn quá hạn theo Khung thời gian thu học phí\n"
                        + "đang được bật, và tự động gửi:\n"
                        + "- Email cho sinh viên (nếu chưa từng gửi)\n"
                        + "- SMS cho phụ huynh (nếu đã gửi email ≥ 1 phút mà vẫn chưa đóng)\n\n"
                        + "Tiếp tục?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (xacNhan != JOptionPane.YES_OPTION) return;

        SwingWorker<NhacNoTuDongService.KetQuaNhacNo, Void> worker = new SwingWorker<>() {
            @Override
            protected NhacNoTuDongService.KetQuaNhacNo doInBackground() throws Exception {
                return new NhacNoTuDongService().quetVaGuiNhacNo();
            }

            @Override
            protected void done() {
                try {
                    NhacNoTuDongService.KetQuaNhacNo kq = get();
                    if (!kq.coCauHinhDangApDung) {
                        UIUtils.thongBaoLoi(CongNoPanel.this,
                                "Chưa có Khung thời gian thu học phí nào đang được bật.\n"
                                        + "Bấm 'Cấu hình khung thu' để đặt trước.");
                        return;
                    }
                    UIUtils.thongBao(CongNoPanel.this,
                            "Đã quét " + kq.tongQuaHan + " hóa đơn còn nợ:\n"
                                    + "- Đã gửi email SV: " + kq.daGuiEmailSV + "\n"
                                    + "- Đã gửi SMS phụ huynh: " + kq.daGuiSmsPH + "\n"
                                    + "- Chưa đến lượt gửi: " + kq.boQuaChuaDenGio + "\n"
                                    + "- Bỏ qua (thiếu thông tin liên hệ): " + kq.boQuaThieuLienHe);
                    taiDuLieu();
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(CongNoPanel.this, "Lỗi: " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private String[] tieuDeCotXuat() {
        return new String[]{"Mã HD", "Mã SV", "Họ tên", "Học kỳ", "Còn nợ", "Trạng thái"};
    }

    private List<String[]> layDuLieuDangHienThi() {
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < table.getRowCount(); i++) {
            String[] row = new String[tableModel.getColumnCount()];
            for (int c = 0; c < row.length; c++) {
                Object gt = table.getValueAt(i, c);
                row[c] = gt == null ? "" : gt.toString();
            }
            rows.add(row);
        }
        return rows;
    }

    private void xuatExcel() {
        if (table.getRowCount() == 0) {
            UIUtils.thongBaoLoi(this, "Không có dữ liệu để xuất.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("cong_no_hoc_phi.xlsx"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".xlsx")) path += ".xlsx";
        String duongDan = path;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                ExcelExporter.export(duongDan, "CongNo", tieuDeCotXuat(), layDuLieuDangHienThi());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UIUtils.thongBao(CongNoPanel.this, "Đã xuất file Excel:\n" + duongDan);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(CongNoPanel.this, "Xuất Excel thất bại: " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void xuatPDF() {
        if (table.getRowCount() == 0) {
            UIUtils.thongBaoLoi(this, "Không có dữ liệu để xuất.");
            return;
        }
        JFileChooser chooser = new JFileChooser();
        chooser.setSelectedFile(new File("cong_no_hoc_phi.pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".pdf")) path += ".pdf";
        String duongDan = path;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                PDFExporter.exportBangDuLieu(duongDan, "DANH SÁCH CÔNG NỢ HỌC PHÍ", tieuDeCotXuat(), layDuLieuDangHienThi());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UIUtils.thongBao(CongNoPanel.this, "Đã xuất file PDF:\n" + duongDan);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(CongNoPanel.this, "Xuất PDF thất bại: " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }
}