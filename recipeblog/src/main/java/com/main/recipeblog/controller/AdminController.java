package com.main.recipeblog.controller;

import com.main.recipeblog.entity.Category;
import com.main.recipeblog.entity.Recipe;
import com.main.recipeblog.repository.CategoryRepository;
import com.main.recipeblog.repository.RecipeRepository;

import com.main.recipeblog.repository.ReviewRepository;
import com.main.recipeblog.repository.WishlistRepository;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin")
public class AdminController {

    private final RecipeRepository recipeRepository;
    private final CategoryRepository categoryRepository;
    private final ReviewRepository reviewRepository;
    private final WishlistRepository wishlistRepository;
    public AdminController(
            RecipeRepository recipeRepository,
            CategoryRepository categoryRepository,
            ReviewRepository reviewRepository,
            WishlistRepository wishlistRepository) {

        this.recipeRepository = recipeRepository;
        this.categoryRepository = categoryRepository;
        this.reviewRepository = reviewRepository;
        this.wishlistRepository = wishlistRepository;
    }

    // =========================
// ADMIN DASHBOARD
// =========================

    @GetMapping
    public String adminPage(Model model) {

        model.addAttribute(
                "featuredRecipes",
                recipeRepository.findByCardType("FEATURED")
        );

        model.addAttribute(
                "smallRecipes",
                recipeRepository.findByCardType("SMALL")
        );

        model.addAttribute(
                "normalRecipes",
                recipeRepository.findByCardType("RECIPE")
        );

        model.addAttribute(
                "categories",
                categoryRepository.findAll()
        );

        return "admin/admin";
    }

    // =========================
    // ADD RECIPE PAGE
    // =========================

    @GetMapping("/add")
    public String addRecipe(Model model) {

        Recipe recipe = new Recipe();
        recipe.setCardType("FEATURED");

        model.addAttribute("recipe", recipe);

        model.addAttribute(
                "mainCategories",
                categoryRepository.findByCategoryType("MAIN")
        );

        model.addAttribute(
                "subCategories",
                categoryRepository.findByCategoryType("SUB")
        );

        return "admin/add-feactured";
    }




    // =========================
    // SAVE RECIPE
    // =========================

    @PostMapping("/save")
    public String saveRecipe(
            @ModelAttribute("recipe") Recipe recipe,
            @RequestParam(value = "categoryIds", required = false)
            List<Long> categoryIds,
            @RequestParam(value = "imageFile", required = false)
            MultipartFile imageFile)
            throws IOException {

        System.out.println("========== SAVE RECIPE ==========");
        System.out.println("Title : " + recipe.getTitle());
        System.out.println("Name  : " + recipe.getName());
        System.out.println("Categories : " + categoryIds);

        // =========================
        // CATEGORIES
        // =========================

        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new RuntimeException("At least one category must be selected");
        }

        Set<Category> categories = new HashSet<>();

