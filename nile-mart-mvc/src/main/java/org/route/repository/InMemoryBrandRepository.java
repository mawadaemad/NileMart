package org.route.repository;

import jakarta.annotation.PostConstruct;
import org.route.model.Brand;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryBrandRepository implements BrandRepository {

    private final List<Brand> brands = new ArrayList<>();

    @PostConstruct
    void init() {
        brands.add(brand("Samsung", "samsung", true));
        brands.add(brand("Asus", "asus", true));
        brands.add(brand("Anker", "anker", true));
        brands.add(brand("Tefal", "tefal", true));
        brands.add(brand("Old Brand", "old-brand", false));
    }

    private static Brand brand(String name, String slug, boolean active) {
        return Brand.builder().name(name).slug(slug).active(active).build();
    }

    @Override
    public List<Brand> findAll() {
        return List.copyOf(brands);
    }

    @Override
    public Optional<Brand> findBySlug(String slug) {
        return brands.stream().filter(b -> b.getSlug().equals(slug)).findFirst();
    }

    @Override
    public void save(Brand brand) {
        brands.add(brand);
    }
}
