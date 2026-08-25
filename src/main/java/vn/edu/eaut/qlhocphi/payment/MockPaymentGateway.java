package vn.edu.eaut.qlhocphi.payment;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Trien khai mo phong (MOCK) cua cong thanh toan.
 * Dung cho demo/bao cao khi chua co hop dong that voi VNPay/MoMo.
 * Kien truc PaymentGateway cho phep thay the bang lop that (VNPayGateway, MoMoGateway...)
 * chi bang cach doi 1 dong khoi tao trong ThanhToanService, khong dung cham GUI.
 */
public class MockPaymentGateway implements PaymentGateway {

    @Override
    public String taoGiaoDich(String maHoaDonThamChieu, BigDecimal soTien, String noiDung) {
        // Trong thuc te: goi API cua cong thanh toan de lay URL thanh toan / QR code
        String maGiaoDich = "GD" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return maGiaoDich;
    }

    @Override
    public PaymentResult xacNhanGiaoDich(String maGiaoDich, BigDecimal soTien) {
        // Mo phong: giao dich luon thanh cong sau khi "xu ly"
        // (co the mo rong de random != that bai nham demo xu ly loi)
        return new PaymentResult(true, maGiaoDich, soTien, "Giao dich thanh cong (mo phong)");
    }
}
