package stockbite.ui;

import stockbite.model.CartItem;
import stockbite.model.SaleTransaction;
import stockbite.util.Format;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/** Shows a simple receipt: items, quantities, prices, total, cash received, and change. */
public class ReceiptDialog extends JDialog {
    private static final int W = 44;

    public ReceiptDialog(Window owner, SaleTransaction t) {
        super(owner, "Receipt #" + t.getId(), ModalityType.APPLICATION_MODAL);
        String text = build(t);

        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setMargin(new Insets(16, 20, 16, 20));

        JButton save = Theme.button("Save as .txt", Theme.GRAY);
        save.addActionListener(e -> saveToFile(t.getId(), text));
        JButton close = Theme.button("Close", Theme.PRIMARY);
        close.addActionListener(e -> dispose());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        buttons.add(save);
        buttons.add(close);

        setLayout(new BorderLayout());
        add(new JScrollPane(area), BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        setSize(440, 520);
        setLocationRelativeTo(owner);
    }

    public static String build(SaleTransaction t) {
        StringBuilder sb = new StringBuilder();
        sb.append(center("STOCK BITE")).append('\n');
        sb.append(center("School Canteen")).append('\n');
        sb.append(center("OFFICIAL RECEIPT")).append("\n\n");
        sb.append("Receipt No.: ").append(String.format("%06d", t.getId())).append('\n');
        sb.append("Date       : ").append(Format.date(t.getDate())).append('\n');
        sb.append("-".repeat(W)).append('\n');
        sb.append(String.format("%-18s %3s %9s %10s%n", "Item", "Qty", "Price", "Amount"));
        sb.append("-".repeat(W)).append('\n');
        for (CartItem i : t.getItems()) {
            String name = i.getName().length() > 18 ? i.getName().substring(0, 17) + "." : i.getName();
            sb.append(String.format("%-18s %3d %9s %10s%n", name, i.getQuantity(),
                    Format.money(i.getPrice()), Format.money(i.getSubtotal())));
        }
        sb.append("-".repeat(W)).append('\n');
        sb.append(line("TOTAL", t.getTotal()));
        sb.append(line("CASH RECEIVED", t.getCash()));
        sb.append(line("CHANGE", t.getChange()));
        sb.append("-".repeat(W)).append("\n\n");
        sb.append(center("Thank you! Please come again.")).append('\n');
        return sb.toString();
    }

    private static String line(String label, java.math.BigDecimal v) {
        return String.format("%-22s %21s%n", label, "PHP " + Format.money(v));
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
            Theme.info(this, "Saved to " + f.getAbsolutePath());
        } catch (IOException ex) {
            Theme.warn(this, "Could not save receipt: " + ex.getMessage());
        }
    }
}
