package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.CauHinhNhacNoTuDongDAO;
import vn.edu.eaut.qlhocphi.dal.HoaDonDAO;
import vn.edu.eaut.qlhocphi.dal.SinhVienDAO;
import vn.edu.eaut.qlhocphi.model.CauHinhNhacNoTuDong;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.SinhVien;
import vn.edu.eaut.qlhocphi.util.EmailUtils;
import vn.edu.eaut.qlhocphi.util.MoneyUtils;
import vn.edu.eaut.qlhocphi.util.SmsUtils;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Nhac no tu dong theo THOI GIAN THUC, dua tren 1 "Khung thoi gian thu hoc phi"
 * DUY NHAT do Admin dat (ap dung chung cho TOAN BO sinh vien, khong phan biet
 * han rieng cua tung hoa don):
 *
 * 1) Sau [NgayGioKetThuc + 1 phut] cua khung thoi gian dang ap dung, neu hoa don
 *    van con no -> tu dong gui EMAIL cho SINH VIEN (chi gui 1 lan).
 * 2) Sau dung 1 phut ke tu luc gui email o buoc 1, neu VAN con no -> tu dong gui
 *    SMS cho PHU HUYNH (chi gui 1 lan).
 * Scheduler goi ham quetVaGuiNhacNo() moi 1 PHUT de dat do chinh xac theo phut.
 */
public class NhacNoTuDongService {
    /** So phut cho tu luc ket thuc khung gio -> gui email SV lan dau. */
    private static final long PHUT_CHO_SAU_KET_THUC = 1;
    /** So phut cho tu luc gui email SV -> leo thang len SMS phu huynh. */
    private static final long PHUT_CHO_LEO_THANG_SMS = 1;

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final SinhVienDAO sinhVienDAO = new SinhVienDAO();
    private final CauHinhNhacNoTuDongDAO cauHinhDAO = new CauHinhNhacNoTuDongDAO();
    private final NhatKyHeThongService nhatKyHeThongService = new NhatKyHeThongService();

    public static class KetQuaNhacNo {
        public int tongQuaHan, daGuiEmailSV, daGuiSmsPH, boQuaThieuLienHe, boQuaChuaDenGio;
        public boolean coCauHinhDangApDung;
    }

    public KetQuaNhacNo quetVaGuiNhacNo() throws SQLException {
        KetQuaNhacNo kq = new KetQuaNhacNo();
        LocalDateTime bayGio = LocalDateTime.now();

        CauHinhNhacNoTuDong cauHinh = cauHinhDAO.layCauHinhDangApDung();
        if (cauHinh == null) {
            kq.coCauHinhDangApDung = false;
            return kq; // Chua co khung thoi gian nao duoc Admin bat -> khong lam gi ca
        }
        kq.coCauHinhDangApDung = true;

        LocalDateTime motPhutSauKetThuc = cauHinh.getNgayGioKetThuc().plusMinutes(PHUT_CHO_SAU_KET_THUC);
        if (bayGio.isBefore(motPhutSauKetThuc)) {
            return kq; // Chua den thoi diem duoc phep gui (con truoc [KetThuc + 1 phut])
        }

        List<HoaDonHocPhi> tatCa = hoaDonDAO.layTatCa();
        for (HoaDonHocPhi hd : tatCa) {
            if (hd.tinhConNo().compareTo(BigDecimal.ZERO) <= 0) continue; // da dong du, bo qua
            kq.tongQuaHan++;

            SinhVien sv = sinhVienDAO.timTheoMa(hd.getMaSV());
            if (sv == null) continue;

            if (hd.getThoiDiemGuiEmailSV() == null) {
                // Chua tung gui email lan nao -> gui NGAY (da qua dieu kien >= KetThuc+1p o tren)
                if (sv.getEmail() == null || sv.getEmail().isBlank()) {
                    kq.boQuaThieuLienHe++;
                    continue;
                }
                boolean ok = EmailUtils.guiNhacHocPhi(sv.getEmail(), sv.getHoTen(),
                        hd.getTenHocKy(), MoneyUtils.format(hd.tinhConNo()));
                if (ok) {
                    hoaDonDAO.capNhatThoiDiemGuiEmail(hd.getMaHoaDon(), bayGio);
                    kq.daGuiEmailSV++;
                    nhatKyHeThongService.ghi(null, "TU_DONG_NHAC_NO_EMAIL", "HoaDon #" + hd.getMaHoaDon(),
                            "Tu dong gui email nhac no cho SV " + sv.getMaSV()
                                    + " luc " + bayGio + " (sau khung thu ket thuc " + PHUT_CHO_SAU_KET_THUC + " phut)");
                }
            } else if (hd.getThoiDiemGuiSmsPH() == null) {
                // Da gui email roi, kiem tra du 1 phut chua thi leo thang SMS
                LocalDateTime motPhutSauEmail = hd.getThoiDiemGuiEmailSV().plusMinutes(PHUT_CHO_LEO_THANG_SMS);
                if (bayGio.isBefore(motPhutSauEmail)) {
                    kq.boQuaChuaDenGio++;
                    continue; // chua du 1 phut ke tu luc gui email
                }

                if (sv.getSoDienThoaiPhuHuynh() == null || sv.getSoDienThoaiPhuHuynh().isBlank()) {
                    kq.boQuaThieuLienHe++;
                    continue;
                }
                long soPhutQuaHan = ChronoUnit.MINUTES.between(cauHinh.getNgayGioKetThuc(), bayGio);
                boolean ok = SmsUtils.guiSmsNhacNoPhuHuynh(sv.getSoDienThoaiPhuHuynh(), sv.getHoTen(), sv.getMaSV(),
                        MoneyUtils.format(hd.tinhConNo()), (int) (soPhutQuaHan / (24 * 60))); // quy doi tham khao ra "ngay" cho de doc, co the la 0
                if (ok) {
                    hoaDonDAO.capNhatThoiDiemGuiSms(hd.getMaHoaDon(), bayGio);
                    kq.daGuiSmsPH++;
                    nhatKyHeThongService.ghi(null, "TU_DONG_NHAC_NO_SMS", "HoaDon #" + hd.getMaHoaDon(),
                            "Tu dong gui SMS nhac no cho PHU HUYNH cua SV " + sv.getMaSV()
                                    + " luc " + bayGio + " (1 phut sau khi gui email SV)");
                }
            }
            // Neu ca 2 (email + SMS) da gui roi -> khong lam gi them, dung o cap do cao nhat
        }
        return kq;
    }

    public CauHinhNhacNoTuDong layCauHinhHienTai() throws SQLException {
        return cauHinhDAO.layCauHinhDangApDung();
    }

    public void datCauHinh(CauHinhNhacNoTuDong cauHinh) throws SQLException {
        cauHinh.setDangApDung(true);
        cauHinhDAO.luuCauHinhMoi(cauHinh);
        nhatKyHeThongService.ghi(null, "CAU_HINH_NHAC_NO", "KhungThoiGianThu",
                "Dat khung thu hoc phi tu " + cauHinh.getNgayGioBatDau() + " den " + cauHinh.getNgayGioKetThuc());
    }

    public void tatCauHinh() throws SQLException {
        cauHinhDAO.tatCauHinhDangApDung();
        nhatKyHeThongService.ghi(null, "CAU_HINH_NHAC_NO", "KhungThoiGianThu", "Tat khung thu hoc phi tu dong");
    }
}