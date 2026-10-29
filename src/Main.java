import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

/**
 * Main.java - start here. Run this file to open Stock Bite.
 * It builds the main window: the orange header and the four tabs.
 */
public class Main extends JFrame {
    private final PosScreen pos = new PosScreen();
    private final ProductsScreen products = new ProductsScreen();
    private final InventoryScreen inventory = new InventoryScreen();
    private final TransactionsScreen transactions = new TransactionsScreen();

  public Main() {
    super("Stock Bite - Canteen Management System");

    setUndecorated(true);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    setExtendedState(JFrame.MAXIMIZED_BOTH);

        // header
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, Style.PEACH, getWidth() * 0.6f, 0, Style.HEADER));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 5, 0, Style.MINT),
                BorderFactory.createEmptyBorder(Style.LG + 4, Style.XXL, Style.LG + 4, Style.XXL)));
        JLabel title = new JLabel("Stock Bite");
        title.setFont(Style.TITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("School Canteen Management System");
        sub.setFont(Style.BASE.deriveFont(14f));
        sub.setForeground(Style.CREAM);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(title);
        text.add(Box.createVerticalStrut(Style.XS));
        text.add(sub);
        header.add(text, BorderLayout.WEST);

        JLabel date = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        date.setFont(Style.BOLD.deriveFont(14f));
        date.setForeground(Color.WHITE);
        JButton logout = Style.button("Logout", Style.CREAM);
        logout.addActionListener(e -> logout());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, Style.LG, 0));
        right.setOpaque(false);
        right.add(date);
        right.add(logout);
        header.add(right, BorderLayout.EAST);

        // tabs
        JTabbedPane tabs = new JTabbedPane();
        tabs.setUI(new Style.FlatTabsUI());
        tabs.setOpaque(false);
        getContentPane().setBackground(Style.BG);
        tabs.setFont(Style.BOLD.deriveFont(14f));
        tabs.setBackground(Style.MINT);
        tabs.setForeground(Style.DARK);
        tabs.addTab("Point of Sale", pos);
        tabs.addTab("Products", products);
        tabs.addTab("Inventory", inventory);
        tabs.addTab("Transactions", transactions);
        tabs.addChangeListener(e -> {          // reload the data when you open a tab
            Component c = tabs.getSelectedComponent();
            if (c == pos) pos.refresh();
            else if (c == products) products.refresh();
            else if (c == inventory) inventory.refresh();
            else if (c == transactions) transactions.refresh();
        });

        setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
        add(tabs, BorderLayout.CENTER);
    }

    /** Asks to log out, then shows the login again; a new main window opens after a successful login. */
    private void logout() {
        if (!Style.confirm(this, "Log out?", "You will return to the login screen.\nAny unfinished order will be cleared.", "Log out")) return;
        setVisible(false);
        LoginScreen login = new LoginScreen(null);
        login.setVisible(true);
        if (login.isSuccess()) {
            dispose();
            new Main().setVisible(true);
        } else {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) { }

            // use the Inter font everywhere
            for (Object key : Collections.list(UIManager.getDefaults().keys())) {
                if (key.toString().endsWith(".font")) UIManager.put(key, Style.BASE);
            }

            // colors and tab spacing
            Color dark = Style.DARK;
            UIManager.put("Panel.background", Style.BG);
            UIManager.put("OptionPane.background", Color.WHITE);
            UIManager.put("OptionPane.messageForeground", dark);
            UIManager.put("TabbedPane.selected", Style.ORANGE);
            UIManager.put("TabbedPane.contentAreaColor", Style.MINT_DK);
            UIManager.put("TabbedPane.light", Style.MINT);
            UIManager.put("TabbedPane.highlight", Style.MINT);
            UIManager.put("TabbedPane.shadow", Style.MINT_DK);
            UIManager.put("TabbedPane.darkShadow", Style.MINT_DK);
            UIManager.put("TabbedPane.focus", Style.ORANGE);
            UIManager.put("TabbedPane.borderHightlightColor", Style.MINT);
            UIManager.put("TextField.selectionBackground", Style.MINT);
            UIManager.put("TextField.selectionForeground", dark);
            UIManager.put("ComboBox.selectionBackground", Style.MINT);
            UIManager.put("ComboBox.selectionForeground", dark);
            UIManager.put("TabbedPane.tabInsets", new Insets(12, 28, 12, 28));
            UIManager.put("TabbedPane.tabAreaInsets", new Insets(10, 24, 0, 24));
            UIManager.put("TabbedPane.contentBorderInsets", new Insets(2, 0, 0, 0));

            LoginScreen login = new LoginScreen(null);
            login.setVisible(true);
            if (!login.isSuccess()) System.exit(0);
            new Main().setVisible(true);
        });
    }
}
