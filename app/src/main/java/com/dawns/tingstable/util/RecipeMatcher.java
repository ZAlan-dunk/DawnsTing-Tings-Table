package com.dawns.tingstable.util;

import com.dawns.tingstable.model.Ingredient;
import com.dawns.tingstable.model.Recipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Pure offline recipe matching for the personal pantry workflow. */
public final class RecipeMatcher {
    private static final int MAX_MISSING = 3;
    private static final int MIN_MATCH_PERCENT = 50;
    private static final Map<String, String> ALIASES = createAliases();

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

        public boolean canCook() {
            return missing.isEmpty();
        }

        public boolean almostReady() {
            return !canCook()
                    && matchedCount >= 1
                    && missing.size() <= MAX_MISSING
                    && percent >= MIN_MATCH_PERCENT;
        }
    }

    public static List<Match> match(List<Recipe> recipes, Set<String> selectedIngredients) {
        Set<String> normalized = new HashSet<>();
        for (String item : selectedIngredients) normalized.add(canonicalize(item));

        List<Match> result = new ArrayList<>();
        for (Recipe recipe : recipes) {
            int required = 0;
            int matched = 0;
            List<String> missing = new ArrayList<>();
            Set<String> counted = new HashSet<>();

            for (Ingredient ingredient : recipe.ingredients) {
                if (ingredient.staple) continue;
                String canonical = canonicalize(ingredient.name);
                if (!counted.add(canonical)) continue;
                required++;
                if (normalized.contains(canonical)) matched++;
                else missing.add(ingredient.name);
            }

            if (required == 0) continue;
            Match match = new Match(recipe, missing, required, matched);
            if (match.canCook() || match.almostReady()) result.add(match);
        }

        Collections.sort(result, new Comparator<Match>() {
            @Override
            public int compare(Match left, Match right) {
                if (left.canCook() != right.canCook()) return left.canCook() ? -1 : 1;
                int missingCompare = Integer.compare(left.missing.size(), right.missing.size());
                if (missingCompare != 0) return missingCompare;
                int percentCompare = Integer.compare(right.percent, left.percent);
                if (percentCompare != 0) return percentCompare;
                int timeCompare = Integer.compare(left.recipe.minutes, right.recipe.minutes);
                if (timeCompare != 0) return timeCompare;
                return left.recipe.name.compareTo(right.recipe.name);
            }
        });
        return result;
    }

    public static String canonicalize(String value) {
        String normalized = value == null ? "" : value
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace(" ", "")
                .replace("　", "");
        String canonical = ALIASES.get(normalized);
        return canonical == null ? normalized : canonical;
    }

    public static boolean queryMatchesIngredient(String query, String ingredientName) {
        String normalizedQuery = canonicalize(query);
        String normalizedIngredient = canonicalize(ingredientName);
        return normalizedIngredient.contains(normalizedQuery)
                || normalizedQuery.contains(normalizedIngredient)
                || ingredientName.toLowerCase(Locale.ROOT).contains(query.trim().toLowerCase(Locale.ROOT));
    }

    private static Map<String, String> createAliases() {
        Map<String, String> aliases = new HashMap<>();
        addAliasGroup(aliases, "西红柿", "番茄", "西红柿");
        addAliasGroup(aliases, "土豆", "马铃薯", "洋芋", "土豆");
        addAliasGroup(aliases, "淀粉", "玉米淀粉", "生粉", "淀粉");
        addAliasGroup(aliases, "姜", "生姜", "姜");
        addAliasGroup(aliases, "葱", "小葱", "香葱", "葱");
        addAliasGroup(aliases, "菜花", "花椰菜", "椰菜花", "菜花");
        addAliasGroup(aliases, "西兰花", "绿花椰菜", "西兰花");
        addAliasGroup(aliases, "青椒", "柿子椒", "甜椒", "青椒");
        addAliasGroup(aliases, "猪肉", "瘦肉", "猪瘦肉", "猪肉");
        addAliasGroup(aliases, "牛肉", "牛腩", "牛肉");
        addAliasGroup(aliases, "食用油", "植物油", "炒菜油", "食用油");
        return aliases;
    }

    private static void addAliasGroup(Map<String, String> aliases, String canonical, String... names) {
        aliases.put(canonical, canonical);
        for (String name : names) aliases.put(name, canonical);
    }
}
