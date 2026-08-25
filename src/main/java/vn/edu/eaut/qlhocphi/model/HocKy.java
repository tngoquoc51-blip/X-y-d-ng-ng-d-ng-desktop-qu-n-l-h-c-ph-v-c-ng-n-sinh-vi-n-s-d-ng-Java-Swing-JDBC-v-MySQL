package vn.edu.eaut.qlhocphi.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Hoc ky va don gia tin chi ap dung. */
public class HocKy {
    private int maHocKy;
    private String tenHocKy;
    private String namHoc;
    private BigDecimal donGiaTinChi;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;

    public HocKy() {}

    public HocKy(int maHocKy, String tenHocKy, String namHoc, BigDecimal donGiaTinChi,
                 LocalDate ngayBatDau, LocalDate ngayKetThuc) {
        this.maHocKy = maHocKy;
        this.tenHocKy = tenHocKy;
        this.namHoc = namHoc;
        this.donGiaTinChi = donGiaTinChi;
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
    }

    public int getMaHocKy() { return maHocKy; }
    public void setMaHocKy(int maHocKy) { this.maHocKy = maHocKy; }

    public String getTenHocKy() { return tenHocKy; }
    public void setTenHocKy(String tenHocKy) { this.tenHocKy = tenHocKy; }

    public String getNamHoc() { return namHoc; }
    public void setNamHoc(String namHoc) { this.namHoc = namHoc; }

    public BigDecimal getDonGiaTinChi() { return donGiaTinChi; }
    public void setDonGiaTinChi(BigDecimal donGiaTinChi) { this.donGiaTinChi = donGiaTinChi; }

    public LocalDate getNgayBatDau() { return ngayBatDau; }
    public void setNgayBatDau(LocalDate ngayBatDau) { this.ngayBatDau = ngayBatDau; }

    public LocalDate getNgayKetThuc() { return ngayKetThuc; }
    public void setNgayKetThuc(LocalDate ngayKetThuc) { this.ngayKetThuc = ngayKetThuc; }

    @Override
    public String toString() { return tenHocKy + " " + namHoc; }
}