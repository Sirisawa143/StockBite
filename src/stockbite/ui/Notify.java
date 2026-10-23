package stockbite.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

/** Styled notifications: toasts for quick feedback and dialogs for warnings, errors, and confirmations. */
public final class Notify {
    public enum Kind { SUCCESS, INFO, WARNING, ERROR }

    private static JWindow currentToast;
    private static Timer currentTimer;

    private Notify() {}

    static Color accent(Kind k) {
        switch (k) {
            case SUCCESS: return Theme.TEXT_GREEN;
            case WARNING: return Theme.ORANGE;
            case ERROR:   return Theme.TEXT_RED;
            default:      return new Color(0x3E, 0x8E, 0x9E);
        }
    }

    private static Window ownerOf(Component parent) {
        if (parent == null) return null;
        return parent instanceof Window ? (Window) parent : SwingUtilities.getWindowAncestor(parent);
    }

    private static String html(String msg, int width) {
        String s = msg.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>");
        return "<html><div style='width:" + width + "px'>" + s + "</div></html>";
    }

    // ---------- Icon badge ----------
    private static class Badge extends JComponent {
        private final Kind kind;
        Badge(Kind kind) {
            this.kind = kind;
            setPreferredSize(new Dimension(44, 44));
        }
        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            Color a = accent(kind);
            g2.setColor(new Color(a.getRed(), a.getGreen(), a.getBlue(), 45));
            g2.fillOval(0, 0, 44, 44);
            g2.setColor(a);
            g2.fillOval(7, 7, 30, 30);
            g2.setColor(Color.WHITE);
            g2.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (kind) {
                case SUCCESS:
                    g2.drawPolyline(new int[]{15, 20, 29}, new int[]{23, 28, 17}, 3);
                    break;
                case ERROR:
                    g2.drawLine(16, 16, 28, 28);
                    g2.drawLine(28, 16, 16, 28);
                    break;
                case WARNING:
                    g2.drawLine(22, 14, 22, 24);
                    g2.fillOval(20, 27, 4, 4);
                    break;
                default:
                    g2.fillOval(20, 13, 4, 4);
                    g2.drawLine(22, 20, 22, 30);
            }
            g2.dispose();
        }
    }

    private static JPanel textBlock(String title, String msg, int width) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel t = new JLabel(title);
        t.setFont(Theme.HEADING.deriveFont(16f));
        t.setForeground(Theme.DARK);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel m = new JLabel(html(msg, width));
        m.setFont(Theme.BASE);
        m.setForeground(new Color(0x4A, 0x4F, 0x4E));
        m.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(t);
        p.add(Box.createVerticalStrut(Theme.XS + 2));
        p.add(m);
        return p;
    }

    // ---------- Toast ----------

    /** Builds the toast card (also used for previews). */
    static JPanel toastCard(Kind kind, String title, String msg) {
        final Color accent = accent(kind);
        JPanel card = new JPanel(new BorderLayout(Theme.LG, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int x = 8, y = 6, w = getWidth() - 16, h = getHeight() - 14;
                for (int i = 6; i >= 1; i--) {   // soft shadow
                    g2.setColor(new Color(0, 0, 0, 7));
                    g2.fillRoundRect(x - i, y - i + 3, w + 2 * i, h + 2 * i, 18 + i, 18 + i);
                }
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(x, y, w, h, 16, 16);
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(x, y, w, h, 16, 16));
                g2.setColor(accent);
                g2.fillRect(x, y, 6, h);
                g2.setClip(null);
                g2.setColor(Theme.BORDER);
                g2.drawRoundRect(x, y, w - 1, h - 1, 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(22, 30, 24, 26));
        card.add(new Badge(kind), BorderLayout.WEST);
        card.add(textBlock(title, msg, 260), BorderLayout.CENTER);
        return card;
    }

    /** Shows a short, non-blocking message in the top-right corner that fades away. */
    public static void toast(Component parent, Kind kind, String title, String msg) {
        Window owner = ownerOf(parent);
        if (owner == null || !owner.isShowing()) return;
        if (currentTimer != null) currentTimer.stop();
        if (currentToast != null) currentToast.dispose();

        final JWindow w = new JWindow(owner);
        w.setFocusableWindowState(false);
        boolean translucent = false;
        try {
            GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
            translucent = gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.PERPIXEL_TRANSLUCENT)
                    && gd.isWindowTranslucencySupported(GraphicsDevice.WindowTranslucency.TRANSLUCENT);
            if (translucent) w.setBackground(new Color(0, 0, 0, 0));
        } catch (Exception ex) { translucent = false; }
        if (!translucent) w.setBackground(Theme.BG);   // no fade; corners blend into the page color
        w.setContentPane(toastCard(kind, title, msg));
        w.pack();
        w.setLocation(owner.getX() + owner.getWidth() - w.getWidth() - 12, owner.getY() + 92);
        currentToast = w;

        final boolean fade = translucent;
        if (fade) { try { w.setOpacity(0f); } catch (Exception ex) { /* fade not supported */ } }
        w.setVisible(true);

        final float[] opacity = {0f};
        final int[] phase = {0};      // 0 = fading in, 1 = holding, 2 = fading out
        final int[] ticks = {0};
        currentTimer = new Timer(25, null);
        currentTimer.addActionListener((ActionEvent e) -> {
            try {
                if (phase[0] == 0) {
                    opacity[0] = Math.min(1f, opacity[0] + 0.12f);
                    if (fade) w.setOpacity(opacity[0]);
                    if (opacity[0] >= 1f) phase[0] = 1;
                } else if (phase[0] == 1) {
                    if (++ticks[0] > 110) phase[0] = 2;      // about 2.7 seconds
                } else {
                    opacity[0] = Math.max(0f, opacity[0] - 0.1f);
                    if (fade) w.setOpacity(opacity[0]);
                    if (opacity[0] <= 0f || !fade) {
                        ((Timer) e.getSource()).stop();
                        w.dispose();
                    }
                }
            } catch (Exception ex) {
                ((Timer) e.getSource()).stop();
                w.dispose();
            }
        });
        currentTimer.start();
    }

    // ---------- Dialog ----------

    /**
     * Shows a modal message. With a cancelText it works as a confirmation and returns true
     * only when the user clicks the main button; otherwise it just shows the message.
     */
    public static boolean dialog(Component parent, Kind kind, String title, String msg,
                                 String okText, String cancelText, Color okColor) {
        Window owner = ownerOf(parent);
        final JDialog d = new JDialog(owner, Dialog.ModalityType.APPLICATION_MODAL);
        d.setUndecorated(true);
        final boolean[] result = {false};

        JPanel body = new JPanel(new BorderLayout(Theme.LG + 4, 0));
        body.setBackground(Color.WHITE);
        body.setBorder(BorderFactory.createEmptyBorder(Theme.XL + 4, Theme.XXL, Theme.LG, Theme.XXL));
        body.add(new Badge(kind), BorderLayout.WEST);
        body.add(textBlock(title, msg, 320), BorderLayout.CENTER);

        JButton ok = Theme.button(okText, okColor);
        ok.addActionListener(e -> { result[0] = true; d.dispose(); });
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Theme.MD, 0));
        buttons.setBackground(Color.WHITE);
        buttons.setBorder(BorderFactory.createEmptyBorder(Theme.SM, Theme.XXL, Theme.XL, Theme.XXL - Theme.MD + Theme.SM));
        if (cancelText != null) {
            JButton cancel = Theme.button(cancelText, Theme.MIST);
            cancel.addActionListener(e -> d.dispose());
            buttons.add(cancel);
        }
        buttons.add(ok);

        JPanel root = new JPanel(new BorderLayout());
        root.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.MINT_DK),
                BorderFactory.createMatteBorder(6, 0, 0, 0, accent(kind))));
        root.add(body, BorderLayout.CENTER);
        root.add(buttons, BorderLayout.SOUTH);
        d.setContentPane(root);

        d.getRootPane().setDefaultButton(ok);
        d.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
        d.getRootPane().getActionMap().put("close", new AbstractAction() {
            @Override public void actionPerformed(ActionEvent e) { d.dispose(); }
        });

        d.pack();
        d.setSize(Math.max(d.getWidth(), 460), d.getHeight());
        d.setLocationRelativeTo(owner);
        d.setVisible(true);
        return result[0];
    }
}
