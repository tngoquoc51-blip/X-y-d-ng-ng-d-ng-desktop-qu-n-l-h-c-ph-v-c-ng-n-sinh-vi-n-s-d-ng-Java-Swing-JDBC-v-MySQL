
package vn.edu.eaut.qlhocphi.model;

/** Chính sách bảo mật hệ thống. */
public class CauHinhBaoMat {
    private int doDaiMatKhauToiThieu = 6;
    private int sessionTimeoutPhut = 60;
    private int soLanDangNhapSaiToiDa = 5;
    private int thoiGianKhoaPhut = 15;
    private boolean batBuocDoiMkLanDau = false;

    public int getDoDaiMatKhauToiThieu() { return doDaiMatKhauToiThieu; }
    public void setDoDaiMatKhauToiThieu(int v) { this.doDaiMatKhauToiThieu = v; }
    public int getSessionTimeoutPhut() { return sessionTimeoutPhut; }
    public void setSessionTimeoutPhut(int v) { this.sessionTimeoutPhut = v; }
    public int getSoLanDangNhapSaiToiDa() { return soLanDangNhapSaiToiDa; }
    public void setSoLanDangNhapSaiToiDa(int v) { this.soLanDangNhapSaiToiDa = v; }
    public int getThoiGianKhoaPhut() { return thoiGianKhoaPhut; }
    public void setThoiGianKhoaPhut(int v) { this.thoiGianKhoaPhut = v; }
    public boolean isBatBuocDoiMkLanDau() { return batBuocDoiMkLanDau; }
    public void setBatBuocDoiMkLanDau(boolean v) { this.batBuocDoiMkLanDau = v; }
}