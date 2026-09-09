package vn.edu.eaut.qlhocphi.dal;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.model.ViDienTu;

import java.math.BigDecimal;
import java.sql.*;

public class ViDienTuDAO {

    public ViDienTu timTheoMaSV(String maSV) throws SQLException {
        String sql = "SELECT * FROM ViDienTu WHERE MaSV = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
                return null;
            }
        }
    }

    /** Tao vi voi so du 0 neu SV chua co vi. Dung INSERT IGNORE de an toan khi goi nhieu lan. */
    public void taoMoiNeuChuaCo(String maSV) throws SQLException {
        String sql = "INSERT IGNORE INTO ViDienTu (MaSV, SoDu) VALUES (?, 0)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.executeUpdate();
        }
    }

    public void congTien(String maSV, BigDecimal soTien) throws SQLException {
        String sql = "UPDATE ViDienTu SET SoDu = SoDu + ?, NgayCapNhat = NOW() WHERE MaSV = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, soTien);
            ps.setString(2, maSV);
            ps.executeUpdate();
        }
    }

    /**
     * Tru tien CO DIEU KIEN ngay trong cau SQL (SoDu >= soTien) - trach nhiem toan
     * ven (atomic) o tang CSDL, tranh tinh trang 2 luong cung tru lam am so du.
     * Tra ve true neu tru thanh cong (nghia la truoc do du tien), false neu khong du.
     */
    public boolean truTienNeuDu(String maSV, BigDecimal soTien) throws SQLException {
        String sql = "UPDATE ViDienTu SET SoDu = SoDu - ?, NgayCapNhat = NOW() WHERE MaSV = ? AND SoDu >= ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, soTien);
            ps.setString(2, maSV);
            ps.setBigDecimal(3, soTien);
            return ps.executeUpdate() > 0;
        }
    }

    private ViDienTu map(ResultSet rs) throws SQLException {
        ViDienTu vi = new ViDienTu();
        vi.setMaSV(rs.getString("MaSV"));
        vi.setSoDu(rs.getBigDecimal("SoDu"));
        Timestamp ts = rs.getTimestamp("NgayCapNhat");
        vi.setNgayCapNhat(ts != null ? ts.toLocalDateTime() : null);
        return vi;
    }
}