package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.model.CauHinhNhacNoTuDong;

import java.sql.*;

public class CauHinhNhacNoTuDongDAO {

    /** Lay cau hinh dang duoc bat (DangApDung=1). Chi co toi da 1 cau hinh dang bat tai 1 thoi diem. */
    public CauHinhNhacNoTuDong layCauHinhDangApDung() throws SQLException {
        String sql = "SELECT * FROM CauHinhNhacNoTuDong WHERE DangApDung = 1 ORDER BY MaCauHinh DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) return map(rs);
            return null;
        }
    }

    /** Tao cau hinh moi va tat het cau hinh cu (chi 1 cau hinh duoc bat tai 1 thoi diem). */
    public void luuCauHinhMoi(CauHinhNhacNoTuDong cauHinh) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement tat = conn.prepareStatement("UPDATE CauHinhNhacNoTuDong SET DangApDung = 0")) {
                tat.executeUpdate();
            }
            String sql = "INSERT INTO CauHinhNhacNoTuDong (NgayGioBatDau, NgayGioKetThuc, DangApDung, NguoiTao) VALUES (?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setTimestamp(1, Timestamp.valueOf(cauHinh.getNgayGioBatDau()));
                ps.setTimestamp(2, Timestamp.valueOf(cauHinh.getNgayGioKetThuc()));
                ps.setBoolean(3, cauHinh.isDangApDung());
                ps.setString(4, cauHinh.getNguoiTao());
                ps.executeUpdate();
            }
            conn.commit();
        }
    }

    public void tatCauHinhDangApDung() throws SQLException {
        String sql = "UPDATE CauHinhNhacNoTuDong SET DangApDung = 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private CauHinhNhacNoTuDong map(ResultSet rs) throws SQLException {
        CauHinhNhacNoTuDong c = new CauHinhNhacNoTuDong();
        c.setMaCauHinh(rs.getInt("MaCauHinh"));
        c.setNgayGioBatDau(rs.getTimestamp("NgayGioBatDau").toLocalDateTime());
        c.setNgayGioKetThuc(rs.getTimestamp("NgayGioKetThuc").toLocalDateTime());
        c.setDangApDung(rs.getBoolean("DangApDung"));
        Timestamp tao = rs.getTimestamp("NgayTao");
        c.setNgayTao(tao != null ? tao.toLocalDateTime() : null);
        c.setNguoiTao(rs.getString("NguoiTao"));
        return c;
    }
}