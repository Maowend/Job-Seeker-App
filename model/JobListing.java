package jobth.model;

// ประกาศงาน
public record JobListing(
        // ข้อมูลงาน
        int id,
        String title,
        String company,
        String location,
        String salary,
        int maxSalary,
        String type,
        String category,
        String description,
        String requirements,
        String contact,
        boolean isHot,
        // ผู้โพสต์ / วันที่ / เปิดรับไหม
        String ownerId,
        String postedBy,
        String postedAt,
        boolean open
) {

    public static final String SEED_OWNER = "jobth";

    public JobListing(int id, String title, String company, String location,
                      String salary, String type, String category,
                      String description, String requirements, String contact, boolean isHot) {
        this(id, title, company, location, salary, parseMaxSalary(salary),
             type, category, description, requirements, contact, isHot,
             SEED_OWNER, "JobTH", "", true);
    }

    // เช็กเจ้าของ / ปิดเปิดงาน
    public boolean isOwnedBy(String userId) {
        return ownerId.equals(userId);
    }

    public JobListing withOpen(boolean value) {
        return new JobListing(id, title, company, location, salary, maxSalary, type, category,
                description, requirements, contact, isHot, ownerId, postedBy, postedAt, value);
    }

    // แปลงเงินเดือนเป็นตัวเลข
    public static int parseMaxSalary(String salary) {
        if (salary == null) {
            return 0;
        }
        String[] parts = salary.replaceAll("[^0-9,\\-]", "").split("-");
        String max = parts.length == 0 ? "" : parts[parts.length - 1].replace(",", "").trim();
        try {
            return max.isEmpty() ? 0 : Integer.parseInt(max);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
