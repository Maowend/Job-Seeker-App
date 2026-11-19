package jobth.web;

import com.sun.net.httpserver.HttpServer;
import jobth.service.AuthService;
import jobth.service.JobService;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// เว็บเซิร์ฟเวอร์
public class WebServer {

    // สิ่งที่ต้องใช้
    private final JobService service;
    private final AuthService auth;
    private final Path publicDir;
    // ตัวเซิร์ฟเวอร์
    private HttpServer server;
    private ExecutorService threads;

    public WebServer(JobService service, AuthService auth, Path publicDir) {
        this.service = service;
        this.auth = auth;
        this.publicDir = publicDir;
    }

    // เริ่มเซิร์ฟเวอร์
    public int start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), port), 0);
        server.createContext("/api", new ApiHandler(service, auth));
        server.createContext("/", new StaticFileHandler(publicDir));
        threads = Executors.newFixedThreadPool(4);
        server.setExecutor(threads);
        server.start();
        return server.getAddress().getPort();
    }

    // หยุดเซิร์ฟเวอร์
    public void stop() {
        if (server != null) {
            server.stop(0);
            threads.shutdownNow();
            server = null;
        }
    }
}
