import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Properties;

/**
 * LoginScreen.java - the admin login window shown before the main window.
 * There is one admin account only. Its login is kept in admin.properties (not in the database).
 */
public class LoginScreen extends JDialog {
    private final JTextField txtUser = new JTextField();
    private final JPasswordField txtPass = new JPasswordField();
    private boolean success = false;

    public LoginScreen(Window owner) {
        super(owner, "Stock Bite - Admin Login", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        Style.styleField(txtUser);
        Style.styleField(txtPass);
        txtUser.setFont(Style.BASE.deriveFont(15f));
        txtPass.setFont(Style.BASE.deriveFont(15f));
        txtPass.setEchoChar('\u2022');
        txtUser.setPreferredSize(new Dimension(10, 48));
        txtPass.setPreferredSize(new Dimension(10, 48));

        // ---------- left: brand panel ----------
        JPanel brand = new JPanel(null) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, Style.PEACH, getWidth(), getHeight(), Style.HEADER));
                g2.fillRect(0, 0, getWidth(), getHeight());
                // soft decorative circles
                g2.setColor(new Color(255, 255, 255, 34));
                g2.fillOval(-90, getHeight() - 210, 300, 300);
                g2.setColor(new Color(255, 236, 178, 46));
                g2.fillOval(getWidth() - 130, -80, 240, 240);
                g2.setColor(new Color(255, 255, 255, 26));
                g2.fillOval(getWidth() - 200, getHeight() - 130, 170, 170);
                // mint stripe on the edge, like the header of the main window
                g2.setColor(Style.MINT);
                g2.fillRect(getWidth() - 6, 0, 6, getHeight());
                g2.dispose();
            }
        };
        brand.setPreferredSize(new Dimension(340, 0));
        brand.setLayout(new GridBagLayout());
        JLabel logo = new JLabel("Stock Bite");
        logo.setFont(Style.TITLE.deriveFont(42f));
        logo.setForeground(Color.WHITE);
        JLabel tagline = new JLabel("School Canteen Management System");
        tagline.setFont(Style.BASE.deriveFont(15f));
        tagline.setForeground(Style.CREAM);
        JLabel features = new JLabel("<html>Products  \u2022  Inventory<br>Point of Sale  \u2022  Transactions</html>");
        features.setFont(Style.SMALL);
        features.setForeground(new Color(255, 255, 255, 215));
        JLabel school = new JLabel("University of Caloocan City");
        school.setFont(Style.BOLD);
        school.setForeground(Color.WHITE);
        JLabel dept = new JLabel("Computer Studies Department");
        dept.setFont(Style.SMALL);
        dept.setForeground(Style.CREAM);

        GridBagConstraints b = new GridBagConstraints();
        b.gridx = 0; b.anchor = GridBagConstraints.WEST; b.weightx = 1;
        b.insets = new Insets(0, 40, 0, 20);
        b.gridy = 0; b.weighty = 1; b.anchor = GridBagConstraints.SOUTHWEST; b.insets = new Insets(0, 40, Style.SM, 20);
        brand.add(logo, b);
        b.gridy = 1; b.weighty = 0; b.anchor = GridBagConstraints.WEST; b.insets = new Insets(0, 42, Style.MD, 20);
        brand.add(tagline, b);
        b.gridy = 2; b.insets = new Insets(0, 42, 0, 20);
        brand.add(features, b);
        b.gridy = 3; b.weighty = 1; b.anchor = GridBagConstraints.SOUTHWEST; b.insets = new Insets(0, 42, 0, 20);
        JPanel footer = new JPanel();
        footer.setOpaque(false);
        footer.setLayout(new BoxLayout(footer, BoxLayout.Y_AXIS));
        school.setAlignmentX(Component.LEFT_ALIGNMENT);
        dept.setAlignmentX(Component.LEFT_ALIGNMENT);
        footer.add(school);
        footer.add(dept);
        footer.setBorder(BorderFactory.createEmptyBorder(0, 0, 34, 0));
        brand.add(footer, b);

        // ---------- right: form ----------
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Style.BG);
        form.setBorder(BorderFactory.createEmptyBorder(Style.XL, 52, Style.XL, 52));
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        JLabel welcome = new JLabel("Welcome back");
        welcome.setFont(Style.TITLE);
        welcome.setForeground(Style.DARK);
        g.gridy = y++; g.insets = new Insets(0, 0, Style.XS, 0);
        form.add(welcome, g);
        JLabel intro = Style.hint("Log in as admin to manage your canteen.");
        intro.setFont(Style.BASE.deriveFont(14f));
        g.gridy = y++; g.insets = new Insets(0, 0, 30, 0);
        form.add(intro, g);

        g.gridy = y++; g.insets = new Insets(0, 0, Style.SM, 0);
        form.add(Style.label("Username"), g);
        g.gridy = y++; g.insets = new Insets(0, 0, Style.LG + 2, 0);
        form.add(txtUser, g);
        g.gridy = y++; g.insets = new Insets(0, 0, Style.SM, 0);
        form.add(Style.label("Password"), g);
        g.gridy = y++; g.insets = new Insets(0, 0, Style.MD, 0);
        form.add(txtPass, g);

        JCheckBox show = new JCheckBox("Show password");
        show.setFont(Style.BASE);
        show.setForeground(Style.MUTED);
        show.setOpaque(false);
        show.setFocusPainted(false);
        show.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        show.addActionListener(e -> txtPass.setEchoChar(show.isSelected() ? (char) 0 : '\u2022'));
        g.gridy = y++; g.insets = new Insets(0, 0, 26, 0);
        form.add(show, g);

        JButton login = Style.button("Log In", Style.PRIMARY);
        login.setFont(Style.BOLD.deriveFont(16f));
        login.setPreferredSize(new Dimension(10, 50));
        login.addActionListener(e -> attempt());
        g.gridy = y++; g.insets = new Insets(0, 0, 0, 0);
        form.add(login, g);

        JPanel root = new JPanel(new BorderLayout());
        root.add(brand, BorderLayout.WEST);
        root.add(form, BorderLayout.CENTER);
        setContentPane(root);
        getRootPane().setDefaultButton(login);   // Enter = Log In
        setSize(860, 540);
        setLocationRelativeTo(owner);
        SwingUtilities.invokeLater(txtUser::requestFocusInWindow);
    }

    public boolean isSuccess() { return success; }

    private void attempt() {
        String user = txtUser.getText().trim();
        String pass = new String(txtPass.getPassword());
        if (Auth.check(user, pass)) {
            success = true;
            dispose();
        } else {
            txtPass.setText("");
            Style.warn(this, "Incorrect username or password.");
            txtPass.requestFocusInWindow();
        }
    }
}

