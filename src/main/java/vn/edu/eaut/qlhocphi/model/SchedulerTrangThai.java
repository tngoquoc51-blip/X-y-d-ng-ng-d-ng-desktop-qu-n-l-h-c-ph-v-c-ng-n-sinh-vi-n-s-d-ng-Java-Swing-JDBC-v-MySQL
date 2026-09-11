
package vn.edu.eaut.qlhocphi.model;

import java.time.LocalDateTime;

/** Trạng thái 1 tiến trình nền (scheduler). */
public class SchedulerTrangThai {
    private String maScheduler;
    private String tenHienThi;
    private String trangThai; // DANG_CHAY | DUNG | LOI
    private LocalDateTime lanChayGanNhat;
    private String ketQuaGanNhat;
    private String ghiChu;

    public String getMaScheduler() { return maScheduler; }
    public void setMaScheduler(String v) { this.maScheduler = v; }
    public String getTenHienThi() { return tenHienThi; }
    public void setTenHienThi(String v) { this.tenHienThi = v; }
    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String v) { this.trangThai = v; }
    public LocalDateTime getLanChayGanNhat() { return lanChayGanNhat; }
    public void setLanChayGanNhat(LocalDateTime v) { this.lanChayGanNhat = v; }
    public String getKetQuaGanNhat() { return ketQuaGanNhat; }
    public void setKetQuaGanNhat(String v) { this.ketQuaGanNhat = v; }
    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String v) { this.ghiChu = v; }
}