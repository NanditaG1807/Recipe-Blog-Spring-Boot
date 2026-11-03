
package com.main.recipeblog.service;

import com.main.recipeblog.entity.Recipe;
import com.main.recipeblog.repository.RecipeRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class ChatbotService {

    private final RecipeRepository recipeRepository;
    private final OllamaService ollamaService;

    public ChatbotService(
            RecipeRepository recipeRepository,
            OllamaService ollamaService) {
        this.recipeRepository = recipeRepository;
        this.ollamaService = ollamaService;
    }

    public String chat(String question) {

        if (question == null || question.isBlank()) {
            return "Please enter a question.";
        }

        String q = question.trim();

        // First, search using the complete question.
        List<Recipe> byName =
                recipeRepository.findByNameContainingIgnoreCase(q);

        List<Recipe> byTitle =
                recipeRepository.findByTitleContainingIgnoreCase(q);

        Map<Long, Recipe> uniqueRecipes = new LinkedHashMap<>();

        Stream.concat(byName.stream(), byTitle.stream())
                .forEach(recipe ->
                        uniqueRecipes.putIfAbsent(
                                recipe.getId(), recipe));

        // If the complete question did not match, try individual words.
        if (uniqueRecipes.isEmpty()) {
            String[] words = q.split("[\\s,?!।]+");

            for (String word : words) {
                String keyword = word.trim();

                if (keyword.length() < 2) {
                    continue;
                }

                recipeRepository
                        .findByNameContainingIgnoreCase(keyword)
                        .forEach(recipe ->
                                uniqueRecipes.putIfAbsent(
                                        recipe.getId(), recipe));

                recipeRepository
                        .findByTitleContainingIgnoreCase(keyword)
                        .forEach(recipe ->
                                uniqueRecipes.putIfAbsent(
                                        recipe.getId(), recipe));

                if (uniqueRecipes.size() >= 5) {
                    break;
                }
            }
        }

        List<Recipe> recipes = uniqueRecipes.values()
                .stream()
                .limit(5)
                .toList();

        String recipeData = recipes.stream()
                .map(r -> """
                        Recipe name: %s
                        Title: %s
                        Description: %s
                        Servings: %d
                        Cooking time: %d minutes
                        Ingredients: %s
                        Cooking steps: %s
                        """.formatted(
                        safe(r.getName()),
                        safe(r.getTitle()),
                        safe(r.getDescription()),
                        r.getServings(),
                        r.getCookTime(),
                        safe(r.getIngredients()),
                        safe(r.getCookingSteps())
                ))
                .collect(Collectors.joining("\n"));

        if (recipeData.isBlank()) {
            recipeData =
                    "No matching recipe was found in the blog database.";
        }

        return ollamaService.ask(q, recipeData);
    }

    private String safe(String value) {
        return value == null ? "Not available" : value;
    }
}
