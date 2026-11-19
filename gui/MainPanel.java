package jobth.gui;

import jobth.service.JobService.Stats;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;

// หน้าหลักหลังล็อกอิน: แถบหัว + แท็บต่าง ๆ
final class MainPanel extends JPanel {

    private final SwingApp app;
    private final JLabel statsLabel = new JLabel();

    MainPanel(SwingApp app) {
        super(new BorderLayout());
        this.app = app;

        // แถบหัว
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0x1E3A8A));
        SwingApp.pad(header, 10, 16);

        JLabel brand = SwingApp.heading("JobTH", 22);
        brand.setForeground(Color.WHITE);
        statsLabel.setForeground(new Color(0xDBEAFE));

        JLabel who = new JLabel("Hello, " + app.session().name() + (app.session().guest() ? " (guest)" : ""));
        who.setForeground(Color.WHITE);
        JButton logout = new JButton("Log out");
        logout.addActionListener(e -> app.logOut());

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        left.setOpaque(false);
        left.add(brand);
        left.add(statsLabel);
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(who);
        right.add(logout);
        header.add(left, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);

        // แท็บ
        JobsPanel browse = new JobsPanel(app, JobsPanel.Mode.BROWSE, this::refreshStats);
        JobsPanel saved = new JobsPanel(app, JobsPanel.Mode.SAVED, this::refreshStats);
        ApplicationsPanel applications = new ApplicationsPanel(app);
        PostJobPanel post = new PostJobPanel(app, this::refreshStats);
        JobsPanel mine = new JobsPanel(app, JobsPanel.Mode.MINE, this::refreshStats);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        tabs.addTab("Find jobs", browse);
        tabs.addTab("Saved", saved);
        tabs.addTab("My applications", applications);
        tabs.addTab("Post a job", post);
        tabs.addTab("My posts", mine);

        // เปลี่ยนแท็บ = โหลดข้อมูลล่าสุด
        tabs.addChangeListener(e -> {
            if (tabs.getSelectedComponent() instanceof Refreshable r) {
                r.refresh();
            }
            refreshStats();
        });

        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
        refreshStats();
    }

    private void refreshStats() {
        Stats s = app.jobs.stats(app.userId());
        statsLabel.setText(s.totalJobs() + " open jobs  ·  " + s.hotJobs() + " hot  ·  "
                + s.bookmarks() + " saved  ·  " + s.applications() + " applied  ·  "
                + s.myPosts() + " posts  ·  " + s.newApplicants() + " new applicants");
    }

    // แท็บที่โหลดข้อมูลใหม่ได้
    interface Refreshable {
        void refresh();
    }
}
