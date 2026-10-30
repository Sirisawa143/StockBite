import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Database {

    private static final String URL =
            "jdbc:mysql://localhost:3307/stockbite_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Manila";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {

            if (e.getMessage() != null && e.getMessage().contains("No suitable driver")) {
                Connection c = connectUsingLibFolder();
                if (c != null) return c;
            }
            throw e;
        }
    }

    private static Driver libDriver;

    private static synchronized Connection connectUsingLibFolder() throws SQLException {
        try {
            if (libDriver == null) {
                java.io.File jar = findConnectorJar();
                if (jar == null) return null;
                java.net.URLClassLoader loader = new java.net.URLClassLoader(
                        new java.net.URL[]{jar.toURI().toURL()}, Database.class.getClassLoader());
                libDriver = (Driver) Class.forName("com.mysql.cj.jdbc.Driver", true, loader)
                        .getDeclaredConstructor().newInstance();
            }
            java.util.Properties props = new java.util.Properties();
            props.setProperty("user", USER);
            props.setProperty("password", PASSWORD);
            return libDriver.connect(URL, props);
        } catch (SQLException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
    }

    private static java.io.File findConnectorJar() {
        java.util.List<java.io.File> dirs = new ArrayList<>();
        dirs.add(new java.io.File("lib"));
        dirs.add(new java.io.File(System.getProperty("user.dir"), "lib"));
        dirs.add(new java.io.File("StockBite/lib"));
        try {
            java.io.File here = new java.io.File(Database.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            java.io.File base = here.isFile() ? here.getParentFile() : here;
            if (base != null) {
                dirs.add(new java.io.File(base, "lib"));
                if (base.getParentFile() != null) dirs.add(new java.io.File(base.getParentFile(), "lib"));
            }
        } catch (Exception ignored) { }
        for (java.io.File d : dirs) {
            java.io.File[] jars = d.listFiles((dir, name) -> name.startsWith("mysql-connector") && name.endsWith(".jar"));
            if (jars != null && jars.length > 0) return jars[0];
        }
        return null;
    }

    public static List<Product> searchProducts(String keyword) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_name LIKE ? OR category LIKE ? ORDER BY product_name";
        List<Product> list = new ArrayList<>();
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            String k = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Product(rs.getInt("product_id"), rs.getString("product_name"),
                            rs.getString("category"), rs.getBigDecimal("price"), rs.getInt("stock")));
                }
            }
        }
        return list;
    }

    private static String normalizeName(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    public static boolean productNameExists(String name, int excludeId) throws SQLException {
        String wanted = normalizeName(name);
        try (Connection c = getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT product_id, product_name FROM products")) {
            while (rs.next()) {
                if (rs.getInt(1) != excludeId && normalizeName(rs.getString(2)).equals(wanted)) return true;
            }
        }
        return false;
    }

    public static List<String> getCategories() throws SQLException {
        List<String> list = new ArrayList<>();
        try (Connection c = getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT DISTINCT category FROM products ORDER BY category")) {
            while (rs.next()) list.add(rs.getString(1));
        }
        return list;
    }

    public static String findCategoryByName(String name) throws SQLException {
        if (name == null || name.trim().isEmpty()) return null;
        String sql = "SELECT category FROM products WHERE LOWER(TRIM(product_name)) = LOWER(?) "
                + "ORDER BY product_id DESC LIMIT 1";
        try (Connection c = getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public static int addProduct(Product p) throws SQLException {
        String sql = "INSERT INTO products (product_name, category, price, stock) VALUES (?,?,?,?)";
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                int id;
                try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, p.getName());
                    ps.setString(2, p.getCategory());
                    ps.setBigDecimal(3, p.getPrice());
                    ps.setInt(4, p.getStock());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        id = keys.getInt(1);
                    }
                }
                if (p.getStock() > 0) {
                    try (PreparedStatement log = c.prepareStatement(
                            "INSERT INTO stock_log (product_id, quantity_added) VALUES (?,?)")) {
                        log.setInt(1, id);
                        log.setInt(2, p.getStock());
                        log.executeUpdate();
                    }
                }
                c.commit();
                return id;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public static void updateProduct(Product p) throws SQLException {
        String sql = "UPDATE products SET product_name=?, category=?, price=?, stock=? WHERE product_id=?";
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                int oldStock = 0;
                try (PreparedStatement q = c.prepareStatement("SELECT stock FROM products WHERE product_id=? FOR UPDATE")) {
                    q.setInt(1, p.getId());
                    try (ResultSet rs = q.executeQuery()) {
                        if (rs.next()) oldStock = rs.getInt(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(sql)) {
                    ps.setString(1, p.getName());
                    ps.setString(2, p.getCategory());
                    ps.setBigDecimal(3, p.getPrice());
                    ps.setInt(4, p.getStock());
                    ps.setInt(5, p.getId());
                    ps.executeUpdate();
                }
                int diff = p.getStock() - oldStock;
                if (diff != 0) {
                    try (PreparedStatement log = c.prepareStatement(
                            "INSERT INTO stock_log (product_id, quantity_added) VALUES (?,?)")) {
                        log.setInt(1, p.getId());
                        log.setInt(2, diff);
                        log.executeUpdate();
                    }
                }
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public static String getCategory(int productId) throws SQLException {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT category FROM products WHERE product_id=?")) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        }
    }

    public static int getStock(int productId) throws SQLException {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT stock FROM products WHERE product_id=?")) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : -1;
            }
        }
    }

    public static void deleteProduct(int id) throws SQLException {
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM products WHERE product_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    public static void addStock(int productId, int qty) throws SQLException {
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try (PreparedStatement up = c.prepareStatement("UPDATE products SET stock = stock + ? WHERE product_id=?");
                 PreparedStatement log = c.prepareStatement("INSERT INTO stock_log (product_id, quantity_added) VALUES (?,?)")) {
                up.setInt(1, qty);
                up.setInt(2, productId);
                up.executeUpdate();
                log.setInt(1, productId);
                log.setInt(2, qty);
                log.executeUpdate();
                c.commit();
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public static List<Object[]> getStockLog() throws SQLException {
        String sql = "SELECT l.log_date, p.product_name, l.quantity_added FROM stock_log l "
                + "JOIN products p ON p.product_id = l.product_id ORDER BY l.log_date DESC, l.log_id DESC LIMIT 100";
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = getConnection(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new Object[]{Style.date(rs.getTimestamp(1).toLocalDateTime()),
                        rs.getString(2), "+" + rs.getInt(3)});
            }
        }
        return rows;
    }

    public static Sale checkout(List<CartItem> cart, BigDecimal total, BigDecimal cash) throws SQLException {
        BigDecimal change = cash.subtract(total);
        try (Connection c = getConnection()) {
            c.setAutoCommit(false);
            try {
                int saleId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO transactions (total_amount, cash_received, change_amount) VALUES (?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setBigDecimal(1, total);
                    ps.setBigDecimal(2, cash);
                    ps.setBigDecimal(3, change);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        saleId = keys.getInt(1);
                    }
                }
                try (PreparedStatement stock = c.prepareStatement(
                        "UPDATE products SET stock = stock - ? WHERE product_id=? AND stock >= ?");
                     PreparedStatement item = c.prepareStatement(
                        "INSERT INTO transaction_items (transaction_id, product_id, product_name, quantity, unit_price, subtotal) VALUES (?,?,?,?,?,?)")) {
                    for (CartItem ci : cart) {
                        stock.setInt(1, ci.getQuantity());
                        stock.setInt(2, ci.getProductId());
                        stock.setInt(3, ci.getQuantity());
                        if (stock.executeUpdate() == 0) {
                            throw new SQLException("Not enough stock for \"" + ci.getName() + "\".");
                        }
                        item.setInt(1, saleId);
                        item.setInt(2, ci.getProductId());
                        item.setString(3, ci.getName());
                        item.setInt(4, ci.getQuantity());
                        item.setBigDecimal(5, ci.getPrice());
                        item.setBigDecimal(6, ci.getSubtotal());
                        item.executeUpdate();
                    }
                }
                c.commit();

                Sale sale;
                try (PreparedStatement ps = c.prepareStatement("SELECT * FROM transactions WHERE transaction_id=?")) {
                    ps.setInt(1, saleId);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        sale = readSale(rs);
                    }
                }
                sale.setItems(new ArrayList<>(cart));
                return sale;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private static Sale readSale(ResultSet rs) throws SQLException {
        return new Sale(rs.getInt("transaction_id"), rs.getTimestamp("transaction_date").toLocalDateTime(),
                rs.getBigDecimal("total_amount"), rs.getBigDecimal("cash_received"), rs.getBigDecimal("change_amount"));
    }

    public static List<Sale> getTransactions() throws SQLException {
        List<Sale> list = new ArrayList<>();
        try (Connection c = getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM transactions ORDER BY transaction_id DESC")) {
            while (rs.next()) list.add(readSale(rs));
        }
        return list;
    }

    public static List<CartItem> getTransactionItems(int saleId) throws SQLException {
        List<CartItem> items = new ArrayList<>();
        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM transaction_items WHERE transaction_id=? ORDER BY item_id")) {
            ps.setInt(1, saleId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new CartItem(rs.getInt("product_id"), rs.getString("product_name"),
                            rs.getBigDecimal("unit_price"), rs.getInt("quantity")));
                }
            }
        }
        return items;
    }
}

