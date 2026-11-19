package jobth.service;

// ตัวกรองการค้นหา
public record JobFilter(
        // ช่องตัวกรอง
        String keyword,
        String category,
        String type,
        Integer minSalary,
        String location,
        boolean hotOnly,
        boolean savedOnly,
        boolean mineOnly
) {

    // ตัวกรองว่าง
    public static final JobFilter NONE = new JobFilter(null, null, null, null, null, false, false, false);

    // เปลี่ยนตัวกรองทีละช่อง
    public JobFilter withKeyword(String value) {
        return new JobFilter(value, category, type, minSalary, location, hotOnly, savedOnly, mineOnly);
    }

    public JobFilter withCategory(String value) {
        return new JobFilter(keyword, value, type, minSalary, location, hotOnly, savedOnly, mineOnly);
    }

    public JobFilter withType(String value) {
        return new JobFilter(keyword, category, value, minSalary, location, hotOnly, savedOnly, mineOnly);
    }

    public JobFilter withMinSalary(Integer value) {
        return new JobFilter(keyword, category, type, value, location, hotOnly, savedOnly, mineOnly);
    }

    public JobFilter withLocation(String value) {
        return new JobFilter(keyword, category, type, minSalary, value, hotOnly, savedOnly, mineOnly);
    }

    public JobFilter withHotOnly(boolean value) {
        return new JobFilter(keyword, category, type, minSalary, location, value, savedOnly, mineOnly);
    }

    public JobFilter withSavedOnly(boolean value) {
        return new JobFilter(keyword, category, type, minSalary, location, hotOnly, value, mineOnly);
    }

    public JobFilter withMineOnly(boolean value) {
        return new JobFilter(keyword, category, type, minSalary, location, hotOnly, savedOnly, value);
    }
}
