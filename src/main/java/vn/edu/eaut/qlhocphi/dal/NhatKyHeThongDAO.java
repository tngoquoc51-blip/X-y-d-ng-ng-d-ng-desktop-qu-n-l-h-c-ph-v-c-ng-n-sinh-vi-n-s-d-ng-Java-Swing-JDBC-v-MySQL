package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.model.NhatKyHeThong;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NhatKyHeThongDAO {

    public void ghi(NhatKyHeThong nk) throws SQLException {
        String sql = "INSERT INTO NhatKyHeThong (MaTK, TenDangNhap, HanhDong, DoiTuong, ChiTiet, ThoiGian) VALUES (?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (nk.getMaTK() != null) ps.setInt(1, nk.getMaTK()); else ps.setNull(1, Types.INTEGER);
            ps.setString(2, nk.getTenDangNhap());
            ps.setString(3, nk.getHanhDong());
            ps.setString(4, nk.getDoiTuong());
            ps.setString(5, nk.getChiTiet());
            // Luon lay gio tu may chay ung dung Java, khong phu thuoc gio MySQL Server
            LocalDateTime thoiGian = nk.getThoiGian() != null ? nk.getThoiGian() : LocalDateTime.now();
            ps.setTimestamp(6, Timestamp.valueOf(thoiGian));
            ps.executeUpdate();
        }
    }

    /** Lay 200 dong gan nhat (moi nhat truoc) - du de xem trong 1 phien lam viec, tranh tai qua nhieu du lieu. */
    public List<NhatKyHeThong> layGanNhat() throws SQLException {
        String sql = "SELECT * FROM NhatKyHeThong ORDER BY ThoiGian DESC LIMIT 200";
        List<NhatKyHeThong> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private NhatKyHeThong map(ResultSet rs) throws SQLException {
        NhatKyHeThong nk = new NhatKyHeThong();
        nk.setMaNhatKy(rs.getInt("MaNhatKy"));
        int maTK = rs.getInt("MaTK");
        nk.setMaTK(rs.wasNull() ? null : maTK);
        nk.setTenDangNhap(rs.getString("TenDangNhap"));
        nk.setHanhDong(rs.getString("HanhDong"));
        nk.setDoiTuong(rs.getString("DoiTuong"));
        nk.setChiTiet(rs.getString("ChiTiet"));
        Timestamp ts = rs.getTimestamp("ThoiGian");
        nk.setThoiGian(ts != null ? ts.toLocalDateTime() : null);
        return nk;
    }
}