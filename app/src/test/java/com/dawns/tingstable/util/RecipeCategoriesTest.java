package com.dawns.tingstable.util;

import static org.junit.Assert.assertEquals;

import com.dawns.tingstable.model.Recipe;

import org.junit.Test;

import java.util.Collections;

public class RecipeCategoriesTest {
    @Test public void mapsStirFry() { assertEquals("炒菜", category("西红柿炒鸡蛋", "家常菜", "翻炒至熟")); }
    @Test public void mapsSoup() { assertEquals("汤羹", category("紫菜蛋花汤", "快手菜", "煮开")); }
    @Test public void mapsSteam() { assertEquals("蒸菜", category("清蒸鲈鱼", "家常菜", "上锅蒸熟")); }
    @Test public void mapsStew() { assertEquals("炖煮", category("土豆炖牛肉", "下饭菜", "小火炖煮")); }
    @Test public void mapsRice() { assertEquals("主食", category("家常蛋炒饭", "主食", "炒匀")); }
    @Test public void mapsPorridge() { assertEquals("主食", category("南瓜小米粥", "早餐", "熬煮")); }
    @Test public void respectsExplicitAirFryer() { assertEquals("空气炸锅", category("薯角", "空气炸锅", "烤熟")); }

    private String category(String name, String original, String step) {
        Recipe recipe = new Recipe("id", name, original, "家常", "简单", 20, 2,
                Collections.emptyList(), Collections.singletonList(step), "", false);
        return RecipeCategories.categoryFor(recipe);
    }
}