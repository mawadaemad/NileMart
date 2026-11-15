package org.route.repository;

import jakarta.annotation.PostConstruct;
import org.route.model.Category;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryCategoryRepository implements CategoryRepository {

    private final List<Category> categories = new ArrayList<>();

    @PostConstruct
    void init() {
        categories.add(category("Electronics", "electronics", "", 1, true));
        categories.add(category("Mobile Phones", "mobile-phones", "electronics", 1, true));
        categories.add(category("Laptops", "laptops", "electronics", 2, true));
        categories.add(category("Accessories", "electronics-accessories", "electronics", 3, true));
        categories.add(category("Home & Kitchen", "home-kitchen", "", 2, true));
        categories.add(category("Cookware", "cookware", "home-kitchen", 1, true));
        categories.add(category("Lighting", "lighting", "home-kitchen", 2, true));
        categories.add(category("Fashion", "fashion", "", 3, false));
        categories.add(category("Men’s Clothing", "mens-clothing", "fashion", 1, true));
    }

    private static Category category(String name, String slug, String parentSlug, int order, boolean active) {
        return Category.builder().name(name).slug(slug).parentSlug(parentSlug)
                .displayOrder(order).active(active).build();
    }

    @Override
    public List<Category> findAll() {
        return List.copyOf(categories);
    }

    @Override
    public Optional<Category> findBySlug(String slug) {
        return categories.stream().filter(c -> c.getSlug().equals(slug)).findFirst();
    }

    @Override
    public void save(Category category) {
        categories.add(category);
    }
}
