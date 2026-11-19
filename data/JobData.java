package jobth.data;

import jobth.model.JobListing;

import java.util.List;

// ข้อมูลตัวอย่าง
public final class JobData {

    // หมวดหมู่งาน
    public static final List<String> CATEGORIES = List.of(
            "IT & Software", "Data & AI", "Design", "Marketing", "IT & Security",
            "Sales", "Finance & Accounting", "Hospitality", "Education", "Other"
    );

    // ประเภทงาน
    public static final List<String> JOB_TYPES = List.of(
            "Full-Time", "Part-Time", "Contract", "Internship", "Freelance"
    );

    private JobData() {
    }

    // งานตัวอย่าง 8 งาน
    public static List<JobListing> seedJobs() {
        return List.of(
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
                "Cybersecurity Analyst", "SecureIT Thailand", "Bangkok (Silom)",
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
}
