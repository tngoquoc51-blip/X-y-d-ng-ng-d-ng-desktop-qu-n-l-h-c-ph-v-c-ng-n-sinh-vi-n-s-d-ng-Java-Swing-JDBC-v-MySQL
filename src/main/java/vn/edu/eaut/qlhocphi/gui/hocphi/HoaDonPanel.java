package vn.edu.eaut.qlhocphi.gui.hocphi;

import vn.edu.eaut.qlhocphi.bus.HocPhiService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.dal.SinhVienDAO;
import vn.edu.eaut.qlhocphi.gui.common.AutoRefreshTimer;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.SinhVien;
import vn.edu.eaut.qlhocphi.model.TrangThaiHoaDon;
import vn.edu.eaut.qlhocphi.gui.thanhtoan.VietQRDialog;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Màn hình sinh hóa đơn học phí, tìm kiếm/lọc, xem thống kê, xuất Excel/PDF,
 * xóa hóa đơn, xem chi tiết (double-click dòng) và gửi email nhắc nợ.
 * Bản sửa lỗi: Toolbar trước đây dùng FlowLayout lồng trong BorderLayout nên
 * khi không đủ chỗ sẽ tự wrap xuống dòng và đè lên bảng dữ liệu bên dưới. Bản
 * này chuyển toàn bộ toolbar sang GridBagLayout - không bao giờ wrap ngoài ý
 * muốn, mỗi thành phần luôn nằm cố định trên 1 hàng duy nhất.
 */
public class HoaDonPanel extends JPanel {
    private final java.util.function.BiConsumer<String, String> dieuHuongTimKiem;
    private final HocPhiService hocPhiService = new HocPhiService();
    private final SinhVienDAO sinhVienDAO = new SinhVienDAO();
    private final vn.edu.eaut.qlhocphi.model.TaiKhoan taiKhoan;
    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField txtTimKiem;
    private JComboBox<String> cboTrangThai;
    private JLabel lblSoLuong;

    private JLabel lblTongHoaDon, lblTongDaThu, lblTongConNo, lblQuaHan;

    /** Danh sách hóa đơn đầy đủ đang tải gần nhất - dùng cho xem chi tiết/gửi nhắc nợ theo dòng. */
    private List<HoaDonHocPhi> danhSachHienTai = new ArrayList<>();

    public HoaDonPanel(java.util.function.BiConsumer<String, String> dieuHuongTimKiem, vn.edu.eaut.qlhocphi.model.TaiKhoan taiKhoan) {
        this.dieuHuongTimKiem = dieuHuongTimKiem;
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(buildHeader());
        north.add(Box.createRigidArea(new Dimension(0, 16)));
        north.add(buildStatsRow());
        north.add(Box.createRigidArea(new Dimension(0, 16)));
        north.add(buildToolbar());

        add(north, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        taiDuLieu();
        AutoRefreshTimer.gan(this, 20, this::taiDuLieu);
    }

    // ================== HEADER (banner + logo) ==================

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
        JLabel tieuDe = new JLabel("Hóa đơn học phí");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Sinh hóa đơn, tìm kiếm, lọc trạng thái và xuất báo cáo");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);

        JButton btnSinh = new JButton("+ Sinh hóa đơn mới");
        btnSinh.setFont(UITheme.FONT_BOLD);
        btnSinh.setBackground(Color.WHITE);
        btnSinh.setForeground(UITheme.PRIMARY_DARK);
        btnSinh.setFocusPainted(false);
        btnSinh.setBorderPainted(false);
        btnSinh.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnSinh.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSinh.addActionListener(e -> moFormSinhHoaDon());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(btnSinh);
        banner.add(actions, BorderLayout.EAST);

        return banner;
    }

