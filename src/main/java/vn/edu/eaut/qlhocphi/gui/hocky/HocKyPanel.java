package vn.edu.eaut.qlhocphi.gui.hocky;

import vn.edu.eaut.qlhocphi.bus.HocPhiService;
import vn.edu.eaut.qlhocphi.config.UITheme;
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
 * Man hinh quan ly hoc ky va don gia tin chi - ban "desktop quan ly":
 * co banner + KPI so lieu that + cot Trang thai tu dong tinh (Dang ap dung /
 * Sap dien ra / Da ket thuc / Thieu du lieu) de canh bao du lieu chua day du.
 */
public class HocKyPanel extends JPanel {
    private final HocPhiService hocPhiService = new HocPhiService();

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

    /** Danh sach hoc ky dang tai gan nhat - dung de tinh trang thai + KPI (khong the lay du tu bang hien thi). */
    private List<HocKy> danhSachHienTai = new ArrayList<>();

    /** null = dang them moi, khac null = dang sua hoc ky co Ma = gia tri nay */
    private Integer dangSuaMaHocKy = null;

    public HocKyPanel() {
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
        JLabel tieuDe = new JLabel("Hoc ky & muc hoc phi");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        lblSoLuong = new JLabel("Dang tai...");
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

    // ================= KPI SO LIEU THAT =================

    private JPanel buildKpiRow() {
        JPanel row = new JPanel(new GridLayout(1, 4, 16, 0));
        row.setOpaque(false);
        row.add(UITheme.statCard("Tong so hoc ky", "0", UITheme.TINT_BLUE, UITheme.TEXT_BLUE));
        row.add(UITheme.statCard("Dang ap dung", "0", UITheme.TINT_GREEN, UITheme.TEXT_GREEN));
        row.add(UITheme.statCard("Don gia TB / tin chi", "0 d", UITheme.TINT_VIOLET, UITheme.TEXT_VIOLET));
        row.add(UITheme.statCard("Thieu ngay cau hinh", "0", UITheme.TINT_RED, UITheme.TEXT_RED));
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
            if (trangThai.equals("Dang ap dung")) dangApDung++;
            if (trangThai.equals("Thieu du lieu")) thieuDuLieu++;
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

    /** Tinh trang thai 1 hoc ky dua tren ngay bat dau/ket thuc so voi ngay hom nay. */
    private String tinhTrangThai(HocKy hk, LocalDate homNay) {
        if (hk.getNgayBatDau() == null || hk.getNgayKetThuc() == null) return "Thieu du lieu";
        if (homNay.isBefore(hk.getNgayBatDau())) return "Sap dien ra";
        if (homNay.isAfter(hk.getNgayKetThuc())) return "Da ket thuc";
        return "Dang ap dung";
    }

    // ================= FORM (them / sua) =================
    private JPanel buildFormCard() {
        formCard = UITheme.card();
        formCard.setPreferredSize(new Dimension(300, 0));
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, UITheme.ACCENT_TEAL),
                        BorderFactory.createLineBorder(UITheme.BORDER, 1, true)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        formCard.setLayout(new BoxLayout(formCard, BoxLayout.Y_AXIS));

        JLabel lblChonNhanh = UIUtils.formLabel("Loai / Chon nhanh:");
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

        JLabel lblGhiChuLoai = new JLabel("<html>3 muc dau la tao HOC KY MOI theo loai. "
                + "Cac muc con lai la sua hoc ky da co san.</html>");
        lblGhiChuLoai.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblGhiChuLoai.setForeground(UITheme.TEXT_MUTED);
        lblGhiChuLoai.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(lblGhiChuLoai);
        formCard.add(Box.createRigidArea(new Dimension(0, 6)));
        formCard.add(new JSeparator());
        formCard.add(Box.createRigidArea(new Dimension(0, 14)));

        lblTieuDeForm = UITheme.sectionLabel("Them hoc ky moi");
        formCard.add(lblTieuDeForm);
        formCard.add(Box.createRigidArea(new Dimension(0, 14)));

        txtTen = UIUtils.textField(16);
        txtNamHoc = UIUtils.textField(16);
        txtDonGia = UIUtils.textField(16);
        txtNgayBatDau = UIUtils.textField(16);
        txtNgayKetThuc = UIUtils.textField(16);

        themDong(formCard, "Ten hoc ky (VD: Hoc ky 1) *", txtTen);
        themDong(formCard, "Nam hoc (VD: 2025-2026) *", txtNamHoc);
        themDong(formCard, "Don gia / tin chi (VND) *", txtDonGia);
        themDong(formCard, "Ngay bat dau (dd/MM/yyyy)", txtNgayBatDau);
        themDong(formCard, "Ngay ket thuc (dd/MM/yyyy)", txtNgayKetThuc);

        JLabel lblGoiY = new JLabel("* Neu bo trong ngay, he thong se khong tinh duoc trang thai ap dung.");
        lblGoiY.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblGoiY.setForeground(UITheme.TEXT_MUTED);
        lblGoiY.setAlignmentX(Component.LEFT_ALIGNMENT);
        formCard.add(lblGoiY);
        formCard.add(Box.createRigidArea(new Dimension(0, 14)));

        btnLuu = UITheme.primaryButton("+ Them hoc ky");
        btnLuu.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLuu.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnLuu.addActionListener(e -> luuHocKy());

        btnHuy = UITheme.secondaryButton("Huy / Lam moi");
        btnHuy.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnHuy.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnHuy.addActionListener(e -> lamMoiForm());

        formCard.add(btnLuu);
        formCard.add(Box.createRigidArea(new Dimension(0, 8)));
        formCard.add(btnHuy);

        return formCard;
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

    /** Doi mau vach + tieu de form theo trang thai them-moi/dang-sua, de nguoi dung nhan biet ro dang lam gi. */
    private void capNhatGiaoDienForm(boolean dangSua, String tenHocKyDangSua) {
        Color mauVach = dangSua ? UITheme.PRIMARY : UITheme.ACCENT_TEAL;
        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, mauVach),
                        BorderFactory.createLineBorder(UITheme.BORDER, 1, true)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        lblTieuDeForm.setText(dangSua ? "Dang sua: " + tenHocKyDangSua : "Them hoc ky moi");
        formCard.revalidate();
        formCard.repaint();
    }


    /** 3 muc dau tien cua combo: tao hoc ky moi theo tung loai, moi loai co mau/ten goi y rieng. */
    private void apDungLoaiTaoMoi(String nhanLoai, Color mauVach, String tenGoiY) {
        dangSuaMaHocKy = null;
        txtTen.setText(tenGoiY);
        txtNamHoc.setText("");
        txtDonGia.setText("");
        txtNgayBatDau.setText("");
        txtNgayKetThuc.setText("");
        btnLuu.setText("+ Them " + nhanLoai.toLowerCase());
        btnXoa.setEnabled(false);
        table.clearSelection();

        formCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 4, 0, 0, mauVach),
                        BorderFactory.createLineBorder(UITheme.BORDER, 1, true)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)));
        lblTieuDeForm.setText("Them " + nhanLoai + " moi");
        formCard.revalidate();
        formCard.repaint();
    }

    /** Do 1 hoc ky da co vao form de sua - dung chung cho ca "bam dong bang" va "chon combo". */
    private void apDuLieuVaoForm(HocKy hk) {
        dangSuaMaHocKy = hk.getMaHocKy();
        txtTen.setText(hk.getTenHocKy());
        txtNamHoc.setText(hk.getNamHoc());
        txtDonGia.setText(hk.getDonGiaTinChi() == null ? "" : hk.getDonGiaTinChi().toBigInteger().toString());
        txtNgayBatDau.setText(DateUtils.format(hk.getNgayBatDau()));
        txtNgayKetThuc.setText(DateUtils.format(hk.getNgayKetThuc()));
        btnLuu.setText("Cap nhat hoc ky");
        btnXoa.setEnabled(true);
        capNhatGiaoDienForm(true, hk.getTenHocKy());
    }

    /** Xu ly khi Admin chon 1 muc trong combo "Loai / Chon nhanh". */
    private void onChonComboHocKy() {
        if (dangDongBoChonHocKy) return;
        int idx = cboChonHocKy.getSelectedIndex();
        int namNay = LocalDate.now().getYear();

        if (idx == 0) {
            apDungLoaiTaoMoi("Hoc ky chinh", UITheme.ACCENT_TEAL, "");
        } else if (idx == 1) {
            apDungLoaiTaoMoi("Hoc ky He", UITheme.WARNING, "Hoc ky He " + namNay);
        } else if (idx == 2) {
            apDungLoaiTaoMoi("Hoc ky hoc lai", UITheme.DANGER, "Hoc ky hoc lai " + namNay);
        } else if (idx >= 3) {
            HocKy hk = danhSachHienTai.get(idx - 3);
            apDuLieuVaoForm(hk);
            moDangKyChoHocKy(hk);
        }
    }

    /** Nap lai danh sach cho combo: 3 muc loai co dinh + toan bo hoc ky da co. */
    private void capNhatComboChonHocKy() {
        dangDongBoChonHocKy = true;
        int viTriDangChon = cboChonHocKy.getItemCount() > 0 ? cboChonHocKy.getSelectedIndex() : 0;
        cboChonHocKy.removeAllItems();
        cboChonHocKy.addItem("+ Tao Hoc ky chinh moi");
        cboChonHocKy.addItem("+ Tao Hoc ky He moi");
        cboChonHocKy.addItem("+ Tao Hoc ky hoc lai moi");
        for (HocKy hk : danhSachHienTai) {
            cboChonHocKy.addItem(hk.getTenHocKy() + " - " + hk.getNamHoc());
        }
        cboChonHocKy.setSelectedIndex(Math.max(0, Math.min(viTriDangChon, cboChonHocKy.getItemCount() - 1)));
        dangDongBoChonHocKy = false;
    }

    /** Dong bo combo ve dung hoc ky vua bam trong bang, khong kich hoat lai su kien onChonComboHocKy. */
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

    // ================= BANG + TIM KIEM =================
    private JPanel buildTableCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));

        JPanel toolbar = new JPanel(new BorderLayout(10, 0));
        toolbar.setOpaque(false);
        txtTimKiem = UIUtils.textField(18);
        toolbar.add(UIUtils.formLabel("Tim kiem: "), BorderLayout.WEST);
        toolbar.add(txtTimKiem, BorderLayout.CENTER);

        btnXoa = UITheme.dangerButton("Xoa hoc ky da chon");
        btnXoa.setEnabled(false);
        btnXoa.addActionListener(e -> xoaHocKy());
        toolbar.add(btnXoa, BorderLayout.EAST);
        card.add(toolbar, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Ma HK", "Ten hoc ky", "Nam hoc", "Don gia/tin chi",
                        "Ngay bat dau", "Ngay ket thuc", "Trang thai"}, 0) {
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

    /** Renderer ve trang thai dang "the mau" (pill), dong bo voi cach lam o HoaDonPanel/CongNoPanel. */
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
                case "Dang ap dung": bg = UITheme.TINT_GREEN; fg = UITheme.TEXT_GREEN; break;
                case "Sap dien ra":  bg = UITheme.TINT_BLUE;  fg = UITheme.TEXT_BLUE;  break;
                case "Thieu du lieu": bg = UITheme.TINT_RED;  fg = UITheme.TEXT_RED;   break;
                default:             bg = new Color(0xEC, 0xEE, 0xF2); fg = UITheme.TEXT_MUTED; // Da ket thuc
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

    // ================= CHON DONG DE SUA =================
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
        apDungLoaiTaoMoi("Hoc ky chinh", UITheme.ACCENT_TEAL, "");
        table.clearSelection();
    }

    // ================= TAI DU LIEU =================
    private void taiDuLieu() {
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
                    lblSoLuong.setText(list.size() + " hoc ky da tao");
                    capNhatKpi();
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(HocKyPanel.this, "Khong the tai danh sach hoc ky.");
                }
            }
        };
        worker.execute();
    }

    // ================= THEM / CAP NHAT =================
    private void luuHocKy() {
        String ten = txtTen.getText().trim();
        String namHoc = txtNamHoc.getText().trim();
        String donGiaText = txtDonGia.getText().trim().replaceAll("[^0-9]", "");

        if (ten.isEmpty() || namHoc.isEmpty() || donGiaText.isEmpty()) {
            UIUtils.thongBaoLoi(this, "Vui long nhap day du thong tin bat buoc (danh dau *)");
            return;
        }
        BigDecimal donGia;
        try {
            donGia = new BigDecimal(donGiaText);
        } catch (NumberFormatException ex) {
            UIUtils.thongBaoLoi(this, "Don gia phai la so hop le");
            return;
        }

        LocalDate ngayBatDau = DateUtils.parseDate(txtNgayBatDau.getText().trim());
        LocalDate ngayKetThuc = DateUtils.parseDate(txtNgayKetThuc.getText().trim());
        if (!txtNgayBatDau.getText().trim().isEmpty() && ngayBatDau == null) {
            UIUtils.thongBaoLoi(this, "Ngay bat dau sai dinh dang, dung dd/MM/yyyy");
            return;
        }
        if (!txtNgayKetThuc.getText().trim().isEmpty() && ngayKetThuc == null) {
            UIUtils.thongBaoLoi(this, "Ngay ket thuc sai dinh dang, dung dd/MM/yyyy");
            return;
        }
        if (ngayBatDau != null && ngayKetThuc != null && ngayKetThuc.isBefore(ngayBatDau)) {
            UIUtils.thongBaoLoi(this, "Ngay ket thuc phai sau ngay bat dau");
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
                    taiDuLieu();
                    UIUtils.thongBao(HocKyPanel.this, dangSua ? "Da cap nhat hoc ky" : "Da them hoc ky moi");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(HocKyPanel.this, cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ================= XOA =================
    private void xoaHocKy() {
        if (dangSuaMaHocKy == null) return;
        int xacNhan = JOptionPane.showConfirmDialog(this,
                "Ban co chac muon xoa hoc ky nay?", "Xac nhan xoa",
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
                    UIUtils.thongBao(HocKyPanel.this, "Da xoa hoc ky");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(HocKyPanel.this, cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ================= DANG KY HOC PHI THEO TUNG LOAI HOC KY =================

    /** 3 loai hoc ky, moi loai co mau/icon/tieu de rieng cho dialog dang ky. */
    private enum LoaiHocKy {
        HOC_LAI("Dang ky HOC LAI", "\u26A0", UITheme.DANGER, UITheme.TINT_RED,
                "Sinh vien dang ky hoc lai se ap dung don gia rieng cua hoc ky nay. Vui long kiem tra ky truoc khi sinh hoa don."),
        HE("Dang ky HOC KY HE", "\u2600", UITheme.WARNING, new Color(0xFD, 0xF3, 0xDA),
                "Hoc ky He thuong co so tin chi it hon hoc ky chinh. Kiem tra dung so tin chi sinh vien dang ky."),
        CHINH("Dang ky hoc phi", "\uD83C\uDF93", UITheme.ACCENT_TEAL, UITheme.TINT_GREEN,
                "Dang ky hoc phi theo hoc ky chinh khoa cho sinh vien.");

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

    /** Bo dau tieng Viet de nhan dien loai hoc ky khong phu thuoc viet co dau hay khong. */
    private String boDauTiengViet(String s) {
        if (s == null) return "";
        String norm = Normalizer.normalize(s, Normalizer.Form.NFD);
        return norm.replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replace('đ', 'd').replace('Đ', 'D').toLowerCase();
    }

    /** Nhan dien loai hoc ky tu ten (khong sua CSDL, chi doc chuoi ten de phan loai hien thi). */
    private LoaiHocKy xacDinhLoai(HocKy hk) {
        String ten = boDauTiengViet(hk.getTenHocKy());
        if (ten.contains("hoc lai")) return LoaiHocKy.HOC_LAI;
        if (ten.contains("he")) return LoaiHocKy.HE;
        return LoaiHocKy.CHINH;
    }

    /** Mo dialog "Dang ky hoc phi" rieng cho hoc ky vua chon - giao dien doi mau/icon/tieu de theo loai. */
    private void moDangKyChoHocKy(HocKy hk) {
        LoaiHocKy loai = xacDinhLoai(hk);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), loai.tieuDe, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setSize(440, 460);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(UITheme.BG_MAIN);
        dialog.setLayout(new BorderLayout());

        // Banner mau rieng theo loai
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
        form.add(Box.createRigidArea(new Dimension(0, 16)));

        JTextField txtMaSV = UIUtils.textField(18);
        JTextField txtSoTinChi = UIUtils.textField(18);
        JTextField txtHanThanhToan = UIUtils.textField(18);
        txtHanThanhToan.setToolTipText("Dinh dang: yyyy-MM-dd, VD 2026-01-15");

        themDong(form, "Ma sinh vien can dang ky *", txtMaSV);
        themDong(form, "So tin chi dang ky *", txtSoTinChi);
        themDong(form, "Han thanh toan (yyyy-MM-dd)", txtHanThanhToan);

        JLabel lblMaHK = new JLabel("Ma hoc ky: #" + hk.getMaHocKy() + "  (tu dong dien, khong can nhap)");
        lblMaHK.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblMaHK.setForeground(UITheme.TEXT_MUTED);
        lblMaHK.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblMaHK);

        dialog.add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setOpaque(false);
        JButton btnHuy = UITheme.secondaryButton("Dong");
        JButton btnDangKy = new JButton("Dang ky ngay");
        btnDangKy.setFont(UITheme.FONT_BOLD);
        btnDangKy.setBackground(loai.mauChinh);
        btnDangKy.setForeground(Color.WHITE);
        btnDangKy.setFocusPainted(false);
        btnDangKy.setBorderPainted(false);
        btnDangKy.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        btnDangKy.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnHuy.addActionListener(e -> dialog.dispose());
        btnDangKy.addActionListener(e -> {
            String maSV = txtMaSV.getText().trim();
            String soTinChiText = txtSoTinChi.getText().trim();
            if (maSV.isEmpty() || soTinChiText.isEmpty()) {
                UIUtils.thongBaoLoi(dialog, "Vui long nhap day du Ma sinh vien va So tin chi");
                return;
            }
            int soTinChi;
            LocalDate han;
            try {
                soTinChi = Integer.parseInt(soTinChiText);
                han = txtHanThanhToan.getText().trim().isEmpty() ? null : LocalDate.parse(txtHanThanhToan.getText().trim());
            } catch (Exception ex) {
                UIUtils.thongBaoLoi(dialog, "So tin chi phai la so nguyen, han thanh toan dung dinh dang yyyy-MM-dd");
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
                                "Da dang ky " + loai.tieuDe.toLowerCase() + " cho sinh vien " + maSV
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
        });

        actions.add(btnHuy);
        actions.add(btnDangKy);
        dialog.add(actions, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
}