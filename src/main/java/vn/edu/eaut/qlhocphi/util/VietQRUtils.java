package vn.edu.eaut.qlhocphi.util;

import vn.edu.eaut.qlhocphi.config.AppConfig;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Sinh mã QR chuyển khoản chuẩn VietQR (chuẩn 100% cua NAPAS, quet duoc bang
 * BAT KY app ngan hang nao tai VN - Vietcombank, MB, Techcombank, ACB, BIDV...).
 * Dung dich vu anh mien phi cua img.vietqr.io (KHONG can dang ky, KHONG can API
 * key) - chi can truyen So BIN ngan hang + So tai khoan + So tien + Noi dung la
 * co ngay anh QR PNG tra ve truc tiep tu URL.
 *
 * Luu y quan trong: day la QR "tinh" (static link) - KHONG co webhook bao ve tu
 * dong khi khach chuyen tien xong (nhu VNPay/MoMo tra phi). Vi vay ke toan van
 * can bam "Xac nhan da nhan tien" thu cong sau khi kiem tra sao ke ngan hang
 * that. Phu hop demo do an / quy mo nho, mien phi hoan toan.
 *
 * Cau hinh trong application.properties:
 *   vietqr.bank.bin=970422          (ma BIN ngan hang, VD MB=970422, VCB=970436)
 *   vietqr.bank.name=MB Bank
 *   vietqr.account.no=0123456789
 *   vietqr.account.name=TRUONG DAI HOC CONG NGHE DONG A
 *   vietqr.template=compact2        (compact2 | compact | qr_only | print)
 */
public class VietQRUtils {
    private static final String IMG_BASE = "https://img.vietqr.io/image/";

    /** Noi dung chuyen khoan goi y - de ke toan doi soat de dang: "HP <MaSV> HD<MaHoaDon>". */
    public static String taoNoiDungChuyenKhoan(String maSV, int maHoaDon) {
        return "HP " + maSV + " HD" + maHoaDon;
    }

    /** URL anh QR PNG (server VietQR ve san, chi can hien thi bang JLabel/ImageIO). */
    public static String taoUrlAnhQR(BigDecimal soTien, String noiDungChuyenKhoan) {
        String bin = AppConfig.get("vietqr.bank.bin");
        String soTk = AppConfig.get("vietqr.account.no");
        String tenTk = AppConfig.get("vietqr.account.name");
        String template = AppConfig.get("vietqr.template", "compact2");

        long soTienLong = soTien != null ? soTien.longValue() : 0L;

        return IMG_BASE + bin + "-" + soTk + "-" + template + ".png"
                + "?amount=" + soTienLong
                + "&addInfo=" + urlEncode(noiDungChuyenKhoan)
                + "&accountName=" + urlEncode(tenTk);
    }

    public static boolean daCauHinh() {
        return !AppConfig.get("vietqr.bank.bin").isBlank() && !AppConfig.get("vietqr.account.no").isBlank();
    }

    public static String tenNganHang() { return AppConfig.get("vietqr.bank.name"); }
    public static String soTaiKhoan() { return AppConfig.get("vietqr.account.no"); }
    public static String tenChuTaiKhoan() { return AppConfig.get("vietqr.account.name"); }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8);
    }
}
