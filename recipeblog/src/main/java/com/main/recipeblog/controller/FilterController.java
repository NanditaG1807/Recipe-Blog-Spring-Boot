package com.main.recipeblog.controller;

import com.main.recipeblog.entity.Recipe;
import com.main.recipeblog.repository.RecipeRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FilterController {

    private final RecipeRepository recipeRepository;

    public FilterController(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }

    @GetMapping("/recipes/filter")
    public String filterController(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Integer time,
            @RequestParam(required = false) String type,
            Model model) {

        List<Recipe> allRecipes = recipeRepository.findAll();

        List<Recipe> filteredRecipes = allRecipes.stream()

                // 1. Search by recipe name or title
                .filter(recipe -> {
                    if (search == null || search.isBlank()) {
                        return true;
                    }

                    String keyword = search.trim().toLowerCase();

                    String name = recipe.getName() == null
                            ? ""
                            : recipe.getName().toLowerCase();

                    String title = recipe.getTitle() == null
                            ? ""
                            : recipe.getTitle().toLowerCase();

                    return name.contains(keyword)
                            || title.contains(keyword);
                })

                // 2. Filter by cooking time
                .filter(recipe -> {
                    if (time == null || time <= 0) {
                        return true;
                    }

                    return recipe.getCookTime() <= time;
                })

                // 3. Filter by food preference
                // 3. Filter by food preference
                .filter(recipe -> {
                    if (type == null || type.isBlank()) {
                        return true;
                    }

                    String foodType = recipe.getFoodType();

                    if (foodType == null) {
                        return false;
                    }

                    return foodType.trim().equalsIgnoreCase(type.trim());
                })

                .collect(Collectors.toList());

        model.addAttribute("recipes", filteredRecipes);
        model.addAttribute("search", search);
        model.addAttribute("keyword", search);
        model.addAttribute("selectedTime", time);
        model.addAttribute("selectedType", type);

        return "search";
    }
}
