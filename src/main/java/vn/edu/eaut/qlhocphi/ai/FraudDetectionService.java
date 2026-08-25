package vn.edu.eaut.qlhocphi.ai;

import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.PhieuThu;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Dich vu AI phat hien giao dich thanh toan bat thuong (fraud / anomaly detection).
 *
 * Hoat dong theo huong RULE-BASED (he thong chuyen gia): ket hop nhieu dau hieu
 * rui ro thanh 1 diem so va muc do canh bao. Cach tiep can nay khong can du lieu
 * huan luyen, de giai thich (giai trinh duoc voi kiem toan) va van duoc xem la
 * mot dang AI ung dung trong phat hien gian lan (rule-based expert system).
 *
 * Diem tich hop de xuat: goi FraudDetectionService.kiemTra(...) trong
 * ThanhToanService TRUOC khi ghi PhieuThu (nhat la voi thanh toan online),
 * neu MucDoRuiRo = CAO thi canh bao / yeu cau xac nhan them tu ke toan.
 */
public class FraudDetectionService {

    public enum MucDoRuiRo { THAP, TRUNG_BINH, CAO }

    /** Ket qua phan tich 1 giao dich thanh toan. */
    public static class KetQuaKiemTra {
        private final MucDoRuiRo mucDoRuiRo;
        private final List<String> lyDo;

        public KetQuaKiemTra(MucDoRuiRo mucDoRuiRo, List<String> lyDo) {
            this.mucDoRuiRo = mucDoRuiRo;
            this.lyDo = lyDo;
        }

        public MucDoRuiRo getMucDoRuiRo() { return mucDoRuiRo; }
        public List<String> getLyDo() { return lyDo; }
        public boolean canCanhBao() { return mucDoRuiRo != MucDoRuiRo.THAP; }
    }

    // ===== Nguong (threshold) cau hinh san - co the chinh theo thuc te van hanh =====
    private static final BigDecimal NGUONG_SO_TIEN_LON = BigDecimal.valueOf(50_000_000); // 50 trieu
    private static final int NGUONG_SO_GIAO_DICH_LIEN_TIEP = 3;      // >=3 giao dich gan nhau
    private static final int NGUONG_PHUT_GIAO_DICH_DON_DAP = 5;      // trong vong 5 phut
    private static final int GIO_BAT_DAU_LAM_VIEC = 6;
    private static final int GIO_KET_THUC_LAM_VIEC = 22;

    /**
     * Phan tich 1 giao dich nop tien dua tren: so tien nop, hoa don lien quan,
     * va lich su cac lan nop gan day cua CUNG hoa don do (de phat hien nop don dap/trung lap).
     */
    public KetQuaKiemTra kiemTra(HoaDonHocPhi hoaDon, BigDecimal soTienNop, List<PhieuThu> lichSuGanDay) {
        List<String> lyDo = new ArrayList<>();
        int diem = 0;

        // 1) So tien nop bat thuong lon
        if (soTienNop != null && soTienNop.compareTo(NGUONG_SO_TIEN_LON) > 0) {
            diem += 2;
            lyDo.add("So tien nop rat lon (" + soTienNop.toPlainString() + " d), vuot nguong canh bao "
                    + NGUONG_SO_TIEN_LON.toPlainString() + " d");
        }

        // 2) So tien nop vuot qua nhieu so voi con no hien tai cua hoa don (nop du/thua bat thuong)
        if (hoaDon != null && soTienNop != null) {
            BigDecimal conNo = hoaDon.tinhConNo();
            if (conNo.compareTo(BigDecimal.ZERO) > 0 && soTienNop.compareTo(conNo.multiply(BigDecimal.valueOf(1.5))) > 0) {
                diem += 1;
                lyDo.add("So tien nop vuot han muc con no cua hoa don (con no: " + conNo.toPlainString() + " d)");
            }
        }

        // 3) Nhieu giao dich lien tiep trong thoi gian ngan tren cung 1 hoa don
        if (lichSuGanDay != null && lichSuGanDay.size() >= NGUONG_SO_GIAO_DICH_LIEN_TIEP) {
            long soGiaoDichDonDap = demGiaoDichTrongKhoangPhut(lichSuGanDay, NGUONG_PHUT_GIAO_DICH_DON_DAP);
            if (soGiaoDichDonDap >= NGUONG_SO_GIAO_DICH_LIEN_TIEP) {
                diem += 2;
                lyDo.add("Phat hien " + soGiaoDichDonDap + " giao dich lien tiep trong vong "
                        + NGUONG_PHUT_GIAO_DICH_DON_DAP + " phut tren cung hoa don - co the la loi he thong hoac gian lan");
            }
        }

        // 4) Giao dich thuc hien ngoai gio hanh chinh (dem khuya)
        int gioHienTai = LocalDateTime.now().getHour();
        if (gioHienTai < GIO_BAT_DAU_LAM_VIEC || gioHienTai >= GIO_KET_THUC_LAM_VIEC) {
            diem += 1;
            lyDo.add("Giao dich thuc hien ngoai khung gio thong thuong (" + gioHienTai + "h)");
        }

        MucDoRuiRo mucDo;
        if (diem >= 4) {
            mucDo = MucDoRuiRo.CAO;
        } else if (diem >= 2) {
            mucDo = MucDoRuiRo.TRUNG_BINH;
        } else {
            mucDo = MucDoRuiRo.THAP;
        }

        if (lyDo.isEmpty()) {
            lyDo.add("Khong phat hien dau hieu bat thuong");
        }

        return new KetQuaKiemTra(mucDo, lyDo);
    }

    private long demGiaoDichTrongKhoangPhut(List<PhieuThu> lichSu, int soPhut) {
        long dem = 0;
        LocalDateTime moc = LocalDateTime.now();
        for (PhieuThu pt : lichSu) {
            if (pt.getNgayNop() == null) continue;
            long phutCachHienTai = Math.abs(Duration.between(pt.getNgayNop(), moc).toMinutes());
            if (phutCachHienTai <= soPhut) {
                dem++;
            }
        }
        return dem;
    }
}
