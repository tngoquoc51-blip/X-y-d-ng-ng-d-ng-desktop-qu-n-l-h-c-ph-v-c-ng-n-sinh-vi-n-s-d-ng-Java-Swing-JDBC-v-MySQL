package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.model.SinhVien;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

/** Form (dialog) them/sua thong tin sinh vien. */
public class SinhVienFormDialog extends JDialog {
    private static final DateTimeFormatter DINH_DANG_NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public SinhVienFormDialog(Frame owner, SinhVien suaSV, Consumer<SinhVien> onSave) {
        super(owner, suaSV == null ? "Them sinh vien" : "Sua sinh vien", true);
        setSize(420, 640);
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
        JTextField txtNgaySinh = UIUtils.textField(20);
        txtNgaySinh.setToolTipText("Dinh dang: dd/MM/yyyy, VD 15/03/2005");
        JTextField txtQueQuan = UIUtils.textField(20);
        JTextField txtDiaChi = UIUtils.textField(20);
        JTextField txtEmail = UIUtils.textField(20);
        JTextField txtSDT = UIUtils.textField(20);
        JTextField txtSDTPhuHuynh = UIUtils.textField(20);

        if (suaSV != null) {
            txtMaSV.setText(suaSV.getMaSV());
            txtMaSV.setEditable(false);
            txtHoTen.setText(suaSV.getHoTen());
            txtLop.setText(suaSV.getLop());
            txtKhoa.setText(suaSV.getKhoa());
            if (suaSV.getNgaySinh() != null) txtNgaySinh.setText(suaSV.getNgaySinh().format(DINH_DANG_NGAY));
            txtQueQuan.setText(suaSV.getQueQuan());
            txtDiaChi.setText(suaSV.getDiaChi());
            txtEmail.setText(suaSV.getEmail());
            txtSDT.setText(suaSV.getSoDienThoai());
            txtSDTPhuHuynh.setText(suaSV.getSoDienThoaiPhuHuynh());
        }
        addField(form, "Ma sinh vien", txtMaSV);
        addField(form, "Ho ten", txtHoTen);
        addField(form, "Lop", txtLop);
        addField(form, "Khoa", txtKhoa);
        addField(form, "Ngay sinh (dd/MM/yyyy)", txtNgaySinh);
        addField(form, "Que quan", txtQueQuan);
        addField(form, "Dia chi", txtDiaChi);
        addField(form, "Email", txtEmail);
        addField(form, "So dien thoai", txtSDT);
        addField(form, "SDT phu huynh (nhan SMS khi qua han lau)", txtSDTPhuHuynh);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(14);
        add(scroll, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setOpaque(false);
        JButton btnHuy = UITheme.secondaryButton("Huy");
        JButton btnLuu = UITheme.primaryButton("Luu");
        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> {
            String maSV = txtMaSV.getText().trim();
            String hoTen = txtHoTen.getText().trim();
            if (maSV.isEmpty() || hoTen.isEmpty()) {
                UIUtils.thongBaoLoi(this, "Vui long nhap Ma sinh vien va Ho ten.");
                return;
            }

            String ngaySinhText = txtNgaySinh.getText().trim();
            LocalDate ngaySinh = null;
            if (!ngaySinhText.isEmpty()) {
                try {
                    ngaySinh = LocalDate.parse(ngaySinhText, DINH_DANG_NGAY);
                } catch (DateTimeParseException ex) {
                    UIUtils.thongBaoLoi(this, "Ngay sinh khong dung dinh dang dd/MM/yyyy (VD: 15/03/2005).");
                    return;
                }
            }

            SinhVien sv = suaSV != null ? suaSV : new SinhVien();
            sv.setMaSV(maSV);
            sv.setHoTen(hoTen);
            sv.setLop(txtLop.getText().trim());
            sv.setKhoa(txtKhoa.getText().trim());
            sv.setNgaySinh(ngaySinh);
            sv.setQueQuan(txtQueQuan.getText().trim());
            sv.setDiaChi(txtDiaChi.getText().trim());
            sv.setEmail(txtEmail.getText().trim());
            sv.setSoDienThoai(txtSDT.getText().trim());
            sv.setSoDienThoaiPhuHuynh(txtSDTPhuHuynh.getText().trim());
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