package com.dawns.tingstable.util;

import com.dawns.tingstable.model.Recipe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RecipeCuisineGroups {
    private RecipeCuisineGroups() {}

    public static Map<String, List<Recipe>> group(List<Recipe> recipes) {
        Map<String, List<Recipe>> staged = new LinkedHashMap<>();
        for (String cuisine : RecipeCuisines.editable()) staged.put(cuisine, new ArrayList<>());
        if (recipes != null) {
            for (Recipe recipe : recipes) {
                if (recipe == null) continue;
                staged.computeIfAbsent(RecipeCuisines.normalize(recipe.cuisine), ignored -> new ArrayList<>())
                        .add(recipe);
            }
        }
        Map<String, List<Recipe>> result = new LinkedHashMap<>();
        for (Map.Entry<String, List<Recipe>> entry : staged.entrySet()) {
            if (!entry.getValue().isEmpty()) result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
}
