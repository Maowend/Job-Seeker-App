package jobth.service;

// ข้อมูลจากฟอร์ม
public final class Forms {

    private Forms() {
    }

    // ฟอร์มสมัครงาน
    public record ApplicationForm(
            String name,
            String email,
            String phone,
            String experience,
            String skills,
            String coverLetter
    ) {
    }

    // ฟอร์มโพสต์งาน
    public record JobForm(
            String title,
            String company,
            String location,
            String salary,
            String type,
            String category,
            String description,
            String requirements,
            String contact
    ) {
    }
}
