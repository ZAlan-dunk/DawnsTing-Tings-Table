package com.dawns.tingstable.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.Arrays;

public class RecipeCuisinesTest {
    @Test
    public void missingCuisineMigratesToHomeFusion() {
        assertEquals("家常融合", RecipeCuisines.normalize(null));
        assertEquals("家常融合", RecipeCuisines.normalize("  "));
    }

    @Test
    public void unknownCuisineFallsBackToOther() {
        assertEquals("其他", RecipeCuisines.normalize("私房菜系"));
    }

    @Test
    public void editableCuisinesKeepProductOrder() {
        assertEquals(
                Arrays.asList("家常融合", "川菜", "湘菜", "粤菜", "江浙菜", "北方菜", "西式", "其他"),
                RecipeCuisines.editable()
        );
    }
}
