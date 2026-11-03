package com.main.recipeblog.controller;

import com.main.recipeblog.entity.Category;
import com.main.recipeblog.entity.Recipe;
import com.main.recipeblog.entity.Review;
import com.main.recipeblog.repository.CategoryRepository;
import com.main.recipeblog.repository.RecipeRepository;

import com.main.recipeblog.repository.ReviewRepository;
import com.main.recipeblog.service.WishlistService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Controller
public class RecipeController {

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final WishlistService wishlistService;

    public RecipeController(
            RecipeRepository recipeRepository,
            CategoryRepository categoryRepository,
            ReviewRepository reviewRepository,
            WishlistService wishlistService) {

        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
        this.reviewRepository = reviewRepository;
        this.wishlistService = wishlistService;
    }



    // =========================
    // SOCIAL MEDIA
    // =========================

    @GetMapping("/facebook")
    public String facebook() {
        return "redirect:https://www.facebook.com/login.php/";
    }

    @GetMapping("/twitter")
    public String twitter() {
        return "redirect:https://x.com/i/flow/login";
    }

    @GetMapping("/linkedin")
    public String linkedin() {
        return "redirect:https://www.linkedin.com/uas/login";
    }

    @GetMapping("/youtube")
    public String youtube() {
        return "redirect:https://www.youtube.com/";
    }

    // =========================
    // HOME
    // =========================

    @GetMapping("/")
    public String viewHomePage() {
        return "index";
    }

    // =========================
    // FRONT PAGE
    // =========================

    @GetMapping("/front-page")
    public String frontPage(Model model) {

        // फक्त MAIN categories दाखवा
        model.addAttribute(
                "mainCategories",
                categoryRepository.findByCategoryType("MAIN")
        );

        model.addAttribute(
                "featuredRecipes",
                recipeRepository.findByCardType("FEATURED")
        );

        model.addAttribute(
                "smallRecipes",
                recipeRepository.findByCardType("SMALL")
        );

        model.addAttribute(
                "savedRecipeIds",
                wishlistService.getWishlistRecipes()
                        .stream()
                        .map(Recipe::getId)
                        .collect(Collectors.toSet())
        );

        return "front-page";
    }







// LIKE RECIPE
// ========================
@PostMapping("/recipe/{id}/like")
@ResponseBody
public Map<String, Object> likeRecipe(@PathVariable Long id) {

    Recipe recipe = recipeRepository.findById(id)
            .orElseThrow(() ->
                    new RuntimeException("Recipe not found: " + id));

    recipe.setLikes(recipe.getLikes() + 1);
    recipeRepository.save(recipe);

    Map<String, Object> response = new HashMap<>();
    response.put("likes", recipe.getLikes());

    return response;
}
    // =========================
    // OTHER PAGES
    // =========================
    @GetMapping("/categories")
    public String categories(Model model) {

        model.addAttribute(
                "categories",
                categoryRepository.findByCategoryType("SUB")
        );

        return "categories";
    }

    // =========================
// RECIPES BY CATEGORY
// =========================

    @GetMapping("/recipes/category/{name}")
    public String recipesByCategory(
            @PathVariable String name,
            Model model) {

        Category category = categoryRepository.findByName(name)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Category not found: " + name
                        ));

        List<Recipe> recipes =
                recipeRepository.findByCategories(category);

        model.addAttribute("recipes", recipes);
        model.addAttribute("categoryName", category.getName());

        // हा नवीन attribute जोडा
        model.addAttribute("categoryId", category.getId());

        return "category-recipes";
    }


    @GetMapping("/contact-us")
    public String contactUs() {
        return "contact-us";
    }

    @GetMapping("/about-us")
    public String aboutUs() {
        return "about-us";
    }

    @GetMapping("/categories/breakfast")
    public String breakfast(Model model) {

        Category category = categoryRepository.findByName("Breakfast")
                .orElseThrow(() ->
                        new RuntimeException("Breakfast category not found"));

        List<Recipe> recipes =
                recipeRepository.findDistinctByCategories_ParentCategories_Id(
                        category.getId()
                );

        model.addAttribute("recipes", recipes);
        model.addAttribute("categoryName", category.getName());

        return "breakfast";
    }





    @GetMapping("/categories/lunch")
    public String lunch(Model model) {

        Category category = categoryRepository.findByName("Lunch")
                .orElseThrow(() ->
                        new RuntimeException("Lunch category not found"));

        List<Recipe> recipes =
                recipeRepository.findDistinctByCategories_ParentCategories_Id(
                        category.getId()
                );

        model.addAttribute("recipes", recipes);
        model.addAttribute("categoryName", category.getName());

        return "lunch";
    }




    @GetMapping("/categories/dinner")
    public String dinner(Model model) {

        Category category = categoryRepository.findByName("Dinner")
                .orElseThrow(() ->
                        new RuntimeException("Dinner category not found"));

        List<Recipe> recipes =
                recipeRepository.findDistinctByCategories_ParentCategories_Id(
                        category.getId()
                );

        model.addAttribute("recipes", recipes);
        model.addAttribute("categoryName", category.getName());

        return "dinner";
    }



    //============================
    //search-box
    //============================
    @GetMapping("/api/search")
    @ResponseBody
    public List<Recipe> liveSearch(@RequestParam String keyword) {

        return recipeRepository.findByNameContainingIgnoreCase(keyword);
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String keyword,
            Model model) {

        List<Recipe> recipes;

        if (keyword == null || keyword.isBlank()) {
            recipes = recipeRepository.findAll();
        } else {
            recipes = recipeRepository
                    .findByNameContainingIgnoreCase(keyword.trim());
        }

        model.addAttribute("recipes", recipes);
        model.addAttribute("keyword", keyword);

        return "search";
    }


// =============================
// REVIEW PAGE
// =============================

    @GetMapping("/review")
    public String reviewPage(Model model) {

        model.addAttribute(
                "reviews",
                reviewRepository.findAll()
        );

        return "review";
    }


// =============================
// RECIPE DETAILS
// =============================

    @GetMapping("/recipe/{id}")
    public String recipeDetails(
            @PathVariable Long id,
            Model model) {

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Recipe not found: " + id));

        List<Review> reviews =
                reviewRepository.findByRecipe(recipe);

        model.addAttribute("recipe", recipe);
        model.addAttribute("reviews", reviews);
        model.addAttribute("review", new Review());

        return "recipe-details";
    }


// =============================
// SUBMIT REVIEW
// =============================

    @PostMapping("/recipe/{id}/review")
    public String submitReview(
            @PathVariable Long id,
            @ModelAttribute Review review) {

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Recipe not found: " + id));

        review.setRecipe(recipe);

        reviewRepository.save(review);

        return "redirect:/recipe/" + id;
    }



}
