package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.bus.SinhVienService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.SinhVien;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * Tra cứu Sinh viên – Kế toán (chỉ xem).
 * Giao diện khác hẳn SinhVienPanel: search-first, bộ lọc ngang (teal), không cây nav trái.
 */
public class TraCuuSinhVienPanel extends JPanel {

    // Đồng bộ màu hệ thống (Sky / PRIMARY) — không dùng teal lệch theme
    private static final Color ACCENT = UITheme.PRIMARY;
    private static final Color ACCENT_DARK = UITheme.PRIMARY_DARK;
    private static final Color CHIP_BG = UITheme.TINT_BLUE != null ? UITheme.TINT_BLUE : new Color(0xE0, 0xF2, 0xFE);
    private static final Color SEARCH_BG = new Color(0xF0, 0xF9, 0xFF);
    private static final Color BTN_ACCENT = UITheme.PRIMARY;
    private static final Color BTN_SUCCESS = UITheme.SUCCESS;

    private final SinhVienService sinhVienService = new SinhVienService();
    private final TaiKhoan taiKhoan;
    private final BiConsumer<String, String> dieuHuongTimKiem;

    private JTextField txtTimKiem;
    private JComboBox<String> cboKhoa;
    private JComboBox<String> cboNam;
    private JComboBox<String> cboLop;
    private JComboBox<String> cboTrangThai;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblKetQua;
    private JLabel lblHint;
    private JButton btnXemChiTiet, btnXemHoaDon, btnLapPhieuThu;

    private List<SinhVien> danhSachGoc = new ArrayList<>();

    public TraCuuSinhVienPanel(TaiKhoan taiKhoan) {
        this(taiKhoan, null);
    }

