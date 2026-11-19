package jobth.model;

// สถานะใบสมัคร
public enum ApplicationStatus {
    PENDING("Pending review"),
    INTERVIEW("Interview"),
    HIRED("Hired"),
    REJECTED("Not selected");

    private final String label;

    ApplicationStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}
