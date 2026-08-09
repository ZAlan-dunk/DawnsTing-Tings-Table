package com.dawns.tingstable.model;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpecialRecipeTest {
    @Test
    public void acceptsOnlyCanonicalMobileRecipeLinks() {
        assertTrue(SpecialRecipe.isAllowedRecipeUrl(
                "https://m.xiachufang.com/recipe/104039047/"));
        assertFalse(SpecialRecipe.isAllowedRecipeUrl(
                "http://m.xiachufang.com/recipe/104039047/"));
        assertFalse(SpecialRecipe.isAllowedRecipeUrl(
                "https://m.xiachufang.com.evil.example/recipe/104039047/"));
        assertFalse(SpecialRecipe.isAllowedRecipeUrl(
                "https://m.xiachufang.com/recipe/not-a-number/"));
        assertFalse(SpecialRecipe.isAllowedRecipeUrl(
                "https://m.xiachufang.com/recipe/104039047/?redirect=1"));
    }

    @Test
    public void validRecipeRequiresIdentityTitleAndSource() {
        assertTrue(new SpecialRecipe(
                "104039047",
                "椒盐虾（超酥香）",
                "https://m.xiachufang.com/recipe/104039047/",
                "https://i2.chuimg.com/cover.jpg"
        ).isValid());
        assertFalse(new SpecialRecipe(
                "",
                "椒盐虾（超酥香）",
                "https://m.xiachufang.com/recipe/104039047/",
                "https://i2.chuimg.com/cover.jpg"
        ).isValid());
    }
}