    public TraCuuSinhVienPanel(TaiKhoan taiKhoan, BiConsumer<String, String> dieuHuongTimKiem) {
        this.taiKhoan = taiKhoan;
        this.dieuHuongTimKiem = dieuHuongTimKiem;
        setLayout(new BorderLayout(0, 0));
        setOpaque(false);

        add(buildTopSearch(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);

        napBoLocVaDuLieu();
    }

    public void timKiem(String keyword) {
        if (txtTimKiem != null) {
            txtTimKiem.setForeground(UITheme.TEXT_PRIMARY);
            txtTimKiem.setText(keyword != null ? keyword : "");
            thucHienTim();
        }
    }

    private JPanel buildTopSearch() {
        JPanel wrap = new JPanel(new BorderLayout(0, 0));
        wrap.setOpaque(false);
        wrap.setBorder(new EmptyBorder(0, 0, 16, 0));

        JPanel banner = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = UITheme.PRIMARY_DARK != null ? UITheme.PRIMARY_DARK : new Color(0x1E, 0x40, 0xAF);
                Color c2 = UITheme.PRIMARY != null ? UITheme.PRIMARY : new Color(0x3B, 0x82, 0xF6);
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), 0, c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        banner.setOpaque(false);
        banner.setBorder(new EmptyBorder(16, 20, 16, 20));
        banner.setPreferredSize(new Dimension(10, 78));

        JPanel trai = new JPanel();
        trai.setOpaque(false);
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));
        JLabel tieuDe = new JLabel("Tra cứu sinh viên");
        tieuDe.setFont(new Font("Segoe UI", Font.BOLD, 20));
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Tìm hồ sơ nhanh để thu học phí  ·  Chỉ xem");
        phu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        phu.setForeground(new Color(255, 255, 255, 200));
        trai.add(tieuDe);
        trai.add(Box.createVerticalStrut(4));
        trai.add(phu);
        banner.add(trai, BorderLayout.WEST);

        JLabel badge = new JLabel("  READ-ONLY  ");
        badge.setFont(new Font("Segoe UI", Font.BOLD, 11));
        badge.setForeground(UITheme.PRIMARY_DARK);
        badge.setOpaque(true);
        badge.setBackground(Color.WHITE);
        badge.setBorder(new EmptyBorder(5, 10, 5, 10));
        JPanel badgeBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        badgeBox.setOpaque(false);
        badgeBox.add(badge);
        banner.add(badgeBox, BorderLayout.EAST);

        wrap.add(banner, BorderLayout.NORTH);

        JPanel searchCard = new JPanel(new BorderLayout(10, 0));
        searchCard.setBackground(UITheme.BG_CARD);
        searchCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xBF, 0xDB, 0xFE), 1),
                new EmptyBorder(14, 16, 14, 16)));

        txtTimKiem = new JTextField();
        txtTimKiem.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txtTimKiem.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.PRIMARY, 2, true),
                new EmptyBorder(10, 14, 10, 14)));
        txtTimKiem.setBackground(SEARCH_BG);
        txtTimKiem.setToolTipText("Nhập Mã SV, họ tên hoặc lớp rồi Enter");
        datPlaceholder(txtTimKiem, "Nhập mã SV, họ tên hoặc lớp…");
        txtTimKiem.addActionListener(e -> thucHienTim());

        JButton btnTim = nutTeal("Tìm kiếm");
        btnTim.addActionListener(e -> thucHienTim());

        JButton btnXoa = nutOutline("Xóa");
        btnXoa.addActionListener(e -> {
            txtTimKiem.setText("");
            datPlaceholder(txtTimKiem, "Nhập mã SV, họ tên hoặc lớp…");
            cboKhoa.setSelectedIndex(0);
            cboNam.setSelectedIndex(0);
            cboLop.setSelectedIndex(0);
            cboTrangThai.setSelectedIndex(0);
            tableModel.setRowCount(0);
            lblKetQua.setText("0 kết quả");
            lblHint.setText("Nhập từ khóa hoặc chọn bộ lọc rồi bấm Tìm kiếm");
            capNhatNut(false);
        });

        JPanel rightBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        rightBtns.setOpaque(false);
        rightBtns.add(btnXoa);
        rightBtns.add(btnTim);

        searchCard.add(txtTimKiem, BorderLayout.CENTER);
        searchCard.add(rightBtns, BorderLayout.EAST);

        JPanel searchWrap = new JPanel(new BorderLayout());
        searchWrap.setOpaque(false);
        searchWrap.setBorder(new EmptyBorder(12, 0, 0, 0));
        searchWrap.add(searchCard, BorderLayout.CENTER);
        wrap.add(searchWrap, BorderLayout.CENTER);

        return wrap;
    }

    private void datPlaceholder(JTextField f, String hint) {
        f.setForeground(UITheme.TEXT_MUTED);
        f.setText(hint);
        f.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (f.getText().equals(hint)) {
                    f.setText("");
                    f.setForeground(UITheme.TEXT_PRIMARY);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (f.getText().isBlank()) {
                    f.setForeground(UITheme.TEXT_MUTED);
                    f.setText(hint);
                }
            }
        });
    }

    private String layTuKhoa() {
        String t = txtTimKiem.getText();
        if (t == null) return "";
        t = t.trim();
        if (t.equals("Nhập mã SV, họ tên hoặc lớp…")) return "";
        return t;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.add(buildFilterBar(), BorderLayout.NORTH);
        body.add(buildResultCard(), BorderLayout.CENTER);
        return body;
    }

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        bar.setOpaque(false);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, UITheme.BORDER),
                new EmptyBorder(4, 0, 10, 0)));

        JLabel lbl = new JLabel("Bộ lọc");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(UITheme.PRIMARY_DARK);
        bar.add(lbl);

        cboKhoa = combo("Tất cả khoa");
        cboNam = combo("Tất cả năm");
        cboLop = combo("Tất cả lớp");
        cboTrangThai = combo("Tất cả trạng thái");
        cboTrangThai.addItem("Đang học");
        cboTrangThai.addItem("Đã nghỉ");

        bar.add(chip("Khoa", cboKhoa));
        bar.add(chip("Năm", cboNam));
        bar.add(chip("Lớp", cboLop));
        bar.add(chip("TT", cboTrangThai));

        cboKhoa.addActionListener(e -> capNhatNamLopTheoKhoa());
        cboNam.addActionListener(e -> capNhatLopTheoNam());

        return bar;
    }

    private JPanel chip(String label, JComponent field) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(true);
        p.setBackground(CHIP_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x93, 0xC5, 0xFD), 1, true),
                new EmptyBorder(4, 8, 4, 8)));
        JLabel l = new JLabel(label);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(UITheme.PRIMARY_DARK);
        p.add(l);
        p.add(field);
        return p;
    }

    private JComboBox<String> combo(String first) {
        JComboBox<String> c = new JComboBox<>();
        c.addItem(first);
        c.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        c.setPreferredSize(new Dimension(140, 28));
        c.setBackground(Color.WHITE);
        return c;
    }

    private JPanel buildResultCard() {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)));

        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        lblKetQua = new JLabel("0 kết quả");
        lblKetQua.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKetQua.setForeground(UITheme.PRIMARY_DARK);
        lblHint = new JLabel("Nhập từ khóa hoặc chọn bộ lọc rồi bấm Tìm kiếm");
        lblHint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblHint.setForeground(UITheme.TEXT_MUTED);
        JPanel headL = new JPanel();
        headL.setOpaque(false);
        headL.setLayout(new BoxLayout(headL, BoxLayout.Y_AXIS));
        headL.add(lblKetQua);
        headL.add(Box.createVerticalStrut(2));
        headL.add(lblHint);
        head.add(headL, BorderLayout.WEST);
        card.add(head, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Mã SV", "Họ tên", "Khoa", "Năm", "Lớp", "TC TL", "TC nợ", "SĐT", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        table.setRowHeight(32);
        table.getColumnModel().getColumn(8).setCellRenderer(trangThaiRenderer());
        table.setSelectionBackground(new Color(0xDB, 0xEA, 0xFE));
        table.setSelectionForeground(UITheme.PRIMARY_DARK);

        table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (e.getValueIsAdjusting()) return;
            capNhatNut(table.getSelectedRow() >= 0);
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.BG_CARD);
        card.add(scroll, BorderLayout.CENTER);

        JPanel foot = new JPanel(new BorderLayout());
        foot.setOpaque(false);
        foot.setBorder(new EmptyBorder(10, 0, 0, 0));

        JLabel tip = new JLabel("Chọn 1 dòng → xem chi tiết / mở hóa đơn / lập phiếu thu");
        tip.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tip.setForeground(UITheme.TEXT_MUTED);
        foot.add(tip, BorderLayout.WEST);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        acts.setOpaque(false);
        // 3 màu nổi: xanh / tím / xanh lá (kể cả disabled vẫn phân biệt)
        btnXemChiTiet = nutMau("Xem chi tiết", new Color(0x25, 0x63, 0xEB));
        btnXemHoaDon = nutMau("Xem hóa đơn", new Color(0x7C, 0x3A, 0xED));
        btnLapPhieuThu = nutMau("Lập phiếu thu", new Color(0x05, 0x96, 0x69));
        btnXemChiTiet.addActionListener(e -> xemChiTiet());
        btnXemHoaDon.addActionListener(e -> chuyenSang("hocphi"));
        btnLapPhieuThu.addActionListener(e -> chuyenSang("thanhtoan"));
        acts.add(btnXemChiTiet);
        acts.add(btnXemHoaDon);
        acts.add(btnLapPhieuThu);
        foot.add(acts, BorderLayout.EAST);
        card.add(foot, BorderLayout.SOUTH);

        // Luôn đậm màu — không disable theo selection
        btnXemChiTiet.setEnabled(true);
        btnXemHoaDon.setEnabled(true);
        btnLapPhieuThu.setEnabled(true);
        return card;
    }

    /** Giữ nút luôn enabled (màu đậm). Chỉ dùng để sync selection UI nếu cần. */
    private void capNhatNut(boolean en) {
        if (btnXemChiTiet != null) btnXemChiTiet.setEnabled(true);
        if (btnXemHoaDon != null) btnXemHoaDon.setEnabled(true);
        if (btnLapPhieuThu != null) btnLapPhieuThu.setEnabled(true);
    }

    private void napBoLocVaDuLieu() {
        SwingWorker<List<SinhVien>, Void> w = new SwingWorker<>() {
            @Override
            protected List<SinhVien> doInBackground() throws Exception {
                return sinhVienService.layTatCa();
            }

            @Override
            protected void done() {
                try {
                    danhSachGoc = get();
                    napComboKhoa();
                } catch (Exception ignored) {
                }
            }
        };
        w.execute();
    }

    private void napComboKhoa() {
        Set<String> khoa = new LinkedHashSet<>();
        for (SinhVien sv : danhSachGoc) {
            if (sv.getKhoa() != null && !sv.getKhoa().isBlank()) khoa.add(sv.getKhoa().trim());
        }
        cboKhoa.removeAllItems();
        cboKhoa.addItem("Tất cả khoa");
        for (String k : khoa) cboKhoa.addItem(k);
        cboNam.removeAllItems();
        cboNam.addItem("Tất cả năm");
        cboLop.removeAllItems();
        cboLop.addItem("Tất cả lớp");
    }

    private void capNhatNamLopTheoKhoa() {
        String khoa = selected(cboKhoa, "Tất cả khoa");
        Set<Integer> nam = new LinkedHashSet<>();
        for (SinhVien sv : danhSachGoc) {
            if (khoa != null && (sv.getKhoa() == null || !sv.getKhoa().trim().equalsIgnoreCase(khoa))) continue;
            nam.add(sv.getNamHoc());
        }
        cboNam.removeAllItems();
        cboNam.addItem("Tất cả năm");
        for (Integer n : nam) cboNam.addItem("Năm " + n);
        cboLop.removeAllItems();
        cboLop.addItem("Tất cả lớp");
    }

    private void capNhatLopTheoNam() {
        String khoa = selected(cboKhoa, "Tất cả khoa");
        String namStr = selected(cboNam, "Tất cả năm");
        Integer nam = parseNam(namStr);
        Set<String> lop = new LinkedHashSet<>();
        for (SinhVien sv : danhSachGoc) {
            if (khoa != null && (sv.getKhoa() == null || !sv.getKhoa().trim().equalsIgnoreCase(khoa))) continue;
            if (nam != null && sv.getNamHoc() != nam) continue;
            if (sv.getLop() != null && !sv.getLop().isBlank()) lop.add(sv.getLop().trim());
        }
        cboLop.removeAllItems();
        cboLop.addItem("Tất cả lớp");
        for (String l : lop) cboLop.addItem(l);
    }

    private Integer parseNam(String s) {
        if (s == null || s.startsWith("Tất cả")) return null;
        try {
            return Integer.parseInt(s.replace("Năm ", "").trim());
        } catch (Exception e) {
            return null;
        }
    }

    private String selected(JComboBox<String> c, String allLabel) {
        Object o = c.getSelectedItem();
        if (o == null) return null;
        String s = o.toString();
        if (s.equals(allLabel) || s.startsWith("Tất cả")) return null;
        return s;
    }

    private void thucHienTim() {
        String kw = layTuKhoa();
        String khoa = selected(cboKhoa, "Tất cả khoa");
        Integer nam = parseNam(selected(cboNam, "Tất cả năm"));
        String lop = selected(cboLop, "Tất cả lớp");
        String tt = selected(cboTrangThai, "Tất cả trạng thái");

        boolean coLoc = (kw != null && !kw.isEmpty()) || khoa != null || nam != null || lop != null || tt != null;
        if (!coLoc) {
            tableModel.setRowCount(0);
            lblKetQua.setText("0 kết quả");
            lblHint.setText("Nhập từ khóa hoặc chọn bộ lọc rồi bấm Tìm kiếm");
            capNhatNut(false);
            return;
        }

        SwingWorker<List<SinhVien>, Void> w = new SwingWorker<>() {
            @Override
            protected List<SinhVien> doInBackground() throws Exception {
                List<SinhVien> list;
                if (kw != null && !kw.isEmpty()) {
                    list = sinhVienService.timKiem(kw);
                } else {
                    list = danhSachGoc.isEmpty() ? sinhVienService.layTatCa() : new ArrayList<>(danhSachGoc);
                }
                return list.stream().filter(sv -> {
                    if (khoa != null && (sv.getKhoa() == null || !sv.getKhoa().trim().equalsIgnoreCase(khoa)))
                        return false;
                    if (nam != null && sv.getNamHoc() != nam) return false;
                    if (lop != null && (sv.getLop() == null || !sv.getLop().trim().equalsIgnoreCase(lop)))
                        return false;
                    if (tt != null) {
                        boolean dangHoc = "Đang học".equals(tt);
                        if (sv.isTrangThai() != dangHoc) return false;
                    }
                    return true;
                }).collect(Collectors.toList());
            }

            @Override
            protected void done() {
                try {
                    List<SinhVien> list = get();
                    tableModel.setRowCount(0);
                    for (SinhVien sv : list) {
                        tableModel.addRow(new Object[]{
                                sv.getMaSV(),
                                sv.getHoTen(),
                                nvl(sv.getKhoa()),
                                "Năm " + sv.getNamHoc(),
                                nvl(sv.getLop()),
                                sv.getTinChiTichLuy(),
                                sv.getTinChiNo(),
                                nvl(sv.getSoDienThoai()),
                                sv.isTrangThai() ? "Đang học" : "Đã nghỉ"
                        });
                    }
                    lblKetQua.setText(list.size() + " kết quả");
                    lblHint.setText(list.isEmpty()
                            ? "Không tìm thấy sinh viên phù hợp"
                            : "Chọn một dòng để xem chi tiết hoặc lập phiếu thu");
                    capNhatNut(false);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(TraCuuSinhVienPanel.this, rootMessage(ex));
                }
            }
        };
        w.execute();
    }

    private void chuyenSang(String key) {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.thongBao(this, "Vui lòng tìm và chọn một sinh viên trong bảng trước.");
            return;
        }
        String maSV = String.valueOf(tableModel.getValueAt(row, 0));
        if (dieuHuongTimKiem != null) {
            dieuHuongTimKiem.accept(key, maSV);
        } else {
            UIUtils.thongBao(this, "SV " + maSV + " — mở màn " + key);
        }
    }

    private void xemChiTiet() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.thongBao(this, "Vui lòng tìm và chọn một sinh viên trong bảng trước.");
            return;
        }
        String maSV = String.valueOf(tableModel.getValueAt(row, 0));
        SwingWorker<SinhVien, Void> w = new SwingWorker<>() {
            @Override
            protected SinhVien doInBackground() throws Exception {
                return sinhVienService.timTheoMa(maSV);
            }

            @Override
            protected void done() {
                try {
                    SinhVien sv = get();
                    if (sv == null) {
                        UIUtils.thongBaoLoi(TraCuuSinhVienPanel.this, "Không tìm thấy " + maSV);
                        return;
                    }
                    hienDialogChiTiet(sv);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(TraCuuSinhVienPanel.this, rootMessage(ex));
                }
            }
        };
        w.execute();
    }

    private void hienDialogChiTiet(SinhVien sv) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                "Chi tiết — " + sv.getMaSV(), true);
        dlg.setSize(420, 480);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UITheme.BG_MAIN);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(new EmptyBorder(16, 20, 16, 20));

        themDong(form, "Mã SV", sv.getMaSV());
        themDong(form, "Họ tên", sv.getHoTen());
        themDong(form, "Lớp", nvl(sv.getLop()));
        themDong(form, "Khoa", nvl(sv.getKhoa()));
        themDong(form, "Năm học", "Năm " + sv.getNamHoc());
        themDong(form, "TC tích lũy", String.valueOf(sv.getTinChiTichLuy()));
        themDong(form, "TC nợ", String.valueOf(sv.getTinChiNo()));
        themDong(form, "Email", nvl(sv.getEmail()));
        themDong(form, "SĐT", nvl(sv.getSoDienThoai()));
        themDong(form, "SĐT PH", nvl(sv.getSoDienThoaiPhuHuynh()));
        themDong(form, "Trạng thái", sv.isTrangThai() ? "Đang học" : "Đã nghỉ");

        JScrollPane sc = new JScrollPane(form);
        sc.setBorder(BorderFactory.createEmptyBorder());
        dlg.add(sc, BorderLayout.CENTER);

        JPanel acts = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acts.setOpaque(false);
        JButton dong = nutOutline("Đóng");
        dong.addActionListener(e -> dlg.dispose());
        acts.add(dong);
        dlg.add(acts, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }

    private void themDong(JPanel form, String nhan, String gt) {
        JLabel l = new JLabel(nhan);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(UITheme.TEXT_MUTED);
        l.setAlignmentX(LEFT_ALIGNMENT);
        JTextField f = UIUtils.textField(20);
        f.setText(gt);
        f.setEditable(false);
        f.setAlignmentX(LEFT_ALIGNMENT);
        f.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        form.add(l);
        form.add(Box.createVerticalStrut(2));
        form.add(f);
        form.add(Box.createVerticalStrut(10));
    }

    /** Nút đặc màu — disabled vẫn giữ tông màu (nhạt hơn), không đồng loạt xám. */
    private JButton nutMau(String text, Color mauNen) {
        final Color mau = mauNen != null ? mauNen : UITheme.PRIMARY;
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c;
                Color base = mau;
                c = (getModel().isRollover() || getModel().isPressed()) ? base.darker() : base;
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
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
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton nutTeal(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = isEnabled()
                        ? (getModel().isRollover() ? UITheme.PRIMARY_DARK : UITheme.PRIMARY)
                        : new Color(0x93, 0xA4, 0xC0);
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
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
        b.setBorder(new EmptyBorder(10, 18, 10, 18));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton nutOutline(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isEnabled() && getModel().isRollover()) {
                    g2.setColor(new Color(0xEF, 0xF6, 0xFF));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                } else {
                    g2.setColor(Color.WHITE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
                g2.setColor(isEnabled() ? UITheme.PRIMARY : new Color(0x94, 0xA3, 0xB8));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(new Font("Segoe UI", Font.BOLD, 12));
        b.setForeground(UITheme.PRIMARY_DARK);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(false);
        b.setBorder(new EmptyBorder(9, 16, 9, 16));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private DefaultTableCellRenderer trangThaiRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel,
                                                           boolean foc, int r, int c) {
                JLabel lb = (JLabel) super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                lb.setHorizontalAlignment(CENTER);
                lb.setOpaque(true);
                boolean ok = "Đang học".equals(v);
                if (!sel) {
                    lb.setBackground(ok ? new Color(0xD1, 0xFA, 0xE5) : new Color(0xFB, 0xEE, 0xF0));
                    lb.setForeground(ok ? UITheme.SUCCESS : UITheme.DANGER);
                }
                return lb;
            }
        };
    }

    private static String nvl(String s) {
        return s == null || s.isBlank() ? "—" : s;
    }

    private String rootMessage(Exception ex) {
        Throwable c = ex.getCause() != null ? ex.getCause() : ex;
        return c.getMessage() != null ? c.getMessage() : c.toString();
    }
}