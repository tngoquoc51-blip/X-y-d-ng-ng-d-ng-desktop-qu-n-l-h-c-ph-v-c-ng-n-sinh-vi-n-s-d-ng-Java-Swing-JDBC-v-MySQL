package vn.edu.eaut.qlhocphi.model;

import java.time.LocalDateTime;

/** 1 dong nhat ky: ai (TenDangNhap) da lam hanh dong gi (HanhDong) tren doi tuong nao (DoiTuong), luc nao. */
public class NhatKyHeThong {
    private int maNhatKy;
    private Integer maTK;
    private String tenDangNhap;
    private String hanhDong;
    private String doiTuong;
    private String chiTiet;
    private LocalDateTime thoiGian;

    public int getMaNhatKy() { return maNhatKy; }
    public void setMaNhatKy(int maNhatKy) { this.maNhatKy = maNhatKy; }
    public Integer getMaTK() { return maTK; }
    public void setMaTK(Integer maTK) { this.maTK = maTK; }
    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }
    public String getHanhDong() { return hanhDong; }
    public void setHanhDong(String hanhDong) { this.hanhDong = hanhDong; }
    public String getDoiTuong() { return doiTuong; }
    public void setDoiTuong(String doiTuong) { this.doiTuong = doiTuong; }
    public String getChiTiet() { return chiTiet; }
    public void setChiTiet(String chiTiet) { this.chiTiet = chiTiet; }
    public LocalDateTime getThoiGian() { return thoiGian; }
    public void setThoiGian(LocalDateTime thoiGian) { this.thoiGian = thoiGian; }
}