package vn.edu.eaut.qlhocphi.util;

import vn.edu.eaut.qlhocphi.config.AppConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Gui SMS qua cong eSMS.vn (nha cung cap SMS pho bien tai VN, co API REST don gian).
 * Neu sms.mode=MOCK (mac dinh), chi in ra console thay vi goi API that - dung de demo
 * khong can tai khoan SMS that. Doi sms.mode=THAT + dien du sms.esms.* trong
 * application.properties khi co tai khoan that de gui SMS thuc su.
 */
public class SmsUtils {
    private static final String API_URL = "http://rest.esms.vn/MainService.svc/json/SendMultipleMessage_V4_get";

    /** Gui 1 tin SMS toi so dien thoai. Tra ve true neu (mo phong hoac) gui thanh cong. */
    public static boolean guiSms(String soDienThoai, String noiDung) {
        String mode = AppConfig.get("sms.mode", "MOCK");

        if ("MOCK".equalsIgnoreCase(mode)) {
            System.out.println("=== [MOCK SMS] Gui toi " + soDienThoai + " ===");
            System.out.println(noiDung);
            System.out.println("=== (Che do mo phong - chua goi API that, doi sms.mode=THAT de gui that) ===");
            return true;
        }

        try {
            String apiKey = AppConfig.get("sms.esms.apikey");
            String secretKey = AppConfig.get("sms.esms.secretkey");
            String brandname = AppConfig.get("sms.esms.brandname", "EAUT");

            String url = API_URL
                    + "?Phone=" + URLEncoder.encode(soDienThoai, StandardCharsets.UTF_8)
                    + "&Content=" + URLEncoder.encode(noiDung, StandardCharsets.UTF_8)
                    + "&ApiKey=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
                    + "&SecretKey=" + URLEncoder.encode(secretKey, StandardCharsets.UTF_8)
                    + "&Brandname=" + URLEncoder.encode(brandname, StandardCharsets.UTF_8)
                    + "&SmsType=2";

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // eSMS tra ve JSON co truong "CodeResult" = "100" neu thanh cong
            return response.statusCode() == 200 && response.body().contains("\"CodeResult\":\"100\"");
        } catch (Exception ex) {
            System.err.println("Gui SMS that bai: " + ex.getMessage());
            return false;
        }
    }

    /** Gui SMS nhac no cho phu huynh - noi dung ngan gon (SMS gioi han ky tu, khong dai dong nhu email). */
    public static boolean guiSmsNhacNoPhuHuynh(String soDienThoai, String hoTenSV, String maSV,
                                               String soTienConNo, int soNgayQuaHan) {
        String noiDung = "[EAUT] Con em " + hoTenSV + " (MaSV " + maSV + ") con no hoc phi "
                + soTienConNo + "d, da qua han " + soNgayQuaHan + " ngay. "
                + "Kinh mong Quy PH nhac nho hoan tat hoc phi. LH Phong Ke toan.";
        return guiSms(soDienThoai, noiDung);
    }
}