package com.main.recipeblog.repository;


import com.main.recipeblog.entity.wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;


public interface WishlistRepository
        extends JpaRepository<wishlist, Long> {
    boolean existsByRecipe_Id(Long recipeId);

    List<wishlist> findAllByOrderByIdDesc();

    Optional<wishlist> findByRecipe_Id(Long recipeId);

    // Delete wishlist entries linked to recipe
    void deleteAllByRecipe_Id(Long recipeId);
}