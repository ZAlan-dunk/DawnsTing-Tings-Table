package com.dawns.tingstable.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.dawns.tingstable.model.PantryItem;
import com.dawns.tingstable.util.RecipeMatcher;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class PantryRepository {
    private static final String PREFS = "tings_table_v01";
    private static final String KEY_PANTRY = "pantry_items_v04";
    private static final String KEY_MIGRATED = "pantry_migration_completed_v04";
    private static final String KEY_SELECTED = "selected_ingredients";

    private final SharedPreferences preferences;

    public PantryRepository(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        migrateSelectedIngredients();
    }

    public List<PantryItem> getItems() {
        List<PantryItem> result = new ArrayList<>();
        String raw = preferences.getString(KEY_PANTRY, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject json = array.optJSONObject(i);
                if (json != null) {
                    PantryItem item = PantryItem.fromJson(json);
                    if (!item.name.isEmpty()) result.add(item);
                }
            }
        } catch (Exception ignored) { }
        return result;
    }

    public void saveItem(PantryItem item) {
        List<PantryItem> items = getItems();
        if (item.id == null || item.id.trim().isEmpty()) item.id = "pantry-" + UUID.randomUUID();
        boolean replaced = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).id.equals(item.id)) {
                items.set(i, item.copy());
                replaced = true;
                break;
            }
        }
        if (!replaced) items.add(item.copy());
        persist(items);
    }

    public void deleteItem(String id) {
        List<PantryItem> items = getItems();
        items.removeIf(item -> item.id.equals(id));
        persist(items);
    }

    public PantryItem findByName(String name) {
        String canonical = RecipeMatcher.canonicalize(name);
        for (PantryItem item : getItems()) {
            if (RecipeMatcher.canonicalize(item.name).equals(canonical)) return item.copy();
        }
        return null;
    }

    public PantryItem addOrRestock(String name, String quantity, String unit) {
        PantryItem item = findByName(name);
        if (item == null) {
            item = new PantryItem("", name, guessCategory(name), quantity, unit,
                    PantryItem.STATUS_FULL, System.currentTimeMillis(), "");
        } else {
            if (quantity != null && !quantity.trim().isEmpty()) item.quantity = quantity.trim();
            if (unit != null && !unit.trim().isEmpty()) item.unit = unit.trim();
            item.status = PantryItem.STATUS_FULL;
            item.purchasedAt = System.currentTimeMillis();
        }
        saveItem(item);
        return item.copy();
    }

    public Set<String> getAvailableIngredientNames() {
        Set<String> names = new LinkedHashSet<>();
        for (PantryItem item : getItems()) if (item.available()) names.add(item.name);
        return names;
    }

    public boolean replaceItems(List<PantryItem> replacement) {
        JSONArray array = new JSONArray();
        Set<String> ids = new LinkedHashSet<>();
        try {
            if (replacement != null) {
                for (PantryItem item : replacement) {
                    if (item == null || item.id.isEmpty() || item.name.isEmpty() || !ids.add(item.id)) continue;
                    array.put(item.copy().toJson());
                }
            }
            return preferences.edit()
                    .putString(KEY_PANTRY, array.toString())
                    .putBoolean(KEY_MIGRATED, true)
                    .commit();
        } catch (Exception ignored) {
            return false;
        }
    }

    private void migrateSelectedIngredients() {
        if (preferences.getBoolean(KEY_MIGRATED, false)) return;
        List<PantryItem> items = getItems();
        Set<String> existing = new LinkedHashSet<>();
        for (PantryItem item : items) existing.add(RecipeMatcher.canonicalize(item.name));
        Set<String> selected = preferences.getStringSet(KEY_SELECTED, new LinkedHashSet<>());
        if (selected != null) {
            for (String name : selected) {
                if (name == null || name.trim().isEmpty()) continue;
                if (existing.add(RecipeMatcher.canonicalize(name))) {
                    items.add(new PantryItem("pantry-" + UUID.randomUUID(), name, guessCategory(name),
                            "", "", PantryItem.STATUS_FULL, 0L, ""));
                }
            }
        }
        persist(items);
        preferences.edit().putBoolean(KEY_MIGRATED, true).apply();
    }

    private void persist(List<PantryItem> items) {
        JSONArray array = new JSONArray();
        try {
            for (PantryItem item : items) array.put(item.toJson());
            preferences.edit().putString(KEY_PANTRY, array.toString()).apply();
        } catch (Exception ignored) { }
    }

    private String guessCategory(String name) {
        String value = name == null ? "" : name;
        if (containsAny(value, "猪", "牛", "羊", "鸡翅", "鸡胸", "鸡肉", "鸭", "鹅", "排骨", "肉")) return "肉禽";
        if (containsAny(value, "鱼", "虾", "蟹", "贝", "紫菜", "海带", "鱿鱼")) return "水鲜";
        if (containsAny(value, "蛋", "豆腐", "豆干", "豆皮", "牛奶", "奶酪", "酸奶")) return "蛋豆";
        if (containsAny(value, "米", "面", "馒头", "饼", "粉", "小米", "燕麦")) return "谷物";
        if (containsAny(value, "油", "盐", "糖", "醋", "酱", "料酒", "花椒", "辣椒粉")) return "调料";
        if (containsAny(value, "苹果", "香蕉", "橙", "梨", "葡萄", "桃", "莓", "水果")) return "水果";
        if (containsAny(value, "菜", "瓜", "椒", "菇", "菌", "笋", "葱", "姜", "蒜", "土豆", "番茄", "西红柿", "萝卜", "茄子", "南瓜")) return "时蔬";
        return "其他";
    }

    private boolean containsAny(String value, String... needles) {
        for (String needle : needles) if (value.contains(needle)) return true;
        return false;
    }
}
