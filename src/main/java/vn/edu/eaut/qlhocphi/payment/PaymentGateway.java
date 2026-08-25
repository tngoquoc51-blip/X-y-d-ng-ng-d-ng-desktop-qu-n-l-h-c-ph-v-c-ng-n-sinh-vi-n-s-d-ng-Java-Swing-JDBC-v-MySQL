package vn.edu.eaut.qlhocphi.payment;

import java.math.BigDecimal;

/**
 * Interface cong thanh toan - cho phep cam thay nhieu nha cung cap
 * (VNPay, MoMo, ZaloPay...) ma khong sua code o tang BUS/GUI.
 */
public interface PaymentGateway {

    /** Khoi tao 1 giao dich thanh toan, tra ve URL/QR de nguoi dung thanh toan (mo phong). */
    String taoGiaoDich(String maHoaDonThamChieu, BigDecimal soTien, String noiDung);

    /**
     * Xu ly/xac nhan ket qua giao dich.
     * Trong he thong that day la callback tu cong thanh toan goi ve (webhook);
     * o day ta mo phong dong bo de phuc vu demo/bao cao.
     */
    PaymentResult xacNhanGiaoDich(String maGiaoDich, BigDecimal soTien);
}