        for (Long categoryId : categoryIds) {

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Category not found: " + categoryId
                            ));

            categories.add(category);
        }

        recipe.setCategories(categories);

        // =========================
        // CARD TYPE
        // =========================

        recipe.setCardType("FEATURED");

        // =========================
        // NORMALIZE
        // =========================

        recipe.setCookingSteps(
                normalizeSteps(recipe.getCookingSteps())
        );

        recipe.setIngredients(
                normalizeSteps(recipe.getIngredients())
        );

        // =========================
        // IMAGE
        // =========================

        if (imageFile != null && !imageFile.isEmpty()) {

            String fileName = imageFile.getOriginalFilename();

            Path uploadPath =
                    Paths.get("src/main/resources/static/img");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath =
                    uploadPath.resolve(fileName);

            Files.copy(
                    imageFile.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            recipe.setImage(fileName);
        }

        // FOOD TYPE VALIDATION
        if (!"VEG".equals(recipe.getFoodType())
                && !"NON_VEG".equals(recipe.getFoodType())) {

            throw new IllegalArgumentException(
                    "Please select Veg or Non-Veg"
            );
        }

// DEFAULT LIKES
        recipe.setLikes(0);

// SAVE RECIPE
        recipeRepository.save(recipe);

        System.out.println("========== RECIPE SAVED ==========");

        return "redirect:/admin";
    }




    private String normalizeSteps(String steps) {

        if (steps == null) {
            return "";
        }

        return steps
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[ \t]+\n", "\n")
                .replaceAll("\n{2,}", "\n")
                .trim();
    }



    // =========================
    // EDIT RECIPE PAGE
    // =========================

    @GetMapping("/edit/{id}")
    public String editRecipe(
            @PathVariable Long id,
            Model model) {

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Recipe not found with id: " + id
                        ));

        model.addAttribute("recipe", recipe);

        // Edit page साठी categories आवश्यक आहेत
        model.addAttribute(
                "categories",
                categoryRepository.findAll()
        );

        return "admin/edit-feactured";
    }


    // =========================
    // UPDATE RECIPE
    // =========================

    @PostMapping("/update")
    public String updateRecipe(
            @ModelAttribute("recipe") Recipe recipe,

            @RequestParam(value = "categoryIds", required = false)
            List<Long> categoryIds,

            @RequestParam(value = "imageFile", required = false)
            MultipartFile imageFile)
            throws IOException {

        System.out.println("========== UPDATE RECIPE ==========");
        System.out.println("Recipe ID    : " + recipe.getId());
        System.out.println("Category IDs : " + categoryIds);

        Recipe existingRecipe =
                recipeRepository.findById(recipe.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Recipe not found: " + recipe.getId()
                                ));

        // =========================
        // TEXT FIELDS
        // =========================

        existingRecipe.setTitle(recipe.getTitle());
        existingRecipe.setName(recipe.getName());
        existingRecipe.setDescription(recipe.getDescription());

        // =========================
// FOOD TYPE VALIDATION
// =========================

        if (!"VEG".equals(recipe.getFoodType())
                && !"NON_VEG".equals(recipe.getFoodType())) {

            throw new IllegalArgumentException(
                    "Please select Veg or Non-Veg"
            );
        }

        existingRecipe.setFoodType(recipe.getFoodType());
        existingRecipe.setIngredients(
                normalizeSteps(recipe.getIngredients())
        );

        existingRecipe.setCookingSteps(
                normalizeSteps(recipe.getCookingSteps())
        );

        existingRecipe.setServings(recipe.getServings());
        existingRecipe.setCookTime(recipe.getCookTime());

        // =========================
        // MULTIPLE CATEGORIES
        // =========================

        if (categoryIds != null && !categoryIds.isEmpty()) {

            Set<Category> categories = new HashSet<>();

            for (Long categoryId : categoryIds) {

                Category category =
                        categoryRepository.findById(categoryId)
                                .orElseThrow(() ->
                                        new RuntimeException(
                                                "Category not found: " + categoryId
                                        ));

                categories.add(category);
            }

            existingRecipe.setCategories(categories);
        }

        // =========================
        // IMAGE
        // =========================

        if (imageFile != null && !imageFile.isEmpty()) {
            saveImage(existingRecipe, imageFile);
        }

        // =========================
        // SAVE
        // =========================

        recipeRepository.save(existingRecipe);

        System.out.println("========== RECIPE UPDATED ==========");

        return "redirect:/admin?updated=true";
    }




    // =========================
    // DELETE RECIPE
    // =========================

    @GetMapping("/delete/{id}")
    @Transactional
    public String deleteRecipe(@PathVariable Long id) {

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Recipe not found: " + id
                        ));

        // Step 1: Delete wishlist records first
        wishlistRepository.deleteAllByRecipe_Id(id);

        // Step 2: Delete reviews
        reviewRepository.deleteReviewsByRecipeId(id);

        // Step 3: Remove category relationships
        if (recipe.getCategories() != null) {
            recipe.getCategories().clear();
        }

        recipeRepository.flush();

        // Step 4: Delete recipe
        recipeRepository.delete(recipe);

        recipeRepository.flush();

        return "redirect:/admin?deleted=true";
    }

    // =========================
    // IMAGE SAVE METHOD
    // =========================

    private void saveImage(
            Recipe recipe,
            MultipartFile imageFile)
            throws IOException {

        if (imageFile != null && !imageFile.isEmpty()) {

            String fileName =
                    imageFile.getOriginalFilename();

            Path uploadPath =
                    Paths.get("src/main/resources/static/img");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath =
                    uploadPath.resolve(fileName);

            Files.write(
                    filePath,
                    imageFile.getBytes()
            );

            recipe.setImage(fileName);
        }
    }



    // =============================
