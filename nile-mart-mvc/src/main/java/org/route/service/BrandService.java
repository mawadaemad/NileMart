package org.route.service;

import org.route.model.Brand;
import org.route.repository.BrandRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public List<Brand> getAllBrands() {
        return brandRepository.findAll();
    }

    /** Only active brands are offered in new choices (the product form). */
    public List<Brand> getActiveBrands() {
        return brandRepository.findAll().stream().filter(Brand::isActive).toList();
    }

    public Optional<Brand> findBySlug(String slug) {
        return brandRepository.findBySlug(slug);
    }

    public boolean isActiveBrand(String slug) {
        return brandRepository.findBySlug(slug).map(Brand::isActive).orElse(false);
    }

    /** Name of the brand, or the slug itself if the brand can't be found. */
    public String getBrandName(String slug) {
        return brandRepository.findBySlug(slug).map(Brand::getName).orElse(slug);
    }

    /** Business-rule errors as field name to message. Empty when the brand can be saved. */
    public Map<String, String> validate(Brand brand) {
        Map<String, String> errors = new LinkedHashMap<>();
        String name = brand.getName() == null ? "" : brand.getName().trim();
        String slug = brand.getSlug() == null ? "" : brand.getSlug().trim();

        if (!name.isEmpty() && brandRepository.findAll().stream()
                .anyMatch(b -> b.getName().equalsIgnoreCase(name))) {
            errors.put("name", "Brand name already exists");
        }
        if (!slug.isEmpty() && brandRepository.findAll().stream()
                .anyMatch(b -> b.getSlug().equals(slug))) {
            errors.put("slug", "Slug already exists");
        }
        return errors;
    }

    public void saveBrand(Brand brand) {
        brand.setName(brand.getName().trim());
        brand.setSlug(brand.getSlug().trim());
        brandRepository.save(brand);
    }
}
