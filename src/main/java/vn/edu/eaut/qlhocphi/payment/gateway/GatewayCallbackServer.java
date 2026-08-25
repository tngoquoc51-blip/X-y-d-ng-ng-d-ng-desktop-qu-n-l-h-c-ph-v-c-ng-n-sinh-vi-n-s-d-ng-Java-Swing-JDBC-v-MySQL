package vn.edu.eaut.qlhocphi.payment.gateway;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import vn.edu.eaut.qlhocphi.config.AppConfig;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Mot HttpServer nhung (JDK co san, khong can thu vien ngoai) dung chung cho
 * callback cua VNPay va MoMo. Chi 1 giao dich duoc cho phep dang cho tai 1 thoi diem
 * (du cho demo do an). Ngrok tunnel (vd: ngrok http 80) se dua request tu internet
 * ve dung server nay.
 */
public class GatewayCallbackServer {
    private static HttpServer server;
    private static Consumer<Map<String, String>> vnpayListener;
    private static Consumer<Map<String, String>> momoListener;

    public static synchronized void start() throws IOException {
        if (server != null) return;
        int port = Integer.parseInt(AppConfig.get("gateway.callback.port", "80"));
        server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/vnpay-return", exchange -> xuLy(exchange, () -> vnpayListener));
        server.createContext("/momo-return", exchange -> xuLy(exchange, () -> momoListener));

        server.setExecutor(null);
        server.start();
    }

    public static synchronized void stop() {
        if (server != null) {
            server.stop(0);
            server = null;
        }
    }

    public static void dangKyVNPay(Consumer<Map<String, String>> listener) {
        vnpayListener = listener;
    }

    public static void dangKyMoMo(Consumer<Map<String, String>> listener) {
        momoListener = listener;
    }

    private interface ListenerSupplier {
        Consumer<Map<String, String>> get();
    }

    private static void xuLy(HttpExchange exchange, ListenerSupplier supplier) throws IOException {
        Map<String, String> params = parseQuery(exchange.getRequestURI().getRawQuery());

        String html = "<html><body style='font-family:sans-serif;text-align:center;padding-top:80px'>"
                + "<h2>Da nhan ket qua thanh toan</h2>"
                + "<p>Ban co the dong tab nay va quay lai ung dung.</p></body></html>";
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }

        Consumer<Map<String, String>> listener = supplier.get();
        if (listener != null && !params.isEmpty()) {
            // Bo qua cac request khong co tham so huu ich (vd: IPN goi bang POST voi
            // du lieu nam trong JSON body ma ham nay chua doc - neu coi map rong la
            // "that bai" se bao sai ket qua truoc khi request GET (redirect trinh
            // duyet, co day du resultCode) kip toi.
            listener.accept(params);
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> map = new HashMap<>();
        if (query == null || query.isBlank()) return map;
        for (String cap : query.split("&")) {
            int idx = cap.indexOf('=');
            if (idx < 0) continue;
            String key = URLDecoder.decode(cap.substring(0, idx), StandardCharsets.UTF_8);
            String value = URLDecoder.decode(cap.substring(idx + 1), StandardCharsets.UTF_8);
            map.put(key, value);
        }
        return map;
    }
}