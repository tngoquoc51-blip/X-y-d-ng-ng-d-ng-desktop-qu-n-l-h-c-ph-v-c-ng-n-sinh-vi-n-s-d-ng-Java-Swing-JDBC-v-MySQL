package vn.edu.eaut.qlhocphi.ai;

import vn.edu.eaut.qlhocphi.config.AppConfig;
import vn.edu.eaut.qlhocphi.dal.HoaDonDAO;
import vn.edu.eaut.qlhocphi.model.HoaDonHocPhi;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dich vu Chatbot ho tro tra loi cau hoi ve hoc phi/cong no.
 *
 * Hoat dong theo 2 tang:
 * 1) RULE-BASED (luon chay, khong can API key): nhan dien mau cau hoi thuong gap
 *    (vi du "cong no cua SV001") va tra loi truc tiep tu du lieu that trong CSDL.
 * 2) AI THAT (tuy chon): neu cau hinh ai.api.key trong application.properties,
 *    cau hoi ngoai pham vi rule-based se duoc chuyen tiep toi API AI (vi du Anthropic)
 *    de tra loi tu nhien hon.
 *
 * Luu y: goi mang o day nen duoc thuc hien trong SwingWorker o tang GUI de khong treo giao dien.
 */
public class ChatbotService {

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private static final Pattern PATTERN_MA_SV = Pattern.compile("SV\\d{3,}", Pattern.CASE_INSENSITIVE);

    public String traLoi(String cauHoi) {
        String cauHoiLower = cauHoi.toLowerCase().trim();

        try {
            // ----- Kich ban 1: hoi cong no theo ma SV -----
            Matcher m = PATTERN_MA_SV.matcher(cauHoi.toUpperCase());
            if (m.find() && (cauHoiLower.contains("cong no") || cauHoiLower.contains("hoc phi") || cauHoiLower.contains("con no"))) {
                return traLoiCongNo(m.group());
            }

            // ----- Kich ban 2: chao hoi -----
            if (cauHoiLower.contains("xin chao") || cauHoiLower.equals("hi") || cauHoiLower.equals("hello")) {
                return "Xin chao! Toi la tro ly ao cua he thong quan ly hoc phi. "
                        + "Ban co the hoi toi ve cong no, hoc phi, hoac cach thanh toan. "
                        + "Vi du: \"Cong no cua SV001 con bao nhieu?\"";
            }

            // ----- Kich ban 3: huong dan thanh toan -----
            if (cauHoiLower.contains("thanh toan") && !m.find()) {
                return "Ban co the thanh toan hoc phi bang 3 cach: (1) Nop tien mat truc tiep tai phong ke toan, "
                        + "(2) Chuyen khoan ngan hang theo thong tin tren hoa don, "
                        + "(3) Thanh toan online ngay trong ung dung qua muc \"Thanh toan online\".";
            }

            // ----- Ngoai pham vi rule-based: goi AI that neu co cau hinh -----
            String apiKey = AppConfig.get("ai.api.key");
            if (apiKey != null && !apiKey.isBlank()) {
                return goiAIThat(cauHoi);
            }

            return "Toi chua chac chan cau tra loi cho cau hoi nay. "
                    + "Ban thu hoi cu the hon, vi du kem ma sinh vien (VD: \"Cong no cua SV001\"), "
                    + "hoac lien he phong ke toan de duoc ho tro them.";

        } catch (SQLException e) {
            return "Xin loi, he thong dang gap su co khi truy van du lieu. Vui long thu lai sau.";
        } catch (Exception e) {
            return "Xin loi, toi khong the tra loi cau hoi nay luc nay. Chi tiet loi: " + e.getMessage();
        }
    }

    private String traLoiCongNo(String maSV) throws SQLException {
        List<HoaDonHocPhi> hoaDons = hoaDonDAO.layTheoSinhVien(maSV);
        if (hoaDons.isEmpty()) {
            return "Khong tim thay hoa don hoc phi nao cho sinh vien " + maSV + ".";
        }
        BigDecimal tongNo = BigDecimal.ZERO;
        for (HoaDonHocPhi hd : hoaDons) {
            tongNo = tongNo.add(hd.tinhConNo());
        }
        if (tongNo.compareTo(BigDecimal.ZERO) <= 0) {
            return "Sinh vien " + maSV + " hien khong con no hoc phi. Cam on ban da theo doi!";
        }
        return "Sinh vien " + maSV + " hien con no tong cong " + tongNo.toPlainString()
                + " d, tren " + hoaDons.size() + " hoa don. Vui long thanh toan som de tranh bi tinh la phi qua han.";
    }

    /** Goi API AI that (vi du Anthropic Messages API) khi da cau hinh API key. */
    private String goiAIThat(String cauHoi) {
        try {
            String apiKey = AppConfig.get("ai.api.key");
            String apiUrl = AppConfig.get("ai.api.url");
            String model = AppConfig.get("ai.model");

            String noiDungThoat = cauHoi.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n");
            String jsonBody = "{"
                    + "\"model\":\"" + model + "\","
                    + "\"max_tokens\":300,"
                    + "\"messages\":[{\"role\":\"user\",\"content\":\"" + noiDungThoat + "\"}]"
                    + "}";

            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                return trichXuatNoiDung(response.body());
            }
            return "Khong the ket noi toi dich vu AI luc nay (ma loi " + response.statusCode() + ").";
        } catch (Exception e) {
            return "Khong the ket noi toi dich vu AI luc nay. Vui long thu lai sau.";
        }
    }

    /** Trich xuat text tra loi tho tu JSON cua Anthropic Messages API (khong dung thu vien JSON ngoai). */
    private String trichXuatNoiDung(String json) {
        String marker = "\"text\":\"";
        int start = json.indexOf(marker);
        if (start < 0) return "Khong doc duoc phan hoi tu AI.";
        start += marker.length();
        int end = json.indexOf("\"", start);
        while (end > 0 && json.charAt(end - 1) == '\\') {
            end = json.indexOf("\"", end + 1);
        }
        if (end < 0) return "Khong doc duoc phan hoi tu AI.";
        return json.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"");
    }
}
