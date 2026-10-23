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

/** PosScreen.java - the Point of Sale tab: pick products, build the order, take cash, show the change, print the receipt. */
public class PosScreen extends JPanel {
    private final List<CartItem> cart = new ArrayList<>();

    private final DefaultTableModel productModel = Style.model("ID", "Product", "Category", "Price", "Stock");
    private final DefaultTableModel cartModel = Style.model("Item", "Qty", "Price", "Subtotal");
    private final JTable productTable = new JTable(productModel);
    private final JTable cartTable = new JTable(cartModel);
    private final JTextField txtSearch = new JTextField();
    private final JSpinner spQty = new Style.RoundSpinner(new SpinnerNumberModel(1, 1, 1000, 1));
    private final JTextField txtCash = new JTextField();
    private final JLabel lblTotal = new JLabel(Style.peso(BigDecimal.ZERO));
    private final JLabel lblChange = new JLabel(Style.peso(BigDecimal.ZERO));

    public PosScreen() {
        setLayout(new GridLayout(1, 2, Style.XL, 0));
        setBackground(Style.BG);
        setBorder(BorderFactory.createEmptyBorder(Style.XL, Style.XL, Style.XL, Style.XL));
        Style.styleField(txtSearch);
        Style.styleField(spQty, 100);
        Style.styleField(txtCash);
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
        JPanel p = new JPanel(new BorderLayout(0, Style.LG));
        p.setOpaque(false);

        JPanel search = new JPanel(new BorderLayout(Style.MD, 0));
        search.setOpaque(false);
        search.add(Style.label("Search"), BorderLayout.WEST);
        search.add(txtSearch, BorderLayout.CENTER);
        p.add(search, BorderLayout.NORTH);

        Style.styleTable(productTable);
        Style.widths(productTable, 50, 210, 110, 90, 70);
        productTable.getColumnModel().getColumn(0).setMaxWidth(60);
        productTable.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (e.getClickCount() == 2) addToCart(); }
        });
        p.add(Style.scroll(productTable), BorderLayout.CENTER);

        JPanel bar = Style.card(Style.MD + 4);
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, Style.MD, 0));
        JButton add = Style.button("Add to Cart", Style.GREEN);
        add.addActionListener(e -> addToCart());
        bar.add(Style.label("Quantity"));
        bar.add(spQty);
        bar.add(add);
        p.add(bar, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildCartSide() {
        JPanel p = new JPanel(new BorderLayout(0, Style.LG));
        p.setOpaque(false);
        JPanel head = new JPanel(new BorderLayout());
        head.setOpaque(false);
        head.setPreferredSize(new Dimension(10, Style.FIELD_H));
        head.add(Style.heading("Current Order"), BorderLayout.WEST);
        p.add(head, BorderLayout.NORTH);

        Style.styleTable(cartTable);
        Style.widths(cartTable, 200, 70, 100, 110);
        cartTable.getColumnModel().getColumn(1).setMaxWidth(80);
        p.add(Style.scroll(cartTable), BorderLayout.CENTER);

        JButton remove = Style.button("Remove Item", Style.RED);
        JButton clear = Style.button("Clear Cart", Style.GRAY);
        remove.addActionListener(e -> removeSelected());
        clear.addActionListener(e -> { cart.clear(); refreshCart(); });
        JPanel cartButtons = new JPanel(new FlowLayout(FlowLayout.LEFT, Style.MD, 0));
        cartButtons.setOpaque(false);
        cartButtons.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        cartButtons.add(remove);
        cartButtons.add(clear);

        lblTotal.setFont(Style.BIG);
        lblTotal.setForeground(Style.TEXT_ORANGE);
        lblChange.setFont(Style.BIG);
        lblChange.setForeground(Style.TEXT_GREEN);
        txtCash.setFont(Style.INPUT_BIG);
        txtCash.setPreferredSize(new Dimension(220, 44));

        JPanel pay = Style.card(Style.LG);
        pay.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridy = 0; g.gridx = 0; g.weightx = 0; g.insets = new Insets(0, 0, Style.SM, Style.LG);
        pay.add(Style.label("Total"), g);
        g.gridx = 1; g.weightx = 1; g.insets = new Insets(0, 0, Style.SM, 0);
        lblTotal.setHorizontalAlignment(SwingConstants.RIGHT); pay.add(lblTotal, g);
        g.gridy = 1; g.gridx = 0; g.weightx = 0; g.insets = new Insets(0, 0, Style.SM, Style.LG);
        pay.add(Style.label("Cash (\u20B1)"), g);
        g.gridx = 1; g.weightx = 1; g.insets = new Insets(0, 0, Style.SM, 0);
        txtCash.setHorizontalAlignment(SwingConstants.RIGHT); pay.add(txtCash, g);
        g.gridy = 2; g.gridx = 0; g.weightx = 0; g.insets = new Insets(0, 0, Style.MD, Style.LG);
        pay.add(Style.label("Change"), g);
        g.gridx = 1; g.weightx = 1; g.insets = new Insets(0, 0, Style.MD, 0);
        lblChange.setHorizontalAlignment(SwingConstants.RIGHT); pay.add(lblChange, g);
        JButton checkout = Style.button("Complete Purchase", Style.PRIMARY);
        checkout.setFont(Style.BOLD.deriveFont(15f));
        checkout.setPreferredSize(new Dimension(10, 46));
        checkout.addActionListener(e -> checkout());
        g.gridy = 3; g.gridx = 0; g.gridwidth = 2; g.insets = new Insets(0, 0, 0, 0); pay.add(checkout, g);

        JPanel south = new JPanel(new BorderLayout(0, Style.LG));
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
            for (Product p : Database.searchProducts(txtSearch.getText())) {
                productModel.addRow(new Object[]{p.getId(), p.getName(), p.getCategory(), Style.peso(p.getPrice()), p.getStock()});
            }
        } catch (SQLException e) {
            Style.error(this, e);
        }
    }

    private int inCart(int productId) {
        for (CartItem c : cart) if (c.getProductId() == productId) return c.getQuantity();
        return 0;
    }

    private void addToCart() {
        int r = productTable.getSelectedRow();
        if (r < 0) { Style.warn(this, "Select a product to add."); return; }
        int id = (Integer) productModel.getValueAt(r, 0);
        String name = productModel.getValueAt(r, 1).toString();
        BigDecimal price = Style.parseMoney(productModel.getValueAt(r, 3).toString().replace("\u20B1", ""));
        int stock = (Integer) productModel.getValueAt(r, 4);
        int qty = (Integer) spQty.getValue();

        if (stock == 0) { Style.warn(this, name + " is out of stock."); return; }
        if (inCart(id) + qty > stock) {
            Style.warn(this, "Not enough stock for " + name + ".\nAvailable: " + stock + ", already in cart: " + inCart(id));
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
        if (r < 0) { Style.warn(this, "Select an item in the cart to remove."); return; }
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
            cartModel.addRow(new Object[]{c.getName(), c.getQuantity(), Style.peso(c.getPrice()), Style.peso(c.getSubtotal())});
        }
        lblTotal.setText(Style.peso(total()));
        updateChange();
    }

    private void updateChange() {
        try {
            BigDecimal change = Style.parseMoney(txtCash.getText()).subtract(total());
            lblChange.setText(Style.peso(change.max(BigDecimal.ZERO)));
        } catch (NumberFormatException ex) {
            lblChange.setText(Style.peso(BigDecimal.ZERO));
        }
    }

    private void checkout() {
        if (cart.isEmpty()) { Style.warn(this, "The cart is empty."); return; }
        BigDecimal cash;
        try {
            cash = Style.parseMoney(txtCash.getText());
        } catch (NumberFormatException ex) {
            Style.warn(this, "Please enter the cash received.");
            return;
        }
        BigDecimal total = total();
        if (cash.compareTo(total) < 0) {
            Style.warn(this, "Insufficient cash. Total is " + Style.peso(total) + ".");
            return;
        }
        try {
            Sale t = Database.checkout(new ArrayList<>(cart), total, cash);
            cart.clear();
            txtCash.setText("");
            refreshCart();
            loadProducts();
            new Receipt(SwingUtilities.getWindowAncestor(this), t).setVisible(true);
        } catch (SQLException e) {
            Style.error(this, e);
            loadProducts();
        }
    }
}
