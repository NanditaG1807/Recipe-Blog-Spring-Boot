package com.main.recipeblog.entity;

import com.main.recipeblog.entity.Recipe;
import jakarta.persistence.*;

@Entity
@Table(
        name = "wishlist",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_wishlist_recipe",
                columnNames = "recipe_id"
        )
)
public class wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    public wishlist() {
    }

    public wishlist(Recipe recipe) {
        this.recipe = recipe;
    }

    public Long getId() {
        return id;
    }

    public Recipe getRecipe() {
        return recipe;
    }

    public void setRecipe(Recipe recipe) {
        this.recipe = recipe;
    }
}