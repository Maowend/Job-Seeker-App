package jobth.model;

// บัญชีผู้ใช้
public record User(
        String id,
        String name,
        String email,
        String passwordHash,
        boolean guest
) {
}
