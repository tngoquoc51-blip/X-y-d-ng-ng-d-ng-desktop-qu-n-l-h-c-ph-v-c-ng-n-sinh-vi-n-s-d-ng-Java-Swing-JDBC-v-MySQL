package vn.edu.eaut.qlhocphi.model;

/** Mẫu nội dung Email / SMS. */
public class MauThongBao {
    private int maMau;
    private String maLoai;
    private String kenh; // EMAIL | SMS
    private String tieuDe;
    private String noiDung;
    private String bienHoTro;
    private boolean dangDung;

    public int getMaMau() { return maMau; }
    public void setMaMau(int v) { this.maMau = v; }
    public String getMaLoai() { return maLoai; }
    public void setMaLoai(String v) { this.maLoai = v; }
    public String getKenh() { return kenh; }
    public void setKenh(String v) { this.kenh = v; }
    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String v) { this.tieuDe = v; }
    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String v) { this.noiDung = v; }
    public String getBienHoTro() { return bienHoTro; }
    public void setBienHoTro(String v) { this.bienHoTro = v; }
    public boolean isDangDung() { return dangDung; }
    public void setDangDung(boolean v) { this.dangDung = v; }
}