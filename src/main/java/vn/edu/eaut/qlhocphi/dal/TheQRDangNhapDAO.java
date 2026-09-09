package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.model.TheQRDangNhap;

import java.sql.*;
import java.time.LocalDateTime;

public class TheQRDangNhapDAO {

    /** Vo hieu hoa TAT CA the QR cu (con hieu luc) cua 1 tai khoan - dam bao tai 1 thoi
     *  diem chi co DUY NHAT 1 the QR con dung, tranh nhieu the cu-moi cung dang nhap duoc. */
    public void thuHoiTatCa(int maTK) throws SQLException {
        String sql = "UPDATE TheQRDangNhap SET TrangThai = 0 WHERE MaTK = ? AND TrangThai = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTK);
            ps.executeUpdate();
        }
    }

    public void taoMoi(int maTK, String token) throws SQLException {
        String sql = "INSERT INTO TheQRDangNhap (MaTK, Token, TrangThai) VALUES (?, ?, 1)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTK);
            ps.setString(2, token);
            ps.executeUpdate();
        }
    }

    /** Tim the QR CON HIEU LUC theo token - dung khi sinh vien quet QR de dang nhap. */
    public TheQRDangNhap timTheoToken(String token) throws SQLException {
        String sql = "SELECT * FROM TheQRDangNhap WHERE Token = ? AND TrangThai = 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    private TheQRDangNhap map(ResultSet rs) throws SQLException {
        TheQRDangNhap t = new TheQRDangNhap();
        t.setMaThe(rs.getInt("MaThe"));
        t.setMaTK(rs.getInt("MaTK"));
        t.setToken(rs.getString("Token"));
        Timestamp ts = rs.getTimestamp("ThoiGianTao");
        t.setThoiGianTao(ts != null ? ts.toLocalDateTime() : null);
        t.setTrangThai(rs.getBoolean("TrangThai"));
        return t;
    }
}