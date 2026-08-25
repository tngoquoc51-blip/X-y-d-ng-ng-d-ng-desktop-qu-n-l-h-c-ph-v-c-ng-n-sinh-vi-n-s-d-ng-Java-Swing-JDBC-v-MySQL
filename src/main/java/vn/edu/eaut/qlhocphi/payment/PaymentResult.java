package vn.edu.eaut.qlhocphi.payment;

import java.math.BigDecimal;

/** Ket qua tra ve tu cong thanh toan. */
public class PaymentResult {
    private boolean thanhCong;
    private String maGiaoDich;
    private BigDecimal soTien;
    private String thongDiep;

    public PaymentResult(boolean thanhCong, String maGiaoDich, BigDecimal soTien, String thongDiep) {
        this.thanhCong = thanhCong;
        this.maGiaoDich = maGiaoDich;
        this.soTien = soTien;
        this.thongDiep = thongDiep;
    }

    public boolean isThanhCong() { return thanhCong; }
    public String getMaGiaoDich() { return maGiaoDich; }
    public BigDecimal getSoTien() { return soTien; }
    public String getThongDiep() { return thongDiep; }
}
