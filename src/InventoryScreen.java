import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

/** InventoryScreen.java - the Inventory tab: see stock levels, add stock, and view the stock-in history. */
public class InventoryScreen extends JPanel {
    public static final int LOW_STOCK = 10;
    private final DefaultTableModel model = Style.model("ID", "Product Name", "Category", "Stock", "Status");
    private final DefaultTableModel logModel = Style.model("Date", "Product", "Added");
    private final JTable table = new JTable(model);
    private final JTable logTable = new JTable(logModel);
    private final JTextField txtSearch = new JTextField();
    private final JLabel lblSelected = new JLabel("No product selected");
    private final JLabel lblBanner = new JLabel(" ");
    private final JSpinner spQty = new Style.RoundSpinner(new SpinnerNumberModel(1, 1, 100000, 1));

    public InventoryScreen() {
        setBackground(Style.BG);
        setBorder(BorderFactory.createEmptyBorder(Style.XL, Style.XL, Style.XL, Style.XL));
        Style.styleField(txtSearch);
        Style.styleField(spQty, 110);

        JPanel search = new JPanel(new BorderLayout(Style.MD, 0));
        search.setOpaque(false);
        search.add(Style.label("Search"), BorderLayout.WEST);
        search.add(txtSearch, BorderLayout.CENTER);

        Style.styleTable(table);
        Style.styleTable(logTable);
        Style.widths(table, 60, 240, 120, 80, 120);
        Style.widths(logTable, 200, 150, 70);
        logTable.setPreferredScrollableViewportSize(new Dimension(450, 120));
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());

        JPanel stockIn = Style.card(Style.LG);
        stockIn.setLayout(new FlowLayout(FlowLayout.LEFT, Style.MD, 0));
        lblSelected.setFont(Style.BOLD);
        lblSelected.setForeground(Style.DARK);
        lblSelected.setPreferredSize(new Dimension(300, Style.BUTTON_H));
        JButton add = Style.button("Add Stock", Style.GREEN);
        add.addActionListener(e -> addStock());
        stockIn.add(lblSelected);
        stockIn.add(Style.label("Quantity"));
        stockIn.add(spQty);
        stockIn.add(add);

        lblBanner.setFont(Style.BOLD);
        JPanel banner = Style.card(Style.MD);
        banner.setLayout(new BorderLayout());
        banner.add(lblBanner, BorderLayout.CENTER);

        JPanel north = new JPanel(new BorderLayout(0, Style.LG));
        north.setOpaque(false);
        north.add(banner, BorderLayout.NORTH);
        north.add(search, BorderLayout.CENTER);

        JPanel top = new JPanel(new BorderLayout(0, Style.LG));
        top.setOpaque(false);
        top.add(north, BorderLayout.NORTH);
        top.add(Style.scroll(table), BorderLayout.CENTER);
        top.add(stockIn, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new BorderLayout(0, Style.MD));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        JPanel historyHead = new JPanel(new BorderLayout());
        historyHead.setOpaque(false);
        historyHead.setPreferredSize(new Dimension(10, Style.FIELD_H));
        historyHead.add(Style.heading("Stock-In History"), BorderLayout.WEST);
        bottom.add(historyHead, BorderLayout.NORTH);
        bottom.add(Style.scroll(logTable), BorderLayout.CENTER);

        bottom.setPreferredSize(new Dimension(450, 10));
        setLayout(new BorderLayout(Style.XL, 0));
        add(top, BorderLayout.CENTER);
        add(bottom, BorderLayout.EAST);

        table.getSelectionModel().addListSelectionListener(e -> {
            int r = table.getSelectedRow();
            lblSelected.setText(r < 0 ? "No product selected" : "Selected: " + model.getValueAt(r, 1));
        });
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        refresh();
    }

    public void refresh() {
        try {
            int keep = table.getSelectedRow() >= 0 ? (Integer) model.getValueAt(table.getSelectedRow(), 0) : -1;
            model.setRowCount(0);
            for (Product p : Database.searchProducts(txtSearch.getText())) {
                String status = p.getStock() == 0 ? "Out of Stock" : p.getStock() <= LOW_STOCK ? "Low Stock" : "In Stock";
                model.addRow(new Object[]{p.getId(), p.getName(), p.getCategory(), p.getStock(), status});
            }
            for (int i = 0; i < model.getRowCount() && keep >= 0; i++) {
                if ((Integer) model.getValueAt(i, 0) == keep) { table.setRowSelectionInterval(i, i); break; }
            }
            logModel.setRowCount(0);
            for (Object[] row : Database.getStockLog()) logModel.addRow(row);
            updateBanner();
        } catch (SQLException e) {
            Style.error(this, e);
        }
    }

    /** Counts low and out-of-stock products across ALL products (not only the searched ones). */
    private void updateBanner() throws SQLException {
        int low = 0, out = 0;
        for (Product p : Database.searchProducts("")) {
            if (p.getStock() == 0) out++;
            else if (p.getStock() <= LOW_STOCK) low++;
        }
        StringBuilder sb = new StringBuilder();
        if (low > 0) sb.append(low).append(low == 1 ? " item" : " items").append(" low on stock");
        if (out > 0) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(out).append(out == 1 ? " item" : " items").append(" out of stock");
        }
        if (sb.length() == 0) sb.append("All items well stocked");
        lblBanner.setText(sb.toString());
        lblBanner.setForeground(out > 0 ? Style.TEXT_RED : low > 0 ? Style.TEXT_ORANGE : Style.TEXT_GREEN);
    }

    private void addStock() {
        int r = table.getSelectedRow();
        if (r < 0) { Style.warn(this, "Select a product first."); return; }
        int id = (Integer) model.getValueAt(r, 0);
        int qty = (Integer) spQty.getValue();
        try {
            Database.addStock(id, qty);
            spQty.setValue(1);
            refresh();
            Style.info(this, "Added " + qty + " to stock.");
        } catch (SQLException e) { Style.error(this, e); }
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, sel, focus, r, c);
            String s = String.valueOf(v);
            setBorder(BorderFactory.createEmptyBorder(0, Style.MD + 2, 0, Style.MD + 2));
            setHorizontalAlignment(SwingConstants.LEFT);
            comp.setForeground(s.equals("Out of Stock") ? Style.TEXT_RED : s.equals("Low Stock") ? Style.TEXT_ORANGE : Style.TEXT_GREEN);
            comp.setFont(Style.BOLD);
            comp.setBackground(sel ? Style.SELECT : (r % 2 == 0 ? Style.WHITE : Style.STRIPE));
            return comp;
        }
    }
}
