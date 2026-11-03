package com.main.recipeblog.controller;


import com.main.recipeblog.entity.Recipe;
import com.main.recipeblog.service.WishlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping("/wishlist/add/{id}")
    @ResponseBody
    public ResponseEntity<String> addToWishlist(@PathVariable Long id) {
        wishlistService.addToWishlist(id);
        return ResponseEntity.ok("Added to your Wishlist!");
    }

    @GetMapping("/wishlist")
    public String showWishlist(Model model) {

        List<Recipe> recipes = wishlistService.getWishlistRecipes();

        if (recipes == null) {
            recipes = java.util.Collections.emptyList();
        }

        model.addAttribute("recipes", recipes);

        return "wishlist";
    }

    @PostMapping("/wishlist/remove/{id}")
    public String removeFromWishlist(@PathVariable Long id) {

        wishlistService.removeFromWishlist(id);

        return "redirect:/wishlist";
    }
}