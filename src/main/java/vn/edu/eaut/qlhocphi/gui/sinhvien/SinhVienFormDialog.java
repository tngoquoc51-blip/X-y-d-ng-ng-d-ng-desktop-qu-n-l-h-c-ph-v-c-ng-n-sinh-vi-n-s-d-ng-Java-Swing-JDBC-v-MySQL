package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.SinhVien;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

/** Form (dialog) them/sua thong tin sinh vien. */
public class SinhVienFormDialog extends JDialog {

    public SinhVienFormDialog(Frame owner, SinhVien suaSV, Consumer<SinhVien> onSave) {
        super(owner, suaSV == null ? "Them sinh vien" : "Sua sinh vien", true);
        setSize(420, 460);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JTextField txtMaSV = UIUtils.textField(20);
        JTextField txtHoTen = UIUtils.textField(20);
        JTextField txtLop = UIUtils.textField(20);
        JTextField txtKhoa = UIUtils.textField(20);
        JTextField txtEmail = UIUtils.textField(20);
        JTextField txtSDT = UIUtils.textField(20);

        if (suaSV != null) {
            txtMaSV.setText(suaSV.getMaSV());
            txtMaSV.setEditable(false);
            txtHoTen.setText(suaSV.getHoTen());
            txtLop.setText(suaSV.getLop());
            txtKhoa.setText(suaSV.getKhoa());
            txtEmail.setText(suaSV.getEmail());
            txtSDT.setText(suaSV.getSoDienThoai());
        }

        addField(form, "Ma sinh vien", txtMaSV);
        addField(form, "Ho ten", txtHoTen);
        addField(form, "Lop", txtLop);
        addField(form, "Khoa", txtKhoa);
        addField(form, "Email", txtEmail);
        addField(form, "So dien thoai", txtSDT);

        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setOpaque(false);
        JButton btnHuy = UITheme.secondaryButton("Huy");
        JButton btnLuu = UITheme.primaryButton("Luu");
        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> {
            SinhVien sv = suaSV != null ? suaSV : new SinhVien();
            sv.setMaSV(txtMaSV.getText().trim());
            sv.setHoTen(txtHoTen.getText().trim());
            sv.setLop(txtLop.getText().trim());
            sv.setKhoa(txtKhoa.getText().trim());
            sv.setEmail(txtEmail.getText().trim());
            sv.setSoDienThoai(txtSDT.getText().trim());
            onSave.accept(sv);
            dispose();
        });
        actions.add(btnHuy);
        actions.add(btnLuu);
        add(actions, BorderLayout.SOUTH);
    }

    private void addField(JPanel form, String label, JComponent field) {
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
