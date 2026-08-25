package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.dal.PhieuThuDAO;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.PhieuThu;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;
import vn.edu.eaut.qlhocphi.util.PDFExporter;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Khoi "LICH SU THANH TOAN" danh cho cong Sinh vien.
 *
 * Panel nay duoc dung o 2 noi khac nhau, nen co 2 CHE DO hien thi rieng biet:
 *
 *  - CHE DO GON (trangDayDu = false): nhung trong 1 o vuong co dinh 380x260
 *    tren trang "Tong quan" - giu nguyen dang "the" (card) don gian, cuon doc,
 *    KHONG thay doi gi so voi ban truoc, de khong lam vo layout o do.
 *
 *  - CHE DO TRANG DAY DU (trangDayDu = true): dung lam 1 trang rieng "Lich su
 *    thanh toan" trong sidebar. Ban V3 chuyen sang BANG DU LIEU (JTable) thay
 *    vi danh sach the, dung phong cach giong het cac man hinh Admin (banner +
 *    the thong ke + thanh loc + bang co the sap xep/loc) - vi day la do an
 *    "ung dung quan ly desktop" nen giao dien can dong bo phong cach chuyen
 *    nghiep thay vi kieu the cua ung dung dien thoai.
 *
 * Cach dung: nhung panel nay vao vi tri card "Lich su thanh toan" cu, roi goi
 * taiDuLieu(dsHoaDon) moi khi danh sach hoa don cua sinh vien duoc nap/lam moi.
 */
public class LichSuThanhToanPanel extends JPanel {
    private static final DateTimeFormatter DMY_HM = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    /** Doi ten truong o day cho dung voi truong ban - se hien o dau moi bien lai PDF. */
    private static final String TEN_TRUONG = "TRUONG DAI HOC EAUT";

    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();
    private final String hoTenSV;
    private final String maSV;
    private final boolean trangDayDu;

    /** Danh sach phieu thu dang hien thi - dung de xuat bien lai khi bam nut tren tung dong/the. */
    private List<PhieuThu> danhSachHienTai = new ArrayList<>();
    /** Hoa don day du tuong ung voi tung MaHoaDon, de lay Ten hoc ky + Tong hoc phi + Con no in len bien lai. */
    private final Map<Integer, HoaDonHocPhi> hoaDonTheoMa = new HashMap<>();

    // ---- Che do gon (card list) ----
    private final JPanel danhSachBox = new JPanel();

    // ---- Che do trang day du (bang du lieu) ----
    private final JPanel theThongKeBox = new JPanel(new GridLayout(1, 3, 14, 0));
    private JComboBox<String> cboHinhThuc;
    private JLabel lblSoLuong;
    private JTable table;
    private PhieuThuTableModel tableModel;
    private TableRowSorter<PhieuThuTableModel> sorter;

    public LichSuThanhToanPanel(String hoTenSV, String maSV) {
        this(hoTenSV, maSV, false);
    }

    public LichSuThanhToanPanel(String hoTenSV, String maSV, boolean trangDayDu) {
        this.hoTenSV = hoTenSV;
        this.maSV = maSV;
        this.trangDayDu = trangDayDu;
        setOpaque(false);

        if (trangDayDu) {
            xayDungTrangDayDu();
        } else {
            xayDungCheDoGon();
        }
    }

    // ================================================================
    // ==================  CHE DO TRANG DAY DU (BANG)  ================
    // ================================================================

