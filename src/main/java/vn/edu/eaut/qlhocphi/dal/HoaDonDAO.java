package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class HoaDonDAO {

    private static final String SELECT_JOIN =
            "SELECT hd.*, sv.HoTen AS TenSV, hk.TenHocKy AS TenHocKy, " +
                    "COALESCE((SELECT SUM(pt.SoTienNop) FROM PhieuThu pt WHERE pt.MaHoaDon = hd.MaHoaDon), 0) AS DaNop " +
                    "FROM HoaDonHocPhi hd " +
                    "JOIN SinhVien sv ON sv.MaSV = hd.MaSV " +
                    "JOIN HocKy hk ON hk.MaHocKy = hd.MaHocKy ";

    public List<HoaDonHocPhi> layTatCa() throws SQLException {
        String sql = SELECT_JOIN + "ORDER BY hd.MaHoaDon DESC";
        List<HoaDonHocPhi> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<HoaDonHocPhi> layTheoSinhVien(String maSV) throws SQLException {
        String sql = SELECT_JOIN + "WHERE hd.MaSV = ? ORDER BY hd.MaHoaDon DESC";
        List<HoaDonHocPhi> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public HoaDonHocPhi timTheoMa(int maHoaDon) throws SQLException {
        String sql = SELECT_JOIN + "WHERE hd.MaHoaDon = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHoaDon);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public int them(HoaDonHocPhi hd) throws SQLException {
        String sql = "INSERT INTO HoaDonHocPhi (MaSV,MaHocKy,SoTinChi,SoTien,HanThanhToan) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, hd.getMaSV());
            ps.setInt(2, hd.getMaHocKy());
            ps.setInt(3, hd.getSoTinChi());
            ps.setBigDecimal(4, hd.getSoTien());
            ps.setDate(5, hd.getHanThanhToan() != null ? Date.valueOf(hd.getHanThanhToan()) : null);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    /** MOI: cap nhat % mien giam + ly do cho 1 hoa don da co san. */
    public void capNhatMienGiam(int maHoaDon, BigDecimal tyLeMienGiam, String lyDo) throws SQLException {
        String sql = "UPDATE HoaDonHocPhi SET TyLeMienGiam = ?, LyDoMienGiam = ? WHERE MaHoaDon = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, tyLeMienGiam);
            ps.setString(2, lyDo);
            ps.setInt(3, maHoaDon);
            ps.executeUpdate();
        }
    }

    /** MOI: cap nhat ngay gan nhat da gui nhac no (dung cho tinh nang tu dong nhac no qua email/SMS). */
    public void capNhatNgayNhacNo(int maHoaDon, LocalDate ngayNhacNo) throws SQLException {
        String sql = "UPDATE HoaDonHocPhi SET NgayNhacNoGanNhat = ? WHERE MaHoaDon = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, ngayNhacNo != null ? Date.valueOf(ngayNhacNo) : null);
            ps.setInt(2, maHoaDon);
            ps.executeUpdate();
        }
    }

    /** MOI: cap nhat thoi diem CHINH XAC (ca gio/phut) da gui email nhac no cho sinh vien. */
    public void capNhatThoiDiemGuiEmail(int maHoaDon, LocalDateTime thoiDiem) throws SQLException {
        String sql = "UPDATE HoaDonHocPhi SET ThoiDiemGuiEmailSV = ? WHERE MaHoaDon = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(thoiDiem));
            ps.setInt(2, maHoaDon);
            ps.executeUpdate();
        }
    }

    /** MOI: cap nhat thoi diem CHINH XAC (ca gio/phut) da gui SMS nhac no cho phu huynh. */
    public void capNhatThoiDiemGuiSms(int maHoaDon, LocalDateTime thoiDiem) throws SQLException {
        String sql = "UPDATE HoaDonHocPhi SET ThoiDiemGuiSmsPH = ? WHERE MaHoaDon = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(thoiDiem));
            ps.setInt(2, maHoaDon);
            ps.executeUpdate();
        }
    }

    public void xoa(int maHoaDon) throws SQLException {
        String sql = "DELETE FROM HoaDonHocPhi WHERE MaHoaDon = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHoaDon);
            ps.executeUpdate();
        }
    }

    private HoaDonHocPhi map(ResultSet rs) throws SQLException {
        HoaDonHocPhi hd = new HoaDonHocPhi();
        hd.setMaHoaDon(rs.getInt("MaHoaDon"));
        hd.setMaSV(rs.getString("MaSV"));
        hd.setTenSV(rs.getString("TenSV"));
        hd.setMaHocKy(rs.getInt("MaHocKy"));
        hd.setTenHocKy(rs.getString("TenHocKy"));
        hd.setSoTinChi(rs.getInt("SoTinChi"));
        hd.setSoTien(rs.getBigDecimal("SoTien"));
        BigDecimal daNop = rs.getBigDecimal("DaNop");
        hd.setDaNop(daNop != null ? daNop : BigDecimal.ZERO);
        Date han = rs.getDate("HanThanhToan");
        hd.setHanThanhToan(han != null ? han.toLocalDate() : null);
        Date nhacNo = rs.getDate("NgayNhacNoGanNhat");
        hd.setNgayNhacNoGanNhat(nhacNo != null ? nhacNo.toLocalDate() : null);
        Timestamp tao = rs.getTimestamp("NgayTao");
        hd.setNgayTao(tao != null ? tao.toLocalDateTime() : null);
        hd.setTyLeMienGiam(rs.getBigDecimal("TyLeMienGiam"));
        hd.setLyDoMienGiam(rs.getString("LyDoMienGiam"));
        Timestamp tsEmail = rs.getTimestamp("ThoiDiemGuiEmailSV");
        hd.setThoiDiemGuiEmailSV(tsEmail != null ? tsEmail.toLocalDateTime() : null);
        Timestamp tsSms = rs.getTimestamp("ThoiDiemGuiSmsPH");
        hd.setThoiDiemGuiSmsPH(tsSms != null ? tsSms.toLocalDateTime() : null);
        return hd;
    }
}