// SMALL CARD
// =============================
    @GetMapping("/small-card")
    public String smallCard(
            @RequestParam(value = "categoryId", required = false)
            Long categoryId,
            Model model) {

        Recipe recipe = new Recipe();
        recipe.setCardType("SMALL");

        model.addAttribute("recipe", recipe);

        model.addAttribute(
                "mainCategories",
                categoryRepository.findByCategoryType("MAIN")
        );

        model.addAttribute(
                "subCategories",
                categoryRepository.findByCategoryType("SUB")
        );

        // जर एखाद्या category मधून page उघडले असेल
        model.addAttribute("categoryId", categoryId);

        return "admin/add-small-card";
    }
    @PostMapping("/save-small-card")
    public String saveSmallCard(
            @ModelAttribute("recipe") Recipe recipe,

            @RequestParam(value = "categoryIds", required = false)
            List<Long> categoryIds,

            @RequestParam(value = "imageFile", required = false)
            MultipartFile imageFile)
            throws IOException {

        // 1. Card Type
        recipe.setCardType("SMALL");

        // 2. FOOD TYPE VALIDATION - इथे add करा
        if (!"VEG".equals(recipe.getFoodType())
                && !"NON_VEG".equals(recipe.getFoodType())) {

            throw new IllegalArgumentException(
                    "Please select Veg or Non-Veg"
            );
        }

        // 3. Validate Categories
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new RuntimeException(
                    "At least one category must be selected"
            );
        }

        // 4. Multiple Categories
        Set<Category> categories = new HashSet<>();

        for (Long categoryId : categoryIds) {

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Category not found: " + categoryId
                            ));

            categories.add(category);
        }

        recipe.setCategories(categories);

        // 5. Normalize Ingredients and Cooking Steps
        recipe.setIngredients(
                normalizeSteps(recipe.getIngredients())
        );

        recipe.setCookingSteps(
                normalizeSteps(recipe.getCookingSteps())
        );

        // 6. Save Image
        if (imageFile != null && !imageFile.isEmpty()) {
            saveImage(recipe, imageFile);
        }

        // 7. Default Likes
        recipe.setLikes(0);

        // 8. Save Recipe
        recipeRepository.save(recipe);

        return "redirect:/admin";
    }
    @GetMapping("/edit-small-card/{id}")
    public String editSmallCard(
            @PathVariable Long id,
            Model model) {

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Recipe not found: " + id
                        ));

        // फक्त SMALL card edit करता येईल
        if (!"SMALL".equals(recipe.getCardType())) {
            throw new IllegalArgumentException(
                    "This recipe is not a Small Card"
            );
        }

        model.addAttribute("recipe", recipe);

        model.addAttribute(
                "mainCategories",
                categoryRepository.findByCategoryType("MAIN")
        );

        model.addAttribute(
                "subCategories",
                categoryRepository.findByCategoryType("SUB")
        );

        return "admin/edit-small-card";
    }

    @PostMapping("/update-small-card")
    public String updateSmallCard(
            @ModelAttribute("recipe") Recipe recipe,
            @RequestParam(value = "categoryIds", required = false)
            List<Long> categoryIds,
            @RequestParam(value = "imageFile", required = false)
            MultipartFile imageFile)
            throws IOException {

        Recipe existingRecipe =
                recipeRepository.findById(recipe.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Recipe not found: "
                                                + recipe.getId()
                                ));

        if (!"SMALL".equals(existingRecipe.getCardType())) {
            throw new IllegalArgumentException(
                    "This recipe is not a Small Card"
            );
        }

        // Text fields update
        existingRecipe.setTitle(recipe.getTitle());
        existingRecipe.setName(recipe.getName());
        existingRecipe.setDescription(recipe.getDescription());
