package org.route.service;

import org.route.model.Category;
import org.route.model.CategoryRow;
import org.route.repository.CategoryRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CategoryService {

    private static final Comparator<Category> BY_ORDER =
            Comparator.comparing(Category::getDisplayOrder).thenComparing(Category::getName);

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    private static boolean isTopLevel(Category category) {
        return category.getParentSlug() == null || category.getParentSlug().isBlank();
    }

    private static String normalize(String parentSlug) {
        return parentSlug == null ? "" : parentSlug.trim();
    }

    public Optional<Category> findBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    /** Active top-level categories, for the Parent drop-down. */
    public List<Category> getTopLevelOptions() {
        return categoryRepository.findAll().stream()
                .filter(c -> isTopLevel(c) && c.isActive())
                .sorted(BY_ORDER).toList();
    }

    /** Active sub-categories, for the product form (CAT-01: products go only in sub-categories). */
    public List<Category> getSubCategoryOptions() {
        return categoryRepository.findAll().stream()
                .filter(c -> !isTopLevel(c) && c.isActive())
                .sorted(BY_ORDER).toList();
    }

    /** True when a category with this slug exists and has a parent. */
    public boolean isSubCategory(String slug) {
        return categoryRepository.findBySlug(slug).map(c -> !isTopLevel(c)).orElse(false);
    }

    /** Name of the category, or the slug itself if it can't be found. */
    public String getCategoryName(String slug) {
        return categoryRepository.findBySlug(slug).map(Category::getName).orElse(slug);
    }

    /** Each top-level category followed by its sub-categories, both sorted by display order. */
    public List<CategoryRow> getCategoryRows() {
        List<Category> all = categoryRepository.findAll();
        List<CategoryRow> rows = new ArrayList<>();
        all.stream().filter(CategoryService::isTopLevel).sorted(BY_ORDER).forEach(top -> {
            rows.add(new CategoryRow(top.getName(), top.getSlug(), "—",
                    top.getDisplayOrder(), top.isActive()));
            all.stream()
                    .filter(c -> !isTopLevel(c) && c.getParentSlug().equals(top.getSlug()))
                    .sorted(BY_ORDER)
                    .forEach(sub -> rows.add(new CategoryRow("— " + sub.getName(), sub.getSlug(),
                            top.getName(), sub.getDisplayOrder(), sub.isActive())));
        });
        return rows;
    }

    /** Business-rule errors as field name to message. Empty when the category can be saved. */
    public Map<String, String> validate(Category category) {
        Map<String, String> errors = new LinkedHashMap<>();
        String name = category.getName() == null ? "" : category.getName().trim();
        String slug = category.getSlug() == null ? "" : category.getSlug().trim();
        String parentSlug = normalize(category.getParentSlug());

        // CAT-01: the parent must exist and be a top-level category
        Optional<Category> parent = Optional.empty();
        boolean parentOk = true;
        if (!parentSlug.isEmpty()) {
            parent = categoryRepository.findBySlug(parentSlug);
            if (parent.isEmpty()) {
                errors.put("parentSlug", "Parent category does not exist");
                parentOk = false;
            } else if (!isTopLevel(parent.get())) {
                errors.put("parentSlug", "Parent must be a top-level category");
                parentOk = false;
            }
        }

        // name is unique within the same parent, ignoring case
        if (!name.isEmpty() && parentOk && categoryRepository.findAll().stream()
                .anyMatch(c -> normalize(c.getParentSlug()).equals(parentSlug)
                        && c.getName().equalsIgnoreCase(name))) {
            errors.put("name", parent.isPresent()
                    ? "A category with this name already exists under " + parent.get().getName()
                    : "A top-level category with this name already exists");
        }

        // slug is unique across all categories
        if (!slug.isEmpty() && categoryRepository.findBySlug(slug).isPresent()) {
            errors.put("slug", "Slug already exists");
        }
        return errors;
    }

    public void saveCategory(Category category) {
        category.setName(category.getName().trim());
        category.setSlug(category.getSlug().trim());
        category.setParentSlug(normalize(category.getParentSlug()));
        categoryRepository.save(category);
    }
}
