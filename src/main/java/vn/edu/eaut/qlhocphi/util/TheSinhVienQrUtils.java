package vn.edu.eaut.qlhocphi.util;

import vn.edu.eaut.qlhocphi.model.SinhVien;

/**
 * Dinh dang noi dung ma QR gan tren "the sinh vien": thay vi chi ma hoa 1 minh
 * Ma SV, QR moi ma hoa DAY DU thong tin can thiet (Ma SV, Ho ten, Lop, Khoa)
 * duoi dang chuoi key=value ngan gon, de:
 *  - Quet la ra ngay thong tin day du, khong can tra CSDL truoc khi hien thi so bo.
 *  - Van hoat dong OFFLINE 100% (khong goi API, chi doc/ghi chuoi vao anh QR).
 *
 * Van TUONG THICH NGUOC voi cac QR CU chi chua 1 minh Ma SV (vi du in tay truoc
 * do) - trichMaSV() se tu nhan dien va tra ve dung Ma SV trong ca 2 truong hop.
 *
 * Dinh dang QR MOI (vi du that):
 *   MASV=20230101;HOTEN=Nguyen Van An;LOP=CNTT14.1;KHOA=Cong nghe thong tin
 */
public class TheSinhVienQrUtils {

    private static final String KEY_MASV = "MASV=";
    private static final String KEY_HOTEN = "HOTEN=";
    private static final String KEY_LOP = "LOP=";
    private static final String KEY_KHOA = "KHOA=";

    /** Sinh chuoi noi dung se duoc ma hoa thanh QR cho 1 sinh vien. */
    public static String taoNoiDungThe(SinhVien sv) {
        StringBuilder sb = new StringBuilder();
        sb.append(KEY_MASV).append(antoan(sv.getMaSV())).append(";");
        sb.append(KEY_HOTEN).append(antoan(sv.getHoTen())).append(";");
        sb.append(KEY_LOP).append(antoan(sv.getLop())).append(";");
        sb.append(KEY_KHOA).append(antoan(sv.getKhoa()));
        return sb.toString();
    }

    /** Doc lai Ma SV tu noi dung da quet duoc - ho tro CA QR moi (co cau truc)
     *  LAN QR cu (chi la 1 chuoi Ma SV thuan, khong co dau '='). */
    public static String trichMaSV(String noiDungQR) {
        if (noiDungQR == null) return null;
        String s = noiDungQR.trim();
        if (s.contains(KEY_MASV)) {
            for (String phan : s.split(";")) {
                if (phan.startsWith(KEY_MASV)) return phan.substring(KEY_MASV.length()).trim();
            }
        }
        return s.isEmpty() ? null : s; // QR cu: ca chuoi chinh la Ma SV
    }

    /** Doc Ho ten tu QR moi (co cau truc) - tra ve null neu la QR cu (khong co thong tin nay). */
    public static String trichHoTen(String noiDungQR) {
        return trichTruong(noiDungQR, KEY_HOTEN);
    }

    public static String trichLop(String noiDungQR) {
        return trichTruong(noiDungQR, KEY_LOP);
    }

    public static String trichKhoa(String noiDungQR) {
        return trichTruong(noiDungQR, KEY_KHOA);
    }

    private static String trichTruong(String noiDungQR, String key) {
        if (noiDungQR == null) return null;
        for (String phan : noiDungQR.split(";")) {
            if (phan.startsWith(key)) {
                String v = phan.substring(key.length()).trim();
                return v.isEmpty() ? null : v;
            }
        }
        return null;
    }

    /** Thay ky tu ';' va '=' trong du lieu that (vi du ten co dau '=' la khong the, nhung
     *  phong ho) de khong lam vo dinh dang key=value;key=value cua QR. */
    private static String antoan(String s) {
        if (s == null) return "";
        return s.replace(";", ",").replace("=", ":");
    }
}
