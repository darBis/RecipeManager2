package haveric.recipeManager.recipes.craft;

import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.flag.Flags;
import haveric.recipeManager.flag.conditions.ConditionsIngredient;
import haveric.recipeManager.flag.flags.FlagIngredientCondition;
import haveric.recipeManager.flag.flags.FlagItemName;
import haveric.recipeManager.messages.Messages;
import haveric.recipeManager.recipes.BaseRecipe;
import haveric.recipeManager.recipes.ItemResult;
import haveric.recipeManager.tools.Tools;
import haveric.recipeManager.tools.ToolsItem;
import haveric.recipeManager.tools.Version;
import haveric.recipeManagerCommon.RMCChatColor;
import haveric.recipeManagerCommon.RMCVanilla;
import haveric.recipeManagerCommon.recipes.RMCRecipeType;
import haveric.recipeManagerCommon.util.RMCUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;

import java.util.*;
import java.util.Map.Entry;

public class CraftRecipe1_13 extends CraftRecipe {
    private Map<Character, List<Material>> ingredientsChoiceMap = new HashMap<>();
    private String[] choiceShape;

    private ItemStack[] ingredients;
    private int width;
    private int height;
    private boolean mirror = false;

    public CraftRecipe1_13() {
    }

    public CraftRecipe1_13(ShapedRecipe recipe) {
        setBukkitRecipe(recipe);
        setIngredients(Tools.convertShapedRecipeToItemMatrix(recipe));
        setResult(recipe.getResult());
    }

    public CraftRecipe1_13(BaseRecipe recipe) {
        super(recipe);

        if (recipe instanceof CraftRecipe1_13) {
            CraftRecipe1_13 r = (CraftRecipe1_13) recipe;

            if (r.getIngredients() != null) {
                ingredients = r.getIngredients();
            }

            if (r.ingredientsChoiceMap.size() > 0) {
                ingredientsChoiceMap.putAll(r.ingredientsChoiceMap);
            }

            choiceShape = r.choiceShape;

            width = r.width;
            height = r.height;
            mirror = r.mirror;
        }
    }

    public CraftRecipe1_13(Flags flags) {
        super(flags);
    }

    public void setIngredientsChoiceMap(Map<Character, List<Material>> newIngredientsChoiceMap) {
        ingredientsChoiceMap.clear();
        ingredientsChoiceMap.putAll(newIngredientsChoiceMap);

        updateChoiceHash();
    }

    public Map<Character, List<Material>> getIngredientsChoiceMap() {
        return ingredientsChoiceMap;
    }

    public void setChoiceShape(String[] shape) {
        choiceShape = shape;

        width = shape[0].length();
        height = shape.length;
    }

    public String[] getChoiceShape() {
        return choiceShape;
    }

    /**
     * @return clone of ingredients array's elements
     */
    public ItemStack[] getIngredients() {
        if (ingredients != null) {
            int ingredientsLength = ingredients.length;
            ItemStack[] items = new ItemStack[ingredientsLength];

            for (int i = 0; i < ingredientsLength; i++) {
                if (ingredients[i] == null) {
                    items[i] = null;
                } else {
                    items[i] = ingredients[i].clone();
                }
            }

            return items;
        }

        return null;
    }

    /**
     * Set the ingredients matrix. <br>
     * This also calculates the width and height of the shape matrix.<br>
     * <b>NOTE: Array must have exactly 9 elements, use null for empty slots.</b>
     *
     * @param newIngredients
     *            ingredients matrix, this also defines the shape, width and height.
     */
    public void setIngredients(ItemStack[] newIngredients) {
        if (newIngredients.length != 9) {
            throw new IllegalArgumentException("Recipe " + this.name + " must have exactly 9 items, use null to specify empty slots!");
        }

        ingredients = newIngredients.clone();
        calculate();
    }

