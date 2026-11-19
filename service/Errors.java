package jobth.service;

// ข้อผิดพลาดทั้งหมด
public final class Errors {

    private Errors() {
    }

    // 400 กรอกข้อมูลผิด
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }

    // 401 ล็อกอินไม่ผ่าน
    public static class InvalidLoginException extends RuntimeException {
        public InvalidLoginException() {
            super("Email or password is incorrect");
        }
    }

    // 403 ไม่มีสิทธิ์
    public static class ForbiddenException extends RuntimeException {
        public ForbiddenException(String message) {
            super(message);
        }
    }

    // 404 หาไม่เจอ
    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }

    public static class JobNotFoundException extends NotFoundException {
        public JobNotFoundException(int jobId) {
            super("Job ID " + jobId + " not found");
        }
    }

    // 409 ซ้ำ / งานปิดแล้ว
    public static class DuplicateAccountException extends RuntimeException {
        public DuplicateAccountException() {
            super("An account with this email already exists. Please log in.");
        }
    }

    public static class DuplicateApplicationException extends RuntimeException {
        public DuplicateApplicationException(String appliedAt) {
            super("You have already applied to this job (" + appliedAt + ")");
        }
    }

    public static class JobClosedException extends RuntimeException {
        public JobClosedException() {
            super("This job is closed and no longer takes applications");
        }
    }
}
