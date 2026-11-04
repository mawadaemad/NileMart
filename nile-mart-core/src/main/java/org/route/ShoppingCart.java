package org.route;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Scope("prototype")
public class ShoppingCart {

    @Getter
    @Setter
    private int cartNumber;

    @Getter
    @Setter
    private Customer customer;

    private final Map<String, Integer> lines = new LinkedHashMap<>();

    private final ProductService productService;
    private final int maxQuantityPerProduct;

    public ShoppingCart(ProductService productService,
                        @Value("${store.max-quantity-per-product}") int maxQuantityPerProduct) {
        this.productService = productService;
        this.maxQuantityPerProduct = maxQuantityPerProduct;
    }

    public Map<String, Integer> getLines() {
        return Collections.unmodifiableMap(lines);
    }

    public void addItem(String sku, int qty) {
        if (qty <= 0) {
            throw new StoreException("Quantity must be positive.");
        }
        Product product = productService.findBySku(sku);
        if (!product.isActive()) {
            throw new StoreException(product.getName() + " isn't available.");
        }
        // CART-01
        if (product.getStockQuantity() <= 0) {
            throw new StoreException(product.getName() + " is out of stock.");
        }
        // CART-02: adding an existing product increases its quantity
        int newQty = lines.getOrDefault(sku, 0) + qty;
        // CART-03
        if (newQty > product.getStockQuantity()) {
            throw new StoreException("Cannot add " + qty + " x " + product.getName()
                    + ": only " + product.getStockQuantity() + " available.");
        }
        if (newQty > maxQuantityPerProduct) {
            throw new StoreException("Cannot add " + qty + " x " + product.getName()
                    + ": maximum " + maxQuantityPerProduct + " per product.");
        }
        lines.put(sku, newQty);
    }

    public BigDecimal subtotal() {
        BigDecimal sum = BigDecimal.ZERO;
        for (Map.Entry<String, Integer> line : lines.entrySet()) {
            sum = sum.add(productService.getPrice(line.getKey(), line.getValue()));
        }
        return Money.round(sum);
    }

    public boolean isEmpty() {
        return lines.isEmpty();
    }

    public void clear() {
        lines.clear();
    }
}
