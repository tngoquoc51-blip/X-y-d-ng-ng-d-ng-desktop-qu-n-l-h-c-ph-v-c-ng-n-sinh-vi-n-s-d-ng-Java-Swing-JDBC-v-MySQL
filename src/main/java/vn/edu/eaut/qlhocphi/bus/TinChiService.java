
package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.TinChiDAO;
import vn.edu.eaut.qlhocphi.model.TinChiSinhVien;
import vn.edu.eaut.qlhocphi.model.TinChiTheoNam;

import java.sql.SQLException;
import java.util.List;

public class TinChiService {
    private final TinChiDAO dao = new TinChiDAO();

    public TinChiSinhVien layTienDo(String maSV) throws SQLException {
        if (maSV == null || maSV.isBlank()) return null;
        return dao.layTheoMaSV(maSV.trim());
    }

    public List<TinChiTheoNam> layLichSuNam(String maSV) throws SQLException {
        if (maSV == null || maSV.isBlank()) return List.of();
        return dao.layLichSuTheoNam(maSV.trim());
    }
}