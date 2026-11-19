package jobth.service;

import jobth.service.Errors.ValidationException;

import java.util.regex.Pattern;

// ตรวจข้อมูล
final class Validation {

    // รูปแบบอีเมล
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private Validation() {
    }

    // ตัวช่วยตรวจ
    static String clean(String s) {
        return s == null ? "" : s.trim();
    }

    static boolean isEmail(String s) {
        return EMAIL.matcher(s).matches();
    }

    static void checkLength(String field, String value, int max) {
        if (value.length() > max) {
            throw new ValidationException(field + " is too long (max " + max + " characters)");
        }
    }
}
