package vn.edu.eaut.qlhocphi.google;

/**
 * Thong tin ket noi toi Google OAuth 2.0 - da dien san CLIENT_ID/CLIENT_SECRET
 * tu Google Cloud Console (project "Du an dau tien cua toi").
 *
 * Neu can tao lai (vi du bi lo Client secret), xem lai huong dan:
 * 1. https://console.cloud.google.com/ -> chon dung project.
 * 2. "Nen tang xac thuc" -> "Khach hang" -> bam vao ten client "QLHocPhi Desktop"
 *    de xem lai Client ID, hoac tao Client moi (Loai ung dung = "Ung dung may
 *    tinh de ban" / Desktop app).
 * 3. Nho vao "Khan gia" -> "Nguoi dung thu nghiem" -> them Gmail se dung de
 *    dang nhap thu (chi Gmail co trong danh sach nay moi dang nhap duoc, vi
 *    app dang o che do "Thu nghiem").
 */
public class GoogleOAuthConfig {

    /** Client ID - lay tu Google Cloud Console, project "Du an dau tien cua toi". */
    public static final String CLIENT_ID =
            "1040848740515-5732ls6sial0s3rno3l93c90i4d2p7er.apps.googleusercontent.com";

    /** Client secret - lay tu Google Cloud Console, project "Du an dau tien cua toi". */
    public static final String CLIENT_SECRET = "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";

    /** Cac quyen (scope) can xin - chi can biet Gmail va ten, khong dong bo du lieu gi khac. */
    public static final String SCOPE = "openid email profile";

    public static boolean daCauHinh() {
        return CLIENT_ID != null && !CLIENT_ID.isBlank()
                && !CLIENT_ID.startsWith("DIEN_")
                && CLIENT_SECRET != null && !CLIENT_SECRET.isBlank()
                && !CLIENT_SECRET.startsWith("DIEN_");
    }

    private GoogleOAuthConfig() {}
}