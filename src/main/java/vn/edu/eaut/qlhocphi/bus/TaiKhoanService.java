package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.TaiKhoanDAO;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.VaiTro;
import vn.edu.eaut.qlhocphi.model.SinhVien;
import vn.edu.eaut.qlhocphi.util.PasswordUtils;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.List;

/**
 * Nghiep vu quan ly tai khoan he thong.
 * Chi tai khoan vai tro ADMIN moi duoc phep goi cac thao tac o day (kiem tra o tang GUI).
 */
public class TaiKhoanService {
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    private final SinhVienService sinhVienService = new SinhVienService();
    private static final SecureRandom RANDOM = new SecureRandom();

    public List<TaiKhoan> layTatCa() throws SQLException {
        return taiKhoanDAO.layTatCa();
    }

    public void themTaiKhoan(String tenDangNhap, String matKhau, String hoTen, VaiTro vaiTro, String maSV) throws SQLException {
        themTaiKhoan(tenDangNhap, matKhau, hoTen, vaiTro, maSV, null);
    }

    public void themTaiKhoan(String tenDangNhap, String matKhau, String hoTen, VaiTro vaiTro, String maSV,
                             String googleEmail) throws SQLException {
        if (tenDangNhap == null || tenDangNhap.isBlank()) {
            throw new IllegalArgumentException("Ten dang nhap khong duoc de trong");
        }
        if (matKhau == null || matKhau.length() < 6) {
            throw new IllegalArgumentException("Mat khau phai co it nhat 6 ky tu");
        }
        if (hoTen == null || hoTen.isBlank()) {
            throw new IllegalArgumentException("Ho ten khong duoc de trong");
        }
        if (taiKhoanDAO.timTheoTenDangNhap(tenDangNhap) != null) {
            throw new IllegalArgumentException("Ten dang nhap da ton tai");
        }
        if (vaiTro == VaiTro.SINHVIEN && (maSV == null || maSV.isBlank())) {
            throw new IllegalArgumentException("Tai khoan vai tro SINHVIEN phai gan Ma sinh vien");
        }
        if (googleEmail != null && !googleEmail.isBlank() && taiKhoanDAO.timTheoGoogleEmail(googleEmail.trim()) != null) {
            throw new IllegalArgumentException("Gmail nay da duoc gan cho tai khoan khac");
        }

        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap(tenDangNhap.trim());
        tk.setMatKhauHash(PasswordUtils.hash(matKhau));
        tk.setHoTen(hoTen.trim());
        tk.setVaiTro(vaiTro);
        tk.setMaSV(vaiTro == VaiTro.SINHVIEN ? maSV.trim() : null);
        tk.setGoogleEmail(googleEmail == null || googleEmail.isBlank() ? null : googleEmail.trim());
        tk.setTrangThai(true);
        taiKhoanDAO.themTaiKhoan(tk);
    }

    public void capNhatThongTin(int maTK, String hoTen, VaiTro vaiTro, String maSV, boolean trangThai) throws SQLException {
        capNhatThongTin(maTK, hoTen, vaiTro, maSV, null, trangThai);
    }

    public void capNhatThongTin(int maTK, String hoTen, VaiTro vaiTro, String maSV, String googleEmail,
                                boolean trangThai) throws SQLException {
        if (hoTen == null || hoTen.isBlank()) {
            throw new IllegalArgumentException("Ho ten khong duoc de trong");
        }
        String googleEmailChuan = googleEmail == null || googleEmail.isBlank() ? null : googleEmail.trim();
        if (googleEmailChuan != null) {
            TaiKhoan trung = taiKhoanDAO.timTheoGoogleEmail(googleEmailChuan);
            if (trung != null && trung.getMaTK() != maTK) {
                throw new IllegalArgumentException("Gmail nay da duoc gan cho tai khoan khac");
            }
        }
        TaiKhoan tk = new TaiKhoan();
        tk.setMaTK(maTK);
        tk.setHoTen(hoTen.trim());
        tk.setVaiTro(vaiTro);
        tk.setMaSV(vaiTro == VaiTro.SINHVIEN ? maSV : null);
        tk.setGoogleEmail(googleEmailChuan);
        tk.setTrangThai(trangThai);
        taiKhoanDAO.capNhat(tk);

        if (vaiTro == VaiTro.SINHVIEN && maSV != null && !maSV.isBlank()) {
            dongBoTenSinhVien(maSV, hoTen);
        }
    }

