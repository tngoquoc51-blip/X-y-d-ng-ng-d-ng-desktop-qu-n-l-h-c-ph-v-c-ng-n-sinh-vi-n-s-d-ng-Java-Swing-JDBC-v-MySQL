package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.AdminConfigDAO;
import vn.edu.eaut.qlhocphi.model.*;

import java.util.List;
import java.util.Map;

/** Service lớp nghiệp vụ cho cấu hình Admin. */
public class AdminConfigService {
    private final AdminConfigDAO dao = new AdminConfigDAO();

    public List<CauHinhHeThong> layCauHinhTheoNhom(String nhom) throws Exception {
        return dao.layTheoNhom(nhom);
    }

    public List<CauHinhHeThong> layTatCaCauHinh() throws Exception {
        return dao.layTatCaCauHinh();
    }

    public void luuNhieuCauHinh(Map<String, String> khoaGiaTri, String nhom, String nguoi) throws Exception {
        for (Map.Entry<String, String> e : khoaGiaTri.entrySet()) {
            dao.luuCauHinh(nhom, e.getKey(), e.getValue(), nguoi);
        }
    }

    public void luuMotCauHinh(String nhom, String khoa, String giaTri, String nguoi) throws Exception {
        dao.luuCauHinh(nhom, khoa, giaTri, nguoi);
    }

    public String layGiaTri(String nhom, String khoa, String macDinh) {
        try {
            String v = dao.layGiaTri(nhom, khoa);
            return (v == null || v.isBlank()) ? macDinh : v;
        } catch (Exception e) {
            return macDinh;
        }
    }

    public CauHinhBaoMat layCauHinhBaoMat() throws Exception {
        return dao.layCauHinhBaoMat();
    }

    public void luuCauHinhBaoMat(CauHinhBaoMat b) throws Exception {
        if (b.getDoDaiMatKhauToiThieu() < 4 || b.getDoDaiMatKhauToiThieu() > 32)
            throw new IllegalArgumentException("Độ dài mật khẩu tối thiểu phải từ 4–32");
        if (b.getSessionTimeoutPhut() < 5)
            throw new IllegalArgumentException("Session timeout tối thiểu 5 phút");
        if (b.getSoLanDangNhapSaiToiDa() < 1)
            throw new IllegalArgumentException("Số lần đăng nhập sai tối thiểu là 1");
        dao.luuCauHinhBaoMat(b);
    }

    public List<SchedulerTrangThai> layTrangThaiScheduler() throws Exception {
        return dao.layTatCaScheduler();
    }

    public void ghiNhanScheduler(String ma, String trangThai, String ketQua) {
        try { dao.capNhatScheduler(ma, trangThai, ketQua); } catch (Exception ignored) {}
    }

    public List<MauThongBao> layMauThongBao() throws Exception {
        return dao.layTatCaMau();
    }

    public void capNhatMau(MauThongBao m) throws Exception {
        if (m.getNoiDung() == null || m.getNoiDung().isBlank())
            throw new IllegalArgumentException("Nội dung mẫu không được trống");
        dao.capNhatMau(m);
    }

    public List<ThongBaoBroadcast> layTatCaBroadcast() throws Exception {
        return dao.layTatCaBroadcast();
    }

    public List<ThongBaoBroadcast> layBroadcastDangHieuLuc() throws Exception {
        return dao.layBroadcastDangHieuLuc();
    }

    public void themBroadcast(ThongBaoBroadcast b) throws Exception {
        if (b.getTieuDe() == null || b.getTieuDe().isBlank())
            throw new IllegalArgumentException("Tiêu đề không được trống");
        if (b.getNoiDung() == null || b.getNoiDung().isBlank())
            throw new IllegalArgumentException("Nội dung không được trống");
        if (b.getHienThiTu() == null)
            throw new IllegalArgumentException("Phải chọn thời gian bắt đầu hiển thị");
        dao.themBroadcast(b);
    }

    public void capNhatBroadcast(ThongBaoBroadcast b) throws Exception {
        dao.capNhatBroadcast(b);
    }

    public void xoaBroadcast(int ma) throws Exception {
        dao.xoaBroadcast(ma);
    }
}