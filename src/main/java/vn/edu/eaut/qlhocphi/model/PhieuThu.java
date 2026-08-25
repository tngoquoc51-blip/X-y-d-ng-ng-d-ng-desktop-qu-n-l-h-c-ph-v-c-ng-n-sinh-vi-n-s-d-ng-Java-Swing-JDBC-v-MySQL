package vn.edu.eaut.qlhocphi.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Phieu thu - 1 lan nop tien cho 1 hoa don hoc phi. */
public class PhieuThu {
    private int maPhieuThu;
    private int maHoaDon;
    private BigDecimal soTienNop;
    private LocalDateTime ngayNop;
    private String hinhThuc;   // TIEN_MAT, CHUYEN_KHOAN, THANH_TOAN_ONLINE
    private String maGiaoDich;
    private String nguoiThu;

    // 2 truong chi phuc vu hien thi (JOIN tu HoaDonHocPhi/SinhVien), khong luu vao PhieuThu.
    private String tenSV;
    private String tenHocKy;

    public PhieuThu() {}

    public int getMaPhieuThu() { return maPhieuThu; }
    public void setMaPhieuThu(int maPhieuThu) { this.maPhieuThu = maPhieuThu; }

    public int getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(int maHoaDon) { this.maHoaDon = maHoaDon; }

    public BigDecimal getSoTienNop() { return soTienNop; }
    public void setSoTienNop(BigDecimal soTienNop) { this.soTienNop = soTienNop; }

    public LocalDateTime getNgayNop() { return ngayNop; }
    public void setNgayNop(LocalDateTime ngayNop) { this.ngayNop = ngayNop; }

    public String getHinhThuc() { return hinhThuc; }
    public void setHinhThuc(String hinhThuc) { this.hinhThuc = hinhThuc; }

    public String getMaGiaoDich() { return maGiaoDich; }
    public void setMaGiaoDich(String maGiaoDich) { this.maGiaoDich = maGiaoDich; }

    public String getNguoiThu() { return nguoiThu; }
    public void setNguoiThu(String nguoiThu) { this.nguoiThu = nguoiThu; }

    public String getTenSV() { return tenSV; }
    public void setTenSV(String tenSV) { this.tenSV = tenSV; }

    public String getTenHocKy() { return tenHocKy; }
    public void setTenHocKy(String tenHocKy) { this.tenHocKy = tenHocKy; }
}