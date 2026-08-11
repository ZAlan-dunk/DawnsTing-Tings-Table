package com.dawns.tingstable.model;

import com.dawns.tingstable.util.RecipeCategories;
import com.dawns.tingstable.util.RecipeCuisines;

public final class RecipeBrowseState {
    public static final String SCOPE_ALL = "ALL";
    public static final String SCOPE_FAVORITES = "FAVORITES";
    public static final String SCOPE_CUSTOM = "CUSTOM";
    public static final String DIMENSION_CUISINE = "CUISINE";
    public static final String DIMENSION_METHOD = "METHOD";
    public static final String VIEW_LIST = "LIST";
    public static final String VIEW_CUISINE = "CUISINE_GROUPS";
    public static final String SORT_DEFAULT = "DEFAULT";
    public static final String SORT_HABIT = "HABIT";

    private String query = "";
    private String scope = SCOPE_ALL;
    private String cuisine = RecipeCuisines.ALL;
    private String cookingMethod = RecipeCategories.ALL;
    private String dimension = DIMENSION_CUISINE;
    private String viewMode = VIEW_LIST;
    private String sortMode = SORT_DEFAULT;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query == null ? "" : query.trim();
    }

    public String getScope() {
        return scope;
    }

    public String scopeLabel() {
        if (SCOPE_FAVORITES.equals(scope)) return "我的收藏";
        if (SCOPE_CUSTOM.equals(scope)) return "自定义";
        return "全部菜谱";
    }

    public void setScope(String scope) {
        if (SCOPE_FAVORITES.equals(scope) || SCOPE_CUSTOM.equals(scope)) {
            this.scope = scope;
        } else {
            this.scope = SCOPE_ALL;
        }
    }

    public String getCuisine() {
        return cuisine;
    }

    public void setCuisine(String cuisine) {
        if (cuisine == null || cuisine.trim().isEmpty() || RecipeCuisines.ALL.equals(cuisine)) {
            this.cuisine = RecipeCuisines.ALL;
        } else {
            this.cuisine = RecipeCuisines.normalize(cuisine);
        }
    }

    public String getCookingMethod() {
        return cookingMethod;
    }

    public void setCookingMethod(String cookingMethod) {
        this.cookingMethod = RecipeCategories.all().contains(cookingMethod)
                ? cookingMethod
                : RecipeCategories.ALL;
    }

    public String getDimension() {
        return dimension;
    }

    public void setDimension(String dimension) {
        this.dimension = DIMENSION_METHOD.equals(dimension) ? DIMENSION_METHOD : DIMENSION_CUISINE;
    }

    public String getViewMode() {
        return viewMode;
    }

    public void setViewMode(String viewMode) {
        this.viewMode = VIEW_CUISINE.equals(viewMode) ? VIEW_CUISINE : VIEW_LIST;
    }

    public void toggleViewMode() {
        viewMode = VIEW_CUISINE.equals(viewMode) ? VIEW_LIST : VIEW_CUISINE;
    }

    public String getSortMode() {
        return sortMode;
    }

    public void setSortMode(String sortMode) {
        this.sortMode = SORT_HABIT.equals(sortMode) ? SORT_HABIT : SORT_DEFAULT;
    }

    public String sortLabel() {
        return SORT_HABIT.equals(sortMode) ? "习惯排序" : "默认顺序";
    }

    public String summary() {
        String cuisineLabel = RecipeCuisines.ALL.equals(cuisine) ? "全部菜系" : cuisine;
        String methodLabel = RecipeCategories.ALL.equals(cookingMethod) ? "全部做法" : cookingMethod;
        return cuisineLabel + " · " + methodLabel;
    }

    public String compactSummary() {
        StringBuilder summary = new StringBuilder(scopeLabel());
        if (hasActiveQuery()) summary.append(" · 搜索“").append(query).append("”");
        if (!RecipeCuisines.ALL.equals(cuisine)) summary.append(" · ").append(cuisine);
        if (!RecipeCategories.ALL.equals(cookingMethod)) summary.append(" · ").append(cookingMethod);
        if (SORT_HABIT.equals(sortMode)) summary.append(" · 习惯排序");
        if (VIEW_CUISINE.equals(viewMode)) summary.append(" · 菜系分类");
        return summary.toString();
    }

    public int activeFilterCount() {
        int count = SCOPE_ALL.equals(scope) ? 0 : 1;
        if (!RecipeCuisines.ALL.equals(cuisine)) count++;
        if (!RecipeCategories.ALL.equals(cookingMethod)) count++;
        return count;
    }

    public boolean hasActiveQuery() {
        return !query.isEmpty();
    }

    public boolean hasFilters() {
        return hasActiveQuery()
                || !RecipeCuisines.ALL.equals(cuisine)
                || !RecipeCategories.ALL.equals(cookingMethod);
    }

    public void resetFilters() {
        query = "";
        cuisine = RecipeCuisines.ALL;
        cookingMethod = RecipeCategories.ALL;
    }
}
