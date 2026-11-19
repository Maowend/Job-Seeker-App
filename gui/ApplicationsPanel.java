package jobth.gui;

import jobth.model.Applicant;
import jobth.model.ApplicationStatus;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;

// แท็บใบสมัครของฉัน
final class ApplicationsPanel extends JPanel implements MainPanel.Refreshable {

    private final SwingApp app;
    private final Model model = new Model();
    private final JLabel empty = SwingApp.muted("");

    ApplicationsPanel(SwingApp app) {
        super(new BorderLayout(0, 8));
        this.app = app;
        SwingApp.pad(this, 8, 8);

        JTable table = new JTable(model);
        table.setRowHeight(26);
        table.setAutoCreateRowSorter(true);
        // สีตามสถานะ
        table.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean f, int r, int c) {
                Component comp = super.getTableCellRendererComponent(t, v, sel, f, r, c);
                if (!sel) {
                    String s = String.valueOf(v);
                    comp.setForeground(s.equals(ApplicationStatus.HIRED.label()) ? new Color(0x059669)
                            : s.equals(ApplicationStatus.REJECTED.label()) ? SwingApp.HOT
                            : s.equals(ApplicationStatus.INTERVIEW.label()) ? SwingApp.BRAND
                            : Color.DARK_GRAY);
                }
                return comp;
            }
        });

        JPanel top = new JPanel(new BorderLayout());
        top.add(SwingApp.heading("My applications", 16), BorderLayout.WEST);
        top.add(empty, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        refresh();
    }

    @Override
    public void refresh() {
        model.rows = app.jobs.applications(app.userId());
        model.fireTableDataChanged();
        empty.setText(model.rows.isEmpty() ? "You have not applied to any jobs yet." : model.rows.size() + " application(s)");
    }

    private final class Model extends AbstractTableModel {
        private static final String[] COLS = {"Job", "Company", "Applied as", "Applied at", "Status"};
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
            var job = app.jobs.findById(a.jobId());
            return switch (c) {
                case 0 -> job.map(j -> j.title()).orElse("(removed)");
                case 1 -> job.map(j -> j.company()).orElse("");
                case 2 -> a.name() + " <" + a.email() + ">";
                case 3 -> a.appliedAt();
                default -> a.status().label();
            };
        }
    }
}
