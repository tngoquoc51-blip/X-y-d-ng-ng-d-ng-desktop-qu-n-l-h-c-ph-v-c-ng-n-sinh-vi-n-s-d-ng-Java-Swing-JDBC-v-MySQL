package vn.edu.eaut.qlhocphi.model;

import java.time.LocalDateTime;

public class TheQRDangNhap {
    private int maThe;
    private int maTK;
    private String token;
    private LocalDateTime thoiGianTao;
    private boolean trangThai;

    public int getMaThe() { return maThe; }
    public void setMaThe(int maThe) { this.maThe = maThe; }
    public int getMaTK() { return maTK; }
    public void setMaTK(int maTK) { this.maTK = maTK; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public LocalDateTime getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(LocalDateTime thoiGianTao) { this.thoiGianTao = thoiGianTao; }
    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }
}