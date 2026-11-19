package jobth.model;

// ใบสมัครงาน
public record Applicant(
        int id,
        // ข้อมูลผู้สมัครกรอก
        String name,
        String email,
        String phone,
        String experience,
        String skills,
        String coverLetter,
        // ข้อมูลที่ระบบใส่ให้
        int jobId,
        String userId,
        String appliedAt,
        ApplicationStatus status
) {

    public Applicant withStatus(ApplicationStatus value) {
        return new Applicant(id, name, email, phone, experience, skills, coverLetter, jobId, userId, appliedAt, value);
    }
}
