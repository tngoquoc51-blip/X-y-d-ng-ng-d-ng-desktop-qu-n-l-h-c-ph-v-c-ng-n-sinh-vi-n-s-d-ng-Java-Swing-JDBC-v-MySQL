package vn.edu.eaut.qlhocphi.bus;

import vn.edu.eaut.qlhocphi.dal.TaiKhoanDAO;
import vn.edu.eaut.qlhocphi.dal.TheQRDangNhapDAO;
import vn.edu.eaut.qlhocphi.model.TaiKhoan;
import vn.edu.eaut.qlhocphi.model.TheQRDangNhap;

import java.security.SecureRandom;
import java.sql.SQLException;
import java.util.Base64;

/** "Doi thi dang nhap bang the QR" cho sinh vien - KHONG lien quan gi den mat
 *  khau: sinh 1 token ngau nhien rieng, luu vao bang TheQRDangNhap, ma hoa
 *  thanh QR de sinh vien quet dang nhap thang, khong can go mat khau. */
public class DangNhapQRService {
    private static final String TIEN_TO_QR = "SVLOGIN:";
    private final TheQRDangNhapDAO theQrDAO = new TheQRDangNhapDAO();
    private final TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();
    private final SecureRandom random = new SecureRandom();

    /** Tao the QR moi cho 1 tai khoan - tu dong thu hoi cac the cu con hieu luc truoc do.
     *  Tra ve chuoi noi dung se duoc ma hoa thanh anh QR. */
    public String taoTheMoi(int maTK) throws SQLException {
        theQrDAO.thuHoiTatCa(maTK);
        byte[] bytesNgauNhien = new byte[32];
        random.nextBytes(bytesNgauNhien);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytesNgauNhien);
        theQrDAO.taoMoi(maTK, token);
        return TIEN_TO_QR + token;
    }

    public void thuHoiThe(int maTK) throws SQLException {
        theQrDAO.thuHoiTatCa(maTK);
    }

    /** Xac thuc noi dung doc duoc tu QR -> tra ve TaiKhoan neu hop le, null neu khong. */
    public TaiKhoan xacThucNoiDungQR(String noiDungQR) throws SQLException {
        if (noiDungQR == null || !noiDungQR.startsWith(TIEN_TO_QR)) return null;
        String token = noiDungQR.substring(TIEN_TO_QR.length());
        TheQRDangNhap the = theQrDAO.timTheoToken(token);
        if (the == null) return null;
        TaiKhoan tk = taiKhoanDAO.timTheoMa(the.getMaTK());
        if (tk == null || !tk.isTrangThai()) return null;
        return tk;
    }
}