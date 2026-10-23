package stockbite.dao;

import stockbite.db.DBConnection;
import stockbite.model.Product;
import stockbite.util.Format;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAO {

    private Product map(ResultSet rs) throws SQLException {
        return new Product(rs.getInt("product_id"), rs.getString("product_name"),
                rs.getString("category"), rs.getBigDecimal("price"), rs.getInt("stock"));
    }

    /** Returns all products, or only those matching the keyword (name or category). */
    public List<Product> search(String keyword) throws SQLException {
        String sql = "SELECT * FROM products WHERE product_name LIKE ? OR category LIKE ? ORDER BY product_name";
        List<Product> list = new ArrayList<>();
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            String k = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            ps.setString(1, k);
            ps.setString(2, k);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Product> getAll() throws SQLException { return search(""); }

    public void add(Product p) throws SQLException {
        String sql = "INSERT INTO products (product_name, category, price, stock) VALUES (?,?,?,?)";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setBigDecimal(3, p.getPrice());
            ps.setInt(4, p.getStock());
            ps.executeUpdate();
        }
    }

    public void update(Product p) throws SQLException {
        String sql = "UPDATE products SET product_name=?, category=?, price=?, stock=? WHERE product_id=?";
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getCategory());
            ps.setBigDecimal(3, p.getPrice());
            ps.setInt(4, p.getStock());
            ps.setInt(5, p.getId());
            ps.executeUpdate();
        }
    }

    public void delete(int id) throws SQLException {
        try (Connection c = DBConnection.get(); PreparedStatement ps = c.prepareStatement("DELETE FROM products WHERE product_id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /** Adds stock to a product and records it in the stock log. */
    public void addStock(int productId, int qty) throws SQLException {
        try (Connection c = DBConnection.get()) {
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

    /** Recent stock additions: {date, product name, quantity}. */
    public List<Object[]> getStockLog() throws SQLException {
        String sql = "SELECT l.log_date, p.product_name, l.quantity_added FROM stock_log l "
                + "JOIN products p ON p.product_id = l.product_id ORDER BY l.log_date DESC, l.log_id DESC LIMIT 100";
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = DBConnection.get(); Statement st = c.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                rows.add(new Object[]{Format.date(rs.getTimestamp(1).toLocalDateTime()),
                        rs.getString(2), "+" + rs.getInt(3)});
            }
        }
        return rows;
    }
}
