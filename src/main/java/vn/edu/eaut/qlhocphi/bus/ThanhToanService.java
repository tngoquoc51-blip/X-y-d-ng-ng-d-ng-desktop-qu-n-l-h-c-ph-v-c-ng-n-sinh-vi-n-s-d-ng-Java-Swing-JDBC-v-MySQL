package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.HoaDonDAO;
import vn.edu.eaut.qlhocphi.dal.PhieuThuDAO;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.PhieuThu;
import vn.edu.eaut.qlhocphi.payment.MockPaymentGateway;
import vn.edu.eaut.qlhocphi.payment.PaymentGateway;
import vn.edu.eaut.qlhocphi.payment.PaymentResult;

import java.math.BigDecimal;
import java.sql.SQLException;

/**
 * Nghiep vu ghi nhan thanh toan hoc phi.
 * Ho tro 2 luong: nop truc tiep (tien mat/chuyen khoan) va thanh toan online qua PaymentGateway.
 */
public class ThanhToanService {
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();

    // De thay bang cong thanh toan that (VD: VNPayGateway), chi can doi dong khoi tao nay
    private final PaymentGateway paymentGateway = new MockPaymentGateway();

    /** Ghi nhan 1 khoan nop truc tiep (tien mat / chuyen khoan thu cong). */
    public void ghiNhanThanhToan(int maHoaDon, BigDecimal soTien, String hinhThuc, String nguoiThu) throws SQLException {
        kiemTraSoTienHopLe(maHoaDon, soTien);

        PhieuThu pt = new PhieuThu();
        pt.setMaHoaDon(maHoaDon);
        pt.setSoTienNop(soTien);
        pt.setHinhThuc(hinhThuc);
        pt.setNguoiThu(nguoiThu);
        phieuThuDAO.them(pt);
        new vn.edu.eaut.qlhocphi.bus.NhatKyHeThongService().ghi("THANH_TOAN", "Hoa don #" + maHoaDon,
                "Thu " + soTien + " qua " + hinhThuc);
    }

    /**
     * Thuc hien thanh toan online qua PaymentGateway (mo phong).
     * Buoc 1: tao giao dich -> Buoc 2: xac nhan giao dich -> Buoc 3: ghi PhieuThu.
     */
    public PaymentResult thanhToanOnline(int maHoaDon, BigDecimal soTien) throws SQLException {
        kiemTraSoTienHopLe(maHoaDon, soTien);

        String maGiaoDich = paymentGateway.taoGiaoDich(String.valueOf(maHoaDon), soTien, "Thanh toan hoc phi HD" + maHoaDon);
        PaymentResult result = paymentGateway.xacNhanGiaoDich(maGiaoDich, soTien);

        if (result.isThanhCong()) {
            PhieuThu pt = new PhieuThu();
            pt.setMaHoaDon(maHoaDon);
            pt.setSoTienNop(result.getSoTien());
            pt.setHinhThuc("THANH_TOAN_ONLINE");
            pt.setMaGiaoDich(result.getMaGiaoDich());
            pt.setNguoiThu("He thong (online)");
            phieuThuDAO.them(pt);
        }
        return result;
    }


    public String taoThanhToanVNPay(int maHoaDon, BigDecimal soTien) throws Exception {
        kiemTraSoTienHopLe(maHoaDon, soTien);
        String txnRef = "HD" + maHoaDon + "T" + System.currentTimeMillis();
        return vn.edu.eaut.qlhocphi.payment.gateway.VNPayGateway.taoUrlThanhToan(
                txnRef, soTien, "Thanh toan hoc phi hoa don " + maHoaDon);
    }

    public String taoThanhToanMoMo(int maHoaDon, BigDecimal soTien) throws Exception {
        kiemTraSoTienHopLe(maHoaDon, soTien);
        String orderId = "HD" + maHoaDon + "T" + System.currentTimeMillis();
        return vn.edu.eaut.qlhocphi.payment.gateway.MoMoGateway.taoUrlThanhToan(
                orderId, soTien, "Thanh toan hoc phi hoa don " + maHoaDon);
    }

    /** Goi tu callback server sau khi VNPay/MoMo xac nhan giao dich thanh cong. */
    public void ghiNhanThanhToanCongThanhToan(int maHoaDon, BigDecimal soTien, String maGiaoDich, String nguonCong) throws SQLException {
        kiemTraSoTienHopLe(maHoaDon, soTien);
        PhieuThu pt = new PhieuThu();
        pt.setMaHoaDon(maHoaDon);
        pt.setSoTienNop(soTien);
        pt.setHinhThuc("THANH_TOAN_ONLINE");
        pt.setMaGiaoDich(maGiaoDich);
        pt.setNguoiThu("He thong (" + nguonCong + ")");
        phieuThuDAO.them(pt);
    }

    private void kiemTraSoTienHopLe(int maHoaDon, BigDecimal soTien) throws SQLException {
        if (soTien == null || soTien.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("So tien nop phai lon hon 0");
        }
        HoaDonHocPhi hd = hoaDonDAO.timTheoMa(maHoaDon);
        if (hd == null) {
            throw new IllegalArgumentException("Hoa don khong ton tai");
        }
        BigDecimal conNo = hd.tinhConNo();
        if (soTien.compareTo(conNo) > 0) {
            throw new IllegalArgumentException("So tien nop (" + soTien + ") vuot qua so con no (" + conNo + ")");
        }
    }
}