package com.dawns.tingstable.data;

import com.dawns.tingstable.model.PantryItem;
import com.dawns.tingstable.model.Recipe;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** Versioned, portable representation of user-created app data. */
public final class BackupPayload {
    public static final int SCHEMA_VERSION = 1;
    private static final int MAX_ITEMS = 2000;

    public final String profileId;
    public final String appVersion;
    public final long createdAt;
    public final List<Recipe> customRecipes;
    public final Set<String> favorites;
    public final List<PantryItem> pantryItems;
    public final List<String> shoppingList;
    public final Set<String> selectedIngredients;
    public final String themeMode;

    public BackupPayload(String profileId, String appVersion, long createdAt,
                         List<Recipe> customRecipes, Set<String> favorites,
                         List<PantryItem> pantryItems, List<String> shoppingList,
                         Set<String> selectedIngredients, String themeMode) {
        this.profileId = requireText(profileId, "备份身份为空");
        this.appVersion = appVersion == null ? "" : appVersion.trim();
        this.createdAt = Math.max(0L, createdAt);
        this.customRecipes = new ArrayList<>(customRecipes == null ? new ArrayList<>() : customRecipes);
        this.favorites = new LinkedHashSet<>(favorites == null ? new LinkedHashSet<>() : favorites);
        this.pantryItems = new ArrayList<>(pantryItems == null ? new ArrayList<>() : pantryItems);
        this.shoppingList = new ArrayList<>(shoppingList == null ? new ArrayList<>() : shoppingList);
        this.selectedIngredients = new LinkedHashSet<>(selectedIngredients == null ? new LinkedHashSet<>() : selectedIngredients);
        this.themeMode = themeMode == null || themeMode.trim().isEmpty() ? "light" : themeMode.trim();
        validateSize(this.customRecipes.size(), "自定义菜谱");
        validateSize(this.favorites.size(), "收藏");
        validateSize(this.pantryItems.size(), "菜篮");
        validateSize(this.shoppingList.size(), "采购清单");
        validateSize(this.selectedIngredients.size(), "已选食材");
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("format", "dawnsdew-recipe-backup");
        json.put("schemaVersion", SCHEMA_VERSION);
        json.put("profileId", profileId);
        json.put("appVersion", appVersion);
        json.put("createdAt", createdAt);
        json.put("customRecipes", recipeArray(customRecipes));
        json.put("favorites", stringArray(favorites));
        json.put("pantryItems", pantryArray(pantryItems));
        json.put("shoppingList", stringArray(shoppingList));
        json.put("selectedIngredients", stringArray(selectedIngredients));
        json.put("themeMode", themeMode);
        return json;
    }

    public static BackupPayload parse(String raw, String expectedProfileId) throws JSONException {
        JSONObject json = new JSONObject(raw);
        if (!"dawnsdew-recipe-backup".equals(json.optString("format"))) {
            throw new JSONException("无法识别的备份文件");
        }
        if (json.optInt("schemaVersion", -1) != SCHEMA_VERSION) {
            throw new JSONException("暂不支持这个备份版本");
        }
        String profileId = json.optString("profileId").trim();
        if (profileId.isEmpty()) throw new JSONException("备份身份为空");
        if (expectedProfileId != null && !expectedProfileId.equals(profileId)) {
            throw new JSONException("备份不属于当前使用者");
        }
        return new BackupPayload(
                profileId,
                json.optString("appVersion"),
                json.optLong("createdAt", 0L),
                parseRecipes(json.optJSONArray("customRecipes")),
                parseStrings(json.optJSONArray("favorites")),
                parsePantry(json.optJSONArray("pantryItems")),
                new ArrayList<>(parseStrings(json.optJSONArray("shoppingList"))),
                parseStrings(json.optJSONArray("selectedIngredients")),
                json.optString("themeMode", "light")
        );
    }

    public String summary() {
        return "自定义菜谱 " + customRecipes.size() + " 道 · 收藏 " + favorites.size()
                + " 项\n菜篮 " + pantryItems.size() + " 项 · 清单 " + shoppingList.size() + " 项";
    }

    private static JSONArray recipeArray(List<Recipe> recipes) throws JSONException {
        JSONArray array = new JSONArray();
        for (Recipe recipe : recipes) array.put(recipe.toJson());
        return array;
    }

    private static JSONArray pantryArray(List<PantryItem> items) throws JSONException {
        JSONArray array = new JSONArray();
        for (PantryItem item : items) array.put(item.toJson());
        return array;
    }

    private static JSONArray stringArray(Iterable<String> values) {
        JSONArray array = new JSONArray();
        for (String value : values) {
            String item = value == null ? "" : value.trim();
            if (!item.isEmpty()) array.put(item);
        }
        return array;
    }

    private static List<Recipe> parseRecipes(JSONArray array) throws JSONException {
        List<Recipe> result = new ArrayList<>();
        if (array == null) return result;
        validateSize(array.length(), "自定义菜谱");
        Set<String> ids = new LinkedHashSet<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) continue;
            Recipe recipe = Recipe.fromJson(item);
            if (recipe.id == null || recipe.id.trim().isEmpty() || recipe.name.trim().isEmpty()) continue;
            recipe.custom = true;
            if (ids.add(recipe.id)) result.add(recipe);
        }
        return result;
    }

    private static List<PantryItem> parsePantry(JSONArray array) throws JSONException {
        List<PantryItem> result = new ArrayList<>();
        if (array == null) return result;
        validateSize(array.length(), "菜篮");
        Set<String> ids = new LinkedHashSet<>();
        for (int i = 0; i < array.length(); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) continue;
            PantryItem pantryItem = PantryItem.fromJson(item);
            if (pantryItem.id.isEmpty() || pantryItem.name.isEmpty()) continue;
            if (ids.add(pantryItem.id)) result.add(pantryItem);
        }
        return result;
    }

    private static Set<String> parseStrings(JSONArray array) throws JSONException {
        Set<String> result = new LinkedHashSet<>();
        if (array == null) return result;
        validateSize(array.length(), "数据项");
        for (int i = 0; i < array.length(); i++) {
            String value = array.optString(i, "").trim();
            if (!value.isEmpty()) result.add(value);
        }
        return result;
    }

    private static String requireText(String value, String message) {
        String result = value == null ? "" : value.trim();
        if (result.isEmpty()) throw new IllegalArgumentException(message);
        return result;
    }

    private static void validateSize(int size, String label) {
        if (size > MAX_ITEMS) throw new IllegalArgumentException(label + "数量异常");
    }
}
