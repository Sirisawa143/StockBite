package stockbite.ui;

import stockbite.dao.TransactionDAO;
import stockbite.model.SaleTransaction;
import stockbite.util.Format;

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

/** Transaction Records: saved sales from the database, with receipt viewing. */
public class TransactionPanel extends JPanel {
    private final TransactionDAO dao = new TransactionDAO();
    private final DefaultTableModel model = Theme.model("Receipt No.", "Date", "Total", "Cash Received", "Change");
    private final JTable table = new JTable(model);
    private final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
    private final JTextField txtSearch = new JTextField();
    private final JLabel lblSummary = new JLabel(" ");
    private List<SaleTransaction> loaded = new ArrayList<>();

    public TransactionPanel() {
        setLayout(new BorderLayout(0, 8));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel search = new JPanel(new BorderLayout(8, 0));
        search.setOpaque(false);
        search.add(Theme.label("Search (receipt no. or date):"), BorderLayout.WEST);
        search.add(txtSearch, BorderLayout.CENTER);
        add(search, BorderLayout.NORTH);

        Theme.styleTable(table);
        table.setRowSorter(sorter);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) viewReceipt(); }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);

        JButton view = Theme.button("View Receipt", Theme.PRIMARY);
        JButton reload = Theme.button("Refresh", Theme.GRAY);
        view.addActionListener(e -> viewReceipt());
        reload.addActionListener(e -> refresh());
        lblSummary.setFont(Theme.BOLD);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
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
            for (SaleTransaction t : loaded) {
                if (t.getId() == id) { sum = sum.add(t.getTotal()); break; }
            }
        }
        lblSummary.setText("Transactions: " + table.getRowCount() + "   |   Total Sales: " + Format.peso(sum));
    }

    public void refresh() {
        try {
            loaded = dao.getAll();
            model.setRowCount(0);
            for (SaleTransaction t : loaded) {
                model.addRow(new Object[]{t.getId(), Format.date(t.getDate()), Format.peso(t.getTotal()),
                        Format.peso(t.getCash()), Format.peso(t.getChange())});
            }
            updateSummary();
        } catch (SQLException e) {
            Theme.error(this, e);
        }
    }

    private void viewReceipt() {
        int r = table.getSelectedRow();
        if (r < 0) { Theme.warn(this, "Select a transaction first."); return; }
        int id = (Integer) model.getValueAt(table.convertRowIndexToModel(r), 0);
        try {
            for (SaleTransaction t : loaded) {
                if (t.getId() == id) {
                    t.setItems(dao.getItems(id));
                    new ReceiptDialog(SwingUtilities.getWindowAncestor(this), t).setVisible(true);
                    return;
                }
            }
        } catch (SQLException e) { Theme.error(this, e); }
    }
}
