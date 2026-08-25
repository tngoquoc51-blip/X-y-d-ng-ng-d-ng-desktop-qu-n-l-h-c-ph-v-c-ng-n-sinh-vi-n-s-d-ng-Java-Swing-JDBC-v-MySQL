package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.bus.SinhVienService;
import vn.edu.eaut.qlhocphi.config.UITheme;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * Man hinh quan ly sinh vien - ban sua loi + nang cap "desktop quan ly":
 * banner dong bo mau, toolbar GridBagLayout (khong bao gio wrap/chong de),
 * sua loi loc Lop/Khoa tra ve 0 ket qua do khong trim khoang trang, va them
 * tinh nang tu dong lam moi du lieu dinh ky (Swing Timer, khong can API ngoai
 * vi du an hien khong ket noi he thong nao khac de dong bo that su).
 */
public class SinhVienPanel extends JPanel {
    private static final String TAT_CA = "Tat ca";
    private static final int CHU_KY_TU_DONG_GIAY = 30;

    private final SinhVienService sinhVienService = new SinhVienService();

    private JTextField txtTimKiem;
    private JComboBox<String> cboLop;
    private JComboBox<String> cboKhoa;
    private JTable table;
    private DefaultTableModel tableModel;
    private JLabel lblSoLuong;
    private JToggleButton btnTuDong;
    private Timer timerTuDong;

    /** Danh sach day du (khong loc) - chi dung de do du lieu cho 2 combo Lop/Khoa. */
    private List<SinhVien> danhSachGoc = new ArrayList<>();

    private JLabel lblTongSo, lblDangHoc, lblDaNghi;
    private JLabel lblDaChon;

    public SinhVienPanel() {
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

        taiBoLoc();
        taiDuLieu(null);
    }

    /** Cho phep man hinh khac (VD: TraCuuFrame) kich hoat tim kiem tu ben ngoai. */
    public void timKiem(String keyword) {
        if (txtTimKiem != null) txtTimKiem.setText(keyword);
        taiDuLieu(keyword);
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
        JLabel tieuDe = new JLabel("Quan ly sinh vien");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Tim kiem, loc theo lop/khoa, nhap Excel va quan ly ho so sinh vien");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);

        JButton btnThem = new JButton("+ Them sinh vien");
        btnThem.setFont(UITheme.FONT_BOLD);
        btnThem.setBackground(Color.WHITE);
        btnThem.setForeground(UITheme.PRIMARY_DARK);
        btnThem.setFocusPainted(false);
        btnThem.setBorderPainted(false);
        btnThem.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnThem.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnThem.addActionListener(e -> moFormThem());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
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

    // ================== THONG KE NHANH ==================

    private JPanel buildStatsRow() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);

        JPanel theTongSo = UITheme.statCard("Tong so", "0", UITheme.TINT_BLUE, UITheme.TEXT_BLUE);
        JPanel theDangHoc = UITheme.statCard("Dang hoc", "0", UITheme.TINT_GREEN, UITheme.TEXT_GREEN);
        JPanel theDaNghi = UITheme.statCard("Da nghi hoc", "0", UITheme.TINT_RED, UITheme.TEXT_RED);

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

