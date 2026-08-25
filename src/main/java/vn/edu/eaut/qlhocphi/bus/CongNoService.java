package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.HoaDonDAO;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;
import vn.edu.eaut.qlhocphi.model.TrangThaiHoaDon;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/** Nghiep vu tra cuu va thong ke cong no. */
public class CongNoService {
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    public List<HoaDonHocPhi> layDanhSachConNo() throws SQLException {
        return hoaDonDAO.layTatCa().stream()
                .filter(hd -> hd.tinhConNo().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
    }

    /** Danh sach hoa don cua rieng 1 sinh vien - dung cho cong Sinh vien tu tra cuu. */
    public List<HoaDonHocPhi> layTheoSinhVien(String maSV) throws SQLException {
        return hoaDonDAO.layTheoSinhVien(maSV);
    }

    public List<HoaDonHocPhi> layDanhSachQuaHan() throws SQLException {
        return hoaDonDAO.layTatCa().stream()
                .filter(hd -> hd.tinhTrangThai() == TrangThaiHoaDon.QUA_HAN)
                .collect(Collectors.toList());
    }

    public BigDecimal tongConNoToanTruong() throws SQLException {
        return hoaDonDAO.layTatCa().stream()
                .map(HoaDonHocPhi::tinhConNo)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal tongDaThuTatCa() throws SQLException {
        return hoaDonDAO.layTatCa().stream()
                .map(HoaDonHocPhi::getDaNop)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}