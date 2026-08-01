package com.dawns.tingstable.util;

import com.dawns.tingstable.model.Ingredient;
import com.dawns.tingstable.model.Recipe;
import com.dawns.tingstable.model.RecipeBrowseState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class RecipeFilters {
    private RecipeFilters() {}

    public static List<Recipe> filter(List<Recipe> recipes, Set<String> favorites,
                                      RecipeBrowseState state) {
        if (recipes == null || recipes.isEmpty()) return Collections.emptyList();
        Set<String> favoriteIds = favorites == null ? Collections.emptySet() : favorites;
        RecipeBrowseState criteria = state == null ? new RecipeBrowseState() : state;
        List<Recipe> result = new ArrayList<>();

        for (Recipe recipe : recipes) {
            if (recipe == null) continue;
            if (RecipeBrowseState.SCOPE_FAVORITES.equals(criteria.getScope())
                    && !favoriteIds.contains(recipe.id)) continue;
            if (RecipeBrowseState.SCOPE_CUSTOM.equals(criteria.getScope()) && !recipe.custom) continue;
            if (!RecipeCuisines.ALL.equals(criteria.getCuisine())
                    && !criteria.getCuisine().equals(RecipeCuisines.normalize(recipe.cuisine))) continue;
            if (!RecipeCategories.ALL.equals(criteria.getCookingMethod())
                    && !criteria.getCookingMethod().equals(RecipeCategories.categoryFor(recipe))) continue;
            if (!matchesQuery(recipe, criteria.getQuery())) continue;
            result.add(recipe);
        }
        return result;
    }

    private static boolean matchesQuery(Recipe recipe, String query) {
        String normalized = safe(query).trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) return true;

        String cuisine = RecipeCuisines.normalize(recipe.cuisine);
        String cookingMethod = RecipeCategories.categoryFor(recipe);
        if (safe(recipe.name).toLowerCase(Locale.ROOT).contains(normalized)
                || safe(recipe.flavor).toLowerCase(Locale.ROOT).contains(normalized)
                || cuisine.toLowerCase(Locale.ROOT).contains(normalized)
                || cookingMethod.toLowerCase(Locale.ROOT).contains(normalized)) {
            return true;
        }
        if (recipe.ingredients == null) return false;
        for (Ingredient ingredient : recipe.ingredients) {
            if (ingredient != null && RecipeMatcher.queryMatchesIngredient(normalized, safe(ingredient.name))) {
                return true;
            }
        }
        return false;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }
}
