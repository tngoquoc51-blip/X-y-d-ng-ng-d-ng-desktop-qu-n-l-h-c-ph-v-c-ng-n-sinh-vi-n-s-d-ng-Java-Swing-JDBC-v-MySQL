package vn.edu.eaut.qlhocphi.gui.congno;

import vn.edu.eaut.qlhocphi.bus.NhacNoTuDongService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.model.CauHinhNhacNoTuDong;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/** Dialog cho Admin dat "Khung thoi gian thu hoc phi" ap dung chung cho toan bo SV. */
public class CauHinhNhacNoDialog extends JDialog {
    private final NhacNoTuDongService nhacNoTuDongService = new NhacNoTuDongService();
    private JSpinner spBatDau, spKetThuc;
    private JLabel lblTrangThaiHienTai;

    public CauHinhNhacNoDialog(Window chaMe, TaiKhoan taiKhoan, Runnable khiThanhCong) {
        super(chaMe, "Cau Hinh Khung Thoi Gian Thu Hoc Phi", ModalityType.APPLICATION_MODAL);
        setSize(460, 340);
        setLocationRelativeTo(chaMe);

        JPanel noiDung = new JPanel();
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        noiDung.setBackground(Color.WHITE);

        lblTrangThaiHienTai = new JLabel("Dang tai cau hinh hien tai...");
        lblTrangThaiHienTai.setFont(UITheme.FONT_BASE);
        lblTrangThaiHienTai.setForeground(UITheme.TEXT_MUTED);
        lblTrangThaiHienTai.setAlignmentX(Component.LEFT_ALIGNMENT);
        noiDung.add(lblTrangThaiHienTai);
        noiDung.add(Box.createRigidArea(new Dimension(0, 16)));

        JLabel lblBatDau = new JLabel("Bat dau thu hoc phi tu:");
        lblBatDau.setFont(UITheme.FONT_BOLD);
        lblBatDau.setAlignmentX(Component.LEFT_ALIGNMENT);
        spBatDau = new JSpinner(new SpinnerDateModel());
        spBatDau.setEditor(new JSpinner.DateEditor(spBatDau, "dd/MM/yyyy HH:mm"));
        spBatDau.setAlignmentX(Component.LEFT_ALIGNMENT);
        spBatDau.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel lblKetThuc = new JLabel("Ket thuc thu hoc phi luc:");
        lblKetThuc.setFont(UITheme.FONT_BOLD);
        lblKetThuc.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        lblKetThuc.setAlignmentX(Component.LEFT_ALIGNMENT);
        spKetThuc = new JSpinner(new SpinnerDateModel());
        spKetThuc.setEditor(new JSpinner.DateEditor(spKetThuc, "dd/MM/yyyy HH:mm"));
        spKetThuc.setAlignmentX(Component.LEFT_ALIGNMENT);
        spKetThuc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        JLabel lblGhiChu = new JLabel("<html><i>Sau thoi diem ket thuc 1 phut: SV chua dong se tu dong nhan email.<br>"
                + "Sau do 1 phut neu van chua dong: Phu huynh se tu dong nhan SMS.</i></html>");
        lblGhiChu.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblGhiChu.setForeground(UITheme.TEXT_MUTED);
        lblGhiChu.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));
        lblGhiChu.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnApDung = UITheme.primaryButton("Ap Dung Khung Thoi Gian Nay");
        btnApDung.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnApDung.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnApDung.setBorder(BorderFactory.createEmptyBorder(18, 0, 8, 0));
        btnApDung.addActionListener(e -> apDung(taiKhoan, khiThanhCong));

        JButton btnTat = UITheme.dangerButton("Tat Nhac No Tu Dong");
        btnTat.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnTat.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnTat.addActionListener(e -> tat(khiThanhCong));

        noiDung.add(lblBatDau);
        noiDung.add(spBatDau);
        noiDung.add(lblKetThuc);
        noiDung.add(spKetThuc);
        noiDung.add(lblGhiChu);
        noiDung.add(btnApDung);
        noiDung.add(btnTat);

        setContentPane(noiDung);
        taiCauHinhHienTai();
    }

    private void taiCauHinhHienTai() {
        try {
            CauHinhNhacNoTuDong hienTai = nhacNoTuDongService.layCauHinhHienTai();
            if (hienTai != null) {
                lblTrangThaiHienTai.setText("<html><b style='color:#0e8a6e'>DANG BAT</b> - Ket thuc: "
                        + hienTai.getNgayGioKetThuc() + "</html>");
                spBatDau.setValue(toDate(hienTai.getNgayGioBatDau()));
                spKetThuc.setValue(toDate(hienTai.getNgayGioKetThuc()));
            } else {
                lblTrangThaiHienTai.setText("Hien chua co khung thoi gian nao duoc bat.");
                spBatDau.setValue(toDate(LocalDateTime.now()));
                spKetThuc.setValue(toDate(LocalDateTime.now().plusDays(7)));
            }
        } catch (Exception ex) {
            lblTrangThaiHienTai.setText("Loi tai cau hinh: " + ex.getMessage());
        }
    }

    private void apDung(TaiKhoan taiKhoan, Runnable khiThanhCong) {
        LocalDateTime batDau = toLocalDateTime((Date) spBatDau.getValue());
        LocalDateTime ketThuc = toLocalDateTime((Date) spKetThuc.getValue());
        if (ketThuc.isBefore(batDau)) {
            JOptionPane.showMessageDialog(this, "Thoi diem ket thuc phai sau thoi diem bat dau.");
            return;
        }
        try {
            CauHinhNhacNoTuDong ch = new CauHinhNhacNoTuDong();
            ch.setNgayGioBatDau(batDau);
            ch.setNgayGioKetThuc(ketThuc);
            ch.setNguoiTao(taiKhoan != null ? taiKhoan.getTenDangNhap() : "he_thong");
            nhacNoTuDongService.datCauHinh(ch);
            JOptionPane.showMessageDialog(this, "Da ap dung khung thoi gian thu hoc phi moi.");
            khiThanhCong.run();
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void tat(Runnable khiThanhCong) {
        try {
            nhacNoTuDongService.tatCauHinh();
            JOptionPane.showMessageDialog(this, "Da tat tinh nang nhac no tu dong.");
            khiThanhCong.run();
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Loi: " + ex.getMessage(), "Loi", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}