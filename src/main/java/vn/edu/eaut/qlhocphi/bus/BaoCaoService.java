
package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.HoaDonDAO;
import vn.edu.eaut.qlhocphi.dal.PhieuThuDAO;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.PhieuThu;
import vn.edu.eaut.qlhocphi.model.TrangThaiHoaDon;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Báo cáo / thống kê – hỗ trợ vận hành 5 năm (biểu đồ tháng theo năm + biểu đồ năm).
 */
public class BaoCaoService {
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();

    /** Số năm hệ thống cần hỗ trợ báo cáo (yêu cầu đồ án). */
    public static final int SO_NAM_VAN_HANH = 5;

    public Map<String, BigDecimal> thongKeThuTheoHocKy() throws SQLException {
        List<HoaDonHocPhi> dsHoaDon = hoaDonDAO.layTatCa();
        return dsHoaDon.stream()
                .collect(Collectors.groupingBy(
                        HoaDonHocPhi::getTenHocKy,
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, HoaDonHocPhi::getDaNop, BigDecimal::add)
                ));
    }

    public Map<TrangThaiHoaDon, Long> thongKeSoLuongTheoTrangThai() throws SQLException {
        List<HoaDonHocPhi> dsHoaDon = hoaDonDAO.layTatCa();
        return dsHoaDon.stream()
                .collect(Collectors.groupingBy(HoaDonHocPhi::tinhTrangThai, Collectors.counting()));
    }

    public Map<String, BigDecimal> thongKeThuTheoThang() throws SQLException {
        return thongKeThuTheoThang(null);
    }

    /**
     * Thu theo tháng.
     * @param nam null = 12 tháng × 5 năm gần nhất (key: yyyy-MM);
     *            có giá trị = đủ T1…T12 của năm đó (key: T1…T12).
     */
    public Map<String, BigDecimal> thongKeThuTheoThang(Integer nam) throws SQLException {
        List<PhieuThu> all = layTatCaPhieuThu();
        Map<String, BigDecimal> ketQua = new LinkedHashMap<>();

        if (nam != null) {
            for (int m = 1; m <= 12; m++) ketQua.put("T" + m, BigDecimal.ZERO);
            for (PhieuThu pt : all) {
                LocalDateTime ngay = pt.getNgayNop();
                if (ngay == null || ngay.getYear() != nam) continue;
                String key = "T" + ngay.getMonthValue();
                ketQua.merge(key, soTien(pt), BigDecimal::add);
            }
            return ketQua;
        }

        // 5 năm × 12 tháng (đủ khung thời gian vận hành)
        int namHienTai = LocalDate.now().getYear();
        int namBatDau = namHienTai - SO_NAM_VAN_HANH + 1;
        for (int y = namBatDau; y <= namHienTai; y++) {
            for (int m = 1; m <= 12; m++) {
                ketQua.put(String.format("%04d-%02d", y, m), BigDecimal.ZERO);
            }
        }
        for (PhieuThu pt : all) {
            LocalDateTime ngay = pt.getNgayNop();
            if (ngay == null) continue;
            if (ngay.getYear() < namBatDau || ngay.getYear() > namHienTai) continue;
            String key = YearMonth.from(ngay).toString();
            ketQua.merge(key, soTien(pt), BigDecimal::add);
        }
        return ketQua;
    }

    /**
     * Tổng thu theo từng năm trong khung 5 năm vận hành (đủ năm kể cả 0đ).
     * Key: "2022", "2023"… năm hiện tại.
     */
    public Map<String, BigDecimal> thongKeThuTheoNam() throws SQLException {
        List<PhieuThu> all = layTatCaPhieuThu();
        Map<String, BigDecimal> ketQua = new LinkedHashMap<>();
        int namHienTai = LocalDate.now().getYear();
        int namBatDau = namHienTai - SO_NAM_VAN_HANH + 1;
        for (int y = namBatDau; y <= namHienTai; y++) {
            ketQua.put(String.valueOf(y), BigDecimal.ZERO);
        }
        for (PhieuThu pt : all) {
            LocalDateTime ngay = pt.getNgayNop();
            if (ngay == null) continue;
            int y = ngay.getYear();
            if (y < namBatDau || y > namHienTai) continue;
            ketQua.merge(String.valueOf(y), soTien(pt), BigDecimal::add);
        }
        return ketQua;
    }

    /**
     * Danh sách 5 năm vận hành (mới → cũ) để đổ combo lọc.
     * Luôn đủ 5 năm kể cả năm chưa có phiếu thu.
     */
    public List<Integer> layDanhSachNamVanHanh() {
        List<Integer> ds = new ArrayList<>();
        int namHienTai = LocalDate.now().getYear();
        for (int i = 0; i < SO_NAM_VAN_HANH; i++) {
            ds.add(namHienTai - i);
        }
        return ds;
    }

    /** Tương thích code cũ. */
    public List<Integer> layDanhSachNamCoDuLieu() throws SQLException {
        return layDanhSachNamVanHanh();
    }

    public Map<String, BigDecimal> thongKeThuTheoHinhThuc() throws SQLException {
        Map<String, BigDecimal> ketQua = new LinkedHashMap<>();
        for (PhieuThu pt : layTatCaPhieuThu()) {
            String ht = pt.getHinhThuc() != null ? pt.getHinhThuc() : "KHAC";
            ketQua.merge(ht, soTien(pt), BigDecimal::add);
        }
        return ketQua;
    }

    public List<HoaDonHocPhi> topSinhVienConNoNhieuNhat(int soLuong) throws SQLException {
        return hoaDonDAO.layTatCa().stream()
                .filter(hd -> hd.tinhConNo().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(HoaDonHocPhi::tinhConNo).reversed())
                .limit(soLuong)
                .collect(Collectors.toList());
    }

    public BaoCaoTongQuan layTongQuan() throws SQLException {
        List<HoaDonHocPhi> dsHoaDon = hoaDonDAO.layTatCa();
        BigDecimal tongHocPhi = dsHoaDon.stream().map(HoaDonHocPhi::getSoTien).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tongDaThu = dsHoaDon.stream().map(HoaDonHocPhi::getDaNop).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tongConNo = dsHoaDon.stream().map(HoaDonHocPhi::tinhConNo).reduce(BigDecimal.ZERO, BigDecimal::add);
        long soHoaDonQuaHan = dsHoaDon.stream().filter(hd -> hd.tinhTrangThai() == TrangThaiHoaDon.QUA_HAN).count();
        return new BaoCaoTongQuan(dsHoaDon.size(), tongHocPhi, tongDaThu, tongConNo, soHoaDonQuaHan);
    }

    private List<PhieuThu> layTatCaPhieuThu() throws SQLException {
        List<PhieuThu> all = new ArrayList<>();
        for (HoaDonHocPhi hd : hoaDonDAO.layTatCa()) {
            all.addAll(phieuThuDAO.layTheoHoaDon(hd.getMaHoaDon()));
        }
        return all;
    }

    private static BigDecimal soTien(PhieuThu pt) {
        return pt.getSoTienNop() != null ? pt.getSoTienNop() : BigDecimal.ZERO;
    }

    public static class BaoCaoTongQuan {
        private final int tongSoHoaDon;
        private final BigDecimal tongHocPhi;
        private final BigDecimal tongDaThu;
        private final BigDecimal tongConNo;
        private final long soHoaDonQuaHan;

        public BaoCaoTongQuan(int tongSoHoaDon, BigDecimal tongHocPhi, BigDecimal tongDaThu,
                              BigDecimal tongConNo, long soHoaDonQuaHan) {
            this.tongSoHoaDon = tongSoHoaDon;
            this.tongHocPhi = tongHocPhi;
            this.tongDaThu = tongDaThu;
            this.tongConNo = tongConNo;
            this.soHoaDonQuaHan = soHoaDonQuaHan;
        }

        public int getTongSoHoaDon() { return tongSoHoaDon; }
        public BigDecimal getTongHocPhi() { return tongHocPhi; }
        public BigDecimal getTongDaThu() { return tongDaThu; }
        public BigDecimal getTongConNo() { return tongConNo; }
        public long getSoHoaDonQuaHan() { return soHoaDonQuaHan; }
    }
}