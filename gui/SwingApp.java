package jobth.gui;

import jobth.service.AuthService;
import jobth.service.AuthService.Session;
import jobth.service.JobService;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.WindowConstants;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

// หน้าต่างหลักของแอป (Swing): สลับระหว่างหน้าล็อกอินกับหน้าหลัก
public final class SwingApp extends JFrame {

    // สีและฟอนต์ที่ใช้ร่วมกัน
    static final Color BRAND = new Color(0x2563EB);
    static final Color HOT = new Color(0xDC2626);
    static final Color MUTED = new Color(0x6B7280);

    final JobService jobs;
    final AuthService auth;
    private Session session;

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);

    private SwingApp(JobService jobs, AuthService auth) {
        super("JobTH - Career Platform");
        this.jobs = jobs;
        this.auth = auth;
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 650));
        root.add(new LoginPanel(this), "login");
        setContentPane(root);
        setSize(1180, 760);
        setLocationRelativeTo(null);
    }

    // เปิดแอป
    public static void launch(JobService jobs, AuthService auth) {
        SwingUtilities.invokeLater(() -> {
            useNimbus();
            new SwingApp(jobs, auth).setVisible(true);
        });
    }

    // ผู้ใช้ที่ล็อกอินอยู่
    Session session() {
        return session;
    }

    String userId() {
        return session.userId();
    }

    // ล็อกอินสำเร็จ -> สร้างหน้าหลักใหม่
    void onLoggedIn(Session s) {
        this.session = s;
        root.add(new MainPanel(this), "main");
        cards.show(root, "main");
    }

    // ออกจากระบบ -> กลับหน้าล็อกอิน
    void logOut() {
        auth.logOut(session.token());
        session = null;
        for (Component c : root.getComponents()) {
            if (c instanceof MainPanel) {
                root.remove(c);
            }
        }
        cards.show(root, "login");
    }

    // ตัวช่วย UI
    static void showError(Component parent, RuntimeException e) {
        JOptionPane.showMessageDialog(parent, e.getMessage(), "Cannot do that", JOptionPane.WARNING_MESSAGE);
    }

    static JLabel heading(String text, float size) {
        JLabel l = new JLabel(text);
        l.setFont(l.getFont().deriveFont(Font.BOLD, size));
        return l;
    }

    static JLabel muted(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(MUTED);
        return l;
    }

    static void pad(JComponent c, int v, int h) {
        c.setBorder(BorderFactory.createEmptyBorder(v, h, v, h));
    }

    // ฟอร์มสองคอลัมน์: ป้ายชื่อ | ช่องกรอก
    static void addRow(JPanel form, int row, String label, JComponent field) {
        GridBagConstraints l = new GridBagConstraints();
        l.gridx = 0;
        l.gridy = row;
        l.anchor = GridBagConstraints.NORTHWEST;
        l.insets = new Insets(6, 0, 6, 12);
        form.add(new JLabel(label), l);

        GridBagConstraints f = new GridBagConstraints();
        f.gridx = 1;
        f.gridy = row;
        f.weightx = 1;
        f.fill = GridBagConstraints.HORIZONTAL;
        f.insets = new Insets(6, 0, 6, 0);
        form.add(field, f);
    }

    static JPanel formPanel() {
        return new JPanel(new GridBagLayout());
    }

    private static void useNimbus() {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if (info.getName().equals("Nimbus")) {
                    UIManager.setLookAndFeel(info.getClassName());
                    UIManager.put("nimbusBase", new Color(0x1E3A8A));
                    UIManager.put("nimbusFocus", BRAND);
                    UIManager.put("nimbusSelectionBackground", BRAND);
                    return;
                }
            }
        } catch (Exception ignored) {
            // ใช้หน้าตาปกติของ Java แทน
        }
    }
}