class Product {
    private final int id;
    private final String name;
    private final String category;
    private final BigDecimal price;
    private final int stock;

    Product(int id, String name, String category, BigDecimal price, int stock) {
        this.id = id; this.name = name; this.category = category; this.price = price; this.stock = stock;
    }
    int getId() { return id; }
    String getName() { return name; }
    String getCategory() { return category; }
    BigDecimal getPrice() { return price; }
    int getStock() { return stock; }
}

class CartItem {
    private final int productId;
    private final String name;
    private final BigDecimal price;
    private int quantity;

    CartItem(int productId, String name, BigDecimal price, int quantity) {
        this.productId = productId; this.name = name; this.price = price; this.quantity = quantity;
    }
    int getProductId() { return productId; }
    String getName() { return name; }
    BigDecimal getPrice() { return price; }
    int getQuantity() { return quantity; }
    void addQuantity(int q) { quantity += q; }
    void setQuantity(int q) { quantity = q; }
    BigDecimal getSubtotal() { return price.multiply(BigDecimal.valueOf(quantity)); }
}

class Sale {
    private final int id;
    private final LocalDateTime date;
    private final BigDecimal total, cash, change;
    private List<CartItem> items = new ArrayList<>();

    Sale(int id, LocalDateTime date, BigDecimal total, BigDecimal cash, BigDecimal change) {
        this.id = id; this.date = date; this.total = total; this.cash = cash; this.change = change;
    }
    int getId() { return id; }
    LocalDateTime getDate() { return date; }
    BigDecimal getTotal() { return total; }
    BigDecimal getCash() { return cash; }
    BigDecimal getChange() { return change; }
    List<CartItem> getItems() { return items; }
    void setItems(List<CartItem> items) { this.items = items; }
}
