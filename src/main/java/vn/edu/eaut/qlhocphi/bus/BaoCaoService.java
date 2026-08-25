package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.HoaDonDAO;
import vn.edu.eaut.qlhocphi.dal.PhieuThuDAO;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.PhieuThu;
import vn.edu.eaut.qlhocphi.model.TrangThaiHoaDon;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Nghiep vu tong hop bao cao / thong ke, phuc vu cho DashboardPanel va ExportReportDialog.
 * Chi doc du lieu (khong ghi CSDL) nen goi truc tiep DAO, khong can validate dau vao phuc tap.
 */
public class BaoCaoService {
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();

    /** Tong so tien da thu theo tung hoc ky (ten hoc ky -> tong tien). */
    public Map<String, BigDecimal> thongKeThuTheoHocKy() throws SQLException {
        List<HoaDonHocPhi> dsHoaDon = hoaDonDAO.layTatCa();
        return dsHoaDon.stream()
                .collect(Collectors.groupingBy(
                        HoaDonHocPhi::getTenHocKy,
                        LinkedHashMap::new,
                        Collectors.reducing(BigDecimal.ZERO, HoaDonHocPhi::getDaNop, BigDecimal::add)
                ));
    }

    /** So luong hoa don theo tung trang thai (CHUA_DONG, DONG_MOT_PHAN, DA_DONG_DU, QUA_HAN). */
    public Map<TrangThaiHoaDon, Long> thongKeSoLuongTheoTrangThai() throws SQLException {
        List<HoaDonHocPhi> dsHoaDon = hoaDonDAO.layTatCa();
        return dsHoaDon.stream()
                .collect(Collectors.groupingBy(HoaDonHocPhi::tinhTrangThai, Collectors.counting()));
    }

    /** Tong tien thu duoc theo tung thang (dua tren ngay nop cua PhieuThu), phuc vu bieu do xu huong. */
    public Map<String, BigDecimal> thongKeThuTheoThang() throws SQLException {
        List<HoaDonHocPhi> dsHoaDon = hoaDonDAO.layTatCa();
        Map<String, BigDecimal> ketQua = new LinkedHashMap<>();

        for (HoaDonHocPhi hd : dsHoaDon) {
            List<PhieuThu> dsPhieuThu = phieuThuDAO.layTheoHoaDon(hd.getMaHoaDon());
            for (PhieuThu pt : dsPhieuThu) {
                LocalDateTime ngay = pt.getNgayNop();
                if (ngay == null) continue;
                String thang = YearMonth.from(ngay).toString(); // vd: 2026-08
                ketQua.merge(thang, pt.getSoTienNop(), BigDecimal::add);
            }
        }
        return ketQua;
    }

    /** Top N sinh vien con no nhieu nhat, sap xep giam dan theo so tien con no. */
    public List<HoaDonHocPhi> topSinhVienConNoNhieuNhat(int soLuong) throws SQLException {
        return hoaDonDAO.layTatCa().stream()
                .filter(hd -> hd.tinhConNo().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(HoaDonHocPhi::tinhConNo).reversed())
                .limit(soLuong)
                .collect(Collectors.toList());
    }

    /** Tong quan nhanh cho Dashboard: tong hoa don, tong da thu, tong con no, so hoa don qua han. */
    public BaoCaoTongQuan layTongQuan() throws SQLException {
        List<HoaDonHocPhi> dsHoaDon = hoaDonDAO.layTatCa();

        BigDecimal tongHocPhi = dsHoaDon.stream()
                .map(HoaDonHocPhi::getSoTien)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tongDaThu = dsHoaDon.stream()
                .map(HoaDonHocPhi::getDaNop)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tongConNo = dsHoaDon.stream()
                .map(HoaDonHocPhi::tinhConNo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long soHoaDonQuaHan = dsHoaDon.stream()
                .filter(hd -> hd.tinhTrangThai() == TrangThaiHoaDon.QUA_HAN)
                .count();

        return new BaoCaoTongQuan(dsHoaDon.size(), tongHocPhi, tongDaThu, tongConNo, soHoaDonQuaHan);
    }

    /** DTO gon nhe chua so lieu tong quan, dung de bind len DashboardPanel. */
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