// FOOD TYPE UPDATE
        if (!"VEG".equals(recipe.getFoodType())
                && !"NON_VEG".equals(recipe.getFoodType())) {

            throw new IllegalArgumentException(
                    "Please select Veg or Non-Veg"
            );
        }

        existingRecipe.setFoodType(recipe.getFoodType());
        existingRecipe.setServings(recipe.getServings());
        existingRecipe.setCookTime(recipe.getCookTime());

        existingRecipe.setIngredients(
                normalizeSteps(recipe.getIngredients())
        );

        existingRecipe.setCookingSteps(
                normalizeSteps(recipe.getCookingSteps())
        );

        // Categories update
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one category must be selected"
            );
        }

        Set<Category> categories = new HashSet<>();

        for (Long categoryId : categoryIds) {
            Category category =
                    categoryRepository.findById(categoryId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Category not found: "
                                                    + categoryId
                                    ));

            categories.add(category);
        }

        existingRecipe.setCategories(categories);

        // नवीन image निवडली असेल तरच update कर
        if (imageFile != null && !imageFile.isEmpty()) {
            saveImage(existingRecipe, imageFile);
        }

        recipeRepository.save(existingRecipe);

        return "redirect:/admin?smallCardUpdated=true";
    }


    @GetMapping("/add-recipe")
    public String addNormalRecipe(Model model) {

        Recipe recipe = new Recipe();
        recipe.setCardType("RECIPE");

        model.addAttribute("recipe", recipe);

        model.addAttribute(
                "mainCategories",
                categoryRepository.findByCategoryType("MAIN")
        );

        model.addAttribute(
                "subCategories",
                categoryRepository.findByCategoryType("SUB")
        );

        return "admin/add-recipe";
    }


    //save recipe
    @PostMapping("/save-recipe")
    public String saveNormalRecipe(
            @ModelAttribute("recipe") Recipe recipe,
            @RequestParam(value = "categoryIds", required = false)
            List<Long> categoryIds,
            @RequestParam(value = "imageFile", required = false)
            MultipartFile imageFile) throws IOException {

        // CATEGORY VALIDATION
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one category must be selected"
            );
        }

        Set<Category> categories = new HashSet<>();

        for (Long categoryId : categoryIds) {
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Category not found: " + categoryId
                            ));

            categories.add(category);
        }

        recipe.setCategories(categories);
        recipe.setCardType("RECIPE");

        // FOOD TYPE VALIDATION
        if (!"VEG".equals(recipe.getFoodType())
                && !"NON_VEG".equals(recipe.getFoodType())) {

            throw new IllegalArgumentException(
                    "Please select Veg or Non-Veg"
            );
        }

        // NORMALIZE INGREDIENTS
        recipe.setIngredients(
                normalizeSteps(recipe.getIngredients())
        );

        // NORMALIZE COOKING STEPS
        recipe.setCookingSteps(
                normalizeSteps(recipe.getCookingSteps())
        );

        // SAVE IMAGE
        if (imageFile != null && !imageFile.isEmpty()) {
            saveImage(recipe, imageFile);
        }

        // DEFAULT LIKES
        recipe.setLikes(0);

        // SAVE RECIPE
        recipeRepository.save(recipe);

        return "redirect:/admin";
    }


    @GetMapping("/edit-recipe/{id}")
    public String editNormalRecipe(
            @PathVariable Long id,
            Model model) {

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Recipe not found: " + id
                        ));

        if (!"RECIPE".equals(recipe.getCardType())) {
            throw new IllegalArgumentException(
                    "This is not a normal recipe"
            );
        }

        model.addAttribute("recipe", recipe);

        model.addAttribute(
                "mainCategories",
                categoryRepository.findByCategoryType("MAIN")
        );

        model.addAttribute(
                "subCategories",
                categoryRepository.findByCategoryType("SUB")
        );

        return "admin/edit-recipe";
    }

    @PostMapping("/update-recipe")
    public String updateNormalRecipe(
            @ModelAttribute("recipe") Recipe recipe,
            @RequestParam(value = "categoryIds", required = false)
            List<Long> categoryIds,
            @RequestParam(value = "imageFile", required = false)
            MultipartFile imageFile)
            throws IOException {

        Recipe existingRecipe =
                recipeRepository.findById(recipe.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Recipe not found: " + recipe.getId()
                                ));

        if (!"RECIPE".equals(existingRecipe.getCardType())) {
            throw new IllegalArgumentException(
                    "This is not a normal recipe"
            );
        }

        existingRecipe.setTitle(recipe.getTitle());
        existingRecipe.setName(recipe.getName());
        existingRecipe.setDescription(recipe.getDescription());
