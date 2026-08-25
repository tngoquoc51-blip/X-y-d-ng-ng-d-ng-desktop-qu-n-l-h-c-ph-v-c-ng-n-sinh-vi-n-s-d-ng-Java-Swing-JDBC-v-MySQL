package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.util.EmailUtils;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Xu ly luong "Quen mat khau": sinh ma xac nhan 6 so, gui qua email (Gmail da duoc
 * Admin lien ket voi tai khoan), roi xac nhan ma nguoi dung nhap vao co dung khong.
 *
 * Ma duoc luu TAM THOI trong bo nho (khong luu CSDL) - vi day la ma "dung 1 lan",
 * chi song trong vai phut roi bi xoa, khong can ton tai lau dai. Neu app bi tat giua
 * chung thi ma cung mat theo - nguoi dung chi can bam "Gui lai ma" de xin ma moi.
 */
public class OtpService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final long THOI_HAN_MS = 5 * 60 * 1000; // Ma co hieu luc 5 phut

    /** Dung chung (static) cho toan bo ung dung - de "gui ma" va "xac nhan ma" cung
     *  nhin thay cung 1 kho ma, du duoc goi tu 2 lan tao OtpService khac nhau. */
    private static final Map<String, MaOtp> BO_NHO = new ConcurrentHashMap<>();

    private final TaiKhoanService taiKhoanService = new TaiKhoanService();

    private static class MaOtp {
        final String ma;
        final long hetHanLuc;
        MaOtp(String ma, long hetHanLuc) { this.ma = ma; this.hetHanLuc = hetHanLuc; }
    }

    /**
     * Tim tai khoan sinh vien theo Gmail da lien ket, sinh ma OTP 6 so, gui qua email.
     * Nem IllegalArgumentException voi thong bao de hieu neu khong hop le - GUI se hien
     * thong bao nay truc tiep cho nguoi dung.
     */
    public void guiMaXacNhan(String googleEmail) throws SQLException {
        if (googleEmail == null || googleEmail.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập Gmail");
        }
        String emailChuan = googleEmail.trim();
        TaiKhoan tk = taiKhoanService.timTheoGoogleEmail(emailChuan);
        if (tk == null) {
            throw new IllegalArgumentException("Gmail này chưa được Admin liên kết với tài khoản nào.\nVui lòng liên hệ Phòng Kế toán.");
        }
        if (!tk.isTrangThai()) {
            throw new IllegalArgumentException("Tài khoản này đã bị khóa. Vui lòng liên hệ Phòng Kế toán.");
        }

        String ma = taoMa6So();
        BO_NHO.put(emailChuan.toLowerCase(), new MaOtp(ma, System.currentTimeMillis() + THOI_HAN_MS));

        String noiDung = "Chào " + tk.getHoTen() + ",\n\n"
                + "Mã xác nhận lấy lại mật khẩu của bạn là: " + ma + "\n"
                + "Mã có hiệu lực trong 5 phút. Nếu bạn không yêu cầu, vui lòng bỏ qua email này.\n\n"
                + "Trân trọng,\nHệ thống Quản lý Học phí.";
        boolean guiThanhCong = EmailUtils.sendMail(emailChuan, "Mã xác nhận lấy lại mật khẩu", noiDung);
        if (!guiThanhCong) {
            BO_NHO.remove(emailChuan.toLowerCase());
            throw new IllegalArgumentException(
                    "Gửi email thất bại. Kiểm tra lại cấu hình mail.host/mail.username/mail.password\ntrong file application.properties.");
        }
    }

    /**
     * Kiem tra ma OTP nguoi dung nhap co dung va con han khong. Neu dung: TU DONG dat
     * 1 mat khau ngau nhien moi cho tai khoan + bat co "bat buoc doi mat khau", roi tra
     * ve TaiKhoan do de GUI mo tiep dialog doi mat khau chinh thuc.
     */
    public TaiKhoan xacNhanMaVaDatLaiMatKhau(String googleEmail, String maNguoiDungNhap) throws SQLException {
        if (googleEmail == null || maNguoiDungNhap == null || maNguoiDungNhap.isBlank()) {
            throw new IllegalArgumentException("Vui lòng nhập mã xác nhận");
        }
        String key = googleEmail.trim().toLowerCase();
        MaOtp luu = BO_NHO.get(key);
        if (luu == null) {
            throw new IllegalArgumentException("Bạn chưa yêu cầu mã hoặc mã đã hết hạn.\nBấm \"Gửi lại mã\" để nhận mã mới.");
        }
        if (System.currentTimeMillis() > luu.hetHanLuc) {
            BO_NHO.remove(key);
            throw new IllegalArgumentException("Mã xác nhận đã hết hạn.\nBấm \"Gửi lại mã\" để nhận mã mới.");
        }
        if (!luu.ma.equals(maNguoiDungNhap.trim())) {
            throw new IllegalArgumentException("Mã xác nhận không đúng");
        }
        BO_NHO.remove(key);

        TaiKhoan tk = taiKhoanService.timTheoGoogleEmail(googleEmail.trim());
        if (tk == null) {
            throw new IllegalArgumentException("Không tìm thấy tài khoản tương ứng");
        }
        taiKhoanService.datLaiMatKhauNgauNhienVaBatBuocDoi(tk.getMaTK());
        tk.setBatBuocDoiMatKhau(true);
        return tk;
    }

    private String taoMa6So() {
        int so = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(so);
    }
}