package vn.edu.eaut.qlhocphi.gui.congno;

import vn.edu.eaut.qlhocphi.bus.CongNoService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.dal.SinhVienDAO;
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

/**
 * Man hinh tra cuu cong no - ban "desktop quan ly" day du: banner, KPI, tim
 * kiem/loc trang thai, xem chi tiet (double-click), gui email nhac no ngay tai
 * cho, xuat Excel/PDF. Dong bo giao dien voi HoaDonPanel/HocKyPanel da lam.
 */
public class CongNoPanel extends JPanel {
    private final CongNoService congNoService = new CongNoService();
    private final SinhVienDAO sinhVienDAO = new SinhVienDAO();
    private final java.util.function.BiConsumer<String, String> dieuHuongTimKiem;

    private JTable table;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JLabel lblTongNo, lblTongThu, lblSoHoaDonNo;
    private JTextField txtTimKiem;
    private JComboBox<String> cboTrangThai;
    private JLabel lblSoLuong;

    /** Danh sach hoa don con no day du dang tai gan nhat - dung cho chi tiet/nhac no theo dong. */
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
        JLabel tieuDe = new JLabel("Cong no hoc phi");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Tra cuu, canh bao va nhac no sinh vien con no hoc phi");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);

        JButton btnLamMoi = new JButton("Lam moi");
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

    // ================== KPI ==================

