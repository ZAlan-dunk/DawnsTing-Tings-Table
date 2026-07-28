package com.dawns.tingstable.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.dawns.tingstable.model.Ingredient;
import com.dawns.tingstable.model.Recipe;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

public class RecipeMatcherTest {
    @Test
    public void excludesRecipesWithoutAnyMatchedMainIngredient() {
        Recipe recipe = recipe("土豆炖牛肉", ingredient("土豆"), ingredient("牛肉"));
        List<RecipeMatcher.Match> matches = RecipeMatcher.match(
                Collections.singletonList(recipe),
                new LinkedHashSet<>(Collections.singletonList("鸡蛋"))
        );
        assertTrue(matches.isEmpty());
    }

    @Test
    public void acceptsAliasNamesAsTheSameIngredient() {
        Recipe recipe = recipe("西红柿炒鸡蛋", ingredient("西红柿"), ingredient("鸡蛋"));
        List<RecipeMatcher.Match> matches = RecipeMatcher.match(
                Collections.singletonList(recipe),
                new LinkedHashSet<>(Arrays.asList("番茄", "鸡蛋"))
        );
        assertEquals(1, matches.size());
        assertTrue(matches.get(0).canCook());
        assertEquals(100, matches.get(0).percent);
    }

    @Test
    public void almostReadyRequiresAtLeastHalfOfMainIngredients() {
        Recipe recipe = recipe("四样小炒", ingredient("鸡蛋"), ingredient("木耳"), ingredient("黄瓜"), ingredient("胡萝卜"));
        List<RecipeMatcher.Match> oneOfFour = RecipeMatcher.match(
                Collections.singletonList(recipe),
                new LinkedHashSet<>(Collections.singletonList("鸡蛋"))
        );
        assertTrue(oneOfFour.isEmpty());

        List<RecipeMatcher.Match> twoOfFour = RecipeMatcher.match(
                Collections.singletonList(recipe),
                new LinkedHashSet<>(Arrays.asList("鸡蛋", "木耳"))
        );
        assertEquals(1, twoOfFour.size());
        assertFalse(twoOfFour.get(0).canCook());
        assertEquals(50, twoOfFour.get(0).percent);
    }

    @Test
    public void excludesRecipesWithoutMainIngredients() {
        Recipe recipe = recipe("只有调料", staple("盐"), staple("食用油"));
        List<RecipeMatcher.Match> matches = RecipeMatcher.match(
                Collections.singletonList(recipe),
                new LinkedHashSet<>(Collections.singletonList("盐"))
        );
        assertTrue(matches.isEmpty());
    }

    @Test
    public void staplesDoNotReduceMatchPercent() {
        Recipe recipe = recipe("清炒土豆丝", ingredient("土豆"), staple("盐"), staple("食用油"));
        List<RecipeMatcher.Match> matches = RecipeMatcher.match(
                Collections.singletonList(recipe),
                new LinkedHashSet<>(Collections.singletonList("马铃薯"))
        );
        assertEquals(1, matches.size());
        assertEquals(1, matches.get(0).requiredCount);
        assertTrue(matches.get(0).canCook());
    }

    private Recipe recipe(String name, Ingredient... ingredients) {
        return new Recipe(name, name, "家常菜", "家常", "简单", 20, 2,
                Arrays.asList(ingredients), Collections.singletonList("完成"), "", false);
    }

    private Ingredient ingredient(String name) { return new Ingredient(name, "适量", false); }
    private Ingredient staple(String name) { return new Ingredient(name, "适量", true); }
}