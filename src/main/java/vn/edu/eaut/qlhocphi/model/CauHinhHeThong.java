package vn.edu.eaut.qlhocphi.model;

/** Cấu hình hệ thống kỹ thuật (SMTP, SMS, VNPay, MoMo, OAuth) – key-value. */
public class CauHinhHeThong {
    private int maCauHinh;
    private String nhom;
    private String khoa;
    private String giaTri;
    private String moTa;

    public CauHinhHeThong() {}
    public CauHinhHeThong(String nhom, String khoa, String giaTri, String moTa) {
        this.nhom = nhom; this.khoa = khoa; this.giaTri = giaTri; this.moTa = moTa;
    }

    public int getMaCauHinh() { return maCauHinh; }
    public void setMaCauHinh(int v) { this.maCauHinh = v; }
    public String getNhom() { return nhom; }
    public void setNhom(String v) { this.nhom = v; }
    public String getKhoa() { return khoa; }
    public void setKhoa(String v) { this.khoa = v; }
    public String getGiaTri() { return giaTri; }
    public void setGiaTri(String v) { this.giaTri = v; }
    public String getMoTa() { return moTa; }
    public void setMoTa(String v) { this.moTa = v; }
}