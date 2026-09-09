package vn.edu.eaut.qlhocphi.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 1 lan nap tien vao Vi hoc phi dien tu. */
public class LichSuNapVi {
    private int maGiaoDich;
    private String maSV;
    private BigDecimal soTien;
    private LocalDateTime ngayNap;
    private String hinhThuc;      // VNPAY, MOMO, TIEN_MAT
    private String maGiaoDichCong;

    public LichSuNapVi() {}

    public int getMaGiaoDich() { return maGiaoDich; }
    public void setMaGiaoDich(int maGiaoDich) { this.maGiaoDich = maGiaoDich; }

    public String getMaSV() { return maSV; }
    public void setMaSV(String maSV) { this.maSV = maSV; }

    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }

    public LocalDateTime getNgayNap() { return ngayNap; }
    public void setNgayNap(LocalDateTime ngayNap) { this.ngayNap = ngayNap; }

    public String getHinhThuc() { return hinhThuc; }
    public void setHinhThuc(String hinhThuc) { this.hinhThuc = hinhThuc; }

    public String getMaGiaoDichCong() { return maGiaoDichCong; }
    public void setMaGiaoDichCong(String maGiaoDichCong) { this.maGiaoDichCong = maGiaoDichCong; }
}