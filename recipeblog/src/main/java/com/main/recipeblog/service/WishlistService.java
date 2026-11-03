package com.main.recipeblog.service;

import com.main.recipeblog.entity.Recipe;
import com.main.recipeblog.entity.wishlist;
import com.main.recipeblog.repository.RecipeRepository;
import com.main.recipeblog.repository.WishlistRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final RecipeRepository recipeRepository;

    public WishlistService(
            WishlistRepository wishlistRepository,
            RecipeRepository recipeRepository) {

        this.wishlistRepository = wishlistRepository;
        this.recipeRepository = recipeRepository;
    }

    // Add Recipe to Wishlist
    @Transactional
    public void addToWishlist(Long recipeId) {

        if (wishlistRepository.existsByRecipe_Id(recipeId)) {
            return;
        }

        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Recipe not found: " + recipeId));

        wishlist wishlist = new wishlist(recipe);

        wishlistRepository.save(wishlist);
    }

    // Get All Wishlist Recipes
    @Transactional(readOnly = true)
    public List<Recipe> getWishlistRecipes() {
        return wishlistRepository.findAllByOrderByIdDesc()
                .stream()
                .map(w -> w.getRecipe())
                .filter(recipe -> recipe != null)
                .filter(recipe -> recipe.getId() != null)
                .toList();
    }

    // Check Whether Recipe Is Saved
    @Transactional(readOnly = true)
    public boolean isRecipeSaved(Long recipeId) {

        return wishlistRepository.existsByRecipe_Id(recipeId);
    }

    // Remove Recipe from Wishlist
    @Transactional
    public void removeFromWishlist(Long recipeId) {

        wishlist wishlistItem = wishlistRepository
                .findByRecipe_Id(recipeId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Wishlist recipe not found: " + recipeId));

        wishlistRepository.delete(wishlistItem);
    }
}