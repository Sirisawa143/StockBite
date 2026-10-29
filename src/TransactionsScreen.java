import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** TransactionsScreen.java - the Transactions tab: list of completed sales, with receipts. */
public class TransactionsScreen extends JPanel {
    private final DefaultTableModel model = Style.model("Receipt No.", "Date", "Total", "Cash Received", "Change");
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField txtSearch = new JTextField();
    private final JLabel lblSummary = new JLabel(" ");
    private final JComboBox<String> cbPeriod =
            new Style.RoundCombo<>(new String[]{"Today", "Last 7 Days", "This Month", "All"});
    private List<Sale> loaded = new ArrayList<>();
    private final Map<Integer, Sale> byId = new HashMap<>();

    public TransactionsScreen() {
        setLayout(new BorderLayout(0, Style.LG));
        setBackground(Style.BG);
        setBorder(BorderFactory.createEmptyBorder(Style.XL, Style.XL, Style.XL, Style.XL));
        Style.styleField(txtSearch);
        txtSearch.setToolTipText("Search by receipt number or date");
        cbPeriod.setSelectedItem("All");
        cbPeriod.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                    boolean sel, boolean focus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, sel, focus);
                l.setBorder(BorderFactory.createEmptyBorder(4, Style.MD, 4, Style.MD));
                return l;
            }
        });
        Style.styleField(cbPeriod, 170);

        JPanel search = new JPanel(new BorderLayout(Style.MD, 0));
        search.setOpaque(false);
        search.add(Style.label("Search (receipt no. or date)"), BorderLayout.WEST);
        search.add(txtSearch, BorderLayout.CENTER);
        search.add(cbPeriod, BorderLayout.EAST);
        add(search, BorderLayout.NORTH);

        Style.styleTable(table);
        table.setRowSorter(sorter);
        sorter.setComparator(0, (a, b) -> Integer.compare((Integer) a, (Integer) b));
        // show the receipt number with 6 digits (000012), the same as on the printed receipt
        final TableCellRenderer base = table.getDefaultRenderer(Number.class);
        table.getColumnModel().getColumn(0).setCellRenderer((t, v, sel, foc, r, c) ->
                base.getTableCellRendererComponent(t, v instanceof Integer ? receiptNo((Integer) v) : v, sel, foc, r, c));
        Style.widths(table, 110, 230, 130, 150, 130);
        table.getColumnModel().getColumn(0).setMaxWidth(130);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) viewReceipt(); }
        });
        add(Style.scroll(table), BorderLayout.CENTER);

        JButton view = Style.button("View Receipt", Style.PRIMARY);
        JButton reload = Style.button("Refresh", Style.GRAY);
        view.addActionListener(e -> viewReceipt());
        reload.addActionListener(e -> refresh());
        lblSummary.setFont(Style.BOLD);
        lblSummary.setForeground(Style.DARK);

        JPanel south = Style.card(Style.LG);
        south.setLayout(new BorderLayout());
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, Style.MD, 0));
        btns.setOpaque(false);
        btns.add(view);
        btns.add(reload);
        south.add(btns, BorderLayout.WEST);
        south.add(lblSummary, BorderLayout.EAST);
        add(south, BorderLayout.SOUTH);

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        });
        cbPeriod.addActionListener(e -> applyFilter());
        refresh();
    }

    private static String receiptNo(int id) { return String.format("%06d", id); }

    /** True if the sale falls inside the chosen period (Today, Last 7 Days, This Month, All). */
    private boolean inPeriod(LocalDateTime d) {
        String period = String.valueOf(cbPeriod.getSelectedItem());
        LocalDate today = LocalDate.now();
        LocalDate day = d.toLocalDate();
        switch (period) {
            case "Today":       return day.equals(today);
            case "Last 7 Days": return !day.isBefore(today.minusDays(6)) && !day.isAfter(today);
            case "This Month":  return day.getYear() == today.getYear() && day.getMonth() == today.getMonth();
            default:            return true;
        }
    }

    private void applyFilter() {
        final String q = txtSearch.getText().trim().toLowerCase();
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                int id = (Integer) entry.getValue(0);
                Sale sale = byId.get(id);
                if (sale != null && !inPeriod(sale.getDate())) return false;
                if (q.isEmpty()) return true;
                return receiptNo(id).contains(q)                      // "000012" or part of it
                        || String.valueOf(id).equals(q)               // "12"
                        || entry.getStringValue(1).toLowerCase().contains(q);   // date text
            }
        });
        updateSummary();
    }

    private void updateSummary() {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < table.getRowCount(); i++) {
            Sale t = byId.get((Integer) table.getValueAt(i, 0));
            if (t != null) sum = sum.add(t.getTotal());
        }
        lblSummary.setText("Transactions: " + table.getRowCount() + "     Total Sales: " + Style.peso(sum));
    }

    public void refresh() {
        try {
            loaded = Database.getTransactions();
            byId.clear();
            for (Sale t : loaded) byId.put(t.getId(), t);
            model.setRowCount(0);
            for (Sale t : loaded) {
                model.addRow(new Object[]{t.getId(), Style.date(t.getDate()), Style.peso(t.getTotal()),
                        Style.peso(t.getCash()), Style.peso(t.getChange())});
            }
            applyFilter();
        } catch (SQLException e) {
            Style.error(this, e);
        }
    }

    private void viewReceipt() {
        int r = table.getSelectedRow();
        if (r < 0) { Style.warn(this, "Select a transaction first."); return; }
        int id = (Integer) model.getValueAt(table.convertRowIndexToModel(r), 0);
        try {
            for (Sale t : loaded) {
                if (t.getId() == id) {
                    t.setItems(Database.getTransactionItems(id));
                    new Receipt(SwingUtilities.getWindowAncestor(this), t).setVisible(true);
                    return;
                }
            }
        } catch (SQLException e) { Style.error(this, e); }
    }
}
