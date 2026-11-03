package com.main.recipeblog.repository;

import com.main.recipeblog.entity.Recipe;
import com.main.recipeblog.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRecipe(Recipe recipe);
    @Modifying
    @Query("DELETE FROM Review r WHERE r.recipe.id = :recipeId")
    int deleteReviewsByRecipeId(@Param("recipeId") Long recipeId);
}
