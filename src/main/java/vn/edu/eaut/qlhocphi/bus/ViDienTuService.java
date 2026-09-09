package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.LichSuNapViDAO;
import vn.edu.eaut.qlhocphi.dal.ViDienTuDAO;
import vn.edu.eaut.qlhocphi.model.LichSuNapVi;
import vn.edu.eaut.qlhocphi.model.ViDienTu;
import vn.edu.eaut.qlhocphi.payment.gateway.MoMoGateway;
import vn.edu.eaut.qlhocphi.payment.gateway.VNPayGateway;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ViDienTuService {
    private final ViDienTuDAO viDienTuDAO = new ViDienTuDAO();
    private final LichSuNapViDAO lichSuNapViDAO = new LichSuNapViDAO();

    public BigDecimal laySoDu(String maSV) throws SQLException {
        ViDienTu vi = viDienTuDAO.timTheoMaSV(maSV);
        return vi != null ? vi.getSoDu() : BigDecimal.ZERO;
    }

    /** Sinh vien nap tien vao vi (qua VNPay/MoMo hoac mo phong tien mat). */
    public void napTien(String maSV, BigDecimal soTien, String hinhThuc, String maGiaoDichCong) throws SQLException {
        if (soTien == null || soTien.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("So tien nap phai lon hon 0");
        }
        viDienTuDAO.taoMoiNeuChuaCo(maSV);
        viDienTuDAO.congTien(maSV, soTien);

        LichSuNapVi ls = new LichSuNapVi();
        ls.setMaSV(maSV);
        ls.setSoTien(soTien);
        ls.setHinhThuc(hinhThuc);
        ls.setMaGiaoDichCong(maGiaoDichCong);
        lichSuNapViDAO.them(ls);

        new NhatKyHeThongService().ghi("NAP_VI", "ViDienTu " + maSV,
                "Nap " + soTien + " qua " + hinhThuc + " vao vi hoc phi");
    }

    /** Dung boi ThuTuDongService khi tu dong tru tien - tra ve true neu du tien. */
    public boolean truTienNeuDu(String maSV, BigDecimal soTien) throws SQLException {
        return viDienTuDAO.truTienNeuDu(maSV, soTien);
    }

    /**
     * MOI: Sinh vien tu bam thanh toan hoa don TRUC TIEP bang so du Vi hoc phi
     * dien tu cua minh, khong can qua VNPay/MoMo, khong can Ke toan phe duyet
     * lich thu truoc. Tra ve true neu vi du tien va da tru + ghi PhieuThu thanh
     * cong; false neu vi khong du so du (khong tru, khong ghi gi ca).
     */
    public boolean thanhToanHoaDonBangVi(String maSV, int maHoaDon, BigDecimal soTien) throws SQLException {
        if (soTien == null || soTien.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("So tien thanh toan phai lon hon 0");
        }
        boolean duTien = viDienTuDAO.truTienNeuDu(maSV, soTien);
        if (!duTien) return false;

        vn.edu.eaut.qlhocphi.model.PhieuThu pt = new vn.edu.eaut.qlhocphi.model.PhieuThu();
        pt.setMaHoaDon(maHoaDon);
        pt.setSoTienNop(soTien);
        pt.setHinhThuc("VI_DIEN_TU");
        pt.setNguoiThu("Sinh vien " + maSV + " (tu vi hoc phi)");
        new vn.edu.eaut.qlhocphi.dal.PhieuThuDAO().them(pt);

        new NhatKyHeThongService().ghi("THANH_TOAN_BANG_VI", "HoaDon #" + maHoaDon,
                "SV " + maSV + " tu thanh toan " + soTien + " bang Vi hoc phi dien tu");
        return true;
    }

    public List<LichSuNapVi> layLichSuNap(String maSV) throws SQLException {
        return lichSuNapViDAO.layTheoSinhVien(maSV);
    }

    /** Tao URL nap vi qua VNPay Sandbox - txnRef ma hoa MaSV de phan biet voi thanh toan hoa don. */
    public String taoUrlNapViVNPay(String maSV, BigDecimal soTien) {
        kiemTraSoTienNap(soTien);
        String txnRef = "VI" + maSV + "T" + System.currentTimeMillis();
        return VNPayGateway.taoUrlThanhToan(txnRef, soTien, "Nap vi hoc phi SV " + maSV);
    }

    /** Tao URL nap vi qua MoMo Test Environment. */
    public String taoUrlNapViMoMo(String maSV, BigDecimal soTien) throws Exception {
        kiemTraSoTienNap(soTien);
        String orderId = "VI" + maSV + "T" + System.currentTimeMillis();
        return MoMoGateway.taoUrlThanhToan(orderId, soTien, "Nap vi hoc phi SV " + maSV);
    }

    private void kiemTraSoTienNap(BigDecimal soTien) {
        if (soTien == null || soTien.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("So tien nap phai lon hon 0");
        }
    }
}