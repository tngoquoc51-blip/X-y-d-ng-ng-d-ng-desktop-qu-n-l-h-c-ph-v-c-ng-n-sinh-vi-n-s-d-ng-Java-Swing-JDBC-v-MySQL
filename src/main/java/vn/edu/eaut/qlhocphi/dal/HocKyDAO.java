package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.model.HocKy;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HocKyDAO {

    public List<HocKy> layTatCa() throws SQLException {
        String sql = "SELECT * FROM HocKy ORDER BY MaHocKy DESC";
        List<HocKy> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public HocKy timTheoMa(int maHocKy) throws SQLException {
        String sql = "SELECT * FROM HocKy WHERE MaHocKy = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHocKy);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public boolean them(HocKy hk) throws SQLException {
        String sql = "INSERT INTO HocKy (TenHocKy,NamHoc,DonGiaTinChi,NgayBatDau,NgayKetThuc) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hk.getTenHocKy());
            ps.setString(2, hk.getNamHoc());
            ps.setBigDecimal(3, hk.getDonGiaTinChi());
            ps.setDate(4, hk.getNgayBatDau() != null ? Date.valueOf(hk.getNgayBatDau()) : null);
            ps.setDate(5, hk.getNgayKetThuc() != null ? Date.valueOf(hk.getNgayKetThuc()) : null);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean capNhat(HocKy hk) throws SQLException {
        String sql = "UPDATE HocKy SET TenHocKy=?, NamHoc=?, DonGiaTinChi=?, NgayBatDau=?, NgayKetThuc=? WHERE MaHocKy=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hk.getTenHocKy());
            ps.setString(2, hk.getNamHoc());
            ps.setBigDecimal(3, hk.getDonGiaTinChi());
            ps.setDate(4, hk.getNgayBatDau() != null ? Date.valueOf(hk.getNgayBatDau()) : null);
            ps.setDate(5, hk.getNgayKetThuc() != null ? Date.valueOf(hk.getNgayKetThuc()) : null);
            ps.setInt(6, hk.getMaHocKy());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean xoa(int maHocKy) throws SQLException {
        String sql = "DELETE FROM HocKy WHERE MaHocKy = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHocKy);
            return ps.executeUpdate() > 0;
        }
    }

    private HocKy map(ResultSet rs) throws SQLException {
        HocKy hk = new HocKy();
        hk.setMaHocKy(rs.getInt("MaHocKy"));
        hk.setTenHocKy(rs.getString("TenHocKy"));
        hk.setNamHoc(rs.getString("NamHoc"));
        hk.setDonGiaTinChi(rs.getBigDecimal("DonGiaTinChi"));
        Date bd = rs.getDate("NgayBatDau");
        Date kt = rs.getDate("NgayKetThuc");
        hk.setNgayBatDau(bd != null ? bd.toLocalDate() : null);
        hk.setNgayKetThuc(kt != null ? kt.toLocalDate() : null);
        return hk;
    }
}