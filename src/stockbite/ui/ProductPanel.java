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
        setLayout(new BorderLayout(Theme.XL, 0));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(Theme.XL, Theme.XL, Theme.XL, Theme.XL));
        cbCategory.setEditable(true);
        Theme.styleField(txtName);
        Theme.styleField(txtPrice);
        Theme.styleField(txtSearch);
        Theme.styleField(spStock);
        Theme.styleField(cbCategory);

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
        JPanel form = Theme.card(Theme.XL);
        form.setLayout(new GridBagLayout());
        form.setPreferredSize(new Dimension(340, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        g.gridy = y++; g.insets = new Insets(0, 0, Theme.XS, 0);
        form.add(Theme.heading("Product Details"), g);
        g.gridy = y++; g.insets = new Insets(0, 0, Theme.LG, 0);
        form.add(Theme.hint("Select a row to edit, or fill in a new product."), g);

        String[] labels = {"Product Name", "Category", "Price (\u20B1)", "Stock Quantity"};
        JComponent[] fields = {txtName, cbCategory, txtPrice, spStock};
        for (int i = 0; i < labels.length; i++) {
            g.gridy = y++; g.insets = new Insets(Theme.SM, 0, Theme.XS + 2, 0);
            form.add(Theme.label(labels[i]), g);
            g.gridy = y++; g.insets = new Insets(0, 0, Theme.XS, 0);
            form.add(fields[i], g);
        }

        JButton add = Theme.button("Add", Theme.GREEN);
        JButton update = Theme.button("Update", Theme.PRIMARY);
        JButton delete = Theme.button("Delete", Theme.RED);
        JButton clear = Theme.button("Clear", Theme.GRAY);
        add.addActionListener(e -> addProduct());
        update.addActionListener(e -> updateProduct());
        delete.addActionListener(e -> deleteProduct());
        clear.addActionListener(e -> clearForm());

        JPanel grid = new JPanel(new GridLayout(2, 2, Theme.SM + 2, Theme.SM + 2));
        grid.setOpaque(false);
        grid.add(add);
        grid.add(update);
        grid.add(delete);
        grid.add(clear);
        g.gridy = y++; g.insets = new Insets(Theme.XL, 0, 0, 0);
        form.add(grid, g);

        g.gridy = y; g.weighty = 1; form.add(Box.createVerticalGlue(), g);
        return form;
    }

    private JPanel buildTable() {
        JPanel p = new JPanel(new BorderLayout(0, Theme.LG));
        p.setOpaque(false);
        JPanel search = new JPanel(new BorderLayout(Theme.MD, 0));
        search.setOpaque(false);
        search.add(Theme.label("Search"), BorderLayout.WEST);
        search.add(txtSearch, BorderLayout.CENTER);
        txtSearch.setToolTipText("Search by product name or category");
        p.add(search, BorderLayout.NORTH);

        Theme.styleTable(table);
        Theme.widths(table, 60, 260, 130, 110, 80);
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        p.add(Theme.scroll(table), BorderLayout.CENTER);
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
        boolean ok = Theme.confirm(this, "Delete product?",
                "\"" + txtName.getText() + "\" will be removed.\nPast transaction records will be kept.", "Delete");
        if (!ok) return;
        try {
            dao.delete(selectedId);
            clearForm();
            refresh();
        } catch (SQLException e) { Theme.error(this, e); }
    }
}
