package vn.edu.eaut.qlhocphi.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Hoa don hoc phi cua 1 sinh vien trong 1 hoc ky. */
public class HoaDonHocPhi {
    private int maHoaDon;
    private String maSV;
    private String tenSV;
    private int maHocKy;
    private String tenHocKy;
    private int soTinChi;
    private BigDecimal soTien;
    private BigDecimal daNop = BigDecimal.ZERO;
    private LocalDate hanThanhToan;
    private LocalDate ngayNhacNoGanNhat; // CU - khong dung nua, giu lai de tuong thich
    private java.time.LocalDateTime thoiDiemGuiEmailSV;
    private java.time.LocalDateTime thoiDiemGuiSmsPH;
    private LocalDateTime ngayTao;
    private BigDecimal tyLeMienGiam = BigDecimal.ZERO; // % (0-100), VD 20 = giam 20%
    private String lyDoMienGiam;

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
    public LocalDate getNgayNhacNoGanNhat() { return ngayNhacNoGanNhat; }
    public void setNgayNhacNoGanNhat(LocalDate ngayNhacNoGanNhat) { this.ngayNhacNoGanNhat = ngayNhacNoGanNhat; }

    public java.time.LocalDateTime getThoiDiemGuiEmailSV() { return thoiDiemGuiEmailSV; }
    public void setThoiDiemGuiEmailSV(java.time.LocalDateTime t) { this.thoiDiemGuiEmailSV = t; }

    public java.time.LocalDateTime getThoiDiemGuiSmsPH() { return thoiDiemGuiSmsPH; }
    public void setThoiDiemGuiSmsPH(java.time.LocalDateTime t) { this.thoiDiemGuiSmsPH = t; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }
    public BigDecimal getTyLeMienGiam() { return tyLeMienGiam; }
    public void setTyLeMienGiam(BigDecimal tyLeMienGiam) { this.tyLeMienGiam = tyLeMienGiam == null ? BigDecimal.ZERO : tyLeMienGiam; }
    public String getLyDoMienGiam() { return lyDoMienGiam; }
    public void setLyDoMienGiam(String lyDoMienGiam) { this.lyDoMienGiam = lyDoMienGiam; }

    /** So tien duoc mien giam = SoTien * TyLeMienGiam / 100. */
    public BigDecimal tinhSoTienMienGiam() {
        if (soTien == null || tyLeMienGiam == null || tyLeMienGiam.compareTo(BigDecimal.ZERO) <= 0) return BigDecimal.ZERO;
        return soTien.multiply(tyLeMienGiam).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
    }

    /** So tien thuc phai dong = SoTien goc - So tien mien giam. */
    public BigDecimal tinhSoTienPhaiDong() {
        return soTien.subtract(tinhSoTienMienGiam());
    }

    /** SUA: Con no gio tinh tren So tien PHAI DONG (sau mien giam), khong phai SoTien goc nua. */
    public BigDecimal tinhConNo() {
        BigDecimal conNo = tinhSoTienPhaiDong().subtract(daNop == null ? BigDecimal.ZERO : daNop);
        return conNo.max(BigDecimal.ZERO);
    }

    public TrangThaiHoaDon tinhTrangThai() {
        BigDecimal conNo = tinhConNo();
        if (conNo.compareTo(BigDecimal.ZERO) <= 0) return TrangThaiHoaDon.DA_DONG_DU;
        if (hanThanhToan != null && LocalDate.now().isAfter(hanThanhToan)) return TrangThaiHoaDon.QUA_HAN;
        if (daNop != null && daNop.compareTo(BigDecimal.ZERO) > 0) return TrangThaiHoaDon.DONG_MOT_PHAN;
        return TrangThaiHoaDon.CHUA_DONG;
    }
}