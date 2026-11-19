package jobth.web;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ตัวช่วยเว็บ
final class Http {

    // ขนาดคำขอสูงสุด
    static final int MAX_BODY_BYTES = 16 * 1024;

    // error พร้อมรหัส HTTP
    static final class HttpError extends RuntimeException {
        final int status;

        HttpError(int status, String message) {
            super(message);
            this.status = status;
        }
    }

    private Http() {
    }

    // อ่านคำขอ
    static Map<String, String> parseForm(String raw) {
        Map<String, String> out = new HashMap<>();
        if (raw == null || raw.isEmpty()) {
            return out;
        }
        for (String pair : raw.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int eq = pair.indexOf('=');
            String key = eq < 0 ? pair : pair.substring(0, eq);
            String value = eq < 0 ? "" : pair.substring(eq + 1);
            try {
                out.put(URLDecoder.decode(key, StandardCharsets.UTF_8), URLDecoder.decode(value, StandardCharsets.UTF_8));
            } catch (IllegalArgumentException e) {
                throw new HttpError(400, "Badly encoded parameter");
            }
        }
        return out;
    }

    static Map<String, String> query(HttpExchange ex) {
        return parseForm(ex.getRequestURI().getRawQuery());
    }

    static Map<String, String> formBody(HttpExchange ex) throws IOException {
        try (InputStream in = ex.getRequestBody()) {
            byte[] bytes = in.readNBytes(MAX_BODY_BYTES + 1);
            if (bytes.length > MAX_BODY_BYTES) {
                throw new HttpError(413, "Request body too large");
            }
            return parseForm(new String(bytes, StandardCharsets.UTF_8));
        }
    }

    static String cookie(HttpExchange ex, String name) {
        for (String header : ex.getRequestHeaders().getOrDefault("Cookie", List.of())) {
            for (String part : header.split(";")) {
                String[] kv = part.trim().split("=", 2);
                if (kv.length == 2 && kv[0].equals(name) && !kv[1].isEmpty()) {
                    return kv[1];
                }
            }
        }
        return null;
    }

    // ส่งคำตอบ
    static void sendJson(HttpExchange ex, int status, Object body) throws IOException {
        send(ex, status, "application/json; charset=utf-8", Json.write(body).getBytes(StandardCharsets.UTF_8));
    }

    static void sendError(HttpExchange ex, int status, String message) throws IOException {
        sendJson(ex, status, Map.of("error", message));
    }

    static void send(HttpExchange ex, int status, String contentType, byte[] body) throws IOException {
        Headers h = ex.getResponseHeaders();
        h.set("Content-Type", contentType);
        h.set("X-Content-Type-Options", "nosniff");
        h.set("Cache-Control", "no-store");
        boolean head = "HEAD".equals(ex.getRequestMethod());
        ex.sendResponseHeaders(status, head ? -1 : body.length);
        if (!head) {
            try (OutputStream out = ex.getResponseBody()) {
                out.write(body);
            }
        }
        ex.close();
    }
}
