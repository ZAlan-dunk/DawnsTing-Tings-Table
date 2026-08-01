package com.dawns.tingstable.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.dawns.tingstable.model.Ingredient;
import com.dawns.tingstable.model.Recipe;
import com.dawns.tingstable.model.RecipeBrowseState;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecipeFiltersTest {
    private List<Recipe> recipes;

    @Before
    public void setUp() {
        recipes = Arrays.asList(
                recipe("tomato", "西红柿炒鸡蛋", RecipeCategories.STIR_FRY,
                        RecipeCuisines.HOME_FUSION, false, "西红柿", "鸡蛋"),
                recipe("mapo", "麻婆豆腐", RecipeCategories.STIR_FRY,
                        RecipeCuisines.SICHUAN, false, "豆腐", "猪肉"),
                recipe("beef", "水煮牛肉", RecipeCategories.STEW,
                        RecipeCuisines.SICHUAN, false, "牛肉", "豆芽"),
                recipe("fish", "清蒸鲈鱼", RecipeCategories.STEAM,
                        RecipeCuisines.CANTONESE, true, "鲈鱼", "姜")
        );
    }

    @Test
    public void combinesScopeCuisineMethodAndQuery() {
        RecipeBrowseState state = new RecipeBrowseState();
        state.setScope(RecipeBrowseState.SCOPE_FAVORITES);
        state.setCuisine(RecipeCuisines.SICHUAN);
        state.setCookingMethod(RecipeCategories.STIR_FRY);
        state.setQuery("豆腐");

        List<Recipe> result = RecipeFilters.filter(
                recipes,
                Collections.singleton("mapo"),
                state
        );

        assertEquals(Collections.singletonList("麻婆豆腐"), names(result));
    }

    @Test
    public void queryMatchesCuisineMethodAndIngredientAlias() {
        assertEquals(Collections.singletonList("清蒸鲈鱼"), names(filterWithQuery("粤菜")));
        assertEquals(Arrays.asList("西红柿炒鸡蛋", "麻婆豆腐"), names(filterWithQuery("炒菜")));
        assertEquals(Collections.singletonList("西红柿炒鸡蛋"), names(filterWithQuery("番茄")));
    }

    @Test
    public void customScopeAndFacetsRemainComposable() {
        RecipeBrowseState state = new RecipeBrowseState();
        state.setScope(RecipeBrowseState.SCOPE_CUSTOM);
        state.setCuisine(RecipeCuisines.CANTONESE);
        state.setCookingMethod(RecipeCategories.STEAM);

        assertEquals(Collections.singletonList("清蒸鲈鱼"), names(
                RecipeFilters.filter(recipes, Collections.emptySet(), state)
        ));
    }

    @Test
    public void emptyOrNullInputsReturnStableResults() {
        RecipeBrowseState state = new RecipeBrowseState();

        assertEquals(names(recipes), names(RecipeFilters.filter(recipes, null, state)));
        assertTrue(RecipeFilters.filter(null, null, state).isEmpty());
    }

    private List<Recipe> filterWithQuery(String query) {
        RecipeBrowseState state = new RecipeBrowseState();
        state.setQuery(query);
        return RecipeFilters.filter(recipes, Collections.emptySet(), state);
    }

    private Recipe recipe(String id, String name, String category, String cuisine,
                          boolean custom, String... ingredientNames) {
        List<Ingredient> ingredients = new ArrayList<>();
        for (String ingredientName : ingredientNames) {
            ingredients.add(new Ingredient(ingredientName, "适量", false));
        }
        return new Recipe(id, name, category, cuisine, "家常", "简单", 20, 2,
                ingredients, Collections.singletonList("完成"), "", custom);
    }

    private List<String> names(List<Recipe> values) {
        List<String> names = new ArrayList<>();
        for (Recipe value : values) names.add(value.name);
        return names;
    }
}
