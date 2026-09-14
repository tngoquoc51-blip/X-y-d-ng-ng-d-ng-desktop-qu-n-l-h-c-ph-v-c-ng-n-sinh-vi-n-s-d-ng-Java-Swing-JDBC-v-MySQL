package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.model.TinChiSinhVien;
import vn.edu.eaut.qlhocphi.model.TinChiTheoNam;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TinChiDAO {

    public TinChiSinhVien layTheoMaSV(String maSV) throws SQLException {
        String sql = "SELECT t.*, sv.HoTen, sv.NamNhapHoc, sv.NamThu, sv.TrangThaiHoc "
                + "FROM tinchi_sinhvien t "
                + "JOIN SinhVien sv ON sv.MaSV = t.MaSV WHERE t.MaSV = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        // Chưa có dòng tín chỉ → trả về 0 kèm thông tin SV
        String sql2 = "SELECT MaSV, HoTen, NamNhapHoc, NamThu, TrangThaiHoc FROM SinhVien WHERE MaSV = ?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql2)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TinChiSinhVien t = new TinChiSinhVien();
                    t.setMaSV(rs.getString("MaSV"));
                    t.setHoTen(rs.getString("HoTen"));
                    t.setNamNhapHoc((Integer) rs.getObject("NamNhapHoc"));
                    t.setNamThu((Integer) rs.getObject("NamThu"));
                    t.setTrangThaiHoc(rs.getString("TrangThaiHoc"));
                    return t;
                }
            }
        }
        return null;
    }

    public List<TinChiTheoNam> layLichSuTheoNam(String maSV) throws SQLException {
        String sql = "SELECT * FROM tinchi_theonam WHERE MaSV = ? ORDER BY NamHoc";
        List<TinChiTheoNam> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TinChiTheoNam n = new TinChiTheoNam();
                    n.setId(rs.getInt("Id"));
                    n.setMaSV(rs.getString("MaSV"));
                    n.setNamHoc(rs.getInt("NamHoc"));
                    n.setTinChiDat(rs.getInt("TinChiDat"));
                    n.setTinChiRut(rs.getInt("TinChiRut"));
                    n.setGhiChu(rs.getString("GhiChu"));
                    list.add(n);
                }
            }
        }
        return list;
    }

    private TinChiSinhVien map(ResultSet rs) throws SQLException {
        TinChiSinhVien t = new TinChiSinhVien();
        t.setMaSV(rs.getString("MaSV"));
        t.setTinChiTichLuy(rs.getInt("TinChiTichLuy"));
        t.setTinChiBiRut(rs.getInt("TinChiBiRut"));
        t.setTinChiDangKy(rs.getInt("TinChiDangKy"));
        Timestamp ts = rs.getTimestamp("CapNhatLuc");
        if (ts != null) t.setCapNhatLuc(ts.toLocalDateTime());
        try {
            t.setHoTen(rs.getString("HoTen"));
            t.setNamNhapHoc((Integer) rs.getObject("NamNhapHoc"));
            t.setNamThu((Integer) rs.getObject("NamThu"));
            t.setTrangThaiHoc(rs.getString("TrangThaiHoc"));
        } catch (SQLException ignored) {}
        return t;
    }
}