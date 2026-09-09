package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.NhatKyHeThongDAO;
import vn.edu.eaut.qlhocphi.model.NhatKyHeThong;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;

import java.sql.SQLException;
import java.util.List;

public class NhatKyHeThongService {
    private final NhatKyHeThongDAO dao = new NhatKyHeThongDAO();

    /** Dung khi da co AuditContext (nguoi dung dang lam viec sau khi dang nhap xong). */
    public void ghi(String hanhDong, String doiTuong, String chiTiet) {
        TaiKhoan tk = AuditContext.layNguoiDung();
        ghi(tk, hanhDong, doiTuong, chiTiet);
    }

    /** Dung khi CHUA co AuditContext, VD: ngay luc dang nhap - truyen thang TaiKhoan vao,
     *  khong phu thuoc AuditContext, de ghi dung ten nguoi dang nhap thay vi "he_thong". */
    public void ghi(TaiKhoan taiKhoan, String hanhDong, String doiTuong, String chiTiet) {
        try {
            NhatKyHeThong nk = new NhatKyHeThong();
            nk.setMaTK(taiKhoan != null ? taiKhoan.getMaTK() : null);
            nk.setTenDangNhap(taiKhoan != null ? taiKhoan.getTenDangNhap() : "he_thong");
            nk.setHanhDong(hanhDong);
            nk.setDoiTuong(doiTuong != null ? doiTuong : (taiKhoan != null ? taiKhoan.getVaiTro().toString() : "TaiKhoan"));
            nk.setChiTiet(chiTiet);
            dao.ghi(nk);
        } catch (SQLException ex) {
            System.err.println("Khong the ghi nhat ky he thong: " + ex.getMessage());
        }
    }

    public List<NhatKyHeThong> layGanNhat() throws SQLException {
        return dao.layGanNhat();
    }
}