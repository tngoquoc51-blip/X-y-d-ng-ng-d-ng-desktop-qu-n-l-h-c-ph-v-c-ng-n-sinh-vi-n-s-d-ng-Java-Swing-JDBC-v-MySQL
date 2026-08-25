package vn.edu.eaut.qlhocphi.payment.gateway;

import vn.edu.eaut.qlhocphi.config.AppConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Goi API tao giao dich MoMo (test environment) va xac thuc IPN tra ve.
 * LUU Y: thu tu truong trong chu ky (signature) cua MoMo co the thay doi giua
 * cac phien ban API - doi chieu lai voi tai lieu MoMo neu ban thay bao loi chu ky.
 */
public class MoMoGateway {

    public static String taoUrlThanhToan(String orderId, BigDecimal soTien, String noiDung) throws Exception {
        String partnerCode = AppConfig.get("momo.partnercode");
        String accessKey = AppConfig.get("momo.accesskey");
        String secretKey = AppConfig.get("momo.secretkey");
        String payUrlApi = AppConfig.get("momo.payurl");
        String publicUrl = AppConfig.get("gateway.public.url");

        String requestId = UUID.randomUUID().toString();
        String amount = soTien.toBigInteger().toString();
        String orderInfo = noiDung;
        String redirectUrl = publicUrl + "/momo-return";
        String ipnUrl = publicUrl + "/momo-return";
        String requestType = "payWithATM";
        String extraData = "";

        String rawSignature = "accessKey=" + accessKey
                + "&amount=" + amount
                + "&extraData=" + extraData
                + "&ipnUrl=" + ipnUrl
                + "&orderId=" + orderId
                + "&orderInfo=" + orderInfo
                + "&partnerCode=" + partnerCode
                + "&redirectUrl=" + redirectUrl
                + "&requestId=" + requestId
                + "&requestType=" + requestType;
        String signature = hmacSHA256(secretKey, rawSignature);

        String json = "{"
                + "\"partnerCode\":\"" + partnerCode + "\","
                + "\"partnerName\":\"QLHocPhi\","
                + "\"storeId\":\"QLHocPhiStore\","
                + "\"requestId\":\"" + requestId + "\","
                + "\"amount\":\"" + amount + "\","
                + "\"orderId\":\"" + orderId + "\","
                + "\"orderInfo\":\"" + orderInfo + "\","
                + "\"redirectUrl\":\"" + redirectUrl + "\","
                + "\"ipnUrl\":\"" + ipnUrl + "\","
                + "\"lang\":\"vi\","
                + "\"extraData\":\"" + extraData + "\","
                + "\"requestType\":\"" + requestType + "\","
                + "\"signature\":\"" + signature + "\""
                + "}";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(payUrlApi))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        String body = response.body();
        String payUrl = layGiaTriJson(body, "payUrl");
        if (payUrl == null) {
            throw new RuntimeException("MoMo khong tra ve payUrl. Phan hoi: " + body);
        }
        return payUrl;
    }

    public static boolean thanhCong(Map<String, String> params) {
        return "0".equals(params.get("resultCode"));
    }

    private static String layGiaTriJson(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : null;
    }

    private static String hmacSHA256(String key, String data) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] bytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Loi tao chu ky MoMo", e);
        }
    }
}