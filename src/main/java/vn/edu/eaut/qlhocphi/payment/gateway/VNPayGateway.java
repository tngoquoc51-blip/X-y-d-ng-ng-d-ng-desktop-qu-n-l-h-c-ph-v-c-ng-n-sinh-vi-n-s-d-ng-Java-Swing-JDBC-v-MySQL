package vn.edu.eaut.qlhocphi.payment.gateway;

import vn.edu.eaut.qlhocphi.config.AppConfig;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/** Xay dung URL thanh toan VNPay va xac thuc chu ky (secure hash) khi co ket qua tra ve. */
public class VNPayGateway {

    /** Tao URL de mo trinh duyet, nguoi dung thanh toan tren trang VNPay Sandbox. */
    public static String taoUrlThanhToan(String txnRef, BigDecimal soTien, String noiDung) {
        String tmnCode = AppConfig.get("vnpay.tmncode");
        String hashSecret = AppConfig.get("vnpay.hashsecret");
        String payUrl = AppConfig.get("vnpay.payurl");
        String returnUrl = AppConfig.get("gateway.public.url") + "/vnpay-return";

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", "2.1.0");
        params.put("vnp_Command", "pay");
        params.put("vnp_TmnCode", tmnCode);
        params.put("vnp_Amount", soTien.multiply(BigDecimal.valueOf(100)).toBigInteger().toString());
        params.put("vnp_CurrCode", "VND");
        params.put("vnp_TxnRef", txnRef);
        params.put("vnp_OrderInfo", noiDung);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", "vn");
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", "127.0.0.1");
        params.put("vnp_CreateDate", new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

        String hashData = buildQuery(params, false);
        String secureHash = hmacSHA512(hashSecret, hashData);

        String query = buildQuery(params, true) + "&vnp_SecureHash=" + secureHash;
        String finalUrl = payUrl + "?" + query;

        // DEBUG - xoa sau khi test xong
        System.out.println("=== VNPAY DEBUG ===");
        System.out.println("TmnCode: " + tmnCode);
        System.out.println("HashSecret: " + hashSecret);
        System.out.println("hashData: " + hashData);
        System.out.println("secureHash: " + secureHash);
        System.out.println("URL day du: " + finalUrl);
        System.out.println("===================");

        return finalUrl;
    }

    /** Kiem tra chu ky cua du lieu VNPay tra ve co dung khong. */
    public static boolean kiemTraChuKy(Map<String, String> params) {
        String hashSecret = AppConfig.get("vnpay.hashsecret");
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) return false;

        TreeMap<String, String> sorted = new TreeMap<>(params);
        sorted.remove("vnp_SecureHash");
        sorted.remove("vnp_SecureHashType");

        String hashData = buildQuery(sorted, false);
        String calculated = hmacSHA512(hashSecret, hashData);
        return calculated.equalsIgnoreCase(receivedHash);
    }

    public static boolean thanhCong(Map<String, String> params) {
        return "00".equals(params.get("vnp_ResponseCode"));
    }

    private static String buildQuery(Map<String, String> params, boolean encodeKey) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (e.getValue() == null || e.getValue().isEmpty()) continue;
            if (sb.length() > 0) sb.append('&');
            sb.append(encodeKey ? urlEncode(e.getKey()) : e.getKey())
                    .append('=')
                    .append(urlEncode(e.getValue()));
        }
        return sb.toString();
    }

    private static String urlEncode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    private static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            hmac512.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Loi tao chu ky VNPay", e);
        }
    }
}