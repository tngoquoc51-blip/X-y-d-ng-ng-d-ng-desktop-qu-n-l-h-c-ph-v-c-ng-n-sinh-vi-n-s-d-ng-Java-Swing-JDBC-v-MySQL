package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.model.LichSuNapVi;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LichSuNapViDAO {

    public void them(LichSuNapVi ls) throws SQLException {
        String sql = "INSERT INTO LichSuNapVi (MaSV, SoTien, HinhThuc, MaGiaoDichCong) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ls.getMaSV());
            ps.setBigDecimal(2, ls.getSoTien());
            ps.setString(3, ls.getHinhThuc());
            ps.setString(4, ls.getMaGiaoDichCong());
            ps.executeUpdate();
        }
    }

    public List<LichSuNapVi> layTheoSinhVien(String maSV) throws SQLException {
        String sql = "SELECT * FROM LichSuNapVi WHERE MaSV = ? ORDER BY NgayNap DESC";
        List<LichSuNapVi> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private LichSuNapVi map(ResultSet rs) throws SQLException {
        LichSuNapVi ls = new LichSuNapVi();
        ls.setMaGiaoDich(rs.getInt("MaGiaoDich"));
        ls.setMaSV(rs.getString("MaSV"));
        ls.setSoTien(rs.getBigDecimal("SoTien"));
        Timestamp ts = rs.getTimestamp("NgayNap");
        ls.setNgayNap(ts != null ? ts.toLocalDateTime() : null);
        ls.setHinhThuc(rs.getString("HinhThuc"));
        ls.setMaGiaoDichCong(rs.getString("MaGiaoDichCong"));
        return ls;
    }
}