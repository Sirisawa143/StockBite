import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** TransactionsScreen.java - the Transactions tab: list of completed sales, with receipts. */
public class TransactionsScreen extends JPanel {
    private final DefaultTableModel model = Style.model("Receipt No.", "Date", "Total", "Cash Received", "Change");
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField txtSearch = new JTextField();
    private final JLabel lblSummary = new JLabel(" ");
    private List<Sale> loaded = new ArrayList<>();

    public TransactionsScreen() {
        setLayout(new BorderLayout(0, Style.LG));
        setBackground(Style.BG);
        setBorder(BorderFactory.createEmptyBorder(Style.XL, Style.XL, Style.XL, Style.XL));
        Style.styleField(txtSearch);
        txtSearch.setToolTipText("Search by receipt number or date");

        JPanel search = new JPanel(new BorderLayout(Style.MD, 0));
        search.setOpaque(false);
        search.add(Style.label("Search (receipt no. or date)"), BorderLayout.WEST);
        search.add(txtSearch, BorderLayout.CENTER);
        add(search, BorderLayout.NORTH);

        Style.styleTable(table);
        table.setRowSorter(sorter);
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
        refresh();
    }

    private void applyFilter() {
        String q = txtSearch.getText().trim();
        sorter.setRowFilter(q.isEmpty() ? null : RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(q), 0, 1));
        updateSummary();
    }

    private void updateSummary() {
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < table.getRowCount(); i++) {
            int id = (Integer) table.getValueAt(i, 0);
            for (Sale t : loaded) {
                if (t.getId() == id) { sum = sum.add(t.getTotal()); break; }
            }
        }
        lblSummary.setText("Transactions: " + table.getRowCount() + "     Total Sales: " + Style.peso(sum));
    }

    public void refresh() {
        try {
            loaded = Database.getTransactions();
            model.setRowCount(0);
            for (Sale t : loaded) {
                model.addRow(new Object[]{t.getId(), Style.date(t.getDate()), Style.peso(t.getTotal()),
                        Style.peso(t.getCash()), Style.peso(t.getChange())});
            }
            updateSummary();
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
