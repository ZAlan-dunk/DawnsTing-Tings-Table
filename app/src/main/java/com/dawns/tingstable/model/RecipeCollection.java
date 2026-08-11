package com.dawns.tingstable.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class RecipeCollection {
    public static final int SCHEMA_VERSION = 1;
    public static final String TYPE_STANDARD = "standard";
    public static final String TYPE_SPECIAL = "special";

    public final String id;
    public final String name;
    public final String type;
    public final long revision;
    public final long createdAt;
    public final long updatedAt;
    public final boolean editable;
    public final List<Recipe> recipes;

    public RecipeCollection(String id, String name, String type, long revision,
                            long createdAt, long updatedAt, boolean editable,
                            List<Recipe> recipes) {
        this.id = requireText(id, "菜谱集编号为空");
        this.name = requireText(name, "菜谱集名称为空");
        this.type = TYPE_SPECIAL.equals(type) ? TYPE_SPECIAL : TYPE_STANDARD;
        if (TYPE_STANDARD.equals(this.type) && !this.id.matches("Dew-\\d{4,}")) {
            throw new IllegalArgumentException("普通菜谱集编号格式错误");
        }
        if (TYPE_SPECIAL.equals(this.type) && !this.id.matches("[A-Z0-9]{4,16}")) {
            throw new IllegalArgumentException("特典菜谱集编号格式错误");
        }
        this.revision = Math.max(0L, revision);
        this.createdAt = Math.max(0L, createdAt);
        this.updatedAt = Math.max(this.createdAt, updatedAt);
        this.editable = editable;
        this.recipes = Collections.unmodifiableList(cleanRecipes(recipes));
    }

    public RecipeCollection withName(String value, long nextRevision, long now) {
        return new RecipeCollection(id, value, type, nextRevision, createdAt, now, editable, recipes);
    }

    public RecipeCollection withRecipes(List<Recipe> values, long nextRevision, long now) {
        return new RecipeCollection(id, name, type, nextRevision, createdAt, now, editable, values);
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("schemaVersion", SCHEMA_VERSION);
        json.put("id", id);
        json.put("name", name);
        json.put("type", type);
        json.put("revision", revision);
        json.put("createdAt", createdAt);
        json.put("updatedAt", updatedAt);
        json.put("editable", editable);
        JSONArray items = new JSONArray();
        for (Recipe recipe : recipes) items.put(recipe.toJson());
        json.put("recipes", items);
        return json;
    }

    public static RecipeCollection fromJson(JSONObject json) {
        if (json == null) throw new IllegalArgumentException("菜谱集数据为空");
        if (json.optInt("schemaVersion", SCHEMA_VERSION) != SCHEMA_VERSION) {
            throw new IllegalArgumentException("暂不支持这个菜谱集版本");
        }
        List<Recipe> recipes = new ArrayList<>();
        JSONArray items = json.optJSONArray("recipes");
        if (items != null) {
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = items.optJSONObject(i);
                if (item != null) recipes.add(Recipe.fromJson(item));
            }
        }
        return new RecipeCollection(
                json.optString("id"), json.optString("name"), json.optString("type"),
                json.optLong("revision"), json.optLong("createdAt"), json.optLong("updatedAt"),
                json.optBoolean("editable"), recipes
        );
    }

    private static List<Recipe> cleanRecipes(List<Recipe> values) {
        List<Recipe> result = new ArrayList<>();
        Set<String> ids = new LinkedHashSet<>();
        if (values == null) return result;
        for (Recipe recipe : values) {
            if (recipe == null || recipe.id == null || recipe.id.trim().isEmpty()
                    || recipe.name == null || recipe.name.trim().isEmpty()
                    || !ids.add(recipe.id)) continue;
            result.add(recipe);
        }
        return result;
    }

    private static String requireText(String value, String message) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) throw new IllegalArgumentException(message);
        return trimmed;
    }
}
