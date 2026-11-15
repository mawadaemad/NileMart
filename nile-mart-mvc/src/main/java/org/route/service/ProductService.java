package org.route.service;

import org.route.model.Product;
import org.route.model.ProductRow;
import org.route.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final BrandService brandService;

    public ProductService(ProductRepository productRepository,
                          CategoryService categoryService,
                          BrandService brandService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.brandService = brandService;
    }

    public List<Product> getAvailableProducts() {
        List<Product> availableProducts = new ArrayList<>();
        for (Product p : productRepository.findAll()) {
            if (p.getInStock() != null && p.getInStock() > 0)
                availableProducts.add(p);
        }
        return availableProducts;
    }

    /** Available products with the category and brand names looked up ("—" when there is no brand). */
    public List<ProductRow> getAvailableProductRows() {
        return getAvailableProducts().stream()
                .map(p -> new ProductRow(p.getSku(), p.getName(), p.getPrice(), p.getInStock(),
                        categoryService.getCategoryName(p.getCategory()),
                        hasBrand(p) ? brandService.getBrandName(p.getBrandSlug()) : "—"))
                .toList();
    }

    private static boolean hasBrand(Product product) {
        return product.getBrandSlug() != null && !product.getBrandSlug().isBlank();
    }

    /** Business-rule errors as field name to message. Empty when the product can be saved. */
    public Map<String, String> validate(Product product) {
        Map<String, String> errors = new LinkedHashMap<>();
        if (product.getSku() != null && !product.getSku().isBlank() && isProductExist(product.getSku())) {
            errors.put("sku", "sku is already exist");
        }
        // CAT-01: a product goes in a sub-category
        if (product.getCategory() != null && !product.getCategory().isBlank()
                && !categoryService.isSubCategory(product.getCategory())) {
            errors.put("category", "Category must be an existing sub-category");
        }
        // the brand is optional, but when given it must exist and be active
        if (hasBrand(product) && !brandService.isActiveBrand(product.getBrandSlug())) {
            errors.put("brandSlug", "Brand does not exist or is inactive");
        }
        return errors;
    }

    public void saveProduct(Product product) {
        if (!hasBrand(product)) {
            product.setBrandSlug(null);
        }
        productRepository.save(product);
    }

    public boolean isProductExist(String sku) {
        return productRepository.findBySku(sku) != null;
    }
}
