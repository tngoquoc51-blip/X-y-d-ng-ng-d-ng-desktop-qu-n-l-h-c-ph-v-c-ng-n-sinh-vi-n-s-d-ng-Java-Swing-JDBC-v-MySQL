package vn.edu.eaut.qlhocphi.util;

import vn.edu.eaut.qlhocphi.config.DBConnection;
import vn.edu.eaut.qlhocphi.dal.SinhVienDAO;
import vn.edu.eaut.qlhocphi.model.SinhVien;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;

/**
 * Công cụ chuyển dữ liệu cũ (plaintext) sang dạng mã hóa.
 * Chạy một lần sau khi triển khai EncryptionUtils.
 * <p>
 * Cách dùng:
 *   java -cp ... vn.edu.eaut.qlhocphi.util.DataEncryptionMigrator
 * hoặc gọi từ menu Admin (nếu muốn).
 */
public class DataEncryptionMigrator {

    public static void main(String[] args) {
        System.out.println("=== Bắt đầu migrate mã hóa dữ liệu SinhVien ===");
        try {
            int count = migrateSinhVien();
            System.out.println("Hoàn tất! Đã mã hóa " + count + " bản ghi.");
        } catch (Exception e) {
            System.err.println("Lỗi migrate: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Đọc tất cả sinh viên, nếu trường nhạy cảm chưa mã hóa thì mã hóa và cập nhật lại.
     * Dữ liệu đã mã hóa sẽ được bỏ qua → an toàn chạy nhiều lần.
     */
    public static int migrateSinhVien() throws Exception {
        SinhVienDAO dao = new SinhVienDAO();
        List<SinhVien> list = dao.layTatCa(); // đã decrypt sẵn
        int updated = 0;

        String sql = "UPDATE SinhVien SET Email=?, SoDienThoai=?, SoDienThoaiPhuHuynh=?, QueQuan=?, DiaChi=? WHERE MaSV=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (SinhVien sv : list) {
                // Kiểm tra xem DB còn plaintext không (đọc raw)
                if (needsEncryption(sv.getMaSV())) {
                    ps.setString(1, EncryptionUtils.encrypt(sv.getEmail()));
                    ps.setString(2, EncryptionUtils.encrypt(sv.getSoDienThoai()));
                    ps.setString(3, EncryptionUtils.encrypt(sv.getSoDienThoaiPhuHuynh()));
                    ps.setString(4, EncryptionUtils.encrypt(sv.getQueQuan()));
                    ps.setString(5, EncryptionUtils.encrypt(sv.getDiaChi()));
                    ps.setString(6, sv.getMaSV());
                    ps.executeUpdate();
                    updated++;
                    System.out.println("  → Đã mã hóa: " + sv.getMaSV() + " - " + sv.getHoTen());
                }
            }
        }
        return updated;
    }

    /** Kiểm tra trực tiếp trong DB xem còn trường nào chưa có prefix ENC: */
    private static boolean needsEncryption(String maSV) throws Exception {
        String sql = "SELECT Email, SoDienThoai, SoDienThoaiPhuHuynh, QueQuan, DiaChi FROM SinhVien WHERE MaSV=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return !EncryptionUtils.isEncrypted(rs.getString("Email"))
                            || !EncryptionUtils.isEncrypted(rs.getString("SoDienThoai"))
                            || !EncryptionUtils.isEncrypted(rs.getString("SoDienThoaiPhuHuynh"))
                            || !EncryptionUtils.isEncrypted(rs.getString("QueQuan"))
                            || !EncryptionUtils.isEncrypted(rs.getString("DiaChi"));
                }
            }
        }
        return false;
    }
}