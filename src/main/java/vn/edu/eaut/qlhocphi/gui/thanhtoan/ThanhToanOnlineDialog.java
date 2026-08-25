package vn.edu.eaut.qlhocphi.gui.thanhtoan;

import vn.edu.eaut.qlhocphi.bus.ThanhToanService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.payment.PaymentResult;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Dialog mo phong thanh toan online.
 * Goi ThanhToanService.thanhToanOnline(...) trong SwingWorker de khong treo giao dien
 * trong luc "cho cong thanh toan xu ly".
 */
public class ThanhToanOnlineDialog extends JDialog {
    private final ThanhToanService thanhToanService;

    private JTextField txtMaHoaDon, txtSoTien;
    private JLabel lblTrangThai;
    private JButton btnThanhToan;

    public ThanhToanOnlineDialog(Frame owner, ThanhToanService thanhToanService) {
        this(owner, thanhToanService, null, null);
    }

    /**
     * Cho phep dien san Ma hoa don + So tien con no (VD: bam "Thanh toan ngay" tu 1 dong
     * hoa don cu the) de nguoi dung khong phai tu go tay. Ma hoa don se bi khoa (chi doc)
     * khi da duoc truyen san de tranh nham lan sang hoa don khac.
     */
    public ThanhToanOnlineDialog(Frame owner, ThanhToanService thanhToanService,
                                 Integer maHoaDonMacDinh, java.math.BigDecimal soTienMacDinh) {
        super(owner, "Thanh toan online", true);
        this.thanhToanService = thanhToanService;
        setSize(420, 340);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 10, 24));

        txtMaHoaDon = UIUtils.textField(18);
        txtSoTien = UIUtils.textField(18);
        if (maHoaDonMacDinh != null) {
            txtMaHoaDon.setText(String.valueOf(maHoaDonMacDinh));
            txtMaHoaDon.setEditable(false);
        }
        if (soTienMacDinh != null) {
            txtSoTien.setText(soTienMacDinh.toPlainString());
        }

        themDong(form, "Ma hoa don", txtMaHoaDon);
        themDong(form, "So tien thanh toan (VND)", txtSoTien);

        lblTrangThai = new JLabel(" ");
        lblTrangThai.setFont(UITheme.FONT_BASE);
        lblTrangThai.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(lblTrangThai);

        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setOpaque(false);
        JButton btnDong = UITheme.secondaryButton("Dong");
        btnThanhToan = UITheme.primaryButton("Xac nhan thanh toan");
        btnDong.addActionListener(e -> dispose());
        btnThanhToan.addActionListener(e -> thucHienThanhToan());
        actions.add(btnDong);
        actions.add(btnThanhToan);
        add(actions, BorderLayout.SOUTH);
    }

    private void themDong(JPanel form, String label, JComponent field) {
        JLabel l = UIUtils.formLabel(label);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        form.add(l);
        form.add(Box.createRigidArea(new Dimension(0, 4)));
        form.add(field);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
    }

    private void thucHienThanhToan() {
        int maHoaDon;
        BigDecimal soTien;
        try {
            maHoaDon = Integer.parseInt(txtMaHoaDon.getText().trim());
            soTien = new BigDecimal(txtSoTien.getText().trim());
        } catch (NumberFormatException ex) {
            UIUtils.thongBaoLoi(this, "Vui long nhap dung dinh dang so");
            return;
        }

        final int maHoaDonFinal = maHoaDon;
        final BigDecimal soTienFinal = soTien;

        btnThanhToan.setEnabled(false);
        lblTrangThai.setForeground(UITheme.TEXT_MUTED);
        lblTrangThai.setText("Dang ket noi cong thanh toan...");

        SwingWorker<PaymentResult, Void> worker = new SwingWorker<>() {
            @Override
            protected PaymentResult doInBackground() throws Exception {
                return thanhToanService.thanhToanOnline(maHoaDonFinal, soTienFinal);
            }

            @Override
            protected void done() {
                btnThanhToan.setEnabled(true);
                try {
                    PaymentResult result = get();
                    if (result.isThanhCong()) {
                        lblTrangThai.setForeground(UITheme.SUCCESS);
                        lblTrangThai.setText("Thanh cong! Ma giao dich: " + result.getMaGiaoDich());
                    } else {
                        lblTrangThai.setForeground(UITheme.DANGER);
                        lblTrangThai.setText("That bai: " + result.getThongDiep());
                    }
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                    lblTrangThai.setForeground(UITheme.DANGER);
                    lblTrangThai.setText(cause.getMessage());
                }
            }
        };
        worker.execute();
    }
}