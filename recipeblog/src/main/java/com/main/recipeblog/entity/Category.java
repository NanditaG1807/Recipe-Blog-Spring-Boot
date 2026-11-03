package com.main.recipeblog.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String image;

    private String description;


    // =========================
    // PARENT CATEGORY
    // =========================

    // Main Category -> parentCategory = null
    // Sub Category  -> parentCategory = Main Category



    @ManyToMany
    @JoinTable(
            name = "category_parent_mapping",
            joinColumns = @JoinColumn(name = "sub_category_id"),
            inverseJoinColumns = @JoinColumn(name = "parent_category_id")
    )
    private Set<Category> parentCategories = new HashSet<>();
    // =========================
    // RECIPES
    // =========================

    @ManyToMany(mappedBy = "categories")
    private Set<Recipe> recipes = new HashSet<>();

    @Column(name = "category_type", length = 10)
    private String categoryType;


    @Column(name = "display_order")
    private Integer displayOrder = 0;


    // =========================
    // CONSTRUCTOR
    // =========================

    public Category() {
    }


    public Category(String name, String image) {
        this.name = name;
        this.image = image;
    }


    // =========================
    // GETTERS / SETTERS
    // =========================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    // =========================
    // PARENT CATEGORY
    // =========================



    // =========================
    // RECIPES
    // =========================

    public Set<Recipe> getRecipes() {
        return recipes;
    }

    public void setRecipes(Set<Recipe> recipes) {
        this.recipes = recipes;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public Set<Category> getParentCategories() {
        return parentCategories;
    }

    public void setParentCategories(Set<Category> parentCategories) {
        this.parentCategories = parentCategories;
    }

    public void addParentCategory(Category parent) {
        this.parentCategories.add(parent);
    }

    public void removeParentCategory(Category parent) {
        this.parentCategories.remove(parent);
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}
