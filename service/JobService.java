package jobth.service;

import jobth.service.Errors.DuplicateApplicationException;
import jobth.service.Errors.ForbiddenException;
import jobth.service.Errors.JobClosedException;
import jobth.service.Errors.JobNotFoundException;
import jobth.service.Errors.NotFoundException;
import jobth.service.Errors.ValidationException;
import jobth.service.Forms.ApplicationForm;
import jobth.service.Forms.JobForm;

import jobth.data.JobData;
import jobth.model.Applicant;
import jobth.model.ApplicationStatus;
import jobth.model.JobListing;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static jobth.service.Validation.checkLength;
import static jobth.service.Validation.clean;

// ระบบงานหลัก
public class JobService {

    // ค่าตั้งต้น
    private static final DateTimeFormatter APPLIED_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter POSTED_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int MAX_SHORT_TEXT = 120;
    private static final int MAX_LONG_TEXT = 2000;

    // ชนิดผลลัพธ์
    public record Stats(int totalJobs, int hotJobs, int bookmarks, int applications, int myPosts, int newApplicants) {
    }

    public record JobView(JobListing job, boolean bookmarked, boolean applied, int applicantCount, int pendingCount) {
    }

    // ข้อมูลในหน่วยความจำ
    private final List<JobListing> jobs = new ArrayList<>();
    private final List<Applicant> applications = new ArrayList<>();
    private final Map<String, Set<Integer>> bookmarksByUser = new HashMap<>();
    private final Clock clock;
    private int nextJobId;
    private int nextApplicationId = 1;

    // ตั้งค่าเริ่มต้น
    public JobService(List<JobListing> seedJobs, Clock clock) {
        this.jobs.addAll(seedJobs);
        this.clock = clock;
        this.nextJobId = seedJobs.stream().mapToInt(JobListing::id).max().orElse(0) + 1;
    }

    // ระบบค้นหา
    public synchronized List<JobListing> search(String userId, JobFilter filter) {
        Set<Integer> saved = bookmarksOf(userId);
        return jobs.reversed().stream()
                .filter(j -> filter.mineOnly() ? j.isOwnedBy(userId) : (j.open() || filter.savedOnly()))
                .filter(j -> isBlank(filter.keyword())
                        || containsIgnoreCase(j.title(), filter.keyword())
                        || containsIgnoreCase(j.company(), filter.keyword())
                        || containsIgnoreCase(j.description(), filter.keyword()))
                .filter(j -> isBlank(filter.category()) || j.category().equalsIgnoreCase(filter.category().trim()))
                .filter(j -> isBlank(filter.type()) || j.type().equalsIgnoreCase(filter.type().trim()))
                .filter(j -> filter.minSalary() == null || j.maxSalary() >= filter.minSalary())
                .filter(j -> isBlank(filter.location()) || containsIgnoreCase(j.location(), filter.location()))
                .filter(j -> !filter.hotOnly() || j.isHot())
                .filter(j -> !filter.savedOnly() || saved.contains(j.id()))
                .toList();
    }

    public synchronized List<JobView> searchForUser(String userId, JobFilter filter) {
        return search(userId, filter).stream().map(j -> view(userId, j)).toList();
    }

    public synchronized JobView viewFor(String userId, int jobId) {
        return view(userId, requireJob(jobId));
    }

    public synchronized Optional<JobListing> findById(int id) {
        return jobs.stream().filter(j -> j.id() == id).findFirst();
    }

    // ระบบโพสต์งาน
    public synchronized JobListing postJob(String userId, String posterName, JobForm form) {
        String title = clean(form.title());
        String company = clean(form.company());
        String location = clean(form.location());
        String salary = clean(form.salary());
        String type = clean(form.type());
        String category = clean(form.category());
        String description = clean(form.description());
        String requirements = clean(form.requirements());
        String contact = clean(form.contact());

        require("Job title", title);
        require("Company", company);
        require("Location", location);
        require("Job description", description);
        if (!JobData.JOB_TYPES.contains(type)) {
            throw new ValidationException("Please choose a job type");
        }
        if (!JobData.CATEGORIES.contains(category)) {
            throw new ValidationException("Please choose a category");
        }
        checkLength("Job title", title, MAX_SHORT_TEXT);
        checkLength("Company", company, MAX_SHORT_TEXT);
        checkLength("Location", location, MAX_SHORT_TEXT);
        checkLength("Salary", salary, MAX_SHORT_TEXT);
        checkLength("Contact", contact, MAX_SHORT_TEXT);
        checkLength("Job description", description, MAX_LONG_TEXT);
        checkLength("Requirements", requirements, MAX_LONG_TEXT);

        String shownSalary = salary.isEmpty() ? "Negotiable" : salary;
        JobListing job = new JobListing(nextJobId++, title, company, location, shownSalary,
                JobListing.parseMaxSalary(shownSalary), type, category, description, requirements, contact,
                false, userId, clean(posterName), LocalDateTime.now(clock).format(POSTED_FORMAT), true);
        jobs.add(job);
        return job;
    }

    public synchronized JobListing setOpen(String userId, int jobId, boolean open) {
        JobListing job = requireOwnJob(userId, jobId);
        JobListing changed = job.withOpen(open);
        jobs.set(jobs.indexOf(job), changed);
        return changed;
    }

    public synchronized List<Applicant> applicantsFor(String userId, int jobId) {
        requireOwnJob(userId, jobId);
        return applications.stream().filter(a -> a.jobId() == jobId).toList();
    }

