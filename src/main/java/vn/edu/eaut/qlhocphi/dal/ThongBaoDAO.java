package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.model.ThongBao;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ThongBaoDAO {

    /** Tao 1 thong bao moi (goi tu cac diem "bien dong" trong nghiep vu). */
    public void taoThongBao(String manHinhKey, String vaiTroNhan, String maSVNhan, String tieuDe, String noiDung) throws SQLException {
        String sql = "INSERT INTO ThongBao (ManHinhKey, VaiTroNhan, MaSVNhan, TieuDe, NoiDung) VALUES (?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, manHinhKey);
            ps.setString(2, vaiTroNhan);
            if (maSVNhan != null) ps.setString(3, maSVNhan); else ps.setNull(3, Types.VARCHAR);
            ps.setString(4, tieuDe);
            ps.setString(5, noiDung);
            ps.executeUpdate();
        }
    }

    /** Dem so thong bao CHUA DOC, gom nhom theo ManHinhKey - dung de ve badge tren sidebar. */
    public Map<String, Integer> demChuaDocTheoManHinh(int maTK, String vaiTro, String maSV) throws SQLException {
        String sql = "SELECT tb.ManHinhKey, COUNT(*) AS SoLuong " +
                "FROM ThongBao tb " +
                "WHERE (tb.VaiTroNhan = 'ALL' OR (tb.VaiTroNhan = ? AND (tb.MaSVNhan IS NULL OR tb.MaSVNhan = ?))) " +
                "AND NOT EXISTS (SELECT 1 FROM ThongBaoDaDoc d WHERE d.MaThongBao = tb.MaThongBao AND d.MaTK = ?) " +
                "GROUP BY tb.ManHinhKey";
        Map<String, Integer> ketQua = new LinkedHashMap<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, vaiTro);
            if (maSV != null) ps.setString(2, maSV); else ps.setNull(2, Types.VARCHAR);
            ps.setInt(3, maTK);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ketQua.put(rs.getString("ManHinhKey"), rs.getInt("SoLuong"));
            }
        }
        return ketQua;
    }

    public void danhDauDaDocTheoManHinh(int maTK, String vaiTro, String maSV, String manHinhKey) throws SQLException {
        String sql = "INSERT IGNORE INTO ThongBaoDaDoc (MaThongBao, MaTK) " +
                "SELECT tb.MaThongBao, ? FROM ThongBao tb " +
                "WHERE tb.ManHinhKey = ? " +
                "AND (tb.VaiTroNhan = 'ALL' OR (tb.VaiTroNhan = ? AND (tb.MaSVNhan IS NULL OR tb.MaSVNhan = ?)))";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTK);
            ps.setString(2, manHinhKey);
            ps.setString(3, vaiTro);
            if (maSV != null) ps.setString(4, maSV); else ps.setNull(4, Types.VARCHAR);
            ps.executeUpdate();
        }
    }

    public List<ThongBao> layChuaDocTheoManHinh(int maTK, String vaiTro, String maSV, String manHinhKey) throws SQLException {
        String sql = "SELECT tb.* FROM ThongBao tb " +
                "WHERE tb.ManHinhKey = ? " +
                "AND (tb.VaiTroNhan = 'ALL' OR (tb.VaiTroNhan = ? AND (tb.MaSVNhan IS NULL OR tb.MaSVNhan = ?))) " +
                "AND NOT EXISTS (SELECT 1 FROM ThongBaoDaDoc d WHERE d.MaThongBao = tb.MaThongBao AND d.MaTK = ?) " +
                "ORDER BY tb.ThoiGianTao DESC";
        List<ThongBao> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, manHinhKey);
            ps.setString(2, vaiTro);
            if (maSV != null) ps.setString(3, maSV); else ps.setNull(3, Types.VARCHAR);
            ps.setInt(4, maTK);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    /** Lay TAT CA thong bao danh cho sinh vien (ca da doc / chua doc). */
    public List<ThongBao> layTatCaChoSinhVien(int maTK, String maSV) throws SQLException {
        String sql = "SELECT tb.*, " +
                "CASE WHEN d.MaThongBao IS NULL THEN 0 ELSE 1 END AS DaDoc " +
                "FROM ThongBao tb " +
                "LEFT JOIN ThongBaoDaDoc d ON d.MaThongBao = tb.MaThongBao AND d.MaTK = ? " +
                "WHERE (tb.VaiTroNhan = 'ALL' OR tb.VaiTroNhan = 'SINHVIEN') " +
                "AND (tb.MaSVNhan IS NULL OR tb.MaSVNhan = ?) " +
                "ORDER BY DaDoc ASC, tb.ThoiGianTao DESC " +
                "LIMIT 200";
        List<ThongBao> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTK);
            ps.setString(2, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ThongBao tb = map(rs);
                    tb.setDaDoc(rs.getInt("DaDoc") == 1);
                    list.add(tb);
                }
            }
        }
        return list;
    }

    public int demChuaDocSinhVien(int maTK, String maSV) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ThongBao tb " +
                "WHERE (tb.VaiTroNhan = 'ALL' OR tb.VaiTroNhan = 'SINHVIEN') " +
                "AND (tb.MaSVNhan IS NULL OR tb.MaSVNhan = ?) " +
                "AND NOT EXISTS (SELECT 1 FROM ThongBaoDaDoc d WHERE d.MaThongBao = tb.MaThongBao AND d.MaTK = ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setInt(2, maTK);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public void danhDauDaDocMot(int maTK, int maThongBao) throws SQLException {
        String sql = "INSERT IGNORE INTO ThongBaoDaDoc (MaThongBao, MaTK) VALUES (?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maThongBao);
            ps.setInt(2, maTK);
            ps.executeUpdate();
        }
    }

    public void danhDauTatCaDaDocSinhVien(int maTK, String maSV) throws SQLException {
        String sql = "INSERT IGNORE INTO ThongBaoDaDoc (MaThongBao, MaTK) " +
                "SELECT tb.MaThongBao, ? FROM ThongBao tb " +
                "WHERE (tb.VaiTroNhan = 'ALL' OR tb.VaiTroNhan = 'SINHVIEN') " +
                "AND (tb.MaSVNhan IS NULL OR tb.MaSVNhan = ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTK);
            ps.setString(2, maSV);
            ps.executeUpdate();
        }
    }

    /** Lay tat ca thong bao theo vai tro + man hinh key (hop thu noi bo). */
    public List<ThongBao> layTatCaTheoVaiTro(int maTK, String vaiTro, String manHinhKey) throws SQLException {
        String sql = "SELECT tb.*, " +
                "CASE WHEN d.MaThongBao IS NULL THEN 0 ELSE 1 END AS DaDoc " +
                "FROM ThongBao tb " +
                "LEFT JOIN ThongBaoDaDoc d ON d.MaThongBao = tb.MaThongBao AND d.MaTK = ? " +
                "WHERE tb.ManHinhKey = ? " +
                "AND (tb.VaiTroNhan = 'ALL' OR tb.VaiTroNhan = ?) " +
                "ORDER BY DaDoc ASC, tb.ThoiGianTao DESC LIMIT 200";
        List<ThongBao> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maTK);
            ps.setString(2, manHinhKey);
            ps.setString(3, vaiTro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ThongBao tb = map(rs);
                    try {
                        tb.setDaDoc(rs.getInt("DaDoc") == 1);
                    } catch (SQLException ignored) {
                    }
                    list.add(tb);
                }
            }
        }
        return list;
    }

    private ThongBao map(ResultSet rs) throws SQLException {
        ThongBao tb = new ThongBao();
        tb.setMaThongBao(rs.getInt("MaThongBao"));
        tb.setManHinhKey(rs.getString("ManHinhKey"));
        tb.setVaiTroNhan(rs.getString("VaiTroNhan"));
        tb.setMaSVNhan(rs.getString("MaSVNhan"));
        tb.setTieuDe(rs.getString("TieuDe"));
        tb.setNoiDung(rs.getString("NoiDung"));
        Timestamp ts = rs.getTimestamp("ThoiGianTao");
        tb.setThoiGianTao(ts != null ? ts.toLocalDateTime() : null);
        return tb;
    }
}