package com.dawns.tingstable.util;

import com.dawns.tingstable.model.Ingredient;
import com.dawns.tingstable.model.Recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class RecipeMatcher {
    private RecipeMatcher() {}

    public static class Match {
        public final Recipe recipe;
        public final List<String> missing;
        public final int requiredCount;
        public final int matchedCount;
        public final int percent;

        public Match(Recipe recipe, List<String> missing, int requiredCount, int matchedCount) {
            this.recipe = recipe;
            this.missing = missing;
            this.requiredCount = requiredCount;
            this.matchedCount = matchedCount;
            this.percent = requiredCount == 0 ? 100 : Math.round(matchedCount * 100f / requiredCount);
        }

        public boolean canCook() { return missing.isEmpty(); }
        public boolean almostReady() { return !missing.isEmpty() && missing.size() <= 3; }
    }

    public static List<Match> match(List<Recipe> recipes, Set<String> selectedIngredients) {
        Set<String> normalized = new HashSet<>();
        for (String item : selectedIngredients) normalized.add(item.trim().toLowerCase(Locale.ROOT));
        List<Match> result = new ArrayList<>();
        for (Recipe recipe : recipes) {
            int required = 0;
            int matched = 0;
            List<String> missing = new ArrayList<>();
            for (Ingredient ingredient : recipe.ingredients) {
                if (ingredient.staple) continue;
                required++;
                if (normalized.contains(ingredient.name.trim().toLowerCase(Locale.ROOT))) matched++;
                else missing.add(ingredient.name);
            }
            Match match = new Match(recipe, missing, required, matched);
            if (match.canCook() || match.almostReady()) result.add(match);
        }
        Collections.sort(result, new Comparator<Match>() {
            @Override public int compare(Match left, Match right) {
                if (left.canCook() != right.canCook()) return left.canCook() ? -1 : 1;
                int percentCompare = Integer.compare(right.percent, left.percent);
                if (percentCompare != 0) return percentCompare;
                return left.recipe.name.compareTo(right.recipe.name);
            }
        });
        return result;
    }
}
