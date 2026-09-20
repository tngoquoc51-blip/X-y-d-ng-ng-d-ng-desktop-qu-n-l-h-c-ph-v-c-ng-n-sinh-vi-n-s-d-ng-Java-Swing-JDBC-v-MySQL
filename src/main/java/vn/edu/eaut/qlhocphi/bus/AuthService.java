package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.TaiKhoanDAO;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.VaiTro;
import vn.edu.eaut.qlhocphi.util.PasswordUtils;

import java.sql.SQLException;

/** Nghiep vu xac thuc dang nhap. */
public class AuthService {
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    public TaiKhoan dangNhap(String tenDangNhap, String matKhau) throws SQLException {
        TaiKhoan tk = taiKhoanDAO.timTheoTenDangNhap(tenDangNhap);
        if (tk == null || !tk.isTrangThai()) return null;
        if (!PasswordUtils.matches(matKhau, tk.getMatKhauHash())) return null;
        return tk;
    }

    /** Cổng cán bộ: Admin / Phòng đào tạo / Kế toán */
    public TaiKhoan dangNhapCanBo(String tenDangNhap, String matKhau) throws SQLException {
        TaiKhoan tk = dangNhap(tenDangNhap, matKhau);
        if (tk == null) return null;
        if (tk.getVaiTro() == VaiTro.SINHVIEN) return null;
        return tk;
    }

    /** Cổng sinh viên */
    public TaiKhoan dangNhapSinhVien(String tenDangNhap, String matKhau) throws SQLException {
        TaiKhoan tk = dangNhap(tenDangNhap, matKhau);
        if (tk == null) return null;
        if (tk.getVaiTro() != VaiTro.SINHVIEN) return null;
        return tk;
    }
}