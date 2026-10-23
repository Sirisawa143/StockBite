package stockbite.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/** Shared colors, fonts, and small UI helpers (UCC AIMS portal shades: orange, mint green, cream). */
public final class Theme {
    // Palette sampled from the UCC AIMS portal
    // Surfaces
    public static final Color WHITE   = Color.WHITE;
    public static final Color BG      = new Color(0xF0, 0xF9, 0xF6);   // very light teal
    public static final Color BORDER  = new Color(0xBF, 0xE0, 0xD6);   // soft teal-mint border
    public static final Color STRIPE  = new Color(0xFF, 0xF9, 0xEA);   // light cream
    public static final Color DARK    = new Color(0x22, 0x22, 0x22);   // text (black)

    // Portal shades
    public static final Color HEADER  = new Color(0xFF, 0x76, 0x01);   // header orange
    public static final Color PEACH   = new Color(0xFB, 0x81, 0x50);   // header gradient start
    public static final Color CREAM   = new Color(0xFF, 0xEC, 0xB2);   // header cream
    public static final Color MINT    = new Color(0xB5, 0xDA, 0xBE);   // nav bar mint green
    public static final Color MINT_DK = new Color(0xA8, 0xCB, 0xB1);   // nav bar bottom edge
    public static final Color ORANGE  = new Color(0xF5, 0x95, 0x06);   // active tab orange
    public static final Color CORAL   = new Color(0xEF, 0x7D, 0x78);   // sign-out red, softened
    public static final Color MIST    = new Color(0xE6, 0xE9, 0xE6);   // neutral gray (welcome bar)
    public static final Color SELECT  = new Color(0xFF, 0xEC, 0xB2);   // cream selection

    // Readable text versions of the accents
    public static final Color TEXT_ORANGE = new Color(0xD9, 0x65, 0x00);
    public static final Color TEXT_GREEN  = new Color(0x2E, 0x8B, 0x6A);
    public static final Color TEXT_RED    = new Color(0xD1, 0x2A, 0x2B);

    // Kept for existing references
    public static final Color PRIMARY = ORANGE;
    public static final Color GREEN   = MINT;
    public static final Color RED     = CORAL;
    public static final Color GRAY    = MIST;

    public static final Font BASE  = new Font("SansSerif", Font.PLAIN, 13);
    public static final Font BOLD  = new Font("SansSerif", Font.BOLD, 13);
    public static final Font TITLE = new Font("SansSerif", Font.BOLD, 22);
    public static final Font BIG   = new Font("SansSerif", Font.BOLD, 26);

    private Theme() {}

    /** Rounded pastel button with dark text and a hover effect. */
    public static JButton button(String text, Color color) {
        final Color base = color;
        final Color hover = color.darker();
        JButton b = new JButton(text) {
            private boolean over = false;
            {
                addMouseListener(new MouseAdapter() {
                    @Override public void mouseEntered(MouseEvent e) { over = true; repaint(); }
                    @Override public void mouseExited(MouseEvent e) { over = false; repaint(); }
                });
            }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c = !isEnabled() ? MIST : (getModel().isPressed() ? hover : (over ? mix(base) : base));
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setFont(BOLD);
        b.setForeground(DARK);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(BorderFactory.createEmptyBorder(9, 18, 9, 18));
        return b;
    }

    private static Color mix(Color c) {
        // slightly deeper than the base color for the hover state
        return new Color(Math.max(0, c.getRed() - 18), Math.max(0, c.getGreen() - 18), Math.max(0, c.getBlue() - 18));
    }

    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    public static void styleTable(JTable t) {
        t.setFont(BASE);
        t.setForeground(DARK);
        t.setBackground(WHITE);
        t.setRowHeight(30);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.setSelectionBackground(SELECT);
        t.setSelectionForeground(DARK);
        t.setGridColor(BORDER);
        t.setShowVerticalLines(false);
        t.setIntercellSpacing(new Dimension(0, 1));
        t.setFillsViewportHeight(true);

        DefaultTableCellRenderer striped = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                Component comp = super.getTableCellRendererComponent(tbl, v, sel, foc, r, c);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
                if (!sel) comp.setBackground(r % 2 == 0 ? WHITE : STRIPE);
                comp.setForeground(DARK);
                return comp;
            }
        };
        t.setDefaultRenderer(Object.class, striped);
        t.setDefaultRenderer(Number.class, striped);

        JTableHeader h = t.getTableHeader();
        h.setFont(BOLD);
        h.setBackground(MINT);
        h.setForeground(DARK);
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(h.getPreferredSize().width, 34));
        DefaultTableCellRenderer head = new DefaultTableCellRenderer();
        head.setBackground(MINT);
        head.setForeground(DARK);
        head.setFont(BOLD);
        head.setHorizontalAlignment(SwingConstants.LEFT);
        head.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        h.setDefaultRenderer(head);

        JScrollPane sp = null;
        Container parentHolder = t.getParent();
        if (parentHolder != null && parentHolder.getParent() instanceof JScrollPane) sp = (JScrollPane) parentHolder.getParent();
        if (sp != null) sp.setBorder(BorderFactory.createLineBorder(BORDER));
    }

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BOLD);
        l.setForeground(DARK);
        return l;
    }

    public static void error(Component parent, Exception e) {
        String msg = e.getMessage() == null ? e.toString() : e.getMessage();
        if (e instanceof java.sql.SQLException && (msg.contains("Communications link failure")
                || msg.contains("No suitable driver") || msg.contains("Unknown database")
                || msg.contains("Access denied"))) {
            msg += "\n\nCheck that MySQL is running in XAMPP, the database is imported "
                 + "(database/stockbite.sql), and the MySQL connector JAR is in the lib folder.";
        }
        JOptionPane.showMessageDialog(parent, msg, "Database Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Stock Bite", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void warn(Component parent, String msg) {
        JOptionPane.showMessageDialog(parent, msg, "Stock Bite", JOptionPane.WARNING_MESSAGE);
    }
}
