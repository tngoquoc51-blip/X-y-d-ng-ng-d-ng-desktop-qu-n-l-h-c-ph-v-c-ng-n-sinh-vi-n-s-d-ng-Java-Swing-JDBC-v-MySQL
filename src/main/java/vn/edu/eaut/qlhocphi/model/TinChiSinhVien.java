
package vn.edu.eaut.qlhocphi.model;

import java.time.LocalDateTime;

/** Tổng hợp tín chỉ của một sinh viên. */
public class TinChiSinhVien {
    private String maSV;
    private int tinChiTichLuy;
    private int tinChiBiRut;
    private int tinChiDangKy;
    private LocalDateTime capNhatLuc;
    // Thông tin kèm từ SinhVien (join)
    private String hoTen;
    private Integer namNhapHoc;
    private Integer namThu;
    private String trangThaiHoc;

    public String getMaSV() { return maSV; }
    public void setMaSV(String maSV) { this.maSV = maSV; }
    public int getTinChiTichLuy() { return tinChiTichLuy; }
    public void setTinChiTichLuy(int tinChiTichLuy) { this.tinChiTichLuy = tinChiTichLuy; }
    public int getTinChiBiRut() { return tinChiBiRut; }
    public void setTinChiBiRut(int tinChiBiRut) { this.tinChiBiRut = tinChiBiRut; }
    public int getTinChiDangKy() { return tinChiDangKy; }
    public void setTinChiDangKy(int tinChiDangKy) { this.tinChiDangKy = tinChiDangKy; }
    public LocalDateTime getCapNhatLuc() { return capNhatLuc; }
    public void setCapNhatLuc(LocalDateTime capNhatLuc) { this.capNhatLuc = capNhatLuc; }
    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }
    public Integer getNamNhapHoc() { return namNhapHoc; }
    public void setNamNhapHoc(Integer namNhapHoc) { this.namNhapHoc = namNhapHoc; }
    public Integer getNamThu() { return namThu; }
    public void setNamThu(Integer namThu) { this.namThu = namThu; }
    public String getTrangThaiHoc() { return trangThaiHoc; }
    public void setTrangThaiHoc(String trangThaiHoc) { this.trangThaiHoc = trangThaiHoc; }

    public int getTinChiConHieuLuc() {
        return Math.max(0, tinChiTichLuy);
    }
}