package com.dawns.tingstable.util;

import static org.junit.Assert.assertEquals;

import com.dawns.tingstable.model.Recipe;
import com.dawns.tingstable.model.RecipeUsage;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecipeHistorySorterTest {
    @Test
    public void sortsByOpenCountThenMostRecentOpen() {
        Recipe first = recipe("first");
        Recipe second = recipe("second");
        Recipe third = recipe("third");
        Map<String, RecipeUsage> usage = new LinkedHashMap<>();
        usage.put(first.id, new RecipeUsage(2, 100L));
        usage.put(second.id, new RecipeUsage(4, 80L));
        usage.put(third.id, new RecipeUsage(4, 120L));

        List<Recipe> sorted = RecipeHistorySorter.sort(Arrays.asList(first, second, third), usage);

        assertEquals(Arrays.asList("third", "second", "first"),
                Arrays.asList(sorted.get(0).id, sorted.get(1).id, sorted.get(2).id));
    }

    @Test
    public void keepsOriginalOrderWhenThereIsNoHistory() {
        List<Recipe> sorted = RecipeHistorySorter.sort(
                Arrays.asList(recipe("b"), recipe("a")), Collections.emptyMap());

        assertEquals("b", sorted.get(0).id);
        assertEquals("a", sorted.get(1).id);
    }

    private Recipe recipe(String id) {
        return new Recipe(id, id, "炒菜", "家常", "简单", 10, 1,
                Collections.emptyList(), Collections.singletonList("完成"), "", false);
    }
}
