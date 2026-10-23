package stockbite.ui;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * Shared colors, fonts, spacing, and small UI helpers.
 * Palette follows the UCC AIMS portal (orange, mint green, cream).
 * Fonts: Lora for headings, Inter for body text (loaded from the /fonts folder).
 */
public final class Theme {
    // ---------- Colors ----------
    public static final Color WHITE   = Color.WHITE;
    public static final Color BG      = new Color(0xF0, 0xF9, 0xF6);
    public static final Color BORDER  = new Color(0xBF, 0xE0, 0xD6);
    public static final Color STRIPE  = new Color(0xFF, 0xF9, 0xEA);
    public static final Color DARK    = new Color(0x22, 0x22, 0x22);
    public static final Color MUTED   = new Color(0x6B, 0x72, 0x70);

    public static final Color HEADER  = new Color(0xFF, 0x76, 0x01);
    public static final Color PEACH   = new Color(0xFB, 0x81, 0x50);
    public static final Color CREAM   = new Color(0xFF, 0xEC, 0xB2);
    public static final Color MINT    = new Color(0xB5, 0xDA, 0xBE);
    public static final Color MINT_DK = new Color(0xA8, 0xCB, 0xB1);
    public static final Color ORANGE  = new Color(0xF5, 0x95, 0x06);
    public static final Color CORAL   = new Color(0xEF, 0x7D, 0x78);
    public static final Color MIST    = new Color(0xE6, 0xE9, 0xE6);
    public static final Color SELECT  = new Color(0xFF, 0xEC, 0xB2);

    public static final Color TEXT_ORANGE = new Color(0xD9, 0x65, 0x00);
    public static final Color TEXT_GREEN  = new Color(0x2E, 0x8B, 0x6A);
    public static final Color TEXT_RED    = new Color(0xD1, 0x2A, 0x2B);

    public static final Color PRIMARY = ORANGE;
    public static final Color GREEN   = MINT;
    public static final Color RED     = CORAL;
    public static final Color GRAY    = MIST;

    // ---------- Spacing scale ----------
    public static final int XS = 4, SM = 8, MD = 12, LG = 16, XL = 24, XXL = 32;
    public static final int FIELD_H = 40;   // height of inputs
    public static final int BUTTON_H = 40;  // height of buttons

    // ---------- Fonts ----------
    public static final Font BASE;      // Inter Regular 13
    public static final Font BOLD;      // Inter SemiBold 13
    public static final Font SMALL;     // Inter Regular 12
    public static final Font HEADING;   // Lora Bold 18
    public static final Font TITLE;     // Lora Bold 28
    public static final Font BIG;       // Inter Bold 28 (amounts)
    public static final Font INPUT_BIG; // Inter SemiBold 18

    static {
        Font interRegular = load("Inter-Regular.ttf", Font.PLAIN, "SansSerif");
        Font interSemi    = load("Inter-SemiBold.ttf", Font.BOLD, "SansSerif");
        Font interBold    = load("Inter-Bold.ttf", Font.BOLD, "SansSerif");
        Font loraBold     = load("Lora-Bold.ttf", Font.BOLD, "Serif");
        BASE = interRegular.deriveFont(13f);
        BOLD = interSemi.deriveFont(13f);
        SMALL = interRegular.deriveFont(12f);
        HEADING = loraBold.deriveFont(18f);
        TITLE = loraBold.deriveFont(28f);
        BIG = interBold.deriveFont(28f);
        INPUT_BIG = interSemi.deriveFont(18f);
    }

    private Theme() {}

