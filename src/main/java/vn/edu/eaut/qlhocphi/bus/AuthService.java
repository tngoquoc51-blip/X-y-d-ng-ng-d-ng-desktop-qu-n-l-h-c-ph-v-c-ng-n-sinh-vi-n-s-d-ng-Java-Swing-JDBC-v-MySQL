package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.TaiKhoanDAO;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.util.PasswordUtils;

import java.sql.SQLException;

/** Nghiep vu xac thuc dang nhap. */
public class AuthService {
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    /** Tra ve TaiKhoan neu dang nhap thanh cong, null neu sai tai khoan/mat khau. */
    public TaiKhoan dangNhap(String tenDangNhap, String matKhau) throws SQLException {
        TaiKhoan tk = taiKhoanDAO.timTheoTenDangNhap(tenDangNhap);
        if (tk == null || !tk.isTrangThai()) return null;
        if (!PasswordUtils.matches(matKhau, tk.getMatKhauHash())) return null;
        return tk;
    }
}