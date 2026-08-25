package vn.edu.eaut.qlhocphi.model;

import java.time.LocalDate;

/** Thong tin sinh vien. */
public class SinhVien {
    private String maSV;
    private String hoTen;
    private String lop;
    private String khoa;
    private LocalDate ngaySinh;
    private String email;
    private String soDienThoai;
    private boolean trangThai = true;
    private String anhDaiDien;

    public SinhVien() {}

    public SinhVien(String maSV, String hoTen, String lop, String khoa,
                    LocalDate ngaySinh, String email, String soDienThoai) {
        this.maSV = maSV;
        this.hoTen = hoTen;
        this.lop = lop;
        this.khoa = khoa;
        this.ngaySinh = ngaySinh;
        this.email = email;
        this.soDienThoai = soDienThoai;
    }

    public String getMaSV() { return maSV; }
    public void setMaSV(String maSV) { this.maSV = maSV; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getLop() { return lop; }
    public void setLop(String lop) { this.lop = lop; }

    public String getKhoa() { return khoa; }
    public void setKhoa(String khoa) { this.khoa = khoa; }

    public LocalDate getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(LocalDate ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public String getAnhDaiDien() { return anhDaiDien; }
    public void setAnhDaiDien(String anhDaiDien) { this.anhDaiDien = anhDaiDien; }

    @Override
    public String toString() { return maSV + " - " + hoTen; }
}