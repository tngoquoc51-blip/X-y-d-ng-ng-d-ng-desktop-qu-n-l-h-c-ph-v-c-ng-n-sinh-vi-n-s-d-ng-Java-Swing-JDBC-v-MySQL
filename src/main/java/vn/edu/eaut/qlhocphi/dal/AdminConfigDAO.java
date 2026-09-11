package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.model.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** DAO cho toàn bộ cấu hình Admin (kỹ thuật, bảo mật, scheduler, mẫu TB, broadcast). */
public class AdminConfigDAO {

    // ========== Cấu hình kỹ thuật ==========
    public List<CauHinhHeThong> layTheoNhom(String nhom) throws SQLException {
        String sql = "SELECT * FROM cauhinhhethong WHERE Nhom=? ORDER BY Khoa";
        List<CauHinhHeThong> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nhom);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapCauHinh(rs));
            }
        }
        return list;
    }

    public List<CauHinhHeThong> layTatCaCauHinh() throws SQLException {
        String sql = "SELECT * FROM cauhinhhethong ORDER BY Nhom, Khoa";
        List<CauHinhHeThong> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapCauHinh(rs));
        }
        return list;
    }

    public void luuCauHinh(String nhom, String khoa, String giaTri, String nguoi) throws SQLException {
        String sql = "INSERT INTO cauhinhhethong (Nhom, Khoa, GiaTri, NguoiCapNhat) VALUES (?,?,?,?) "
                + "ON DUPLICATE KEY UPDATE GiaTri=VALUES(GiaTri), NguoiCapNhat=VALUES(NguoiCapNhat)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nhom);
            ps.setString(2, khoa);
            ps.setString(3, giaTri);
            ps.setString(4, nguoi);
            ps.executeUpdate();
        }
    }

    public String layGiaTri(String nhom, String khoa) throws SQLException {
        String sql = "SELECT GiaTri FROM cauhinhhethong WHERE Nhom=? AND Khoa=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, nhom);
            ps.setString(2, khoa);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString(1);
            }
        }
        return null;
    }

    private CauHinhHeThong mapCauHinh(ResultSet rs) throws SQLException {
        CauHinhHeThong x = new CauHinhHeThong();
        x.setMaCauHinh(rs.getInt("MaCauHinh"));
        x.setNhom(rs.getString("Nhom"));
        x.setKhoa(rs.getString("Khoa"));
        x.setGiaTri(rs.getString("GiaTri"));
        x.setMoTa(rs.getString("MoTa"));
        return x;
    }

    // ========== Bảo mật ==========
    public CauHinhBaoMat layCauHinhBaoMat() throws SQLException {
        String sql = "SELECT * FROM cauhinhbaomat WHERE MaCauHinh=1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                CauHinhBaoMat b = new CauHinhBaoMat();
                b.setDoDaiMatKhauToiThieu(rs.getInt("DoDaiMatKhauToiThieu"));
                b.setSessionTimeoutPhut(rs.getInt("SessionTimeoutPhut"));
                b.setSoLanDangNhapSaiToiDa(rs.getInt("SoLanDangNhapSaiToiDa"));
                b.setThoiGianKhoaPhut(rs.getInt("ThoiGianKhoaPhut"));
                b.setBatBuocDoiMkLanDau(rs.getBoolean("BatBuocDoiMkLanDau"));
                return b;
            }
        }
        return new CauHinhBaoMat();
    }

    public void luuCauHinhBaoMat(CauHinhBaoMat b) throws SQLException {
        String sql = "UPDATE cauhinhbaomat SET DoDaiMatKhauToiThieu=?, SessionTimeoutPhut=?, "
                + "SoLanDangNhapSaiToiDa=?, ThoiGianKhoaPhut=?, BatBuocDoiMkLanDau=? WHERE MaCauHinh=1";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, b.getDoDaiMatKhauToiThieu());
            ps.setInt(2, b.getSessionTimeoutPhut());
            ps.setInt(3, b.getSoLanDangNhapSaiToiDa());
            ps.setInt(4, b.getThoiGianKhoaPhut());
            ps.setBoolean(5, b.isBatBuocDoiMkLanDau());
            ps.executeUpdate();
        }
    }

    // ========== Scheduler ==========
    public List<SchedulerTrangThai> layTatCaScheduler() throws SQLException {
        String sql = "SELECT * FROM scheduler_trangthai ORDER BY MaScheduler";
        List<SchedulerTrangThai> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SchedulerTrangThai s = new SchedulerTrangThai();
                s.setMaScheduler(rs.getString("MaScheduler"));
                s.setTenHienThi(rs.getString("TenHienThi"));
                s.setTrangThai(rs.getString("TrangThai"));
                Timestamp t = rs.getTimestamp("LanChayGanNhat");
                if (t != null) s.setLanChayGanNhat(t.toLocalDateTime());
                s.setKetQuaGanNhat(rs.getString("KetQuaGanNhat"));
                s.setGhiChu(rs.getString("GhiChu"));
                list.add(s);
            }
        }
        return list;
    }

    public void capNhatScheduler(String ma, String trangThai, String ketQua) throws SQLException {
        String sql = "UPDATE scheduler_trangthai SET TrangThai=?, LanChayGanNhat=NOW(), KetQuaGanNhat=? WHERE MaScheduler=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setString(2, ketQua);
            ps.setString(3, ma);
            ps.executeUpdate();
        }
    }

    // ========== Mẫu thông báo ==========
    public List<MauThongBao> layTatCaMau() throws SQLException {
        String sql = "SELECT * FROM mau_thongbao ORDER BY Kenh, MaLoai";
        List<MauThongBao> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                MauThongBao m = new MauThongBao();
                m.setMaMau(rs.getInt("MaMau"));
                m.setMaLoai(rs.getString("MaLoai"));
                m.setKenh(rs.getString("Kenh"));
                m.setTieuDe(rs.getString("TieuDe"));
                m.setNoiDung(rs.getString("NoiDung"));
                m.setBienHoTro(rs.getString("BienHoTro"));
                m.setDangDung(rs.getBoolean("DangDung"));
                list.add(m);
            }
        }
        return list;
    }

    public void capNhatMau(MauThongBao m) throws SQLException {
        String sql = "UPDATE mau_thongbao SET TieuDe=?, NoiDung=?, DangDung=? WHERE MaMau=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, m.getTieuDe());
            ps.setString(2, m.getNoiDung());
            ps.setBoolean(3, m.isDangDung());
            ps.setInt(4, m.getMaMau());
            ps.executeUpdate();
        }
    }

    // ========== Broadcast ==========
    public List<ThongBaoBroadcast> layTatCaBroadcast() throws SQLException {
        String sql = "SELECT * FROM thongbao_broadcast ORDER BY NgayTao DESC";
        List<ThongBaoBroadcast> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapBroadcast(rs));
        }
        return list;
    }

    /** Lấy các broadcast đang hiệu lực (để hiện khi đăng nhập). */
    public List<ThongBaoBroadcast> layBroadcastDangHieuLuc() throws SQLException {
        String sql = "SELECT * FROM thongbao_broadcast WHERE DangBat=1 "
                + "AND HienThiTu <= NOW() AND (HienThiDen IS NULL OR HienThiDen >= NOW()) "
                + "ORDER BY FIELD(MucDo,'KHAN_CAP','CANH_BAO','THONG_TIN'), NgayTao DESC";
        List<ThongBaoBroadcast> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(mapBroadcast(rs));
        }
        return list;
    }

    public void themBroadcast(ThongBaoBroadcast b) throws SQLException {
        String sql = "INSERT INTO thongbao_broadcast (TieuDe, NoiDung, MucDo, HienThiTu, HienThiDen, DangBat, NguoiTao) "
                + "VALUES (?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, b.getTieuDe());
            ps.setString(2, b.getNoiDung());
            ps.setString(3, b.getMucDo());
            ps.setTimestamp(4, Timestamp.valueOf(b.getHienThiTu()));
            if (b.getHienThiDen() != null) ps.setTimestamp(5, Timestamp.valueOf(b.getHienThiDen()));
            else ps.setNull(5, Types.TIMESTAMP);
            ps.setBoolean(6, b.isDangBat());
            ps.setString(7, b.getNguoiTao());
            ps.executeUpdate();
        }
    }

    public void capNhatBroadcast(ThongBaoBroadcast b) throws SQLException {
        String sql = "UPDATE thongbao_broadcast SET TieuDe=?, NoiDung=?, MucDo=?, HienThiTu=?, HienThiDen=?, DangBat=? WHERE MaBroadcast=?";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, b.getTieuDe());
            ps.setString(2, b.getNoiDung());
            ps.setString(3, b.getMucDo());
            ps.setTimestamp(4, Timestamp.valueOf(b.getHienThiTu()));
            if (b.getHienThiDen() != null) ps.setTimestamp(5, Timestamp.valueOf(b.getHienThiDen()));
            else ps.setNull(5, Types.TIMESTAMP);
            ps.setBoolean(6, b.isDangBat());
            ps.setInt(7, b.getMaBroadcast());
            ps.executeUpdate();
        }
    }

    public void xoaBroadcast(int ma) throws SQLException {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM thongbao_broadcast WHERE MaBroadcast=?")) {
            ps.setInt(1, ma);
            ps.executeUpdate();
        }
    }

    private ThongBaoBroadcast mapBroadcast(ResultSet rs) throws SQLException {
        ThongBaoBroadcast b = new ThongBaoBroadcast();
        b.setMaBroadcast(rs.getInt("MaBroadcast"));
        b.setTieuDe(rs.getString("TieuDe"));
        b.setNoiDung(rs.getString("NoiDung"));
        b.setMucDo(rs.getString("MucDo"));
        Timestamp t1 = rs.getTimestamp("HienThiTu");
        if (t1 != null) b.setHienThiTu(t1.toLocalDateTime());
        Timestamp t2 = rs.getTimestamp("HienThiDen");
        if (t2 != null) b.setHienThiDen(t2.toLocalDateTime());
        b.setDangBat(rs.getBoolean("DangBat"));
        b.setNguoiTao(rs.getString("NguoiTao"));
        Timestamp t3 = rs.getTimestamp("NgayTao");
        if (t3 != null) b.setNgayTao(t3.toLocalDateTime());
        return b;
    }
}