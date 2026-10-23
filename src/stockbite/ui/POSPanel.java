package stockbite.ui;

import stockbite.dao.ProductDAO;
import stockbite.dao.TransactionDAO;
import stockbite.model.CartItem;
import stockbite.model.Product;
import stockbite.model.SaleTransaction;
import stockbite.util.Format;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Point of Sale: build an order, take cash, compute total and change, save, and print a receipt. */
public class POSPanel extends JPanel {
    private final ProductDAO productDao = new ProductDAO();
    private final TransactionDAO txDao = new TransactionDAO();
    private final List<CartItem> cart = new ArrayList<>();

    private final DefaultTableModel productModel = Theme.model("ID", "Product", "Category", "Price", "Stock");
    private final DefaultTableModel cartModel = Theme.model("Item", "Qty", "Price", "Subtotal");
    private final JTable productTable = new JTable(productModel);
    private final JTable cartTable = new JTable(cartModel);
    private final JTextField txtSearch = new JTextField();
    private final JSpinner spQty = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
    private final JTextField txtCash = new JTextField();
    private final JLabel lblTotal = new JLabel(Format.peso(BigDecimal.ZERO));
    private final JLabel lblChange = new JLabel(Format.peso(BigDecimal.ZERO));

    public POSPanel() {
        setLayout(new GridLayout(1, 2, Theme.XL, 0));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(Theme.XL, Theme.XL, Theme.XL, Theme.XL));
        Theme.styleField(txtSearch);
        Theme.styleField(spQty, 100);
        Theme.styleField(txtCash);
        add(buildProductSide());
        add(buildCartSide());

        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { loadProducts(); }
            public void removeUpdate(DocumentEvent e) { loadProducts(); }
            public void changedUpdate(DocumentEvent e) { loadProducts(); }
        });
        txtCash.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateChange(); }
            public void removeUpdate(DocumentEvent e) { updateChange(); }
            public void changedUpdate(DocumentEvent e) { updateChange(); }
        });
        loadProducts();
    }

    private JPanel buildProductSide() {
        JPanel p = new JPanel(new BorderLayout(0, Theme.LG));
        p.setOpaque(false);

        JPanel search = new JPanel(new BorderLayout(Theme.MD, 0));
        search.setOpaque(false);
        search.add(Theme.label("Search"), BorderLayout.WEST);
        search.add(txtSearch, BorderLayout.CENTER);
        p.add(search, BorderLayout.NORTH);

        Theme.styleTable(productTable);
        Theme.widths(productTable, 50, 210, 110, 90, 70);
        productTable.getColumnModel().getColumn(0).setMaxWidth(60);
        productTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) addToCart(); }
        });
        p.add(Theme.scroll(productTable), BorderLayout.CENTER);

        JPanel bar = Theme.card(Theme.MD + 4);
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, Theme.MD, 0));
        JButton add = Theme.button("Add to Cart", Theme.GREEN);
        add.addActionListener(e -> addToCart());
        bar.add(Theme.label("Quantity"));
        bar.add(spQty);
        bar.add(add);
        p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildCartSide() {
        JPanel p = new JPanel(new BorderLayout(0, Theme.LG));
        p.setOpaque(false);
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setPreferredSize(new Dimension(10, Theme.FIELD_H));
        head.add(Theme.heading("Current Order"), BorderLayout.WEST);
        p.add(head, BorderLayout.NORTH);

        Theme.styleTable(cartTable);
        Theme.widths(cartTable, 200, 70, 100, 110);
        cartTable.getColumnModel().getColumn(1).setMaxWidth(80);
        p.add(Theme.scroll(cartTable), BorderLayout.CENTER);

        JButton remove = Theme.button("Remove Item", Theme.RED);
        JButton clear = Theme.button("Clear Cart", Theme.GRAY);
        remove.addActionListener(e -> removeSelected());
        clear.addActionListener(e -> { cart.clear(); refreshCart(); });
        JPanel cartButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, Theme.MD, 0));
        cartButtons.setOpaque(false);
        cartButtons.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cartButtons.add(remove);
        cartButtons.add(clear);

        lblTotal.setFont(Theme.BIG);
        lblTotal.setForeground(Theme.TEXT_ORANGE);
        lblChange.setFont(Theme.BIG);
        lblChange.setForeground(Theme.TEXT_GREEN);
        txtCash.setFont(Theme.INPUT_BIG);
        txtCash.setPreferredSize(new Dimension(220, 44));

        JPanel pay = Theme.card(Theme.LG);
        pay.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridy = 0; g.gridx = 0; g.weightx = 0; g.insets = new Insets(0, 0, Theme.SM, Theme.LG);
        pay.add(Theme.label("Total"), g);
        g.gridx = 1; g.weightx = 1; g.insets = new Insets(0, 0, Theme.SM, 0);
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT); pay.add(lblTotal, g);
        g.gridy = 1; g.gridx = 0; g.weightx = 0; g.insets = new Insets(0, 0, Theme.SM, Theme.LG);
        pay.add(Theme.label("Cash (\u20B1)"), g);
        g.gridx = 1; g.weightx = 1; g.insets = new Insets(0, 0, Theme.SM, 0);
        txtCash.setHorizontalAlignment(SwingConstants.RIGHT); pay.add(txtCash, g);
        g.gridy = 2; g.gridx = 0; g.weightx = 0; g.insets = new Insets(0, 0, Theme.MD, Theme.LG);
        pay.add(Theme.label("Change"), g);
        g.gridx = 1; g.weightx = 1; g.insets = new Insets(0, 0, Theme.MD, 0);
        lblChange.setHorizontalAlignment(SwingConstants.RIGHT); pay.add(lblChange, g);
        JButton checkout = Theme.button("Complete Purchase", Theme.PRIMARY);
        checkout.setFont(Theme.BOLD.deriveFont(15f));
        checkout.setPreferredSize(new Dimension(10, 46));
        checkout.addActionListener(e -> checkout());
        g.gridy = 3; g.gridx = 0; g.gridwidth = 2; g.insets = new Insets(0, 0, 0, 0); pay.add(checkout, g);

        JPanel south = new JPanel(new BorderLayout(0, Theme.LG));
        south.setOpaque(false);
        south.add(cartButtons, BorderLayout.NORTH);
        south.add(pay, BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    public void refresh() { loadProducts(); }

    private void loadProducts() {
        try {
            productModel.setRowCount(0);
            for (Product p : productDao.search(txtSearch.getText())) {
                productModel.addRow(new Object[]{p.getId(), p.getName(), p.getCategory(), Format.peso(p.getPrice()), p.getStock()});
            }
        } catch (SQLException e) {
            Theme.error(this, e);
        }
    }

    private int inCart(int productId) {
        for (CartItem c : cart) if (c.getProductId() == productId) return c.getQuantity();
        return 0;
    }

    private void addToCart() {
        int r = productTable.getSelectedRow();
        if (r < 0) { Theme.warn(this, "Select a product to add."); return; }
        int id = (Integer) productModel.getValueAt(r, 0);
        String name = productModel.getValueAt(r, 1).toString();
        BigDecimal price = Format.parseMoney(productModel.getValueAt(r, 3).toString().replace("\u20B1", ""));
        int stock = (Integer) productModel.getValueAt(r, 4);
        int qty = (Integer) spQty.getValue();

        if (stock == 0) { Theme.warn(this, name + " is out of stock."); return; }
        if (inCart(id) + qty > stock) {
            Theme.warn(this, "Not enough stock for " + name + ".\nAvailable: " + stock + ", already in cart: " + inCart(id));
            return;
        }
        boolean merged = false;
        for (CartItem c : cart) {
            if (c.getProductId() == id) { c.addQuantity(qty); merged = true; break; }
        }
        if (!merged) cart.add(new CartItem(id, name, price, qty));
        spQty.setValue(1);
        refreshCart();
    }

    private void removeSelected() {
        int r = cartTable.getSelectedRow();
        if (r < 0) { Theme.warn(this, "Select an item in the cart to remove."); return; }
        cart.remove(r);
        refreshCart();
    }

    private BigDecimal total() {
        BigDecimal t = BigDecimal.ZERO;
        for (CartItem c : cart) t = t.add(c.getSubtotal());
        return t;
    }

    private void refreshCart() {
        cartModel.setRowCount(0);
        for (CartItem c : cart) {
            cartModel.addRow(new Object[]{c.getName(), c.getQuantity(), Format.peso(c.getPrice()), Format.peso(c.getSubtotal())});
        }
        lblTotal.setText(Format.peso(total()));
        updateChange();
    }

    private void updateChange() {
        try {
            BigDecimal change = Format.parseMoney(txtCash.getText()).subtract(total());
            lblChange.setText(Format.peso(change.max(BigDecimal.ZERO)));
        } catch (NumberFormatException ex) {
            lblChange.setText(Format.peso(BigDecimal.ZERO));
        }
    }

    private void checkout() {
        if (cart.isEmpty()) { Theme.warn(this, "The cart is empty."); return; }
        BigDecimal cash;
        try {
            cash = Format.parseMoney(txtCash.getText());
        } catch (NumberFormatException ex) {
            Theme.warn(this, "Please enter the cash received.");
            return;
        }
        BigDecimal total = total();
        if (cash.compareTo(total) < 0) {
            Theme.warn(this, "Insufficient cash. Total is " + Format.peso(total) + ".");
            return;
        }
        try {
            SaleTransaction t = txDao.checkout(new ArrayList<>(cart), total, cash);
            cart.clear();
            txtCash.setText("");
            refreshCart();
            loadProducts();
            new ReceiptDialog(SwingUtilities.getWindowAncestor(this), t).setVisible(true);
        } catch (SQLException e) {
            Theme.error(this, e);
            loadProducts();
        }
    }
}
