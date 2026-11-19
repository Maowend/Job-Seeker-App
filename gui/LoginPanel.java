package jobth.gui;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagLayout;

// หน้าล็อกอิน / สมัครสมาชิก / เข้าแบบ Guest
final class LoginPanel extends JPanel {

    LoginPanel(SwingApp app) {
        super(new GridBagLayout());

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(0xD1D5DB)),
                BorderFactory.createEmptyBorder(24, 28, 24, 28)));
        card.setPreferredSize(new Dimension(420, 400));

        var title = SwingApp.heading("JobTH", 30);
        title.setForeground(SwingApp.BRAND);
        var subtitle = SwingApp.muted("Find your next job in Thailand");

        // แท็บล็อกอิน
        JTextField loginEmail = new JTextField();
        JPasswordField loginPassword = new JPasswordField();
        JButton loginButton = new JButton("Log in");
        JPanel loginForm = SwingApp.formPanel();
        SwingApp.addRow(loginForm, 0, "Email", loginEmail);
        SwingApp.addRow(loginForm, 1, "Password", loginPassword);
        SwingApp.addRow(loginForm, 2, "", loginButton);
        SwingApp.pad(loginForm, 10, 10);

        // แท็บสมัครสมาชิก
        JTextField signName = new JTextField();
        JTextField signEmail = new JTextField();
        JPasswordField signPassword = new JPasswordField();
        JButton signButton = new JButton("Create account");
        JPanel signForm = SwingApp.formPanel();
        SwingApp.addRow(signForm, 0, "Name", signName);
        SwingApp.addRow(signForm, 1, "Email", signEmail);
        SwingApp.addRow(signForm, 2, "Password", signPassword);
        SwingApp.addRow(signForm, 3, "", signButton);
        SwingApp.pad(signForm, 10, 10);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Log in", loginForm);
        tabs.addTab("Sign up", signForm);

        JButton guestButton = new JButton("Continue as guest");

        // การทำงานของปุ่ม
        Runnable doLogin = () -> {
            try {
                app.onLoggedIn(app.auth.logIn(loginEmail.getText(), new String(loginPassword.getPassword())));
                loginPassword.setText("");
            } catch (RuntimeException e) {
                SwingApp.showError(this, e);
            }
        };
        loginButton.addActionListener(e -> doLogin.run());
        loginPassword.addActionListener(e -> doLogin.run());

        signButton.addActionListener(e -> {
            try {
                app.onLoggedIn(app.auth.signUp(signName.getText(), signEmail.getText(),
                        new String(signPassword.getPassword())));
                signPassword.setText("");
            } catch (RuntimeException ex) {
                SwingApp.showError(this, ex);
            }
        });

        guestButton.addActionListener(e -> app.onLoggedIn(app.auth.guest()));

        for (Component c : new Component[] {title, subtitle, Box.createVerticalStrut(16), tabs,
                Box.createVerticalStrut(12), guestButton}) {
            ((javax.swing.JComponent) c).setAlignmentX(Component.LEFT_ALIGNMENT);
            card.add(c);
        }
        add(card);
    }
}
