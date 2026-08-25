package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.SinhVienDAO;
import vn.edu.eaut.qlhocphi.model.SinhVien;

import java.sql.SQLException;
import java.util.List;

/** Nghiep vu quan ly sinh vien - kiem tra hop le truoc khi ghi CSDL. */
public class SinhVienService {
    private final SinhVienDAO sinhVienDAO = new SinhVienDAO();

    public List<SinhVien> layTatCa() throws SQLException {
        return sinhVienDAO.layTatCa();
    }

    public List<SinhVien> timKiem(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) return layTatCa();
        return sinhVienDAO.timKiem(keyword.trim());
    }
    /** Tim dung 1 sinh vien theo ma - dung cho man hinh tra cuu cong khai (khong can dang nhap). */
    public SinhVien timTheoMa(String maSV) throws SQLException {
        if (maSV == null || maSV.isBlank()) return null;
        return sinhVienDAO.timTheoMa(maSV.trim());
    }
    public void them(SinhVien sv) throws SQLException {
        validate(sv);
        if (sinhVienDAO.timTheoMa(sv.getMaSV()) != null) {
            throw new IllegalArgumentException("Ma sinh vien da ton tai: " + sv.getMaSV());
        }
        sinhVienDAO.them(sv);
    }

    public void capNhat(SinhVien sv) throws SQLException {
        validate(sv);
        sinhVienDAO.capNhat(sv);
    }

    public void xoa(String maSV) throws SQLException {
        sinhVienDAO.xoa(maSV);
    }

    private void validate(SinhVien sv) {
        if (sv.getMaSV() == null || sv.getMaSV().isBlank()) {
            throw new IllegalArgumentException("Ma sinh vien khong duoc de trong");
        }
        if (sv.getHoTen() == null || sv.getHoTen().isBlank()) {
            throw new IllegalArgumentException("Ho ten khong duoc de trong");
        }
    }
}