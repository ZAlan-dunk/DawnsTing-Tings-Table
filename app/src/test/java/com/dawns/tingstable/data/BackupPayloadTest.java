package com.dawns.tingstable.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.dawns.tingstable.model.Ingredient;
import com.dawns.tingstable.model.PantryItem;
import com.dawns.tingstable.model.Recipe;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedHashSet;

public class BackupPayloadTest {
    @Test
    public void roundTripPreservesPersonalData() throws Exception {
        Recipe recipe = new Recipe(
                "custom-1", "测试菜", "炒菜", "川菜", "微辣", "简单", 18, 2,
                Arrays.asList(new Ingredient("青椒", "2个", false)),
                Arrays.asList("切配", "翻炒"), "趁热吃", true
        );
        PantryItem pantry = new PantryItem(
                "pantry-1", "青椒", "时蔬", "2", "个",
                PantryItem.STATUS_FULL, 1234L, "今晚用"
        );
        BackupPayload original = new BackupPayload(
                "tings", "0.6.6-Bata", 5678L,
                Arrays.asList(recipe), setOf("custom-1"), Arrays.asList(pantry),
                Arrays.asList("鸡蛋", "青椒"), setOf("青椒"), "night"
        );

        BackupPayload parsed = BackupPayload.parse(original.toJson().toString(), "tings");

        assertEquals("tings", parsed.profileId);
        assertEquals("0.6.6-Bata", parsed.appVersion);
        assertEquals(5678L, parsed.createdAt);
        assertEquals(1, parsed.customRecipes.size());
        assertEquals("测试菜", parsed.customRecipes.get(0).name);
        assertEquals(1, parsed.pantryItems.size());
        assertEquals(Arrays.asList("鸡蛋", "青椒"), parsed.shoppingList);
        assertEquals("night", parsed.themeMode);
    }

    @Test
    public void rejectsAnotherProfile() throws Exception {
        BackupPayload payload = emptyPayload("tings");
        try {
            BackupPayload.parse(payload.toJson().toString(), "lulu");
            fail("Expected a profile mismatch");
        } catch (JSONException expected) {
            assertEquals("备份不属于当前使用者", expected.getMessage());
        }
    }

    @Test
    public void rejectsUnknownSchema() throws Exception {
        JSONObject json = emptyPayload("tings").toJson();
        json.put("schemaVersion", 99);
        try {
            BackupPayload.parse(json.toString(), "tings");
            fail("Expected an unsupported schema");
        } catch (JSONException expected) {
            assertEquals("暂不支持这个备份版本", expected.getMessage());
        }
    }

    @Test
    public void removesDuplicateIdsAndStrings() throws Exception {
        JSONObject json = emptyPayload("tings").toJson();
        Recipe recipe = new Recipe(
                "custom-1", "测试菜", "炒菜", "家常", "简单", 10, 1,
                Arrays.asList(), Arrays.asList("完成"), "", true
        );
        json.getJSONArray("customRecipes").put(recipe.toJson()).put(recipe.toJson());
        json.getJSONArray("favorites").put("custom-1").put("custom-1").put(" ");
        json.getJSONArray("shoppingList").put("鸡蛋").put("鸡蛋");

        BackupPayload parsed = BackupPayload.parse(json.toString(), "tings");

        assertEquals(1, parsed.customRecipes.size());
        assertEquals(1, parsed.favorites.size());
        assertEquals(1, parsed.shoppingList.size());
        assertTrue(parsed.favorites.contains("custom-1"));
    }

    private BackupPayload emptyPayload(String profileId) {
        return new BackupPayload(
                profileId, "0.6.6-Bata", 1L,
                Arrays.asList(), setOf(), Arrays.asList(), Arrays.asList(), setOf(), "light"
        );
    }

    private LinkedHashSet<String> setOf(String... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }
}