    /** Logo tròn vẽ bằng Graphics2D, không cần file ảnh ngoài. */
    private JComponent logoBadge() {
        JComponent badge = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 55));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
                FontMetrics fm = g2.getFontMetrics();
                String bieuTuong = "Đ";
                int x = (getWidth() - fm.stringWidth(bieuTuong)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(bieuTuong, x, y);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(52, 52));
        badge.setOpaque(false);
        return badge;
    }

    // ================== THỐNG KÊ NHANH ==================

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);

        lblTongHoaDon = new JLabel("0");
        lblTongDaThu = new JLabel("0 đ");
        lblTongConNo = new JLabel("0 đ");
        lblQuaHan = new JLabel("0");

        row.add(thongKeCard("Tổng số hóa đơn", lblTongHoaDon, UITheme.PRIMARY));
        row.add(thongKeCard("Tổng đã thu", lblTongDaThu, UITheme.SUCCESS));
        row.add(thongKeCard("Tổng còn nợ", lblTongConNo, UITheme.WARNING));
        row.add(thongKeCard("Hóa đơn quá hạn", lblQuaHan, UITheme.DANGER));
        return row;
    }

    /** Nut chuc nang to mau dam rieng biet - moi nut 1 mau, khong con dung chung
     *  kieu "secondaryButton" (vien trang nhat, de bi mo/nhat nhoa) nhu truoc.
     *  Mau nen truyen vao la field UITheme (SUCCESS, WARNING...) nen tu dong doi
     *  dung theo Sang/Toi, khong bi "cung mau" nhu hardcode raw Color. */
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

    // ================== TOOLBAR: GRIDBAGLAYOUT - KHÔNG BAO GIỜ WRAP ==================

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

        gbc.gridx = col++;
        toolbar.add(UIUtils.formLabel("Trạng thái:"), gbc);

        cboTrangThai = new JComboBox<>(new String[]{
                "Tất cả trạng thái", "Chưa đóng", "Đóng một phần", "Đã đóng đủ", "Quá hạn"
        });
        cboTrangThai.setFont(UITheme.FONT_BASE);
        cboTrangThai.addActionListener(e -> apDungBoLoc());
        gbc.gridx = col++;
        toolbar.add(cboTrangThai, gbc);

        // Ô trống giãn nở - đẩy toàn bộ nút hành động về sát lề phải, không bao giờ wrap
        gbc.gridx = col++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        toolbar.add(Box.createHorizontalGlue(), gbc);
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        JButton btnLamMoi = nutMauSac("Làm mới", UITheme.SIDEBAR_BLUE);
        btnLamMoi.addActionListener(e -> taiDuLieu());
        gbc.gridx = col++;
        toolbar.add(btnLamMoi, gbc);

        JButton btnNhacNo = nutMauSac("Gửi nhắc nợ", UITheme.WARNING);
        btnNhacNo.addActionListener(e -> guiNhacNoDaChon());
        gbc.gridx = col++;
        toolbar.add(btnNhacNo, gbc);

        JButton btnQR = nutMauSac("Mã QR chuyển khoản", UITheme.SIDEBAR_PURPLE);
        btnQR.addActionListener(e -> moQRDaChon());
        gbc.gridx = col++;
        toolbar.add(btnQR, gbc);

        JButton btnMienGiam = nutMauSac("Miễn giảm", UITheme.SUCCESS);
        btnMienGiam.addActionListener(e -> moMienGiam());
        gbc.gridx = col++;
        toolbar.add(btnMienGiam, gbc);

        JButton btnXuatExcel = nutMauSac("Xuất Excel", UITheme.ACCENT_TEAL);
        btnXuatExcel.addActionListener(e -> xuatExcel());
        gbc.gridx = col++;
        toolbar.add(btnXuatExcel, gbc);

        JButton btnXuatPDF = nutMauSac("Xuất PDF", UITheme.PRIMARY_DARK);
        btnXuatPDF.addActionListener(e -> xuatPDF());
        gbc.gridx = col++;
        toolbar.add(btnXuatPDF, gbc);

        if (taiKhoan.getVaiTro() != vn.edu.eaut.qlhocphi.model.VaiTro.KETOAN) {
            JButton btnXoa = UITheme.dangerButton("Xóa hóa đơn");
            btnXoa.addActionListener(e -> xoaHoaDonDaChon());
            gbc.gridx = col++;
            gbc.insets = new Insets(0, 0, 0, 0);
            toolbar.add(btnXoa, gbc);
        }

        return toolbar;
    }

    // ================== BẢNG DỮ LIỆU ==================

    private JPanel buildTableCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        tableModel = new DefaultTableModel(new Object[]{
                "Mã HĐ", "Mã SV", "Họ tên", "Học kỳ", "Số TC", "Số tiền", "Đã nộp", "Còn nợ", "Trạng thái"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setToolTipText("Nhấp đôi (double-click) 1 dòng để xem chi tiết hóa đơn");

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);

        // Renderer sọc xọc kẻ cho toàn bảng
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
        // Renderer riêng cho cột Trạng thái: hiển thị dạng "thẻ màu"
        table.getColumnModel().getColumn(8).setCellRenderer(new TrangThaiCellRenderer());

        // Double-click 1 dòng -> xem chi tiết hóa đơn (tính năng "quản lý" mới)
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) xemChiTietDongDangChon();
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        lblSoLuong = new JLabel("Hiển thị 0 / 0 hóa đơn");
        lblSoLuong.setFont(UITheme.FONT_BASE);
        lblSoLuong.setForeground(UITheme.TEXT_MUTED);
        card.add(lblSoLuong, BorderLayout.SOUTH);

        return card;
    }

    /** Renderer vẽ trạng thái dạng "thẻ màu" (pill) thay vì chữ thường. */
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
            if (text.equals(TrangThaiHoaDon.DA_DONG_DU.getNhan())) {
                bg = new Color(0xE3, 0xF7, 0xEC); fg = UITheme.SUCCESS;
            } else if (text.equals(TrangThaiHoaDon.DONG_MOT_PHAN.getNhan())) {
                bg = new Color(0xFD, 0xF3, 0xDA); fg = UITheme.WARNING;
            } else if (text.equals(TrangThaiHoaDon.QUA_HAN.getNhan())) {
                bg = new Color(0xFC, 0xE4, 0xE4); fg = UITheme.DANGER;
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

    // ================== TÌM KIẾM / LỌC ==================

    private void apDungBoLoc() {
        List<RowFilter<Object, Object>> danhSachLoc = new ArrayList<>();

        String tuKhoa = txtTimKiem.getText().trim();
        if (!tuKhoa.isEmpty()) {
            danhSachLoc.add(RowFilter.regexFilter("(?i)" + Pattern.quote(tuKhoa), 1, 2));
        }
        String trangThai = (String) cboTrangThai.getSelectedItem();
        if (trangThai != null && !trangThai.equals("Tất cả trạng thái")) {
            danhSachLoc.add(RowFilter.regexFilter("^" + Pattern.quote(trangThai) + "$", 8));
        }

        sorter.setRowFilter(danhSachLoc.isEmpty() ? null : RowFilter.andFilter(danhSachLoc));
        capNhatSoLuongHienThi();
    }

    private void capNhatSoLuongHienThi() {
        lblSoLuong.setText("Hiển thị " + table.getRowCount() + " / " + tableModel.getRowCount() + " hóa đơn");
    }

    // ================== TẢI DỮ LIỆU + THỐNG KÊ ==================

    private void taiDuLieu() {
        SwingWorker<List<HoaDonHocPhi>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<HoaDonHocPhi> doInBackground() throws Exception {
                return hocPhiService.layTatCaHoaDon();
            }

            @Override
            protected void done() {
                try {
                    List<HoaDonHocPhi> list = get();
                    danhSachHienTai = list;
                    tableModel.setRowCount(0);

                    BigDecimal tongDaNop = BigDecimal.ZERO;
                    BigDecimal tongConNo = BigDecimal.ZERO;
                    int soQuaHan = 0;

                    for (HoaDonHocPhi hd : list) {
                        String soTienHienThi = hd.getTyLeMienGiam() != null && hd.getTyLeMienGiam().compareTo(BigDecimal.ZERO) > 0
                                ? MoneyUtils.format(hd.tinhSoTienPhaiDong()) + " (giảm " + hd.getTyLeMienGiam().intValue() + "%)"
                                : MoneyUtils.format(hd.getSoTien());
                        tableModel.addRow(new Object[]{
                                hd.getMaHoaDon(), hd.getMaSV(), hd.getTenSV(), hd.getTenHocKy(),
                                hd.getSoTinChi(), soTienHienThi,
                                MoneyUtils.format(hd.getDaNop()), MoneyUtils.format(hd.tinhConNo()),
                                hd.tinhTrangThai().getNhan()
                        });
                        tongDaNop = tongDaNop.add(hd.getDaNop() == null ? BigDecimal.ZERO : hd.getDaNop());
                        tongConNo = tongConNo.add(hd.tinhConNo());
                        if (hd.tinhTrangThai() == TrangThaiHoaDon.QUA_HAN) soQuaHan++;
                    }

                    lblTongHoaDon.setText(String.valueOf(list.size()));
                    lblTongDaThu.setText(MoneyUtils.format(tongDaNop));
                    lblTongConNo.setText(MoneyUtils.format(tongConNo));
                    lblQuaHan.setText(String.valueOf(soQuaHan));

                    apDungBoLoc();
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(HoaDonPanel.this, "Không thể tải danh sách hóa đơn.");
                }
            }
        };
        worker.execute();
    }

    /** Tìm hóa đơn đầy đủ (có đầy đủ field) tương ứng với dòng đang chọn trên bảng. */
    private HoaDonHocPhi layHoaDonDangChon() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) return null;
        int modelRow = table.convertRowIndexToModel(viewRow);
        int maHoaDon = (int) tableModel.getValueAt(modelRow, 0);
        return danhSachHienTai.stream()
                .filter(hd -> hd.getMaHoaDon() == maHoaDon)
                .findFirst().orElse(null);
    }

    // ================== XEM CHI TIẾT (double-click) ==================

    private void xemChiTietDongDangChon() {
        HoaDonHocPhi hd = layHoaDonDangChon();
        if (hd == null) return;

        JPanel noiDung = new JPanel();
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        dongChiTiet(noiDung, "Mã hóa đơn", "#" + hd.getMaHoaDon());
        dongChiTiet(noiDung, "Sinh viên", hd.getTenSV() + " (" + hd.getMaSV() + ")");
        dongChiTiet(noiDung, "Học kỳ", hd.getTenHocKy());
        dongChiTiet(noiDung, "Số tín chỉ đăng ký", String.valueOf(hd.getSoTinChi()));
        noiDung.add(Box.createRigidArea(new Dimension(0, 8)));
        dongChiTiet(noiDung, "Học phí gốc", MoneyUtils.format(hd.getSoTien()));
        if (hd.getTyLeMienGiam() != null && hd.getTyLeMienGiam().compareTo(BigDecimal.ZERO) > 0) {
            dongChiTiet(noiDung, "Miễn giảm", hd.getTyLeMienGiam().intValue() + "% (-" + MoneyUtils.format(hd.tinhSoTienMienGiam()) + ")");
            dongChiTiet(noiDung, "Lý do", hd.getLyDoMienGiam() == null ? "-" : hd.getLyDoMienGiam());
            dongChiTiet(noiDung, "Phải đóng sau giảm", MoneyUtils.format(hd.tinhSoTienPhaiDong()));
        }
        dongChiTiet(noiDung, "Đã nộp", MoneyUtils.format(hd.getDaNop()));
        dongChiTiet(noiDung, "Còn nợ", MoneyUtils.format(hd.tinhConNo()));
        dongChiTiet(noiDung, "Trạng thái", hd.tinhTrangThai().getNhan());

        JButton btnXemCongNo = UITheme.primaryButton("Xem công nợ");
        btnXemCongNo.setAlignmentX(Component.LEFT_ALIGNMENT);
        noiDung.add(Box.createRigidArea(new Dimension(0, 10)));
        noiDung.add(btnXemCongNo);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiết hóa đơn #" + hd.getMaHoaDon());
        dialog.setModal(true);
        dialog.getContentPane().add(noiDung);
        dialog.pack();
        dialog.setLocationRelativeTo(this);

        btnXemCongNo.addActionListener(e -> {
            dialog.dispose();
            if (dieuHuongTimKiem != null) dieuHuongTimKiem.accept("congno", hd.getMaSV());
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

    // ================== GỬI NHẮC NỢ QUA EMAIL ==================

    private void guiNhacNoDaChon() {
        HoaDonHocPhi hd = layHoaDonDangChon();
        if (hd == null) {
            UIUtils.thongBaoLoi(this, "Vui lòng chọn 1 hóa đơn trong bảng để gửi nhắc nợ.");
            return;
        }
        if (hd.tinhConNo().compareTo(BigDecimal.ZERO) <= 0) {
            UIUtils.thongBaoLoi(this, "Hóa đơn này đã đóng đủ, không cần gửi nhắc nợ.");
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
                        UIUtils.thongBao(HoaDonPanel.this, "Đã gửi email nhắc nợ cho sinh viên " + hd.getTenSV());
                    } else {
                        UIUtils.thongBaoLoi(HoaDonPanel.this,
                                "Gửi email thất bại. Kiểm tra lại cấu hình mail.host/mail.username/mail.password trong application.properties.");
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(HoaDonPanel.this, cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ================== MÃ QR VIETQR CHUYỂN KHOẢN (MỚI) ==================

    private void moQRDaChon() {
        HoaDonHocPhi hd = layHoaDonDangChon();
        if (hd == null) {
            UIUtils.thongBaoLoi(this, "Vui lòng chọn 1 hóa đơn trong bảng để tạo mã QR.");
            return;
        }
        if (hd.tinhConNo().compareTo(BigDecimal.ZERO) <= 0) {
            UIUtils.thongBaoLoi(this, "Hóa đơn này đã đóng đủ, không cần chuyển khoản thêm.");
            return;
        }
        // choPhepXacNhanThu = true: kế toán được phép bấm xác nhận đã nhận tiền thủ công.
        new VietQRDialog(SwingUtilities.getWindowAncestor(this), hd, true, this::taiDuLieu)
                .setVisible(true);
    }

    // ================== MIỄN GIẢM HỌC PHÍ (MỚI) ==================

    private void moMienGiam() {
        HoaDonHocPhi hd = layHoaDonDangChon();
        if (hd == null) {
            UIUtils.thongBaoLoi(this, "Vui lòng chọn 1 hóa đơn trong bảng để thiết lập miễn giảm.");
            return;
        }
        new MienGiamDialog((Frame) SwingUtilities.getWindowAncestor(this), hd, this::taiDuLieu).setVisible(true);
    }

    // ================== SINH HÓA ĐƠN ==================

    private void moFormSinhHoaDon() {
        SinhHoaDonDialog dialog = new SinhHoaDonDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                (maSV, maHocKy, soTinChi, hanThanhToan) -> sinhHoaDon(maSV, maHocKy, soTinChi, hanThanhToan));
        dialog.setVisible(true);
    }

    private void sinhHoaDon(String maSV, int maHocKy, int soTinChi, LocalDate hanThanhToan) {
        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return hocPhiService.sinhHoaDon(maSV, maHocKy, soTinChi, hanThanhToan);
            }

            @Override
            protected void done() {
                try {
                    get();
                    taiDuLieu();
                    UIUtils.thongBao(HoaDonPanel.this, "Đã sinh hóa đơn học phí cho sinh viên " + maSV);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(HoaDonPanel.this, cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ================== XÓA HÓA ĐƠN ==================

    private void xoaHoaDonDaChon() {
        if (taiKhoan.getVaiTro() == vn.edu.eaut.qlhocphi.model.VaiTro.KETOAN) {
            vn.edu.eaut.qlhocphi.gui.common.ErrorScreens.hienTuChoiTruyCap(this, "Xóa hóa đơn học phí");
            return;
        }
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            UIUtils.thongBaoLoi(this, "Vui lòng chọn 1 hóa đơn trong bảng để xóa.");
            return;
        }
        int modelRow = table.convertRowIndexToModel(viewRow);
        int maHoaDon = (int) tableModel.getValueAt(modelRow, 0);
        String tenSV = String.valueOf(tableModel.getValueAt(modelRow, 2));
        String daNop = String.valueOf(tableModel.getValueAt(modelRow, 6));

        String canhBao = daNop.startsWith("0") ? "" : "\nLưu ý: hóa đơn này đã có phiếu thu, xóa sẽ mất luôn lịch sử nộp tiền.";
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Xóa hóa đơn #" + maHoaDon + " của sinh viên " + tenSV + "?" + canhBao,
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                hocPhiService.xoaHoaDon(maHoaDon);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    taiDuLieu();
                    UIUtils.thongBao(HoaDonPanel.this, "Đã xóa hóa đơn #" + maHoaDon);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(HoaDonPanel.this, "Không thể xóa: " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ================== XUẤT EXCEL / PDF ==================

    private String[] tieuDeCotXuat() {
        return new String[]{"Mã HĐ", "Mã SV", "Họ tên", "Học kỳ", "Số TC", "Số tiền", "Đã nộp", "Còn nợ", "Trạng thái"};
    }

    /** Lấy dữ liệu đang HIỂN THỊ TRÊN BẢNG (đã qua tìm kiếm/lọc) để xuất file. */
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
        chooser.setSelectedFile(new File("hoa_don_hoc_phi.xlsx"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".xlsx")) path += ".xlsx";
        String duongDan = path;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                ExcelExporter.export(duongDan, "HoaDonHocPhi", tieuDeCotXuat(), layDuLieuDangHienThi());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UIUtils.thongBao(HoaDonPanel.this, "Đã xuất file Excel:\n" + duongDan);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(HoaDonPanel.this, "Xuất Excel thất bại: " + cause.getMessage());
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
        chooser.setSelectedFile(new File("hoa_don_hoc_phi.pdf"));
        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String path = chooser.getSelectedFile().getAbsolutePath();
        if (!path.toLowerCase().endsWith(".pdf")) path += ".pdf";
        String duongDan = path;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                PDFExporter.exportBangDuLieu(duongDan, "DANH SÁCH HÓA ĐƠN HỌC PHÍ", tieuDeCotXuat(), layDuLieuDangHienThi());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UIUtils.thongBao(HoaDonPanel.this, "Đã xuất file PDF:\n" + duongDan);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(HoaDonPanel.this, "Xuất PDF thất bại: " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }
}