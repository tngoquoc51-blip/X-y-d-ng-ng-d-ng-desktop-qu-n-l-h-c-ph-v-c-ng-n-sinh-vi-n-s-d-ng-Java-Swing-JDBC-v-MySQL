package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.model.GiaoDichNganHang;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GiaoDichNganHangDAO {

    /** Them 1 giao dich moi nhap tu sao ke. Neu MaThamChieu da ton tai (trung lap khi nhap
     *  lai file cu / import chong 2 lan) -> nem SQLIntegrityConstraintViolationException,
     *  Service se bat rieng loi nay de dem "trung lap" thay vi coi la loi that su. */
    public int them(GiaoDichNganHang gd) throws SQLException {
        String sql = "INSERT INTO GiaoDichNganHang (MaThamChieu, ThoiGianGiaoDich, SoTien, NoiDung, TrangThaiDoiSoat) " +
                "VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, gd.getMaThamChieu());
            ps.setTimestamp(2, gd.getThoiGianGiaoDich() != null ? Timestamp.valueOf(gd.getThoiGianGiaoDich()) : null);
            ps.setBigDecimal(3, gd.getSoTien());
            ps.setString(4, gd.getNoiDung());
            ps.setString(5, gd.getTrangThaiDoiSoat());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public List<GiaoDichNganHang> layTheoTrangThai(String trangThai) throws SQLException {
        String sql = "SELECT * FROM GiaoDichNganHang WHERE TrangThaiDoiSoat = ? ORDER BY ThoiGianGiaoDich DESC";
        List<GiaoDichNganHang> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<GiaoDichNganHang> layTatCa() throws SQLException {
        String sql = "SELECT * FROM GiaoDichNganHang ORDER BY ThoiGianGiaoDich DESC";
        List<GiaoDichNganHang> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /** Cap nhat ket qua doi soat tu dong (goi tu engine matching). */
    public void capNhatKetQuaDoiSoat(int maGiaoDich, String trangThai, Integer maHoaDonKhop, Integer doTinCay) throws SQLException {
        String sql = "UPDATE GiaoDichNganHang SET TrangThaiDoiSoat=?, MaHoaDonKhop=?, DoTinCay=? WHERE MaGiaoDich=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            if (maHoaDonKhop != null) ps.setInt(2, maHoaDonKhop); else ps.setNull(2, Types.INTEGER);
            if (doTinCay != null) ps.setInt(3, doTinCay); else ps.setNull(3, Types.INTEGER);
            ps.setInt(4, maGiaoDich);
            ps.executeUpdate();
        }
    }

    /** Danh dau da xac nhan thu tien (sau khi Service da tao PhieuThu thanh cong). */
    public void danhDauDaXacNhan(int maGiaoDich, String nguoiXacNhan) throws SQLException {
        String sql = "UPDATE GiaoDichNganHang SET TrangThaiDoiSoat='DA_XAC_NHAN', NguoiXacNhan=?, ThoiGianXacNhan=NOW() WHERE MaGiaoDich=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nguoiXacNhan);
            ps.setInt(2, maGiaoDich);
            ps.executeUpdate();
        }
    }

    public void boQua(int maGiaoDich) throws SQLException {
        String sql = "UPDATE GiaoDichNganHang SET TrangThaiDoiSoat='BO_QUA' WHERE MaGiaoDich=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maGiaoDich);
            ps.executeUpdate();
        }
    }

    private GiaoDichNganHang map(ResultSet rs) throws SQLException {
        GiaoDichNganHang gd = new GiaoDichNganHang();
        gd.setMaGiaoDich(rs.getInt("MaGiaoDich"));
        gd.setMaThamChieu(rs.getString("MaThamChieu"));
        Timestamp tgGD = rs.getTimestamp("ThoiGianGiaoDich");
        gd.setThoiGianGiaoDich(tgGD != null ? tgGD.toLocalDateTime() : null);
        gd.setSoTien(rs.getBigDecimal("SoTien"));
        gd.setNoiDung(rs.getString("NoiDung"));
        gd.setTrangThaiDoiSoat(rs.getString("TrangThaiDoiSoat"));
        int maHD = rs.getInt("MaHoaDonKhop");
        gd.setMaHoaDonKhop(rs.wasNull() ? null : maHD);
        int tinCay = rs.getInt("DoTinCay");
        gd.setDoTinCay(rs.wasNull() ? null : tinCay);
        gd.setNguoiXacNhan(rs.getString("NguoiXacNhan"));
        Timestamp tgXN = rs.getTimestamp("ThoiGianXacNhan");
        gd.setThoiGianXacNhan(tgXN != null ? tgXN.toLocalDateTime() : null);
        Timestamp tgNhap = rs.getTimestamp("ThoiGianNhap");
        gd.setThoiGianNhap(tgNhap != null ? tgNhap.toLocalDateTime() : null);
        return gd;
    }
}