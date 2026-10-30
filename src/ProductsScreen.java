import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;

public class ProductsScreen extends JPanel {

    private final JTextField txtName = new JTextField();
    private static final String CATEGORY_PROMPT = "Select a category";
    private final JComboBox<String> cbCategory =
            new Style.RoundCombo<>(new String[]{CATEGORY_PROMPT, "Meals", "Snacks", "Drinks", "Desserts", "Others"});
    private final JLabel lblCategoryHint = Style.hint(" ");
    private final JLabel lblStockHint = Style.hint(" ");
    private final Timer categoryLookup = new Timer(250, e -> autoFillCategory());
    private final JTextField txtPrice = new JTextField();
    private final JSpinner spStock = new Style.RoundSpinner(new SpinnerNumberModel(0, 0, 100000, 1));
    private final JTextField txtSearch = new JTextField();

    private final DefaultTableModel model = Style.model("ID", "Product Name", "Category", "Price", "Stock");
    private final JTable table = new JTable(model);
    private int selectedId = -1;

    public ProductsScreen() {
        setLayout(new BorderLayout(Style.XL, 0));
        setBackground(Style.BG);
        setBorder(BorderFactory.createEmptyBorder(Style.XL, Style.XL, Style.XL, Style.XL));
        cbCategory.setEditable(false);
        cbCategory.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                    boolean sel, boolean focus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, sel, focus);
                l.setBorder(BorderFactory.createEmptyBorder(4, Style.MD, 4, Style.MD));
                return l;
            }
        });
        Style.styleField(txtName);
        Style.styleField(txtPrice);
        Style.styleField(txtSearch);
        Style.styleField(spStock);
        Style.styleField(cbCategory);

        reloadCategories();
        categoryLookup.setRepeats(false);
        txtName.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { categoryLookup.restart(); }
            public void removeUpdate(DocumentEvent e) { categoryLookup.restart(); }
            public void changedUpdate(DocumentEvent e) { categoryLookup.restart(); }
        });

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
        JPanel form = Style.card(Style.XL);
        form.setLayout(new GridBagLayout());
        form.setPreferredSize(new Dimension(340, 0));

        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        int y = 0;

        g.gridy = y++; g.insets = new Insets(0, 0, Style.XS, 0);
        form.add(Style.heading("Product Details"), g);
        g.gridy = y++; g.insets = new Insets(0, 0, Style.LG, 0);
        form.add(Style.hint("Select a row to edit, or fill in a new product."), g);

        String[] labels = {"Product Name", "Category", "Price (\u20B1)", "Stock Quantity"};
        JComponent[] fields = {txtName, cbCategory, txtPrice, spStock};
        for (int i = 0; i < labels.length; i++) {
            g.gridy = y++; g.insets = new Insets(Style.SM, 0, Style.XS + 2, 0);
            form.add(Style.label(labels[i]), g);
            g.gridy = y++; g.insets = new Insets(0, 0, Style.XS, 0);
            form.add(fields[i], g);
            if (fields[i] == cbCategory) {
                g.gridy = y++; g.insets = new Insets(0, 0, Style.XS, 0);
                form.add(lblCategoryHint, g);
            }
            if (fields[i] == spStock) {
                g.gridy = y++; g.insets = new Insets(0, 0, Style.XS, 0);
                form.add(lblStockHint, g);
            }
        }

        JButton add = Style.button("Add", Style.GREEN);
        JButton update = Style.button("Update", Style.PRIMARY);
        JButton delete = Style.button("Delete", Style.RED);
        JButton clear = Style.button("Clear", Style.GRAY);
        add.addActionListener(e -> addProduct());
        update.addActionListener(e -> updateProduct());
        delete.addActionListener(e -> deleteProduct());
        clear.addActionListener(e -> clearForm());

        JPanel grid = new JPanel(new GridLayout(2, 2, Style.SM + 2, Style.SM + 2));
        grid.setOpaque(false);
        grid.add(add);
        grid.add(update);
        grid.add(delete);
        grid.add(clear);
        g.gridy = y++; g.insets = new Insets(Style.XL, 0, 0, 0);
        form.add(grid, g);

        g.gridy = y; g.weighty = 1; form.add(Box.createVerticalGlue(), g);
        return form;
    }

    private JPanel buildTable() {
        JPanel p = new JPanel(new BorderLayout(0, Style.LG));
        p.setOpaque(false);
        JPanel search = new JPanel(new BorderLayout(Style.MD, 0));
        search.setOpaque(false);
        search.add(Style.label("Search"), BorderLayout.WEST);
        search.add(txtSearch, BorderLayout.CENTER);
        txtSearch.setToolTipText("Search by product name or category");
        p.add(search, BorderLayout.NORTH);

        Style.styleTable(table);
        Style.widths(table, 60, 260, 130, 110, 80);
        table.getColumnModel().getColumn(0).setMaxWidth(70);
        p.add(Style.scroll(table), BorderLayout.CENTER);
        return p;
    }

    private void reloadCategories() {
        java.util.LinkedHashMap<String, String> items = new java.util.LinkedHashMap<>();
        for (String c : new String[]{"Meals", "Snacks", "Drinks", "Desserts", "Others"}) items.put(c.toLowerCase(), c);
        try {
            for (String c : Database.getCategories()) items.putIfAbsent(c.toLowerCase(), c);
        } catch (SQLException ex) {  }
        Object keep = cbCategory.getSelectedItem();
        cbCategory.removeAllItems();
        cbCategory.addItem(CATEGORY_PROMPT);
        for (String c : items.values()) cbCategory.addItem(c);
        cbCategory.setSelectedItem(keep == null ? CATEGORY_PROMPT : keep);
    }

    private void autoFillCategory() {
        if (selectedId >= 0) return;
        String name = txtName.getText().trim();
        String known = null;
        if (!name.isEmpty()) {
            try {
                known = Database.findCategoryByName(name);
            } catch (SQLException ex) {
                known = null;
            }
        }
        if (known != null) {
            cbCategory.setSelectedItem(known);
            cbCategory.setEnabled(false);
            lblCategoryHint.setText("Category filled in automatically.");
        } else {
            if (!cbCategory.isEnabled() || name.isEmpty()) cbCategory.setSelectedIndex(0);
            cbCategory.setEnabled(true);
            lblCategoryHint.setText(name.isEmpty() ? " " : "New product: please choose a category.");
        }
    }

    public void refresh() {
        try {
            model.setRowCount(0);
            for (Product p : Database.searchProducts(txtSearch.getText())) {
                model.addRow(new Object[]{p.getId(), p.getName(), p.getCategory(), Style.peso(p.getPrice()), p.getStock()});
            }
        } catch (SQLException e) {
            Style.error(this, e);
        }
    }

    private void fillFormFromSelection() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        selectedId = (Integer) model.getValueAt(row, 0);
        categoryLookup.stop();
        cbCategory.setEnabled(false);
        lblCategoryHint.setText("Category is set automatically for existing items.");
        txtName.setText(model.getValueAt(row, 1).toString());
        cbCategory.setSelectedItem(model.getValueAt(row, 2).toString());
        txtPrice.setText(model.getValueAt(row, 3).toString().replace("\u20B1", "").replace(",", ""));
        spStock.setValue(model.getValueAt(row, 4));
        spStock.setEnabled(true);
        lblStockHint.setText("You can correct the stock quantity here.");
    }

    private void clearForm() {
        selectedId = -1;
        txtName.setText("");
        txtPrice.setText("");
        spStock.setValue(0);
        spStock.setEnabled(true);
        lblStockHint.setText(" ");
        categoryLookup.stop();
        cbCategory.setEnabled(true);
        cbCategory.setSelectedIndex(0);
        lblCategoryHint.setText(" ");
        table.clearSelection();
        txtName.requestFocus();
    }

    private Product readForm(int id) {
        String name = txtName.getText().trim();
        String category = String.valueOf(cbCategory.getSelectedItem()).trim();
        if (name.isEmpty()) { Style.warn(this, "Please enter the product name."); return null; }
        try {
            if (Database.productNameExists(name, id)) {
                Style.warn(this, "A product named \"" + name + "\" already exists (capital letters and "
                        + "extra spaces don't count as different).");
                return null;
            }
        } catch (SQLException ex) { Style.error(this, ex); return null; }
        if (id == 0) {
            try {
                String known = Database.findCategoryByName(name);
                if (known != null) category = known;
            } catch (SQLException ex) {  }
        }
        if (id > 0) {
            try {
                String known = Database.getCategory(id);
                if (known != null) category = known;
            } catch (SQLException ex) { Style.error(this, ex); return null; }
        }
        if (category.isEmpty() || category.equals(CATEGORY_PROMPT)) {
            Style.warn(this, "Please choose a category for this new product.");
            return null;
        }
        BigDecimal price;
        try {
            price = Style.parseMoney(txtPrice.getText());
        } catch (NumberFormatException ex) {
            Style.warn(this, "Please enter a valid price.");
            return null;
        }
        if (price.signum() <= 0) { Style.warn(this, "Price must be greater than zero."); return null; }
        int stock = (Integer) spStock.getValue();
        return new Product(id, name, category, price.setScale(2, java.math.RoundingMode.HALF_UP), stock);
    }

    private void addProduct() {
        Product p = readForm(0);
        if (p == null) return;
        try {
            Database.addProduct(p);
            reloadCategories();
            clearForm();
            refresh();
            Style.info(this, "Product added.");
        } catch (SQLException e) { Style.error(this, e); }
    }

    private void updateProduct() {
        if (selectedId < 0) { Style.warn(this, "Select a product from the table first."); return; }
        Product p = readForm(selectedId);
        if (p == null) return;
        try {
            Database.updateProduct(p);
            reloadCategories();
            clearForm();
            refresh();
            Style.info(this, "Product updated.");
        } catch (SQLException e) { Style.error(this, e); }
    }

    private void deleteProduct() {
        if (selectedId < 0) { Style.warn(this, "Select a product from the table first."); return; }
        boolean ok = Style.confirm(this, "Delete product?",
                "\"" + txtName.getText() + "\" will be removed.\nPast transaction records will be kept, but its stock-in history will be removed.", "Delete");
        if (!ok) return;
        try {
            Database.deleteProduct(selectedId);
            clearForm();
            refresh();
        } catch (SQLException e) { Style.error(this, e); }
    }
}
