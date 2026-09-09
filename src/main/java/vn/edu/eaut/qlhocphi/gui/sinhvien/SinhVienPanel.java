package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.bus.SinhVienService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.AutoRefreshTimer;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.SinhVien;

import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Màn Hình Quản Lý Sinh Viên - Bản Sửa Lỗi + Nâng Cấp "Desktop Quản Lý":
 * Banner Đồng Bộ Màu, Toolbar GridBagLayout (Không Bao Giờ Wrap/Chồng Đè),
 * Thêm Tính Năng Tự Động Làm Mới Dữ Liệu Định Kỳ (Swing Timer), Và THAY THẾ
 * 2 Combo Lớp/Khoa Riêng Lẻ Bằng 1 Panel Điều Hướng Dạng CÂY (KhoaLopNavPanel)
 * Ở Bên Trái Bảng: Bấm Khoa Sẽ Xổ Ra Các Lớp Thuộc Khoa Đó, Giống Kiểu Website
 * Quản Lý Sinh Viên Của Trường Đại Học - Cho Phép Quản Lý Nhanh Nhiều Khoa/Viện
 * Khác Nhau Trong Cùng 1 Màn Hình Mà Không Cần Gõ Tay Tên Khoa/Lớp.
 */
public class SinhVienPanel extends JPanel {
    private static final int CHU_KY_TU_DONG_GIAY = 30;

    private final SinhVienService sinhVienService = new SinhVienService();
    private final vn.edu.eaut.qlhocphi.model.TaiKhoan taiKhoan;

    private JTextField txtTimKiem;
    private KhoaLopNavPanel navPanel;
    // SỬA: Mặc Định Là Null (Chưa Chọn Khoa Nào) Thay Vì TAT_CA - Đảm Bảo
    // KHÔNG Tự Động Hiện Toàn Bộ Dữ Liệu Khi Vừa Mở Màn Hình, Đúng Yêu Cầu
    // Của Giáo Viên: Phải Bấm Chọn 1 Khoa Thì Dữ Liệu Mới Được Hiện Lên.
    private KhoaLopNavPanel.LuaChon luaChonHienTai = null;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblSoLuong;
    private JToggleButton btnTuDong;
    private Timer timerTuDong;

    /** Danh Sách Đầy Đủ (Không Lọc) - Dùng Để Nạp Dữ Liệu Cho Panel Điều Hướng Khoa/Lớp. */
    private List<SinhVien> danhSachGoc = new ArrayList<>();

    private JLabel lblTongSo, lblDangHoc, lblDaNghi;
    private JLabel lblDaChon;

    public SinhVienPanel(vn.edu.eaut.qlhocphi.model.TaiKhoan taiKhoan) {
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

        // Khu Vực Giữa: Trái Là Cây Điều Hướng Khoa/Lớp, Phải Là Bảng Dữ Liệu.
        JPanel giua = new JPanel(new BorderLayout(16, 0));
        giua.setOpaque(false);
        giua.add(buildNavCard(), BorderLayout.WEST);
        giua.add(buildTableCard(), BorderLayout.CENTER);
        add(giua, BorderLayout.CENTER);

        taiBoLoc();
        // SỬA: KHÔNG Gọi taiDuLieu(null) Ở Đây Nữa - Trước Đây Dòng Này Làm
        // Bảng Tự Động Hiện HẾT Dữ Liệu Ngay Khi Mở Màn Hình (Chính Là Thứ
        // Giáo Viên Chê "Lỗi Thời"). Thay Vào Đó Chỉ Hiện Trạng Thái Rỗng,
        // Chờ Người Dùng Tự Bấm Chọn 1 Khoa Bên Trái.
        hienThiTrangThaiChuaChon();
        // Bật Sẵn Chế Độ Tự Động Làm Mới Ngay Khi Mở Màn Hình - Người Dùng
        // Không Cần Bấm Nút "Tự Động Làm Mới" Nữa, Đúng Yêu Cầu "Tự Động Mỗi
        // Lúc Mỗi Thời Điểm". Nút Vẫn Còn Để Người Dùng Tự Tắt Nếu Muốn.
        btnTuDong.setSelected(true);
        chuyenDoiTuDongLamMoi();
    }

