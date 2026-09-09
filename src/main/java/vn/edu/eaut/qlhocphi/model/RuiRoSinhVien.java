package vn.edu.eaut.qlhocphi.model;

/** Ket qua phan tich rui ro tre han hoc phi cua 1 sinh vien, dua tren lich su
 *  thanh toan cac hoa don da qua (khong chi trang thai hien tai). */
public class RuiRoSinhVien {
    private String maSV;
    private String tenSV;
    private int tongHoaDon;
    private int soLanTre;          // so hoa don TUNG hoac DANG bi tre han thanh toan
    private double tyLeTre;        // % = soLanTre / tongHoaDon * 100
    private int soNgayTreDangNo;   // so ngay tre cua hoa don DANG no qua han (0 neu khong co)
    private String mucDoRuiRo;     // "CAO", "TRUNG_BINH", "THAP", "CHUA_DU_DU_LIEU"

    public String getMaSV() { return maSV; }
    public void setMaSV(String maSV) { this.maSV = maSV; }
    public String getTenSV() { return tenSV; }
    public void setTenSV(String tenSV) { this.tenSV = tenSV; }
    public int getTongHoaDon() { return tongHoaDon; }
    public void setTongHoaDon(int tongHoaDon) { this.tongHoaDon = tongHoaDon; }
    public int getSoLanTre() { return soLanTre; }
    public void setSoLanTre(int soLanTre) { this.soLanTre = soLanTre; }
    public double getTyLeTre() { return tyLeTre; }
    public void setTyLeTre(double tyLeTre) { this.tyLeTre = tyLeTre; }
    public int getSoNgayTreDangNo() { return soNgayTreDangNo; }
    public void setSoNgayTreDangNo(int soNgayTreDangNo) { this.soNgayTreDangNo = soNgayTreDangNo; }
    public String getMucDoRuiRo() { return mucDoRuiRo; }
    public void setMucDoRuiRo(String mucDoRuiRo) { this.mucDoRuiRo = mucDoRuiRo; }
}