package haveric.recipeManager.recipes.campfire;

import haveric.recipeManager.ErrorReporter;
import haveric.recipeManager.Vanilla;
import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.flag.Flags;
import haveric.recipeManager.flag.args.ArgBuilder;
import haveric.recipeManager.flag.args.Args;
import haveric.recipeManager.recipes.BaseRecipeParser;
import haveric.recipeManager.recipes.ItemResult;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class RMCampfireRecipeParser extends BaseRecipeParser {
    public RMCampfireRecipeParser() {
        super();
    }

    @Override
    public boolean parseRecipe(int directiveLine) {
        RMCampfireRecipe recipe = new RMCampfireRecipe(fileFlags); // create recipe and copy flags from file
        reader.parseFlags(recipe.getFlags()); // check for @flags

        // get the ingredient and cooking time
        String[] split = reader.getLine().split("%");

        while (!reader.lineIsResult()) {
            String[] splitIngredient = reader.getLine().split("%");

            List<Material> choices = parseIngredient(splitIngredient, recipe.getType());
            if (choices == null || choices.isEmpty()) {
                return false;
            }

            Flags ingredientFlags = new Flags();
            reader.parseFlags(ingredientFlags);

            if (ingredientFlags.hasFlags()) {
                List<ItemStack> items = new ArrayList<>();
                for (Material choice : choices) {
                    Args a = ArgBuilder.create().result(new ItemStack(choice)).build();
                    ingredientFlags.sendCrafted(a, true);

                    items.add(a.result());
                }
                recipe.addIngredientChoiceItems(items);
            } else {
                recipe.addIngredientChoice(choices);
            }
        }

        boolean isRemove = recipe.hasFlag(FlagType.REMOVE);

        if (!isRemove) { // if it's got @remove we don't care about cook time
            float minTime = Vanilla.CAMPFIRE_RECIPE_TIME;
            float maxTime = -1;

            if (split.length >= 2) {
                String[] timeSplit = split[1].trim().toLowerCase().split("-");

                if (timeSplit[0].equals("instant")) {
                    minTime = 0;
                } else {
                    try {
                        minTime = Float.parseFloat(timeSplit[0]);

                        if (timeSplit.length >= 2) {
                            maxTime = Float.parseFloat(timeSplit[1]);
                        }
                    } catch (NumberFormatException e) {
                        ErrorReporter.getInstance().warning("Invalid burn time float number! Campfire time left as default.");

                        minTime = Vanilla.CAMPFIRE_RECIPE_TIME;
                        maxTime = -1;
                    }
                }

                if (maxTime > -1.0 && minTime >= maxTime) {
                    return ErrorReporter.getInstance().error("Campfire recipe has the min-time less or equal to max-time!", "Use a single number if you want a fixed value.");
                }
            }

            recipe.setMinTime(minTime);
            recipe.setMaxTime(maxTime);

            reader.nextLine();
        }

        // get result or move current line after them if we got @remove and results
        List<ItemResult> results = new ArrayList<>();

        if (isRemove) { // ignore result errors if we have @remove
            ErrorReporter.getInstance().setIgnoreErrors(true);
        }

        boolean hasResults = parseResults(recipe, results);

        if (!hasResults) {
            return false;
        }

        ItemResult result = results.get(0);

        recipe.setResult(result);

        if (isRemove) { // un-ignore result errors
            ErrorReporter.getInstance().setIgnoreErrors(false);
        }

        // check if the recipe already exists
        if (!conditionEvaluator.recipeExists(recipe, directiveLine, reader.getFileName())) {
            return recipe.hasFlag(FlagType.REMOVE);
        }

        if (recipeName != null && !recipeName.isEmpty()) {
            recipe.setName(recipeName); // set recipe's name if defined
        }

        // add the recipe to the Recipes class and to the list for later adding to the server
        recipeRegistrator.queueRecipe(recipe, reader.getFileName());


        return true;
    }
}
