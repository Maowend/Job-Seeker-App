package jobth.gui;

import jobth.model.Applicant;
import jobth.model.ApplicationStatus;
import jobth.model.JobListing;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.List;

// หน้าต่างดูผู้สมัครของงานที่ฉันโพสต์ + เปลี่ยนสถานะ
final class ApplicantsDialog extends JDialog {

    private final SwingApp app;
    private final JobListing job;
    private final Model model = new Model();
    private final JTable table = new JTable(model);
    private final JEditorPane detail = new JEditorPane("text/html", "");
    private final JComboBox<ApplicationStatus> status = new JComboBox<>(ApplicationStatus.values());

    ApplicantsDialog(SwingApp app, Component parent, JobListing job) {
        super(SwingUtilities.getWindowAncestor(parent), "Applicants: " + job.title(), ModalityType.APPLICATION_MODAL);
        this.app = app;
        this.job = job;

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setRowHeight(24);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelected();
            }
        });
        detail.setEditable(false);

        // เลือกสถานะใหม่ -> บันทึกทันที
        status.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(javax.swing.JList<?> l, Object v, int i, boolean s, boolean f) {
                return super.getListCellRendererComponent(l, v == null ? "" : ((ApplicationStatus) v).label(), i, s, f);
            }
        });
        JButton save = new JButton("Update status");
        save.addActionListener(e -> updateStatus());
        JButton close = new JButton("Close");
        close.addActionListener(e -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
        actions.add(SwingApp.muted("Status:"));
        actions.add(status);
        actions.add(save);
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(actions, BorderLayout.WEST);
        JPanel closeBox = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        closeBox.add(close);
        bottom.add(closeBox, BorderLayout.EAST);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(table), new JScrollPane(detail));
        split.setResizeWeight(0.5);

        setLayout(new BorderLayout());
        add(split, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        setSize(new Dimension(760, 520));
        setLocationRelativeTo(parent);

        reload();
    }

    private void reload() {
        int row = table.getSelectedRow();
        model.rows = app.jobs.applicantsFor(app.userId(), job.id());
        model.fireTableDataChanged();
        if (!model.rows.isEmpty()) {
            int r = row >= 0 && row < model.rows.size() ? row : 0;
            table.setRowSelectionInterval(r, r);
        }
        showSelected();
    }

    private void showSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            detail.setText("<html><body style='font-family:sans-serif;padding:8px;color:#6B7280'>"
                    + "No one has applied to this job yet.</body></html>");
            status.setEnabled(false);
            return;
        }
        Applicant a = model.rows.get(row);
        status.setEnabled(true);
        status.setSelectedItem(a.status());
        detail.setText("<html><body style='font-family:sans-serif;font-size:11px;padding:8px'>"
                + "<h3 style='margin:0'>" + JobsPanel.esc(a.name()) + "</h3>"
                + "<p>" + JobsPanel.esc(a.email()) + (a.phone().isBlank() ? "" : "  ·  " + JobsPanel.esc(a.phone())) + "</p>"
                + "<p><b>Experience:</b> " + JobsPanel.esc(a.experience()) + "<br>"
                + "<b>Skills:</b> " + JobsPanel.esc(a.skills()) + "<br>"
                + "<b>Applied:</b> " + JobsPanel.esc(a.appliedAt()) + "</p>"
                + "<p><b>Cover letter</b><br>" + JobsPanel.esc(a.coverLetter()).replace("\n", "<br>") + "</p>"
                + "</body></html>");
        detail.setCaretPosition(0);
    }

    private void updateStatus() {
        int row = table.getSelectedRow();
        if (row < 0) {
            return;
        }
        try {
            ApplicationStatus s = (ApplicationStatus) status.getSelectedItem();
            app.jobs.setStatus(app.userId(), model.rows.get(row).id(), s.name());
            reload();
        } catch (RuntimeException e) {
            SwingApp.showError(this, e);
        }
    }

    // ตารางผู้สมัคร
    private static final class Model extends AbstractTableModel {
        private static final String[] COLS = {"Name", "Email", "Experience", "Applied", "Status"};
        List<Applicant> rows = List.of();

        @Override
        public int getRowCount() {
            return rows.size();
        }

        @Override
        public int getColumnCount() {
            return COLS.length;
        }

        @Override
        public String getColumnName(int c) {
            return COLS[c];
        }

        @Override
        public Object getValueAt(int r, int c) {
            Applicant a = rows.get(r);
            return switch (c) {
                case 0 -> a.name();
                case 1 -> a.email();
                case 2 -> a.experience();
                case 3 -> a.appliedAt();
                default -> a.status().label();
            };
        }
    }
}
