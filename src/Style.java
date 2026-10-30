import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicArrowButton;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicSpinnerUI;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

public final class Style {

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

    public static final int XS = 4, SM = 8, MD = 12, LG = 16, XL = 24, XXL = 32;
    public static final int FIELD_H = 40;
    public static final int BUTTON_H = 40;

    public static final Font BASE;
    public static final Font BOLD;
    public static final Font SMALL;
    public static final Font HEADING;
    public static final Font TITLE;
    public static final Font BIG;
    public static final Font INPUT_BIG;

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

    private Style() {}

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
        try (InputStream in = Style.class.getResourceAsStream("/fonts/" + file)) {
            if (in != null) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, in);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
                return font;
            }
        } catch (Exception ignored) { }
        return new Font(fallbackName, fallbackStyle, 13);
    }

    public static JButton button(String text, Color color) {
        final Color base = color;
        final Color hover = shade(color, 18);
        final Color pressed = shade(color, 36);
        JButton b = new JButton(text) {
            private boolean over = false;
            @Override public Dimension getPreferredSize() {
                Dimension d = super.getPreferredSize();
                return new Dimension(d.width, BUTTON_H);
            }
            @Override public Dimension getMinimumSize() {
                return getPreferredSize();
            }
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
        b.setBorder(BorderFactory.createEmptyBorder(0, LG, 0, LG));
        return b;
    }

    private static Color shade(Color c, int amount) {
        return new Color(Math.max(0, c.getRed() - amount), Math.max(0, c.getGreen() - amount), Math.max(0, c.getBlue() - amount));
    }

    public static JPanel card(int padding) {
        return new RoundedPanel(padding);
    }

    public static void styleField(JComponent c) { styleField(c, -1); }

    public static void styleField(JComponent c, int width) {
        c.setFont(BASE);
        c.setForeground(DARK);
        JComponent focusTarget = c;
        if (c instanceof JTextField) {
            c.setBackground(WHITE);
            c.setOpaque(true);
            c.setBorder(new RoundedBorder(10, MD + 1));
        } else if (c instanceof JSpinner) {
            JComponent editor = ((JSpinner) c).getEditor();
            if (editor instanceof JSpinner.DefaultEditor) {
                JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
                tf.setFont(BASE);
                tf.setBorder(BorderFactory.createEmptyBorder(0, MD, 0, MD));
                tf.setHorizontalAlignment(SwingConstants.LEFT);
                focusTarget = tf;
            }
            c.setBorder(new RoundedBorder(10, 1));
        } else if (c instanceof JComboBox) {
            c.setBackground(WHITE);
            JComboBox<?> cb = (JComboBox<?>) c;
            if (cb.isEditable() && cb.getEditor().getEditorComponent() instanceof JComponent) {
                JComponent ed = (JComponent) cb.getEditor().getEditorComponent();
                ed.setFont(BASE);
                ed.setBorder(BorderFactory.createEmptyBorder(0, MD, 0, MD));
                focusTarget = ed;
            }
            c.setBorder(new RoundedBorder(10, 1));
        }

        final JComponent box = c;
        FocusAdapter repaint = new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) { box.repaint(); }
            @Override public void focusLost(FocusEvent e) { box.repaint(); }
        };
        focusTarget.addFocusListener(repaint);
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

    public static void widths(JTable t, int... w) {
        for (int i = 0; i < w.length && i < t.getColumnCount(); i++) {
            t.getColumnModel().getColumn(i).setPreferredWidth(w[i]);
        }
    }

    public static JComponent scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getViewport().setBackground(WHITE);
        sp.getVerticalScrollBar().setUI(new SlimScrollBarUI());
        sp.getHorizontalScrollBar().setUI(new SlimScrollBarUI());
        sp.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        sp.getHorizontalScrollBar().setPreferredSize(new Dimension(0, 12));
        sp.getVerticalScrollBar().setUnitIncrement(20);
        RoundedPanel card = new RoundedPanel(1);
        card.setLayout(new BorderLayout());
        card.add(sp, BorderLayout.CENTER);
        return card;
    }

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

    public static JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setFont(BOLD);
        l.setForeground(DARK);
        return l;
    }

    public static JLabel heading(String text) {
        JLabel l = new JLabel(text);
        l.setFont(HEADING);
        l.setForeground(DARK);
        return l;
    }

    public static JLabel hint(String text) {
        JLabel l = new JLabel(text);
        l.setFont(SMALL);
        l.setForeground(MUTED);
        return l;
    }

    public static final java.time.format.DateTimeFormatter DATE_TIME =
            java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");

    public static String money(java.math.BigDecimal v) { return String.format("%,.2f", v); }
    public static String peso(java.math.BigDecimal v) { return "\u20B1" + money(v); }
    public static String date(java.time.LocalDateTime d) { return d == null ? "" : d.format(DATE_TIME); }

    public static java.math.BigDecimal parseMoney(String s) {
        return new java.math.BigDecimal(s.trim().replace(",", ""));
    }

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

    public static void info(Component parent, String msg) {
        Notify.toast(parent, Notify.Kind.SUCCESS, "Done", msg);
    }

    public static void warn(Component parent, String msg) {
        Notify.dialog(parent, Notify.Kind.WARNING, "Please check", msg, "OK", null, ORANGE);
    }

    public static boolean confirm(Component parent, String title, String msg, String confirmText) {
        return Notify.dialog(parent, Notify.Kind.WARNING, title, msg, confirmText, "Cancel", CORAL);
    }

    private static final int SHADOW = 5;

    public static class RoundedPanel extends JPanel {
        private static final int RADIUS = 16;

        public RoundedPanel(int padding) {
            setOpaque(false);
            setBackground(WHITE);
            setBorder(BorderFactory.createEmptyBorder(padding, padding, padding + SHADOW, padding));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight() - SHADOW;
            for (int i = SHADOW; i >= 1; i--) {
                g2.setColor(new Color(31, 70, 58, 11));
                g2.fillRoundRect(0, i - 1, w, h, RADIUS, RADIUS);
            }
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, w, h, RADIUS, RADIUS);
            g2.setColor(BORDER);
            g2.drawRoundRect(0, 0, w - 1, h - 1, RADIUS, RADIUS);
            g2.dispose();
        }

        @Override protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.clip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight() - SHADOW, RADIUS, RADIUS));
            super.paintChildren(g2);
            g2.dispose();
        }
    }

    static class RoundedBorder extends AbstractBorder {
        private final int radius, padding;

        RoundedBorder(int radius, int padding) { this.radius = radius; this.padding = padding; }

        @Override public Insets getBorderInsets(Component c) { return new Insets(2, padding + 1, 2, padding + 1); }

        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color behind = c.getParent() != null ? c.getParent().getBackground() : BG;
            RoundRectangle2D.Float shape = new RoundRectangle2D.Float(x + 0.5f, y + 0.5f, w - 1, h - 1, radius, radius);
            Area corners = new Area(new Rectangle2D.Float(x, y, w, h));
            corners.subtract(new Area(shape));
            g2.setColor(behind);
            g2.fill(corners);
            Component owner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getPermanentFocusOwner();
            boolean focused = owner != null && SwingUtilities.isDescendingFrom(owner, c);
            g2.setColor(focused ? ORANGE : BORDER);
            g2.setStroke(new BasicStroke(focused ? 1.8f : 1.2f));
            g2.draw(shape);
            g2.dispose();
        }
    }

    private static BasicArrowButton arrow(int direction) {
        BasicArrowButton b = new BasicArrowButton(direction, MIST, MINT_DK, DARK, MIST);
        b.setBorder(BorderFactory.createEmptyBorder());
        b.setPreferredSize(new Dimension(28, 20));
        return b;
    }

    public static class RoundSpinner extends JSpinner {
        public RoundSpinner(SpinnerModel model) { super(model); }

        @Override public void updateUI() {
            super.updateUI();
            setUI(new BasicSpinnerUI() {
                @Override protected Component createNextButton() {
                    JButton b = arrow(SwingConstants.NORTH);
                    installNextButtonListeners(b);
                    return b;
                }
                @Override protected Component createPreviousButton() {
                    JButton b = arrow(SwingConstants.SOUTH);
                    installPreviousButtonListeners(b);
                    return b;
                }
            });
        }

        @Override protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.clip(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 10, 10));
            super.paintChildren(g2);
            g2.dispose();
        }
    }

    public static class RoundCombo<E> extends JComboBox<E> {
        public RoundCombo(E[] items) { super(items); }

        @Override public void updateUI() {
            super.updateUI();
            setUI(new BasicComboBoxUI() {
                @Override protected JButton createArrowButton() {
                    BasicArrowButton b = arrow(SwingConstants.SOUTH);
                    b.setPreferredSize(new Dimension(32, 20));
                    return b;
                }
                @Override protected void installDefaults() {
                    super.installDefaults();
                    squareButton = false;
                }
            });
        }

        @Override protected void paintChildren(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.clip(new RoundRectangle2D.Float(1, 1, getWidth() - 2, getHeight() - 2, 10, 10));
            super.paintChildren(g2);
            g2.dispose();
        }
    }

    public static class FlatTabsUI extends BasicTabbedPaneUI {
        @Override protected void installDefaults() {
            super.installDefaults();
            selectedTabPadInsets = new Insets(0, 0, 0, 0);
            tabRunOverlay = 0;
        }

        @Override protected void paintTabBackground(Graphics g, int placement, int index, int x, int y, int w, int h, boolean selected) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.clip(new Rectangle(x, y, w, selected ? h + 4 : h));
            g2.setColor(selected ? ORANGE : MINT);
            g2.fillRoundRect(x + 2, y + 3, w - 4, h + 16, 18, 18);
            g2.dispose();
        }

        @Override protected int getTabLabelShiftX(int placement, int index, boolean selected) { return 0; }
        @Override protected int getTabLabelShiftY(int placement, int index, boolean selected) { return 0; }
        @Override protected void paintTabBorder(Graphics g, int placement, int index, int x, int y, int w, int h, boolean selected) { }
        @Override protected void paintFocusIndicator(Graphics g, int placement, Rectangle[] rects, int index, Rectangle iconRect, Rectangle textRect, boolean selected) { }
        @Override protected void paintContentBorderTopEdge(Graphics g, int placement, int selected, int x, int y, int w, int h) {
            g.setColor(MINT);
            g.fillRect(x, y, w, 3);
        }
        @Override protected void paintContentBorderLeftEdge(Graphics g, int placement, int selected, int x, int y, int w, int h) { }
        @Override protected void paintContentBorderBottomEdge(Graphics g, int placement, int selected, int x, int y, int w, int h) { }
        @Override protected void paintContentBorderRightEdge(Graphics g, int placement, int selected, int x, int y, int w, int h) { }
    }
}
