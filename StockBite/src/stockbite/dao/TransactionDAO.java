package stockbite.dao;

import stockbite.db.DBConnection;
import stockbite.model.CartItem;
import stockbite.model.SaleTransaction;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    /**
     * Saves a completed sale: records the transaction and its items, and deducts stock.
     * Everything succeeds or nothing is saved.
     */
    public SaleTransaction checkout(List<CartItem> cart, BigDecimal total, BigDecimal cash) throws SQLException {
        BigDecimal change = cash.subtract(total);
        try (Connection c = DBConnection.get()) {
            c.setAutoCommit(false);
            try {
                int txId;
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO transactions (total_amount, cash_received, change_amount) VALUES (?,?,?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setBigDecimal(1, total);
                    ps.setBigDecimal(2, cash);
                    ps.setBigDecimal(3, change);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        txId = keys.getInt(1);
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
                        item.setInt(1, txId);
                        item.setInt(2, ci.getProductId());
                        item.setString(3, ci.getName());
                        item.setInt(4, ci.getQuantity());
                        item.setBigDecimal(5, ci.getPrice());
                        item.setBigDecimal(6, ci.getSubtotal());
                        item.executeUpdate();
                    }
                }
                c.commit();
                SaleTransaction t = getById(c, txId);
                t.setItems(new ArrayList<>(cart));
                return t;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private SaleTransaction getById(Connection c, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("SELECT * FROM transactions WHERE transaction_id=?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return map(rs);
            }
        }
    }

    private SaleTransaction map(ResultSet rs) throws SQLException {
        return new SaleTransaction(rs.getInt("transaction_id"),
                rs.getTimestamp("transaction_date").toLocalDateTime(),
                rs.getBigDecimal("total_amount"), rs.getBigDecimal("cash_received"), rs.getBigDecimal("change_amount"));
    }

    public List<SaleTransaction> getAll() throws SQLException {
        List<SaleTransaction> list = new ArrayList<>();
        try (Connection c = DBConnection.get(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM transactions ORDER BY transaction_id DESC")) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<CartItem> getItems(int transactionId) throws SQLException {
        List<CartItem> items = new ArrayList<>();
        try (Connection c = DBConnection.get();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM transaction_items WHERE transaction_id=? ORDER BY item_id")) {
            ps.setInt(1, transactionId);
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
