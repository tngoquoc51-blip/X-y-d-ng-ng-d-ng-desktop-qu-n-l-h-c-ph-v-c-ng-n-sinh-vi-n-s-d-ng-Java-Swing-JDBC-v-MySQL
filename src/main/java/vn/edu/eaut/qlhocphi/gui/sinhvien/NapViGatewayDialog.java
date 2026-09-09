package vn.edu.eaut.qlhocphi.gui.sinhvien;

import vn.edu.eaut.qlhocphi.bus.ViDienTuService;
import vn.edu.eaut.qlhocphi.config.UITheme;
import vn.edu.eaut.qlhocphi.payment.gateway.GatewayCallbackServer;
import vn.edu.eaut.qlhocphi.payment.gateway.MoMoGateway;
import vn.edu.eaut.qlhocphi.payment.gateway.VNPayGateway;

import javax.swing.*;
import java.awt.*;
import java.awt.Desktop;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

/**
 * Dialog nap tien vao Vi hoc phi dien tu qua VNPay Sandbox / MoMo Test Environment
 * that (khong mo phong) - mo trinh duyet, lang nghe callback qua GatewayCallbackServer
 * dung chung voi luong thanh toan hoa don, tu dong cong tien vao vi khi thanh cong.
 */
public class NapViGatewayDialog extends JDialog {
    private final ViDienTuService viDienTuService = new ViDienTuService();
    private JLabel lblTrangThai;
    private boolean daXuLyXong = false;

    public NapViGatewayDialog(Window chaMe, String maSV, BigDecimal soTien, String loaiCong, Runnable khiThanhCong) {
        super(chaMe, "Nap vi qua " + loaiCong, ModalityType.APPLICATION_MODAL);
        setSize(420, 220);
        setLocationRelativeTo(chaMe);
        setResizable(false);
        setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        JPanel noiDung = new JPanel();
        noiDung.setLayout(new BoxLayout(noiDung, BoxLayout.Y_AXIS));
        noiDung.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        noiDung.setBackground(Color.WHITE);

        lblTrangThai = new JLabel("Dang tao lien ket thanh toan...");
        lblTrangThai.setFont(UITheme.FONT_BASE);
        lblTrangThai.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblGhiChu = new JLabel("<html><div style='text-align:center'>Trinh duyet se tu mo. Sau khi hoan tat<br>thanh toan tren " + loaiCong + " Sandbox, quay lai day -<br>ung dung se tu dong nap tien vao vi.</div></html>");
        lblGhiChu.setFont(UITheme.FONT_BASE);
        lblGhiChu.setForeground(UITheme.TEXT_MUTED);
        lblGhiChu.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblGhiChu.setBorder(BorderFactory.createEmptyBorder(12, 0, 16, 0));

        JButton btnHuy = new JButton("Huy giao dich");
        btnHuy.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnHuy.setFocusPainted(false);
        btnHuy.addActionListener(e -> dongVaDungServer());

        noiDung.add(lblTrangThai);
        noiDung.add(lblGhiChu);
        noiDung.add(btnHuy);
        setContentPane(noiDung);

        batDauThanhToan(maSV, soTien, loaiCong, khiThanhCong);
    }

    private void batDauThanhToan(String maSV, BigDecimal soTien, String loaiCong, Runnable khiThanhCong) {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                GatewayCallbackServer.start();

                if ("VNPAY".equals(loaiCong)) {
                    GatewayCallbackServer.dangKyVNPay(params -> xuLyKetQua(params, "VNPAY", maSV, soTien, khiThanhCong));
                    return viDienTuService.taoUrlNapViVNPay(maSV, soTien);
                } else {
                    GatewayCallbackServer.dangKyMoMo(params -> xuLyKetQua(params, "MOMO", maSV, soTien, khiThanhCong));
                    return viDienTuService.taoUrlNapViMoMo(maSV, soTien);
                }
            }

            @Override
            protected void done() {
                try {
                    String url = get();
                    Desktop.getDesktop().browse(URI.create(url));
                    lblTrangThai.setText("Dang cho ket qua tu " + loaiCong + "...");
                } catch (Exception ex) {
                    lblTrangThai.setText("Loi: khong tao duoc lien ket thanh toan.");
                    JOptionPane.showMessageDialog(NapViGatewayDialog.this,
                            "Chi tiet loi: " + rootMessage(ex), "Loi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /** Chay tren luong cua HttpServer khi nhan duoc callback - can dua ve EDT truoc khi dung Swing/DB. */
    private void xuLyKetQua(Map<String, String> params, String loaiCong, String maSV, BigDecimal soTienDuKien, Runnable khiThanhCong) {
        SwingUtilities.invokeLater(() -> {
            if (daXuLyXong) return;
            daXuLyXong = true;

            boolean thanhCong;
            String maGiaoDich;
            if ("VNPAY".equals(loaiCong)) {
                thanhCong = VNPayGateway.kiemTraChuKy(params) && VNPayGateway.thanhCong(params);
                maGiaoDich = params.getOrDefault("vnp_TransactionNo", params.get("vnp_TxnRef"));
            } else {
                thanhCong = MoMoGateway.thanhCong(params);
                maGiaoDich = params.getOrDefault("transId", params.get("orderId"));
            }

            if (thanhCong) {
                try {
                    String soTienStr = "VNPAY".equals(loaiCong) ? params.get("vnp_Amount") : params.get("amount");
                    BigDecimal soTien = soTienStr != null
                            ? new BigDecimal(soTienStr).divide(
                            "VNPAY".equals(loaiCong) ? BigDecimal.valueOf(100) : BigDecimal.ONE,
                            java.math.RoundingMode.HALF_UP)
                            : soTienDuKien;

                    viDienTuService.napTien(maSV, soTien, loaiCong, maGiaoDich);
                    JOptionPane.showMessageDialog(this, "Nap vi thanh cong!", "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
                    if (khiThanhCong != null) khiThanhCong.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Giao dich thanh cong nhung khong ghi duoc vao he thong.\n"
                            + rootMessage(ex), "Loi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Nap vi khong thanh cong hoac bi huy.", "That bai", JOptionPane.WARNING_MESSAGE);
            }
            dongVaDungServer();
        });
    }

    private void dongVaDungServer() {
        dispose();
        javax.swing.Timer timer = new javax.swing.Timer(5000, e -> GatewayCallbackServer.stop());
        timer.setRepeats(false);
        timer.start();
    }

    private String rootMessage(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }
}