package com.dawns.tingstable.data;

import com.dawns.tingstable.model.RecipeCollection;

import java.util.Set;

public final class RecipeCollectionVisibility {
    private RecipeCollectionVisibility() { }

    public static boolean isVisible(RecipeCollection collection, Set<String> unlockedSpecialIds) {
        if (collection == null) return false;
        return !RecipeCollection.TYPE_SPECIAL.equals(collection.type)
                || (unlockedSpecialIds != null && unlockedSpecialIds.contains(collection.id));
    }

    public static boolean canEdit(RecipeCollection collection, Set<String> unlockedSpecialIds) {
        return isVisible(collection, unlockedSpecialIds);
    }
}
