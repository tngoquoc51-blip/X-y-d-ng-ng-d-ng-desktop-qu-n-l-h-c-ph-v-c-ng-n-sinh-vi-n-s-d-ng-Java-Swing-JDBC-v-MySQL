package vn.edu.eaut.qlhocphi.gui;

import vn.edu.eaut.qlhocphi.bus.SinhVienService;
import vn.edu.eaut.qlhocphi.bus.ThanhToanService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.QuetQRDialog;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.gui.thanhtoan.ThanhToanOnlineDialog;
import vn.edu.eaut.qlhocphi.gui.thanhtoan.VietQRDialog;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.SinhVien;
import vn.edu.eaut.qlhocphi.dal.HoaDonDAO;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;

public class TraCuuFrame extends JFrame {
    private final SinhVienService sinhVienService = new SinhVienService();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final ThanhToanService thanhToanService = new ThanhToanService();

    private JTextField txtMaSV;
    private JButton btnTraCuu;
    private JLabel lblHoTen, lblMaSVKq, lblLop, lblKhoa;
    private JPanel panelThongTin;
    private JTable table;
    private DefaultTableModel tableModel;
    private JButton btnThanhToan;
    private JButton btnXemQR;
    private List<HoaDonHocPhi> danhSachHienTai;

    public TraCuuFrame(JFrame chaMe) {
        setTitle("Tra cuu cong no hoc phi");
        setSize(880, 620);
        setMinimumSize(new Dimension(700, 520));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(chaMe);
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());

        add(buildThanhTren(chaMe), BorderLayout.NORTH);

        JPanel noiDung = new JPanel();
        noiDung.setOpaque(false);
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setBorder(new EmptyBorder(18, 22, 18, 22));

        noiDung.add(buildOTimKiem());
        noiDung.add(Box.createRigidArea(new Dimension(0, 16)));

        panelThongTin = buildThongTinCard();
        panelThongTin.setAlignmentX(Component.LEFT_ALIGNMENT);
        panelThongTin.setVisible(false);
        noiDung.add(panelThongTin);
        noiDung.add(Box.createRigidArea(new Dimension(0, 16)));

        noiDung.add(buildBangHoaDon());