    /**
     * Sets an ingredient slot to material with wildcard data value.<br>
     * Slots are like:<br>
     * <code>
     * | 0 1 2 |<br>
     * | 3 4 5 |<br>
     * | 6 7 8 |</code> <br>
     * Null slots are ignored and allow the recipe to be
     * used in a smaller grid (inventory's 2x2 for example)<br> <br>
     * <b>NOTE: always start with index 0!</b> Then you can use whatever index you want up to 8.<br>
     * This is required because ingredients are shifted to top-left corner of the 2D matrix on each call of this method.
     *
     * @param slot
     *            start with 0, then use any index from 1 to 8
     * @param type
     */
    public void setIngredient(int slot, Material type) {
        setIngredient(slot, type, RMCVanilla.DATA_WILDCARD);
    }

    /**
     * Sets an ingredient slot to material with specific data value.<br>
     * Slots are like:<br>
     * <code>
     * | 0 1 2 |<br>
     * | 3 4 5 |<br>
     * | 6 7 8 |</code> <br>
     * Null slots are ignored and allow the recipe to be
     * used in a smaller grid (inventory's 2x2 for example)<br> <br>
     * <b>NOTE: always start with index 0!</b> Then you can use whatever index you want up to 8.<br>
     * This is required because ingredients are shifted to top-left corner of the 2D matrix on each call of this method.
     *
     * @param slot
     *            start with 0, then use any index from 1 to 8
     * @param type
     * @param data
     */
    public void setIngredient(int slot, Material type, int data) {
        if (ingredients == null) {
            ingredients = new ItemStack[9];
        }

        if (slot != 0 && ingredients[0] == null) {
            throw new IllegalArgumentException("A plugin is using setIngredient() with index NOT starting at 0, shape is corrupted!!!");
        }

        if (type == null) {
            ingredients[slot] = null;
        } else {
            ingredients[slot] = new ItemStack(type, 1, (short) data);
        }

        calculate();
    }

    /**
     * @return true if shape was mirrored, usually false.
     */
    public boolean isMirrorShape() {
        return mirror;
    }

    /**
     * Mirror the ingredients shape.<br>
     * Useful for matching recipes, no other real effect.<br>
     * This triggers a hashCode recalculation.
     *
     * @param newMirror
     */
    public void setMirrorShape(boolean newMirror) {
        mirror = newMirror;
        calculate();
    }

    private void calculate() {
        if (ingredients == null) {
            return;
        }

        StringBuilder str = new StringBuilder("craft");

        if (mirror) {
            // Mirror the ingredients shape and trim the item matrix, shift ingredients to top-left corner
            ingredients = Tools.mirrorItemMatrix(ingredients);
        } else {
            // Trim the item matrix, shift ingredients to top-left corner
            Tools.trimItemMatrix(ingredients);
        }

        width = 0;
        height = 0;

        // Calculate width and height of the shape and build the ingredient string for hashing
        for (int h = 0; h < 3; h++) {
            for (int w = 0; w < 3; w++) {
                ItemStack item = ingredients[(h * 3) + w];

                if (item != null) {
                    width = Math.max(width, w);
                    height = Math.max(height, h);

                    str.append(item.getType().toString());
                    if (!Version.has1_13Support() || item instanceof Damageable) {
                        str.append(':').append(item.getDurability());
                    }

                    if (item.getEnchantments().size() > 0) {
                        for (Entry<Enchantment, Integer> entry : item.getEnchantments().entrySet()) {
                            str.append("enchant:").append(entry.getKey().getName()).append(':').append(entry.getValue());
                        }
                    }
                }

                str.append(';');
            }
        }

        width++;
        height++;
        hash = str.toString().hashCode();
    }

    private void updateChoiceHash() {
        StringBuilder str = new StringBuilder("craft ");
        int shapeSize = choiceShape.length;
        for (int i = 0; i < shapeSize; i++) {
            str.append(choiceShape[i]);

            if (i + 1 < shapeSize) {
                str.append(",");
            }
        }

        for (Entry<Character, List<Material>> entry : ingredientsChoiceMap.entrySet()) {
            str.append(" ").append(entry.getKey()).append(":");

            List<Material> materials = entry.getValue();

            int materialsSize = materials.size();
            for (int i = 0; i < materialsSize; i++) {
                str.append(materials.get(i).toString());

                if (i + 1 < materialsSize) {
                    str.append(",");
                }
            }
        }

        hash = str.toString().hashCode();
    }

