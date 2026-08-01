package com.dawns.tingstable.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.dawns.tingstable.util.RecipeCategories;
import com.dawns.tingstable.util.RecipeCuisines;

import org.junit.Test;

public class RecipeBrowseStateTest {
    @Test
    public void changingVisibleDimensionKeepsBothSelections() {
        RecipeBrowseState state = new RecipeBrowseState();
        state.setCuisine(RecipeCuisines.SICHUAN);
        state.setCookingMethod(RecipeCategories.STIR_FRY);
        state.setDimension(RecipeBrowseState.DIMENSION_METHOD);

        assertEquals(RecipeCuisines.SICHUAN, state.getCuisine());
        assertEquals(RecipeCategories.STIR_FRY, state.getCookingMethod());
        assertEquals(RecipeBrowseState.DIMENSION_METHOD, state.getDimension());
        assertEquals("川菜 · 炒菜", state.summary());
    }

    @Test
    public void resetKeepsScopeAndDimensionButClearsQueryAndFacets() {
        RecipeBrowseState state = new RecipeBrowseState();
        state.setScope(RecipeBrowseState.SCOPE_FAVORITES);
        state.setDimension(RecipeBrowseState.DIMENSION_METHOD);
        state.setCuisine(RecipeCuisines.CANTONESE);
        state.setCookingMethod(RecipeCategories.STEAM);
        state.setQuery("鲈鱼");

        state.resetFilters();

        assertEquals(RecipeBrowseState.SCOPE_FAVORITES, state.getScope());
        assertEquals(RecipeBrowseState.DIMENSION_METHOD, state.getDimension());
        assertEquals(RecipeCuisines.ALL, state.getCuisine());
        assertEquals(RecipeCategories.ALL, state.getCookingMethod());
        assertEquals("", state.getQuery());
        assertEquals("全部菜系 · 全部做法", state.summary());
        assertFalse(state.hasFilters());
    }

    @Test
    public void activeQueryCountsAsAFilter() {
        RecipeBrowseState state = new RecipeBrowseState();
        state.setQuery("  豆腐  ");

        assertEquals("豆腐", state.getQuery());
        assertTrue(state.hasFilters());
    }
}
