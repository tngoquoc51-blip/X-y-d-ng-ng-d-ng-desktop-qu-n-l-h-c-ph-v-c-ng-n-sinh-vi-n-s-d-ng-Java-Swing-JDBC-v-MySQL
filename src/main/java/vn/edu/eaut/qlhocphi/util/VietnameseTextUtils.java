package vn.edu.eaut.qlhocphi.util;

import java.text.Normalizer;
import java.util.regex.Pattern;

/** Chuan hoa va so khop chuoi tieng Viet - dung cho doi soat ngan hang thong minh. */
public class VietnameseTextUtils {
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    /** Bo dau, viet HOA, gom khoang trang thua. VD: "Nguyễn Văn A" -> "NGUYEN VAN A". */
    public static String boDauVietHoa(String s) {
        if (s == null) return "";
        String norm = Normalizer.normalize(s, Normalizer.Form.NFD);
        String khongDau = DIACRITICS.matcher(norm).replaceAll("");
        khongDau = khongDau.replace('Đ', 'D').replace('đ', 'd');
        return khongDau.toUpperCase().trim().replaceAll("\\s+", " ");
    }

    /** Khoang cach Levenshtein giua 2 chuoi (so ky tu can them/xoa/sua de bien a thanh b). */
    public static int levenshtein(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i <= a.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= b.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= a.length(); i++) {
            for (int j = 1; j <= b.length(); j++) {
                int chiPhi = a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + chiPhi);
            }
        }
        return dp[a.length()][b.length()];
    }

    /** % tuong dong 0-100 giua 2 chuoi (da bo dau, viet hoa) - dung Levenshtein chuan hoa theo do dai. */
    public static int doTuongDong(String a, String b) {
        String na = boDauVietHoa(a);
        String nb = boDauVietHoa(b);
        int maxLen = Math.max(na.length(), nb.length());
        if (maxLen == 0) return 100;
        int khoangCach = levenshtein(na, nb);
        return (int) Math.round((1 - (double) khoangCach / maxLen) * 100);
    }

    /** Kiem tra chuoi noi dung (da bo dau) co CHUA 1 chuoi con (VD: ma SV) hay khong. */
    public static boolean chua(String noiDungDaBoDau, String chuoiCon) {
        if (noiDungDaBoDau == null || chuoiCon == null || chuoiCon.isBlank()) return false;
        return noiDungDaBoDau.contains(boDauVietHoa(chuoiCon));
    }
    /**
     * So khop 1 chuoi NGAN (vd: ten sinh vien) co xuat hien ben TRONG 1 chuoi DAI (vd: noi
     * dung chuyen khoan day du) hay khong - khac voi doTuongDong() von so sanh nguyen ca 2
     * chuoi nen bi diem thap khi 1 ben la ca cau dai chi chua ten o giua. Ham nay "truot"
     * 1 cua so co do dai bang chuoiCanTim doc qua vanBanDai, tim vi tri khop tot nhat.
     */
    public static int doTuongDongChuaChuoi(String vanBanDai, String chuoiCanTim) {
        String vb = boDauVietHoa(vanBanDai);
        String ct = boDauVietHoa(chuoiCanTim);
        if (ct.isEmpty()) return 0;
        if (vb.length() <= ct.length()) {
            return doTuongDong(vb, ct);
        }
        int diemToiDa = 0;
        for (int i = 0; i <= vb.length() - ct.length(); i++) {
            String cuaSo = vb.substring(i, i + ct.length());
            int diem = doTuongDong(cuaSo, ct);
            if (diem > diemToiDa) diemToiDa = diem;
            if (diemToiDa == 100) break;
        }
        return diemToiDa;
    }
}