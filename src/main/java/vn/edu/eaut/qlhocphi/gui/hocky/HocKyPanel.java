package vn.edu.eaut.qlhocphi.gui.hocky;

import vn.edu.eaut.qlhocphi.bus.HocPhiService;
import vn.edu.eaut.qlhocphi.dal.SinhVienDAO;
import vn.edu.eaut.qlhocphi.model.SinhVien;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.AutoRefreshTimer;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.HocKy;
import vn.edu.eaut.qlhocphi.util.DateUtils;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;

/**
 * Màn hình quản lý học kỳ và đơn giá tín chỉ - bản "desktop quản lý":
 * có banner + KPI số liệu thật + cột Trạng thái tự động tính (Đang áp dụng /
 * Sắp diễn ra / Đã kết thúc / Thiếu dữ liệu) để cảnh báo dữ liệu chưa đầy đủ.
 */
public class HocKyPanel extends JPanel {
    private final HocPhiService hocPhiService = new HocPhiService();
    private final SinhVienDAO sinhVienDAO = new SinhVienDAO();
    private final vn.edu.eaut.qlhocphi.model.TaiKhoan taiKhoan;

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    private JTextField txtTen, txtNamHoc, txtDonGia, txtNgayBatDau, txtNgayKetThuc, txtTimKiem;
    private JButton btnLuu, btnHuy, btnXoa;
    private JLabel lblSoLuong;
    private JLabel lblTieuDeForm;
    private JPanel formCard;
    private JPanel kpiRow;
    private JComboBox<String> cboChonHocKy;
    private boolean dangDongBoChonHocKy = false;

    /** Danh sách học kỳ đang tải gần nhất - dùng để tính trạng thái + KPI (không thể lấy đủ từ bảng hiển thị). */
    private List<HocKy> danhSachHienTai = new ArrayList<>();

    /** null = đang thêm mới, khác null = đang sửa học kỳ có Mã = giá trị này */
    private Integer dangSuaMaHocKy = null;

