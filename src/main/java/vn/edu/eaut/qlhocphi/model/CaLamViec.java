package vn.edu.eaut.qlhocphi.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CaLamViec {
    private int maCa;
    private int maTK;
    private String tenNhanVien;
    private LocalDateTime thoiGianMoCa;
    private LocalDateTime thoiGianDongCa;
    private BigDecimal tongTienMat = BigDecimal.ZERO;
    private BigDecimal tongTienChuyenKhoan = BigDecimal.ZERO;
    private BigDecimal tongTienOnline = BigDecimal.ZERO;
    private int soGiaoDich;
    private String trangThai;

    public int getMaCa() { return maCa; }
    public void setMaCa(int maCa) { this.maCa = maCa; }
    public int getMaTK() { return maTK; }
    public void setMaTK(int maTK) { this.maTK = maTK; }
    public String getTenNhanVien() { return tenNhanVien; }
    public void setTenNhanVien(String tenNhanVien) { this.tenNhanVien = tenNhanVien; }
    public LocalDateTime getThoiGianMoCa() { return thoiGianMoCa; }
    public void setThoiGianMoCa(LocalDateTime t) { this.thoiGianMoCa = t; }
    public LocalDateTime getThoiGianDongCa() { return thoiGianDongCa; }
    public void setThoiGianDongCa(LocalDateTime t) { this.thoiGianDongCa = t; }
    public BigDecimal getTongTienMat() { return tongTienMat; }
    public void setTongTienMat(BigDecimal t) { this.tongTienMat = t; }
    public BigDecimal getTongTienChuyenKhoan() { return tongTienChuyenKhoan; }
    public void setTongTienChuyenKhoan(BigDecimal t) { this.tongTienChuyenKhoan = t; }
    public BigDecimal getTongTienOnline() { return tongTienOnline; }
    public void setTongTienOnline(BigDecimal t) { this.tongTienOnline = t; }
    public int getSoGiaoDich() { return soGiaoDich; }
    public void setSoGiaoDich(int s) { this.soGiaoDich = s; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String t) { this.trangThai = t; }

    public BigDecimal tongTatCa() {
        return tongTienMat.add(tongTienChuyenKhoan).add(tongTienOnline);
    }
    public boolean dangMo() { return "DANG_MO".equals(trangThai); }
}