    // ================== TOOLBAR: GridBagLayout (khong bao gio wrap/chong de) ==================

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new GridBagLayout());
        toolbar.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 8);
        int col = 0;

        gbc.gridx = col++;
        toolbar.add(nhanNho("Lop:"), gbc);

        cboLop = new JComboBox<>(new String[]{TAT_CA});
        styleCombo(cboLop, 120);
        cboLop.addActionListener(e -> taiDuLieu(txtTimKiem.getText()));
        gbc.gridx = col++;
        toolbar.add(cboLop, gbc);

        gbc.gridx = col++;
        toolbar.add(nhanNho("Khoa:"), gbc);

        cboKhoa = new JComboBox<>(new String[]{TAT_CA});
        styleCombo(cboKhoa, 170);
        cboKhoa.addActionListener(e -> taiDuLieu(txtTimKiem.getText()));
        gbc.gridx = col++;
        toolbar.add(cboKhoa, gbc);

        txtTimKiem = UIUtils.textField(15);
        txtTimKiem.setToolTipText("Tim theo ma SV, ho ten, lop");
        txtTimKiem.addActionListener(e -> taiDuLieu(txtTimKiem.getText()));
        gbc.gridx = col++;
        toolbar.add(txtTimKiem, gbc);

        // O trong gian no - day nut hanh dong ve sat le phai, khong bao gio wrap xuong dong
        gbc.gridx = col++;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        toolbar.add(Box.createHorizontalGlue(), gbc);
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;

        JButton btnTim = UITheme.secondaryButton("Tim kiem");
        btnTim.addActionListener(e -> taiDuLieu(txtTimKiem.getText()));
        gbc.gridx = col++;
        toolbar.add(btnTim, gbc);

        btnTuDong = new JToggleButton("Tu dong lam moi (30s)");
        btnTuDong.setFont(UITheme.FONT_BASE);
        btnTuDong.addActionListener(e -> chuyenDoiTuDongLamMoi());
        gbc.gridx = col++;
        toolbar.add(btnTuDong, gbc);

        JButton btnNhapExcel = UITheme.secondaryButton("Nhap Excel");
        btnNhapExcel.addActionListener(e -> moNhapExcel());
        gbc.gridx = col++;
        gbc.insets = new Insets(0, 0, 0, 0);
        toolbar.add(btnNhapExcel, gbc);

        return toolbar;
    }

    private JLabel nhanNho(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(UITheme.TEXT_MUTED);
        return l;
    }

    private void styleCombo(JComboBox<String> combo, int width) {
        combo.setPreferredSize(new Dimension(width, 32));
        combo.setFont(UITheme.FONT_BASE);
        combo.setBackground(Color.WHITE);
        combo.setFocusable(false);
    }

    /** Bat/tat bo dem tu dong tai lai danh sach moi 30 giay - tinh nang "tu dong hoa" trong pham vi ung dung. */
    private void chuyenDoiTuDongLamMoi() {
        if (btnTuDong.isSelected()) {
            timerTuDong = new Timer(CHU_KY_TU_DONG_GIAY * 1000, e -> {
                taiBoLoc();
                taiDuLieu(txtTimKiem.getText());
            });
            timerTuDong.start();
            btnTuDong.setText("Dang tu dong (30s)...");
        } else {
            if (timerTuDong != null) timerTuDong.stop();
            btnTuDong.setText("Tu dong lam moi (30s)");
        }
    }

    // ================== BANG DU LIEU ==================

    private JPanel buildTableCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        tableModel = new DefaultTableModel(
                new Object[]{"Ma SV", "Ho ten", "Lop", "Khoa", "Email", "SDT", "Trang thai"}, 0) {
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

        lblSoLuong = new JLabel("Hien thi 0 sinh vien");
        lblSoLuong.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSoLuong.setForeground(UITheme.TEXT_MUTED);

        lblDaChon = new JLabel("Chua chon sinh vien nao");
        lblDaChon.setFont(UITheme.FONT_BASE);
        lblDaChon.setForeground(UITheme.TEXT_MUTED);
        lblDaChon.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 0));

        footerTrai.add(lblSoLuong);
        footerTrai.add(lblDaChon);
        footer.add(footerTrai, BorderLayout.WEST);

        table.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (e.getValueIsAdjusting()) return;
            int row = table.getSelectedRow();
            lblDaChon.setText(row < 0 ? "Chua chon sinh vien nao"
                    : "Da chon: " + tableModel.getValueAt(row, 0) + " - " + tableModel.getValueAt(row, 1));
        });

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        toolbar.setOpaque(false);
        JButton btnSua = UITheme.secondaryButton("Sua");
        JButton btnXoa = UITheme.dangerButton("Xoa");
        btnSua.addActionListener(e -> moFormSua());
        btnXoa.addActionListener(e -> xoaSinhVienDangChon());
        toolbar.add(btnSua);
        toolbar.add(btnXoa);
        footer.add(toolbar, BorderLayout.EAST);

        card.add(footer, BorderLayout.SOUTH);

        return card;
    }

    /** To mau nhan cho cot Trang thai: xanh = Dang hoc, do nhat = Da nghi. */
    private DefaultTableCellRenderer trangThaiRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                JLabel label = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setOpaque(true);
                boolean dangHoc = "Dang hoc".equals(value);
                if (!isSelected) {
                    label.setBackground(dangHoc ? new Color(0xE3, 0xF6, 0xEA) : new Color(0xF3, 0xE9, 0xEA));
                    label.setForeground(dangHoc ? UITheme.SUCCESS : UITheme.DANGER);
                }
                return label;
            }
        };
    }

    // ================== NAP COMBO LOP/KHOA ==================

    /** Tai toan bo sinh vien (khong loc) de do du lieu 2 combo Lop / Khoa. */
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
                    capNhatLuaChonCombo(cboLop, layGiaTriDuyNhat(SinhVien::getLop));
                    capNhatLuaChonCombo(cboKhoa, layGiaTriDuyNhat(SinhVien::getKhoa));
                } catch (Exception ignored) {
                    // Neu loi tai combo, o tim kiem chinh van dung binh thuong
                }
            }
        };
        worker.execute();
    }

    private List<String> layGiaTriDuyNhat(Function<SinhVien, String> lay) {
        Set<String> set = new LinkedHashSet<>();
        for (SinhVien sv : danhSachGoc) {
            String gt = lay.apply(sv);
            if (gt != null && !gt.isBlank()) set.add(gt.trim());
        }
        List<String> ds = new ArrayList<>(set);
        ds.sort(String::compareToIgnoreCase);
        return ds;
    }

    private void capNhatLuaChonCombo(JComboBox<String> combo, List<String> giaTriMoi) {
        Object dangChon = combo.getSelectedItem();
        combo.removeAllItems();
        combo.addItem(TAT_CA);
        for (String gt : giaTriMoi) combo.addItem(gt);
        combo.setSelectedItem(giaTriMoi.contains(dangChon) ? dangChon : TAT_CA);
    }

    // ================== TAI DU LIEU + LOC ==================

    private void taiDuLieu(String keyword) {
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
                                sv.isTrangThai() ? "Dang hoc" : "Da nghi"
                        });
                        if (sv.isTrangThai()) dangHoc++;
                    }
                    if (lblTongSo != null) {
                        lblTongSo.setText(String.valueOf(list.size()));
                        lblDangHoc.setText(String.valueOf(dangHoc));
                        lblDaNghi.setText(String.valueOf(list.size() - dangHoc));
                    }
                    lblSoLuong.setText("Hien thi " + list.size() + " sinh vien");
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(SinhVienPanel.this, "Khong the tai danh sach sinh vien.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    /**
     * Loc them theo Lop / Khoa dang chon tren 2 combo (loc phia giao dien, khong doi CSDL).
     * SUA LOI: truoc day so sanh truc tiep sv.getLop()/sv.getKhoa() khong trim, trong khi gia
     * tri trong combo da duoc trim khi do du lieu (xem layGiaTriDuyNhat) - neu CSDL co khoang
     * trang thua o dau/cuoi, 2 chuoi nhin giong het nhau nhung equalsIgnoreCase() luon tra ve
     * false, khien loc luon ra 0 dong. Them .trim() vao ca 2 ve de so sanh chinh xac.
     */
    private List<SinhVien> apDungBoLoc(List<SinhVien> list) {
        String lop = cboLop == null ? TAT_CA : (String) cboLop.getSelectedItem();
        String khoa = cboKhoa == null ? TAT_CA : (String) cboKhoa.getSelectedItem();
        List<SinhVien> ket = new ArrayList<>();
        for (SinhVien sv : list) {
            String lopSV = sv.getLop() == null ? "" : sv.getLop().trim();
            String khoaSV = sv.getKhoa() == null ? "" : sv.getKhoa().trim();
            boolean khopLop = TAT_CA.equals(lop) || lop.trim().equalsIgnoreCase(lopSV);
            boolean khopKhoa = TAT_CA.equals(khoa) || khoa.trim().equalsIgnoreCase(khoaSV);
            if (khopLop && khopKhoa) ket.add(sv);
        }
        return ket;
    }

    // ================== THEM / SUA / XOA ==================

    private void moFormThem() {
        SinhVienFormDialog dialog = new SinhVienFormDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), null,
                sv -> {
                    try {
                        sinhVienService.them(sv);
                        taiBoLoc();
                        taiDuLieu(null);
                        UIUtils.thongBao(this, "Da them sinh vien " + sv.getMaSV());
                    } catch (Exception ex) {
                        UIUtils.thongBaoLoi(this, rootMessage(ex));
                    }
                });
        dialog.setVisible(true);
    }

    private void moFormSua() {
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.thongBaoLoi(this, "Vui long chon 1 sinh vien trong bang de sua");
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
                                    UIUtils.thongBao(SinhVienPanel.this, "Da cap nhat sinh vien " + updated.getMaSV());
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
        int row = table.getSelectedRow();
        if (row < 0) {
            UIUtils.thongBaoLoi(this, "Vui long chon 1 sinh vien trong bang de xoa");
            return;
        }
        String maSV = (String) tableModel.getValueAt(row, 0);
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Xoa sinh vien " + maSV + "? Thao tac nay cung xoa cac hoa don lien quan.",
                "Xac nhan xoa", JOptionPane.YES_NO_OPTION);
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
                    UIUtils.thongBao(SinhVienPanel.this, "Da xoa sinh vien " + maSV);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(SinhVienPanel.this, rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    // ================== NHAP EXCEL ==================

    private void moNhapExcel() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Chon file Excel danh sach sinh vien (.xlsx)");
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
                            "Da nhap " + tongKet[0] + " sinh vien.\nBo qua " + tongKet[1] + " dong loi du lieu.");
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(SinhVienPanel.this, "Nhap Excel that bai.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    /**
     * Doc file Excel theo thu tu cot: MaSV, HoTen, Lop, Khoa, Email, SDT (dong 1 la tieu de, bo qua).
     * Sinh vien da ton tai (trung MaSV) se duoc cap nhat lai thong tin thay vi bao loi.
     * Cac truong Lop/Khoa duoc trim ngay khi nhap de tranh lap lai loi khoang trang thua da sua o tren.
     * @return mang 2 phan tu: [0] = so dong nhap thanh cong, [1] = so dong bi bo qua do loi
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

    private String rootMessage(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }
}