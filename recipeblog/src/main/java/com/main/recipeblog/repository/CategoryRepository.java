
package com.main.recipeblog.repository;

import com.main.recipeblog.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository
        extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);

    // Fetch MAIN or SUB categories
    List<Category> findByCategoryType(String categoryType);

    // Find subcategories linked to a parent category
    List<Category> findByParentCategoriesContaining(
            Category parentCategory
    );

    // Categories display order
    List<Category> findAllByOrderByDisplayOrderAsc();
}
