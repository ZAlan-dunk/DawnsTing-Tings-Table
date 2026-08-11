package com.dawns.tingstable.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import com.dawns.tingstable.model.Recipe;
import com.dawns.tingstable.model.RecipeCollection;

import org.junit.Test;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecipeCollectionCloudStateTest {
    @Test
    public void normalNumbersRemainSequentialAndSkipOccupiedIds() {
        RecipeCollectionCloudState state = RecipeCollectionCloudState.empty();
        state.createStandard("一号", Collections.emptyList(), 10L);
        state.createStandard("二号", Collections.emptyList(), 20L);

        RecipeCollectionCloudState parsed = RecipeCollectionCloudState.fromJson(state.toJson());
        RecipeCollection third = parsed.createStandard("三号", Collections.emptyList(), 30L);

        assertEquals("Dew-0003", third.id);
    }

    @Test
    public void staleCounterNeverReusesAnOlderNormalNumber() throws Exception {
        JSONObject raw = new JSONObject();
        raw.put("schemaVersion", 1);
        raw.put("nextNormalNumber", 1);
        raw.put("legacyMigratedProfiles", new JSONArray());
        JSONObject collections = new JSONObject();
        collections.put("Dew-0005", new RecipeCollection(
                "Dew-0005", "旧菜谱集", RecipeCollection.TYPE_STANDARD,
                1L, 1L, 1L, true, Collections.emptyList()).toJson());
        raw.put("collections", collections);

        RecipeCollection created = RecipeCollectionCloudState.fromJson(raw)
                .createStandard("新菜谱集", Collections.emptyList(), 2L);

        assertEquals("Dew-0006", created.id);
    }

    @Test
    public void specialCollectionIsStoredInTheCompleteCloudState() {
        RecipeCollectionCloudState state = RecipeCollectionCloudState.empty();

        RecipeCollection special = state.ensureSpecial("KKLLTL", "露露的小厨房", 10L);
        RecipeCollectionCloudState parsed = RecipeCollectionCloudState.fromJson(state.toJson());

        assertEquals(RecipeCollection.TYPE_SPECIAL, special.type);
        assertEquals("露露的小厨房", parsed.find("KKLLTL").name);
        assertEquals(1, parsed.collections().size());
    }

    @Test
    public void sameNameRecipesWithDifferentIdsRemainDistinct() {
        List<Recipe> merged = RecipeCollectionCloudState.mergeRecipes(Arrays.asList(
                recipe("custom-a", "同名菜"), recipe("custom-b", "同名菜"),
                recipe("custom-a", "更新后的同名菜")
        ));

        assertEquals(2, merged.size());
        assertTrue(merged.stream().anyMatch(value -> value.id.equals("custom-b")));
        assertEquals("更新后的同名菜", merged.stream()
                .filter(value -> value.id.equals("custom-a")).findFirst().orElseThrow().name);
    }

    @Test
    public void staleRevisionRequiresAnExplicitForceSave() {
        RecipeCollectionCloudState state = RecipeCollectionCloudState.empty();
        RecipeCollection original = state.createStandard("原名", Collections.emptyList(), 10L);
        RecipeCollection firstUpdate = original.withName("云端新名", original.revision, 20L);
        state.applyUpdate(firstUpdate, false, 20L);

        RecipeCollection stale = original.withName("本机名称", original.revision, 30L);
        assertThrows(IllegalStateException.class, () -> state.applyUpdate(stale, false, 30L));

        RecipeCollection forced = state.applyUpdate(stale, true, 30L);
        assertEquals("本机名称", forced.name);
        assertEquals(3L, forced.revision);
    }

    private Recipe recipe(String id, String name) {
        return new Recipe(id, name, "炒菜", "川菜", "家常", "简单", 10, 1,
                Collections.emptyList(), Collections.singletonList("完成"), "", true);
    }
}
