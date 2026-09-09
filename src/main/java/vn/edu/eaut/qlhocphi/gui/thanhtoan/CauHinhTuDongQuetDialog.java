package vn.edu.eaut.qlhocphi.gui.thanhtoan;

import vn.edu.eaut.qlhocphi.bus.TuDongQuetScheduler;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/** Admin cau hinh: bat/tat + chon ngay-gio bat dau + chu ky quet lai (phut). */
public class CauHinhTuDongQuetDialog extends JDialog {
    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

    public CauHinhTuDongQuetDialog(Window owner, Runnable sauKhiLuu) {
        super(owner, "Cấu hình tự động quét thu học phí", ModalityType.APPLICATION_MODAL);
        setSize(440, 430);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UITheme.BG_MAIN);
        setLayout(new BorderLayout());

        TuDongQuetScheduler scheduler = TuDongQuetScheduler.getInstance();

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        JCheckBox chkBat = new JCheckBox("Bật tự động quét thu học phí");
        chkBat.setFont(UITheme.FONT_BOLD);
        chkBat.setOpaque(false);
        chkBat.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkBat.setSelected(scheduler.dangBat());

        JTextField txtNgay = UIUtils.textField(18);
        JTextField txtGio = UIUtils.textField(18);
        JSpinner spnChuKy = new JSpinner(new SpinnerNumberModel(
                scheduler.dangBat() ? scheduler.layChuKyPhut() : 60, 1, 1440, 1));
        spnChuKy.setFont(UITheme.FONT_BASE);
        spnChuKy.setAlignmentX(Component.LEFT_ALIGNMENT);
        spnChuKy.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        LocalDateTime macDinh = scheduler.dangBat() && scheduler.layThoiDiemBatDau() != null
                ? scheduler.layThoiDiemBatDau() : LocalDateTime.now();
        txtNgay.setText(macDinh.toLocalDate().format(DMY));
        txtGio.setText(macDinh.toLocalTime().format(HM));

        JLabel lblGhiChu = new JLabel(
                "<html><body style='width:340px;color:#6B7480;font-size:11px'>"
                        + "Kể từ thời điểm này, hệ thống sẽ tự động quét và trừ tiền Ví điện tử "
                        + "học phí của sinh viên có đủ số dư - không cần bấm 'Quét Thu Ngay' thủ công nữa."
                        + "</body></html>");
        lblGhiChu.setAlignmentX(Component.LEFT_ALIGNMENT);

        form.add(chkBat);
        form.add(Box.createRigidArea(new Dimension(0, 14)));
        themDong(form, "Ngày bắt đầu (dd/MM/yyyy)", txtNgay);
        themDong(form, "Giờ bắt đầu (HH:mm)", txtGio);
        themDong(form, "Chu kỳ quét lại (phút)", spnChuKy);
        form.add(lblGhiChu);

        JLabel lblTrangThai = new JLabel(scheduler.moTaTrangThai());
        lblTrangThai.setFont(UITheme.FONT_BASE);
        lblTrangThai.setForeground(UITheme.TEXT_MUTED);
        lblTrangThai.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTrangThai.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        form.add(lblTrangThai);

        add(form, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        actions.setOpaque(false);
        JButton btnHuy = UITheme.secondaryButton("Hủy");
        JButton btnLuu = UITheme.primaryButton("Lưu cấu hình");
        btnHuy.addActionListener(e -> dispose());
        btnLuu.addActionListener(e -> {
            if (!chkBat.isSelected()) {
                scheduler.dungLai();
                UIUtils.thongBao(this, "Đã tắt tự động quét thu học phí.");
                dispose();
                if (sauKhiLuu != null) sauKhiLuu.run();
                return;
            }
            try {
                LocalDate ngay = LocalDate.parse(txtNgay.getText().trim(), DMY);
                LocalTime gio = LocalTime.parse(txtGio.getText().trim(), HM);
                int chuKy = (int) spnChuKy.getValue();
                LocalDateTime thoiDiem = LocalDateTime.of(ngay, gio);

                scheduler.batDau(thoiDiem, chuKy);
                UIUtils.thongBao(this, "Đã bật tự động quét, bắt đầu từ "
                        + thoiDiem.format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"))
                        + ", mỗi " + chuKy + " phút quét lại 1 lần.");
                dispose();
                if (sauKhiLuu != null) sauKhiLuu.run();
            } catch (DateTimeParseException ex) {
                UIUtils.thongBaoLoi(this, "Ngày phải đúng dd/MM/yyyy và giờ phải đúng HH:mm (ví dụ 08:00).");
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