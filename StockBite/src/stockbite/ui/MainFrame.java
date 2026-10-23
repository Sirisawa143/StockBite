package stockbite.ui;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final ProductPanel products = new ProductPanel();
    private final InventoryPanel inventory = new InventoryPanel();
    private final POSPanel pos = new POSPanel();
    private final TransactionPanel transactions = new TransactionPanel();

    public MainFrame() {
        super("Stock Bite - Canteen Management System");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setMinimumSize(new Dimension(950, 620));
        setLocationRelativeTo(null);

        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, Theme.PEACH, getWidth() * 0.6f, 0, Theme.HEADER));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        JLabel title = new JLabel("Stock Bite");
        title.setFont(Theme.TITLE);
        title.setForeground(Color.WHITE);
        JLabel sub = new JLabel("School Canteen Management System");
        sub.setFont(Theme.BASE);
        sub.setForeground(Theme.CREAM);
        JPanel text = new JPanel(new GridLayout(2, 1));
        text.setOpaque(false);
        text.add(title);
        text.add(sub);
        header.add(text, BorderLayout.WEST);
        header.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 4, 0, Theme.MINT),
                BorderFactory.createEmptyBorder(12, 20, 12, 20)));

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 14));
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
