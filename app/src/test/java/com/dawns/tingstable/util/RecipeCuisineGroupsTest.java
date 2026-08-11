package com.dawns.tingstable.util;

import static org.junit.Assert.assertEquals;

import com.dawns.tingstable.model.Recipe;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class RecipeCuisineGroupsTest {
    @Test
    public void groupsInTheConfiguredCuisineOrderAndOmitsEmptyGroups() {
        Map<String, java.util.List<Recipe>> groups = RecipeCuisineGroups.group(Arrays.asList(
                recipe("cantonese", RecipeCuisines.CANTONESE),
                recipe("sichuan", RecipeCuisines.SICHUAN),
                recipe("sichuan-2", RecipeCuisines.SICHUAN)
        ));

        assertEquals(Arrays.asList(RecipeCuisines.SICHUAN, RecipeCuisines.CANTONESE),
                Arrays.asList(groups.keySet().toArray(new String[0])));
        assertEquals(2, groups.get(RecipeCuisines.SICHUAN).size());
    }

    private Recipe recipe(String id, String cuisine) {
        return new Recipe(id, id, "炒菜", cuisine, "家常", "简单", 10, 1,
                Collections.emptyList(), Collections.singletonList("完成"), "", false);
    }
}