    private void xayDungTrangDayDu() {
        setLayout(new BorderLayout(0, 16));

        JPanel dauTrang = new JPanel();
        dauTrang.setOpaque(false);
        dauTrang.setLayout(new BoxLayout(dauTrang, BoxLayout.Y_AXIS));

        dauTrang.add(buildBanner());
        dauTrang.add(Box.createRigidArea(new Dimension(0, 16)));

        theThongKeBox.setOpaque(false);
        theThongKeBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        theThongKeBox.add(UITheme.statCard("Tổng đã thanh toán", "…", UITheme.TINT_GREEN, UITheme.TEXT_GREEN));
        theThongKeBox.add(UITheme.statCard("Số giao dịch", "…", UITheme.TINT_BLUE, UITheme.TEXT_BLUE));
        theThongKeBox.add(UITheme.statCard("Giao dịch gần nhất", "…", UITheme.TINT_VIOLET, UITheme.TEXT_VIOLET));
        dauTrang.add(theThongKeBox);
        dauTrang.add(Box.createRigidArea(new Dimension(0, 16)));

        dauTrang.add(buildToolbar());
        add(dauTrang, BorderLayout.NORTH);
        add(buildTableCard(), BorderLayout.CENTER);
    }

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
        JLabel tieuDe = new JLabel("Lịch sử thanh toán");
        tieuDe.setFont(UITheme.FONT_TITLE);
        tieuDe.setForeground(Color.WHITE);
        JLabel phu = new JLabel("Toàn bộ giao dịch bạn đã thực hiện, kèm biên lai PDF");
        phu.setFont(UITheme.FONT_BASE);
        phu.setForeground(new Color(255, 255, 255, 210));
        chuText.add(tieuDe);
        chuText.add(Box.createRigidArea(new Dimension(0, 4)));
        chuText.add(phu);
        trai.add(chuText, BorderLayout.CENTER);
        banner.add(trai, BorderLayout.WEST);

