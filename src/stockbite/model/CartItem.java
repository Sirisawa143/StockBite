package stockbite.model;

import java.math.BigDecimal;

public class CartItem {
    private final int productId;
    private final String name;
    private final BigDecimal price;
    private int quantity;

    public CartItem(int productId, String name, BigDecimal price, int quantity) {
        this.productId = productId; this.name = name; this.price = price; this.quantity = quantity;
    }
    public int getProductId() { return productId; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public void addQuantity(int q) { quantity += q; }
    public BigDecimal getSubtotal() { return price.multiply(BigDecimal.valueOf(quantity)); }
}
