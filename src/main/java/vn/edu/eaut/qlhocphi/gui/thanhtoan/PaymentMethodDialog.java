package vn.edu.eaut.qlhocphi.gui.thanhtoan;

import vn.edu.eaut.qlhocphi.bus.ViDienTuService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.gui.common.UIUtils;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;

/**
 * Popup cho sinh vien chon cong thanh toan: VNPay, MoMo, hoac tru truc tiep
 * tu so du Vi hoc phi dien tu cua chinh minh (MOI - khong can qua cong ngoai,
 * tru tien ngay lap tuc neu vi du so du).
 */
public class PaymentMethodDialog extends JDialog {

    /** Constructor cu (khong co Vi hoc phi) - giu lai de tuong thich nguoc neu noi khac dang goi. */
    public PaymentMethodDialog(Window chaMe, int maHoaDon, BigDecimal soTien, Runnable khiThanhCong) {
        this(chaMe, null, maHoaDon, soTien, khiThanhCong);
    }

    /**
     * @param maSV neu khac null se hien them nut "Vi hoc phi" voi so du hien tai;
     *             neu null (khong biet Ma SV) thi chi hien VNPay/MoMo nhu cu.
     */
    public PaymentMethodDialog(Window chaMe, String maSV, int maHoaDon, BigDecimal soTien, Runnable khiThanhCong) {
        super(chaMe, "Chon phuong thuc thanh toan", ModalityType.APPLICATION_MODAL);
        setSize(380, maSV != null ? 330 : 260);
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

        if (maSV != null && !maSV.isBlank()) {
            noiDung.add(nutViHocPhi(chaMe, maSV, maHoaDon, soTien, khiThanhCong));
            noiDung.add(Box.createRigidArea(new Dimension(0, 10)));
        }

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

    /** Nut "Vi hoc phi" - tu load so du hien tai, tru ngay khi bam neu du tien. */
    private JButton nutViHocPhi(Window chaMe, String maSV, int maHoaDon, BigDecimal soTien, Runnable khiThanhCong) {
        JButton btn = nutCong("VI HOC PHI - Dang tai so du...", new Color(0x0E, 0xA5, 0x69));
        btn.setEnabled(false);

        ViDienTuService viDienTuService = new ViDienTuService();
        SwingWorker<BigDecimal, Void> worker = new SwingWorker<>() {
            @Override
            protected BigDecimal doInBackground() throws Exception {
                return viDienTuService.laySoDu(maSV);
            }

            @Override
            protected void done() {
                try {
                    BigDecimal soDu = get();
                    boolean duTien = soDu.compareTo(soTien) >= 0;
                    btn.setText("VI HOC PHI - So du: " + MoneyUtils.format(soDu));
                    btn.setEnabled(duTien);
                    if (!duTien) {
                        btn.setToolTipText("So du khong du (can them " + MoneyUtils.format(soTien.subtract(soDu)) + ") - vao 'Vi hoc phi' de nap them.");
                    }
                } catch (Exception ex) {
                    btn.setText("VI HOC PHI - Khong tai duoc so du");
                }
            }
        };
        worker.execute();

        btn.addActionListener(e -> {
            btn.setEnabled(false);
            btn.setText("Dang xu ly...");
            SwingWorker<Boolean, Void> thanhToan = new SwingWorker<>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return viDienTuService.thanhToanHoaDonBangVi(maSV, maHoaDon, soTien);
                }

                @Override
                protected void done() {
                    try {
                        boolean thanhCong = get();
                        if (thanhCong) {
                            UIUtils.thongBao(PaymentMethodDialog.this,
                                    "Da thanh toan " + MoneyUtils.format(soTien) + " bang Vi hoc phi dien tu.");
                            dispose();
                            if (khiThanhCong != null) khiThanhCong.run();
                        } else {
                            UIUtils.thongBaoLoi(PaymentMethodDialog.this,
                                    "So du Vi hoc phi khong du. Vui long nap them tien vao vi.");
                            btn.setEnabled(true);
                            btn.setText("VI HOC PHI - Thu lai");
                        }
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        UIUtils.thongBaoLoi(PaymentMethodDialog.this, "Loi: " + cause.getMessage());
                        btn.setEnabled(true);
                        btn.setText("VI HOC PHI - Thu lai");
                    }
                }
            };
            thanhToan.execute();
        });

        return btn;
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