        JButton btnLamMoi = UITheme.secondaryButton("Làm mới");
        btnLamMoi.addActionListener(e -> tuTaiDuLieu());
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
                // Icon dong ho / lich su: vong tron + kim
                int r = 11;
                g2.drawOval(cx - r, cy - r, r * 2, r * 2);
                g2.drawLine(cx, cy, cx, cy - 6);
                g2.drawLine(cx, cy, cx + 5, cy + 2);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(52, 52));
        badge.setOpaque(false);
        return badge;
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setOpaque(false);
        toolbar.setAlignmentX(Component.LEFT_ALIGNMENT);
        toolbar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        trai.setOpaque(false);
        trai.add(UIUtils.formLabel("Hình thức:"));

        cboHinhThuc = new JComboBox<>(new String[]{
                "Tất cả hình thức", "Tiền mặt", "Chuyển khoản", "Online"
        });
        cboHinhThuc.setFont(UITheme.FONT_BASE);
        cboHinhThuc.addActionListener(e -> apDungBoLoc());
        trai.add(cboHinhThuc);
        toolbar.add(trai, BorderLayout.WEST);

        lblSoLuong = new JLabel("Hiển thị 0 / 0 giao dịch");
        lblSoLuong.setFont(UITheme.FONT_BASE);
        lblSoLuong.setForeground(UITheme.TEXT_MUTED);
        JPanel phai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        phai.setOpaque(false);
        phai.add(lblSoLuong);
        toolbar.add(phai, BorderLayout.EAST);

        return toolbar;
    }

    private JPanel buildTableCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());

        tableModel = new PhieuThuTableModel();
        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        table.setRowHeight(46);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        sorter = new TableRowSorter<>(tableModel);
        table.setRowSorter(sorter);
        sorter.setSortable(PhieuThuTableModel.COT_THAO_TAC, false);

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

        table.getColumnModel().getColumn(PhieuThuTableModel.COT_NGAY_NOP).setCellRenderer(ngayNopRenderer());
        table.getColumnModel().getColumn(PhieuThuTableModel.COT_HOC_KY).setCellRenderer(stripedRenderer);
        table.getColumnModel().getColumn(PhieuThuTableModel.COT_SO_TIEN).setCellRenderer(tienRenderer());
        table.getColumnModel().getColumn(PhieuThuTableModel.COT_HINH_THUC).setCellRenderer(new HinhThucCellRenderer());
        table.getColumnModel().getColumn(PhieuThuTableModel.COT_MA_GD).setCellRenderer(stripedRenderer);
        table.getColumnModel().getColumn(PhieuThuTableModel.COT_THAO_TAC).setCellRenderer(new ThaoTacRenderer());
        table.getColumnModel().getColumn(PhieuThuTableModel.COT_THAO_TAC).setCellEditor(new ThaoTacEditor());

        table.getColumnModel().getColumn(PhieuThuTableModel.COT_NGAY_NOP).setPreferredWidth(148);
        table.getColumnModel().getColumn(PhieuThuTableModel.COT_HOC_KY).setPreferredWidth(160);
        table.getColumnModel().getColumn(PhieuThuTableModel.COT_SO_TIEN).setPreferredWidth(118);
        table.getColumnModel().getColumn(PhieuThuTableModel.COT_HINH_THUC).setPreferredWidth(108);
        table.getColumnModel().getColumn(PhieuThuTableModel.COT_MA_GD).setPreferredWidth(130);
        table.getColumnModel().getColumn(PhieuThuTableModel.COT_THAO_TAC).setPreferredWidth(110);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    private TableCellRenderer ngayNopRenderer() {
        return new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int col) {
                String text = value instanceof LocalDateTime ? ((LocalDateTime) value).format(DMY_HM) : "-";
                JLabel l = (JLabel) super.getTableCellRendererComponent(t, text, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    l.setBackground(row % 2 == 0 ? Color.WHITE : new Color(0xF7, 0xF9, 0xFC));
                    l.setForeground(UITheme.TEXT_PRIMARY);
                }
                l.setFont(UITheme.FONT_BOLD);
                l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                return l;
            }
        };
    }

    private TableCellRenderer tienRenderer() {
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
                    l.setForeground(UITheme.TEXT_GREEN);
                }
                l.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 10));
                return l;
            }
        };
    }

    /** Renderer ve hinh thuc thanh toan dang "the mau" (pill bo tron). */
    private class HinhThucCellRenderer implements TableCellRenderer {
        private final RoundedPillLabel label = new RoundedPillLabel();

        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int col) {
            String hinhThuc = value == null ? "" : value.toString();
            label.setText(rutGonHinhThuc(hinhThuc));
            Color mau = mauTheoHinhThuc(hinhThuc);
            label.mauChu = mau;
            label.mauNen = phoiMauNhat(mau);
            return label;
        }
    }

    /** JLabel tu ve nen bo tron, tai su dung 1 instance cho toan bang. */
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

    private class ThaoTacRenderer implements TableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int col) {
            return taoOThaoTac(value, row);
        }
    }

    private class ThaoTacEditor extends AbstractCellEditor implements TableCellEditor {
        private PhieuThu phieuThuDangSua;

        @Override
        public Component getTableCellEditorComponent(JTable t, Object value, boolean isSelected, int row, int col) {
            phieuThuDangSua = value instanceof PhieuThu ? (PhieuThu) value : null;
            JPanel o = taoOThaoTac(value, row);
            for (Component c : o.getComponents()) {
                if (c instanceof JButton) {
                    ((JButton) c).addActionListener(e -> {
                        fireEditingStopped();
                        if (phieuThuDangSua != null) taiBienLai(phieuThuDangSua);
                    });
                }
            }
            return o;
        }

        @Override
        public Object getCellEditorValue() { return phieuThuDangSua; }

        @Override
        public boolean isCellEditable(EventObject e) { return true; }
    }

    private JPanel taoOThaoTac(Object value, int row) {
        JPanel o = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        boolean mauXen = row % 2 != 0;
        o.setBackground(mauXen ? new Color(0xF7, 0xF9, 0xFC) : Color.WHITE);
        if (value instanceof PhieuThu) {
            JButton btn = UITheme.secondaryButton("Tải biên lai");
            btn.setFont(new Font("Segoe UI", Font.BOLD, 11));
            btn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
            o.add(btn);
        }
        // Buoc layout ngay tai day de nut luon co toa do dung ngay tu lan ve dau tien.
        o.setSize(o.getPreferredSize());
        o.doLayout();
        return o;
    }

    private void apDungBoLoc() {
        if (sorter == null) return;
        String chon = (String) cboHinhThuc.getSelectedItem();
        String maLoc = anhXaLoc(chon);
        if (maLoc == null) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("^" + java.util.regex.Pattern.quote(maLoc) + "$",
                    PhieuThuTableModel.COT_HINH_THUC));
        }
        capNhatSoLuongHienThi();
    }

    private String anhXaLoc(String nhan) {
        if (nhan == null) return null;
        switch (nhan) {
            case "Tiền mặt": return "TIEN_MAT";
            case "Chuyển khoản": return "CHUYEN_KHOAN";
            case "Online": return "THANH_TOAN_ONLINE";
            default: return null;
        }
    }

    private void capNhatSoLuongHienThi() {
        if (lblSoLuong == null || table == null) return;
        lblSoLuong.setText("Hiển thị " + table.getRowCount() + " / " + tableModel.getRowCount() + " giao dịch");
    }

    private void capNhatTheThongKe(List<PhieuThu> ds) {
        if (!trangDayDu) return;
        BigDecimal tongDaDong = ds.stream().map(PhieuThu::getSoTienNop)
                .filter(java.util.Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        String giaoDichGanNhat = ds.isEmpty() || ds.get(0).getNgayNop() == null
                ? "-" : ds.get(0).getNgayNop().format(DMY_HM);

        capNhatGiaTriThe(0, MoneyUtils.format(tongDaDong));
        capNhatGiaTriThe(1, ds.size() + " giao dịch");
        capNhatGiaTriThe(2, giaoDichGanNhat);
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

    /** TableModel cho che do trang day du - tra ve dung kieu du lieu goc de sap xep dung. */
    private class PhieuThuTableModel extends AbstractTableModel {
        static final int COT_NGAY_NOP = 0;
        static final int COT_HOC_KY = 1;
        static final int COT_SO_TIEN = 2;
        static final int COT_HINH_THUC = 3;
        static final int COT_MA_GD = 4;
        static final int COT_THAO_TAC = 5;

        private static final String[] TEN_COT = {
                "Ngày nộp", "Học kỳ", "Số tiền", "Hình thức", "Mã giao dịch", ""
        };

        @Override public int getRowCount() { return danhSachHienTai.size(); }
        @Override public int getColumnCount() { return TEN_COT.length; }
        @Override public String getColumnName(int c) { return c < TEN_COT.length ? TEN_COT[c] : ""; }
        @Override public boolean isCellEditable(int row, int col) { return col == COT_THAO_TAC; }

        @Override
        public Class<?> getColumnClass(int c) {
            switch (c) {
                case COT_NGAY_NOP: return LocalDateTime.class;
                case COT_SO_TIEN: return BigDecimal.class;
                case COT_THAO_TAC: return PhieuThu.class;
                default: return String.class;
            }
        }

        @Override
        public Object getValueAt(int row, int col) {
            PhieuThu pt = danhSachHienTai.get(row);
            switch (col) {
                case COT_NGAY_NOP: return pt.getNgayNop();
                case COT_HOC_KY: {
                    HoaDonHocPhi hd = hoaDonTheoMa.get(pt.getMaHoaDon());
                    return hd != null && hd.getTenHocKy() != null ? hd.getTenHocKy() : "-";
                }
                case COT_SO_TIEN: return pt.getSoTienNop();
                case COT_HINH_THUC: return pt.getHinhThuc();
                case COT_MA_GD: return (pt.getMaGiaoDich() == null || pt.getMaGiaoDich().isBlank()) ? "-" : pt.getMaGiaoDich();
                case COT_THAO_TAC: return pt;
                default: return null;
            }
        }
    }

    // ================================================================
    // ======================  CHE DO GON (THE)  =======================
    // ================================================================

    private void xayDungCheDoGon() {
        setLayout(new BorderLayout(0, 14));
        JPanel dauTrang = new JPanel();
        dauTrang.setOpaque(false);
        dauTrang.setLayout(new BoxLayout(dauTrang, BoxLayout.Y_AXIS));
        dauTrang.add(nhanTieuDeCoVach("LỊCH SỬ THANH TOÁN", UITheme.WARNING));
        add(dauTrang, BorderLayout.NORTH);

        danhSachBox.setLayout(new BoxLayout(danhSachBox, BoxLayout.Y_AXIS));
        danhSachBox.setOpaque(false);

        // QUAN TRONG: dung KhungTuKhopChieuRong (implements Scrollable) thay vi JPanel
        // thuong - neu khong, JScrollPane se GIU NGUYEN chieu rong "tu nhien" cua noi
        // dung ben trong lam chieu rong rieng cua no, dan den 2 hau qua: (1) hien thanh
        // cuon ngang xau; (2) lam sai preferredWidth ma GridLayout ben ngoai
        // (SinhVienCongNoPanel) dung de chia doi 2 the.
        JPanel wrap = new KhungTuKhopChieuRong(new BorderLayout());
        wrap.setOpaque(false);
        wrap.add(danhSachBox, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(wrap);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        setMinimumSize(new Dimension(0, 220));
        setPreferredSize(null);
        add(scroll, BorderLayout.CENTER);

        hienThiRong();
    }

    private JPanel nhanTieuDeCoVach(String text, Color mauVach) {
        JPanel p = new JPanel(new BorderLayout(8, 0));
        p.setOpaque(false);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        JPanel vach = new JPanel();
        vach.setBackground(mauVach);
        vach.setPreferredSize(new Dimension(4, 18));
        p.add(vach, BorderLayout.WEST);
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lbl.setForeground(UITheme.TEXT_PRIMARY);
        p.add(lbl, BorderLayout.CENTER);
        return p;
    }

    private void hienThiRong() {
        danhSachBox.removeAll();
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BorderLayout());
        JLabel trong = new JLabel("Chưa có giao dịch nào.");
        trong.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        trong.setForeground(UITheme.TEXT_MUTED);
        trong.setBorder(new EmptyBorder(6, 2, 6, 2));
        p.add(trong, BorderLayout.WEST);
        danhSachBox.add(p);
        danhSachBox.revalidate();
        danhSachBox.repaint();
    }

    private void hienThiThe(List<PhieuThu> ds) {
        danhSachBox.removeAll();
        if (ds == null || ds.isEmpty()) {
            hienThiRong();
            return;
        }
        for (PhieuThu pt : ds) {
            danhSachBox.add(theGiaoDich(pt));
            danhSachBox.add(Box.createRigidArea(new Dimension(0, 8)));
        }
        danhSachBox.revalidate();
        danhSachBox.repaint();
    }

    /** 1 the giao dich (che do gon): goc bo tron, ngay + hinh thuc ben trai, so tien + nut tai bien lai ben phai. */
    private JPanel theGiaoDich(PhieuThu pt) {
        Color mauHt = mauTheoHinhThuc(pt.getHinhThuc());

        JPanel the = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.setColor(UITheme.BORDER);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.setColor(mauHt);
                g2.fillRoundRect(0, 6, 4, getHeight() - 12, 4, 4);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        the.setOpaque(false);
        the.setAlignmentX(Component.LEFT_ALIGNMENT);
        the.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        the.setBorder(new EmptyBorder(10, 14, 10, 12));
        String maGD = (pt.getMaGiaoDich() != null && !pt.getMaGiaoDich().isBlank())
                ? "\nMã GD: " + pt.getMaGiaoDich() : "";
        the.setToolTipText("<html>" + rutGonHinhThuc(pt.getHinhThuc())
                + (maGD.isEmpty() ? "" : maGD.replace("\n", "<br>")) + "</html>");

        JPanel trai = new JPanel();
        trai.setOpaque(false);
        trai.setLayout(new BoxLayout(trai, BoxLayout.Y_AXIS));

        JLabel lblNgay = new JLabel(pt.getNgayNop() != null ? pt.getNgayNop().format(DMY_HM) : "-");
        lblNgay.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNgay.setForeground(UITheme.TEXT_PRIMARY);
        lblNgay.setAlignmentX(Component.LEFT_ALIGNMENT);
        trai.add(lblNgay);

        JLabel lblHt = UITheme.pill(rutGonHinhThuc(pt.getHinhThuc()), phoiMauNhat(mauHt), mauHt);
        lblHt.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblHt.setBorder(new EmptyBorder(3, 10, 3, 10));
        lblHt.setAlignmentX(Component.LEFT_ALIGNMENT);

        trai.add(Box.createRigidArea(new Dimension(0, 5)));
        trai.add(lblHt);
        the.add(trai, BorderLayout.WEST);

        JPanel phai = new JPanel(new BorderLayout(10, 0));
        phai.setOpaque(false);

        JLabel lblSoTien = new JLabel(MoneyUtils.format(pt.getSoTienNop()));
        lblSoTien.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSoTien.setForeground(UITheme.TEXT_GREEN);
        phai.add(lblSoTien, BorderLayout.CENTER);

        JButton btnTai = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int cx = getWidth() / 2, cy = getHeight() / 2;
                g2.drawLine(cx, cy - 6, cx, cy + 5);
                g2.drawLine(cx - 5, cy, cx, cy + 6);
                g2.drawLine(cx + 5, cy, cx, cy + 6);
                g2.dispose();
            }
        };
        btnTai.setToolTipText("Tải biên lai PDF");
        btnTai.setBackground(UITheme.PRIMARY);
        btnTai.setFocusPainted(false);
        btnTai.setBorderPainted(false);
        btnTai.setContentAreaFilled(true);
        btnTai.setOpaque(true);
        btnTai.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTai.setPreferredSize(new Dimension(34, 34));
        btnTai.addActionListener(e -> taiBienLai(pt));
        phai.add(btnTai, BorderLayout.EAST);

        the.add(phai, BorderLayout.EAST);
        return the;
    }

    // ================================================================
    // ======================  DUNG CHUNG 2 CHE DO  =====================
    // ================================================================

    private Color mauTheoHinhThuc(String hinhThuc) {
        if (hinhThuc == null) return UITheme.SUCCESS;
        switch (hinhThuc) {
            case "THANH_TOAN_ONLINE": return UITheme.ACCENT_TEAL;
            case "CHUYEN_KHOAN": return UITheme.PRIMARY;
            default: return UITheme.SUCCESS;
        }
    }

    private String rutGonHinhThuc(String hinhThuc) {
        if (hinhThuc == null) return "-";
        switch (hinhThuc) {
            case "TIEN_MAT": return "Tiền mặt";
            case "CHUYEN_KHOAN": return "Chuyển khoản";
            case "THANH_TOAN_ONLINE": return "Online";
            default: return hinhThuc;
        }
    }

    private Color phoiMauNhat(Color mau) {
        return new Color(mau.getRed(), mau.getGreen(), mau.getBlue(), 35);
    }

    /**
     * Tu tai toan bo hoa don cua sinh vien (theo maSV truyen vao constructor) roi nap
     * lich su thanh toan tuong ung - dung khi panel nay duoc dat rieng thanh 1 trang
     * "Lich su thanh toan" doc lap trong sidebar, khong phu thuoc panel Tong quan.
     */
    public void tuTaiDuLieu() {
        if (maSV == null || maSV.isBlank()) return;
        SwingWorker<List<HoaDonHocPhi>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<HoaDonHocPhi> doInBackground() throws Exception {
                return new vn.edu.eaut.qlhocphi.bus.CongNoService().layTheoSinhVien(maSV);
            }

            @Override
            protected void done() {
                try {
                    taiDuLieu(get());
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(LichSuThanhToanPanel.this,
                            "Khong the tai danh sach hoa don.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    /** Nap lai lich su thanh toan tu danh sach hoa don hien tai cua sinh vien. */
    public void taiDuLieu(List<HoaDonHocPhi> dsHoaDon) {
        hoaDonTheoMa.clear();
        for (HoaDonHocPhi hd : dsHoaDon) {
            hoaDonTheoMa.put(hd.getMaHoaDon(), hd);
        }

        SwingWorker<List<PhieuThu>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<PhieuThu> doInBackground() throws Exception {
                List<PhieuThu> tatCa = new ArrayList<>();
                for (HoaDonHocPhi hd : dsHoaDon) {
                    tatCa.addAll(phieuThuDAO.layTheoHoaDon(hd.getMaHoaDon()));
                }
                tatCa.sort((a, b) -> {
                    if (a.getNgayNop() == null || b.getNgayNop() == null) return 0;
                    return b.getNgayNop().compareTo(a.getNgayNop());
                });
                return tatCa;
            }

            @Override
            protected void done() {
                try {
                    danhSachHienTai = get();
                    if (trangDayDu) {
                        capNhatTheThongKe(danhSachHienTai);
                        tableModel.fireTableDataChanged();
                        apDungBoLoc();
                    } else {
                        hienThiThe(danhSachHienTai);
                    }
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(LichSuThanhToanPanel.this,
                            "Khong the tai lich su thanh toan.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    /** Xuat bien lai PDF chuyen nghiep cho 1 giao dich va cho nguoi dung chon noi luu file. */
    private void taiBienLai(PhieuThu pt) {
        HoaDonHocPhi hoaDon = hoaDonTheoMa.get(pt.getMaHoaDon());
        String tenHocKy = hoaDon != null ? hoaDon.getTenHocKy() : "";
        java.math.BigDecimal tongHocPhi = hoaDon != null ? hoaDon.getSoTien() : pt.getSoTienNop();
        java.math.BigDecimal conNoConLai = hoaDon != null ? hoaDon.tinhConNo() : java.math.BigDecimal.ZERO;

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Luu bien lai PDF");
        chooser.setSelectedFile(new File("BienLai_" + maSV + "_" + pt.getMaPhieuThu() + ".pdf"));
        int ketQua = chooser.showSaveDialog(this);
        if (ketQua != JFileChooser.APPROVE_OPTION) return;

        String duongDan = chooser.getSelectedFile().getAbsolutePath();
        if (!duongDan.toLowerCase().endsWith(".pdf")) duongDan += ".pdf";
        String duongDanCuoi = duongDan;
        String ngayNopChuoi = pt.getNgayNop() != null ? pt.getNgayNop().format(DMY_HM) : "";

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                PDFExporter.xuatBienLaiChuyenNghiep(
                        duongDanCuoi, TEN_TRUONG, pt.getMaPhieuThu(),
                        hoTenSV, maSV, tenHocKy,
                        tongHocPhi, pt.getSoTienNop(), conNoConLai,
                        pt.getHinhThuc(), pt.getMaGiaoDich(), pt.getNguoiThu(),
                        ngayNopChuoi);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                    UIUtils.thongBao(LichSuThanhToanPanel.this, "Da luu bien lai:\n" + duongDanCuoi);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(LichSuThanhToanPanel.this, "Xuat bien lai that bai.\n" + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private String rootMessage(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }

    /** JPanel biet "bam sat" chieu rong khung cuon (Scrollable) - chi dung o che do gon. */
    private static class KhungTuKhopChieuRong extends JPanel implements Scrollable {
        KhungTuKhopChieuRong(LayoutManager lm) { super(lm); }

        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle r, int huong, int dir) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle r, int huong, int dir) { return 120; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
}