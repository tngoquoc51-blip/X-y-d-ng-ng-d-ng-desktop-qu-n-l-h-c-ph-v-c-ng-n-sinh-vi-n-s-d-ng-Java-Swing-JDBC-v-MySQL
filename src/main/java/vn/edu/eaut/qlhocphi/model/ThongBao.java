package vn.edu.eaut.qlhocphi.model;

import java.time.LocalDateTime;

public class ThongBao {
    private int maThongBao;
    private String manHinhKey;
    private String vaiTroNhan;
    private String maSVNhan;
    private String tieuDe;
    private String noiDung;
    private LocalDateTime thoiGianTao;
    /** Chi dung o man hinh hop thu sinh vien (khong map tu DB truc tiep o moi query). */
    private boolean daDoc;

    public int getMaThongBao() { return maThongBao; }
    public void setMaThongBao(int maThongBao) { this.maThongBao = maThongBao; }

    public String getManHinhKey() { return manHinhKey; }
    public void setManHinhKey(String manHinhKey) { this.manHinhKey = manHinhKey; }

    public String getVaiTroNhan() { return vaiTroNhan; }
    public void setVaiTroNhan(String vaiTroNhan) { this.vaiTroNhan = vaiTroNhan; }

    public String getMaSVNhan() { return maSVNhan; }
    public void setMaSVNhan(String maSVNhan) { this.maSVNhan = maSVNhan; }

    public String getTieuDe() { return tieuDe; }
    public void setTieuDe(String tieuDe) { this.tieuDe = tieuDe; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public LocalDateTime getThoiGianTao() { return thoiGianTao; }
    public void setThoiGianTao(LocalDateTime thoiGianTao) { this.thoiGianTao = thoiGianTao; }

    public boolean isDaDoc() { return daDoc; }
    public void setDaDoc(boolean daDoc) { this.daDoc = daDoc; }
}