    public HocKyPanel(vn.edu.eaut.qlhocphi.model.TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        setLayout(new BorderLayout(0, 16));
        setOpaque(false);

        JPanel north = new JPanel();
        north.setOpaque(false);
        north.setLayout(new BoxLayout(north, BoxLayout.Y_AXIS));
        north.add(buildBanner());
        north.add(Box.createRigidArea(new Dimension(0, 16)));
        kpiRow = buildKpiRow();
        north.add(kpiRow);
        add(north, BorderLayout.NORTH);

        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.add(buildFormCard(), BorderLayout.WEST);
        center.add(buildTableCard(), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        taiDuLieu();
        // 30 giây (thay vì 15) vì màn hình này có form nhập liệu bên trái -
        // dù taiDuLieu() không đụng form, vẫn ưu tiên độ tải ít hơn cho an toàn.
        AutoRefreshTimer.gan(this, 30, this::taiDuLieu);
    }

    // ================= BANNER =================

    private JPanel buildBanner() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 84));

        JPanel trai = new JPanel(new BorderLayout(14, 0));
        trai.setOpaque(false);
        trai.add(logoBadge(), BorderLayout.WEST);

        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));
        JLabel tieuDe = new JLabel("Học kỳ & mức học phí");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        lblSoLuong = new JLabel("Đang tải...");
        lblSoLuong.setFont(UITheme.FONT_BASE);
        lblSoLuong.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(lblSoLuong);
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
                FontMetrics fm = g2.getFontMetrics();
                String bieuTuong = "\uD83C\uDF93";
                int x = (getWidth() - fm.stringWidth(bieuTuong)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(bieuTuong, x, y);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(48, 48));
        badge.setOpaque(false);
        return badge;
    }

    // ================= KPI SỐ LIỆU THẬT =================

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.add(UITheme.statCard("Tổng số học kỳ", "0", UITheme.TINT_BLUE, UITheme.TEXT_BLUE));
        row.add(UITheme.statCard("Đang áp dụng", "0", UITheme.TINT_GREEN, UITheme.TEXT_GREEN));
        row.add(UITheme.statCard("Đơn giá TB / tín chỉ", "0 đ", UITheme.TINT_VIOLET, UITheme.TEXT_VIOLET));
        row.add(UITheme.statCard("Thiếu ngày cấu hình", "0", UITheme.TINT_RED, UITheme.TEXT_RED));
        return row;
    }

    private void capNhatKpi() {
        int tong = danhSachHienTai.size();
        int dangApDung = 0, thieuDuLieu = 0;
        BigDecimal tongDonGia = BigDecimal.ZERO;
        LocalDate homNay = LocalDate.now();

        for (HocKy hk : danhSachHienTai) {
            tongDonGia = tongDonGia.add(hk.getDonGiaTinChi() == null ? BigDecimal.ZERO : hk.getDonGiaTinChi());
            String trangThai = tinhTrangThai(hk, homNay);
            if (trangThai.equals("Đang áp dụng")) dangApDung++;
            if (trangThai.equals("Thiếu dữ liệu")) thieuDuLieu++;
        }
        BigDecimal trungBinh = tong > 0
                ? tongDonGia.divide(BigDecimal.valueOf(tong), 0, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        capNhatMotTheKpi(0, String.valueOf(tong));
        capNhatMotTheKpi(1, String.valueOf(dangApDung));
        capNhatMotTheKpi(2, MoneyUtils.format(trungBinh));
        capNhatMotTheKpi(3, String.valueOf(thieuDuLieu));
    }

    private void capNhatMotTheKpi(int index, String giaTriMoi) {
        JPanel the = (JPanel) kpiRow.getComponent(index);
        for (Component c : the.getComponents()) {
            if (c instanceof JLabel && "giaTri".equals(c.getName())) {
                ((JLabel) c).setText(giaTriMoi);
            }
        }
    }

    /** Tính trạng thái 1 học kỳ dựa trên ngày bắt đầu/kết thúc so với ngày hôm nay. */
    private String tinhTrangThai(HocKy hk, LocalDate homNay) {
        if (hk.getNgayBatDau() == null || hk.getNgayKetThuc() == null) return "Thiếu dữ liệu";
        if (homNay.isBefore(hk.getNgayBatDau())) return "Sắp diễn ra";
        if (homNay.isAfter(hk.getNgayKetThuc())) return "Đã kết thúc";
        return "Đang áp dụng";
    }

    // ================= FORM (THÊM / SỬA) =================
    private JPanel buildFormCard() {
        formCard = UITheme.card();
        formCard.setPreferredSize(new Dimension(300, 0));
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, UITheme.ACCENT_TEAL),
                        BorderFactory.createLineBorder(UITheme.BORDER, 1, true)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        JLabel lblChonNhanh = UIUtils.formLabel("Loại / Chọn nhanh:");
        lblChonNhanh.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(lblChonNhanh);
        formCard.add(Box.createRigidArea(new Dimension(0, 4)));

        cboChonHocKy = new JComboBox<>();
        cboChonHocKy.setFont(UITheme.FONT_BASE);
        cboChonHocKy.setAlignmentX(Component.LEFT_ALIGNMENT);
        cboChonHocKy.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cboChonHocKy.addActionListener(e -> onChonComboHocKy());
        formCard.add(cboChonHocKy);
        formCard.add(Box.createRigidArea(new Dimension(0, 6)));

        JLabel lblGhiChuLoai = new JLabel("<html>3 mục đầu là tạo HỌC KỲ MỚI theo loại. "
                + "Các mục còn lại là sửa học kỳ đã có sẵn.</html>");
        lblGhiChuLoai.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblGhiChuLoai.setForeground(UITheme.TEXT_MUTED);
        lblGhiChuLoai.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(lblGhiChuLoai);
        formCard.add(Box.createRigidArea(new Dimension(0, 6)));
        formCard.add(new JSeparator());
        formCard.add(Box.createRigidArea(new Dimension(0, 14)));

        lblTieuDeForm = UITheme.sectionLabel("Thêm học kỳ mới");
        formCard.add(lblTieuDeForm);
        formCard.add(Box.createRigidArea(new Dimension(0, 14)));

        txtTen = UIUtils.textField(16);
        txtNamHoc = UIUtils.textField(16);
        txtDonGia = UIUtils.textField(16);
        txtNgayBatDau = UIUtils.textField(16);
        txtNgayKetThuc = UIUtils.textField(16);

        themDong(formCard, "Tên học kỳ (VD: Học kỳ 1) *", txtTen);
        themDong(formCard, "Năm học (VD: 2025-2026) *", txtNamHoc);
        themDong(formCard, "Đơn giá / tín chỉ (VND) *", txtDonGia);
        themDong(formCard, "Ngày bắt đầu (dd/MM/yyyy)", txtNgayBatDau);
        themDong(formCard, "Ngày kết thúc (dd/MM/yyyy)", txtNgayKetThuc);

        JLabel lblGoiY = new JLabel("* Nếu bỏ trống ngày, hệ thống sẽ không tính được trạng thái áp dụng.");
        lblGoiY.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblGoiY.setForeground(UITheme.TEXT_MUTED);
        lblGoiY.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(lblGoiY);
        formCard.add(Box.createRigidArea(new Dimension(0, 14)));

        btnLuu = nutMauSac("+ Thêm học kỳ", UITheme.PRIMARY);
        btnLuu.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLuu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnLuu.addActionListener(e -> luuHocKy());

        btnHuy = nutMauSac("Hủy / Làm mới", UITheme.SIDEBAR_BLUE);
        btnHuy.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnHuy.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnHuy.addActionListener(e -> lamMoiForm());

        formCard.add(btnLuu);
        formCard.add(Box.createRigidArea(new Dimension(0, 8)));
        formCard.add(btnHuy);

        return formCard;
    }

    /** Nut chuc nang to mau dam rieng biet - moi nut 1 mau, khong con dung
     *  "secondaryButton" (vien trang nhat, de bi mo) nhu truoc. Mau nen truyen
     *  vao la field UITheme nen tu dong doi dung theo Sang/Toi. */
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

    /** Đổi màu vạch + tiêu đề form theo trạng thái thêm-mới/đang-sửa, để người dùng nhận biết rõ đang làm gì. */
    private void capNhatGiaoDienForm(boolean dangSua, String tenHocKyDangSua) {
        Color mauVach = dangSua ? UITheme.PRIMARY : UITheme.ACCENT_TEAL;
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, mauVach),
                        BorderFactory.createLineBorder(UITheme.BORDER, 1, true)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        lblTieuDeForm.setText(dangSua ? "Đang sửa: " + tenHocKyDangSua : "Thêm học kỳ mới");
        formCard.revalidate();
        formCard.repaint();
    }


    /** 3 mục đầu tiên của combo: tạo học kỳ mới theo từng loại, mỗi loại có màu/tên gợi ý riêng. */
    private void apDungLoaiTaoMoi(String nhanLoai, Color mauVach, String tenGoiY) {
        dangSuaMaHocKy = null;
        txtTen.setText(tenGoiY);
        txtNamHoc.setText("");
        txtDonGia.setText("");
        txtNgayBatDau.setText("");
        txtNgayKetThuc.setText("");
        btnLuu.setText("+ Thêm " + nhanLoai.toLowerCase());
        btnXoa.setEnabled(false);
        table.clearSelection();

        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, mauVach),
                        BorderFactory.createLineBorder(UITheme.BORDER, 1, true)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        lblTieuDeForm.setText("Thêm " + nhanLoai + " mới");
        formCard.revalidate();
        formCard.repaint();
    }

    /** Đổ 1 học kỳ đã có vào form để sửa - dùng chung cho cả "bấm dòng bảng" và "chọn combo". */
    private void apDuLieuVaoForm(HocKy hk) {
        dangSuaMaHocKy = hk.getMaHocKy();
        txtTen.setText(hk.getTenHocKy());
        txtNamHoc.setText(hk.getNamHoc());
        txtDonGia.setText(hk.getDonGiaTinChi() == null ? "" : hk.getDonGiaTinChi().toBigInteger().toString());
        txtNgayBatDau.setText(DateUtils.format(hk.getNgayBatDau()));
        txtNgayKetThuc.setText(DateUtils.format(hk.getNgayKetThuc()));
        btnLuu.setText("Cập nhật học kỳ");
        btnXoa.setEnabled(true);
        capNhatGiaoDienForm(true, hk.getTenHocKy());
    }

    /** Xử lý khi Admin chọn 1 mục trong combo "Loại / Chọn nhanh". */
    private void onChonComboHocKy() {
        if (dangDongBoChonHocKy) return;
        int idx = cboChonHocKy.getSelectedIndex();
        int namNay = LocalDate.now().getYear();

        if (idx == 0) {
            apDungLoaiTaoMoi("Học kỳ chính", UITheme.ACCENT_TEAL, "");
        } else if (idx == 1) {
            apDungLoaiTaoMoi("Học kỳ Hè", UITheme.WARNING, "Học kỳ Hè " + namNay);
        } else if (idx == 2) {
            apDungLoaiTaoMoi("Học kỳ học lại", UITheme.DANGER, "Học kỳ học lại " + namNay);
        } else if (idx >= 3) {
            HocKy hk = danhSachHienTai.get(idx - 3);
            apDuLieuVaoForm(hk);
            moDangKyChoHocKy(hk);
        }
    }

    /** Nạp lại danh sách cho combo: 3 mục loại cố định + toàn bộ học kỳ đã có. */
    private void capNhatComboChonHocKy() {
        dangDongBoChonHocKy = true;
        int viTriDangChon = cboChonHocKy.getItemCount() > 0 ? cboChonHocKy.getSelectedIndex() : 0;
        cboChonHocKy.removeAllItems();
        cboChonHocKy.addItem("+ Tạo Học kỳ chính mới");
        cboChonHocKy.addItem("+ Tạo Học kỳ Hè mới");
        cboChonHocKy.addItem("+ Tạo Học kỳ học lại mới");
        for (HocKy hk : danhSachHienTai) {
            cboChonHocKy.addItem(hk.getTenHocKy() + " - " + hk.getNamHoc());
        }
        cboChonHocKy.setSelectedIndex(Math.max(0, Math.min(viTriDangChon, cboChonHocKy.getItemCount() - 1)));
        dangDongBoChonHocKy = false;
    }

    /** Đồng bộ combo về đúng học kỳ vừa bấm trong bảng, không kích hoạt lại sự kiện onChonComboHocKy. */
    private void dongBoComboTheoMa(int maHK) {
        for (int i = 0; i < danhSachHienTai.size(); i++) {
            if (danhSachHienTai.get(i).getMaHocKy() == maHK) {
                dangDongBoChonHocKy = true;
                cboChonHocKy.setSelectedIndex(i + 3);
                dangDongBoChonHocKy = false;
                break;
            }
        }
    }

    // ================= BẢNG + TÌM KIẾM =================
    private JPanel buildTableCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);
        txtTimKiem = UIUtils.textField(18);
        toolbar.add(UIUtils.formLabel("Tìm kiếm: "), BorderLayout.WEST);
        toolbar.add(txtTimKiem, BorderLayout.CENTER);

        btnXoa = nutNguyHiemToVe("Xóa học kỳ đã chọn");
        btnXoa.setEnabled(false);
        btnXoa.addActionListener(e -> xoaHocKy());
        if (taiKhoan.getVaiTro() == vn.edu.eaut.qlhocphi.model.VaiTro.KETOAN) {
            btnXoa.setVisible(false);
        }
        toolbar.add(btnXoa, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Mã HK", "Tên học kỳ", "Năm học", "Đơn giá/tín chỉ",
                        "Ngày bắt đầu", "Ngày kết thúc", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(6).setCellRenderer(new TrangThaiCellRenderer());

        table.getSelectionModel().addListSelectionListener(this::onChonDong);

        card.add(new JScrollPane(table), BorderLayout.CENTER);

        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { locDuLieu(); }
            public void removeUpdate(DocumentEvent e) { locDuLieu(); }
            public void changedUpdate(DocumentEvent e) { locDuLieu(); }
        });

        return card;
    }

    /** Renderer vẽ trạng thái dạng "thẻ màu" (pill), đồng bộ với cách làm ở HoaDonPanel/CongNoPanel. */
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
            switch (text) {
                case "Đang áp dụng": bg = UITheme.TINT_GREEN; fg = UITheme.TEXT_GREEN; break;
                case "Sắp diễn ra":  bg = UITheme.TINT_BLUE;  fg = UITheme.TEXT_BLUE;  break;
                case "Thiếu dữ liệu": bg = UITheme.TINT_RED;  fg = UITheme.TEXT_RED;   break;
                default:             bg = new Color(0xEC, 0xEE, 0xF2); fg = UITheme.TEXT_MUTED; // Đã kết thúc
            }
            if (!isSelected) {
                label.setBackground(bg);
                label.setForeground(fg);
            }
            return label;
        }
    }

    private void locDuLieu() {
        String tuKhoa = txtTimKiem.getText().trim();
        if (tuKhoa.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(tuKhoa)));
        }
    }

    // ================= CHỌN DÒNG ĐỂ SỬA =================
    private void onChonDong(ListSelectionEvent e) {
        if (e.getValueIsAdjusting()) return;
        int row = table.getSelectedRow();
        if (row < 0) {
            btnXoa.setEnabled(false);
            return;
        }
        int modelRow = table.convertRowIndexToModel(row);
        int maHK = (Integer) tableModel.getValueAt(modelRow, 0);
        HocKy hk = danhSachHienTai.stream().filter(h -> h.getMaHocKy() == maHK).findFirst().orElse(null);
        if (hk == null) return;

        apDuLieuVaoForm(hk);
        dongBoComboTheoMa(maHK);
    }

    private void lamMoiForm() {
        if (cboChonHocKy != null) {
            dangDongBoChonHocKy = true;
            cboChonHocKy.setSelectedIndex(0);
            dangDongBoChonHocKy = false;
        }
        apDungLoaiTaoMoi("Học kỳ chính", UITheme.ACCENT_TEAL, "");
        table.clearSelection();
    }

    // ================= TẢI DỮ LIỆU =================
    private void taiDuLieu() {
        taiDuLieu(null);
    }

    /** MOI: cho phep chay 1 hanh dong NGAY SAU KHI du lieu da tai xong va cap nhat
     *  vao danhSachHienTai - dung de tu dong mo dialog "gan hang loat" cho hoc ky
     *  VUA TAO MOI (luuHocKy()), khong can nguoi dung phai chon lai tu combo. */
    private void taiDuLieu(Runnable khiXongCallback) {
        SwingWorker<List<HocKy>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<HocKy> doInBackground() throws Exception {
                return hocPhiService.layTatCaHocKy();
            }

            @Override
            protected void done() {
                try {
                    List<HocKy> list = get();
                    danhSachHienTai = list;
                    capNhatComboChonHocKy();
                    LocalDate homNay = LocalDate.now();
                    tableModel.setRowCount(0);
                    for (HocKy hk : list) {
                        tableModel.addRow(new Object[]{
                                hk.getMaHocKy(), hk.getTenHocKy(), hk.getNamHoc(),
                                MoneyUtils.format(hk.getDonGiaTinChi()),
                                DateUtils.format(hk.getNgayBatDau()),
                                DateUtils.format(hk.getNgayKetThuc()),
                                tinhTrangThai(hk, homNay)
                        });
                    }
                    lblSoLuong.setText(list.size() + " học kỳ đã tạo");
                    capNhatKpi();
                    if (khiXongCallback != null) khiXongCallback.run();
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(HocKyPanel.this, "Không thể tải danh sách học kỳ.");
                }
            }
        };
        worker.execute();
    }

    // ================= THÊM / CẬP NHẬT =================
    private void luuHocKy() {
        String ten = txtTen.getText().trim();
        String namHoc = txtNamHoc.getText().trim();
        String donGiaText = txtDonGia.getText().trim().replaceAll("[^0-9]", "");

        if (ten.isEmpty() || namHoc.isEmpty() || donGiaText.isEmpty()) {
            UIUtils.thongBaoLoi(this, "Vui lòng nhập đầy đủ thông tin bắt buộc (đánh dấu *)");
            return;
        }
        BigDecimal donGia;
        try {
            donGia = new BigDecimal(donGiaText);
        } catch (NumberFormatException ex) {
            UIUtils.thongBaoLoi(this, "Đơn giá phải là số hợp lệ");
            return;
        }

        LocalDate ngayBatDau = DateUtils.parseDate(txtNgayBatDau.getText().trim());
        LocalDate ngayKetThuc = DateUtils.parseDate(txtNgayKetThuc.getText().trim());
        if (!txtNgayBatDau.getText().trim().isEmpty() && ngayBatDau == null) {
            UIUtils.thongBaoLoi(this, "Ngày bắt đầu sai định dạng, dùng dd/MM/yyyy");
            return;
        }
        if (!txtNgayKetThuc.getText().trim().isEmpty() && ngayKetThuc == null) {
            UIUtils.thongBaoLoi(this, "Ngày kết thúc sai định dạng, dùng dd/MM/yyyy");
            return;
        }
        if (ngayBatDau != null && ngayKetThuc != null && ngayKetThuc.isBefore(ngayBatDau)) {
            UIUtils.thongBaoLoi(this, "Ngày kết thúc phải sau ngày bắt đầu");
            return;
        }

        HocKy hk = new HocKy();
        hk.setTenHocKy(ten);
        hk.setNamHoc(namHoc);
        hk.setDonGiaTinChi(donGia);
        hk.setNgayBatDau(ngayBatDau);
        hk.setNgayKetThuc(ngayKetThuc);

        boolean dangSua = dangSuaMaHocKy != null;
        if (dangSua) hk.setMaHocKy(dangSuaMaHocKy);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                if (dangSua) {
                    hocPhiService.capNhatHocKy(hk);
                } else {
                    hocPhiService.themHocKy(hk);
                }
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    lamMoiForm();
                    if (dangSua) {
                        taiDuLieu();
                        UIUtils.thongBao(HocKyPanel.this, "Đã cập nhật học kỳ");
                    } else {
                        // MOI: hoc ky vua tao xong -> tu dong mo ngay dialog gan hang loat
                        // theo Khoa/Lop, khong bat nguoi dung phai quay lai chon combo nua.
                        taiDuLieu(() -> {
                            // ✅ SUA: lay theo Ma HK LON NHAT (auto-increment) thay vi so khop chuoi
                            // ten/nam hoc - cach cu de bi "false negative" neu chuoi luu trong DB
                            // lech 1 ky tu khoang trang so voi chuoi go tren form, khien hkVuaTao = null
                            // va dialog dang ky hang loat khong bao gio hien len.
                            HocKy hkVuaTao = danhSachHienTai.stream()
                                    .max(java.util.Comparator.comparingInt(HocKy::getMaHocKy))
                                    .orElse(null);
                            UIUtils.thongBao(HocKyPanel.this, "Đã thêm học kỳ mới");
                            if (hkVuaTao != null) {
                                moDangKyChoHocKy(hkVuaTao, true); // true = mac dinh mo tab "Hang loat theo Khoa/Lop"
                            }
                        });
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(HocKyPanel.this, cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ================= XÓA =================
    private void xoaHocKy() {
        if (taiKhoan.getVaiTro() == vn.edu.eaut.qlhocphi.model.VaiTro.KETOAN) {
            vn.edu.eaut.qlhocphi.gui.common.ErrorScreens.hienTuChoiTruyCap(this, "Xóa học kỳ");
            return;
        }
        if (dangSuaMaHocKy == null) return;
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Bạn có chắc muốn xóa học kỳ này?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (xacNhan != JOptionPane.YES_OPTION) return;

        int maHocKy = dangSuaMaHocKy;
        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                hocPhiService.xoaHocKy(maHocKy);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    lamMoiForm();
                    taiDuLieu();
                    UIUtils.thongBao(HocKyPanel.this, "Đã xóa học kỳ");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(HocKyPanel.this, cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ================= ĐĂNG KÝ HỌC PHÍ THEO TỪNG LOẠI HỌC KỲ =================

    /** 3 loại học kỳ, mỗi loại có màu/icon/tiêu đề riêng cho dialog đăng ký. */
    private enum LoaiHocKy {
        HOC_LAI("Đăng ký HỌC LẠI", "\u26A0", UITheme.DANGER, UITheme.TINT_RED,
                "Sinh viên đăng ký học lại sẽ áp dụng đơn giá riêng của học kỳ này. Vui lòng kiểm tra kỹ trước khi sinh hóa đơn."),
        HE("Đăng ký HỌC KỲ HÈ", "\u2600", UITheme.WARNING, new Color(0xFD, 0xF3, 0xDA),
                "Học kỳ Hè thường có số tín chỉ ít hơn học kỳ chính. Kiểm tra đúng số tín chỉ sinh viên đăng ký."),
        CHINH("Đăng ký học phí", "\uD83C\uDF93", UITheme.ACCENT_TEAL, UITheme.TINT_GREEN,
                "Đăng ký học phí theo học kỳ chính khóa cho sinh viên.");

        final String tieuDe, icon, moTa;
        final Color mauChinh, mauNen;

        LoaiHocKy(String tieuDe, String icon, Color mauChinh, Color mauNen, String moTa) {
            this.tieuDe = tieuDe;
            this.icon = icon;
            this.mauChinh = mauChinh;
            this.mauNen = mauNen;
            this.moTa = moTa;
        }
    }

    /** Bỏ dấu tiếng Việt để nhận diện loại học kỳ không phụ thuộc viết có dấu hay không. */
    private String boDauTiengViet(String s) {
        if (s == null) return "";
        String norm = Normalizer.normalize(s, Normalizer.Form.NFD);
        return norm.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd').replace('Đ', 'D').toLowerCase();
    }

    /** Nut "nguy hiem" (xoa) tu ve bang Graphics2D thay vi dua vao setBackground()
     *  cua JButton thuong - tranh bi LookAndFeel he thong (Windows) de them lop
     *  gradient bong len tren lam mau bi "loa" sang, giam do tuong phan voi chu
     *  trang, kho doc (dung loi nay khi thay nut do bi "lóa"). */
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

    /** Nhận diện loại học kỳ từ tên (không sửa CSDL, chỉ đọc chuỗi tên để phân loại hiển thị). */
    private LoaiHocKy xacDinhLoai(HocKy hk) {
        String ten = boDauTiengViet(hk.getTenHocKy());
        if (ten.contains("hoc lai")) return LoaiHocKy.HOC_LAI;
        if (ten.contains("he")) return LoaiHocKy.HE;
        return LoaiHocKy.CHINH;
    }

    /** Mở dialog "Đăng ký học phí" riêng cho học kỳ vừa chọn - giao diện đổi màu/icon/tiêu đề theo loại. */
    /** Mở dialog "Đăng ký học phí" riêng cho học kỳ vừa chọn - giao diện đổi màu/icon/tiêu đề theo loại.
     *  MOI: them lua chon "Dang ky hang loat theo Khoa/Lop" - thay vi phai nhap tung Ma SV
     *  mot, Admin chi can chon Khoa (hoac "Tat ca") + Lop (hoac "Tat ca") la tu dong sinh
     *  hoa don cho MOI sinh vien khop dieu kien do trong 1 lan bam. */
    private void moDangKyChoHocKy(HocKy hk) {
        moDangKyChoHocKy(hk, false); // mac dinh giu nguyen hanh vi cu: mo tab "1 Ma sinh vien"
    }

    /** macDinhHangLoat = true: mo san tab "Hang loat theo Khoa/Lop" thay vi "1 Ma sinh vien" -
     *  dung khi vua tao xong 1 hoc ky moi (xem luuHocKy()), giup Admin gan ngay cho ca Khoa/Lop
     *  ma khong phai tu tay chuyen tab. */
    private void moDangKyChoHocKy(HocKy hk, boolean macDinhHangLoat) {
        LoaiHocKy loai = xacDinhLoai(hk);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), loai.tieuDe, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(460, 560);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UITheme.BG_MAIN);
        dialog.setLayout(new BorderLayout());

        // Banner màu riêng theo loại
        JPanel banner = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(loai.mauChinh);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        banner.setPreferredSize(new Dimension(10, 84));
        banner.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));

        JLabel lblIcon = new JLabel(loai.icon, SwingConstants.CENTER);
        lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 26));
        lblIcon.setForeground(Color.WHITE);
        lblIcon.setOpaque(true);
        lblIcon.setBackground(new Color(255, 255, 255, 50));
        lblIcon.setPreferredSize(new Dimension(48, 48));
        banner.add(lblIcon, BorderLayout.WEST);

        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));
        JLabel lblTieuDe = new JLabel(loai.tieuDe);
        lblTieuDe.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTieuDe.setForeground(Color.WHITE);
        JLabel lblTenHK = new JLabel(hk.getTenHocKy() + " - " + hk.getNamHoc());
        lblTenHK.setFont(UITheme.FONT_BASE);
        lblTenHK.setForeground(new Color(255, 255, 255, 220));
        chuText.add(lblTieuDe);
        chuText.add(lblTenHK);
        banner.add(chuText, BorderLayout.CENTER);

        dialog.add(banner, BorderLayout.NORTH);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JPanel ghiChuBox = new JPanel();
        ghiChuBox.setLayout(new BoxLayout(ghiChuBox, BoxLayout.Y_AXIS));
        ghiChuBox.setBackground(loai.mauNen);
        ghiChuBox.setOpaque(true);
        ghiChuBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        ghiChuBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
        ghiChuBox.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JLabel lblGhiChu = new JLabel("<html>" + loai.moTa + "</html>");
        lblGhiChu.setFont(UITheme.FONT_BASE);
        lblGhiChu.setForeground(loai.mauChinh);
        ghiChuBox.add(lblGhiChu);
        form.add(ghiChuBox);
        form.add(Box.createRigidArea(new Dimension(0, 14)));

        // ===== MOI: Chon kieu dang ky - 1 sinh vien hay hang loat theo Khoa/Lop =====
        JLabel lblKieu = UIUtils.formLabel("Kiểu đăng ký:");
        lblKieu.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblKieu);
        form.add(Box.createRigidArea(new Dimension(0, 4)));

        JRadioButton rbMotSV = new JRadioButton("Theo 1 Mã sinh viên", !macDinhHangLoat);
        JRadioButton rbHangLoat = new JRadioButton("Hàng loạt theo Khoa/Lớp", macDinhHangLoat);
        rbMotSV.setOpaque(false);
        rbHangLoat.setOpaque(false);
        rbMotSV.setAlignmentX(Component.LEFT_ALIGNMENT);
        rbHangLoat.setAlignmentX(Component.LEFT_ALIGNMENT);
        ButtonGroup nhomKieu = new ButtonGroup();
        nhomKieu.add(rbMotSV);
        nhomKieu.add(rbHangLoat);
        form.add(rbMotSV);
        form.add(rbHangLoat);
        form.add(Box.createRigidArea(new Dimension(0, 10)));

        // Card "1 sinh vien": giu nguyen o Ma SV nhu ban cu
        JTextField txtMaSV = UIUtils.textField(18);
        JPanel cardMotSV = new JPanel();
        cardMotSV.setOpaque(false);
        cardMotSV.setLayout(new BoxLayout(cardMotSV, BoxLayout.Y_AXIS));
        cardMotSV.setAlignmentX(Component.LEFT_ALIGNMENT);
        themDong(cardMotSV, "Mã sinh viên cần đăng ký *", txtMaSV);

        // Card "Hang loat": chon Khoa + Lop, hien so luong sinh vien se ap dung
        JComboBox<String> cboKhoaHangLoat = new JComboBox<>(new String[]{"-- Đang tải --"});
        JComboBox<String> cboLopHangLoat = new JComboBox<>(new String[]{"-- Đang tải --"});
        cboKhoaHangLoat.setFont(UITheme.FONT_BASE);
        cboLopHangLoat.setFont(UITheme.FONT_BASE);
        cboKhoaHangLoat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cboLopHangLoat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cboKhoaHangLoat.setAlignmentX(Component.LEFT_ALIGNMENT);
        cboLopHangLoat.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblSoLuongApDung = new JLabel("Đang tải danh sách sinh viên...");
        lblSoLuongApDung.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSoLuongApDung.setForeground(loai.mauChinh);
        lblSoLuongApDung.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel cardHangLoat = new JPanel();
        cardHangLoat.setOpaque(false);
        cardHangLoat.setLayout(new BoxLayout(cardHangLoat, BoxLayout.Y_AXIS));
        cardHangLoat.setAlignmentX(Component.LEFT_ALIGNMENT);
        themDong(cardHangLoat, "Khoa (hoặc \"Tất cả\")", cboKhoaHangLoat);
        themDong(cardHangLoat, "Lớp (hoặc \"Tất cả\")", cboLopHangLoat);
        cardHangLoat.add(lblSoLuongApDung);

        CardLayout layoutChonKieu = new CardLayout();
        JPanel cardsChonKieu = new JPanel(layoutChonKieu);
        cardsChonKieu.setOpaque(false);
        cardsChonKieu.setAlignmentX(Component.LEFT_ALIGNMENT);
        cardsChonKieu.add(cardMotSV, "MOT_SV");
        cardsChonKieu.add(cardHangLoat, "HANG_LOAT");
        form.add(cardsChonKieu);
        if (macDinhHangLoat) layoutChonKieu.show(cardsChonKieu, "HANG_LOAT"); // hien dung tab ngay tu dau

        rbMotSV.addActionListener(e -> layoutChonKieu.show(cardsChonKieu, "MOT_SV"));
        rbHangLoat.addActionListener(e -> layoutChonKieu.show(cardsChonKieu, "HANG_LOAT"));

        // Tai danh sach sinh vien nen (chi 1 lan luc mo dialog), dung de do 2 combo Khoa/Lop
        // va tinh so luong khop dieu kien theo thoi gian thuc khi doi lua chon.
        final List<SinhVien>[] cacheSinhVien = new List[]{new ArrayList<>()};
        SwingWorker<List<SinhVien>, Void> taiSV = new SwingWorker<>() {
            @Override
            protected List<SinhVien> doInBackground() throws Exception {
                return sinhVienDAO.layTatCa();
            }
            @Override
            protected void done() {
                try {
                    cacheSinhVien[0] = get();
                    java.util.TreeSet<String> khoaSet = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
                    for (SinhVien sv : cacheSinhVien[0]) {
                        if (sv.getKhoa() != null && !sv.getKhoa().isBlank()) khoaSet.add(sv.getKhoa().trim());
                    }
                    cboKhoaHangLoat.removeAllItems();
                    cboKhoaHangLoat.addItem("Tất cả");
                    for (String k : khoaSet) cboKhoaHangLoat.addItem(k);
                    capNhatComboLopHangLoat(cboLopHangLoat, cacheSinhVien[0], "Tất cả");
                    capNhatSoLuongApDung(lblSoLuongApDung, cacheSinhVien[0], "Tất cả", "Tất cả");
                } catch (Exception ex) {
                    lblSoLuongApDung.setText("Không thể tải danh sách sinh viên.");
                }
            }
        };
        taiSV.execute();

        cboKhoaHangLoat.addActionListener(e -> {
            String khoaChon = (String) cboKhoaHangLoat.getSelectedItem();
            capNhatComboLopHangLoat(cboLopHangLoat, cacheSinhVien[0], khoaChon);
            capNhatSoLuongApDung(lblSoLuongApDung, cacheSinhVien[0], khoaChon, (String) cboLopHangLoat.getSelectedItem());
        });
        cboLopHangLoat.addActionListener(e ->
                capNhatSoLuongApDung(lblSoLuongApDung, cacheSinhVien[0], (String) cboKhoaHangLoat.getSelectedItem(), (String) cboLopHangLoat.getSelectedItem()));

        form.add(Box.createRigidArea(new Dimension(0, 14)));

        JTextField txtSoTinChi = UIUtils.textField(18);
        JTextField txtHanThanhToan = UIUtils.textField(18);
        txtHanThanhToan.setToolTipText("Định dạng: yyyy-MM-dd, VD 2026-01-15");
        themDong(form, "Số tín chỉ đăng ký *", txtSoTinChi);
        themDong(form, "Hạn thanh toán (yyyy-MM-dd)", txtHanThanhToan);

        JLabel lblMaHK = new JLabel("Mã học kỳ: #" + hk.getMaHocKy() + "  (tự động điền, không cần nhập)");
        lblMaHK.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblMaHK.setForeground(UITheme.TEXT_MUTED);
        lblMaHK.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblMaHK);

        JScrollPane scrollForm = new JScrollPane(form);
        scrollForm.setBorder(BorderFactory.createEmptyBorder());
        dialog.add(scrollForm, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setOpaque(false);
        JButton btnHuy = UITheme.secondaryButton("Đóng");
        JButton btnDangKy = new JButton("Đăng ký ngay");
        btnDangKy.setFont(UITheme.FONT_BOLD);
        btnDangKy.setBackground(loai.mauChinh);
        btnDangKy.setForeground(Color.WHITE);
        btnDangKy.setFocusPainted(false);
        btnDangKy.setBorderPainted(false);
        btnDangKy.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btnDangKy.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnHuy.addActionListener(e -> dialog.dispose());
        btnDangKy.addActionListener(e -> {
            String soTinChiText = txtSoTinChi.getText().trim();
            if (soTinChiText.isEmpty()) {
                UIUtils.thongBaoLoi(dialog, "Vui lòng nhập Số tín chỉ");
                return;
            }
            int soTinChi;
            LocalDate han;
            try {
                soTinChi = Integer.parseInt(soTinChiText);
                han = txtHanThanhToan.getText().trim().isEmpty() ? null : LocalDate.parse(txtHanThanhToan.getText().trim());
            } catch (Exception ex) {
                UIUtils.thongBaoLoi(dialog, "Số tín chỉ phải là số nguyên, hạn thanh toán đúng định dạng yyyy-MM-dd");
                return;
            }

            if (rbMotSV.isSelected()) {
                // ===== Giu nguyen luong dang ky 1 sinh vien nhu cu =====
                String maSV = txtMaSV.getText().trim();
                if (maSV.isEmpty()) {
                    UIUtils.thongBaoLoi(dialog, "Vui lòng nhập Mã sinh viên");
                    return;
                }
                btnDangKy.setEnabled(false);
                SwingWorker<Integer, Void> worker = new SwingWorker<>() {
                    @Override
                    protected Integer doInBackground() throws Exception {
                        return hocPhiService.sinhHoaDon(maSV, hk.getMaHocKy(), soTinChi, han);
                    }
                    @Override
                    protected void done() {
                        try {
                            get();
                            UIUtils.thongBao(HocKyPanel.this,
                                    "Đã đăng ký " + loai.tieuDe.toLowerCase() + " cho sinh viên " + maSV
                                            + " (" + hk.getTenHocKy() + ")");
                            dialog.dispose();
                        } catch (Exception ex) {
                            btnDangKy.setEnabled(true);
                            Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                            UIUtils.thongBaoLoi(dialog, cause.getMessage());
                        }
                    }
                };
                worker.execute();
            } else {
                // ===== MOI: Dang ky hang loat cho TOAN BO sinh vien khop Khoa/Lop da chon =====
                String khoaChon = (String) cboKhoaHangLoat.getSelectedItem();
                String lopChon = (String) cboLopHangLoat.getSelectedItem();
                List<SinhVien> danhSachApDung = locSinhVienTheoKhoaLop(cacheSinhVien[0], khoaChon, lopChon);
                if (danhSachApDung.isEmpty()) {
                    UIUtils.thongBaoLoi(dialog, "Không có sinh viên nào khớp với Khoa/Lớp đã chọn.");
                    return;
                }
                int xacNhan = JOptionPane.showConfirmDialog(dialog,
                        "Đăng ký " + loai.tieuDe.toLowerCase() + " cho " + danhSachApDung.size()
                                + " sinh viên (Khoa: " + khoaChon + ", Lớp: " + lopChon + ")?\n"
                                + "Sinh viên nào ĐÃ có hóa đơn học kỳ này sẽ tự động bị bỏ qua.",
                        "Xác nhận đăng ký hàng loạt", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (xacNhan != JOptionPane.YES_OPTION) return;

                btnDangKy.setEnabled(false);
                SwingWorker<int[], Void> worker = new SwingWorker<>() {
                    @Override
                    protected int[] doInBackground() {
                        int thanhCong = 0, boQua = 0;
                        for (SinhVien sv : danhSachApDung) {
                            try {
                                hocPhiService.sinhHoaDon(sv.getMaSV(), hk.getMaHocKy(), soTinChi, han);
                                thanhCong++;
                            } catch (Exception ex) {
                                boQua++; // Thuong la do sinh vien do da co hoa don o hoc ky nay roi
                            }
                        }
                        return new int[]{thanhCong, boQua};
                    }
                    @Override
                    protected void done() {
                        try {
                            int[] ketQua = get();
                            UIUtils.thongBao(HocKyPanel.this,
                                    "Đã đăng ký thành công cho " + ketQua[0] + " sinh viên."
                                            + (ketQua[1] > 0 ? "\nBỏ qua " + ketQua[1] + " sinh viên (có thể đã có hóa đơn học kỳ này)." : ""));
                            dialog.dispose();
                        } catch (Exception ex) {
                            btnDangKy.setEnabled(true);
                            UIUtils.thongBaoLoi(dialog, "Có lỗi khi đăng ký hàng loạt.");
                        }
                    }
                };
                worker.execute();
            }
        });

        actions.add(btnHuy);
        actions.add(btnDangKy);
        dialog.add(actions, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }

    /** MOI: nap lai combo Lop chi gom cac lop THUOC dung Khoa dang chon (hoac tat ca lop neu Khoa = "Tat ca"). */
    private void capNhatComboLopHangLoat(JComboBox<String> cboLop, List<SinhVien> tatCaSV, String khoaChon) {
        java.util.TreeSet<String> lopSet = new java.util.TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (SinhVien sv : tatCaSV) {
            if (sv.getLop() == null || sv.getLop().isBlank()) continue;
            if (!"Tất cả".equals(khoaChon) && (sv.getKhoa() == null || !sv.getKhoa().trim().equalsIgnoreCase(khoaChon))) continue;
            lopSet.add(sv.getLop().trim());
        }
        Object dangChon = cboLop.getSelectedItem();
        cboLop.removeAllItems();
        cboLop.addItem("Tất cả");
        for (String lop : lopSet) cboLop.addItem(lop);
        if (dangChon != null && lopSet.contains(dangChon)) cboLop.setSelectedItem(dangChon);
        else cboLop.setSelectedItem("Tất cả");
    }

    /** MOI: loc danh sach sinh vien khop dung Khoa/Lop da chon ("Tat ca" nghia la khong loc truong do). */
    private List<SinhVien> locSinhVienTheoKhoaLop(List<SinhVien> tatCaSV, String khoaChon, String lopChon) {
        List<SinhVien> ket = new ArrayList<>();
        for (SinhVien sv : tatCaSV) {
            boolean khopKhoa = "Tất cả".equals(khoaChon) || (sv.getKhoa() != null && sv.getKhoa().trim().equalsIgnoreCase(khoaChon));
            boolean khopLop = "Tất cả".equals(lopChon) || (sv.getLop() != null && sv.getLop().trim().equalsIgnoreCase(lopChon));
            if (khopKhoa && khopLop) ket.add(sv);
        }
        return ket;
    }

    /** MOI: cap nhat dong chu "Se ap dung cho: X sinh vien" moi khi doi Khoa/Lop, de Admin biet
     *  truoc so luong se bi anh huong TRUOC KHI bam "Dang ky ngay", tranh bam nham hang loat. */
    private void capNhatSoLuongApDung(JLabel lbl, List<SinhVien> tatCaSV, String khoaChon, String lopChon) {
        int soLuong = locSinhVienTheoKhoaLop(tatCaSV, khoaChon, lopChon).size();
        lbl.setText("Sẽ áp dụng cho: " + soLuong + " sinh viên");
    }
}