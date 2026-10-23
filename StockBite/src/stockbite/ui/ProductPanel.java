package stockbite.ui;

import stockbite.dao.ProductDAO;
import stockbite.model.Product;
import stockbite.util.Format;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;

/** Product Management: add, edit, delete, and search canteen products. */
public class ProductPanel extends JPanel {
    private final ProductDAO dao = new ProductDAO();

    private final JTextField txtName = new JTextField();
    private final JComboBox<String> cbCategory =
            new JComboBox<>(new String[]{"Meals", "Snacks", "Drinks", "Desserts", "Others"});
    private final JTextField txtPrice = new JTextField();
    private final JSpinner spStock = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
    private final JTextField txtSearch = new JTextField();

    private final DefaultTableModel model = Theme.model("ID", "Product Name", "Category", "Price", "Stock");
    private final JTable table = new JTable(model);
    private int selectedId = -1;

    public ProductPanel() {
        setLayout(new BorderLayout(16, 0));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        cbCategory.setEditable(true);

        add(buildForm(), BorderLayout.WEST);
        add(buildTable(), BorderLayout.CENTER);

        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) fillFormFromSelection();
        });
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { refresh(); }
            public void removeUpdate(DocumentEvent e) { refresh(); }
            public void changedUpdate(DocumentEvent e) { refresh(); }
        });
        refresh();
    }

    private JPanel buildForm() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Theme.BORDER),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        form.setPreferredSize(new Dimension(290, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.fill = GridBagConstraints.HORIZONTAL; g.weightx = 1; g.insets = new Insets(4, 0, 4, 0);

        JLabel title = new JLabel("Product Details");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        g.gridy = 0; form.add(title, g);

        String[] labels = {"Product Name", "Category", "Price (\u20B1)", "Stock Quantity"};
        JComponent[] fields = {txtName, cbCategory, txtPrice, spStock};
        int y = 1;
        for (int i = 0; i < labels.length; i++) {
            g.gridy = y++; form.add(Theme.label(labels[i]), g);
            g.gridy = y++; form.add(fields[i], g);
        }

        JButton add = Theme.button("Add Product", Theme.GREEN);
        JButton update = Theme.button("Update Product", Theme.PRIMARY);
        JButton delete = Theme.button("Delete Product", Theme.RED);
        JButton clear = Theme.button("Clear", Theme.GRAY);
        add.addActionListener(e -> addProduct());
        update.addActionListener(e -> updateProduct());
        delete.addActionListener(e -> deleteProduct());
        clear.addActionListener(e -> clearForm());

        g.insets = new Insets(10, 0, 4, 0);
        g.gridy = y++; form.add(add, g);
        g.insets = new Insets(4, 0, 4, 0);
        g.gridy = y++; form.add(update, g);
        g.gridy = y++; form.add(delete, g);
        g.gridy = y++; form.add(clear, g);

        g.gridy = y; g.weighty = 1; form.add(Box.createVerticalGlue(), g);
        return form;
    }

    private JPanel buildTable() {
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.setOpaque(false);
        JPanel search = new JPanel(new BorderLayout(8, 0));
        search.setOpaque(false);
        search.add(Theme.label("Search:"), BorderLayout.WEST);
        search.add(txtSearch, BorderLayout.CENTER);
        txtSearch.setToolTipText("Search by product name or category");
        p.add(search, BorderLayout.NORTH);

        Theme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        return p;
    }

    public void refresh() {
        try {
            model.setRowCount(0);
            for (Product p : dao.search(txtSearch.getText())) {
                model.addRow(new Object[]{p.getId(), p.getName(), p.getCategory(), Format.peso(p.getPrice()), p.getStock()});
            }
        } catch (SQLException e) {
            Theme.error(this, e);
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (Integer) model.getValueAt(row, 0);
        txtName.setText(model.getValueAt(row, 1).toString());
        cbCategory.setSelectedItem(model.getValueAt(row, 2).toString());
        txtPrice.setText(model.getValueAt(row, 3).toString().replace("\u20B1", "").replace(",", ""));
        spStock.setValue(model.getValueAt(row, 4));
    }

    private void clearForm() {
        selectedId = -1;
        txtName.setText("");
        txtPrice.setText("");
        spStock.setValue(0);
        cbCategory.setSelectedIndex(0);
        table.clearSelection();
        txtName.requestFocus();
    }

    /** Validates the form; returns null (after showing a message) if something is wrong. */
    private Product readForm(int id) {
        String name = txtName.getText().trim();
        String category = String.valueOf(cbCategory.getSelectedItem()).trim();
        if (name.isEmpty()) { Theme.warn(this, "Please enter the product name."); return null; }
        if (category.isEmpty()) { Theme.warn(this, "Please enter a category."); return null; }
        BigDecimal price;
        try {
            price = Format.parseMoney(txtPrice.getText());
        } catch (NumberFormatException ex) {
            Theme.warn(this, "Please enter a valid price.");
            return null;
        }
        if (price.signum() <= 0) { Theme.warn(this, "Price must be greater than zero."); return null; }
        int stock = (Integer) spStock.getValue();
        return new Product(id, name, category, price.setScale(2, java.math.RoundingMode.HALF_UP), stock);
    }

    private void addProduct() {
        Product p = readForm(0);
        if (p == null) return;
        try {
            dao.add(p);
            clearForm();
            refresh();
            Theme.info(this, "Product added.");
        } catch (SQLException e) { Theme.error(this, e); }
    }

    private void updateProduct() {
        if (selectedId < 0) { Theme.warn(this, "Select a product from the table first."); return; }
        Product p = readForm(selectedId);
        if (p == null) return;
        try {
            dao.update(p);
            clearForm();
            refresh();
            Theme.info(this, "Product updated.");
        } catch (SQLException e) { Theme.error(this, e); }
    }

    private void deleteProduct() {
        if (selectedId < 0) { Theme.warn(this, "Select a product from the table first."); return; }
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete \"" + txtName.getText() + "\"?\nPast transaction records will be kept.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            dao.delete(selectedId);
            clearForm();
            refresh();
        } catch (SQLException e) { Theme.error(this, e); }
    }
}
