package jobth;

import jobth.data.JobData;
import jobth.gui.SwingApp;
import jobth.service.AuthService;
import jobth.service.JobService;
import jobth.web.WebServer;

import java.awt.Desktop;
import java.awt.GraphicsEnvironment;
import java.net.BindException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

// จุดเริ่มโปรแกรม
//   java jobth.Main              -> เปิดแอปหน้าต่าง Swing (ค่าเริ่มต้น)
//   java jobth.Main --web [port] -> เปิดแบบเว็บ (ต้องมีโฟลเดอร์ public)
public final class Main {

    private Main() {
    }

    public static void main(String[] args) throws Exception {
        boolean web = false;
        boolean openBrowser = true;
        Integer port = null;
        for (String arg : args) {
            switch (arg) {
                case "--web" -> web = true;
                case "--no-browser" -> openBrowser = false;
                default -> {
                    try {
                        port = Integer.parseInt(arg);
                        web = true;
                    } catch (NumberFormatException e) {
                        System.err.println("Unknown argument: " + arg + " (expected --web, a port number or --no-browser)");
                        System.exit(2);
                    }
                }
            }
        }

        JobService service = new JobService(JobData.seedJobs(), Clock.systemDefaultZone());
        AuthService auth = new AuthService();

        if (!web) {
            if (GraphicsEnvironment.isHeadless()) {
                System.err.println("No screen available. Use --web to run the web version instead.");
                System.exit(1);
            }
            SwingApp.launch(service, auth);
            return;
        }
        runWeb(service, auth, port, openBrowser);
    }

    // โหมดเว็บ (แบบเดิม)
    private static void runWeb(JobService service, AuthService auth, Integer port, boolean openBrowser)
            throws Exception {
        Path publicDir = Path.of("public");
        if (!Files.isDirectory(publicDir)) {
            publicDir = null;
            if (Main.class.getResource("/public/index.html") == null) {
                // ไม่มีหน้าเว็บ: เปิดเฉพาะ REST API (/api/...)
                System.out.println("No public folder: running the REST API only (/api/...).");
                openBrowser = false;
            }
        }

        WebServer server = new WebServer(service, auth, publicDir);
        int actualPort;
        try {
            actualPort = server.start(port == null ? 8080 : port);
        } catch (BindException e) {
            if (port != null) {
                System.err.println("Port " + port + " is already in use. Try another number, e.g. 9000.");
                System.exit(1);
            }
            actualPort = server.start(0);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(server::stop));

        String url = "http://localhost:" + actualPort;
        System.out.println("JobTH web edition running at " + url + "  (Ctrl+C to stop)");
        if (openBrowser && Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            try {
                Desktop.getDesktop().browse(new URI(url));
            } catch (Exception e) {
                System.out.println("Open the address above in your browser.");
            }
        }
    }
}
