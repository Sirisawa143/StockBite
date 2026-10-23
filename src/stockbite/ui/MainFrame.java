package stockbite.ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MainFrame extends JFrame {
    private final ProductPanel products = new ProductPanel();
    private final InventoryPanel inventory = new InventoryPanel();
    private final POSPanel pos = new POSPanel();
    private final TransactionPanel transactions = new TransactionPanel();

    public MainFrame() {
        super("Stock Bite - Canteen Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1240, 740);
        setMinimumSize(new Dimension(1060, 660));
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, Theme.PEACH, getWidth() * 0.6f, 0, Theme.HEADER));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 5, 0, Theme.MINT),
                BorderFactory.createEmptyBorder(Theme.LG + 4, Theme.XXL, Theme.LG + 4, Theme.XXL)));
        JLabel title = new JLabel("Stock Bite");
        title.setFont(Theme.TITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("School Canteen Management System");
        sub.setFont(Theme.BASE.deriveFont(14f));
        sub.setForeground(Theme.CREAM);
        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(title);
        text.add(Box.createVerticalStrut(Theme.XS));
        text.add(sub);
        header.add(text, BorderLayout.WEST);

        JLabel date = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        date.setFont(Theme.BOLD.deriveFont(14f));
        date.setForeground(Color.WHITE);
        header.add(date, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(Theme.BOLD.deriveFont(14f));
        tabs.setBackground(Theme.MINT);
        tabs.setForeground(Theme.DARK);
        tabs.addTab("Point of Sale", pos);
        tabs.addTab("Products", products);
        tabs.addTab("Inventory", inventory);
        tabs.addTab("Transactions", transactions);
        tabs.addChangeListener(e -> {
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
}