// =========================
// FOOD TYPE VALIDATION
// =========================

        if (!"VEG".equals(recipe.getFoodType())
                && !"NON_VEG".equals(recipe.getFoodType())) {

            throw new IllegalArgumentException(
                    "Please select Veg or Non-Veg"
            );
        }

// FOOD TYPE UPDATE
        existingRecipe.setFoodType(recipe.getFoodType());
        existingRecipe.setServings(recipe.getServings());
        existingRecipe.setCookTime(recipe.getCookTime());

        existingRecipe.setIngredients(
                normalizeSteps(recipe.getIngredients())
        );

        existingRecipe.setCookingSteps(
                normalizeSteps(recipe.getCookingSteps())
        );

        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one category must be selected"
            );
        }

        Set<Category> categories = new HashSet<>();

        for (Long categoryId : categoryIds) {
            Category category =
                    categoryRepository.findById(categoryId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Category not found: " + categoryId
                                    ));

            categories.add(category);
        }

        existingRecipe.setCategories(categories);

        if (imageFile != null && !imageFile.isEmpty()) {
            saveImage(existingRecipe, imageFile);
        }

        recipeRepository.save(existingRecipe);

        return "redirect:/admin?recipeUpdated=true";
    }
// ========================
// CATEGORY
// ========================
// ========================
// ADD CATEGORY PAGE
// ========================

    @GetMapping("/categories/add")
    public String addCategory(Model model) {

        model.addAttribute("category", new Category());

        model.addAttribute(
                "parentCategories",
                categoryRepository.findByCategoryType("MAIN")
        );

        return "admin/add-category";
    }


    @PostMapping("/categories/save")
    @Transactional
    public String saveCategory(
            @ModelAttribute("category") Category category,
            @RequestParam("categoryType") String categoryType,
            @RequestParam(value = "parentIds", required = false)
            List<Long> parentIds,
            @RequestParam(value = "imageFile", required = false)
            MultipartFile imageFile) throws IOException {

        if (!"MAIN".equals(categoryType)
                && !"SUB".equals(categoryType)) {
            throw new IllegalArgumentException("Invalid category type");
        }

        category.setCategoryType(categoryType);

        // Parent category is optional
        Set<Category> selectedParents = new HashSet<>();

        if ("SUB".equals(categoryType)
                && parentIds != null
                && !parentIds.isEmpty()) {

            for (Long parentId : parentIds) {
                Category parent = categoryRepository.findById(parentId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Parent category not found: " + parentId
                                ));

                if (!"MAIN".equals(parent.getCategoryType())) {
                    throw new IllegalArgumentException(
                            "Only MAIN categories can be parents"
                    );
                }

                selectedParents.add(parent);
            }
        }

        category.setParentCategories(selectedParents);

        // Save image using original file name
        if (imageFile != null && !imageFile.isEmpty()) {

            String fileName = Paths
                    .get(imageFile.getOriginalFilename())
                    .getFileName()
                    .toString();

            Path uploadPath =
                    Paths.get("src/main/resources/static/img");

            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(
                    imageFile.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            category.setImage(fileName);
        }
        categoryRepository.save(category);

        if ("MAIN".equals(categoryType)) {
            return "redirect:/front-page";
        }

        return "redirect:/categories";
    }

    @GetMapping("/categories/delete/{id}")
    @Transactional
    public String deleteCategory(@PathVariable Long id) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Category not found: " + id));

        // Find subcategories linked to this parent
        List<Category> subCategories =
                categoryRepository.findByParentCategoriesContaining(category);

        // Do not delete the last parent of any subcategory
        if ("MAIN".equals(category.getCategoryType())) {

            boolean hasOnlyThisParent = subCategories.stream()
                    .anyMatch(sub -> sub.getParentCategories().size() <= 1);

            if (hasOnlyThisParent) {
                return "redirect:/admin?error=subcategories";
            }

            // Remove this parent only; preserve other parent mappings
            for (Category subCategory : subCategories) {
                subCategory.getParentCategories().remove(category);
            }

            categoryRepository.saveAll(subCategories);
        }

        // Remove this category from all recipes
        List<Recipe> recipes =
                recipeRepository.findByCategoriesContaining(category);

        for (Recipe recipe : recipes) {
            recipe.getCategories().remove(category);
        }

        recipeRepository.saveAll(recipes);

        // Delete the category
        categoryRepository.delete(category);

        return "redirect:/admin?categoryDeleted=true";
    }




    // ========================
