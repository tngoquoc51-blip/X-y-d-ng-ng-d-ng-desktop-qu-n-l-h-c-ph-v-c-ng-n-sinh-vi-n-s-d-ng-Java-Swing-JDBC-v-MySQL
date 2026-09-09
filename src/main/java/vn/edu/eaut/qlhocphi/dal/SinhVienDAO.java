package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.model.SinhVien;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SinhVienDAO {

    public List<SinhVien> layTatCa() throws SQLException {
        String sql = "SELECT * FROM SinhVien ORDER BY MaSV";
        List<SinhVien> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<SinhVien> timKiem(String keyword) throws SQLException {
        String sql = "SELECT * FROM SinhVien WHERE MaSV LIKE ? OR HoTen LIKE ? OR Lop LIKE ? ORDER BY MaSV";
        List<SinhVien> list = new ArrayList<>();
        String kw = "%" + keyword + "%";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setString(3, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public SinhVien timTheoMa(String maSV) throws SQLException {
        String sql = "SELECT * FROM SinhVien WHERE MaSV = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        }
        return null;
    }

    public boolean them(SinhVien sv) throws SQLException {
        String sql = "INSERT INTO SinhVien (MaSV,HoTen,Lop,Khoa,NgaySinh,Email,SoDienThoai,TrangThai,AnhDaiDien,SoDienThoaiPhuHuynh,QueQuan,DiaChi) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            bind(ps, sv);
            ps.setBoolean(8, sv.isTrangThai());
            ps.setString(9, sv.getAnhDaiDien());
            ps.setString(10, sv.getSoDienThoaiPhuHuynh());
            ps.setString(11, sv.getQueQuan());
            ps.setString(12, sv.getDiaChi());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean capNhat(SinhVien sv) throws SQLException {
        String sql = "UPDATE SinhVien SET HoTen=?,Lop=?,Khoa=?,NgaySinh=?,Email=?,SoDienThoai=?,TrangThai=?,AnhDaiDien=?,SoDienThoaiPhuHuynh=?,QueQuan=?,DiaChi=? WHERE MaSV=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sv.getHoTen());
            ps.setString(2, sv.getLop());
            ps.setString(3, sv.getKhoa());
            ps.setDate(4, sv.getNgaySinh() != null ? Date.valueOf(sv.getNgaySinh()) : null);
            ps.setString(5, sv.getEmail());
            ps.setString(6, sv.getSoDienThoai());
            ps.setBoolean(7, sv.isTrangThai());
            ps.setString(8, sv.getAnhDaiDien());
            ps.setString(9, sv.getSoDienThoaiPhuHuynh());
            ps.setString(10, sv.getQueQuan());
            ps.setString(11, sv.getDiaChi());
            ps.setString(12, sv.getMaSV());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean xoa(String maSV) throws SQLException {
        String sql = "DELETE FROM SinhVien WHERE MaSV = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            return ps.executeUpdate() > 0;
        }
    }

    private void bind(PreparedStatement ps, SinhVien sv) throws SQLException {
        ps.setString(1, sv.getMaSV());
        ps.setString(2, sv.getHoTen());
        ps.setString(3, sv.getLop());
        ps.setString(4, sv.getKhoa());
        ps.setDate(5, sv.getNgaySinh() != null ? Date.valueOf(sv.getNgaySinh()) : null);
        ps.setString(6, sv.getEmail());
        ps.setString(7, sv.getSoDienThoai());
    }

    private SinhVien map(ResultSet rs) throws SQLException {
        SinhVien sv = new SinhVien();
        sv.setMaSV(rs.getString("MaSV"));
        sv.setHoTen(rs.getString("HoTen"));
        sv.setLop(rs.getString("Lop"));
        sv.setKhoa(rs.getString("Khoa"));
        Date ns = rs.getDate("NgaySinh");
        sv.setNgaySinh(ns != null ? ns.toLocalDate() : null);
        sv.setEmail(rs.getString("Email"));
        sv.setSoDienThoai(rs.getString("SoDienThoai"));
        sv.setTrangThai(rs.getBoolean("TrangThai"));
        sv.setAnhDaiDien(rs.getString("AnhDaiDien"));
        sv.setSoDienThoaiPhuHuynh(rs.getString("SoDienThoaiPhuHuynh"));
        sv.setQueQuan(rs.getString("QueQuan"));
        sv.setDiaChi(rs.getString("DiaChi"));
        return sv;
    }
}