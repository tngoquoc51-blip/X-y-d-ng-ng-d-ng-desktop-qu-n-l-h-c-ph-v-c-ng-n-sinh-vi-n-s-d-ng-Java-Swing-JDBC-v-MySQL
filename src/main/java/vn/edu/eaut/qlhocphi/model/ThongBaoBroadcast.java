
package vn.edu.eaut.qlhocphi.model;

import java.time.LocalDateTime;

/** Thông báo hệ thống toàn trường (broadcast khi đăng nhập). */
public class ThongBaoBroadcast {
    private int maBroadcast;
    private String tieuDe;
    private String noiDung;
    private String mucDo; // THONG_TIN | CANH_BAO | KHAN_CAP
    private LocalDateTime hienThiTu;
    private LocalDateTime hienThiDen;
    private boolean dangBat;
    private String nguoiTao;
    private LocalDateTime ngayTao;

    public int getMaBroadcast() { return maBroadcast; }
    public void setMaBroadcast(int v) { this.maBroadcast = v; }
    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String v) { this.tieuDe = v; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String v) { this.noiDung = v; }
    public String getMucDo() { return mucDo; }
    public void setMucDo(String v) { this.mucDo = v; }
    public LocalDateTime getHienThiTu() { return hienThiTu; }
    public void setHienThiTu(LocalDateTime v) { this.hienThiTu = v; }
    public LocalDateTime getHienThiDen() { return hienThiDen; }
    public void setHienThiDen(LocalDateTime v) { this.hienThiDen = v; }
    public boolean isDangBat() { return dangBat; }
    public void setDangBat(boolean v) { this.dangBat = v; }
    public String getNguoiTao() { return nguoiTao; }
    public void setNguoiTao(String v) { this.nguoiTao = v; }
    public LocalDateTime getNgayTao() { return ngayTao; }
    public void setNgayTao(LocalDateTime v) { this.ngayTao = v; }
}