package com.dawns.tingstable.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.dawns.tingstable.model.Ingredient;
import com.dawns.tingstable.model.Recipe;
import com.dawns.tingstable.util.RecipeCuisines;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class RecipeRepository {
    private static final String PREFS = "tings_table_v01";
    private static final String KEY_CUSTOM = "custom_recipes";
    private static final String KEY_FAVORITES = "favorites";
    private static final String KEY_SELECTED = "selected_ingredients";
    private static final String KEY_SHOPPING = "shopping_list";
    private static final String KEY_SHOPPING_ORDERED = "shopping_list_ordered_v02";
    private static final String KEY_DATA_VERSION = "data_version";

    private final SharedPreferences preferences;
    private final List<Recipe> builtIns;

    public RecipeRepository(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        builtIns = createBuiltIns();
        preferences.edit().putInt(KEY_DATA_VERSION, Math.max(3, preferences.getInt(KEY_DATA_VERSION, 1))).apply();
    }

    public List<Recipe> getAllRecipes() {
        List<Recipe> recipes = new ArrayList<>(builtIns);
        recipes.addAll(getCustomRecipes());
        Collections.sort(recipes, Comparator.comparing(recipe -> recipe.name));
        return recipes;
    }

    public Recipe findById(String id) {
        for (Recipe recipe : getAllRecipes()) if (recipe.id.equals(id)) return recipe;
        return null;
    }

    public List<Recipe> getCustomRecipes() {
        List<Recipe> result = new ArrayList<>();
        String raw = preferences.getString(KEY_CUSTOM, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item != null) result.add(Recipe.fromJson(item));
            }
        } catch (Exception ignored) { }
        return result;
    }

    public void saveCustomRecipe(Recipe recipe) {
        List<Recipe> recipes = getCustomRecipes();
        if (recipe.id == null || recipe.id.trim().isEmpty()) recipe.id = "custom-" + UUID.randomUUID();
        recipe.custom = true;
        boolean replaced = false;
        for (int i = 0; i < recipes.size(); i++) {
            if (recipes.get(i).id.equals(recipe.id)) {
                recipes.set(i, recipe);
                replaced = true;
                break;
            }
        }
        if (!replaced) recipes.add(recipe);
        persistCustomRecipes(recipes);
    }

    public void deleteCustomRecipe(String id) {
        List<Recipe> recipes = getCustomRecipes();
        recipes.removeIf(recipe -> recipe.id.equals(id));
        persistCustomRecipes(recipes);
        Set<String> favorites = getFavorites();
        favorites.remove(id);
        saveStringSet(KEY_FAVORITES, favorites);
    }

    private void persistCustomRecipes(List<Recipe> recipes) {
        JSONArray array = new JSONArray();
        try {
            for (Recipe recipe : recipes) array.put(recipe.toJson());
            preferences.edit().putString(KEY_CUSTOM, array.toString()).apply();
        } catch (Exception ignored) { }
    }

    public boolean replacePersonalData(List<Recipe> customRecipes, Set<String> favorites,
                                       Set<String> selectedIngredients, List<String> shoppingItems) {
        try {
            JSONArray recipeArray = new JSONArray();
            Set<String> recipeIds = new LinkedHashSet<>();
            for (Recipe recipe : customRecipes) {
                if (recipe == null || recipe.id == null || recipe.id.trim().isEmpty()
                        || recipe.name == null || recipe.name.trim().isEmpty()
                        || !recipeIds.add(recipe.id)) continue;
                recipe.custom = true;
                recipeArray.put(recipe.toJson());
            }

            LinkedHashSet<String> cleanFavorites = cleanStrings(favorites);
            LinkedHashSet<String> cleanSelected = cleanStrings(selectedIngredients);
            List<String> cleanShopping = new ArrayList<>(cleanStrings(shoppingItems));
            JSONArray shoppingArray = new JSONArray();
            for (String item : cleanShopping) shoppingArray.put(item);

            return preferences.edit()
                    .putString(KEY_CUSTOM, recipeArray.toString())
                    .putStringSet(KEY_FAVORITES, cleanFavorites)
                    .putStringSet(KEY_SELECTED, cleanSelected)
                    .putString(KEY_SHOPPING_ORDERED, shoppingArray.toString())
                    .putStringSet(KEY_SHOPPING, new LinkedHashSet<>(cleanShopping))
                    .commit();
        } catch (Exception ignored) {
            return false;
        }
    }

    public Set<String> getFavorites() { return getStringSet(KEY_FAVORITES); }

    public boolean isFavorite(String id) { return getFavorites().contains(id); }

    public void toggleFavorite(String id) {
        Set<String> favorites = getFavorites();
        if (!favorites.add(id)) favorites.remove(id);
        saveStringSet(KEY_FAVORITES, favorites);
    }

    public Set<String> getSelectedIngredients() { return getStringSet(KEY_SELECTED); }
    public void saveSelectedIngredients(Set<String> values) { saveStringSet(KEY_SELECTED, values); }

    public Set<String> getShoppingList() { return new LinkedHashSet<>(getShoppingItems()); }

    public List<String> getShoppingItems() {
        List<String> result = new ArrayList<>();
        String ordered = preferences.getString(KEY_SHOPPING_ORDERED, "");
        if (!ordered.isEmpty()) {
            try {
                JSONArray array = new JSONArray(ordered);
                for (int i = 0; i < array.length(); i++) {
                    String item = array.optString(i, "").trim();
                    if (!item.isEmpty() && !result.contains(item)) result.add(item);
                }
                return result;
            } catch (Exception ignored) { }
        }
        result.addAll(getStringSet(KEY_SHOPPING));
        Collections.sort(result);
        return result;
    }

    public void addShoppingItems(List<String> values) {
        List<String> items = getShoppingItems();
        for (String value : values) {
            String item = value == null ? "" : value.trim();
            if (!item.isEmpty() && !items.contains(item)) items.add(item);
        }
        saveShoppingItems(items);
    }

    public void removeShoppingItem(String value) {
        List<String> items = getShoppingItems();
        items.remove(value);
        saveShoppingItems(items);
    }

    public void clearShoppingList() {
        preferences.edit().remove(KEY_SHOPPING).remove(KEY_SHOPPING_ORDERED).apply();
    }

    private void saveShoppingItems(List<String> values) {
        JSONArray array = new JSONArray();
        for (String value : values) array.put(value);
        preferences.edit()
                .putString(KEY_SHOPPING_ORDERED, array.toString())
                .putStringSet(KEY_SHOPPING, new LinkedHashSet<>(values))
                .apply();
    }

    private Set<String> getStringSet(String key) {
        return new LinkedHashSet<>(preferences.getStringSet(key, Collections.emptySet()));
    }

    private void saveStringSet(String key, Set<String> values) {
        preferences.edit().putStringSet(key, new LinkedHashSet<>(values)).apply();
    }

    private LinkedHashSet<String> cleanStrings(Iterable<String> values) {
        LinkedHashSet<String> result = new LinkedHashSet<>();
        if (values == null) return result;
        for (String value : values) {
            String item = value == null ? "" : value.trim();
            if (!item.isEmpty()) result.add(item);
        }
        return result;
    }

    public List<String> getAllIngredientNames() {
        Set<String> names = new LinkedHashSet<>();
        for (Recipe recipe : getAllRecipes()) {
            for (Ingredient ingredient : recipe.ingredients) {
                if (!ingredient.staple) names.add(ingredient.name);
            }
        }
        List<String> result = new ArrayList<>(names);
        Collections.sort(result);
        return result;
    }

    private static Ingredient i(String name, String amount) { return new Ingredient(name, amount, false); }
    private static Ingredient s(String name, String amount) { return new Ingredient(name, amount, true); }
    private static List<Ingredient> ingredients(Ingredient... values) { return Arrays.asList(values); }
    private static List<String> steps(String... values) { return Arrays.asList(values); }

    private static Recipe r(String id, String name, String category, String cuisine, String flavor, String difficulty,
                            int minutes, int servings, List<Ingredient> ingredients,
                            List<String> steps, String tips) {
        return new Recipe(id, name, category, cuisine, flavor, difficulty, minutes, servings,
                ingredients, steps, tips, false);
    }

    private static List<Recipe> createBuiltIns() {
        List<Recipe> recipes = new ArrayList<>();
        recipes.add(r("builtin-tomato-egg", "西红柿炒鸡蛋", "快手菜", RecipeCuisines.HOME_FUSION, "咸鲜微甜", "简单", 15, 2,
                ingredients(i("西红柿", "2个"), i("鸡蛋", "3个"), s("食用油", "适量"), s("盐", "适量"), s("白糖", "1小勺")),
                steps("西红柿切块，鸡蛋加少许盐打散。", "热锅下油，将鸡蛋炒至凝固后盛出。", "原锅炒软西红柿，加盐和白糖调味。", "倒回鸡蛋，翻炒均匀即可。"),
                "西红柿炒出汤汁后再放鸡蛋，口感更融合。"));
        recipes.add(r("builtin-potato", "酸辣土豆丝", "素菜", RecipeCuisines.SICHUAN, "酸辣", "简单", 20, 2,
                ingredients(i("土豆", "2个"), i("青椒", "1个"), i("干辣椒", "3个"), s("食用油", "适量"), s("盐", "适量"), s("醋", "1汤勺")),
                steps("土豆切细丝，用清水洗去表面淀粉。", "青椒切丝，干辣椒切段。", "热锅下油爆香干辣椒，放入土豆丝大火翻炒。", "加入青椒、盐和醋，炒至断生。"),
                "土豆丝洗净淀粉并大火快炒，成品更爽脆。"));
        recipes.add(r("builtin-green-pepper-pork", "青椒肉丝", "家常菜", RecipeCuisines.SICHUAN, "咸鲜", "中等", 25, 2,
                ingredients(i("猪里脊", "200克"), i("青椒", "2个"), s("生抽", "1汤勺"), s("淀粉", "1小勺"), s("食用油", "适量"), s("盐", "适量")),
                steps("猪里脊切丝，加生抽和淀粉抓匀腌制10分钟。", "青椒去籽切丝。", "热锅下油，将肉丝滑炒至变色。", "加入青椒大火翻炒，调盐后出锅。"),
                "肉丝逆纹切、短时间滑炒，口感更嫩。"));
        recipes.add(r("builtin-cola-wings", "可乐鸡翅", "家常菜", RecipeCuisines.HOME_FUSION, "咸甜", "简单", 35, 2,
                ingredients(i("鸡翅中", "8个"), i("可乐", "330毫升"), i("姜", "3片"), s("生抽", "2汤勺"), s("食用油", "少许")),
                steps("鸡翅两面划刀，冷水下锅焯去浮沫。", "锅中少油，将鸡翅煎至两面微黄。", "加入姜片、生抽和可乐，大火煮开。", "转中小火焖20分钟，最后大火收汁。"),
                "收汁时勤翻动，避免含糖汤汁粘锅。"));
        recipes.add(r("builtin-mapo-tofu", "麻婆豆腐", "下饭菜", RecipeCuisines.SICHUAN, "麻辣", "中等", 25, 2,
                ingredients(i("嫩豆腐", "1盒"), i("猪肉末", "100克"), i("豆瓣酱", "1汤勺"), i("花椒", "适量"), i("葱", "1根"), s("生抽", "1小勺"), s("淀粉", "1小勺")),
                steps("豆腐切块，用淡盐水浸泡。", "锅中炒香肉末，加入豆瓣酱炒出红油。", "加适量清水和豆腐，小火煮8分钟。", "淀粉水分两次勾芡，撒花椒粉和葱花。"),
                "豆腐下锅后轻推，不要频繁翻动。"));
        recipes.add(r("builtin-broccoli", "蒜蓉西兰花", "素菜", RecipeCuisines.HOME_FUSION, "清香", "简单", 15, 2,
                ingredients(i("西兰花", "1棵"), i("大蒜", "5瓣"), s("食用油", "适量"), s("盐", "适量")),
                steps("西兰花切小朵，在淡盐水中浸泡后洗净。", "沸水加少许盐和油，将西兰花焯水1分钟。", "热锅下油爆香蒜末，放入西兰花快速翻炒。", "加盐调味后出锅。"),
                "焯水时间不要过长，以保持翠绿和脆嫩。"));
        recipes.add(r("builtin-kungpao", "宫保鸡丁", "下饭菜", RecipeCuisines.SICHUAN, "酸甜微辣", "中等", 30, 3,
                ingredients(i("鸡胸肉", "250克"), i("花生米", "50克"), i("黄瓜", "半根"), i("干辣椒", "5个"), i("葱", "1根"), s("生抽", "1汤勺"), s("醋", "1汤勺"), s("白糖", "1汤勺"), s("淀粉", "1小勺")),
                steps("鸡胸肉切丁，加生抽和淀粉腌制。", "黄瓜切丁，调好糖醋碗汁。", "热锅炒香干辣椒和葱段，放鸡丁炒至变色。", "加入黄瓜、花生米和碗汁，大火翻匀。"),
                "花生米最后放，能保持酥脆。"));
        recipes.add(r("builtin-egg-fried-rice", "家常蛋炒饭", "主食", RecipeCuisines.HOME_FUSION, "咸香", "简单", 15, 1,
                ingredients(i("米饭", "1碗"), i("鸡蛋", "2个"), i("胡萝卜", "半根"), i("葱", "1根"), s("食用油", "适量"), s("盐", "适量")),
                steps("米饭提前拨散，胡萝卜切小丁。", "鸡蛋打散后炒熟盛出。", "原锅炒香胡萝卜丁，加入米饭大火翻炒。", "加入鸡蛋和葱花，以盐调味。"),
                "隔夜冷米饭水分较少，更容易炒得粒粒分明。"));
        recipes.add(r("builtin-seaweed-soup", "紫菜蛋花汤", "汤羹", RecipeCuisines.HOME_FUSION, "清鲜", "简单", 10, 2,
                ingredients(i("紫菜", "1小把"), i("鸡蛋", "1个"), i("葱", "1根"), s("盐", "适量"), s("香油", "少许")),
                steps("紫菜用清水快速冲洗，鸡蛋打散。", "锅中水烧开，放入紫菜。", "沿锅边缓慢淋入蛋液，待凝固后轻推。", "加盐和香油，撒葱花。"),
                "蛋液下锅后先不要搅动，蛋花会更完整。"));
        recipes.add(r("builtin-mushroom-greens", "香菇炒青菜", "素菜", RecipeCuisines.HOME_FUSION, "清鲜", "简单", 15, 2,
                ingredients(i("鲜香菇", "6朵"), i("上海青", "300克"), i("大蒜", "3瓣"), s("食用油", "适量"), s("盐", "适量")),
                steps("香菇切片，上海青洗净沥水。", "热锅下油爆香蒜末，先炒香菇。", "加入上海青大火翻炒至变软。", "加盐调味，快速出锅。"),
                "青菜下锅前尽量沥干，避免炒制时出水过多。"));
        recipes.add(r("builtin-braised-eggplant", "家常红烧茄子", "下饭菜", RecipeCuisines.HOME_FUSION, "咸鲜", "中等", 30, 3,
                ingredients(i("茄子", "2根"), i("青椒", "1个"), i("大蒜", "4瓣"), s("生抽", "1汤勺"), s("白糖", "半小勺"), s("淀粉", "1小勺"), s("食用油", "适量")),
                steps("茄子切滚刀块，加少许盐静置后挤去水分。", "茄子表面薄裹淀粉，煎至软嫩。", "加入蒜末和青椒翻炒。", "倒入生抽、白糖和少量清水调成的料汁，收浓即可。"),
                "茄子提前用盐腌制，可以减少吸油。"));
        recipes.add(r("builtin-steamed-fish", "清蒸鲈鱼", "家常菜", RecipeCuisines.CANTONESE, "鲜香", "中等", 25, 3,
                ingredients(i("鲈鱼", "1条"), i("姜", "5片"), i("葱", "2根"), s("蒸鱼豉油", "2汤勺"), s("食用油", "1汤勺")),
                steps("鲈鱼处理干净，在鱼身两侧划刀。", "盘底铺姜葱，放鱼后水开上锅蒸8至10分钟。", "倒掉盘中腥水，换上新葱丝。", "淋蒸鱼豉油，再浇一勺热油。"),
                "蒸制时间按鱼的大小调整，关火后可焖2分钟。"));
        recipes.add(r("builtin-beef-potato", "土豆炖牛肉", "炖菜", RecipeCuisines.NORTHERN, "浓香", "中等", 80, 4,
                ingredients(i("牛腩", "500克"), i("土豆", "2个"), i("胡萝卜", "1根"), i("番茄", "1个"), i("姜", "4片"), s("生抽", "2汤勺"), s("盐", "适量")),
                steps("牛腩切块，冷水下锅焯水。", "锅中炒香姜片和番茄，加入牛肉翻炒。", "加热水没过食材，小火炖50分钟。", "加入土豆和胡萝卜，再炖20分钟并调味。"),
                "炖肉时加入热水，肉质不易骤然收紧。"));
        recipes.add(r("builtin-lettuce", "蚝油生菜", "素菜", RecipeCuisines.CANTONESE, "咸鲜", "简单", 10, 2,
                ingredients(i("生菜", "1棵"), i("大蒜", "3瓣"), s("蚝油", "1汤勺"), s("生抽", "半汤勺"), s("食用油", "适量")),
                steps("生菜洗净，大蒜切末。", "生菜放入沸水中快速焯至变色，捞出装盘。", "锅中少油炒香蒜末，加入蚝油、生抽和少量清水。", "将料汁煮开后淋在生菜上。"),
                "生菜焯水十几秒即可，时间过长会失去爽脆口感。"));
        recipes.add(r("builtin-pumpkin-porridge", "南瓜小米粥", "早餐", RecipeCuisines.NORTHERN, "清甜", "简单", 40, 3,
                ingredients(i("南瓜", "250克"), i("小米", "100克"), s("清水", "1000毫升")),
                steps("小米淘洗后浸泡10分钟，南瓜去皮切小块。", "锅中水烧开，放入小米并搅拌。", "加入南瓜，小火煮30分钟。", "期间偶尔搅拌，煮至浓稠。"),
                "水开后再下小米，粥更容易香浓。"));
        return recipes;
    }
}