    @Override
    public void resetName() {
        StringBuilder s = new StringBuilder();
        boolean removed = hasFlag(FlagType.REMOVE);

        s.append("shaped ").append(getWidth()).append('x').append(getHeight());

        s.append(" (");

        if (choiceShape != null) {
            int shapeSize = choiceShape.length;
            for (int i = 0; i < shapeSize; i++) {
                s.append(choiceShape[i]);

                if (i + 1 < shapeSize) {
                    s.append(",");
                }
            }
        }

        for (Entry<Character, List<Material>> entry : ingredientsChoiceMap.entrySet()) {
            s.append(" ").append(entry.getKey()).append(":");

            List<Material> materials = entry.getValue();

            int materialsSize = materials.size();
            for (int i = 0; i < materialsSize; i++) {
                s.append(materials.get(i).toString());

                if (i + 1 < materialsSize) {
                    s.append(",");
                }
            }
        }

        s.append(") ");

        if (removed) {
            s.append("removed recipe");
        } else {
            s.append(getResultsString());
        }

        name = s.toString();
        customName = false;
    }

    /**
     * @return Shape width, 1 to 3
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return Shape height, 1 to 3
     */
    public int getHeight() {
        return height;
    }

    @Override
    public ShapedRecipe toBukkitRecipe(boolean vanilla) {
        if (!hasIngredientChoices() || !hasResults()) {
            return null;
        }

        ShapedRecipe bukkitRecipe;
        if (Version.has1_12Support()) {
            if (vanilla) {
                bukkitRecipe = new ShapedRecipe(getNamespacedKey(), getFirstResult());
            } else {
                bukkitRecipe = new ShapedRecipe(getNamespacedKey(), Tools.createItemRecipeId(getFirstResult(), getIndex()));
            }
        } else {
        if (vanilla) {
            bukkitRecipe = new ShapedRecipe(getFirstResult());
        } else {
            bukkitRecipe = new ShapedRecipe(Tools.createItemRecipeId(getFirstResult(), getIndex()));
        }
    }

        bukkitRecipe.shape(choiceShape);

        for (Entry<Character, List<Material>> entry : ingredientsChoiceMap.entrySet()) {
            bukkitRecipe.setIngredient(entry.getKey(), new RecipeChoice.MaterialChoice(entry.getValue()));
        }

        return bukkitRecipe;
    }

    public boolean hasIngredients() {
        return ingredients != null && ingredients.length == 9;
    }

    public boolean hasIngredientChoices() {
        return !ingredientsChoiceMap.isEmpty();
    }

    @Override
    public boolean isValid() {
        return hasIngredientChoices() && (hasFlag(FlagType.REMOVE) || hasFlag(FlagType.RESTRICT) || hasResults());
    }

    @Override
    public RMCRecipeType getType() {
        return RMCRecipeType.CRAFT;
    }
    /*
    public String printBookIndex() {
        String print;

        if (hasCustomName()) {
            print = RMCChatColor.ITALIC + getName();
        } else {
            ItemResult result = getFirstResult();

            if (result.hasFlag(FlagType.ITEM_NAME)) {
                FlagItemName flag = (FlagItemName)result.getFlag(FlagType.ITEM_NAME);
                print = RMCUtil.parseColors(flag.getItemName(), false);
            } else {
                print = ToolsItem.getName(getFirstResult());
            }
        }

        return print;
    }
    */
    @Override
    public List<String> printBookIndices() {
        List<String> print = new ArrayList<>();

        if (hasFlag(FlagType.INDIVIDUAL_RESULTS)) {
            for (ItemResult result : getResults()) {
                print.add(getResultPrintName(result));
            }
        } else {
            print.add(getResultPrintName(getFirstResult()));
        }

        return print;
    }

    private String getResultPrintName(ItemResult result) {
        String print;

        if (result.hasFlag(FlagType.ITEM_NAME)) {
            FlagItemName flag = (FlagItemName)result.getFlag(FlagType.ITEM_NAME);
            print = RMCUtil.parseColors(flag.getItemName(), false);
        } else {
            print = ToolsItem.getName(getFirstResult());
        }

        return print;
    }

