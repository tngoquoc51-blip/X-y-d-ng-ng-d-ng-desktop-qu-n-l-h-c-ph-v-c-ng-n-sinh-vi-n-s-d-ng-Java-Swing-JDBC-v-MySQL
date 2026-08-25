package vn.edu.eaut.qlhocphi.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Hoa don hoc phi cua 1 sinh vien trong 1 hoc ky. */
public class HoaDonHocPhi {
    private int maHoaDon;
    private String maSV;
    private String tenSV;       // ho tro hien thi (join), khong luu rieng trong bang HoaDon
    private int maHocKy;
    private String tenHocKy;    // ho tro hien thi
    private int soTinChi;
    private BigDecimal soTien;
    private BigDecimal daNop = BigDecimal.ZERO; // tinh tu tong PhieuThu
    private LocalDate hanThanhToan;
    private LocalDateTime ngayTao;

    public HoaDonHocPhi() {}

    public int getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(int maHoaDon) { this.maHoaDon = maHoaDon; }

    public String getMaSV() { return maSV; }
    public void setMaSV(String maSV) { this.maSV = maSV; }

    public String getTenSV() { return tenSV; }
    public void setTenSV(String tenSV) { this.tenSV = tenSV; }

    public int getMaHocKy() { return maHocKy; }
    public void setMaHocKy(int maHocKy) { this.maHocKy = maHocKy; }

    public String getTenHocKy() { return tenHocKy; }
    public void setTenHocKy(String tenHocKy) { this.tenHocKy = tenHocKy; }

    public int getSoTinChi() { return soTinChi; }
    public void setSoTinChi(int soTinChi) { this.soTinChi = soTinChi; }

    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }

    public BigDecimal getDaNop() { return daNop; }
    public void setDaNop(BigDecimal daNop) { this.daNop = daNop; }

    public LocalDate getHanThanhToan() { return hanThanhToan; }
    public void setHanThanhToan(LocalDate hanThanhToan) { this.hanThanhToan = hanThanhToan; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    /** So tien con no = SoTien - DaNop (khong am). */
    public BigDecimal tinhConNo() {
        BigDecimal conNo = soTien.subtract(daNop == null ? BigDecimal.ZERO : daNop);
        return conNo.max(BigDecimal.ZERO);
    }

    public TrangThaiHoaDon tinhTrangThai() {
        BigDecimal conNo = tinhConNo();
        if (conNo.compareTo(BigDecimal.ZERO) <= 0) {
            return TrangThaiHoaDon.DA_DONG_DU;
        }
        if (hanThanhToan != null && LocalDate.now().isAfter(hanThanhToan)) {
            return TrangThaiHoaDon.QUA_HAN;
        }
        if (daNop != null && daNop.compareTo(BigDecimal.ZERO) > 0) {
            return TrangThaiHoaDon.DONG_MOT_PHAN;
        }
        return TrangThaiHoaDon.CHUA_DONG;
    }
}