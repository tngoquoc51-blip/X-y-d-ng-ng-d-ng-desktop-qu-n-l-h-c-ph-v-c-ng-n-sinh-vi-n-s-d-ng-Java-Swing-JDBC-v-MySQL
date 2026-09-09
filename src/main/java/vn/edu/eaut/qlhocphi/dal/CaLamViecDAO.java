package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.model.CaLamViec;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CaLamViecDAO {

    public int moCa(int maTK, String tenNhanVien) throws SQLException {
        String sql = "INSERT INTO CaLamViec (MaTK, TenNhanVien, ThoiGianMoCa, TrangThai) VALUES (?,?,?,'DANG_MO')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, maTK);
            ps.setString(2, tenNhanVien);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public CaLamViec layCaDangMo(int maTK) throws SQLException {
        String sql = "SELECT * FROM CaLamViec WHERE MaTK=? AND TrangThai='DANG_MO' ORDER BY MaCa DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public void dongCa(int maCa, BigDecimal tienMat, BigDecimal tienCK, BigDecimal tienOnline, int soGiaoDich) throws SQLException {
        String sql = "UPDATE CaLamViec SET ThoiGianDongCa=?, TongTienMat=?, TongTienChuyenKhoan=?, " +
                "TongTienOnline=?, SoGiaoDich=?, TrangThai='DA_DONG' WHERE MaCa=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            ps.setBigDecimal(2, tienMat);
            ps.setBigDecimal(3, tienCK);
            ps.setBigDecimal(4, tienOnline);
            ps.setInt(5, soGiaoDich);
            ps.setInt(6, maCa);
            ps.executeUpdate();
        }
    }

    public List<CaLamViec> layLichSu(int maTK, int gioiHan) throws SQLException {
        String sql = "SELECT * FROM CaLamViec WHERE MaTK=? ORDER BY MaCa DESC LIMIT ?";
        List<CaLamViec> ds = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTK);
            ps.setInt(2, gioiHan);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ds.add(map(rs));
            }
        }
        return ds;
    }

    private CaLamViec map(ResultSet rs) throws SQLException {
        CaLamViec ca = new CaLamViec();
        ca.setMaCa(rs.getInt("MaCa"));
        ca.setMaTK(rs.getInt("MaTK"));
        ca.setTenNhanVien(rs.getString("TenNhanVien"));
        Timestamp moTs = rs.getTimestamp("ThoiGianMoCa");
        if (moTs != null) ca.setThoiGianMoCa(moTs.toLocalDateTime());
        Timestamp dongTs = rs.getTimestamp("ThoiGianDongCa");
        if (dongTs != null) ca.setThoiGianDongCa(dongTs.toLocalDateTime());
        ca.setTongTienMat(rs.getBigDecimal("TongTienMat"));
        ca.setTongTienChuyenKhoan(rs.getBigDecimal("TongTienChuyenKhoan"));
        ca.setTongTienOnline(rs.getBigDecimal("TongTienOnline"));
        ca.setSoGiaoDich(rs.getInt("SoGiaoDich"));
        ca.setTrangThai(rs.getString("TrangThai"));
        return ca;
    }
}