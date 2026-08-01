package com.dawns.tingstable.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class RecipeCuisines {
    public static final String ALL = "全部菜系";
    public static final String HOME_FUSION = "家常融合";
    public static final String SICHUAN = "川菜";
    public static final String HUNAN = "湘菜";
    public static final String CANTONESE = "粤菜";
    public static final String JIANGZHE = "江浙菜";
    public static final String NORTHERN = "北方菜";
    public static final String WESTERN = "西式";
    public static final String OTHER = "其他";

    private static final List<String> EDITABLE = Collections.unmodifiableList(Arrays.asList(
            HOME_FUSION, SICHUAN, HUNAN, CANTONESE, JIANGZHE, NORTHERN, WESTERN, OTHER
    ));
    private static final List<String> ALL_VALUES;

    static {
        List<String> values = new ArrayList<>();
        values.add(ALL);
        values.addAll(EDITABLE);
        ALL_VALUES = Collections.unmodifiableList(values);
    }

    private RecipeCuisines() {}

    public static List<String> all() {
        return ALL_VALUES;
    }

    public static List<String> editable() {
        return EDITABLE;
    }

    public static String normalize(String value) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) return HOME_FUSION;
        return EDITABLE.contains(trimmed) ? trimmed : OTHER;
    }
}
