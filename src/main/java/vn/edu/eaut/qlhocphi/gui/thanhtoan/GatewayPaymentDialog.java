package vn.edu.eaut.qlhocphi.gui.thanhtoan;

import vn.edu.eaut.qlhocphi.bus.ThanhToanService;
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
 * Dialog "dang cho thanh toan" - mo trinh duyet toi cong VNPay/MoMo, lang nghe
 * callback qua GatewayCallbackServer (chay sau ngrok), tu dong cap nhat ket qua.
 */
public class GatewayPaymentDialog extends JDialog {
    private final ThanhToanService thanhToanService = new ThanhToanService();
    private JLabel lblTrangThai;
    private boolean daXuLyXong = false;

    public GatewayPaymentDialog(Window chaMe, int maHoaDon, BigDecimal soTien, String loaiCong, Runnable khiThanhCong) {
        super(chaMe, "Thanh toan qua " + loaiCong, ModalityType.APPLICATION_MODAL);
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

        JLabel lblGhiChu = new JLabel("<html><div style='text-align:center'>Trinh duyet se tu mo. Sau khi hoan tat<br>thanh toan, quay lai day - ung dung se<br>tu dong nhan ket qua.</div></html>");
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

        batDauThanhToan(maHoaDon, soTien, loaiCong, khiThanhCong);
    }

    private void batDauThanhToan(int maHoaDon, BigDecimal soTien, String loaiCong, Runnable khiThanhCong) {
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() throws Exception {
                GatewayCallbackServer.start();

                if ("VNPAY".equals(loaiCong)) {
                    GatewayCallbackServer.dangKyVNPay(params -> xuLyKetQua(params, "VNPAY", maHoaDon, khiThanhCong));
                    return thanhToanService.taoThanhToanVNPay(maHoaDon, soTien);
                } else {
                    GatewayCallbackServer.dangKyMoMo(params -> xuLyKetQua(params, "MOMO", maHoaDon, khiThanhCong));
                    return thanhToanService.taoThanhToanMoMo(maHoaDon, soTien);
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
                    JOptionPane.showMessageDialog(GatewayPaymentDialog.this,
                            "Chi tiet loi: " + rootMessage(ex), "Loi", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    /** Chay tren luong cua HttpServer khi nhan duoc callback - can dua ve EDT truoc khi dung Swing/DB. */
    private void xuLyKetQua(Map<String, String> params, String loaiCong, int maHoaDon, Runnable khiThanhCong) {
        // DEBUG - xoa sau khi test xong
        System.out.println("=== CALLBACK NHAN DUOC (" + loaiCong + ") ===");
        System.out.println("So luong tham so: " + params.size());
        for (Map.Entry<String, String> e : params.entrySet()) {
            System.out.println("  " + e.getKey() + " = " + e.getValue());
        }
        System.out.println("daXuLyXong hien tai: " + daXuLyXong);
        System.out.println("=====================================");

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
                    String soTienStr = "VNPAY".equals(loaiCong)
                            ? params.get("vnp_Amount")
                            : params.get("amount");
                    BigDecimal soTien = new BigDecimal(soTienStr)
                            .divide("VNPAY".equals(loaiCong) ? BigDecimal.valueOf(100) : BigDecimal.ONE,
                                    java.math.RoundingMode.HALF_UP);

                    thanhToanService.ghiNhanThanhToanCongThanhToan(maHoaDon, soTien, maGiaoDich, loaiCong);
                    JOptionPane.showMessageDialog(this, "Thanh toan thanh cong!", "Thanh cong", JOptionPane.INFORMATION_MESSAGE);
                    if (khiThanhCong != null) khiThanhCong.run();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this, "Thanh toan thanh cong nhung khong ghi duoc vao he thong.\n"
                            + rootMessage(ex), "Loi", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "Thanh toan khong thanh cong hoac bi huy.", "That bai", JOptionPane.WARNING_MESSAGE);
            }
            dongVaDungServer();
        });
    }

    private void dongVaDungServer() {
        dispose();
        GatewayCallbackServer.henTat(5000);
    }

    private String rootMessage(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        return cause.getMessage() != null ? cause.getMessage() : cause.toString();
    }
}