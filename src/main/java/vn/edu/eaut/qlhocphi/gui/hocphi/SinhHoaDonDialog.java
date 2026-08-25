package vn.edu.eaut.qlhocphi.gui.hocphi;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

/** Form sinh 1 hoa don hoc phi moi cho sinh vien theo hoc ky. */
public class SinhHoaDonDialog extends JDialog {

    /** Callback khi nguoi dung bam Luu: (maSV, maHocKy, soTinChi, hanThanhToan). */
    public interface Listener {
        void onSinh(String maSV, int maHocKy, int soTinChi, LocalDate hanThanhToan);
    }

    public SinhHoaDonDialog(Frame owner, Listener listener) {
        super(owner, "Sinh hoa don hoc phi", true);
        setSize(420, 400);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JTextField txtMaSV = UIUtils.textField(18);
        JTextField txtMaHocKy = UIUtils.textField(18);
        JTextField txtSoTinChi = UIUtils.textField(18);
        JTextField txtHanThanhToan = UIUtils.textField(18);
        txtHanThanhToan.setToolTipText("Dinh dang: yyyy-MM-dd, VD 2026-01-15");

        themDong(form, "Ma sinh vien", txtMaSV);
        themDong(form, "Ma hoc ky (xem trong tab Hoc ky)", txtMaHocKy);
        themDong(form, "So tin chi dang ky", txtSoTinChi);
        themDong(form, "Han thanh toan (yyyy-MM-dd)", txtHanThanhToan);

        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setOpaque(false);
        JButton btnHuy = UITheme.secondaryButton("Huy");
        JButton btnLuu = UITheme.primaryButton("Sinh hoa don");
        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> {
            try {
                String maSV = txtMaSV.getText().trim();
                int maHocKy = Integer.parseInt(txtMaHocKy.getText().trim());
                int soTinChi = Integer.parseInt(txtSoTinChi.getText().trim());
                LocalDate han = txtHanThanhToan.getText().trim().isEmpty()
                        ? null : LocalDate.parse(txtHanThanhToan.getText().trim());
                if (maSV.isEmpty()) {
                    UIUtils.thongBaoLoi(this, "Vui long nhap ma sinh vien");
                    return;
                }
                listener.onSinh(maSV, maHocKy, soTinChi, han);
                dispose();
            } catch (NumberFormatException ex) {
                UIUtils.thongBaoLoi(this, "Ma hoc ky va so tin chi phai la so nguyen");
            } catch (Exception ex) {
                UIUtils.thongBaoLoi(this, "Dinh dang ngay khong hop le (dung yyyy-MM-dd)");
            }
        });
        actions.add(btnHuy);
        actions.add(btnLuu);
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
}