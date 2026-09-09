package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.model.LichThuTuDong;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LichThuTuDongDAO {

    public int them(LichThuTuDong lich) throws SQLException {
        String sql = "INSERT INTO LichThuTuDong (MaHoaDon, NgayBatDauQuet, NgayKetThucQuet, NguoiTao) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, lich.getMaHoaDon());
            ps.setDate(2, Date.valueOf(lich.getNgayBatDauQuet()));
            ps.setDate(3, Date.valueOf(lich.getNgayKetThucQuet()));
            ps.setString(4, lich.getNguoiTao());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    /** Lay tat ca lich dang o trang thai DANG_CHO - danh cho scheduler quet dinh ky. */
    public List<LichThuTuDong> layDangCho() throws SQLException {
        String sql = "SELECT l.*, h.MaSV, s.HoTen AS TenSV FROM LichThuTuDong l " +
                "JOIN HoaDonHocPhi h ON l.MaHoaDon = h.MaHoaDon " +
                "JOIN SinhVien s ON h.MaSV = s.MaSV " +
                "WHERE l.TrangThai = 'DANG_CHO'";
        return layTheoCau(sql);
    }

    /** Lay toan bo lich (moi tao truoc) - danh cho man hinh quan tri xem danh sach. */
    public List<LichThuTuDong> layTatCa() throws SQLException {
        String sql = "SELECT l.*, h.MaSV, s.HoTen AS TenSV FROM LichThuTuDong l " +
                "JOIN HoaDonHocPhi h ON l.MaHoaDon = h.MaHoaDon " +
                "JOIN SinhVien s ON h.MaSV = s.MaSV " +
                "ORDER BY l.NgayTao DESC";
        return layTheoCau(sql);
    }

    private List<LichThuTuDong> layTheoCau(String sql) throws SQLException {
        List<LichThuTuDong> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public void capNhatTrangThai(int maLich, String trangThaiMoi) throws SQLException {
        String sql = "UPDATE LichThuTuDong SET TrangThai = ?, LanQuetGanNhat = NOW() WHERE MaLich = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThaiMoi);
            ps.setInt(2, maLich);
            ps.executeUpdate();
        }
    }

    /** Chi cap nhat "lan quet gan nhat", giu nguyen trang thai (VD: van DANG_CHO vi chua du tien). */
    public void capNhatLanQuet(int maLich) throws SQLException {
        String sql = "UPDATE LichThuTuDong SET LanQuetGanNhat = NOW() WHERE MaLich = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maLich);
            ps.executeUpdate();
        }
    }

    public void huy(int maLich) throws SQLException {
        capNhatTrangThai(maLich, "DA_HUY");
    }

    private LichThuTuDong map(ResultSet rs) throws SQLException {
        LichThuTuDong l = new LichThuTuDong();
        l.setMaLich(rs.getInt("MaLich"));
        l.setMaHoaDon(rs.getInt("MaHoaDon"));
        l.setMaSV(rs.getString("MaSV"));
        l.setTenSV(rs.getString("TenSV"));
        l.setNgayBatDauQuet(rs.getDate("NgayBatDauQuet").toLocalDate());
        l.setNgayKetThucQuet(rs.getDate("NgayKetThucQuet").toLocalDate());
        l.setTrangThai(rs.getString("TrangThai"));
        Timestamp tsTao = rs.getTimestamp("NgayTao");
        l.setNgayTao(tsTao != null ? tsTao.toLocalDateTime() : null);
        l.setNguoiTao(rs.getString("NguoiTao"));
        Timestamp tsQuet = rs.getTimestamp("LanQuetGanNhat");
        l.setLanQuetGanNhat(tsQuet != null ? tsQuet.toLocalDateTime() : null);
        return l;
    }
}