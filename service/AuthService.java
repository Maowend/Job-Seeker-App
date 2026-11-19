package jobth.service;

import jobth.service.Errors.DuplicateAccountException;
import jobth.service.Errors.InvalidLoginException;
import jobth.service.Errors.ValidationException;

import jobth.model.User;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

// ระบบบัญชีผู้ใช้
public class AuthService {

    // ค่าตั้งต้น
    public static final String GUEST_ID = "guest";
    private static final User GUEST = new User(GUEST_ID, "Guest", "", "", true);
    private static final int MIN_PASSWORD = 6;
    private static final int MAX_PASSWORD = 100;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String DUMMY_HASH = PasswordHasher.hash("not-a-real-password");

    // ข้อมูลคนที่ล็อกอินอยู่
    public record Session(String token, String userId, String name, String email, boolean guest) {
    }

    // ข้อมูลในหน่วยความจำ
    private final Map<String, User> usersByEmail = new ConcurrentHashMap<>();
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    // สมัครสมาชิก
    public Session signUp(String name, String email, String password) {
        String cleanName = Validation.clean(name);
        String id = Validation.clean(email).toLowerCase(Locale.ROOT);
        String pw = password == null ? "" : password;

        if (cleanName.isEmpty()) {
            throw new ValidationException("Please enter your name");
        }
        if (!Validation.isEmail(id)) {
            throw new ValidationException("Please enter a valid email address");
        }
        if (pw.length() < MIN_PASSWORD) {
            throw new ValidationException("Password must be at least " + MIN_PASSWORD + " characters");
        }
        Validation.checkLength("Name", cleanName, 120);
        Validation.checkLength("Email", id, 120);
        Validation.checkLength("Password", pw, MAX_PASSWORD);

        User user = new User(id, cleanName, id, PasswordHasher.hash(pw), false);
        if (usersByEmail.putIfAbsent(id, user) != null) {
            throw new DuplicateAccountException();
        }
        return startSession(user);
    }

    // ล็อกอิน / Guest / ออกจากระบบ
    public Session logIn(String email, String password) {
        String id = Validation.clean(email).toLowerCase(Locale.ROOT);
        User user = usersByEmail.get(id);
        String storedHash = user != null ? user.passwordHash() : DUMMY_HASH;
        boolean passwordOk = PasswordHasher.matches(password == null ? "" : password, storedHash);
        if (user == null || !passwordOk) {
            throw new InvalidLoginException();
        }
        return startSession(user);
    }

    public Session guest() {
        return startSession(GUEST);
    }

    public void logOut(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    // หาผู้ใช้จาก token
    public Optional<Session> find(String token) {
        return token == null ? Optional.empty() : Optional.ofNullable(sessions.get(token));
    }

    public String storedPasswordHashFor(String email) {
        return usersByEmail.get(email.toLowerCase(Locale.ROOT)).passwordHash();
    }

    // สร้าง token
    private Session startSession(User user) {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        Session session = new Session(token, user.id(), user.name(), user.email(), user.guest());
        sessions.put(token, session);
        return session;
    }
}