    public synchronized Applicant setStatus(String userId, int applicationId, String statusName) {
        Applicant app = applications.stream().filter(a -> a.id() == applicationId).findFirst()
                .orElseThrow(() -> new NotFoundException("Application ID " + applicationId + " not found"));
        requireOwnJob(userId, app.jobId());
        ApplicationStatus status;
        try {
            status = ApplicationStatus.valueOf(clean(statusName).toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown status: " + clean(statusName));
        }
        Applicant changed = app.withStatus(status);
        applications.set(applications.indexOf(app), changed);
        return changed;
    }

    // ระบบบันทึกงาน
    public synchronized boolean toggleBookmark(String userId, int jobId) {
        requireJob(jobId);
        Set<Integer> saved = bookmarksByUser.computeIfAbsent(userId, k -> new LinkedHashSet<>());
        if (saved.remove(jobId)) {
            return false;
        }
        saved.add(jobId);
        return true;
    }

    public synchronized boolean isBookmarked(String userId, int jobId) {
        return bookmarksOf(userId).contains(jobId);
    }

    // ระบบสมัครงาน
    public synchronized Applicant apply(String userId, int jobId, ApplicationForm form) {
        JobListing job = requireJob(jobId);
        if (job.isOwnedBy(userId)) {
            throw new ForbiddenException("You can't apply to your own job post");
        }
        if (!job.open()) {
            throw new JobClosedException();
        }

        String name = clean(form.name());
        String email = clean(form.email());
        String phone = clean(form.phone());
        String experience = clean(form.experience());
        String skills = clean(form.skills());
        String coverLetter = clean(form.coverLetter());

        if (name.isEmpty()) {
            throw new ValidationException("Please enter your name");
        }
        if (!Validation.isEmail(email)) {
            throw new ValidationException("Please enter a valid email address");
        }
        checkLength("Name", name, MAX_SHORT_TEXT);
        checkLength("Email", email, MAX_SHORT_TEXT);
        checkLength("Phone", phone, MAX_SHORT_TEXT);
        checkLength("Experience", experience, MAX_SHORT_TEXT);
        checkLength("Skills", skills, MAX_SHORT_TEXT * 2);
        checkLength("Cover letter", coverLetter, MAX_LONG_TEXT);

        Optional<Applicant> existing = applications.stream()
                .filter(a -> a.jobId() == jobId)
                .filter(a -> a.email().equalsIgnoreCase(email)
                        || (a.userId().equals(userId) && !userId.equals(AuthService.GUEST_ID)))
                .findFirst();
        if (existing.isPresent()) {
            throw new DuplicateApplicationException(existing.get().appliedAt());
        }

        String appliedAt = LocalDateTime.now(clock).format(APPLIED_FORMAT);
        Applicant applicant = new Applicant(nextApplicationId++, name, email, phone, experience, skills, coverLetter,
                jobId, userId, appliedAt, ApplicationStatus.PENDING);
        applications.add(applicant);
        return applicant;
    }

    public synchronized List<Applicant> applications(String userId) {
        return Collections.unmodifiableList(new ArrayList<>(
                applications.stream().filter(a -> a.userId().equals(userId)).toList()));
    }

    // ตัวเลขสรุป
    public synchronized Stats stats(String userId) {
        int open = (int) jobs.stream().filter(JobListing::open).count();
        int hot = (int) jobs.stream().filter(j -> j.open() && j.isHot()).count();
        int sent = (int) applications.stream().filter(a -> a.userId().equals(userId)).count();
        int posts = (int) jobs.stream().filter(j -> j.isOwnedBy(userId)).count();
        int waiting = (int) applications.stream()
                .filter(a -> a.status() == ApplicationStatus.PENDING)
                .filter(a -> findById(a.jobId()).map(j -> j.isOwnedBy(userId)).orElse(false))
                .count();
        return new Stats(open, hot, bookmarksOf(userId).size(), sent, posts, waiting);
    }

    // ตัวช่วย
    private JobView view(String userId, JobListing j) {
        boolean applied = applications.stream().anyMatch(a -> a.jobId() == j.id() && a.userId().equals(userId));
        int count = 0;
        int pending = 0;
        if (j.isOwnedBy(userId)) {
            for (Applicant a : applications) {
                if (a.jobId() == j.id()) {
                    count++;
                    if (a.status() == ApplicationStatus.PENDING) {
                        pending++;
                    }
                }
            }
        }
        return new JobView(j, bookmarksOf(userId).contains(j.id()), applied, count, pending);
    }

    private Set<Integer> bookmarksOf(String userId) {
        return bookmarksByUser.getOrDefault(userId, Set.of());
    }

    private JobListing requireJob(int jobId) {
        return findById(jobId).orElseThrow(() -> new JobNotFoundException(jobId));
    }

    private JobListing requireOwnJob(String userId, int jobId) {
        JobListing job = requireJob(jobId);
        if (!job.isOwnedBy(userId)) {
            throw new ForbiddenException("Only the person who posted this job can manage it");
        }
        return job;
    }

    private static void require(String field, String value) {
        if (value.isEmpty()) {
            throw new ValidationException(field + " is required");
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static boolean containsIgnoreCase(String text, String part) {
        return text.toLowerCase(Locale.ROOT).contains(part.trim().toLowerCase(Locale.ROOT));
    }
}
