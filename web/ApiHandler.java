package jobth.web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import jobth.data.JobData;
import jobth.model.Applicant;
import jobth.model.JobListing;
import jobth.service.Forms.ApplicationForm;
import jobth.service.AuthService;
import jobth.service.AuthService.Session;
import jobth.service.Errors.DuplicateAccountException;
import jobth.service.Errors.DuplicateApplicationException;
import jobth.service.Errors.ForbiddenException;
import jobth.service.Errors.InvalidLoginException;
import jobth.service.Errors.JobClosedException;
import jobth.service.JobFilter;
import jobth.service.Forms.JobForm;
import jobth.service.JobService;
import jobth.service.JobService.JobView;
import jobth.service.Errors.NotFoundException;
import jobth.service.Errors.ValidationException;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// API ของหน้าเว็บ
final class ApiHandler implements HttpHandler {

    // คุกกี้ล็อกอิน
    static final String SESSION_COOKIE = "jobth_session";
    private static final String COOKIE_FLAGS = "; Path=/; HttpOnly; SameSite=Strict";

    // รายการ URL ทั้งหมด
    private static final Map<String, List<String>> ROUTES = Map.ofEntries(
            Map.entry("auth/signup", List.of("POST")),
            Map.entry("auth/login", List.of("POST")),
            Map.entry("auth/guest", List.of("POST")),
            Map.entry("auth/logout", List.of("POST")),
            Map.entry("auth/me", List.of("GET")),
            Map.entry("jobs", List.of("GET", "POST")),
            Map.entry("jobs/{id}", List.of("GET")),
            Map.entry("jobs/{id}/bookmark", List.of("POST")),
            Map.entry("jobs/{id}/apply", List.of("POST")),
            Map.entry("jobs/{id}/open", List.of("POST")),
            Map.entry("jobs/{id}/applicants", List.of("GET")),
            Map.entry("applications", List.of("GET")),
            Map.entry("applications/{id}/status", List.of("POST")),
            Map.entry("options", List.of("GET")),
            Map.entry("stats", List.of("GET"))
    );

    // service ที่ใช้
    private final JobService jobs;
    private final AuthService auth;

    ApiHandler(JobService jobs, AuthService auth) {
        this.jobs = jobs;
        this.auth = auth;
    }

