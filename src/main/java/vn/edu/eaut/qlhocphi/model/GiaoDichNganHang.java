package vn.edu.eaut.qlhocphi.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 1 dong giao dich chuyen khoan lay tu sao ke ngan hang, cho doi soat thu hoc phi tu dong. */
public class GiaoDichNganHang {
    private int maGiaoDich;
    private String maThamChieu;
    private LocalDateTime thoiGianGiaoDich;
    private BigDecimal soTien;
    private String noiDung;
    private String trangThaiDoiSoat = "CHUA_XU_LY"; // CHUA_XU_LY, TU_DONG_KHOP, NGHI_VAN, DA_XAC_NHAN, BO_QUA
    private Integer maHoaDonKhop;
    private Integer doTinCay;
    private String nguoiXacNhan;
    private LocalDateTime thoiGianXacNhan;
    private LocalDateTime thoiGianNhap;

    /** Cac truong ho tro hien thi tren GUI (khong luu CSDL) - dien khi ghep voi HoaDonHocPhi da khop. */
    private transient String tenSVGoiY;
    private transient String maSVGoiY;
    private transient String tenHocKyGoiY;

    public GiaoDichNganHang() {}

    public int getMaGiaoDich() { return maGiaoDich; }
    public void setMaGiaoDich(int maGiaoDich) { this.maGiaoDich = maGiaoDich; }

    public String getMaThamChieu() { return maThamChieu; }
    public void setMaThamChieu(String maThamChieu) { this.maThamChieu = maThamChieu; }

    public LocalDateTime getThoiGianGiaoDich() { return thoiGianGiaoDich; }
    public void setThoiGianGiaoDich(LocalDateTime thoiGianGiaoDich) { this.thoiGianGiaoDich = thoiGianGiaoDich; }

    public BigDecimal getSoTien() { return soTien; }
    public void setSoTien(BigDecimal soTien) { this.soTien = soTien; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public String getTrangThaiDoiSoat() { return trangThaiDoiSoat; }
    public void setTrangThaiDoiSoat(String trangThaiDoiSoat) { this.trangThaiDoiSoat = trangThaiDoiSoat; }

    public Integer getMaHoaDonKhop() { return maHoaDonKhop; }
    public void setMaHoaDonKhop(Integer maHoaDonKhop) { this.maHoaDonKhop = maHoaDonKhop; }

    public Integer getDoTinCay() { return doTinCay; }
    public void setDoTinCay(Integer doTinCay) { this.doTinCay = doTinCay; }

    public String getNguoiXacNhan() { return nguoiXacNhan; }
    public void setNguoiXacNhan(String nguoiXacNhan) { this.nguoiXacNhan = nguoiXacNhan; }

    public LocalDateTime getThoiGianXacNhan() { return thoiGianXacNhan; }
    public void setThoiGianXacNhan(LocalDateTime thoiGianXacNhan) { this.thoiGianXacNhan = thoiGianXacNhan; }

    public LocalDateTime getThoiGianNhap() { return thoiGianNhap; }
    public void setThoiGianNhap(LocalDateTime thoiGianNhap) { this.thoiGianNhap = thoiGianNhap; }

    public String getTenSVGoiY() { return tenSVGoiY; }
    public void setTenSVGoiY(String tenSVGoiY) { this.tenSVGoiY = tenSVGoiY; }

    public String getMaSVGoiY() { return maSVGoiY; }
    public void setMaSVGoiY(String maSVGoiY) { this.maSVGoiY = maSVGoiY; }

    public String getTenHocKyGoiY() { return tenHocKyGoiY; }
    public void setTenHocKyGoiY(String tenHocKyGoiY) { this.tenHocKyGoiY = tenHocKyGoiY; }
}