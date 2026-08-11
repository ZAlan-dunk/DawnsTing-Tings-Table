package com.dawns.tingstable.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

public class RecipeCollectionTest {
    @Test
    public void roundTripKeepsNumberTypeAndUniqueRecipes() throws Exception {
        Recipe recipe = recipe("custom-1", "同名菜");
        RecipeCollection collection = new RecipeCollection(
                "Dew-0001", "我的菜谱集", RecipeCollection.TYPE_STANDARD,
                3L, 10L, 20L, true, Arrays.asList(recipe, recipe)
        );

        RecipeCollection parsed = RecipeCollection.fromJson(collection.toJson());

        assertEquals("Dew-0001", parsed.id);
        assertEquals(RecipeCollection.TYPE_STANDARD, parsed.type);
        assertEquals(1, parsed.recipes.size());
        assertEquals("同名菜", parsed.recipes.get(0).name);
    }

    @Test
    public void acceptsServerAssignedSpecialNumber() {
        RecipeCollection collection = new RecipeCollection(
                "SPECIAL01", "定制菜谱集", RecipeCollection.TYPE_SPECIAL,
                1L, 1L, 1L, true, Collections.emptyList()
        );

        assertEquals("SPECIAL01", collection.id);
    }

    @Test
    public void rejectsClientInventedNormalNumber() {
        assertThrows(IllegalArgumentException.class, () -> new RecipeCollection(
                "Dew-next", "错误编号", RecipeCollection.TYPE_STANDARD,
                0L, 0L, 0L, false, Collections.emptyList()
        ));
    }

    private Recipe recipe(String id, String name) {
        return new Recipe(id, name, "炒菜", "川菜", "家常", "简单", 10, 1,
                Collections.emptyList(), Collections.singletonList("完成"), "", true);
    }
}
