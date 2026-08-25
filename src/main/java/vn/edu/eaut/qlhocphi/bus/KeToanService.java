package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.PhieuThuDAO;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.PhieuThu;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tong hop du lieu rieng cho "Bang dieu khien Ke toan" - man hinh dau tien khi tai
 * khoan vai tro KETOAN dang nhap, tap trung vao cong viec hang ngay cua ke toan
 * (thu tien, theo doi cong no, giao dich gan day) thay vi thong ke tong quan chung
 * chung nhu trang Tong Quan cua Admin.
 */
public class KeToanService {
    private final CongNoService congNoService = new CongNoService();
    private final PhieuThuDAO phieuThuDAO = new PhieuThuDAO();

    public static class SoLieuHomNay {
        public final BigDecimal tongThuHomNay;
        public final int soGiaoDichHomNay;
        public final BigDecimal tongConNoToanTruong;
        public final int soHoaDonQuaHan;

        public SoLieuHomNay(BigDecimal tongThuHomNay, int soGiaoDichHomNay,
                            BigDecimal tongConNoToanTruong, int soHoaDonQuaHan) {
            this.tongThuHomNay = tongThuHomNay;
            this.soGiaoDichHomNay = soGiaoDichHomNay;
            this.tongConNoToanTruong = tongConNoToanTruong;
            this.soHoaDonQuaHan = soHoaDonQuaHan;
        }
    }

    /** 4 so lieu chinh hien tren dai the thong ke dau trang. */
    public SoLieuHomNay laySoLieuHomNay() throws SQLException {
        LocalDate homNay = LocalDate.now();
        BigDecimal thu = phieuThuDAO.tongThuTheoNgay(homNay);
        int soGD = phieuThuDAO.demGiaoDichTheoNgay(homNay);
        BigDecimal conNo = congNoService.tongConNoToanTruong();
        int soQuaHan = congNoService.layDanhSachQuaHan().size();
        return new SoLieuHomNay(thu, soGD, conNo, soQuaHan);
    }

    /** N giao dich gan day nhat toan truong, dung cho bang "Giao dich gan day". */
    public List<PhieuThu> layGiaoDichGanDay(int gioiHan) throws SQLException {
        return phieuThuDAO.layGanDayNhat(gioiHan);
    }

    /**
     * Top N sinh vien dang no qua han NHIEU TIEN NHAT - danh sach "can xu ly truoc"
     * de ke toan uu tien nhac no/lien he, sap xep giam dan theo so tien con no.
     */
    public List<HoaDonHocPhi> topSinhVienQuaHan(int gioiHan) throws SQLException {
        return congNoService.layDanhSachQuaHan().stream()
                .sorted(Comparator.comparing(HoaDonHocPhi::tinhConNo).reversed())
                .limit(gioiHan)
                .collect(Collectors.toList());
    }
}