    /** Cho Phép Màn Hình Khác (VD: TraCuuFrame) Kích Hoạt Tìm Kiếm Từ Bên Ngoài. */
    public void timKiem(String keyword) {
        if (txtTimKiem != null) txtTimKiem.setText(keyword);
        taiDuLieu(keyword);
    }

    // ================== HEADER (Banner + Logo) ==================

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
        JLabel tieuDe = new JLabel("Quản Lý Sinh Viên");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Tìm Kiếm, Điều Hướng Theo Khoa/Lớp, Nhập Excel Và Quản Lý Hồ Sơ Sinh Viên");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);

        JButton btnThem = nutMau("+ Thêm Sinh Viên", UITheme.PRIMARY);
        btnThem.addActionListener(e -> moFormThem());

        JButton btnTuQR = nutMau("📷 Từ Mã QR", UITheme.ACCENT_TEAL);
        btnTuQR.addActionListener(e -> moTuMaQR());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(btnTuQR);
        actions.add(btnThem);
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
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
                FontMetrics fm = g2.getFontMetrics();
                String icon = "\uD83C\uDF93";
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

    // ================== THỐNG KÊ NHANH ==================

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);

        JPanel theTongSo = UITheme.statCard("Tổng Số", "0", UITheme.TINT_BLUE, UITheme.TEXT_BLUE);
        JPanel theDangHoc = UITheme.statCard("Đang Học", "0", UITheme.TINT_GREEN, UITheme.TEXT_GREEN);
        JPanel theDaNghi = UITheme.statCard("Đã Nghỉ Học", "0", UITheme.TINT_RED, UITheme.TEXT_RED);

        lblTongSo = timNhanGiaTri(theTongSo);
        lblDangHoc = timNhanGiaTri(theDangHoc);
        lblDaNghi = timNhanGiaTri(theDaNghi);

        row.add(theTongSo);
        row.add(theDangHoc);
        row.add(theDaNghi);
        return row;
    }

    private JLabel timNhanGiaTri(JPanel the) {
        for (Component c : the.getComponents()) {
            if (c instanceof JLabel && "giaTri".equals(c.getName())) return (JLabel) c;
        }
        return null;
    }

    // ================== TOOLBAR: GridBagLayout (Không Bao Giờ Wrap/Chồng Đè) ==================
    // SỬA: Bỏ 2 Combo Lớp/Khoa Riêng Lẻ, Việc Lọc Theo Khoa/Lớp Này Chuyển Hết
    // Sang Panel Điều Hướng Dạng Cây KhoaLopNavPanel Ở Bên Trái Bảng Dữ Liệu.

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new GridBagLayout());
        toolbar.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 8);
        int col = 0;

        txtTimKiem = UIUtils.textField(20);
        txtTimKiem.setToolTipText("Tìm Theo Mã SV, Họ Tên, Lớp");
        txtTimKiem.addActionListener(e -> taiDuLieu(txtTimKiem.getText()));
        gbc.gridx = col++;
        toolbar.add(txtTimKiem, gbc);

        JButton btnTim = nutMau("Tìm Kiếm", UITheme.SIDEBAR_PURPLE);
        btnTim.addActionListener(e -> taiDuLieu(txtTimKiem.getText()));
        gbc.gridx = col++;
        toolbar.add(btnTim, gbc);

        // Ô Trống Giãn Nở - Đẩy Nút Hành Động Về Sát Lề Phải, Không Bao Giờ Wrap Xuống Dòng
        gbc.gridx = col++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        toolbar.add(Box.createHorizontalGlue(), gbc);
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        btnTuDong = nutToggleMau("Tự Động Làm Mới (30s)", UITheme.SIDEBAR_ORANGE, UITheme.SUCCESS);
        btnTuDong.addActionListener(e -> chuyenDoiTuDongLamMoi());
        gbc.gridx = col++;
        toolbar.add(btnTuDong, gbc);

        JButton btnNhapExcel = nutMau("Nhập Excel", UITheme.SUCCESS);
        btnNhapExcel.addActionListener(e -> moNhapExcel());
        gbc.gridx = col++;
        gbc.insets = new Insets(0, 0, 0, 0);
        toolbar.add(btnNhapExcel, gbc);

        return toolbar;
    }

    /** Bật/Tắt Bộ Đếm Tự Động Tải Lại Danh Sách Mỗi 30 Giây - Tính Năng "Tự Động Hóa" Trong Phạm Vi Ứng Dụng. */
    private void chuyenDoiTuDongLamMoi() {
        if (btnTuDong.isSelected()) {
            timerTuDong = new Timer(CHU_KY_TU_DONG_GIAY * 1000, e -> {
                taiBoLoc();
                taiDuLieu(txtTimKiem.getText());
            });
            timerTuDong.start();
            btnTuDong.setText("Đang Tự Động (30s)...");
        } else {
            if (timerTuDong != null) timerTuDong.stop();
            btnTuDong.setText("Tự Động Làm Mới (30s)");
        }
    }

    // ================== PANEL ĐIỀU HƯỚNG KHOA/LỚP (Mới) ==================

    private JPanel buildNavCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());
        card.setPreferredSize(new Dimension(260, 10));

        navPanel = new KhoaLopNavPanel();
        navPanel.setKhiChon(luaChon -> {
            luaChonHienTai = luaChon;
            taiDuLieu(txtTimKiem.getText());
        });
        card.add(navPanel, BorderLayout.CENTER);
        return card;
    }

    // ================== BẢNG DỮ LIỆU ==================

    private JPanel buildTableCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        tableModel = new DefaultTableModel(
                new Object[]{"Mã SV", "Họ Tên", "Lớp", "Khoa", "Email", "SĐT", "Trạng Thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        table.getColumnModel().getColumn(6).setCellRenderer(trangThaiRenderer());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        footer.setOpaque(false);

        JPanel footerTrai = new JPanel();
        footerTrai.setOpaque(false);
        footerTrai.setLayout(new BoxLayout(footerTrai, BoxLayout.Y_AXIS));

        lblSoLuong = new JLabel("Hiển Thị 0 Sinh Viên");
        lblSoLuong.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSoLuong.setForeground(UITheme.TEXT_MUTED);

        lblDaChon = new JLabel("Chưa Chọn Sinh Viên Nào");
        lblDaChon.setFont(UITheme.FONT_BASE);
        lblDaChon.setForeground(UITheme.TEXT_MUTED);
        lblDaChon.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 0));

        footerTrai.add(lblSoLuong);
        footerTrai.add(lblDaChon);
        footer.add(footerTrai, BorderLayout.WEST);

        table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            lblDaChon.setText(row < 0 ? "Chưa Chọn Sinh Viên Nào"
                    : "Đã Chọn: " + tableModel.getValueAt(row, 0) + " - " + tableModel.getValueAt(row, 1));
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        toolbar.setOpaque(false);
        JButton btnSua = nutMau("Sửa", UITheme.WARNING);
        btnSua.addActionListener(e -> moFormSua());
        toolbar.add(btnSua);
        if (taiKhoan.getVaiTro() != vn.edu.eaut.qlhocphi.model.VaiTro.KETOAN) {
            JButton btnXoa = UITheme.dangerButton("Xóa");
            btnXoa.addActionListener(e -> xoaSinhVienDangChon());
            toolbar.add(btnXoa);
        }
        footer.add(toolbar, BorderLayout.EAST);

        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    /** Tô Màu Nhãn Cho Cột Trạng Thái: Xanh = Đang Học, Đỏ Nhạt = Đã Nghỉ. */
    private DefaultTableCellRenderer trangThaiRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setOpaque(true);
                boolean dangHoc = "Đang Học".equals(value);
                if (!isSelected) {
                    label.setBackground(dangHoc ? new Color(0xE3, 0xF6, 0xEA) : new Color(0xF3, 0xE9, 0xEA));
                    label.setForeground(dangHoc ? UITheme.SUCCESS : UITheme.DANGER);
                }
                return label;
            }
        };
    }

    // ================== NẠP DỮ LIỆU CHO CÂY ĐIỀU HƯỚNG ==================

    /** Tải Toàn Bộ Sinh Viên (Không Lọc) Để Xây Lại Cây Khoa/Lớp Bên Trái. */
    private void taiBoLoc() {
        SwingWorker<List<SinhVien>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SinhVien> doInBackground() throws Exception {
                return sinhVienService.layTatCa();
            }

            @Override
            protected void done() {
                try {
                    danhSachGoc = get();
                    navPanel.capNhatDuLieu(danhSachGoc);
                } catch (Exception ignored) {
                    // Nếu Lỗi Tải Cây Khoa/Lớp, Ô Tìm Kiếm Chính Vẫn Dùng Bình Thường
                }
            }
        };
        worker.execute();
    }

    // ================== TẢI DỮ LIỆU + LỌC ==================

    /** Trạng Thái Ban Đầu Khi Chưa Chọn Khoa Nào: Bảng Trống, Không Gọi CSDL. */
    private void hienThiTrangThaiChuaChon() {
        tableModel.setRowCount(0);
        if (lblTongSo != null) {
            lblTongSo.setText("0");
            lblDangHoc.setText("0");
            lblDaNghi.setText("0");
        }
        lblSoLuong.setText("Vui Lòng Chọn 1 Khoa Bên Trái Để Xem Danh Sách Sinh Viên");
    }

    private void taiDuLieu(String keyword) {
        // Chưa Chọn Khoa/Lớp Nào Và Cũng Chưa Gõ Từ Khóa Tìm Kiếm -> Không Gọi
        // CSDL, Chỉ Hiện Trạng Thái Rỗng. Đây Là Điểm Mấu Chốt Xử Lý Yêu Cầu
        // "Không Được Tự Động Hiện Dữ Liệu" Của Giáo Viên.
        if (luaChonHienTai == null && (keyword == null || keyword.isBlank())) {
            hienThiTrangThaiChuaChon();
            return;
        }
        SwingWorker<List<SinhVien>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<SinhVien> doInBackground() throws Exception {
                return sinhVienService.timKiem(keyword);
            }

            @Override
            protected void done() {
                try {
                    List<SinhVien> list = apDungBoLoc(get());
                    tableModel.setRowCount(0);
                    int dangHoc = 0;
                    for (SinhVien sv : list) {
                        tableModel.addRow(new Object[]{
                                sv.getMaSV(), sv.getHoTen(), sv.getLop(), sv.getKhoa(),
                                sv.getEmail(), sv.getSoDienThoai(),
                                sv.isTrangThai() ? "Đang Học" : "Đã Nghỉ"
                        });
                        if (sv.isTrangThai()) dangHoc++;
                    }
                    if (lblTongSo != null) {
                        lblTongSo.setText(String.valueOf(list.size()));
                        lblDangHoc.setText(String.valueOf(dangHoc));
                        lblDaNghi.setText(String.valueOf(list.size() - dangHoc));
                    }
                    lblSoLuong.setText("Hiển Thị " + list.size() + " Sinh Viên");
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(SinhVienPanel.this, "Không Thể Tải Danh Sách Sinh Viên.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    /**
     * Lọc Theo Lựa Chọn Đang Chọn Trên Cây Điều Hướng (Tất Cả / 1 Khoa / 1 Lớp
     * Trong 1 Khoa Cụ Thể). Vẫn trim() Cả 2 Vế Trước Khi So Sánh Để Tránh Lỗi
     * Khoảng Trắng Thừa Như Bản Cũ.
     */
    private List<SinhVien> apDungBoLoc(List<SinhVien> list) {
        // "Tất Cả" (Bấm Nút Riêng, Hoặc Đóng 1 Khoa Đang Mở Lại) Dùng Giá Trị
        // Đặc Biệt TAT_CA (khoa = "__TAT_CA__") Để Phân Biệt Với Trạng Thái
        // "Chưa Chọn Gì" (luaChonHienTai == null) - Cả 2 Trường Hợp Đều KHÔNG Lọc.
        if (luaChonHienTai == null || luaChonHienTai == KhoaLopNavPanel.LuaChon.TAT_CA) return list;

        List<SinhVien> ket = new ArrayList<>();
        for (SinhVien sv : list) {
            String khoaSV = sv.getKhoa() == null ? "(Chưa Phân Khoa)" : sv.getKhoa().trim();
            String lopSV = sv.getLop() == null ? "(Chưa Phân Lớp)" : sv.getLop().trim();
            if (!luaChonHienTai.khoa.equalsIgnoreCase(khoaSV)) continue;
            if (luaChonHienTai.lop != null && !luaChonHienTai.lop.equalsIgnoreCase(lopSV)) continue;
            ket.add(sv);
        }
        return ket;
    }

    // ================== THÊM / SỬA / XÓA ==================

    private void moFormThem() {
        SinhVienFormDialog dialog = new SinhVienFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null,
                sv -> {
                    try {
                        sinhVienService.them(sv);
                        taiBoLoc();
                        taiDuLieu(null);
                        UIUtils.thongBao(this, "Đã Thêm Sinh Viên " + sv.getMaSV());
                        new SinhVienQRDialog((Frame) SwingUtilities.getWindowAncestor(this), sv).setVisible(true);
                    } catch (Exception ex) {
                        UIUtils.thongBaoLoi(this, rootMessage(ex));
                    }
                });
        dialog.setVisible(true);
    }

    private void moTuMaQR() {
        QuetQRSinhVienDialog dialog = new QuetQRSinhVienDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                sv -> { taiBoLoc(); taiDuLieu(null); });
        dialog.setVisible(true);
    }
    private void moFormSua() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.thongBaoLoi(this, "Vui Lòng Chọn 1 Sinh Viên Trong Bảng Để Sửa");
            return;
        }
        String maSV = (String) tableModel.getValueAt(row, 0);

        SwingWorker<SinhVien, Void> worker = new SwingWorker<>() {
            @Override
            protected SinhVien doInBackground() throws Exception {
                return sinhVienService.timKiem(maSV).stream()
                        .filter(sv -> sv.getMaSV().equals(maSV)).findFirst().orElse(null);
            }

            @Override
            protected void done() {
                try {
                    SinhVien sv = get();
                    if (sv == null) return;
                    SinhVienFormDialog dialog = new SinhVienFormDialog(
                            (Frame) SwingUtilities.getWindowAncestor(SinhVienPanel.this), sv,
                            updated -> {
                                try {
                                    sinhVienService.capNhat(updated);
                                    taiBoLoc();
                                    taiDuLieu(null);
                                    UIUtils.thongBao(SinhVienPanel.this, "Đã Cập Nhật Sinh Viên " + updated.getMaSV());
                                } catch (Exception ex) {
                                    UIUtils.thongBaoLoi(SinhVienPanel.this, rootMessage(ex));
                                }
                            });
                    dialog.setVisible(true);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(SinhVienPanel.this, rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void xoaSinhVienDangChon() {
        if (taiKhoan.getVaiTro() == vn.edu.eaut.qlhocphi.model.VaiTro.KETOAN) {
            vn.edu.eaut.qlhocphi.gui.common.ErrorScreens.hienTuChoiTruyCap(this, "Xóa Sinh Viên");
            return;
        }
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.thongBaoLoi(this, "Vui Lòng Chọn 1 Sinh Viên Trong Bảng Để Xóa");
            return;
        }
        String maSV = (String) tableModel.getValueAt(row, 0);
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Xóa Sinh Viên " + maSV + "? Thao Tác Này Cũng Xóa Các Hóa Đơn Liên Quan.",
                "Xác Nhận Xóa", JOptionPane.YES_NO_OPTION);
        if (xacNhan != JOptionPane.YES_OPTION) return;

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                sinhVienService.xoa(maSV);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    taiDuLieu(null);
                    UIUtils.thongBao(SinhVienPanel.this, "Đã Xóa Sinh Viên " + maSV);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(SinhVienPanel.this, rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    // ================== NHẬP EXCEL ==================

    private void moNhapExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chọn File Excel Danh Sách Sinh Viên (.xlsx)");
        chooser.setFileFilter(new FileNameExtensionFilter("Excel (*.xlsx)", "xlsx"));
        int ketQua = chooser.showOpenDialog(this);
        if (ketQua != JFileChooser.APPROVE_OPTION) return;
        File file = chooser.getSelectedFile();

        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            @Override
            protected int[] doInBackground() throws Exception {
                return nhapDanhSachTuExcel(file);
            }

            @Override
            protected void done() {
                try {
                    int[] tongKet = get();
                    taiBoLoc();
                    taiDuLieu(null);
                    UIUtils.thongBao(SinhVienPanel.this,
                            "Đã Nhập " + tongKet[0] + " Sinh Viên.\nBỏ Qua " + tongKet[1] + " Dòng Lỗi Dữ Liệu.");
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(SinhVienPanel.this, "Nhập Excel Thất Bại.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    /**
     * Đọc File Excel Theo Thứ Tự Cột: MaSV, HoTen, Lop, Khoa, Email, SDT (Dòng 1 Là Tiêu Đề, Bỏ Qua).
     * Sinh Viên Đã Tồn Tại (Trùng MaSV) Sẽ Được Cập Nhật Lại Thông Tin Thay Vì Báo Lỗi.
     * @return Mảng 2 Phần Tử: [0] = Số Dòng Nhập Thành Công, [1] = Số Dòng Bị Bỏ Qua Do Lỗi
     */
    private int[] nhapDanhSachTuExcel(File file) throws Exception {
        int thanhCong = 0, loi = 0;
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheetAt(0);
            DataFormatter formatter = new DataFormatter();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row hang = sheet.getRow(i);
                if (hang == null) continue;
                try {
                    String maSV = formatter.formatCellValue(hang.getCell(0)).trim();
                    if (maSV.isEmpty()) continue;

                    SinhVien sv = new SinhVien();
                    sv.setMaSV(maSV);
                    sv.setHoTen(formatter.formatCellValue(hang.getCell(1)).trim());
                    sv.setLop(formatter.formatCellValue(hang.getCell(2)).trim());
                    sv.setKhoa(formatter.formatCellValue(hang.getCell(3)).trim());
                    sv.setEmail(formatter.formatCellValue(hang.getCell(4)).trim());
                    sv.setSoDienThoai(formatter.formatCellValue(hang.getCell(5)).trim());
                    sv.setTrangThai(true);

                    if (sinhVienService.timTheoMa(maSV) == null) {
                        sinhVienService.them(sv);
                    } else {
                        sinhVienService.capNhat(sv);
                    }
                    thanhCong++;
                } catch (Exception dongLoi) {
                    loi++;
                }
            }
        }
        return new int[]{thanhCong, loi};
    }

    /** Nút Bo Góc Tròn, Nền 1 Màu Đặc Trưng Riêng - Dùng Chung Cho Các Nút Hành Động. */
    private JButton nutMau(String text, Color mauNen) {
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
        b.setFont(UITheme.FONT_BOLD);
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** Nút Gạt (Toggle) Đổi Màu Theo Trạng Thái: mauTat Khi Đang Tắt, mauBat Khi Đang Bật. */
    private JToggleButton nutToggleMau(String text, Color mauTat, Color mauBat) {
        JToggleButton b = new JToggleButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color mau = isSelected() ? mauBat : mauTat;
                g2.setColor(getModel().isRollover() ? mau.darker() : mau);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
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
        b.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private String rootMessage(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }
}