/**
 * Keeps the single admin login in admin.properties (username, salt, hash). The password is stored as a
 * salted SHA-256 hash. Created with admin / admin123 on first run. To change the password, write a line
 * "password=newpassword" in the file: it is hashed and removed the next time the app starts.
 */
final class Auth {
    private static final File FILE = new File("admin.properties");
    private static final String DEFAULT_USER = "admin";
    private static final String DEFAULT_PASS = "admin123";

    private Auth() {}

    static boolean check(String username, String password) {
        try {
            Properties p = load();
            String salt = p.getProperty("salt", "");
            String hash = p.getProperty("hash", "");
            return username.equals(p.getProperty("username", DEFAULT_USER))
                    && MessageDigest.isEqual(hash(salt, password).getBytes(StandardCharsets.UTF_8),
                                             hash.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return false;
        }
    }

    /** Reads admin.properties, creating it or hashing a newly typed password when needed. */
    private static Properties load() throws Exception {
        Properties p = new Properties();
        if (FILE.isFile()) {
            try (InputStream in = new FileInputStream(FILE)) { p.load(in); }
        }
        String plain = p.getProperty("password");
        boolean missing = p.getProperty("hash") == null || p.getProperty("salt") == null;
        if (plain != null || missing) {
            if (plain == null) plain = DEFAULT_PASS;
            if (p.getProperty("username") == null || p.getProperty("username").trim().isEmpty()) {
                p.setProperty("username", DEFAULT_USER);
            }
            byte[] raw = new byte[16];
            new SecureRandom().nextBytes(raw);
            String salt = Base64.getEncoder().encodeToString(raw);
            p.setProperty("salt", salt);
            p.setProperty("hash", hash(salt, plain));
            p.remove("password");
            try (OutputStream out = new FileOutputStream(FILE)) {
                p.store(out, "Stock Bite admin login. To change the password add a line: password=newpassword");
            }
        }
        return p;
    }

    private static String hash(String salt, String password) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt.getBytes(StandardCharsets.UTF_8));
        byte[] d = md.digest(password.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : d) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
