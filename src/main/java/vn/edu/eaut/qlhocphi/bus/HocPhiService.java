package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.HocKyDAO;
import vn.edu.eaut.qlhocphi.dal.HoaDonDAO;
import vn.edu.eaut.qlhocphi.model.HocKy;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Nghiep vu quan ly hoc ky va sinh hoa don hoc phi. */
public class HocPhiService {
    private final HocKyDAO hocKyDAO = new HocKyDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    public List<HocKy> layTatCaHocKy() throws SQLException {
        return hocKyDAO.layTatCa();
    }

    public void themHocKy(HocKy hk) throws SQLException {
        if (hk.getTenHocKy() == null || hk.getTenHocKy().isBlank()) {
            throw new IllegalArgumentException("Ten hoc ky khong duoc de trong");
        }
        if (hk.getDonGiaTinChi() == null || hk.getDonGiaTinChi().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Don gia tin chi phai >= 0");
        }
        hocKyDAO.them(hk);
    }

    public void capNhatHocKy(HocKy hk) throws SQLException {
        if (hk.getTenHocKy() == null || hk.getTenHocKy().isBlank()) {
            throw new IllegalArgumentException("Ten hoc ky khong duoc de trong");
        }
        if (hk.getDonGiaTinChi() == null || hk.getDonGiaTinChi().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Don gia tin chi phai >= 0");
        }
        if (hk.getNgayBatDau() != null && hk.getNgayKetThuc() != null
                && hk.getNgayBatDau().isAfter(hk.getNgayKetThuc())) {
            throw new IllegalArgumentException("Ngay bat dau phai truoc ngay ket thuc");
        }
        hocKyDAO.capNhat(hk);
    }

    public void xoaHocKy(int maHocKy) throws SQLException {
        try {
            hocKyDAO.xoa(maHocKy);
        } catch (SQLException ex) {
            // Loi khoa ngoai: hoc ky da co hoa don gan voi no
            throw new IllegalArgumentException("Khong the xoa: hoc ky nay da co hoa don hoc phi lien quan.");
        }
    }
    /** Tra cuu 1 hoa don theo ma - dung cho man hinh Thanh toan tra cuu truoc khi thu tien. */
    public HoaDonHocPhi timTheoMa(int maHoaDon) throws SQLException {
        return hoaDonDAO.timTheoMa(maHoaDon);
    }

    public List<HoaDonHocPhi> layTatCaHoaDon() throws SQLException {
        return hoaDonDAO.layTatCa();
    }

    public List<HoaDonHocPhi> layHoaDonTheoSinhVien(String maSV) throws SQLException {
        return hoaDonDAO.layTheoSinhVien(maSV);
    }

    /** Sinh 1 hoa don hoc phi cho 1 sinh vien trong 1 hoc ky, tinh theo so tin chi. */
    public int sinhHoaDon(String maSV, int maHocKy, int soTinChi, LocalDate hanThanhToan) throws SQLException {
        if (soTinChi <= 0) throw new IllegalArgumentException("So tin chi phai lon hon 0");
        HocKy hk = hocKyDAO.timTheoMa(maHocKy);
        if (hk == null) throw new IllegalArgumentException("Hoc ky khong ton tai");

        BigDecimal soTien = hk.getDonGiaTinChi().multiply(BigDecimal.valueOf(soTinChi));

        HoaDonHocPhi hd = new HoaDonHocPhi();
        hd.setMaSV(maSV);
        hd.setMaHocKy(maHocKy);
        hd.setSoTinChi(soTinChi);
        hd.setSoTien(soTien);
        hd.setHanThanhToan(hanThanhToan);
        return hoaDonDAO.them(hd);
    }
    /** Xoa hoa don hoc phi. Goi tu man hinh HoaDonPanel khi nguoi dung bam "Xoa". */
    public void xoaHoaDon(int maHoaDon) throws SQLException {
        hoaDonDAO.xoa(maHoaDon);
    }
}