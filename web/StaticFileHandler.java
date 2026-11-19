package jobth.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

// ส่งไฟล์หน้าเว็บ
final class StaticFileHandler implements HttpHandler {

    // ไฟล์ที่อนุญาต
    private record Asset(String file, String contentType) {
    }

    private static final Map<String, Asset> ASSETS = Map.of(
            "/", new Asset("index.html", "text/html; charset=utf-8"),
            "/index.html", new Asset("index.html", "text/html; charset=utf-8"),
            "/style.css", new Asset("style.css", "text/css; charset=utf-8"),
            "/app.js", new Asset("app.js", "text/javascript; charset=utf-8"),
            "/favicon.svg", new Asset("favicon.svg", "image/svg+xml")
    );

    // ความปลอดภัย
    private static final String CSP = "default-src 'self'; img-src 'self' data:; object-src 'none'; base-uri 'none'; form-action 'self'";

    // null = อ่านไฟล์จากในไฟล์ .jar
    private final Path publicDir;

    StaticFileHandler(Path publicDir) {
        this.publicDir = publicDir;
    }

    // อ่านไฟล์: จากโฟลเดอร์ public หรือจากในไฟล์ .jar
    private byte[] read(String name) throws IOException {
        if (publicDir != null) {
            Path file = publicDir.resolve(name);
            return Files.isRegularFile(file) ? Files.readAllBytes(file) : null;
        }
        try (InputStream in = StaticFileHandler.class.getResourceAsStream("/public/" + name)) {
            return in == null ? null : in.readAllBytes();
        }
    }

    // ตอบคำขอ
    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        if (!method.equals("GET") && !method.equals("HEAD")) {
            ex.getResponseHeaders().set("Allow", "GET, HEAD");
            Http.send(ex, 405, "text/plain; charset=utf-8", "Method not allowed".getBytes());
            return;
        }
        Asset asset = ASSETS.get(ex.getRequestURI().getPath());
        byte[] body = asset == null ? null : read(asset.file());
        if (body == null) {
            Http.send(ex, 404, "text/plain; charset=utf-8", "Not found".getBytes());
            return;
        }
        ex.getResponseHeaders().set("Content-Security-Policy", CSP);
        ex.getResponseHeaders().set("Referrer-Policy", "no-referrer");
        Http.send(ex, 200, asset.contentType(), body);
    }
}
