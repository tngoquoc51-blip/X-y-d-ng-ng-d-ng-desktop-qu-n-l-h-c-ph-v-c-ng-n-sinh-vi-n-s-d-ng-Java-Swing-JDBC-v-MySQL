package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.PhieuThuDAO;
import vn.edu.eaut.qlhocphi.model.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * "Dự báo công nợ bằng AI": Phân tích TOÀN BỘ lịch sử thanh toán của từng sinh
 * viên (không chỉ nhìn trạng thái hiện tại) để chấm điểm rủi ro trễ hạn cho kỳ
 * tới. Với mỗi hóa đơn có hạn thanh toán, so sánh ngày nộp tiền THỰC TẾ (từ
 * PhieuThu) với hạn - nếu nộp sau hạn (dù hiện tại đã đóng đủ rồi) vẫn tính là
 * "1 lần trễ" trong lịch sử. Đây là điểm khác biệt với cách làm thông thường
 * (chỉ đếm hóa đơn đang QUÁ_HẠN) - phản ánh đúng "thói quen" của sinh viên.
 */
public class DuBaoCongNoService {
    private final HocPhiService hocPhiService = new HocPhiService();
    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();

    private static final double NGUONG_RUI_RO_CAO = 50.0;
    private static final double NGUONG_RUI_RO_TRUNG_BINH = 20.0;

    public List<RuiRoSinhVien> phanTichToanBo() throws Exception {
        List<HoaDonHocPhi> tatCaHoaDon = hocPhiService.layTatCaHoaDon();

        Map<String, List<HoaDonHocPhi>> theoSinhVien = tatCaHoaDon.stream()
                .collect(Collectors.groupingBy(HoaDonHocPhi::getMaSV));

        List<RuiRoSinhVien> ketQua = new ArrayList<>();
        for (Map.Entry<String, List<HoaDonHocPhi>> entry : theoSinhVien.entrySet()) {
            RuiRoSinhVien rr = phanTich1SinhVien(entry.getKey(), entry.getValue());
            if (rr != null) ketQua.add(rr);
        }

        // Sắp xếp: Rủi ro cao nhất lên đầu, trong nhóm rủi ro bằng nhau thì % trễ cao hơn lên trước
        ketQua.sort((a, b) -> {
            int soA = capDoSo(a.getMucDoRuiRo()), soB = capDoSo(b.getMucDoRuiRo());
            if (soA != soB) return soB - soA;
            return Double.compare(b.getTyLeTre(), a.getTyLeTre());
        });
        return ketQua;
    }

    private int capDoSo(String mucDo) {
        return switch (mucDo) {
            case "CAO" -> 3;
            case "TRUNG_BINH" -> 2;
            case "THAP" -> 1;
            default -> 0; // CHUA_DU_DU_LIEU
        };
    }

    private RuiRoSinhVien phanTich1SinhVien(String maSV, List<HoaDonHocPhi> danhSachHD) throws Exception {
        List<HoaDonHocPhi> coHanThanhToan = danhSachHD.stream()
                .filter(hd -> hd.getHanThanhToan() != null)
                .toList();
        if (coHanThanhToan.isEmpty()) return null;

        int soLanTre = 0;
        int soNgayTreDangNo = 0;

        for (HoaDonHocPhi hd : coHanThanhToan) {
            LocalDate han = hd.getHanThanhToan();
            boolean coTre = false;

            if (hd.tinhTrangThai() == TrangThaiHoaDon.QUA_HAN) {
                // Đang nợ và đã quá hạn -> Chắc chắn tính là 1 lần trễ, cộng dồn số ngày trễ hiện tại
                coTre = true;
                int soNgay = (int) ChronoUnit.DAYS.between(han, LocalDate.now());
                soNgayTreDangNo = Math.max(soNgayTreDangNo, soNgay);
            } else if (hd.tinhTrangThai() == TrangThaiHoaDon.DA_DONG_DU) {
                // Đã đóng xong - Kiểm tra LỊCH SỬ: Lần nộp cuối cùng có sau hạn không
                List<PhieuThu> danhSachThu = phieuThuDAO.layTheoHoaDon(hd.getMaHoaDon());
                LocalDateTime lanNopCuoi = danhSachThu.stream()
                        .map(PhieuThu::getNgayNop)
                        .filter(Objects::nonNull)
                        .max(LocalDateTime::compareTo)
                        .orElse(null);
                if (lanNopCuoi != null && lanNopCuoi.toLocalDate().isAfter(han)) {
                    coTre = true; // Đã đóng đủ nhưng đóng TRỄ - Vẫn là 1 điểm xấu trong "thói quen"
                }
            }
            if (coTre) soLanTre++;
        }

        int tongHoaDon = coHanThanhToan.size();
        double tyLeTre = tongHoaDon > 0 ? (soLanTre * 100.0 / tongHoaDon) : 0;

        String mucDo;
        if (tongHoaDon < 2) {
            mucDo = "CHUA_DU_DU_LIEU"; // Mới có 1 hóa đơn - Chưa đủ cơ sở kết luận "thói quen"
        } else if (tyLeTre >= NGUONG_RUI_RO_CAO) {
            mucDo = "CAO";
        } else if (tyLeTre >= NGUONG_RUI_RO_TRUNG_BINH) {
            mucDo = "TRUNG_BINH";
        } else {
            mucDo = "THAP";
        }

        RuiRoSinhVien rr = new RuiRoSinhVien();
        rr.setMaSV(maSV);
        rr.setTenSV(danhSachHD.get(0).getTenSV());
        rr.setTongHoaDon(tongHoaDon);
        rr.setSoLanTre(soLanTre);
        rr.setTyLeTre(Math.round(tyLeTre * 10) / 10.0);
        rr.setSoNgayTreDangNo(soNgayTreDangNo);
        rr.setMucDoRuiRo(mucDo);
        return rr;
    }
}