        JScrollPane scroll = new JScrollPane(noiDung);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);
    }

    private JPanel buildThanhTren(JFrame chaMe) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.PRIMARY_DARK);
        panel.setBorder(new EmptyBorder(10, 16, 10, 16));

        JButton btnQuayLai = new JButton("<  Quay lai dang nhap");
        btnQuayLai.setFont(UITheme.FONT_BASE);
        btnQuayLai.setForeground(Color.WHITE);
        btnQuayLai.setBackground(UITheme.PRIMARY_DARK);
        btnQuayLai.setBorderPainted(false);
        btnQuayLai.setFocusPainted(false);
        btnQuayLai.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnQuayLai.addActionListener(e -> {
            dispose();
            if (chaMe != null) chaMe.setVisible(true);
        });
        panel.add(btnQuayLai, BorderLayout.WEST);

        JLabel lblTitle = new JLabel("TRA CUU THONG TIN & CONG NO HOC PHI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitle.setForeground(Color.WHITE);
        panel.add(lblTitle, BorderLayout.CENTER);
        return panel;
    }

    private JPanel buildOTimKiem() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(10, 0));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));

        JLabel lbl = UIUtils.formLabel("Nhap Ma sinh vien:");
        txtMaSV = UIUtils.textField(18);
        btnTraCuu = UITheme.primaryButton("Tra cuu");
        JButton btnQuetQR = UITheme.accentButton("Quet the SV (QR)");
        btnQuetQR.setToolTipText("Dua the sinh vien co ma QR vao camera de tra cuu ngay, khong can go tay");
        btnQuetQR.addActionListener(e -> quetQRTraCuu());

        JPanel trai = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        trai.setOpaque(false);
        trai.add(lbl);
        trai.add(txtMaSV);
        trai.add(btnQuetQR);

        card.add(trai, BorderLayout.CENTER);
        card.add(btnTraCuu, BorderLayout.EAST);

        btnTraCuu.addActionListener(e -> thucHienTraCuu());
        txtMaSV.addActionListener(e -> thucHienTraCuu());
        return card;
    }

    private JPanel buildThongTinCard() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout());
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        JLabel tieuDe = UITheme.sectionLabel("Thong tin ca nhan");
        card.add(tieuDe, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(2, 2, 24, 10));
        grid.setOpaque(false);
        grid.setBorder(new EmptyBorder(14, 0, 0, 0));

        lblHoTen = new JLabel();
        lblMaSVKq = new JLabel();
        lblLop = new JLabel();
        lblKhoa = new JLabel();

        grid.add(oThongTin("Ho va ten", lblHoTen));
        grid.add(oThongTin("Ma sinh vien", lblMaSVKq));
        grid.add(oThongTin("Lop", lblLop));
        grid.add(oThongTin("Khoa", lblKhoa));

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private JPanel oThongTin(String nhan, JLabel giaTri) {
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        JLabel lblNhan = new JLabel(nhan);
        lblNhan.setFont(UITheme.FONT_BASE);
        lblNhan.setForeground(UITheme.TEXT_MUTED);
        giaTri.setFont(UITheme.FONT_BOLD);
        giaTri.setForeground(UITheme.TEXT_PRIMARY);
        box.add(lblNhan);
        box.add(giaTri);
        return box;
    }

    private JPanel buildBangHoaDon() {
        JPanel card = UITheme.card();
        card.setLayout(new BorderLayout(0, 12));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setPreferredSize(new Dimension(100, 320));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 340));

        JPanel dongTieuDe = new JPanel(new BorderLayout());
        dongTieuDe.setOpaque(false);
        dongTieuDe.add(UITheme.sectionLabel("Danh sach hoa don hoc phi"), BorderLayout.WEST);

        JPanel nhomNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        nhomNut.setOpaque(false);

        btnXemQR = UITheme.secondaryButton("Ma QR chuyen khoan");
        btnXemQR.setEnabled(false);
        btnXemQR.addActionListener(e -> moQR());
        nhomNut.add(btnXemQR);

        btnThanhToan = UITheme.primaryButton("Thanh toan online");
        btnThanhToan.setEnabled(false);
        btnThanhToan.addActionListener(e -> moThanhToan());
        nhomNut.add(btnThanhToan);

        dongTieuDe.add(nhomNut, BorderLayout.EAST);
        card.add(dongTieuDe, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Ma HD", "Hoc ky", "So tin chi", "So tien", "Da nop", "Con no", "Trang thai"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        table = new JTable(tableModel);
        UIUtils.styleTable(table);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean coChon = table.getSelectedRow() >= 0;
                btnThanhToan.setEnabled(coChon);
                btnXemQR.setEnabled(coChon);
            }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER, 1, true));
        card.add(scroll, BorderLayout.CENTER);
        return card;
    }

    /** MOI: mo camera quet ma QR tren the sinh vien, tu dong dien Ma SV va tra cuu ngay. */
    private void quetQRTraCuu() {
        QuetQRDialog dlg = new QuetQRDialog(this, "Quet the sinh vien de tra cuu");
        dlg.setVisible(true);
        String maSV = dlg.layKetQua();
        if (maSV != null && !maSV.isBlank()) {
            txtMaSV.setText(maSV.trim());
            thucHienTraCuu();
        }
    }

    private void thucHienTraCuu() {
        String maSV = txtMaSV.getText().trim();
        if (maSV.isEmpty()) {
            UIUtils.thongBaoLoi(this, "Vui long nhap ma sinh vien");
            return;
        }

        btnTraCuu.setEnabled(false);
        SwingWorker<Object[], Void> worker = new SwingWorker<>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                SinhVien sv = sinhVienService.timTheoMa(maSV);
                List<HoaDonHocPhi> ds = sv != null ? hoaDonDAO.layTheoSinhVien(maSV) : null;
                return new Object[]{sv, ds};
            }

            @Override
            protected void done() {
                btnTraCuu.setEnabled(true);
                try {
                    Object[] result = get();
                    SinhVien sv = (SinhVien) result[0];
                    if (sv == null) {
                        panelThongTin.setVisible(false);
                        tableModel.setRowCount(0);
                        UIUtils.thongBaoLoi(TraCuuFrame.this, "Khong tim thay sinh vien co ma: " + maSV);
                        return;
                    }
                    hienThiThongTin(sv);
                    //noinspection unchecked
                    hienThiHoaDon((List<HoaDonHocPhi>) result[1]);
                } catch (Exception ex) {
                    UIUtils.thongBaoLoi(TraCuuFrame.this, "Loi tra cuu: " + rootMessage(ex));
                }
            }
        };
        worker.execute();
    }

    private void hienThiThongTin(SinhVien sv) {
        lblHoTen.setText(sv.getHoTen());
        lblMaSVKq.setText(sv.getMaSV());
        lblLop.setText(sv.getLop() == null ? "-" : sv.getLop());
        lblKhoa.setText(sv.getKhoa() == null ? "-" : sv.getKhoa());
        panelThongTin.setVisible(true);
    }

    private void hienThiHoaDon(List<HoaDonHocPhi> danhSach) {
        danhSachHienTai = danhSach;
        tableModel.setRowCount(0);
        if (danhSach == null) return;
        for (HoaDonHocPhi hd : danhSach) {
            tableModel.addRow(new Object[]{
                    hd.getMaHoaDon(),
                    hd.getTenHocKy(),
                    hd.getSoTinChi(),
                    MoneyUtils.format(hd.getSoTien()),
                    MoneyUtils.format(hd.getDaNop()),
                    MoneyUtils.format(hd.tinhConNo()),
                    hd.tinhTrangThai()
            });
        }
        btnThanhToan.setEnabled(false);
        btnXemQR.setEnabled(false);
    }

    private void moQR() {
        int row = table.getSelectedRow();
        if (row < 0 || danhSachHienTai == null || row >= danhSachHienTai.size()) return;
        HoaDonHocPhi hd = danhSachHienTai.get(row);
        BigDecimal conNo = hd.tinhConNo();
        if (conNo.compareTo(BigDecimal.ZERO) <= 0) {
            UIUtils.thongBao(this, "Hoa don nay da duoc dong du, khong can chuyen khoan them.");
            return;
        }
        new VietQRDialog(this, hd, false, null).setVisible(true);
    }

    private void moThanhToan() {
        int row = table.getSelectedRow();
        if (row < 0 || danhSachHienTai == null || row >= danhSachHienTai.size()) return;
        HoaDonHocPhi hd = danhSachHienTai.get(row);
        BigDecimal conNo = hd.tinhConNo();
        if (conNo.compareTo(BigDecimal.ZERO) <= 0) {
            UIUtils.thongBao(this, "Hoa don nay da duoc dong du, khong can thanh toan them.");
            return;
        }
        new ThanhToanOnlineDialog(this, thanhToanService).setVisible(true);
        thucHienTraCuu();
    }

    private String rootMessage(Exception ex) {
        Throwable t = ex.getCause() != null ? ex.getCause() : ex;
        return t.getMessage() != null ? t.getMessage() : t.toString();
    }
}
