package vn.edu.eaut.qlhocphi.gui.thanhtoan;

import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/** Popup cho sinh vien chon cong thanh toan: VNPay hoac MoMo. */
public class PaymentMethodDialog extends JDialog {

    public PaymentMethodDialog(Window chaMe, int maHoaDon, BigDecimal soTien, Runnable khiThanhCong) {
        super(chaMe, "Chon phuong thuc thanh toan", ModalityType.APPLICATION_MODAL);
        setSize(380, 260);
        setLocationRelativeTo(chaMe);
        setResizable(false);

        JPanel noiDung = new JPanel();
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        noiDung.setBackground(Color.WHITE);

        JLabel lblSoTien = new JLabel("So tien can thanh toan: " + MoneyUtils.format(soTien));
        lblSoTien.setFont(UITheme.FONT_BOLD);
        lblSoTien.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSoTien.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        noiDung.add(lblSoTien);

        JButton btnVNPay = nutCong("VNPAY - Vi/The/QR ngan hang", new Color(0x00, 0x66, 0xB3));
        btnVNPay.addActionListener(e -> {
            dispose();
            new GatewayPaymentDialog(chaMe, maHoaDon, soTien, "VNPAY", khiThanhCong).setVisible(true);
        });

        JButton btnMoMo = nutCong("MOMO - Vi dien tu", new Color(0xAE, 0x2D, 0x8C));
        btnMoMo.addActionListener(e -> {
            dispose();
            new GatewayPaymentDialog(chaMe, maHoaDon, soTien, "MOMO", khiThanhCong).setVisible(true);
        });

        JButton btnHuy = new JButton("Huy");
        btnHuy.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnHuy.setFocusPainted(false);
        btnHuy.addActionListener(e -> dispose());

        noiDung.add(btnVNPay);
        noiDung.add(Box.createRigidArea(new Dimension(0, 10)));
        noiDung.add(btnMoMo);
        noiDung.add(Box.createRigidArea(new Dimension(0, 16)));
        noiDung.add(btnHuy);

        setContentPane(noiDung);
    }

    private JButton nutCong(String text, Color mau) {
        JButton b = new JButton(text);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        b.setFont(UITheme.FONT_BOLD);
        b.setForeground(Color.WHITE);
        b.setBackground(mau);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }
}