    /** Loads a bundled TTF from the fonts folder; falls back to a built-in font if it is missing. */
    private static Font load(String file, int fallbackStyle, String fallbackName) {
        File[] candidates = {
                new File("fonts", file),
                new File(System.getProperty("user.dir"), "fonts/" + file),
                new File("StockBite/fonts", file)
        };
        for (File f : candidates) {
            if (f.isFile()) {
                try (InputStream in = new FileInputStream(f)) {
                    Font font = Font.createFont(Font.TRUETYPE_FONT, in);
                    GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
                    return font;
                } catch (Exception ignored) { }
            }
        }
        try (InputStream in = Theme.class.getResourceAsStream("/fonts/" + file)) {
            if (in != null) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, in);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
                return font;
            }
        } catch (Exception ignored) { }
        return new Font(fallbackName, fallbackStyle, 13);
    }

    // ---------- Components ----------

    /** Rounded button with dark text and hover/pressed states. */
    public static JButton button(String text, Color color) {
        final Color base = color;
        final Color hover = shade(color, 18);
        final Color pressed = shade(color, 36);
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
                Color c = !isEnabled() ? MIST : (getModel().isPressed() ? pressed : (over ? hover : base));
                g2.setColor(c);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
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
        b.setBorder(BorderFactory.createEmptyBorder(0, XL, 0, XL));
        b.setPreferredSize(new Dimension(b.getPreferredSize().width, BUTTON_H));
        b.setMinimumSize(new Dimension(40, BUTTON_H));
        return b;
    }

    private static Color shade(Color c, int amount) {
        return new Color(Math.max(0, c.getRed() - amount), Math.max(0, c.getGreen() - amount), Math.max(0, c.getBlue() - amount));
    }

    /** White card with a soft border and inner padding. */
    public static JPanel card(int padding) {
        JPanel p = new JPanel();
        p.setBackground(WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                BorderFactory.createEmptyBorder(padding, padding, padding, padding)));
        return p;
    }

    /** Styles a text field, spinner, or combo box to the shared input look. */
    public static void styleField(JComponent c) { styleField(c, -1); }

    public static void styleField(JComponent c, int width) {
        c.setFont(BASE);
        c.setForeground(DARK);
        if (c instanceof JTextField) {
            c.setBackground(WHITE);
            c.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER),
                    BorderFactory.createEmptyBorder(0, MD, 0, MD)));
        } else if (c instanceof JSpinner) {
            JComponent editor = ((JSpinner) c).getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
                tf.setFont(BASE);
                tf.setBorder(BorderFactory.createEmptyBorder(0, MD, 0, MD));
                tf.setHorizontalAlignment(SwingConstants.LEFT);
            }
            c.setBorder(BorderFactory.createLineBorder(BORDER));
        } else if (c instanceof JComboBox) {
            c.setBackground(WHITE);
            JComboBox<?> cb = (JComboBox<?>) c;
            if (cb.isEditable() && cb.getEditor().getEditorComponent() instanceof JComponent) {
                JComponent ed = (JComponent) cb.getEditor().getEditorComponent();
                ed.setFont(BASE);
                ed.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER),
                        BorderFactory.createEmptyBorder(0, MD, 0, MD)));
            }
        }
        int w = width > 0 ? width : c.getPreferredSize().width;
        c.setPreferredSize(new Dimension(w, FIELD_H));
        c.setMinimumSize(new Dimension(Math.min(w, 60), FIELD_H));
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
        t.setRowHeight(36);
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
                setBorder(BorderFactory.createEmptyBorder(0, MD + 2, 0, MD + 2));
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
        h.setPreferredSize(new Dimension(h.getPreferredSize().width, 40));
        DefaultTableCellRenderer head = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(tbl, v, sel, foc, r, c);
                setBackground(MINT);
                setForeground(DARK);
                setFont(BOLD);
                setHorizontalAlignment(SwingConstants.LEFT);
                setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 0, MINT_DK),
                        BorderFactory.createEmptyBorder(0, MD + 2, 0, MD + 2)));
                return this;
            }
        };
        h.setDefaultRenderer(head);
    }

    /** Sets relative column widths (first value is the first column). */
    public static void widths(JTable t, int... w) {
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
    }

    /** Scroll pane with the shared border and slim scrollbars. */
    public static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createLineBorder(BORDER));
        sp.getViewport().setBackground(WHITE);
        sp.getVerticalScrollBar().setUI(new SlimScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new SlimScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 12));
        sp.getVerticalScrollBar().setUnitIncrement(20);
        return sp;
    }

    /** Thin, rounded mint scrollbar without arrow buttons. */
    private static class SlimScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override protected JButton createDecreaseButton(int o) { return zero(); }
        @Override protected JButton createIncreaseButton(int o) { return zero(); }
        private JButton zero() {
            JButton b = new JButton();
            b.setPreferredSize(new Dimension(0, 0));
            b.setMinimumSize(new Dimension(0, 0));
            b.setMaximumSize(new Dimension(0, 0));
            return b;
        }
        @Override protected void paintTrack(Graphics g, JComponent c, Rectangle r) {
            g.setColor(BG);
            g.fillRect(r.x, r.y, r.width, r.height);
        }
        @Override protected void paintThumb(Graphics g, JComponent c, Rectangle r) {
            if (r.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(isThumbRollover() ? MINT_DK : MINT);
            g2.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
            g2.dispose();
        }
    }

    /** Body label (Inter SemiBold). */
    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BOLD);
        l.setForeground(DARK);
        return l;
    }

    /** Heading label (Lora Bold). */
    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(HEADING);
        l.setForeground(DARK);
        return l;
    }

    /** Small gray helper text. */
    public static JLabel hint(String text) {
        JLabel l = new JLabel(text);
        l.setFont(SMALL);
        l.setForeground(MUTED);
        return l;
    }

    // ---------- Notifications (see Notify) ----------

    public static void error(Component parent, Exception e) {
        String msg = e.getMessage() == null ? e.toString() : e.getMessage();
        if (e instanceof java.sql.SQLException && (msg.contains("Communications link failure")
                || msg.contains("No suitable driver") || msg.contains("Unknown database")
                || msg.contains("Access denied"))) {
            msg += "\n\nCheck that MySQL is running in XAMPP, the database is imported "
                 + "(database/stockbite.sql), and the MySQL connector JAR is in the lib folder.";
        }
        Notify.dialog(parent, Notify.Kind.ERROR, "Something went wrong", msg, "OK", null, CORAL);
    }

    /** Success toast (non-blocking). */
    public static void info(Component parent, String msg) {
        Notify.toast(parent, Notify.Kind.SUCCESS, "Done", msg);
    }

    /** Warning dialog. */
    public static void warn(Component parent, String msg) {
        Notify.dialog(parent, Notify.Kind.WARNING, "Please check", msg, "OK", null, ORANGE);
    }

    /** Confirmation dialog; returns true if the user confirms. */
    public static boolean confirm(Component parent, String title, String msg, String confirmText) {
        return Notify.dialog(parent, Notify.Kind.WARNING, title, msg, confirmText, "Cancel", CORAL);
    }
}
