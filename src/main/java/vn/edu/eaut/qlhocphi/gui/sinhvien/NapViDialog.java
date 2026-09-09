package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.bus.ViDienTuService;
import vn.edu.eaut.qlhocphi.config.UITheme;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.UUID;

/**
 * Nap tien vao Vi hoc phi dien tu. Mo phong xac nhan thanh toan ngay (giong
 * MockPaymentGateway dang dung o ThanhToanService) - de thay bang VNPay/MoMo that
 * sau nay, chi can doi than xu ly nut "Xac Nhan Nap" de goi GatewayPaymentDialog.
 */
public class NapViDialog extends JDialog {
    private final ViDienTuService viDienTuService = new ViDienTuService();
    private final Window chaMeGoc;
    private JTextField txtSoTien;
    private JComboBox<String> cboHinhThuc;

    public NapViDialog(Window chaMe, String maSV, Runnable khiThanhCong) {
        super(chaMe, "Nạp Tiền Vào Ví", ModalityType.APPLICATION_MODAL);
        this.chaMeGoc = chaMe;
        setSize(380, 260);
        setLocationRelativeTo(chaMe);
        setResizable(false);

        JPanel noiDung = new JPanel();
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        noiDung.setBackground(Color.WHITE);

        JLabel lblSoTien = new JLabel("Số tiền muốn nạp (VNĐ):");
        lblSoTien.setFont(UITheme.FONT_BOLD);
        lblSoTien.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtSoTien = new JTextField();
        txtSoTien.setAlignmentX(Component.LEFT_ALIGNMENT);
        txtSoTien.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblHinhThuc = new JLabel("Hình thức nạp:");
        lblHinhThuc.setFont(UITheme.FONT_BOLD);
        lblHinhThuc.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblHinhThuc.setBorder(BorderFactory.createEmptyBorder(14, 0, 0, 0));

        cboHinhThuc = new JComboBox<>(new String[]{"VNPAY", "MOMO", "TIEN_MAT"});
        cboHinhThuc.setAlignmentX(Component.LEFT_ALIGNMENT);
        cboHinhThuc.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        JLabel lblGhiChu = new JLabel("<html><i>VNPAY/MOMO: mở trình duyệt sang cổng Sandbox thật. Tiền mặt: ghi nhận ngay.</i></html>");
        lblGhiChu.setFont(UITheme.FONT_BASE);
        lblGhiChu.setForeground(UITheme.TEXT_MUTED);
        lblGhiChu.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblGhiChu.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton btnXacNhan = UITheme.primaryButton("Xác Nhận Nạp");
        btnXacNhan.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnXacNhan.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnXacNhan.setBorder(BorderFactory.createEmptyBorder(18, 0, 0, 0));
        btnXacNhan.addActionListener(e -> napTien(maSV, khiThanhCong));

        noiDung.add(lblSoTien);
        noiDung.add(txtSoTien);
        noiDung.add(lblHinhThuc);
        noiDung.add(cboHinhThuc);
        noiDung.add(lblGhiChu);
        noiDung.add(btnXacNhan);

        setContentPane(noiDung);
    }

    private void napTien(String maSV, Runnable khiThanhCong) {
        BigDecimal soTien;
        try {
            soTien = new BigDecimal(txtSoTien.getText().trim().replaceAll("[^0-9]", ""));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Số tiền không hợp lệ.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (soTien.compareTo(BigDecimal.ZERO) <= 0) {
            JOptionPane.showMessageDialog(this, "Số tiền phải lớn hơn 0.", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String hinhThuc = (String) cboHinhThuc.getSelectedItem();

        if ("VNPAY".equals(hinhThuc) || "MOMO".equals(hinhThuc)) {
            // Nap qua cong that (VNPay Sandbox / MoMo Test) - mo trinh duyet, cho callback
            dispose();
            new NapViGatewayDialog(chaMeGoc, maSV, soTien, hinhThuc, khiThanhCong).setVisible(true);
            return;
        }

        // TIEN_MAT: khong co cong thanh toan online - ghi nhan ngay (giong nop tien mat truc tiep)
        String maGiaoDichCong = "TM-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        try {
            viDienTuService.napTien(maSV, soTien, hinhThuc, maGiaoDichCong);
            JOptionPane.showMessageDialog(this, "Nạp tiền thành công!");
            khiThanhCong.run();
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
}