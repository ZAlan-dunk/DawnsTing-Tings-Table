package com.dawns.tingstable.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.dawns.tingstable.model.RecipeCollection;

import org.junit.Test;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class RecipeCollectionVisibilityTest {
    @Test
    public void standardCollectionsAreVisibleWithoutAKey() {
        RecipeCollection standard = collection("Dew-0001", RecipeCollection.TYPE_STANDARD);

        assertTrue(RecipeCollectionVisibility.isVisible(standard, Collections.emptySet()));
        assertTrue(RecipeCollectionVisibility.canEdit(standard, Collections.emptySet()));
    }

    @Test
    public void specialCollectionStaysHiddenUntilItsLocalIdIsUnlocked() {
        RecipeCollection special = collection("KKLLTL", RecipeCollection.TYPE_SPECIAL);
        Set<String> unlocked = new LinkedHashSet<>();

        assertFalse(RecipeCollectionVisibility.isVisible(special, unlocked));
        unlocked.add("KKLLTL");
        assertTrue(RecipeCollectionVisibility.isVisible(special, unlocked));
        assertTrue(RecipeCollectionVisibility.canEdit(special, unlocked));
    }

    private RecipeCollection collection(String id, String type) {
        return new RecipeCollection(id, "测试菜谱集", type, 1L, 1L, 1L, true,
                Collections.emptyList());
    }
}
