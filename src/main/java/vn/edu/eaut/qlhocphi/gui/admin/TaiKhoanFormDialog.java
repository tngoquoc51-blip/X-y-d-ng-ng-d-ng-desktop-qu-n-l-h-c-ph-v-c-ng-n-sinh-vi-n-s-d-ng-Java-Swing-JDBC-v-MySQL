package vn.edu.eaut.qlhocphi.gui.admin;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.VaiTro;

import javax.swing.*;
import java.awt.*;

/** Form them/sua tai khoan he thong. Khi them moi: nhap mat khau. Khi sua: khong doi mat khau o day. */
public class TaiKhoanFormDialog extends JDialog {

    public interface ThemListener {
        void onThem(String tenDangNhap, String matKhau, String hoTen, VaiTro vaiTro, String maSV, String googleEmail);
    }

    public interface SuaListener {
        void onSua(int maTK, String hoTen, VaiTro vaiTro, String maSV, String googleEmail, boolean trangThai);
    }

    public TaiKhoanFormDialog(Frame owner, TaiKhoan suaTK, ThemListener themListener, SuaListener suaListener) {
        super(owner, suaTK == null ? "Them tai khoan" : "Sua tai khoan", true);
        setSize(440, 560);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JTextField txtTenDangNhap = UIUtils.textField(20);
        JPasswordField txtMatKhau = new JPasswordField(20);
        txtMatKhau.setFont(UITheme.FONT_BASE);
        txtMatKhau.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        JTextField txtHoTen = UIUtils.textField(20);
        JComboBox<VaiTro> cboVaiTro = new JComboBox<>(VaiTro.values());
        cboVaiTro.setFont(UITheme.FONT_BASE);
        JTextField txtMaSV = UIUtils.textField(20);
        JTextField txtGoogleEmail = UIUtils.textField(20);
        JCheckBox chkHoatDong = new JCheckBox("Tai khoan dang hoat dong");
        chkHoatDong.setOpaque(false);
        chkHoatDong.setFont(UITheme.FONT_BASE);
        chkHoatDong.setSelected(true);
        chkHoatDong.setAlignmentX(Component.LEFT_ALIGNMENT);

        boolean laSua = suaTK != null;
        if (laSua) {
            txtTenDangNhap.setText(suaTK.getTenDangNhap());
            txtTenDangNhap.setEditable(false);
            txtHoTen.setText(suaTK.getHoTen());
            cboVaiTro.setSelectedItem(suaTK.getVaiTro());
            txtMaSV.setText(suaTK.getMaSV());
            txtGoogleEmail.setText(suaTK.getGoogleEmail());
            chkHoatDong.setSelected(suaTK.isTrangThai());
        }

        themDong(form, "Ten dang nhap", txtTenDangNhap);
        if (!laSua) {
            themDong(form, "Mat khau (toi thieu 6 ky tu)", txtMatKhau);
        }
        themDong(form, "Ho ten", txtHoTen);
        themDong(form, "Vai tro", cboVaiTro);
        themDong(form, "Ma sinh vien (chi can neu Vai tro = SINHVIEN)", txtMaSV);
        themDong(form, "Gmail dang nhap / khoi phuc mat khau (tuy chon)", txtGoogleEmail);

        JLabel ghiChuGmail = new JLabel(
                "<html><i>Sinh vien co the dang nhap hoac lay lai mat khau bang Gmail nay.</i></html>");
        ghiChuGmail.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        ghiChuGmail.setForeground(UITheme.TEXT_MUTED);
        ghiChuGmail.setAlignmentX(Component.LEFT_ALIGNMENT);
        ghiChuGmail.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        form.add(ghiChuGmail);

        if (laSua) {
            form.add(chkHoatDong);
            form.add(Box.createRigidArea(new Dimension(0, 10)));
        }

        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setOpaque(false);
        JButton btnHuy = UITheme.secondaryButton("Huy");
        JButton btnLuu = UITheme.primaryButton("Luu");
        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> {
            String hoTen = txtHoTen.getText().trim();
            VaiTro vaiTro = (VaiTro) cboVaiTro.getSelectedItem();
            String maSV = txtMaSV.getText().trim();
            String googleEmail = txtGoogleEmail.getText().trim();

            if (laSua) {
                suaListener.onSua(suaTK.getMaTK(), hoTen, vaiTro, maSV.isEmpty() ? null : maSV,
                        googleEmail.isEmpty() ? null : googleEmail, chkHoatDong.isSelected());
            } else {
                String tenDangNhap = txtTenDangNhap.getText().trim();
                String matKhau = new String(txtMatKhau.getPassword());
                themListener.onThem(tenDangNhap, matKhau, hoTen, vaiTro, maSV.isEmpty() ? null : maSV,
                        googleEmail.isEmpty() ? null : googleEmail);
            }
            dispose();
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