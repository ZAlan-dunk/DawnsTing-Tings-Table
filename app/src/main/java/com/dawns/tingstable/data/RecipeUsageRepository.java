package com.dawns.tingstable.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.dawns.tingstable.model.RecipeUsage;

import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Map;

public final class RecipeUsageRepository {
    private static final String PREFS = "recipe_usage_v01";
    private static final String KEY_USAGE = "usage";

    private final SharedPreferences preferences;

    public RecipeUsageRepository(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public void recordOpen(String recipeId, long now) {
        String id = recipeId == null ? "" : recipeId.trim();
        if (id.isEmpty()) return;
        Map<String, RecipeUsage> values = getAll();
        RecipeUsage previous = values.get(id);
        int count = previous == null ? 1 : previous.openCount + 1;
        values.put(id, new RecipeUsage(count, now));
        persist(values);
    }

    public Map<String, RecipeUsage> getAll() {
        Map<String, RecipeUsage> result = new LinkedHashMap<>();
        try {
            JSONObject root = new JSONObject(preferences.getString(KEY_USAGE, "{}"));
            Iterator<String> keys = root.keys();
            while (keys.hasNext()) {
                String id = keys.next();
                JSONObject item = root.optJSONObject(id);
                if (item != null) {
                    result.put(id, new RecipeUsage(item.optInt("count"), item.optLong("lastOpenedAt")));
                }
            }
        } catch (Exception ignored) { }
        return result;
    }

    private void persist(Map<String, RecipeUsage> values) {
        try {
            JSONObject root = new JSONObject();
            for (Map.Entry<String, RecipeUsage> entry : values.entrySet()) {
                JSONObject item = new JSONObject();
                item.put("count", entry.getValue().openCount);
                item.put("lastOpenedAt", entry.getValue().lastOpenedAt);
                root.put(entry.getKey(), item);
            }
            preferences.edit().putString(KEY_USAGE, root.toString()).apply();
        } catch (Exception ignored) { }
    }
}
