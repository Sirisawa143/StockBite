package stockbite.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SaleTransaction {
    private final int id;
    private final LocalDateTime date;
    private final BigDecimal total, cash, change;
    private List<CartItem> items = new ArrayList<>();

    public SaleTransaction(int id, LocalDateTime date, BigDecimal total, BigDecimal cash, BigDecimal change) {
        this.id = id; this.date = date; this.total = total; this.cash = cash; this.change = change;
    }
    public int getId() { return id; }
    public LocalDateTime getDate() { return date; }
    public BigDecimal getTotal() { return total; }
    public BigDecimal getCash() { return cash; }
    public BigDecimal getChange() { return change; }
    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
}
