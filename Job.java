import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Job {

    // ANSI Color and Style Constants
    static final String RESET   = "\u001B[0m";
    static final String BOLD    = "\u001B[1m";
    static final String RED     = "\u001B[31m";
    static final String GREEN   = "\u001B[32m";
    static final String YELLOW  = "\u001B[33m";
    static final String CYAN    = "\u001B[36m";
    static final String WHITE   = "\u001B[37m";
    static final String BG_BLUE = "\u001B[44m";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final List<String> CATEGORIES = List.of(
        "IT & Software", "Data & AI", "Design", "Marketing", "IT & Security"
    );

    // Domain Models
    record JobListing(
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
        boolean isHot
    ) {
        JobListing(int id, String title, String company, String location,
                   String salary, String type, String category,
                   String description, String requirements, String contact, boolean isHot) {
            this(id, title, company, location, salary, parseMaxSalary(salary),
                 type, category, description, requirements, contact, isHot);
        }

        private static int parseMaxSalary(String sal) {
            try {
                String[] parts = sal.replaceAll("[^0-9,\\-]", "").split("-");
                String maxStr = parts[parts.length - 1].replace(",", "").trim();
                return maxStr.isEmpty() ? 0 : Integer.parseInt(maxStr);
            } catch (Exception e) {
                return 0;
            }
        }
    }

    record Applicant(
        String name,
        String email,
        String phone,
        String experience,
        String skills,
        String coverLetter,
        int jobId,
        String appliedAt
    ) {}

    // Application State
    static List<JobListing> jobs = new ArrayList<>();
    static List<Applicant> applications = new ArrayList<>();
    static Set<Integer> bookmarks = new LinkedHashSet<>();
    static Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        seedJobs();
        showApplicationLaunch();
        showBanner();
        mainMenu();
    }

    static void seedJobs() {
        jobs = List.of(
            new JobListing(1,
                "Senior Software Engineer", "TechCorp Thailand", "Bangkok (BTS Asoke)",
                "80,000 - 120,000 THB/mo", "Full-Time", "IT & Software",
                """
                Develop large-scale backend systems handling millions of users
                Design architecture and lead a team of 5-8 developers
                Collaborate with the product manager and UX/UI designer""",
                """
                - Java / Kotlin / Go at least 1 language (5+ yrs)
                - Spring Boot, Microservices, Docker, Kubernetes
                - SQL & NoSQL experience (PostgreSQL, Redis)
                - English communication level""",
                "recruit@techcorp.th | 02-xxx-xxxx", true),

            new JobListing(2,
                "Data Scientist", "DataVision Co., Ltd.", "Bangkok (WFH 3 days/week)",
                "60,000 - 95,000 THB/mo", "Full-Time", "Data & AI",
                """
                Analyze data to create business insights
                Build and deploy ML models in production
                Present data stories to executives and clients""",
                """
                - Python, R, SQL (3+ yrs)
                - ML: Scikit-learn, TensorFlow / PyTorch
                - Advanced statistics and mathematics
                - Bachelor degree in CS or related field""",
                "hr@datavision.co.th", false),

            new JobListing(3,
                "UX/UI Designer", "Creative Studio BKK", "Bangkok (Lat Phrao)",
                "45,000 - 70,000 THB/mo", "Full-Time", "Design",
                """
                Design UI for mobile app and web platform
                Conduct user research and create wireframes/prototypes
                Collaborate with the developer team to deliver accurate design""",
                """
                - Figma, Adobe XD (2+ yrs)
                - Understand user-centered design process
                - Portfolio clearly showing work experience
                - Good communication and presentation skills""",
                "jobs@creativestudio.bkk", true),

            new JobListing(4,
                "Junior Java Developer", "StartupHub Thailand", "Chiang Mai (Hybrid)",
                "25,000 - 40,000 THB/mo", "Full-Time", "IT & Software",
                """
                Develop RESTful APIs using Spring Boot
                Fix bugs and write unit tests
                Learn and grow under senior developer guidance""",
                """
                - Java (1+ yr or academic project)
                - Understanding of OOP and basic design patterns
                - Git, Maven / Gradle
                - Eager to learn new things""",
                "career@startuphub.th | Line: @startuphub", false),

            new JobListing(5,
                "DevOps Engineer", "CloudNet Solutions", "Bangkok (Sathorn) + Remote",
                "70,000 - 100,000 THB/mo", "Full-Time", "IT & Software",
                """
                Manage and develop CI/CD pipelines on AWS / GCP
                Manage infrastructure as code using Terraform
                Monitor system performance and resolve production issues""",
                """
                - Linux, Shell Scripting (3+ yrs)
                - Docker, Kubernetes, Helm
                - AWS / GCP / Azure certification (preferred)
                - Prometheus, Grafana, ELK stack""",
                "devops-recruit@cloudnet.io", true),

            new JobListing(6,
                "Digital Marketing Manager", "E-Commerce TH", "Bangkok (Siam)",
                "50,000 - 75,000 THB/mo", "Full-Time", "Marketing",
                """
                Plan and execute digital marketing strategies
                Manage social media, SEO/SEM, email marketing
                Analyze campaign results and report to management""",
                """
                - 3+ years digital marketing experience
                - Google Ads, Meta Ads, Analytics
                - Strong Thai and English content writing skills
                - Bachelor in marketing or related field""",
                "hr@ecommerceth.com", false),

            new JobListing(7,
                "Cybersecurity Analyst", "SecureIT Thailand", "Bangkok (Nana)",
                "65,000 - 90,000 THB/mo", "Full-Time", "IT & Security",
                """
                Monitor and analyze cyber threats
                Conduct penetration testing and vulnerability assessment
                Create reports and provide security recommendations""",
                """
                - CEH, OSCP, or CISSP (at least 1 cert)
                - SOC / SIEM experience (3+ yrs)
                - Network security knowledge
                - Python / Bash scripting""",
                "security-jobs@secureit.th", true),

            new JobListing(8,
                "Mobile Developer (Flutter)", "AppWorks Studio", "Remote (Nationwide)",
                "40,000 - 65,000 THB/mo", "Full-Time", "IT & Software",
                """
                Develop mobile applications with Flutter for iOS and Android
                Work with designer to implement UI
                Connect REST API and manage state management""",
                """
                - Flutter / Dart (2+ yrs)
                - Riverpod, Provider, or BLoC
                - Git, Firebase
                - App deployed on store (preferred)""",
                "flutter@appworks.dev", false)
        );
    }

    // Screens & Banners
    static void showApplicationLaunch() {
        clearScreen();
        System.out.print(BG_BLUE + BOLD + WHITE + """
          +================================================================+
          |                          JOBTH APP                           |
          |                Career Platform for Thailand                  |
          |                                                                |
          |  [>] Loading job market data...                              |
          |  [>] Checking available positions...                         |
          |  [>] Preparing dashboard...                                  |
          |  [>] Application ready for use...                            |
          |                                                                |
          |  Status: ONLINE  |  Market: TH  |  Version: 1.0.0          |
          +================================================================+
        """ + RESET + "\n");
        pause(1200);
    }

    static void showBanner() {
        clearScreen();
        System.out.print(CYAN + BOLD + """
          +================================================================+
          |                                                                |
          |                 JOBTH CAREER PLATFORM                         |
          |                  Smart Job Search System                      |
          |               Built with Java • Thailand Job Portal            |
          |                                                                |
          |              Version 1.0.0   •   Ready to Use                 |
          |                                                                |
          +================================================================+
        """ + RESET + "\n");
        pause(1000);
    }

    static void exitScreen() {
        clearScreen();
        System.out.print(CYAN + BOLD + """
          +================================================================+
          |                                                                |
          |        Thank you for using JobTH  [*]                         |
          |        Built with Java  -  Programming Presentation            |
          |                                                                |
          |        [*]  Good luck with your job search!  [*]              |
          |                                                                |
          +================================================================+
        """ + RESET + "\n");
    }

    // Menu System
    static void mainMenu() {
        while (true) {
            clearScreen();
            printHeader("[HOME]  MAIN MENU  |  JOBTH");
            System.out.println();
            System.out.println(CYAN + "  +-------------------------------------------+" + RESET);
            printMenuItem("1", "[?]  Search for Jobs");
            printMenuItem("2", "[=]  Browse All Jobs");
            printMenuItem("3", "[!]  Hot Jobs");
            printMenuItem("4", "[*]  Saved Jobs (Bookmarks)");
            printMenuItem("5", "[~]  My Application History");
            printMenuItem("0", "[X]  Exit");
            System.out.println(CYAN + "  +-------------------------------------------+" + RESET);
            System.out.println();
            System.out.print(YELLOW + "  >>> Select menu: " + RESET);

            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1" -> searchMenu();
                case "2" -> browseJobs(jobs, "All Jobs");
                case "3" -> browseHotJobs();
                case "4" -> browseBookmarks();
                case "5" -> showMyApplications();
                case "0" -> { exitScreen(); return; }
                default  -> showError("Please choose 0-5");
            }
        }
    }

    static void searchMenu() {
        clearScreen();
        printHeader("[?]  SEARCH JOBS");
        System.out.println();
        System.out.println(CYAN + "  +-------------------------------------------+" + RESET);
        printMenuItem("1", "Search by Title / Company");
        printMenuItem("2", "Filter by Category");
        printMenuItem("3", "Filter by Salary Range");
        printMenuItem("4", "Filter by Location");
        printMenuItem("9", "<- Back");
        System.out.println(CYAN + "  +-------------------------------------------+" + RESET);
        System.out.println();
        System.out.print(YELLOW + "  >>> Select: " + RESET);

        String choice = sc.nextLine().trim();
        switch (choice) {
            case "1" -> searchByKeyword();
            case "2" -> searchByCategory();
            case "3" -> searchBySalary();
            case "4" -> searchByLocation();
            case "9" -> {}
            default  -> showError("Please choose 1-4 or 9");
        }
    }

    // Search & Filter Operations
    static void searchByKeyword() {
        System.out.print(YELLOW + "\n  [?]  Search (Title / Company): " + RESET);
        String kw = sc.nextLine().trim().toLowerCase();
        List<JobListing> result = jobs.stream()
            .filter(j -> j.title().toLowerCase().contains(kw) || j.company().toLowerCase().contains(kw))
            .toList();
        browseJobs(result, "Search Results: \"" + kw + "\"");
    }

    static void searchByCategory() {
        System.out.println(CYAN + "\n  [=]  Job Categories:" + RESET);
        for (int i = 0; i < CATEGORIES.size(); i++) {
            System.out.printf("  [%d]  %s%n", i + 1, CATEGORIES.get(i));
        }
        System.out.print(YELLOW + "\n  >>> Select: " + RESET);
        try {
            int c = Integer.parseInt(sc.nextLine().trim()) - 1;
            if (c < 0 || c >= CATEGORIES.size()) {
                showError("Invalid choice");
                return;
            }
            String cat = CATEGORIES.get(c);
            List<JobListing> result = jobs.stream()
                .filter(j -> j.category().equalsIgnoreCase(cat))
                .toList();
            browseJobs(result, "Category: " + cat);
        } catch (NumberFormatException e) {
            showError("Please enter a number");
        }
    }

    static void searchBySalary() {
        System.out.println(CYAN + "\n  [$]  Minimum Salary (THB):" + RESET);
        System.out.print(YELLOW + "  >>> " + RESET);
        try {
            int min = Integer.parseInt(sc.nextLine().trim().replace(",", ""));
            List<JobListing> result = jobs.stream()
                .filter(j -> j.maxSalary() >= min)
                .toList();
            browseJobs(result, String.format("Jobs with salary >= %,d THB", min));
        } catch (Exception e) {
            showError("Please enter a valid number");
        }
    }

    static void searchByLocation() {
        System.out.print(YELLOW + "\n  [>]  Enter location / city: " + RESET);
        String loc = sc.nextLine().trim().toLowerCase();
        List<JobListing> result = jobs.stream()
            .filter(j -> j.location().toLowerCase().contains(loc))
            .toList();
        browseJobs(result, "Jobs in: " + loc);
    }

    // Browsing Views
    static void browseJobs(List<JobListing> list, String title) {
        clearScreen();
        printHeader("[=]  " + title);
        System.out.println();

        if (list.isEmpty()) {
            System.out.println(RED + "  [!]  No jobs found matching your criteria" + RESET);
            System.out.print(YELLOW + "\n  Press Enter to go back..." + RESET);
            sc.nextLine();
            return;
        }

        printJobTable(list);
        System.out.println();
        System.out.println(CYAN + "  ---------------------------------------------------------" + RESET);
        System.out.print(YELLOW + "  >>> Enter Job ID to view details (or 0 to go back): " + RESET);

        try {
            int id = Integer.parseInt(sc.nextLine().trim());
            if (id == 0) return;
            findJobInList(list, id).ifPresentOrElse(
                Job::jobDetailView,
                () -> showError("Job ID " + id + " not found")
            );
        } catch (NumberFormatException e) {
            showError("Please enter a number");
        }
    }

    static void browseHotJobs() {
        List<JobListing> hot = jobs.stream().filter(JobListing::isHot).toList();
        browseJobs(hot, "[!]  HOT JOBS -- Featured Positions");
    }

    static void browseBookmarks() {
        if (bookmarks.isEmpty()) {
            clearScreen();
            printHeader("[*]  Saved Jobs");
            System.out.println(YELLOW + "\n  No saved jobs yet.\n  View a job detail and press [B] to bookmark it." + RESET);
            System.out.print(YELLOW + "\n  Press Enter to go back..." + RESET);
            sc.nextLine();
            return;
        }
        List<JobListing> saved = jobs.stream()
            .filter(j -> bookmarks.contains(j.id()))
            .toList();
        browseJobs(saved, "[*]  Saved Jobs (Bookmarks)");
    }

    static void printJobTable(List<JobListing> list) {
        System.out.println(BOLD + CYAN +
            "  +----+----------------------------------+----------------------+------------------------------+" + RESET);
        System.out.println(BOLD + CYAN +
            "  | ID | Job Title                        | Company              | Salary                       |" + RESET);
        System.out.println(BOLD + CYAN +
            "  +----+----------------------------------+----------------------+------------------------------+" + RESET);
        for (JobListing j : list) {
            String hot = j.isHot() ? RED + "[!]" + RESET : "   ";
            String bm = bookmarks.contains(j.id()) ? YELLOW + "[*]" + RESET : "   ";
            String idStr = String.format("%2d", j.id());
            String title = truncate(j.title(), 29);
            String comp = truncate(j.company(), 17);
            String sal = truncate(j.salary(), 28);
            System.out.printf("  | %s | %-3s %-29s| %-3s %-17s| %-30s|%n",
                idStr, hot, title, bm, comp, sal);
        }
        System.out.println(BOLD + CYAN +
            "  +----+----------------------------------+----------------------+------------------------------+" + RESET);
        System.out.println(WHITE + "  " + RED + "[!]" + RESET + WHITE + " = Hot Job   " + YELLOW + "[*]" + RESET + WHITE + " = Bookmarked" + RESET);
    }

    // Details & Applications
    static void jobDetailView(JobListing j) {
        boolean inDetail = true;
        while (inDetail) {
            clearScreen();
            printHeader("[DOC]  Job Detail  #" + j.id());

            System.out.println();
            String hotBadge = j.isHot() ? RED + " [HOT!] " + RESET : "";
            System.out.println(BOLD + CYAN + "  +================================================================+" + RESET);
            System.out.printf(BOLD + CYAN + "  |  %-64s|%n" + RESET, "  [*]  " + j.title() + "  " + hotBadge);
            System.out.println(BOLD + CYAN + "  +================================================================+" + RESET);
            System.out.println();

            printField("[CO]  Company", j.company());
            printField("[>>]  Location", j.location());
            printField("[$]   Salary", j.salary());
            printField("[T]   Type", j.type());
            printField("[/]   Category", j.category());
            printField("[PH]  Contact", j.contact());
            System.out.println();

            System.out.println(BOLD + GREEN + "  [=]  Job Description:" + RESET);
            j.description().lines().forEach(line -> System.out.println(WHITE + "     > " + line + RESET));
            System.out.println();

            System.out.println(BOLD + YELLOW + "  [v]  Requirements:" + RESET);
            j.requirements().lines().forEach(line -> System.out.println(WHITE + "     " + line + RESET));

            System.out.println();
            System.out.println(CYAN + "  -----------------------------------------------------------------" + RESET);
            System.out.println(BOLD + "  [A] Apply for this job   [B] Bookmark/Remove bookmark   [9] Back" + RESET);
            System.out.println(CYAN + "  -----------------------------------------------------------------" + RESET);
            System.out.print(YELLOW + "  >>> Select: " + RESET);

            String choice = sc.nextLine().trim().toUpperCase();
            switch (choice) {
                case "A" -> applyForJob(j);
                case "B" -> toggleBookmark(j);
                case "9" -> inDetail = false;
                default  -> showError("Please choose A, B, or 9");
            }
        }
    }

    static void applyForJob(JobListing j) {
        clearScreen();
        printHeader("[APPLY]  Apply for: " + j.title());

        System.out.println(CYAN + "\n  -----------------------------------------------------------------" + RESET);
        System.out.println(WHITE + "  Fill in your application details (CV / Resume)" + RESET);
        System.out.println(CYAN + "  -----------------------------------------------------------------" + RESET + "\n");

        String name = promptInput("Full Name");
        if (name.isEmpty()) {
            showError("Please enter your name");
            return;
        }

        String email = promptInput("Email");
        String phone = promptInput("Phone Number");
        String exp   = promptInput("Years of Experience");
        String skills= promptInput("Key Skills (e.g. Java, Python, Figma)");

        System.out.println(BOLD + "  [>]  Cover Letter (introduce yourself):" + RESET);
        System.out.println(WHITE + "       (Press Enter twice when done)" + RESET);
        StringBuilder cover = new StringBuilder();
        String line;
        while (!(line = sc.nextLine()).isEmpty()) {
            cover.append(line).append("\n");
        }

        Applicant existing = applications.stream()
            .filter(a -> a.jobId() == j.id() && a.email().equalsIgnoreCase(email))
            .findFirst()
            .orElse(null);

        if (existing != null) {
            showWarning("You have already applied to this job (" + existing.appliedAt() + ")");
            return;
        }

        String ts = LocalDateTime.now().format(DATE_FORMATTER);
        applications.add(new Applicant(name, email, phone, exp, skills, cover.toString().trim(), j.id(), ts));

        clearScreen();
        printHeader("[OK]  Application Submitted!");
        System.out.println();
        System.out.println(GREEN + BOLD +
            "  +================================================================+" + RESET);
        System.out.println(GREEN + BOLD +
            "  |          [OK]  Application submitted successfully!             |" + RESET);
        System.out.println(GREEN + BOLD +
            "  +================================================================+" + RESET);
        System.out.println();
        printField("[*]  Position", j.title());
        printField("[CO] Company", j.company());
        printField("[U]  Name", name);
        printField("[@]  Email", email);
        printField("[t]  Submitted", ts);
        System.out.println();
        System.out.println(YELLOW + "  [i]  HR team will contact you within 3-5 business days" + RESET);
        System.out.print(WHITE + "\n  Press Enter to go back..." + RESET);
        sc.nextLine();
    }

    static void showMyApplications() {
        clearScreen();
        printHeader("[~]  MY APPLICATION HISTORY");

        if (applications.isEmpty()) {
            System.out.println(YELLOW + "\n  No application history yet.\n  Search for jobs and press [A] to apply." + RESET);
            System.out.print(WHITE + "\n  Press Enter to go back..." + RESET);
            sc.nextLine();
            return;
        }

        System.out.println();
        int num = 1;
        for (Applicant a : applications) {
            String jobTitle = findJobById(a.jobId()).map(JobListing::title).orElse("Not found");
            System.out.println(CYAN + "  +---------------------------------------------------------------+" + RESET);
            System.out.printf(CYAN + "  |" + RESET + BOLD + "  #%-2d  %-58s" + CYAN + "|%n" + RESET, num++, jobTitle);
            System.out.println(CYAN + "  +---------------------------------------------------------------+" + RESET);
            System.out.printf(CYAN + "  |" + RESET + "  [U]  Name  : %-50s" + CYAN + "|%n" + RESET, a.name());
            System.out.printf(CYAN + "  |" + RESET + "  [@]  Email : %-50s" + CYAN + "|%n" + RESET, a.email());
            System.out.printf(CYAN + "  |" + RESET + "  [t]  Date  : %-50s" + CYAN + "|%n" + RESET, a.appliedAt());
            System.out.printf(CYAN + "  |" + RESET + GREEN + "  [OK] Status: Pending HR Response%-31s" + CYAN + "|%n" + RESET, "");
            System.out.println(CYAN + "  +---------------------------------------------------------------+" + RESET);
            System.out.println();
        }

        System.out.print(WHITE + "  Press Enter to go back..." + RESET);
        sc.nextLine();
    }

    static void toggleBookmark(JobListing j) {
        if (bookmarks.remove(j.id())) {
            showWarning("Removed bookmark: " + j.title());
        } else {
            bookmarks.add(j.id());
            showSuccess("Bookmarked: " + j.title());
        }
        pause(800);
    }

    // Helper & Utility Methods
    static Optional<JobListing> findJobById(int id) {
        return findJobInList(jobs, id);
    }

    static Optional<JobListing> findJobInList(List<JobListing> list, int id) {
        return list.stream().filter(j -> j.id() == id).findFirst();
    }

    static String promptInput(String label) {
        System.out.print(BOLD + "  [>]  " + label + ": " + RESET);
        return sc.nextLine().trim();
    }

    static void printMenuItem(String key, String text) {
        System.out.printf("%s  |%s  [%s]%s  %-35s%s|%s%n", CYAN, BOLD, key, RESET, text, CYAN, RESET);
    }

    static void printHeader(String title) {
        System.out.println(BG_BLUE + BOLD + WHITE + "  ===  " + title + "  ===  " + RESET);
    }

    static void printField(String label, String value) {
        System.out.printf("  " + BOLD + CYAN + "%-18s" + RESET + WHITE + "  %s%n" + RESET, label + ":", value);
    }

    static String truncate(String s, int max) {
        return s.length() > max ? s.substring(0, max - 1) + "~" : s;
    }

    static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    static void showError(String msg) {
        System.out.println(RED + "\n  [X]  " + msg + RESET);
        System.out.print(WHITE + "  Press Enter to try again..." + RESET);
        sc.nextLine();
    }

    static void showWarning(String msg) {
        System.out.println(YELLOW + "\n  [!]  " + msg + RESET);
    }

    static void showSuccess(String msg) {
        System.out.println(GREEN + "\n  [OK]  " + msg + RESET);
    }

    static void pause(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {}
    }
}