    // รับคำขอ / แปลง error
    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            refuseForeignRequest(ex);
            route(ex);
        } catch (Http.HttpError e) {
            Http.sendError(ex, e.status, e.getMessage());
        } catch (ValidationException e) {
            Http.sendError(ex, 400, e.getMessage());
        } catch (InvalidLoginException e) {
            Http.sendError(ex, 401, e.getMessage());
        } catch (ForbiddenException e) {
            Http.sendError(ex, 403, e.getMessage());
        } catch (NotFoundException e) {
            Http.sendError(ex, 404, e.getMessage());
        } catch (DuplicateApplicationException | DuplicateAccountException | JobClosedException e) {
            Http.sendError(ex, 409, e.getMessage());
        } catch (RuntimeException e) {
            System.err.println("Unexpected error on " + ex.getRequestURI() + ": " + e);
            Http.sendError(ex, 500, "Something went wrong on the server");
        }
    }

    // ส่งคำขอไปที่ระบบ
    private void route(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath();
        String[] parts = path.startsWith("/api/") ? path.substring(5).split("/", -1) : new String[0];
        String id = null;
        if (parts.length >= 2 && (parts[0].equals("jobs") || parts[0].equals("applications")) && !parts[1].isEmpty()) {
            id = parts[1];
            parts[1] = "{id}";
        }
        String route = String.join("/", parts);
        String method = checkMethod(ex, route);

        switch (method + " " + route) {
            case "POST auth/signup" -> signUp(ex);
            case "POST auth/login" -> logIn(ex);
            case "POST auth/guest" -> startSession(ex, 200, auth.guest());
            case "POST auth/logout" -> logOut(ex);
            case "GET auth/me" -> Http.sendJson(ex, 200, userJson(requireSession(ex)));
            case "GET jobs" -> listJobs(ex, userId(ex));
            case "POST jobs" -> postJob(ex, requireSession(ex));
            case "GET jobs/{id}" -> getJob(ex, userId(ex), parseId(id));
            case "POST jobs/{id}/bookmark" -> toggleBookmark(ex, userId(ex), parseId(id));
            case "POST jobs/{id}/apply" -> apply(ex, userId(ex), parseId(id));
            case "POST jobs/{id}/open" -> setOpen(ex, userId(ex), parseId(id));
            case "GET jobs/{id}/applicants" -> listApplicants(ex, userId(ex), parseId(id));
            case "GET applications" -> listApplications(ex, userId(ex));
            case "POST applications/{id}/status" -> setStatus(ex, userId(ex), parseId(id));
            case "GET options" -> {
                userId(ex);
                Http.sendJson(ex, 200, Map.of("categories", JobData.CATEGORIES, "types", JobData.JOB_TYPES));
            }
            case "GET stats" -> Http.sendJson(ex, 200, jobs.stats(userId(ex)));
            default -> throw new IllegalStateException("No handler for " + method + " " + route);
        }
    }

    private static String checkMethod(HttpExchange ex, String route) {
        List<String> allowed = ROUTES.get(route);
        if (allowed == null) {
            throw new Http.HttpError(404, "Unknown API path");
        }
        String method = ex.getRequestMethod().equals("HEAD") ? "GET" : ex.getRequestMethod();
        if (!allowed.contains(method)) {
            String list = String.join(", ", allowed);
            ex.getResponseHeaders().set("Allow", list);
            throw new Http.HttpError(405, "Use " + list + " for this path");
        }
        return method;
    }

    // ใครเรียก
    private Session requireSession(HttpExchange ex) {
        return auth.find(Http.cookie(ex, SESSION_COOKIE))
                .orElseThrow(() -> new Http.HttpError(401, "Please log in"));
    }

    private String userId(HttpExchange ex) {
        return requireSession(ex).userId();
    }

    // ระบบบัญชี
    private void signUp(HttpExchange ex) throws IOException {
        Map<String, String> f = Http.formBody(ex);
        startSession(ex, 201, auth.signUp(f.get("name"), f.get("email"), f.get("password")));
    }

    private void logIn(HttpExchange ex) throws IOException {
        Map<String, String> f = Http.formBody(ex);
        startSession(ex, 200, auth.logIn(f.get("email"), f.get("password")));
    }

    private static void startSession(HttpExchange ex, int status, Session s) throws IOException {
        ex.getResponseHeaders().add("Set-Cookie", SESSION_COOKIE + "=" + s.token() + COOKIE_FLAGS);
        Http.sendJson(ex, status, userJson(s));
    }

    private void logOut(HttpExchange ex) throws IOException {
        auth.logOut(Http.cookie(ex, SESSION_COOKIE));
        ex.getResponseHeaders().add("Set-Cookie", SESSION_COOKIE + "=" + COOKIE_FLAGS + "; Max-Age=0");
        Http.sendJson(ex, 200, Map.of("ok", true));
    }

    // ระบบงาน
    private void listJobs(HttpExchange ex, String user) throws IOException {
        Map<String, String> q = Http.query(ex);
        JobFilter filter = JobFilter.NONE
                .withKeyword(q.get("q"))
                .withCategory(q.get("category"))
                .withType(q.get("type"))
                .withMinSalary(parseMinSalary(q.get("minSalary")))
                .withLocation(q.get("location"))
                .withHotOnly("true".equals(q.get("hot")))
                .withSavedOnly("true".equals(q.get("saved")))
                .withMineOnly("true".equals(q.get("mine")));
        List<Map<String, Object>> body = jobs.searchForUser(user, filter).stream()
                .map(v -> jobJson(v, user))
                .toList();
        Http.sendJson(ex, 200, body);
    }

    private void getJob(HttpExchange ex, String user, int id) throws IOException {
        Http.sendJson(ex, 200, jobJson(jobs.viewFor(user, id), user));
    }

    private void postJob(HttpExchange ex, Session s) throws IOException {
        Map<String, String> f = Http.formBody(ex);
        JobForm form = new JobForm(f.get("title"), f.get("company"), f.get("location"), f.get("salary"),
                f.get("type"), f.get("category"), f.get("description"), f.get("requirements"), f.get("contact"));
        JobListing job = jobs.postJob(s.userId(), s.name(), form);
        Http.sendJson(ex, 201, jobJson(jobs.viewFor(s.userId(), job.id()), s.userId()));
    }

    private void setOpen(HttpExchange ex, String user, int id) throws IOException {
        String open = Http.formBody(ex).get("open");
        if (!"true".equals(open) && !"false".equals(open)) {
            throw new ValidationException("open must be true or false");
        }
        jobs.setOpen(user, id, open.equals("true"));
        Http.sendJson(ex, 200, jobJson(jobs.viewFor(user, id), user));
    }

    private void toggleBookmark(HttpExchange ex, String user, int id) throws IOException {
        boolean saved = jobs.toggleBookmark(user, id);
        Http.sendJson(ex, 200, Map.of("id", id, "bookmarked", saved));
    }

    private void apply(HttpExchange ex, String user, int id) throws IOException {
        Map<String, String> f = Http.formBody(ex);
        ApplicationForm form = new ApplicationForm(
                f.get("name"), f.get("email"), f.get("phone"),
                f.get("experience"), f.get("skills"), f.get("coverLetter"));
        Applicant applicant = jobs.apply(user, id, form);
        Http.sendJson(ex, 201, applicationJson(applicant));
    }

    private void listApplicants(HttpExchange ex, String user, int id) throws IOException {
        List<Map<String, Object>> body = jobs.applicantsFor(user, id).reversed().stream()
                .map(this::applicationJson)
                .toList();
        Http.sendJson(ex, 200, body);
    }

    // ระบบใบสมัคร
    private void listApplications(HttpExchange ex, String user) throws IOException {
        List<Map<String, Object>> body = jobs.applications(user).reversed().stream()
                .map(this::applicationJson)
                .toList();
        Http.sendJson(ex, 200, body);
    }

    private void setStatus(HttpExchange ex, String user, int id) throws IOException {
        Applicant changed = jobs.setStatus(user, id, Http.formBody(ex).get("status"));
        Http.sendJson(ex, 200, applicationJson(changed));
    }

    // แปลงเป็น JSON
    private static Map<String, Object> userJson(Session s) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", s.name());
        m.put("email", s.email());
        m.put("guest", s.guest());
        return m;
    }

    private static Map<String, Object> jobJson(JobView v, String user) {
        JobListing j = v.job();
        boolean mine = j.isOwnedBy(user);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", j.id());
        m.put("title", j.title());
        m.put("company", j.company());
        m.put("location", j.location());
        m.put("salary", j.salary());
        m.put("maxSalary", j.maxSalary());
        m.put("type", j.type());
        m.put("category", j.category());
        m.put("description", j.description());
        m.put("requirements", j.requirements());
        m.put("contact", j.contact());
        m.put("isHot", j.isHot());
        m.put("postedBy", j.postedBy());
        m.put("postedAt", j.postedAt());
        m.put("open", j.open());
        m.put("mine", mine);
        m.put("bookmarked", v.bookmarked());
        m.put("applied", v.applied());
        if (mine) {
            m.put("applicantCount", v.applicantCount());
            m.put("pendingCount", v.pendingCount());
        }
        return m;
    }

    private Map<String, Object> applicationJson(Applicant a) {
        JobListing job = jobs.findById(a.jobId()).orElse(null);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", a.id());
        m.put("jobId", a.jobId());
        m.put("jobTitle", job == null ? "Not found" : job.title());
        m.put("company", job == null ? "" : job.company());
        m.put("jobOpen", job != null && job.open());
        m.put("name", a.name());
        m.put("email", a.email());
        m.put("phone", a.phone());
        m.put("experience", a.experience());
        m.put("skills", a.skills());
        m.put("coverLetter", a.coverLetter());
        m.put("appliedAt", a.appliedAt());
        m.put("status", a.status().name());
        m.put("statusLabel", a.status().label());
        return m;
    }

    // ความปลอดภัย
    private static void refuseForeignRequest(HttpExchange ex) {
        String host = ex.getRequestHeaders().getFirst("Host");
        if (host == null || !isLocalHost(host)) {
            throw new Http.HttpError(403, "Requests must be sent to localhost");
        }
        String origin = ex.getRequestHeaders().getFirst("Origin");
        if (ex.getRequestMethod().equals("POST") && origin != null && !origin.equalsIgnoreCase("http://" + host)) {
            throw new Http.HttpError(403, "Requests from other websites are not allowed");
        }
    }

    private static boolean isLocalHost(String hostHeader) {
        String name = hostHeader.startsWith("[")
                ? hostHeader.substring(0, hostHeader.indexOf(']') + 1)
                : hostHeader.replaceFirst(":\\d+$", "");
        return name.equalsIgnoreCase("localhost") || name.equals("127.0.0.1") || name.equals("[::1]");
    }

    // อ่านเลข ID
    private static int parseId(String text) {
        if (text == null || !text.matches("[0-9]{1,9}")) {
            throw new Http.HttpError(400, "ID must be a number");
        }
        return Integer.parseInt(text);
    }

    private static Integer parseMinSalary(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String digits = text.replace(",", "").trim();
        if (!digits.matches("[0-9]{1,9}")) {
            throw new Http.HttpError(400, "minSalary must be a number");
        }
        return Integer.parseInt(digits);
    }
}
