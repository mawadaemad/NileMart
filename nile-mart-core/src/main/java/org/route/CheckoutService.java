package org.route;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final ProductService productService;
    private final ShippingQuoteService shippingQuoteService;
    private final TaxCalculator taxCalculator;
    private final OrderRegistry orderRegistry;
    private final List<NotificationSender> notificationSenders;

    public Order placeOrder(ShoppingCart cart, Governorate governorate) {
        // 1. ORD-02
        if (cart.isEmpty()) {
            throw new StoreException("Cannot check out an empty cart.");
        }
        if (governorate == null) {
            throw new StoreException("A governorate is required.");
        }
        Map<String, Integer> lines = new LinkedHashMap<>(cart.getLines());

        // 2. ORD-03: re-check every line before changing anything
        for (Map.Entry<String, Integer> line : lines.entrySet()) {
            Product product = productService.findBySku(line.getKey());
            if (product.getStockQuantity() < line.getValue()) {
                throw new StoreException("Not enough stock for " + product.getName()
                        + ": only " + product.getStockQuantity() + " available.");
            }
        }

        // 3. totals
        BigDecimal subtotal = cart.subtotal();
        BigDecimal vat = taxCalculator.calculate(subtotal);
        BigDecimal shipping = shippingQuoteService.quote(governorate, subtotal);
        BigDecimal total = Money.round(subtotal.add(vat).add(shipping));

        // 4. ORD-06: all lines (already verified above)
        lines.forEach(productService::reduceStock);

        // 5. save
        Order order = new Order(orderRegistry.nextOrderNumber(), cart.getCustomer(), governorate,
                lines, subtotal, vat, shipping, total);
        orderRegistry.save(order);

        // 6. notify
        String message = "Order " + order.orderNumber() + " confirmed. Total "
                + Money.format(total) + " EGP";
        for (NotificationSender sender : notificationSenders) {
            sender.send(order.customer(), message);
        }

        // 7. clear
        cart.clear();
        return order;
    }
}