    private JPanel buildThongKeCards() {
        JPanel row = new JPanel(new GridLayout(1, 3, 16, 0));
        row.setOpaque(false);

        lblSoHoaDonNo = new JLabel("0");
        lblTongNo = new JLabel("0 d");
        lblTongThu = new JLabel("0 d");

        row.add(thongKeCard("So hoa don con no", lblSoHoaDonNo, UITheme.WARNING));
        row.add(thongKeCard("Tong cong no toan truong", lblTongNo, UITheme.DANGER));
        row.add(thongKeCard("Tong da thu", lblTongThu, UITheme.SUCCESS));
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

    // ================== TOOLBAR: TIM KIEM + LOC + XUAT + NHAC NO (GridBagLayout, khong wrap) ==================

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new GridBagLayout());
        toolbar.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 0, 8);
        int col = 0;

        gbc.gridx = col++;
        toolbar.add(UIUtils.formLabel("Tim kiem:"), gbc);

        txtTimKiem = UIUtils.textField(15);
        txtTimKiem.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { apDungBoLoc(); }
            @Override public void removeUpdate(DocumentEvent e) { apDungBoLoc(); }
            @Override public void changedUpdate(DocumentEvent e) { apDungBoLoc(); }
        });
        gbc.gridx = col++;
        toolbar.add(txtTimKiem, gbc);

        gbc.gridx = col++;
        toolbar.add(UIUtils.formLabel("Trang thai:"), gbc);

        cboTrangThai = new JComboBox<>(new String[]{"Tat ca trang thai", "Dong mot phan", "Qua han"});
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

        JButton btnNhacNo = UITheme.secondaryButton("Gui nhac no");
        btnNhacNo.addActionListener(e -> guiNhacNoDaChon());
        gbc.gridx = col++;
        toolbar.add(btnNhacNo, gbc);

        JButton btnXuatExcel = UITheme.secondaryButton("Xuat Excel");
        btnXuatExcel.addActionListener(e -> xuatExcel());
        gbc.gridx = col++;
        toolbar.add(btnXuatExcel, gbc);

        JButton btnXuatPDF = UITheme.secondaryButton("Xuat PDF");
        btnXuatPDF.addActionListener(e -> xuatPDF());
        gbc.gridx = col++;
        gbc.insets = new Insets(0, 0, 0, 0);
        toolbar.add(btnXuatPDF, gbc);

        return toolbar;
    }

    // ================== BANG DU LIEU ==================

    private JPanel buildTableCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 10));
        card.add(UITheme.sectionLabel("Danh sach sinh vien con no"), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(new Object[]{
                "Ma HD", "Ma SV", "Ho ten", "Hoc ky", "Con no", "Trang thai"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setToolTipText("Nhap doi (double-click) 1 dong de xem chi tiet cong no");
        table.putClientProperty("phimTat", "doubleclick=chi tiet, alt+click=xem sinh vien");

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

        lblSoLuong = new JLabel("Hien thi 0 / 0 hoa don");
        lblSoLuong.setFont(UITheme.FONT_BASE);
        lblSoLuong.setForeground(UITheme.TEXT_MUTED);
        card.add(lblSoLuong, BorderLayout.SOUTH);

        return card;
    }

    /** Renderer trang thai dang "the mau" (pill), dong bo voi HoaDonPanel. */
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

    // ================== TIM KIEM / LOC ==================

    private void apDungBoLoc() {
        List<RowFilter<Object, Object>> danhSachLoc = new ArrayList<>();

        String tuKhoa = txtTimKiem.getText().trim();
        if (!tuKhoa.isEmpty()) {
            danhSachLoc.add(RowFilter.regexFilter("(?i)" + Pattern.quote(tuKhoa), 1, 2));
        }
        String trangThai = (String) cboTrangThai.getSelectedItem();
        if (trangThai != null && !trangThai.equals("Tat ca trang thai")) {
            danhSachLoc.add(RowFilter.regexFilter("^" + Pattern.quote(trangThai) + "$", 5));
        }

        sorter.setRowFilter(danhSachLoc.isEmpty() ? null : RowFilter.andFilter(danhSachLoc));
        capNhatSoLuongHienThi();
    }

    private void capNhatSoLuongHienThi() {
        lblSoLuong.setText("Hien thi " + table.getRowCount() + " / " + tableModel.getRowCount() + " hoa don");
    }


    /** Cho phep man hinh khac (VD: Thong ke) kich hoat tim kiem tu ben ngoai. */
    public void timKiem(String tuKhoa) {
        txtTimKiem.setText(tuKhoa);
    }

    // ================== TAI DU LIEU ==================

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
                    UIUtils.thongBaoLoi(CongNoPanel.this, "Khong the tai du lieu cong no.");
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

    // ================== XEM CHI TIET (double-click) ==================

    private void xemChiTietDongDangChon() {
        HoaDonHocPhi hd = layHoaDonDangChon();
        if (hd == null) return;

        JPanel noiDung = new JPanel();
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        dongChiTiet(noiDung, "Ma hoa don", "#" + hd.getMaHoaDon());
        dongChiTiet(noiDung, "Sinh vien", hd.getTenSV() + " (" + hd.getMaSV() + ")");
        dongChiTiet(noiDung, "Hoc ky", hd.getTenHocKy());
        noiDung.add(Box.createRigidArea(new Dimension(0, 8)));
        dongChiTiet(noiDung, "Tong hoc phi", MoneyUtils.format(hd.getSoTien()));
        dongChiTiet(noiDung, "Da nop", MoneyUtils.format(hd.getDaNop()));
        dongChiTiet(noiDung, "Con no", MoneyUtils.format(hd.tinhConNo()));
        dongChiTiet(noiDung, "Trang thai", hd.tinhTrangThai().getNhan());

        JButton btnXemSV = UITheme.primaryButton("Xem ho so sinh vien");
        btnXemSV.setAlignmentX(Component.LEFT_ALIGNMENT);
        noiDung.add(Box.createRigidArea(new Dimension(0, 10)));
        noiDung.add(btnXemSV);

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Chi tiet cong no - Hoa don #" + hd.getMaHoaDon());
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

    // ================== GUI NHAC NO QUA EMAIL ==================

    private void guiNhacNoDaChon() {
        HoaDonHocPhi hd = layHoaDonDangChon();
        if (hd == null) {
            UIUtils.thongBaoLoi(this, "Vui long chon 1 dong trong bang de gui nhac no.");
            return;
        }

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                SinhVien sv = sinhVienDAO.timTheoMa(hd.getMaSV());
                if (sv == null || sv.getEmail() == null || sv.getEmail().isBlank()) {
                    throw new IllegalStateException("Sinh vien chua co email trong he thong.");
                }
                return EmailUtils.guiNhacHocPhi(sv.getEmail(), hd.getTenSV(), hd.getTenHocKy(),
                        MoneyUtils.format(hd.tinhConNo()));
            }

            @Override
            protected void done() {
                try {
                    boolean thanhCong = get();
                    if (thanhCong) {
                        UIUtils.thongBao(CongNoPanel.this, "Da gui email nhac no cho sinh vien " + hd.getTenSV());
                    } else {
                        UIUtils.thongBaoLoi(CongNoPanel.this,
                                "Gui email that bai. Kiem tra lai cau hinh mail trong application.properties.");
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(CongNoPanel.this, cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    // ================== XUAT EXCEL / PDF ==================

    private String[] tieuDeCotXuat() {
        return new String[]{"Ma HD", "Ma SV", "Ho ten", "Hoc ky", "Con no", "Trang thai"};
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
            UIUtils.thongBaoLoi(this, "Khong co du lieu de xuat.");
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
                    UIUtils.thongBao(CongNoPanel.this, "Da xuat file Excel:\n" + duongDan);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(CongNoPanel.this, "Xuat Excel that bai: " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void xuatPDF() {
        if (table.getRowCount() == 0) {
            UIUtils.thongBaoLoi(this, "Khong co du lieu de xuat.");
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
                PDFExporter.exportBangDuLieu(duongDan, "DANH SACH CONG NO HOC PHI", tieuDeCotXuat(), layDuLieuDangHienThi());
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UIUtils.thongBao(CongNoPanel.this, "Da xuat file PDF:\n" + duongDan);
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    UIUtils.thongBaoLoi(CongNoPanel.this, "Xuat PDF that bai: " + cause.getMessage());
                }
            }
        };
        worker.execute();
    }
}