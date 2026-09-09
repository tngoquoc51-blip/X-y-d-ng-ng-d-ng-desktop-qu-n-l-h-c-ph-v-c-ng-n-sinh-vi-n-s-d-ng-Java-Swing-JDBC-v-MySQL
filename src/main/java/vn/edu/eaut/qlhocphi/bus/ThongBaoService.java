package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.ThongBaoDAO;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.ThongBao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ThongBaoService {
    private final ThongBaoDAO dao = new ThongBaoDAO();

    /** Goi tu bat ky diem nao trong nghiep vu khi co "bien dong" can bao cho nguoi dung.
     *  vaiTroNhan: "ADMIN", "KETOAN", "SINHVIEN" hoac "ALL" (ca admin lan ke toan).
     *  maSVNhan: chi dien khi vaiTroNhan="SINHVIEN" va muon bao rieng cho 1 sinh vien; con lai de null. */
    public void taoThongBao(String manHinhKey, String vaiTroNhan, String maSVNhan, String tieuDe, String noiDung) {
        try {
            dao.taoThongBao(manHinhKey, vaiTroNhan, maSVNhan, tieuDe, noiDung);
        } catch (SQLException ex) {
            System.err.println("Khong the tao thong bao: " + ex.getMessage());
        }
    }

    public Map<String, Integer> demChuaDocTheoManHinh(TaiKhoan taiKhoan) throws SQLException {
        return dao.demChuaDocTheoManHinh(taiKhoan.getMaTK(), taiKhoan.getVaiTro().toString(), taiKhoan.getMaSV());
    }

    public void danhDauDaDoc(TaiKhoan taiKhoan, String manHinhKey) throws SQLException {
        dao.danhDauDaDocTheoManHinh(taiKhoan.getMaTK(), taiKhoan.getVaiTro().toString(), taiKhoan.getMaSV(), manHinhKey);
    }

    public List<ThongBao> layChuaDoc(TaiKhoan taiKhoan, String manHinhKey) throws SQLException {
        return dao.layChuaDocTheoManHinh(taiKhoan.getMaTK(), taiKhoan.getVaiTro().toString(), taiKhoan.getMaSV(), manHinhKey);
    }
}