    @Override
    public List<String> printBookRecipes() {
        List<String> recipes = new ArrayList<>();

        if (hasFlag(FlagType.INDIVIDUAL_RESULTS)) {
            for (ItemResult result : getResults()) {
                recipes.add(printBookResult(result));
            }
        } else {
            recipes.add(printBookResult(getFirstResult()));
        }

        return recipes;
    }

    private String printBookResult(ItemResult result) {
        StringBuilder s = new StringBuilder(256);

        s.append(Messages.getInstance().parse("recipebook.header.shaped"));

        if (hasCustomName()) {
            s.append('\n').append(RMCChatColor.BLACK).append(RMCChatColor.ITALIC).append(getName());
        }

        s.append('\n').append(RMCChatColor.GRAY).append('=');

        if (result.hasFlag(FlagType.ITEM_NAME)) {
            FlagItemName flag = (FlagItemName)result.getFlag(FlagType.ITEM_NAME);
            s.append(RMCChatColor.BLACK).append(RMCUtil.parseColors(flag.getItemName(), false));
        } else {
            s.append(ToolsItem.print(getFirstResult(), RMCChatColor.DARK_GREEN, null));
        }

        if (isMultiResult() && !hasFlag(FlagType.INDIVIDUAL_RESULTS)) {
            s.append('\n').append(Messages.getInstance().parse("recipebook.moreresults", "{amount}", (getResults().size() - 1)));
        }

        s.append("\n\n");
        s.append(Messages.getInstance().parse("recipebook.header.shape")).append('\n');
        s.append(RMCChatColor.GRAY);

        Map<String, Integer> charItems = new LinkedHashMap<>();
        int num = 1;

        // If ingredients get mirrored at any point, display them as they were written
        ItemStack[] displayIngredients = ingredients;
        if (isMirrorShape()) {
            displayIngredients = Tools.mirrorItemMatrix(ingredients);
        }

        int ingredientsLength = displayIngredients.length;
        for (int i = 0; i < ingredientsLength; i++) {
            int col = i % 3 + 1;
            int row = i / 3 + 1;

            if (col <= getWidth() && row <= getHeight()) {
                if (displayIngredients[i] == null) {
                    s.append('[').append(RMCChatColor.WHITE).append('_').append(RMCChatColor.GRAY).append(']');
                } else {
                    String print = "";
                    if (result.hasFlag(FlagType.INGREDIENT_CONDITION)) {
                        FlagIngredientCondition flag = (FlagIngredientCondition) result.getFlag(FlagType.INGREDIENT_CONDITION);
                        List<ConditionsIngredient> conditions = flag.getIngredientConditions(displayIngredients[i]);

                        if (conditions.size() > 0) {
                            ConditionsIngredient condition = conditions.get(0);

                            if (condition.hasName()) {
                                print = RMCChatColor.BLACK + condition.getName();
                            } else if (condition.hasLore()) {
                                print = RMCChatColor.BLACK + "" + RMCChatColor.ITALIC + condition.getLores().get(0);
                            }
                        }
                    }

                    if (print.equals("")) {
                        print = ToolsItem.print(displayIngredients[i], RMCChatColor.BLACK, RMCChatColor.BLACK);
                    }

                    Integer get = charItems.get(print);

                    if (get == null) {
                        charItems.put(print, num);
                        get = num;
                        num++;
                    }

                    s.append('[').append(RMCChatColor.DARK_PURPLE).append(get).append(RMCChatColor.GRAY).append(']');
                }
            }

            if (col == getWidth() && row <= getHeight()) {
                s.append('\n');
            }
        }

        s.append('\n').append(Messages.getInstance().parse("recipebook.header.ingredients"));

        for (Entry<String, Integer> entry : charItems.entrySet()) {
            s.append('\n').append(RMCChatColor.DARK_PURPLE).append(entry.getValue()).append(RMCChatColor.GRAY).append(": ").append(entry.getKey());
        }

        return s.toString();
    }
}