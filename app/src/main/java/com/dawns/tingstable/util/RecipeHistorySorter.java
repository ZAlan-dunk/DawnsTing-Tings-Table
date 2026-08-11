package com.dawns.tingstable.util;

import com.dawns.tingstable.model.Recipe;
import com.dawns.tingstable.model.RecipeUsage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class RecipeHistorySorter {
    private RecipeHistorySorter() {}

    public static List<Recipe> sort(List<Recipe> recipes, Map<String, RecipeUsage> usage) {
        List<Recipe> result = new ArrayList<>(recipes == null ? Collections.emptyList() : recipes);
        Map<String, RecipeUsage> history = usage == null ? Collections.emptyMap() : usage;
        result.sort((left, right) -> {
            RecipeUsage leftUsage = history.get(left.id);
            RecipeUsage rightUsage = history.get(right.id);
            int count = Integer.compare(count(rightUsage), count(leftUsage));
            if (count != 0) return count;
            return Long.compare(lastOpened(rightUsage), lastOpened(leftUsage));
        });
        return result;
    }

    private static int count(RecipeUsage usage) {
        return usage == null ? 0 : usage.openCount;
    }

    private static long lastOpened(RecipeUsage usage) {
        return usage == null ? 0L : usage.lastOpenedAt;
    }
}
