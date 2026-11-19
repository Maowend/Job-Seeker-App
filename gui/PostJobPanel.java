package jobth.gui;

import jobth.data.JobData;
import jobth.model.JobListing;
import jobth.service.Forms.JobForm;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;

// แท็บโพสต์งานใหม่
final class PostJobPanel extends JPanel {

    PostJobPanel(SwingApp app, Runnable onPosted) {
        super(new BorderLayout());
        SwingApp.pad(this, 8, 8);

        JTextField title = new JTextField();
        JTextField company = new JTextField();
        JTextField location = new JTextField();
        JTextField salary = new JTextField();
        JComboBox<String> type = new JComboBox<>(JobData.JOB_TYPES.toArray(String[]::new));
        JComboBox<String> category = new JComboBox<>(JobData.CATEGORIES.toArray(String[]::new));
        JTextArea description = area();
        JTextArea requirements = area();
        JTextField contact = new JTextField();

        JPanel form = SwingApp.formPanel();
        SwingApp.addRow(form, 0, "Job title *", title);
        SwingApp.addRow(form, 1, "Company *", company);
        SwingApp.addRow(form, 2, "Location *", location);
        SwingApp.addRow(form, 3, "Salary", salary);
        SwingApp.addRow(form, 4, "", SwingApp.muted("e.g. 30,000 - 45,000 THB/mo (blank = Negotiable)"));
        SwingApp.addRow(form, 5, "Job type *", type);
        SwingApp.addRow(form, 6, "Category *", category);
        SwingApp.addRow(form, 7, "Description *", new JScrollPane(description));
        SwingApp.addRow(form, 8, "Requirements", new JScrollPane(requirements));
        SwingApp.addRow(form, 9, "Contact", contact);

        JButton post = new JButton("Post job");
        JButton clear = new JButton("Clear");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(post);
        buttons.add(clear);

        Runnable reset = () -> {
            for (JTextField f : new JTextField[] {title, company, location, salary, contact}) {
                f.setText("");
            }
            description.setText("");
            requirements.setText("");
            type.setSelectedIndex(0);
            category.setSelectedIndex(0);
        };
        clear.addActionListener(e -> reset.run());
        post.addActionListener(e -> {
            try {
                JobListing job = app.jobs.postJob(app.userId(), app.session().name(), new JobForm(
                        title.getText(), company.getText(), location.getText(), salary.getText(),
                        (String) type.getSelectedItem(), (String) category.getSelectedItem(),
                        description.getText(), requirements.getText(), contact.getText()));
                reset.run();
                onPosted.run();
                JOptionPane.showMessageDialog(this, "\"" + job.title() + "\" is now live. Manage it in \"My posts\".",
                        "Job posted", JOptionPane.INFORMATION_MESSAGE);
            } catch (RuntimeException ex) {
                SwingApp.showError(this, ex);
            }
        });

        JPanel body = new JPanel(new BorderLayout());
        body.add(SwingApp.heading("Post a new job", 16), BorderLayout.NORTH);
        body.add(form, BorderLayout.CENTER);
        body.add(buttons, BorderLayout.SOUTH);
        SwingApp.pad(body, 8, 40);
        add(new JScrollPane(body), BorderLayout.CENTER);
    }

    private static JTextArea area() {
        JTextArea a = new JTextArea(5, 40);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        return a;
    }
}
