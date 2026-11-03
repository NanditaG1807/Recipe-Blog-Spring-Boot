
package com.main.recipeblog.repository;

import com.main.recipeblog.entity.Category;
import com.main.recipeblog.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RecipeRepository
        extends JpaRepository<Recipe, Long> {

    List<Recipe> findByCardType(String cardType);

    List<Recipe> findByCategories(Category category);

    List<Recipe> findByCategoriesContaining(Category category);

    List<Recipe> findByNameContainingIgnoreCase(String name);

    List<Recipe> findByTitleContainingIgnoreCase(String title);

    List<Recipe> findAllByOrderByIdAsc();

    List<Recipe> findDistinctByCategories_ParentCategories_Id(
            Long parentId
    );

    // Cooking Time + Veg/Non-Veg + Search Filter
    @Query("""
        SELECT DISTINCT r FROM Recipe r
        WHERE (:time IS NULL OR r.cookTime <= :time)
          AND (:type IS NULL OR LOWER(r.cardType) = LOWER(:type))
          AND (
              :search IS NULL
              OR LOWER(COALESCE(r.title, ''))
                  LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(COALESCE(r.name, ''))
                  LIKE LOWER(CONCAT('%', :search, '%'))
              OR LOWER(COALESCE(r.description, ''))
                  LIKE LOWER(CONCAT('%', :search, '%'))
          )
        ORDER BY r.id DESC
    """)
    List<Recipe> filterRecipes(
            @Param("time") Integer time,
            @Param("type") String type,
            @Param("search") String search
    );
}
