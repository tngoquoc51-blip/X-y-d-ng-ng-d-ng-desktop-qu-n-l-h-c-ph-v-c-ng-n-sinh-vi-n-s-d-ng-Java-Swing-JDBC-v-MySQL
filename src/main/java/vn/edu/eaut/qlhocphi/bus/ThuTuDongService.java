package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.HoaDonDAO;
import vn.edu.eaut.qlhocphi.dal.LichThuTuDongDAO;
import vn.edu.eaut.qlhocphi.dal.PhieuThuDAO;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.LichThuTuDong;
import vn.edu.eaut.qlhocphi.model.PhieuThu;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * "Uy quyen trich no tu dong": Admin/Ke toan dang ky 1 khoang ngay quet cho 1 hoa
 * don; trong khoang do, he thong (qua TuDongThuHocPhiScheduler) tu kiem tra so du
 * Vi hoc phi dien tu cua sinh vien, du tien thi tu dong tru + tao PhieuThu, khong
 * can ai thao tac thu cong. Neu chua du tien, giu nguyen "DANG_CHO" de quet lai
 * vao lan sau (khong bao loi, khong huy).
 */
public class ThuTuDongService {
    private final LichThuTuDongDAO lichDAO = new LichThuTuDongDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();
    private final ViDienTuService viDienTuService = new ViDienTuService();
    private final NhatKyHeThongService nhatKyHeThongService = new NhatKyHeThongService();

    public static class KetQuaQuet {
        public int tongQuet, thuThanhCong, hetHan, boQuaChuaDuTien;
    }

    /** Admin/Ke toan tao 1 "uy quyen" quet cho 1 hoa don con no, trong 1 khoang ngay. */
    public int taoLichThu(int maHoaDon, LocalDate ngayBatDau, LocalDate ngayKetThuc, String nguoiTao) throws SQLException {
        if (ngayKetThuc.isBefore(ngayBatDau)) {
            throw new IllegalArgumentException("Ngay ket thuc quet phai sau ngay bat dau");
        }
        HoaDonHocPhi hd = hoaDonDAO.timTheoMa(maHoaDon);
        if (hd == null) throw new IllegalArgumentException("Hoa don khong ton tai");
        if (hd.tinhConNo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Hoa don nay da dong du, khong can lap lich thu");
        }

        LichThuTuDong lich = new LichThuTuDong();
        lich.setMaHoaDon(maHoaDon);
        lich.setNgayBatDauQuet(ngayBatDau);
        lich.setNgayKetThucQuet(ngayKetThuc);
        lich.setNguoiTao(nguoiTao);
        int maLich = lichDAO.them(lich);

        nhatKyHeThongService.ghi("TAO_LICH_THU_TU_DONG", "HoaDon #" + maHoaDon,
                "Dang ky quet tu dong tu " + ngayBatDau + " den " + ngayKetThuc);
        return maLich;
    }

    /**
     * Quet 1 lan toan bo cac lich DANG_CHO. Goi dinh ky boi scheduler, hoac goi
     * thu cong tu nut "Quet thu ngay" tren giao dien quan tri.
     */
    public KetQuaQuet quetMotLan() throws SQLException {
        KetQuaQuet kq = new KetQuaQuet();
        List<LichThuTuDong> dangCho = lichDAO.layDangCho();
        LocalDate homNay = LocalDate.now();

        for (LichThuTuDong lich : dangCho) {
            kq.tongQuet++;

            if (homNay.isAfter(lich.getNgayKetThucQuet())) {
                lichDAO.capNhatTrangThai(lich.getMaLich(), "HET_HAN");
                kq.hetHan++;
                continue;
            }
            if (homNay.isBefore(lich.getNgayBatDauQuet())) {
                continue; // chua den ngay bat dau quet
            }

            HoaDonHocPhi hd = hoaDonDAO.timTheoMa(lich.getMaHoaDon());
            if (hd == null) { lichDAO.capNhatTrangThai(lich.getMaLich(), "DA_HUY"); continue; }

            BigDecimal conNo = hd.tinhConNo();
            if (conNo.compareTo(BigDecimal.ZERO) <= 0) {
                // Da duoc dong bang cach khac (VD: nop tien mat truc tiep) trong luc cho quet
                lichDAO.capNhatTrangThai(lich.getMaLich(), "DA_THU");
                kq.thuThanhCong++;
                continue;
            }

            boolean duTien = viDienTuService.truTienNeuDu(hd.getMaSV(), conNo);
            if (!duTien) {
                lichDAO.capNhatLanQuet(lich.getMaLich()); // van DANG_CHO, thu lai lan sau
                kq.boQuaChuaDuTien++;
                continue;
            }

            PhieuThu pt = new PhieuThu();
            pt.setMaHoaDon(hd.getMaHoaDon());
            pt.setSoTienNop(conNo);
            pt.setHinhThuc("VI_DIEN_TU");
            pt.setNguoiThu("He thong (tu dong tru vi)");
            phieuThuDAO.them(pt);

            lichDAO.capNhatTrangThai(lich.getMaLich(), "DA_THU");
            kq.thuThanhCong++;

            nhatKyHeThongService.ghi(null, "TU_DONG_THU_HOC_PHI", "HoaDon #" + hd.getMaHoaDon(),
                    "Da tu dong tru " + conNo + " tu Vi hoc phi cua SV " + hd.getMaSV());
        }
        return kq;
    }

    public List<LichThuTuDong> layTatCaLich() throws SQLException {
        return lichDAO.layTatCa();
    }

    public void huyLich(int maLich) throws SQLException {
        lichDAO.huy(maLich);
    }
}