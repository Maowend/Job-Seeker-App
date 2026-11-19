package jobth.gui;

import jobth.data.JobData;
import jobth.model.JobListing;
import jobth.service.JobFilter;
import jobth.service.JobService.JobView;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

// แท็บรายการงาน: ใช้ได้ 3 แบบ (หางาน / งานที่บันทึก / งานที่ฉันโพสต์)
final class JobsPanel extends JPanel implements MainPanel.Refreshable {

    enum Mode { BROWSE, SAVED, MINE }

    private static final String ANY = "Any";

    private final SwingApp app;
    private final Mode mode;
    private final Runnable onChange;

    // ตัวกรอง
    private final JTextField keyword = new JTextField(14);
    private final JComboBox<String> category = combo(JobData.CATEGORIES);
    private final JComboBox<String> type = combo(JobData.JOB_TYPES);
    private final JTextField minSalary = new JTextField(7);
    private final JTextField location = new JTextField(10);
    private final JCheckBox hotOnly = new JCheckBox("Hot only");

    // รายการ + รายละเอียด
    private final DefaultListModel<JobView> model = new DefaultListModel<>();
    private final JList<JobView> list = new JList<>(model);
    private final JEditorPane detail = new JEditorPane("text/html", "");
    private final JLabel countLabel = SwingApp.muted("");
    private final JButton primary = new JButton();
    private final JButton secondary = new JButton();

