package com.dawns.tingstable.util;

import com.dawns.tingstable.model.Recipe;

import java.util.Arrays;
import java.util.List;

public final class RecipeCategories {
    public static final String ALL = "全部";
    public static final String STAPLE = "主食";
    public static final String SOUP = "汤羹";
    public static final String STIR_FRY = "炒菜";
    public static final String STEAM = "蒸菜";
    public static final String STEW = "炖煮";
    public static final String COLD = "凉拌";
    public static final String AIR_FRYER = "空气炸锅";
    public static final String GRILL = "煎烤";
    public static final String BAKING = "烘焙";
    public static final String OTHER = "其他";

    private RecipeCategories() {}

    public static List<String> all() {
        return Arrays.asList(ALL, STAPLE, SOUP, STIR_FRY, STEAM, STEW, COLD, AIR_FRYER, GRILL, BAKING, OTHER);
    }

    public static List<String> editable() {
        return Arrays.asList(STAPLE, SOUP, STIR_FRY, STEAM, STEW, COLD, AIR_FRYER, GRILL, BAKING, OTHER);
    }

    public static String categoryFor(Recipe recipe) {
        if (recipe == null) return OTHER;
        String category = safe(recipe.category);
        for (String known : editable()) if (known.equals(category)) return known;
        String text = safe(recipe.name) + " " + category + " " + String.join(" ", recipe.steps);
        if (containsAny(text, "空气炸锅", "空气炸")) return AIR_FRYER;
        if (containsAny(text, "凉拌", "凉菜", "冷盘")) return COLD;
        if (containsAny(text, "清蒸", "蒸制", "蒸锅", "上锅蒸")) return STEAM;
        if (containsAny(text, "炖", "焖", "红烧", "煨", "卤", "可乐鸡翅")) return STEW;
        if (containsAny(text, "烤箱", "烘焙", "蛋糕", "面包")) return BAKING;
        if (containsAny(text, "煎", "烧烤", "烤制")) return GRILL;
        if (containsAny(text, "米饭", "炒饭", "面条", "面食", "小米粥", "粥", "主食", "早餐", "馒头", "饺子")) return STAPLE;
        if (containsAny(text, "汤", "羹")) return SOUP;
        if (containsAny(text, "炒", "爆", "煸", "下饭菜", "快手菜", "素菜", "家常菜")) return STIR_FRY;
        return OTHER;
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) if (value.contains(needle)) return true;
        return false;
    }
}