    public void doiMatKhau(int maTK, String matKhauMoi) throws SQLException {
        if (matKhauMoi == null || matKhauMoi.length() < 6) {
            throw new IllegalArgumentException("Mat khau phai co it nhat 6 ky tu");
        }
        taiKhoanDAO.doiMatKhau(maTK, PasswordUtils.hash(matKhauMoi));
    }

    /** Doi mat khau tu chinh nguoi dung, dong thoi TU DONG BO co "bat buoc doi mat khau" (neu co). */
    public void doiMatKhauVaBoCoBatBuoc(int maTK, String matKhauMoi) throws SQLException {
        if (matKhauMoi == null || matKhauMoi.length() < 6) {
            throw new IllegalArgumentException("Mat khau phai co it nhat 6 ky tu");
        }
        taiKhoanDAO.doiMatKhauVaCoBatBuoc(maTK, PasswordUtils.hash(matKhauMoi), false);
    }

    /** Khoa/mo khoa tai khoan (bat/tat TrangThai) - khong xoa du lieu, dam bao truy vet. */
    public void doiTrangThai(TaiKhoan tk, boolean trangThaiMoi) throws SQLException {
        capNhatThongTin(tk.getMaTK(), tk.getHoTen(), tk.getVaiTro(), tk.getMaSV(), tk.getGoogleEmail(), trangThaiMoi);
    }

    public void xoaTaiKhoan(int maTK, int maTKDangDangNhap) throws SQLException {
        if (maTK == maTKDangDangNhap) {
            throw new IllegalArgumentException("Khong the tu xoa tai khoan dang dang nhap");
        }
        taiKhoanDAO.xoa(maTK);
    }

    /** Admin gan/go Gmail dung de dang nhap - khoi phuc mat khau qua Google cho 1 tai khoan. */
    public void ganTaiKhoanGoogle(int maTK, String googleEmail) throws SQLException {
        String chuan = googleEmail == null || googleEmail.isBlank() ? null : googleEmail.trim();
        if (chuan != null) {
            if (!chuan.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new IllegalArgumentException("Gmail khong hop le");
            }
            TaiKhoan trung = taiKhoanDAO.timTheoGoogleEmail(chuan);
            if (trung != null && trung.getMaTK() != maTK) {
                throw new IllegalArgumentException("Gmail nay da duoc gan cho tai khoan khac");
            }
        }
        taiKhoanDAO.ganGoogleEmail(maTK, chuan);
    }

    /**
     * Tim tai khoan theo Gmail da xac thuc thanh cong qua Google (dung cho ca "Dang nhap
     * bang Google" va "Quen mat khau -> xac minh qua Google"). Tra ve null neu chua tai
     * khoan nao duoc Admin gan Gmail nay.
     */
    public TaiKhoan timTheoGoogleEmail(String googleEmail) throws SQLException {
        if (googleEmail == null || googleEmail.isBlank()) return null;
        return taiKhoanDAO.timTheoGoogleEmail(googleEmail.trim());
    }

    /**
     * Dat 1 mat khau ngau nhien (tam) cho tai khoan va BAT co "bat buoc doi mat khau" -
     * dung ngay sau khi xac thuc danh tinh thanh cong qua Google trong luong "Quen mat
     * khau". Nguoi dung se bi ep doi mat khau khac ngay khi vao duoc man hinh chinh.
     */
    public void datLaiMatKhauNgauNhienVaBatBuocDoi(int maTK) throws SQLException {
        String matKhauTam = taoMatKhauNgauNhien();
        taiKhoanDAO.doiMatKhauVaCoBatBuoc(maTK, PasswordUtils.hash(matKhauTam), true);
    }

    private String taoMatKhauNgauNhien() {
        String bang = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            sb.append(bang.charAt(RANDOM.nextInt(bang.length())));
        }
        return sb.toString();
    }

    /**
     * Dong bo lai Ho ten trong bang SinhVien khi Admin sua ten cho 1 tai khoan co lien ket Ma SV.
     * Neu khong lam viec nay, ten se lech nhau giua header (lay tu TaiKhoan.HoTen) va banner
     * Tong quan cua sinh vien (lay tu SinhVien.HoTen) - dung 2 nguon du lieu doc lap. Chi doi
     * dung truong Ho ten, giu nguyen moi du lieu khac (Lop, Khoa, Email, SDT, Trang thai...).
     */
    private void dongBoTenSinhVien(String maSV, String hoTenMoi) throws SQLException {
        SinhVien sv = sinhVienService.timTheoMa(maSV);
        if (sv == null) return;
        if (hoTenMoi.trim().equals(sv.getHoTen())) return;
        sv.setHoTen(hoTenMoi.trim());
        sinhVienService.capNhat(sv);
    }
}