package vn.edu.eaut.qlhocphi.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 1 "uy quyen" quet thu tu dong cho 1 hoa don, trong 1 khoang ngay nhat dinh. */
public class LichThuTuDong {
    private int maLich;
    private int maHoaDon;
    private String maSV;       // ho tro hien thi (join)
    private String tenSV;      // ho tro hien thi (join)
    private LocalDate ngayBatDauQuet;
    private LocalDate ngayKetThucQuet;
    private String trangThai;  // DANG_CHO, DA_THU, HET_HAN, DA_HUY
    private LocalDateTime ngayTao;
    private String nguoiTao;
    private LocalDateTime lanQuetGanNhat;

    public LichThuTuDong() {}

    public int getMaLich() { return maLich; }
    public void setMaLich(int maLich) { this.maLich = maLich; }

    public int getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(int maHoaDon) { this.maHoaDon = maHoaDon; }

    public String getMaSV() { return maSV; }
    public void setMaSV(String maSV) { this.maSV = maSV; }

    public String getTenSV() { return tenSV; }
    public void setTenSV(String tenSV) { this.tenSV = tenSV; }

    public LocalDate getNgayBatDauQuet() { return ngayBatDauQuet; }
    public void setNgayBatDauQuet(LocalDate ngayBatDauQuet) { this.ngayBatDauQuet = ngayBatDauQuet; }

    public LocalDate getNgayKetThucQuet() { return ngayKetThucQuet; }
    public void setNgayKetThucQuet(LocalDate ngayKetThucQuet) { this.ngayKetThucQuet = ngayKetThucQuet; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    public String getNguoiTao() { return nguoiTao; }
    public void setNguoiTao(String nguoiTao) { this.nguoiTao = nguoiTao; }

    public LocalDateTime getLanQuetGanNhat() { return lanQuetGanNhat; }
    public void setLanQuetGanNhat(LocalDateTime lanQuetGanNhat) { this.lanQuetGanNhat = lanQuetGanNhat; }
}