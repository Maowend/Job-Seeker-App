package jobth.gui;

import jobth.model.JobListing;
import jobth.service.Forms.ApplicationForm;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

// หน้าต่างกรอกใบสมัครงาน
final class ApplyDialog extends JDialog {

    private boolean applied;

    ApplyDialog(SwingApp app, Component parent, JobListing job) {
        super(SwingUtilities.getWindowAncestor(parent), "Apply: " + job.title(), ModalityType.APPLICATION_MODAL);

        // ช่องกรอก (เติมชื่อ/อีเมลให้ถ้าล็อกอินแล้ว)
        JTextField name = new JTextField(app.session().guest() ? "" : app.session().name(), 28);
        JTextField email = new JTextField(app.session().guest() ? "" : app.session().email());
        JTextField phone = new JTextField();
        JTextField experience = new JTextField();
        JTextField skills = new JTextField();
        JTextArea cover = new JTextArea(6, 28);
        cover.setLineWrap(true);
        cover.setWrapStyleWord(true);

        JPanel form = SwingApp.formPanel();
        SwingApp.pad(form, 12, 16);
        SwingApp.addRow(form, 0, "Applying to", SwingApp.muted(job.title() + " — " + job.company()));
        SwingApp.addRow(form, 1, "Full name *", name);
        SwingApp.addRow(form, 2, "Email *", email);
        SwingApp.addRow(form, 3, "Phone", phone);
        SwingApp.addRow(form, 4, "Experience", experience);
        SwingApp.addRow(form, 5, "Skills", skills);
        SwingApp.addRow(form, 6, "Cover letter", new JScrollPane(cover));

        JButton send = new JButton("Send application");
        JButton cancel = new JButton("Cancel");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(cancel);
        buttons.add(send);

        send.addActionListener(e -> {
            try {
                app.jobs.apply(app.userId(), job.id(), new ApplicationForm(name.getText(), email.getText(),
                        phone.getText(), experience.getText(), skills.getText(), cover.getText()));
                applied = true;
                JOptionPane.showMessageDialog(this, "Your application was sent to " + job.company() + ".",
                        "Application sent", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } catch (RuntimeException ex) {
                SwingApp.showError(this, ex);
            }
        });
        cancel.addActionListener(e -> dispose());

        setLayout(new BorderLayout());
        add(form, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(send);
        pack();
        setLocationRelativeTo(parent);
    }

    // เปิดหน้าต่างแล้วรอ: true = ส่งใบสมัครสำเร็จ
    boolean showAndApply() {
        setVisible(true);
        return applied;
    }
}
