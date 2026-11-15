package org.route.controller;

import jakarta.validation.Valid;
import org.route.model.Category;
import org.route.service.CategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    public String getCategories(Model model) {
        model.addAttribute("categories", categoryService.getCategoryRows());
        return "categories";
    }

    @GetMapping("/categories/new")
    public String showForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("parents", categoryService.getTopLevelOptions());
        return "categoryForm";
    }

    @PostMapping("/categories")
    public String addCategory(@ModelAttribute("category") @Valid Category category,
                              BindingResult result, Model model) {
        categoryService.validate(category).forEach((field, message) -> {
            if (!result.hasFieldErrors(field)) {
                result.rejectValue(field, "business", message);
            }
        });
        if (result.hasErrors()) {
            model.addAttribute("parents", categoryService.getTopLevelOptions());
            return "categoryForm";
        }
        categoryService.saveCategory(category);
        return "redirect:/categories";
    }
}
