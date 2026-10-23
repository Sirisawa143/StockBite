package stockbite.ui;

import stockbite.dao.ProductDAO;
import stockbite.model.Product;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;

/** Inventory Management: view stock levels, add stock, and see the stock-in history. */
public class InventoryPanel extends JPanel {
    public static final int LOW_STOCK = 10;

    private final ProductDAO dao = new ProductDAO();
    private final DefaultTableModel model = Theme.model("ID", "Product Name", "Category", "Stock", "Status");
    private final DefaultTableModel logModel = Theme.model("Date", "Product", "Added");
    private final JTable table = new JTable(model);
    private final JTable logTable = new JTable(logModel);
    private final JTextField txtSearch = new JTextField();
    private final JLabel lblSelected = new JLabel("No product selected");
    private final JSpinner spQty = new JSpinner(new SpinnerNumberModel(1, 1, 100000, 1));

    public InventoryPanel() {
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(Theme.XL, Theme.XL, Theme.XL, Theme.XL));
        Theme.styleField(txtSearch);
        Theme.styleField(spQty, 110);

        JPanel search = new JPanel(new BorderLayout(Theme.MD, 0));
        search.setOpaque(false);
        search.add(Theme.label("Search"), BorderLayout.WEST);
        search.add(txtSearch, BorderLayout.CENTER);

        Theme.styleTable(table);
        Theme.styleTable(logTable);
        Theme.widths(table, 60, 240, 120, 80, 120);
        Theme.widths(logTable, 200, 150, 70);
        logTable.setPreferredScrollableViewportSize(new Dimension(450, 120));
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        table.getColumnModel().getColumn(4).setCellRenderer(new StatusRenderer());

        JPanel stockIn = Theme.card(Theme.LG);
        stockIn.setLayout(new FlowLayout(FlowLayout.LEFT, Theme.MD, 0));
        lblSelected.setFont(Theme.BOLD);
        lblSelected.setForeground(Theme.DARK);
        lblSelected.setPreferredSize(new Dimension(300, Theme.BUTTON_H));
        JButton add = Theme.button("Add Stock", Theme.GREEN);
        add.addActionListener(e -> addStock());
        stockIn.add(lblSelected);
        stockIn.add(Theme.label("Quantity"));
        stockIn.add(spQty);
        stockIn.add(add);

        JPanel top = new JPanel(new BorderLayout(0, Theme.LG));
        top.setOpaque(false);
        top.add(search, BorderLayout.NORTH);
        top.add(Theme.scroll(table), BorderLayout.CENTER);
        top.add(stockIn, BorderLayout.SOUTH);

        JPanel bottom = new JPanel(new BorderLayout(0, Theme.MD));
        bottom.setOpaque(false);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        JPanel historyHead = new JPanel(new BorderLayout());
        historyHead.setOpaque(false);
        historyHead.setPreferredSize(new Dimension(10, Theme.FIELD_H));
        historyHead.add(Theme.heading("Stock-In History"), BorderLayout.WEST);
        bottom.add(historyHead, BorderLayout.NORTH);
        bottom.add(Theme.scroll(logTable), BorderLayout.CENTER);

        bottom.setPreferredSize(new Dimension(450, 10));
        setLayout(new BorderLayout(Theme.XL, 0));
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
            for (Product p : dao.search(txtSearch.getText())) {
                String status = p.getStock() == 0 ? "Out of Stock" : p.getStock() <= LOW_STOCK ? "Low Stock" : "In Stock";
                model.addRow(new Object[]{p.getId(), p.getName(), p.getCategory(), p.getStock(), status});
            }
            for (int i = 0; i < model.getRowCount() && keep >= 0; i++) {
                if ((Integer) model.getValueAt(i, 0) == keep) { table.setRowSelectionInterval(i, i); break; }
            }
            logModel.setRowCount(0);
            for (Object[] row : dao.getStockLog()) logModel.addRow(row);
        } catch (SQLException e) {
            Theme.error(this, e);
        }
    }

    private void addStock() {
        int r = table.getSelectedRow();
        if (r < 0) { Theme.warn(this, "Select a product first."); return; }
        int id = (Integer) model.getValueAt(r, 0);
        int qty = (Integer) spQty.getValue();
        try {
            dao.addStock(id, qty);
            spQty.setValue(1);
            refresh();
            Theme.info(this, "Added " + qty + " to stock.");
        } catch (SQLException e) { Theme.error(this, e); }
    }

    private static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean focus, int r, int c) {
            Component comp = super.getTableCellRendererComponent(t, v, sel, focus, r, c);
            String s = String.valueOf(v);
            setBorder(BorderFactory.createEmptyBorder(0, Theme.MD + 2, 0, Theme.MD + 2));
            setHorizontalAlignment(SwingConstants.LEFT);
            comp.setForeground(s.equals("Out of Stock") ? Theme.TEXT_RED : s.equals("Low Stock") ? Theme.TEXT_ORANGE : Theme.TEXT_GREEN);
            comp.setFont(Theme.BOLD);
            comp.setBackground(sel ? Theme.SELECT : (r % 2 == 0 ? Theme.WHITE : Theme.STRIPE));
            return comp;
        }
    }
}
