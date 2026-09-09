package vn.edu.eaut.qlhocphi.ai;

import vn.edu.eaut.qlhocphi.config.AppConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Client HTTP dung chung de goi Google Gemini API (MIEN PHI - khong can the tin
 * dung, lay key tai https://aistudio.google.com/app/apikey). Dung cho ChatbotService
 * (tra loi cau hoi tu do + doc anh) va FraudDetectionService (danh gia rui ro thanh toan).
 * Khong dung thu vien JSON ngoai - JDK 11+ da co san java.net.http.HttpClient.
 */
public class ChatbotClient {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /** Tra ve true neu da cau hinh ai.api.key trong application.properties. */
    public static boolean daCauHinh() {
        String key = AppConfig.get("ai.api.key");
        return key != null && !key.isBlank();
    }

    /** Goi Gemini voi 1 prompt nguoi dung don gian (khong co system prompt). */
    public static String goi(String noiDung) throws Exception {
        return goi(null, noiDung);
    }

    /** Goi Gemini voi system prompt (dinh huong vai tro) + noi dung cau hoi cua nguoi dung. */
    public static String goi(String systemPrompt, String noiDung) throws Exception {
        StringBuilder jsonBody = new StringBuilder();
        jsonBody.append("{");
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            jsonBody.append("\"system_instruction\":{\"parts\":[{\"text\":\"")
                    .append(escape(systemPrompt)).append("\"}]},");
        }
        jsonBody.append("\"contents\":[{\"role\":\"user\",\"parts\":[{\"text\":\"")
                .append(escape(noiDung)).append("\"}]}],")
                .append("\"generationConfig\":{\"maxOutputTokens\":700}")
                .append("}");

        return guiVaDocKetQua(jsonBody.toString());
    }

    /**
     * Goi Gemini kem theo 1 anh dinh kem (Vision) - Gemini se "nhin" duoc noi dung anh.
     * loaiAnh la media type chuan, VD "image/png", "image/jpeg".
     */
    public static String goiVoiAnh(String systemPrompt, String noiDung, String base64Anh, String loaiAnh) throws Exception {
        StringBuilder jsonBody = new StringBuilder();
        jsonBody.append("{");
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            jsonBody.append("\"system_instruction\":{\"parts\":[{\"text\":\"")
                    .append(escape(systemPrompt)).append("\"}]},");
        }
        jsonBody.append("\"contents\":[{\"role\":\"user\",\"parts\":[")
                .append("{\"text\":\"").append(escape(noiDung)).append("\"},")
                .append("{\"inline_data\":{\"mime_type\":\"").append(loaiAnh)
                .append("\",\"data\":\"").append(base64Anh).append("\"}}")
                .append("]}],")
                .append("\"generationConfig\":{\"maxOutputTokens\":900}")
                .append("}");

        return guiVaDocKetQua(jsonBody.toString());
    }

    private static String guiVaDocKetQua(String jsonBody) throws Exception {
        if (!daCauHinh()) {
            throw new IllegalStateException("Chua cau hinh ai.api.key trong application.properties. "
                    + "Lay key mien phi tai https://aistudio.google.com/app/apikey");
        }
        String apiKey = AppConfig.get("ai.api.key");
        String urlGoc = AppConfig.get("ai.api.url");
        String model = AppConfig.get("ai.model");
        String url = urlGoc + "/" + model + ":generateContent";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API tra ve ma loi " + response.statusCode() + ": " + response.body());
        }
        return trichXuatText(response.body());
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }

    /** Trich xuat noi dung text tra loi tho tu JSON cua Gemini API (khong dung thu vien JSON ngoai). */
    private static String trichXuatText(String json) {
        String marker = "\"text\": \"";
        int start = json.indexOf(marker);
        if (start < 0) {
            marker = "\"text\":\"";
            start = json.indexOf(marker);
        }
        if (start < 0) return "";
        start += marker.length();
        int end = json.indexOf("\"", start);
        while (end > 0 && json.charAt(end - 1) == '\\') {
            end = json.indexOf("\"", end + 1);
        }
        if (end < 0) return "";
        return json.substring(start, end).replace("\\n", "\n").replace("\\\"", "\"");
    }
}