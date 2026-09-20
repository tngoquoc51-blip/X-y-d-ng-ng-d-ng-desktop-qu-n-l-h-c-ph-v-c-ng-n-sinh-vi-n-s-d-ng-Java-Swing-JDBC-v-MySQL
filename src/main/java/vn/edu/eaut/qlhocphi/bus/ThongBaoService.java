package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.ThongBaoDAO;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.ThongBao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ThongBaoService {
    private final ThongBaoDAO dao = new ThongBaoDAO();

    /**
     * Goi tu bat ky diem nao trong nghiep vu khi co "bien dong" can bao cho nguoi dung.
     * vaiTroNhan: "ADMIN", "KETOAN", "SINHVIEN", "PHONGDAOTAO" hoac "ALL".
     * maSVNhan: chi dien khi muon bao rieng 1 sinh vien; con lai de null.
     */
    public void taoThongBao(String manHinhKey, String vaiTroNhan, String maSVNhan, String tieuDe, String noiDung) {
        try {
            dao.taoThongBao(manHinhKey, vaiTroNhan, maSVNhan, tieuDe, noiDung);
        } catch (SQLException ex) {
            System.err.println("Khong the tao thong bao: " + ex.getMessage());
        }
    }

    /**
     * Phong dao tao / nha truong gui thong bao cho toan bo sinh vien (hoac 1 SV).
     * manHinhKey mac dinh "thongbao_sv" de hien o hop thu sinh vien.
     */
    public void guiThongBaoSinhVien(String maSVNhanHoacNull, String tieuDe, String noiDung) {
        taoThongBao("thongbao_sv", "SINHVIEN", maSVNhanHoacNull, tieuDe, noiDung);
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

    public List<ThongBao> layTatCaChoSinhVien(TaiKhoan taiKhoan) throws SQLException {
        return dao.layTatCaChoSinhVien(taiKhoan.getMaTK(), taiKhoan.getMaSV());
    }

    public int demChuaDocSinhVien(TaiKhoan taiKhoan) throws SQLException {
        return dao.demChuaDocSinhVien(taiKhoan.getMaTK(), taiKhoan.getMaSV());
    }

    public void danhDauDaDocMot(TaiKhoan taiKhoan, int maThongBao) throws SQLException {
        dao.danhDauDaDocMot(taiKhoan.getMaTK(), maThongBao);
    }

    public List<ThongBao> layHopThuTheoVaiTro(TaiKhoan taiKhoan, String manHinhKey) throws SQLException {
        return dao.layTatCaTheoVaiTro(taiKhoan.getMaTK(), taiKhoan.getVaiTro().toString(), manHinhKey);
    }

    public void danhDauTatCaDaDocSinhVien(TaiKhoan taiKhoan) throws SQLException {
        dao.danhDauTatCaDaDocSinhVien(taiKhoan.getMaTK(), taiKhoan.getMaSV());
    }
}