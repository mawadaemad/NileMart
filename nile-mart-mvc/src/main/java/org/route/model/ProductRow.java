package org.route.model;

import java.math.BigDecimal;

/** One line of the products table, with the category and brand names already looked up. */
public record ProductRow(String sku, String name, BigDecimal price, Integer inStock,
                         String categoryName, String brandName) {
}
