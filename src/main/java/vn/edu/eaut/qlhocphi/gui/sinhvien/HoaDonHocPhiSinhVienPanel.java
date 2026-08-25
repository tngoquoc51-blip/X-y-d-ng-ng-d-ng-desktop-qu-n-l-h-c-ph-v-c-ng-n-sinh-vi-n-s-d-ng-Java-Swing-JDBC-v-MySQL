package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.bus.CongNoService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.gui.thanhtoan.PaymentMethodDialog;
import vn.edu.eaut.qlhocphi.gui.thanhtoan.ChonSoTienThanhToanDialog;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.TrangThaiHoaDon;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * Trang rieng "Hoa don hoc phi" cho cong Sinh vien.
 *
 * THIET KE V3 (chuyen tu "the/card" sang BANG DU LIEU - dung phong cach phan
 * mem quan ly desktop dong bo voi cac trang Admin trong he thong, vi day la
 * mot do an "ung dung quan ly desktop": banner tieu de, dai the thong ke,
 * thanh cong cu (loc trang thai), va JTable co the sap xep theo cot, dong
 * ke soc, nhan trang thai dang "the mau" trong bang, cot thao tac co nut
 * "Thanh toan ngay" rieng cho tung dong - giong het cach lam cua HoaDonPanel
 * (man hinh Admin) de toan bo he thong dong bo mot phong cach.
 */
public class HoaDonHocPhiSinhVienPanel extends JPanel {
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final CongNoService congNoService = new CongNoService();
    private final TaiKhoan taiKhoan;
    private final String maSV;

    private final JPanel theThongKeBox = new JPanel(new GridLayout(1, 4, 14, 0));
    private JComboBox<String> cboTrangThai;
    private JLabel lblSoLuong;

    private JTable table;
    private HoaDonTableModel tableModel;
    private TableRowSorter<HoaDonTableModel> sorter;

    private List<HoaDonHocPhi> tatCaHoaDon = new ArrayList<>();

    public HoaDonHocPhiSinhVienPanel(TaiKhoan taiKhoan) {
        this.taiKhoan = taiKhoan;
        this.maSV = taiKhoan.getMaSV();
        setOpaque(false);
        setLayout(new BorderLayout(0, 16));

        JPanel dauTrang = new JPanel();
        dauTrang.setOpaque(false);
        dauTrang.setLayout(new BoxLayout(dauTrang, BoxLayout.Y_AXIS));

        dauTrang.add(buildBanner());
        dauTrang.add(Box.createRigidArea(new Dimension(0, 16)));

        theThongKeBox.setOpaque(false);
        theThongKeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        theThongKeBox.add(UITheme.statCard("Tổng học phí", "…", UITheme.TINT_VIOLET, UITheme.TEXT_VIOLET));
        theThongKeBox.add(UITheme.statCard("Đã đóng", "…", UITheme.TINT_GREEN, UITheme.TEXT_GREEN));
        theThongKeBox.add(UITheme.statCard("Còn nợ", "…", UITheme.TINT_RED, UITheme.TEXT_RED));
        theThongKeBox.add(UITheme.statCard("Cần đóng", "…", UITheme.TINT_BLUE, UITheme.TEXT_BLUE));
        dauTrang.add(theThongKeBox);
        dauTrang.add(Box.createRigidArea(new Dimension(0, 16)));

        dauTrang.add(buildToolbar());

        add(dauTrang, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);

        taiDuLieu();
    }

    // ================== BANNER TIEU DE ==================

    private JPanel buildBanner() {
        JPanel banner = UITheme.gradientBanner();
        banner.setLayout(new BorderLayout(14, 0));
        banner.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        banner.setPreferredSize(new Dimension(10, 84));
        banner.setAlignmentX(Component.LEFT_ALIGNMENT);
        banner.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));

        JPanel trai = new JPanel(new BorderLayout(14, 0));
        trai.setOpaque(false);
        trai.add(logoBadge(), BorderLayout.WEST);

        JPanel chuText = new JPanel();
        chuText.setOpaque(false);
        chuText.setLayout(new BoxLayout(chuText, BoxLayout.Y_AXIS));
        JLabel tieuDe = new JLabel("Hóa đơn học phí");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Theo dõi và thanh toán các khoản học phí của bạn");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);

        JButton btnLamMoi = UITheme.secondaryButton("Làm mới");
        btnLamMoi.addActionListener(e -> taiDuLieu());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        actions.add(btnLamMoi);
        banner.add(actions, BorderLayout.EAST);

        return banner;
    }

    /** Logo tron ve bang Graphics2D (khong dung emoji/ky tu dac biet - tranh loi thieu font). */
    private JComponent logoBadge() {
        JComponent badge = new JComponent() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255, 55));
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.4f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                // Ve icon to hoa don don gian: khung chu nhat + 3 gach ngang
                g2.drawRoundRect(cx - 9, cy - 12, 18, 24, 4, 4);
                g2.drawLine(cx - 5, cy - 5, cx + 5, cy - 5);
                g2.drawLine(cx - 5, cy, cx + 5, cy);
                g2.drawLine(cx - 5, cy + 5, cx + 2, cy + 5);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(52, 52));
        badge.setOpaque(false);
        return badge;
    }

    // ================== THANH CONG CU: LOC TRANG THAI ==================

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        trai.setOpaque(false);
        trai.add(UIUtils.formLabel("Trạng thái:"));

        cboTrangThai = new JComboBox<>(new String[]{
                "Tất cả trạng thái", "Chưa đóng", "Đóng một phần", "Quá hạn", "Đã đóng đủ"
        });
        cboTrangThai.setFont(UITheme.FONT_BASE);
        cboTrangThai.addActionListener(e -> apDungBoLoc());
        trai.add(cboTrangThai);
        toolbar.add(trai, BorderLayout.WEST);

        lblSoLuong = new JLabel("Hiển thị 0 / 0 hóa đơn");
        lblSoLuong.setFont(UITheme.FONT_BASE);
        lblSoLuong.setForeground(UITheme.TEXT_MUTED);
        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        phai.setOpaque(false);
        phai.add(lblSoLuong);
        toolbar.add(phai, BorderLayout.EAST);

        return toolbar;
    }

    // ================== BANG DU LIEU ==================

    private JPanel buildTableCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());

        tableModel = new HoaDonTableModel();
        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        table.setRowHeight(46);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setSortable(HoaDonTableModel.COT_THAO_TAC, false);

        // Renderer soc xen ke mac dinh cho cac cot van ban/so thuong
        DefaultTableCellRenderer stripedRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF7, 0xF9, 0xFC));
                    setForeground(UITheme.TEXT_PRIMARY);
                }
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                setToolTipText(value == null ? null : value.toString());
                return c;
            }
        };

        table.getColumnModel().getColumn(HoaDonTableModel.COT_HOC_KY).setCellRenderer(stripedRenderer);

        table.getColumnModel().getColumn(HoaDonTableModel.COT_SO_TC).setCellRenderer(
                soRenderer(SwingConstants.CENTER, null));
        table.getColumnModel().getColumn(HoaDonTableModel.COT_HAN_NOP).setCellRenderer(dateRenderer());
        table.getColumnModel().getColumn(HoaDonTableModel.COT_TONG_TIEN).setCellRenderer(
                tienRenderer(UITheme.TEXT_PRIMARY));
        table.getColumnModel().getColumn(HoaDonTableModel.COT_DA_DONG).setCellRenderer(
                tienRenderer(UITheme.TEXT_GREEN));
        table.getColumnModel().getColumn(HoaDonTableModel.COT_CON_NO).setCellRenderer(conNoRenderer());
        table.getColumnModel().getColumn(HoaDonTableModel.COT_TRANG_THAI).setCellRenderer(new TrangThaiCellRenderer());

        table.getColumnModel().getColumn(HoaDonTableModel.COT_THAO_TAC).setCellRenderer(new ThaoTacRenderer());
        table.getColumnModel().getColumn(HoaDonTableModel.COT_THAO_TAC).setCellEditor(new ThaoTacEditor());

        table.getColumnModel().getColumn(HoaDonTableModel.COT_HOC_KY).setPreferredWidth(160);
        table.getColumnModel().getColumn(HoaDonTableModel.COT_SO_TC).setPreferredWidth(46);
        table.getColumnModel().getColumn(HoaDonTableModel.COT_HAN_NOP).setPreferredWidth(90);
        table.getColumnModel().getColumn(HoaDonTableModel.COT_TONG_TIEN).setPreferredWidth(126);
        table.getColumnModel().getColumn(HoaDonTableModel.COT_DA_DONG).setPreferredWidth(126);
        table.getColumnModel().getColumn(HoaDonTableModel.COT_CON_NO).setPreferredWidth(126);
        table.getColumnModel().getColumn(HoaDonTableModel.COT_TRANG_THAI).setPreferredWidth(126);
        table.getColumnModel().getColumn(HoaDonTableModel.COT_THAO_TAC).setPreferredWidth(150);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);

        return card;
    }

    private TableCellRenderer soRenderer(int align, Color mau) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(align);
                if (!isSelected) {
                    l.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF7, 0xF9, 0xFC));
                    l.setForeground(mau != null ? mau : UITheme.TEXT_PRIMARY);
                }
                l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return l;
            }
        };
    }

    private TableCellRenderer dateRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                String text = value instanceof LocalDate ? ((LocalDate) value).format(DMY) : "-";
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, text, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.CENTER);
                if (!isSelected) {
                    l.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF7, 0xF9, 0xFC));
                    l.setForeground(UITheme.TEXT_MUTED);
                }
                l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return l;
            }
        };
    }

    private TableCellRenderer tienRenderer(Color mau) {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                String text = value instanceof BigDecimal ? MoneyUtils.format((BigDecimal) value) : "-";
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, text, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setFont(UITheme.FONT_BOLD);
                if (!isSelected) {
                    l.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF7, 0xF9, 0xFC));
                    l.setForeground(mau);
                }
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return l;
            }
        };
    }

    private TableCellRenderer conNoRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                BigDecimal conNo = value instanceof BigDecimal ? (BigDecimal) value : BigDecimal.ZERO;
                String text = MoneyUtils.format(conNo);
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, text, isSelected, hasFocus, row, col);
                l.setHorizontalAlignment(SwingConstants.RIGHT);
                l.setFont(UITheme.FONT_BOLD);
                if (!isSelected) {
                    l.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF7, 0xF9, 0xFC));
                    l.setForeground(conNo.compareTo(BigDecimal.ZERO) > 0 ? UITheme.DANGER : UITheme.TEXT_MUTED);
                }
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                return l;
            }
        };
    }

    /** Renderer ve trang thai dang "the mau" (pill bo tron) - dong bo mau voi khoi the thong ke. */
    private class TrangThaiCellRenderer implements TableCellRenderer {
        private final RoundedPillLabel label = new RoundedPillLabel();

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int col) {
            TrangThaiHoaDon trangThai = value instanceof TrangThaiHoaDon ? (TrangThaiHoaDon) value : TrangThaiHoaDon.CHUA_DONG;
            label.setText(trangThai.getNhan());
            Color mau = mauTheoTrangThai(trangThai);
            label.mauChu = mau;
            label.mauNen = phoiMauNhat(mau);
            label.setOpaque(false);
            return label;
        }
    }

    /** JLabel tu ve nen bo tron - dung chung logic voi UITheme.pill() nhung tai su dung 1 instance cho toan bang. */
    private static class RoundedPillLabel extends JLabel {
        Color mauNen = Color.WHITE;
        Color mauChu = Color.BLACK;

        RoundedPillLabel() {
            super("", SwingConstants.CENTER);
            setFont(UITheme.FONT_BOLD);
            setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getParent() != null ? getParent().getBackground() : Color.WHITE);
            g2.fillRect(0, 0, getWidth(), getHeight());
            int pillW = Math.min(getWidth() - 8, getFontMetrics(getFont()).stringWidth(getText()) + 24);
            int pillH = Math.min(getHeight() - 6, 24);
            int x = (getWidth() - pillW) / 2, y = (getHeight() - pillH) / 2;
            g2.setColor(mauNen);
            g2.fillRoundRect(x, y, pillW, pillH, pillH, pillH);
            g2.dispose();
            setForeground(mauChu);
            super.paintComponent(g);
        }
    }

    /** Renderer nut "Thanh toan ngay" trong cot Thao tac - chi hien khi con no > 0. */
    private class ThaoTacRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int col) {
            return taoOThaoTac(value, row);
        }
    }

    /** Editor nut "Thanh toan ngay" - bam vao se mo dialog thanh toan cho dung hoa don cua dong do. */
    private class ThaoTacEditor extends AbstractCellEditor implements TableCellEditor {
        private HoaDonHocPhi hoaDonDangSua;

        @Override
        public Component getTableCellEditorComponent(JTable t, Object value, boolean isSelected, int row, int col) {
            hoaDonDangSua = value instanceof HoaDonHocPhi ? (HoaDonHocPhi) value : null;
            JPanel o = taoOThaoTac(value, row);
            for (Component c : o.getComponents()) {
                if (c instanceof JButton) {
                    ((JButton) c).addActionListener(e -> {
                        fireEditingStopped();
                        if (hoaDonDangSua != null) moThanhToan(hoaDonDangSua);
                    });
                }
            }
            return o;
        }

        @Override
        public Object getCellEditorValue() { return hoaDonDangSua; }

        @Override
        public boolean isCellEditable(EventObject e) { return true; }
    }

    private JPanel taoOThaoTac(Object value, int row) {
        JPanel o = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        boolean mauXen = row % 2 != 0;
        o.setBackground(mauXen ? new Color(0xF7, 0xF9, 0xFC) : Color.WHITE);
        if (value instanceof HoaDonHocPhi) {
            HoaDonHocPhi hd = (HoaDonHocPhi) value;
            if (hd.tinhConNo().compareTo(BigDecimal.ZERO) > 0) {
                JButton btn = UITheme.primaryButton("Thanh toán ngay");
                btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
                btn.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
                o.add(btn);
            } else {
                JLabel xong = new JLabel("Đã hoàn tất");
                xong.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                xong.setForeground(UITheme.TEXT_MUTED);
                o.add(xong);
            }
        }
        // Buoc layout ngay tai day (thay vi cho CellRendererPane tu validate) de dam bao
        // nut/label ben trong luon co toa do dung ngay lan ve dau tien, tranh truong hop
        // vien Swing chua kip validate lam mat noi dung trong 1 khung hinh ve dau tien.
        o.setSize(o.getPreferredSize());
        o.doLayout();
        return o;
    }

    // ================== TAI / LOC DU LIEU ==================

    public void taiDuLieu() {
        if (maSV == null || maSV.isBlank()) return;
        SwingWorker<List<HoaDonHocPhi>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<HoaDonHocPhi> doInBackground() throws Exception {
                return congNoService.layTheoSinhVien(maSV);
            }

            @Override
            protected void done() {
                try {
                    tatCaHoaDon = get();
                    capNhatTheThongKe(tatCaHoaDon);
                    tableModel.capNhat(tatCaHoaDon);
                    apDungBoLoc();
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(HoaDonHocPhiSinhVienPanel.this,
                            "Không thể tải danh sách hóa đơn.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void apDungBoLoc() {
        String chon = (String) cboTrangThai.getSelectedItem();
        TrangThaiHoaDon loc = anhXaLoc(chon);
        if (loc == null) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(loc.name()) + "$",
                    HoaDonTableModel.COT_TRANG_THAI));
        }
        capNhatSoLuongHienThi();
    }

    private TrangThaiHoaDon anhXaLoc(String nhan) {
        if (nhan == null) return null;
        switch (nhan) {
            case "Chưa đóng": return TrangThaiHoaDon.CHUA_DONG;
            case "Đóng một phần": return TrangThaiHoaDon.DONG_MOT_PHAN;
            case "Quá hạn": return TrangThaiHoaDon.QUA_HAN;
            case "Đã đóng đủ": return TrangThaiHoaDon.DA_DONG_DU;
            default: return null;
        }
    }

    private void capNhatSoLuongHienThi() {
        if (lblSoLuong == null) return;
        int tong = tableModel.getRowCount();
        int hien = table.getRowCount();
        lblSoLuong.setText("Hiển thị " + hien + " / " + tong + " hóa đơn");
    }

    private void capNhatTheThongKe(List<HoaDonHocPhi> ds) {
        BigDecimal tongHocPhi = ds.stream().map(HoaDonHocPhi::getSoTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tongDaNop = ds.stream().map(HoaDonHocPhi::getDaNop)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tongConNo = ds.stream().map(HoaDonHocPhi::tinhConNo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long soCanDong = ds.stream().filter(hd -> hd.tinhConNo().compareTo(BigDecimal.ZERO) > 0).count();

        capNhatGiaTriThe(0, MoneyUtils.format(tongHocPhi));
        capNhatGiaTriThe(1, MoneyUtils.format(tongDaNop));
        capNhatGiaTriThe(2, MoneyUtils.format(tongConNo));
        capNhatGiaTriThe(3, soCanDong + " hóa đơn");
    }

    private void capNhatGiaTriThe(int index, String giaTriMoi) {
        JPanel the = (JPanel) theThongKeBox.getComponent(index);
        JLabel lbl = timNhanTheoTen(the, "giaTri");
        if (lbl != null) lbl.setText(giaTriMoi);
    }

    private JLabel timNhanTheoTen(Container container, String name) {
        for (Component c : container.getComponents()) {
            if (name.equals(c.getName()) && c instanceof JLabel) return (JLabel) c;
            if (c instanceof Container) {
                JLabel ket = timNhanTheoTen((Container) c, name);
                if (ket != null) return ket;
            }
        }
        return null;
    }

    private void moThanhToan(HoaDonHocPhi hd) {
        Window chaMe = SwingUtilities.getWindowAncestor(this);
        new ChonSoTienThanhToanDialog(chaMe, hd.tinhConNo(), soTienDaChon ->
                new PaymentMethodDialog(chaMe, hd.getMaHoaDon(), soTienDaChon, this::taiDuLieu).setVisible(true)
        ).setVisible(true);
    }

    private Color mauTheoTrangThai(TrangThaiHoaDon t) {
        switch (t) {
            case QUA_HAN: return UITheme.DANGER;
            case DONG_MOT_PHAN: return UITheme.WARNING;
            case DA_DONG_DU: return UITheme.SUCCESS;
            default: return UITheme.TEXT_MUTED;
        }
    }

    private Color phoiMauNhat(Color mau) {
        return new Color(mau.getRed(), mau.getGreen(), mau.getBlue(), 30);
    }

    private String rootMessage(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }

    /** TableModel rieng, lay du lieu truc tiep tu danh sach HoaDonHocPhi - moi cot tra ve
     *  dung kieu du lieu goc (BigDecimal/LocalDate/Integer...) de JTable sap xep dung kieu
     *  so/ngay thay vi sap xep theo chuoi van ban. */
    private static class HoaDonTableModel extends AbstractTableModel {
        static final int COT_HOC_KY = 0;
        static final int COT_SO_TC = 1;
        static final int COT_HAN_NOP = 2;
        static final int COT_TONG_TIEN = 3;
        static final int COT_DA_DONG = 4;
        static final int COT_CON_NO = 5;
        static final int COT_TRANG_THAI = 6;
        static final int COT_THAO_TAC = 7;

        private static final String[] TEN_COT = {
                "Học kỳ", "Số TC", "Hạn nộp", "Tổng tiền", "Đã đóng", "Còn nợ", "Trạng thái", ""
        };

        private List<HoaDonHocPhi> ds = new ArrayList<>();

        void capNhat(List<HoaDonHocPhi> dsMoi) {
            this.ds = dsMoi;
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return ds.size(); }
        @Override public int getColumnCount() { return TEN_COT.length; }
        @Override public String getColumnName(int c) { return c < TEN_COT.length ? TEN_COT[c] : ""; }
        @Override public boolean isCellEditable(int row, int col) { return col == COT_THAO_TAC; }

        @Override
        public Class<?> getColumnClass(int c) {
            switch (c) {
                case COT_SO_TC: return Integer.class;
                case COT_HAN_NOP: return LocalDate.class;
                case COT_TONG_TIEN:
                case COT_DA_DONG:
                case COT_CON_NO: return BigDecimal.class;
                case COT_TRANG_THAI: return TrangThaiHoaDon.class;
                case COT_THAO_TAC: return HoaDonHocPhi.class;
                default: return String.class;
            }
        }

        @Override
        public Object getValueAt(int row, int col) {
            HoaDonHocPhi hd = ds.get(row);
            switch (col) {
                case COT_HOC_KY: return hd.getTenHocKy();
                case COT_SO_TC: return hd.getSoTinChi();
                case COT_HAN_NOP: return hd.getHanThanhToan();
                case COT_TONG_TIEN: return hd.getSoTien();
                case COT_DA_DONG: return hd.getDaNop();
                case COT_CON_NO: return hd.tinhConNo();
                case COT_TRANG_THAI: return hd.tinhTrangThai();
                case COT_THAO_TAC: return hd;
                default: return null;
            }
        }
    }
}