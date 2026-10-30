import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class Receipt extends JDialog {
    private static final int W = 44;

    public Receipt(Window owner, Sale t) {
        super(owner, "Receipt #" + t.getId(), ModalityType.APPLICATION_MODAL);
        String text = build(t);

        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setMargin(new Insets(Style.XL, Style.XL + 4, Style.XL, Style.XL + 4));
        area.setBackground(new Color(0xFF, 0xFD, 0xF6));
        area.setForeground(Style.DARK);

        JButton save = Style.button("Save as .txt", Style.GRAY);
        save.addActionListener(e -> saveToFile(t.getId(), text));
        JButton close = Style.button("Close", Style.PRIMARY);
        close.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, Style.MD, 0));
        buttons.setBackground(Style.BG);
        buttons.setBorder(BorderFactory.createEmptyBorder(Style.LG, Style.XL, Style.LG, Style.XL));
        buttons.add(save);
        buttons.add(close);

        setLayout(new BorderLayout());
        JComponent sp = Style.scroll(area);
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Style.BG);
        wrap.setBorder(BorderFactory.createEmptyBorder(Style.XL, Style.XL, 0, Style.XL));
        wrap.add(sp, BorderLayout.CENTER);
        add(wrap, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        setSize(500, 600);
        getContentPane().setBackground(Style.BG);
        setLocationRelativeTo(owner);
    }

    public static String build(Sale t) {
        StringBuilder sb = new StringBuilder();
        sb.append(center("STOCK BITE")).append('\n');
        sb.append(center("School Canteen")).append('\n');
        sb.append(center("SALES RECEIPT")).append("\n\n");
        sb.append("Receipt No.: ").append(String.format("%06d", t.getId())).append('\n');
        sb.append("Date       : ").append(Style.date(t.getDate())).append('\n');
        sb.append("-".repeat(W)).append('\n');
        sb.append(String.format("%-18s %3s %9s %10s%n", "Item", "Qty", "Price", "Amount"));
        sb.append("-".repeat(W)).append('\n');
        for (CartItem i : t.getItems()) {
            String name = i.getName().length() > 18 ? i.getName().substring(0, 17) + "." : i.getName();
            sb.append(String.format("%-18s %3d %9s %10s%n", name, i.getQuantity(),
                    Style.money(i.getPrice()), Style.money(i.getSubtotal())));
        }
        sb.append("-".repeat(W)).append('\n');
        sb.append(line("TOTAL", t.getTotal()));
        sb.append(line("CASH RECEIVED", t.getCash()));
        sb.append(line("CHANGE", t.getChange()));
        sb.append("-".repeat(W)).append('\n');
        return sb.toString();
    }

    private static String line(String label, java.math.BigDecimal v) {
        return String.format("%-22s %21s%n", label, "PHP " + Style.money(v));
    }

    private static String center(String s) {
        int pad = Math.max(0, (W - s.length()) / 2);
        return " ".repeat(pad) + s;
    }

    private void saveToFile(int id, String text) {
        try {
            File dir = new File("receipts");
            dir.mkdirs();
            File f = new File(dir, String.format("receipt_%06d.txt", id));
            Files.write(f.toPath(), text.getBytes(StandardCharsets.UTF_8));
            Style.info(this, "Saved to " + f.getAbsolutePath());
        } catch (IOException ex) {
            Style.warn(this, "Could not save receipt: " + ex.getMessage());
        }
    }
}