    JobsPanel(SwingApp app, Mode mode, Runnable onChange) {
        super(new BorderLayout(0, 8));
        this.app = app;
        this.mode = mode;
        this.onChange = onChange;
        SwingApp.pad(this, 8, 8);

        add(buildFilterBar(), BorderLayout.NORTH);

        // รายการงานด้านซ้าย
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new JobCell());
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelected();
            }
        });
        JScrollPane listScroll = new JScrollPane(list);

        // รายละเอียดด้านขวา
        detail.setEditable(false);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        buttons.add(primary);
        buttons.add(secondary);
        JPanel right = new JPanel(new BorderLayout(0, 8));
        right.add(new JScrollPane(detail), BorderLayout.CENTER);
        right.add(buttons, BorderLayout.SOUTH);

        primary.addActionListener(e -> onPrimary());
        secondary.addActionListener(e -> onSecondary());

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, right);
        split.setResizeWeight(0.42);
        split.setDividerLocation(440);
        add(split, BorderLayout.CENTER);

        refresh();
    }

    private JPanel buildFilterBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        if (mode == Mode.BROWSE) {
            JButton search = new JButton("Search");
            JButton clear = new JButton("Clear");
            search.addActionListener(e -> refresh());
            keyword.addActionListener(e -> refresh());
            minSalary.addActionListener(e -> refresh());
            location.addActionListener(e -> refresh());
            category.addActionListener(e -> refresh());
            type.addActionListener(e -> refresh());
            hotOnly.addActionListener(e -> refresh());
            clear.addActionListener(e -> {
                keyword.setText("");
                minSalary.setText("");
                location.setText("");
                category.setSelectedIndex(0);
                type.setSelectedIndex(0);
                hotOnly.setSelected(false);
                refresh();
            });
            bar.add(new JLabel("Keyword"));
            bar.add(keyword);
            bar.add(new JLabel("Category"));
            bar.add(category);
            bar.add(new JLabel("Type"));
            bar.add(type);
            bar.add(new JLabel("Min salary"));
            bar.add(minSalary);
            bar.add(new JLabel("Location"));
            bar.add(location);
            bar.add(hotOnly);
            bar.add(search);
            bar.add(clear);
        } else {
            bar.add(SwingApp.heading(mode == Mode.SAVED ? "Saved jobs" : "Jobs I posted", 16));
        }
        bar.add(countLabel);
        return bar;
    }

    // โหลดรายการใหม่ (คงตัวที่เลือกไว้ถ้ายังอยู่)
    @Override
    public void refresh() {
        JobView selected = list.getSelectedValue();
        int keepId = selected == null ? -1 : selected.job().id();

        List<JobView> views = app.jobs.searchForUser(app.userId(), currentFilter());
        model.clear();
        int index = -1;
        for (int i = 0; i < views.size(); i++) {
            model.addElement(views.get(i));
            if (views.get(i).job().id() == keepId) {
                index = i;
            }
        }
        countLabel.setText("  " + views.size() + " job(s)");
        if (index < 0 && !model.isEmpty()) {
            index = 0;
        }
        if (index >= 0) {
            list.setSelectedIndex(index);
            list.ensureIndexIsVisible(index);
        }
        showSelected();
    }

    private JobFilter currentFilter() {
        return switch (mode) {
            case SAVED -> JobFilter.NONE.withSavedOnly(true);
            case MINE -> JobFilter.NONE.withMineOnly(true);
            case BROWSE -> JobFilter.NONE
                    .withKeyword(keyword.getText())
                    .withCategory(pick(category))
                    .withType(pick(type))
                    .withMinSalary(parseSalary(minSalary.getText()))
                    .withLocation(location.getText())
                    .withHotOnly(hotOnly.isSelected());
        };
    }

    // แสดงรายละเอียดงานที่เลือก + ตั้งปุ่ม
    private void showSelected() {
        JobView v = list.getSelectedValue();
        if (v == null) {
            detail.setText(html("<p style='color:#6B7280'>"
                    + (mode == Mode.MINE ? "You have not posted any jobs yet. Use the \"Post a job\" tab."
                    : mode == Mode.SAVED ? "No saved jobs yet. Press \"Save\" on a job to keep it here."
                    : "No jobs match your search.") + "</p>"));
            primary.setVisible(false);
            secondary.setVisible(false);
            return;
        }
        // อ่านค่าล่าสุดจากระบบ
        v = app.jobs.viewFor(app.userId(), v.job().id());
        JobListing j = v.job();
        detail.setText(html(describe(v)));
        detail.setCaretPosition(0);

        primary.setVisible(true);
        secondary.setVisible(true);
        if (mode == Mode.MINE) {
            primary.setText("View applicants (" + v.applicantCount() + ")");
            primary.setEnabled(true);
            secondary.setText(j.open() ? "Close job" : "Reopen job");
        } else {
            boolean own = j.isOwnedBy(app.userId());
            primary.setText(v.applied() ? "Applied" : !j.open() ? "Closed" : own ? "Your post" : "Apply now");
            primary.setEnabled(!v.applied() && j.open() && !own);
            secondary.setText(v.bookmarked() ? "Unsave" : "Save");
        }
    }

    private void onPrimary() {
        JobView v = list.getSelectedValue();
        if (v == null) {
            return;
        }
        if (mode == Mode.MINE) {
            new ApplicantsDialog(app, this, v.job()).setVisible(true);
        } else if (new ApplyDialog(app, this, v.job()).showAndApply()) {
            onChange.run();
        }
        refresh();
    }

    private void onSecondary() {
        JobView v = list.getSelectedValue();
        if (v == null) {
            return;
        }
        try {
            if (mode == Mode.MINE) {
                app.jobs.setOpen(app.userId(), v.job().id(), !v.job().open());
            } else {
                app.jobs.toggleBookmark(app.userId(), v.job().id());
            }
        } catch (RuntimeException e) {
            SwingApp.showError(this, e);
        }
        onChange.run();
        refresh();
    }

    // HTML ของรายละเอียดงาน
    private static String describe(JobView v) {
        JobListing j = v.job();
        StringBuilder b = new StringBuilder();
        b.append("<h2 style='margin:0'>").append(esc(j.title()));
        if (j.isHot()) {
            b.append(" <span style='color:#DC2626'>HOT</span>");
        }
        if (!j.open()) {
            b.append(" <span style='color:#6B7280'>(closed)</span>");
        }
        b.append("</h2>");
        b.append("<p style='font-size:13px;color:#2563EB;margin-top:4px'><b>").append(esc(j.company())).append("</b></p>");
        b.append("<table cellpadding='3'>")
                .append(row("Location", j.location()))
                .append(row("Salary", j.salary()))
                .append(row("Type", j.type()))
                .append(row("Category", j.category()))
                .append(row("Posted by", j.postedBy() + (j.postedAt().isEmpty() ? "" : "  ·  " + j.postedAt())))
                .append("</table>");
        b.append("<h3>Job description</h3><p>").append(multiline(j.description())).append("</p>");
        if (!j.requirements().isBlank()) {
            b.append("<h3>Requirements</h3><p>").append(multiline(j.requirements())).append("</p>");
        }
        if (!j.contact().isBlank()) {
            b.append("<h3>Contact</h3><p>").append(esc(j.contact())).append("</p>");
        }
        if (v.applied()) {
            b.append("<p style='color:#059669'><b>You have applied to this job.</b></p>");
        }
        return b.toString();
    }

    private static String row(String label, String value) {
        return "<tr><td style='color:#6B7280'>" + label + "</td><td>" + esc(value) + "</td></tr>";
    }

    private static String html(String body) {
        return "<html><body style='font-family:sans-serif;font-size:11px;padding:8px'>" + body + "</body></html>";
    }

    static String esc(String s) {
        return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static String multiline(String s) {
        return esc(s.strip()).replace("\n", "<br>");
    }

    // ตัวช่วยตัวกรอง
    private static JComboBox<String> combo(List<String> values) {
        List<String> all = new ArrayList<>();
        all.add(ANY);
        all.addAll(values);
        return new JComboBox<>(all.toArray(String[]::new));
    }

    private static String pick(JComboBox<String> box) {
        Object v = box.getSelectedItem();
        return v == null || ANY.equals(v) ? null : v.toString();
    }

    private static Integer parseSalary(String text) {
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return null;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // หน้าตาของแต่ละแถวในรายการ
    private final class JobCell extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> l, Object value, int index,
                                                      boolean selected, boolean focus) {
            JobView v = (JobView) value;
            JobListing j = v.job();
            StringBuilder tags = new StringBuilder();
            if (j.isHot()) {
                tags.append("<font color='#DC2626'><b>HOT</b></font> ");
            }
            if (!j.open()) {
                tags.append("<font color='#6B7280'>CLOSED</font> ");
            }
            if (v.bookmarked() && mode != Mode.SAVED) {
                tags.append("<font color='#D97706'>★ saved</font> ");
            }
            if (v.applied()) {
                tags.append("<font color='#059669'>✓ applied</font> ");
            }
            if (mode == Mode.MINE) {
                tags.append("<font color='#2563EB'>").append(v.applicantCount()).append(" applicant(s), ")
                        .append(v.pendingCount()).append(" new</font>");
            }
            String text = "<html><b style='font-size:12px'>" + esc(j.title()) + "</b>  " + tags
                    + "<br>" + esc(j.company())
                    + "<br><font color='#6B7280'>" + esc(j.location()) + "  ·  " + esc(j.salary())
                    + "  ·  " + esc(j.type()) + "</font></html>";
            JLabel label = (JLabel) super.getListCellRendererComponent(l, text, index, selected, focus);
            // แถวที่เลือก: พื้นฟ้าอ่อน ตัวอักษรเข้ม
            label.setOpaque(true);
            label.setBackground(selected ? new java.awt.Color(0xDBEAFE) : java.awt.Color.WHITE);
            label.setForeground(new java.awt.Color(0x111827));
            label.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new java.awt.Color(0xE5E7EB)),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)));
            return label;
        }
    }
}
