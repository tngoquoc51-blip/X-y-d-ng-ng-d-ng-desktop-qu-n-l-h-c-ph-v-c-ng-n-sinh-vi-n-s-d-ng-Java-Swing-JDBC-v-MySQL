package vn.edu.eaut.qlhocphi.ai;

import vn.edu.eaut.qlhocphi.config.AppConfig;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Client HTTP dung chung de goi API AI (vi du Anthropic Messages API).
 * Dung cho ca ChatbotService (tra loi cau hoi) va FraudDetectionService (danh gia rui ro thanh toan).
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

    /** Goi API AI voi 1 prompt nguoi dung don gian (khong co system prompt). */
    public static String goi(String noiDung) throws Exception {
        return goi(null, noiDung);
    }

    /** Goi API AI voi system prompt (dinh huong vai tro cho AI) + noi dung cau hoi cua nguoi dung. */
    public static String goi(String systemPrompt, String noiDung) throws Exception {
        if (!daCauHinh()) {
            throw new IllegalStateException("Chua cau hinh ai.api.key trong application.properties");
        }
        String apiKey = AppConfig.get("ai.api.key");
        String apiUrl = AppConfig.get("ai.api.url");
        String model = AppConfig.get("ai.model");

        StringBuilder jsonBody = new StringBuilder();
        jsonBody.append("{")
                .append("\"model\":\"").append(model).append("\",")
                .append("\"max_tokens\":500,");
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            jsonBody.append("\"system\":\"").append(escape(systemPrompt)).append("\",");
        }
        jsonBody.append("\"messages\":[{\"role\":\"user\",\"content\":\"").append(escape(noiDung)).append("\"}]")
                .append("}");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .header("anthropic-version", "2023-06-01")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody.toString()))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("AI API tra ve ma loi " + response.statusCode() + ": " + response.body());
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

    /** Trich xuat noi dung text tra loi tho tu JSON cua Anthropic Messages API (khong dung thu vien JSON ngoai). */
    private static String trichXuatText(String json) {
        String marker = "\"text\":\"";
        int start = json.indexOf(marker);
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