// EDIT CATEGORY
// ========================

    @GetMapping("/categories/edit/{id}")
    public String editCategory(
            @PathVariable Long id,
            Model model) {

        Category category = categoryRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Category not found: " + id
                        ));

        model.addAttribute("category", category);

        model.addAttribute(
                "parentCategories",
                categoryRepository.findByCategoryType("MAIN")
        );

        model.addAttribute(
                "selectedParentIds",
                category.getParentCategories()
                        .stream()
                        .map(Category::getId)
                        .collect(java.util.stream.Collectors.toSet())
        );

        return "admin/edit-category";
    }

    // ========================
// UPDATE CATEGORY
// ========================


    @PostMapping("/categories/update")
    @Transactional
    public String updateCategory(


            @ModelAttribute("category") Category category,
            @RequestParam(value = "parentIds", required = false)
            List<Long> parentIds,
            @RequestParam(value = "categoryType", required = false)
            String categoryType,
            @RequestParam(value = "imageFile", required = false)
            MultipartFile imageFile) throws IOException {

        Category existingCategory =
                categoryRepository.findById(category.getId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Category not found: " + category.getId()
                                ));

        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());

        String finalType =
                (categoryType == null || categoryType.isBlank())
                        ? existingCategory.getCategoryType()
                        : categoryType;

        if (!"MAIN".equals(finalType)
                && !"SUB".equals(finalType)) {
            throw new IllegalArgumentException("Invalid category type");
        }

        existingCategory.setCategoryType(finalType);

        // Parent category is optional
        Set<Category> selectedParents = new HashSet<>();

        if ("SUB".equals(finalType)
                && parentIds != null
                && !parentIds.isEmpty()) {

            for (Long parentId : parentIds) {

                if (parentId.equals(existingCategory.getId())) {
                    throw new IllegalArgumentException(
                            "A category cannot be its own parent"
                    );
                }

                Category parent = categoryRepository.findById(parentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Parent category not found: " + parentId
                                ));

                if (!"MAIN".equals(parent.getCategoryType())) {
                    throw new IllegalArgumentException(
                            "Only MAIN categories can be parents"
                    );
                }

                selectedParents.add(parent);
            }
        }

        existingCategory.setParentCategories(selectedParents);

        // Replace image only when a new file is selected
        if (imageFile != null && !imageFile.isEmpty()) {

            String fileName = Paths
                    .get(imageFile.getOriginalFilename())
                    .getFileName()
                    .toString();

            Path uploadPath =
                    Paths.get("src/main/resources/static/img");

            Files.createDirectories(uploadPath);

            Path filePath = uploadPath.resolve(fileName);

            Files.copy(
                    imageFile.getInputStream(),
                    filePath,
                    StandardCopyOption.REPLACE_EXISTING
            );

            existingCategory.setImage(fileName);
        }

        categoryRepository.save(existingCategory);

        return "redirect:/admin?categoryUpdated=true";
    }


    @PostMapping("/categories/reorder")
    @ResponseBody
    @Transactional
    public String reorderCategories(
            @RequestParam("categoryIds") List<Long> categoryIds) {

        for (int i = 0; i < categoryIds.size(); i++) {

            Long categoryId = categoryIds.get(i);

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Category not found: " + categoryId
                            ));

            category.setDisplayOrder(i + 1);
            categoryRepository.save(category);
        }

        return "success";
    }


}
