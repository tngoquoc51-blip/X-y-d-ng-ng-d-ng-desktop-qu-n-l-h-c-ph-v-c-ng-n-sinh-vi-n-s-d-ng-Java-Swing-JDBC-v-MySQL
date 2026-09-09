package vn.edu.eaut.qlhocphi.model;

import java.time.LocalDateTime;

/** Khung thoi gian thu hoc phi do Admin dat - ap dung chung cho TOAN BO sinh vien. */
public class CauHinhNhacNoTuDong {
    private int maCauHinh;
    private LocalDateTime ngayGioBatDau;
    private LocalDateTime ngayGioKetThuc;
    private boolean dangApDung;
    private LocalDateTime ngayTao;
    private String nguoiTao;

    public int getMaCauHinh() { return maCauHinh; }
    public void setMaCauHinh(int maCauHinh) { this.maCauHinh = maCauHinh; }

    public LocalDateTime getNgayGioBatDau() { return ngayGioBatDau; }
    public void setNgayGioBatDau(LocalDateTime ngayGioBatDau) { this.ngayGioBatDau = ngayGioBatDau; }

    public LocalDateTime getNgayGioKetThuc() { return ngayGioKetThuc; }
    public void setNgayGioKetThuc(LocalDateTime ngayGioKetThuc) { this.ngayGioKetThuc = ngayGioKetThuc; }

    public boolean isDangApDung() { return dangApDung; }
    public void setDangApDung(boolean dangApDung) { this.dangApDung = dangApDung; }

    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime ngayTao) { this.ngayTao = ngayTao; }

    public String getNguoiTao() { return nguoiTao; }
    public void setNguoiTao(String nguoiTao) { this.nguoiTao = nguoiTao; }
}