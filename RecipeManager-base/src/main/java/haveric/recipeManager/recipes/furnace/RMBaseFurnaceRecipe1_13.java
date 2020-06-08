package haveric.recipeManager.recipes.furnace;

import haveric.recipeManager.RecipeManager;
import haveric.recipeManager.Vanilla;
import haveric.recipeManager.common.RMCChatColor;
import haveric.recipeManager.common.RMCVanilla;
import haveric.recipeManager.common.util.RMCUtil;
import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.flag.Flags;
import haveric.recipeManager.flag.args.ArgBuilder;
import haveric.recipeManager.flag.conditions.ConditionsIngredient;
import haveric.recipeManager.flag.flags.any.FlagIngredientCondition;
import haveric.recipeManager.messages.Messages;
import haveric.recipeManager.recipes.BaseRecipe;
import haveric.recipeManager.recipes.ItemResult;
import haveric.recipeManager.tools.ToolsItem;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.inventory.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RMBaseFurnaceRecipe1_13 extends RMBaseFurnaceRecipe {
    private List<Material> ingredientChoice = new ArrayList<>();
    private ItemResult fuel;
    private float minTime = Vanilla.FURNACE_RECIPE_TIME;
    private float maxTime = -1;

    public RMBaseFurnaceRecipe1_13() {
    }

    public RMBaseFurnaceRecipe1_13(BaseRecipe recipe) {
        super(recipe);

        if (recipe instanceof RMBaseFurnaceRecipe1_13) {
            RMBaseFurnaceRecipe1_13 r = (RMBaseFurnaceRecipe1_13) recipe;

            if (r.ingredientChoice == null) {
                ingredientChoice = null;
            } else {
                ingredientChoice.addAll(r.ingredientChoice);
            }

            if (r.fuel == null) {
                fuel = null;
            } else {
                fuel = r.fuel.clone();
            }

            minTime = r.minTime;
            maxTime = r.maxTime;
            hash = r.hash;
        }
    }

    public RMBaseFurnaceRecipe1_13(Flags flags) {
        super(flags);
    }

    // Legacy constructor for 1.13 / 1.12
    public RMBaseFurnaceRecipe1_13(FurnaceRecipe recipe) {
        setIngredientChoice(recipe.getInputChoice());
        setResult(recipe.getResult());
    }

    // Constructor for 1.14 +
    public RMBaseFurnaceRecipe1_13(CookingRecipe recipe) {
        setIngredientChoice(recipe.getInputChoice());
        setResult(recipe.getResult());
    }

    public String getRecipeBaseHash() {
        return "";
    }

    public ItemStack getIngredient() {
        return null; // TODO: 1.13 doesn't use this, can we remove?
    }

    public void setIngredient(ItemStack newIngredient) {
        // TODO: 1.13 doesn't use this, can we remove?
    }

    public List<Material> getIngredientChoice() {
        return ingredientChoice;
    }

    public void setIngredientChoice(List<Material> materials) {
        RecipeChoice.MaterialChoice materialChoice = new RecipeChoice.MaterialChoice(materials);
        setIngredientChoice(materialChoice);
    }

    private void setIngredientChoice(RecipeChoice choice) {
        if (choice instanceof RecipeChoice.MaterialChoice) {
            ingredientChoice.clear();
            RecipeChoice.MaterialChoice materialChoice = (RecipeChoice.MaterialChoice) choice;

            List<Material> sorted = new ArrayList<>(materialChoice.getChoices());
            Collections.sort(sorted);

            ingredientChoice.addAll(sorted);

            String newHash = getRecipeBaseHash();

            int size = sorted.size();
            for (int i = 0; i < size; i++) {
                newHash += sorted.get(i).toString();

                if (i + 1 < size) {
                    newHash += ", ";
                }
            }

            hash = newHash.hashCode();
        }
    }

    public ItemResult getFuel() {
        return fuel;
    }

    public void setFuel(ItemStack newFuel) {
        Validate.notNull(newFuel);

        if (newFuel instanceof ItemResult) {
            fuel = ((ItemResult) newFuel).setRecipe(this);
        } else {
            fuel = new ItemResult(newFuel).setRecipe(this);
        }
    }

    public boolean hasCustomTime() {
        return minTime != Vanilla.FURNACE_RECIPE_TIME;
    }

    public float getMinTime() {
        return minTime;
    }

    /**
     * @param newMinTime
     *            min random time range (seconds)
     */
    public void setMinTime(float newMinTime) {
        minTime = newMinTime;
    }

    public float getMaxTime() {
        return maxTime;
    }

    /**
     * @param newMaxTime
     *            max random time range (seconds) or set to -1 to disable
     */
    public void setMaxTime(float newMaxTime) {
        maxTime = newMaxTime;
    }

    /**
     * @return if recipe has random time range
     */
    public boolean hasRandomTime() {
        return maxTime > minTime;
    }

    /**
     * @return min time or if hasRandomTime() gets a random between min and max time.
     */
    public float getCookTime() {
        float time;

        if (hasRandomTime()) {
            time = minTime + ((maxTime - minTime) * RecipeManager.random.nextFloat());
        } else {
            time = minTime;
        }

        return time;
    }

    /**
     * @return getCookTime() multiplied by 20.0 and rounded
     */
    public int getCookTicks() {
        return Math.round(getCookTime() * 20.0f);
    }

    @Override
    public void resetName() {
        StringBuilder s = new StringBuilder();
        boolean removed = hasFlag(FlagType.REMOVE);

        s.append(getRecipeBaseHash()).append(" ");

        int size = ingredientChoice.size();
        for (int i = 0; i < size; i++) {
            s.append(ingredientChoice.get(i).toString().toLowerCase());

            if (i + 1 < size) {
                s.append(", ");
            }
        }

        s.append(" to ");
        s.append(getResultString());

        if (removed) {
            s.append(" [removed recipe]");
        }

        name = s.toString();
        customName = false;
    }

    @Override
    public List<String> getIndexes() {
        List<String> indexString = new ArrayList<>();

        for (Material material : ingredientChoice) {
            indexString.add(material.toString());
        }

        return indexString;
    }

    public String getFuelIndex() {
        String fuelIndex = "" + fuel.getType().toString();

        if (fuel.getDurability() != RMCVanilla.DATA_WILDCARD) {
            fuelIndex += ":" + fuel.getDurability();
        }

        return fuelIndex;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    public boolean hasIngredientChoice() {
        return ingredientChoice != null;
    }

    public boolean hasFuel() {
        return fuel != null;
    }

    @Override
    public boolean isValid() {
        return hasIngredientChoice() && (hasFlag(FlagType.REMOVE) || hasFlag(FlagType.RESTRICT) || hasResult());
    }

    @Override
    public String printBookResult(ItemResult result) {
        StringBuilder s = getHeaderResult(getRecipeBaseHash());

        String print = getConditionResultName(result);

        if (print.isEmpty()) {
            print = ToolsItem.printChoice(ingredientChoice, RMCChatColor.RESET, RMCChatColor.BLACK);
        }

        s.append('\n').append(print);

        s.append("\n\n");
        s.append(Messages.getInstance().parse("recipebook.header.cooktime")).append(RMCChatColor.BLACK);
        s.append('\n');

        if (hasCustomTime()) {
            if (maxTime > minTime) {
                s.append(Messages.getInstance().parse("recipebook.smelt.time.random", "{min}", RMCUtil.printNumber(minTime), "{max}", RMCUtil.printNumber(maxTime)));
            } else {
                if (minTime <= 0) {
                    s.append(Messages.getInstance().parse("recipebook.smelt.time.instant"));
                } else {
                    s.append(Messages.getInstance().parse("recipebook.smelt.time.fixed", "{time}", RMCUtil.printNumber(minTime)));
                }
            }
        } else {
            s.append(Messages.getInstance().parse("recipebook.smelt.time.normal", "{time}", RMCUtil.printNumber(minTime)));
        }

        if (hasFuel()) {
            s.append("\n\n");
            s.append(Messages.getInstance().parse("recipebook.header.requirefuel"));
            s.append('\n').append(ToolsItem.print(fuel, RMCChatColor.RESET, RMCChatColor.BLACK));
        }

        return s.toString();
    }

    public void subtractIngredient(FurnaceInventory inv, ItemResult result, boolean onlyExtra) {
        FlagIngredientCondition flagIC;
        if (hasFlag(FlagType.INGREDIENT_CONDITION)) {
            flagIC = (FlagIngredientCondition) getFlag(FlagType.INGREDIENT_CONDITION);
        } else {
            flagIC = null;
        }

        if (flagIC == null && result != null && result.hasFlag(FlagType.INGREDIENT_CONDITION)) {
            flagIC = (FlagIngredientCondition) result.getFlag(FlagType.INGREDIENT_CONDITION);
        }

        ItemStack item = inv.getSmelting();
        if (item != null) {
            int amt = item.getAmount();
            int newAmt = amt;

            if (flagIC != null) {
                List<ConditionsIngredient> condList = flagIC.getIngredientConditions(item);

                for (ConditionsIngredient cond : condList) {
                    if (cond != null && cond.checkIngredient(item, ArgBuilder.create().build())) {
                        if (cond.getAmount() > 1) {
                            newAmt -= (cond.getAmount() - 1);
                        }
                    }
                }
            }

            if (!onlyExtra) {
                newAmt -= 1;
            }

            if (amt != newAmt) {
                if (newAmt > 0) {
                    item.setAmount(newAmt);
                } else {
                    inv.setSmelting(null);
                }
            }
        }
    }

    @Override
    public int findItemInIngredients(Material type, Short data) {
        int found = 0;

        for (Material material : ingredientChoice) {
            if (type == material) {
                found++;
                break;
            }
        }

        return found;
    }

    @Override
    public List<String> getRecipeIndexesForInput(List<ItemStack> ingredients, ItemStack result) {
        List<String> recipeIndexes = new ArrayList<>();
        if (ingredients.size() == 1) {
            recipeIndexes.add(ingredients.get(0).getType().toString());
        }

        return recipeIndexes;
    }
}
