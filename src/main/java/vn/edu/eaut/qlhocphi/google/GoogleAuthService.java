package vn.edu.eaut.qlhocphi.google;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Xu ly toan bo luong "Dang nhap bang Google" (OAuth 2.0 Authorization Code, kieu
 * "loopback" danh cho ung dung desktop) MA KHONG CAN THEM BAT KY THU VIEN NGOAI NAO -
 * chi dung cac lop san co trong JDK: com.sun.net.httpserver.HttpServer (may chu HTTP
 * cuc bo tam thoi) va java.net.http.HttpClient (goi API cua Google).
 *
 * CAC BUOC THUC HIEN (khop voi chuan OAuth 2.0 "Authorization Code" cua Google):
 *  1. Mo mot cong TCP tren dia chi loopback (127.0.0.1) - Google se goi lai (redirect)
 *     ve day sau khi nguoi dung dang nhap Google va dong y cap quyen.
 *  2. Mo trinh duyet mac dinh cua may toi trang dang nhap Google (yeu cau nguoi dung
 *     tu nhap Gmail + mat khau Google that su, xac thuc 2 lop... neu co bat).
 *  3. Google chuyen huong trinh duyet ve lai dia chi loopback o buoc 1, kem theo 1
 *     "authorization code" DUNG DUOC DUY NHAT 1 LAN trong URL (day chinh la "ma dung
 *     1 lan" ma Google cap - ban chat cua chuan OAuth Authorization Code).
 *  4. Ung dung doi "authorization code" do lay 1 access token bang 1 request POST rieng
 *     (khong qua trinh duyet), roi dung access token goi API "userinfo" cua Google de
 *     lay ra dia chi Gmail THAT SU va DA DUOC GOOGLE XAC THUC cua nguoi vua dang nhap.
 *
 * Ket qua tra ve chi la 1 chuoi Gmail da xac thuc - lop nghiep vu ben tren
 * (TaiKhoanService.timTheoGoogleEmail) se doi chieu Gmail nay voi cot GoogleEmail
 * trong bang TaiKhoan de biet Gmail nay thuoc sinh vien nao.
 */
public class GoogleAuthService {

    private static final String AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_ENDPOINT = "https://www.googleapis.com/oauth2/v3/userinfo";

    /** Ket qua xac thuc Google thanh cong: Gmail + ten hien thi (lay tu ho so Google). */
    public static class KetQuaGoogle {
        public final String email;
        public final String hoTen;
        public KetQuaGoogle(String email, String hoTen) { this.email = email; this.hoTen = hoTen; }
    }

    /**
     * Mo trinh duyet cho nguoi dung dang nhap Google, cho toi khi co ket qua (hoac het
     * thoi gian cho 120 giay, hoac nguoi dung dong tab/tu choi cap quyen).
     * PHAI goi trong luong nen (SwingWorker.doInBackground), KHONG goi tren Event
     * Dispatch Thread vi ham nay block cho toi khi xong.
     */
    public KetQuaGoogle dangNhap() throws IOException, InterruptedException, TimeoutException {
        if (!GoogleOAuthConfig.daCauHinh()) {
            throw new IllegalStateException(
                    "Chua cau hinh Google OAuth. Mo file GoogleOAuthConfig.java va dien CLIENT_ID/CLIENT_SECRET theo huong dan trong file do.");
        }

        CompletableFuture<String> maDungMotLan = new CompletableFuture<>();

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        int cong = server.getAddress().getPort();
        String redirectUri = "http://127.0.0.1:" + cong + "/oauth2callback";

        server.createContext("/oauth2callback", exchange -> xuLyCallback(exchange, maDungMotLan));
        server.setExecutor(null);
        server.start();

        try {
            String urlDangNhap = AUTH_ENDPOINT
                    + "?client_id=" + urlEncode(GoogleOAuthConfig.CLIENT_ID)
                    + "&redirect_uri=" + urlEncode(redirectUri)
                    + "&response_type=code"
                    + "&scope=" + urlEncode(GoogleOAuthConfig.SCOPE)
                    + "&prompt=select_account";

            if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                throw new IOException("May nay khong ho tro tu mo trinh duyet. Vui long copy URL va mo thu cong:\n" + urlDangNhap);
            }
            Desktop.getDesktop().browse(URI.create(urlDangNhap));

            String maXacThuc;
            try {
                maXacThuc = maDungMotLan.get(120, TimeUnit.SECONDS);
            } catch (java.util.concurrent.ExecutionException ex) {
                throw new IOException(ex.getCause() != null ? ex.getCause().getMessage() : "Dang nhap Google that bai");
            }

            String accessToken = doiMaLayAccessToken(maXacThuc, redirectUri);
            return layThongTinNguoiDung(accessToken);
        } finally {
            server.stop(0);
        }
    }

    /** Xu ly request Google goi lai ve loopback server, tach "code" tu query string. */
    private void xuLyCallback(HttpExchange exchange, CompletableFuture<String> ketQua) throws IOException {
        try {
            String query = exchange.getRequestURI().getQuery();
            String ma = layThamSo(query, "code");
            String loi = layThamSo(query, "error");

            String htmlPhanHoi;
            if (ma != null) {
                htmlPhanHoi = "<html><body style='font-family:sans-serif;text-align:center;padding-top:80px'>"
                        + "<h2 style='color:#1E3A8A'>Da xac thuc thanh cong voi Google</h2>"
                        + "<p>Ban co the dong tab nay va quay lai ung dung.</p></body></html>";
            } else {
                htmlPhanHoi = "<html><body style='font-family:sans-serif;text-align:center;padding-top:80px'>"
                        + "<h2 style='color:#B91C1C'>Dang nhap Google khong thanh cong</h2>"
                        + "<p>Ban co the dong tab nay va thu lai trong ung dung.</p></body></html>";
            }

            byte[] bytes = htmlPhanHoi.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }

            if (ma != null) {
                ketQua.complete(ma);
            } else {
                ketQua.completeExceptionally(new IOException(
                        loi != null ? "Google tu choi: " + loi : "Khong nhan duoc ma xac thuc tu Google"));
            }
        } catch (Exception ex) {
            ketQua.completeExceptionally(ex);
        }
    }

    /** Doi "authorization code" (dung 1 lan) lay access token - request nay KHONG qua trinh duyet. */
    private String doiMaLayAccessToken(String maXacThuc, String redirectUri) throws IOException, InterruptedException {
        String body = "code=" + urlEncode(maXacThuc)
                + "&client_id=" + urlEncode(GoogleOAuthConfig.CLIENT_ID)
                + "&client_secret=" + urlEncode(GoogleOAuthConfig.CLIENT_SECRET)
                + "&redirect_uri=" + urlEncode(redirectUri)
                + "&grant_type=authorization_code";

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TOKEN_ENDPOINT))
                .timeout(Duration.ofSeconds(15))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Google tu choi cap access token (HTTP " + response.statusCode() + "): " + response.body());
        }
        String accessToken = layGiaTriJson(response.body(), "access_token");
        if (accessToken == null) {
            throw new IOException("Khong doc duoc access_token tu phan hoi cua Google");
        }
        return accessToken;
    }

    /** Dung access token de goi API lay ho so nguoi dung (Gmail + ten) da duoc Google xac thuc. */
    private KetQuaGoogle layThongTinNguoiDung(String accessToken) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(USERINFO_ENDPOINT))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("Khong lay duoc thong tin tai khoan Google (HTTP " + response.statusCode() + ")");
        }
        String email = layGiaTriJson(response.body(), "email");
        String ten = layGiaTriJson(response.body(), "name");
        if (email == null) {
            throw new IOException("Tai khoan Google nay khong co Gmail cong khai");
        }
        return new KetQuaGoogle(email, ten != null ? ten : email);
    }

    // ================== Cac ham tien ich nho (khong can thu vien ngoai) ==================

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String layThamSo(String query, String ten) {
        if (query == null) return null;
        for (String cap : query.split("&")) {
            int idx = cap.indexOf('=');
            if (idx < 0) continue;
            String key = cap.substring(0, idx);
            if (key.equals(ten)) {
                return java.net.URLDecoder.decode(cap.substring(idx + 1), StandardCharsets.UTF_8);
            }
        }
        return null;
    }

    /**
     * Doc 1 gia tri chuoi tu JSON phang (khong long nhau) tra ve boi Google, vi du:
     * {"access_token":"ya29...","expires_in":3599,"email":"a@gmail.com"}
     * Du dung cho cau truc JSON on dinh cua Google userinfo/token endpoint - khong
     * phai 1 bo parse JSON day du, nhung du chinh xac va khong can them thu vien ngoai.
     */
    private static String layGiaTriJson(String json, String key) {
        if (json == null) return null;
        Pattern p = Pattern.compile("\"" + Pattern.quote(key) + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1).replace("\\/", "/").replace("\\\"", "\"");
        }
        return null;
    }
}