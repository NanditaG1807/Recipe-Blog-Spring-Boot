package com.main.recipeblog.entity;

import jakarta.persistence.*;


import java.util.HashSet;

import java.util.Set;



@Entity
@Table(name = "recipes")
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String name;

    @Column(length = 1000)
    private String description;

    private String image;

    private int servings;
    private int cookTime;
    private int likes = 0;

    // Main/Sub Category
    @ManyToMany
    @JoinTable(
            name = "recipe_categories",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    private String cardType;

    @Column(length = 5000)
    private String ingredients;

    @Column(length = 5000)
    private String cookingSteps;

    @Column(length = 20)
    private String foodType;

    public Recipe() {
    }

    public Recipe(Long id, String title, String name, String description, String image, int servings, int cookTime, int likes, Set<Category> categories, String cardType, String ingredients, String cookingSteps) {
        this.id = id;
        this.title = title;
        this.name = name;
        this.description = description;
        this.image = image;
        this.servings = servings;
        this.cookTime = cookTime;
        this.likes = likes;
        this.categories = categories;
        this.cardType = cardType;
        this.ingredients = ingredients;
        this.cookingSteps = cookingSteps;
    }

    // Getters and Setters

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public int getServings() {
        return servings;
    }

    public void setServings(int servings) {
        this.servings = servings;
    }

    public int getCookTime() {
        return cookTime;
    }

    public void setCookTime(int cookTime) {
        this.cookTime = cookTime;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public Set<Category> getCategories() {
        return categories;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public String getIngredients() {
        return ingredients;
    }

    public void setIngredients(String ingredients) {
        this.ingredients = ingredients;
    }


    public String getCookingSteps() {
        return cookingSteps;
    }

    public void setCookingSteps(String cookingSteps) {
        this.cookingSteps = cookingSteps;
    }


    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

}
