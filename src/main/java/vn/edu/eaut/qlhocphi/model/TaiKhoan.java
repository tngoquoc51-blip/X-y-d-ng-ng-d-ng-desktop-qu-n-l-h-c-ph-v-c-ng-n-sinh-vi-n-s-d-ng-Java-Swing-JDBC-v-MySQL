package vn.edu.eaut.qlhocphi.model;

/** Tai khoan dang nhap he thong. */
public class TaiKhoan {
    private int maTK;
    private String tenDangNhap;
    private String matKhauHash;
    private String hoTen;
    private VaiTro vaiTro;
    private String maSV;      // chi co gia tri neu vaiTro = SINHVIEN
    private String googleEmail;        // Gmail duoc Admin gan de dang nhap/khoi phuc mat khau qua Google
    private boolean trangThai;
    private boolean batBuocDoiMatKhau; // true = bat buoc doi mat khau ngay khi dang nhap lan ke tiep

    public TaiKhoan() {}

    public TaiKhoan(int maTK, String tenDangNhap, String matKhauHash, String hoTen,
                    VaiTro vaiTro, String maSV, boolean trangThai) {
        this.maTK = maTK;
        this.tenDangNhap = tenDangNhap;
        this.matKhauHash = matKhauHash;
        this.hoTen = hoTen;
        this.vaiTro = vaiTro;
        this.maSV = maSV;
        this.trangThai = trangThai;
    }

    public int getMaTK() { return maTK; }
    public void setMaTK(int maTK) { this.maTK = maTK; }

    public String getTenDangNhap() { return tenDangNhap; }
    public void setTenDangNhap(String tenDangNhap) { this.tenDangNhap = tenDangNhap; }

    public String getMatKhauHash() { return matKhauHash; }
    public void setMatKhauHash(String matKhauHash) { this.matKhauHash = matKhauHash; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public VaiTro getVaiTro() { return vaiTro; }
    public void setVaiTro(VaiTro vaiTro) { this.vaiTro = vaiTro; }

    public String getMaSV() { return maSV; }
    public void setMaSV(String maSV) { this.maSV = maSV; }

    public String getGoogleEmail() { return googleEmail; }
    public void setGoogleEmail(String googleEmail) { this.googleEmail = googleEmail; }

    public boolean isTrangThai() { return trangThai; }
    public void setTrangThai(boolean trangThai) { this.trangThai = trangThai; }

    public boolean isBatBuocDoiMatKhau() { return batBuocDoiMatKhau; }
    public void setBatBuocDoiMatKhau(boolean batBuocDoiMatKhau) { this.batBuocDoiMatKhau = batBuocDoiMatKhau; }
}