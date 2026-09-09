package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.dal.CaLamViecDAO;
import vn.edu.eaut.qlhocphi.model.CaLamViec;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

/** Nghiep vu ca lam viec: mo ca, tinh tong tien da thu trong ca, dong ca (chot so). */
public class CaLamViecService {
    private final CaLamViecDAO caLamViecDAO = new CaLamViecDAO();

    public CaLamViec layCaDangMo(TaiKhoan tk) throws SQLException {
        return caLamViecDAO.layCaDangMo(tk.getMaTK());
    }

    public CaLamViec moCaMoi(TaiKhoan tk) throws SQLException {
        CaLamViec dangMo = layCaDangMo(tk);
        if (dangMo != null) return dangMo;
        int maCa = caLamViecDAO.moCa(tk.getMaTK(), tk.getHoTen());
        return layCaTheoId(maCa);
    }

    private CaLamViec layCaTheoId(int maCa) throws SQLException {
        // Don gian: lay lai ca dang mo gan nhat cua chinh nguoi vua mo (du dung cho luong nghiep vu 1 nguoi/1 ca)
        return caLamViecDAO.layLichSu(0, 0).stream().filter(c -> c.getMaCa() == maCa).findFirst().orElse(null);
    }

    /** Tinh tong tien da thu (theo tung hinh thuc) tu luc mo ca den hien tai, dua vao bang PhieuThu.NguoiThu + NgayNop. */
    public CaLamViec tinhTongHienTai(CaLamViec ca) throws SQLException {
        String sql = "SELECT HinhThuc, COUNT(*) SoLuong, SUM(SoTienNop) Tong FROM PhieuThu " +
                "WHERE NguoiThu = ? AND NgayNop >= ? GROUP BY HinhThuc";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ca.getTenNhanVien());
            ps.setTimestamp(2, Timestamp.valueOf(ca.getThoiGianMoCa()));
            try (ResultSet rs = ps.executeQuery()) {
                BigDecimal tienMat = BigDecimal.ZERO, tienCK = BigDecimal.ZERO, tienOnline = BigDecimal.ZERO;
                int tongSoGiaoDich = 0;
                while (rs.next()) {
                    String hinhThuc = rs.getString("HinhThuc");
                    BigDecimal tong = rs.getBigDecimal("Tong");
                    if (tong == null) tong = BigDecimal.ZERO;
                    tongSoGiaoDich += rs.getInt("SoLuong");
                    switch (hinhThuc) {
                        case "TIEN_MAT" -> tienMat = tong;
                        case "CHUYEN_KHOAN" -> tienCK = tong;
                        case "THANH_TOAN_ONLINE" -> tienOnline = tong;
                    }
                }
                ca.setTongTienMat(tienMat);
                ca.setTongTienChuyenKhoan(tienCK);
                ca.setTongTienOnline(tienOnline);
                ca.setSoGiaoDich(tongSoGiaoDich);
            }
        }
        return ca;
    }

    public void dongCa(CaLamViec ca) throws SQLException {
        tinhTongHienTai(ca);
        caLamViecDAO.dongCa(ca.getMaCa(), ca.getTongTienMat(), ca.getTongTienChuyenKhoan(),
                ca.getTongTienOnline(), ca.getSoGiaoDich());
        ca.setTrangThai("DA_DONG");
        ca.setThoiGianDongCa(LocalDateTime.now());
    }

    public List<CaLamViec> layLichSu(TaiKhoan tk, int gioiHan) throws SQLException {
        return caLamViecDAO.layLichSu(tk.getMaTK(), gioiHan);
    }
}