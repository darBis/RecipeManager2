package haveric.recipeManager.recipes.stonecutting;

import haveric.recipeManager.ErrorReporter;
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

public class RMStonecuttingRecipeParser extends BaseRecipeParser {
    public RMStonecuttingRecipeParser() {
        super();
    }

    @Override
    public boolean parseRecipe(int directiveLine) {
        RMStonecuttingRecipe recipe = new RMStonecuttingRecipe(fileFlags); // create recipe and copy flags from file
        reader.parseFlags(recipe.getFlags()); // check for @flags

        while (!reader.lineIsResult()) {
            // get the ingredient
            String line = reader.getLine();

            List<Material> choices = parseIngredient(new String[]{line}, recipe.getType());
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

        if (recipe.hasFlag(FlagType.OVERRIDE)) {
            return ErrorReporter.getInstance().error("Recipe does not allow Overriding. Try removing the original and adding a new one.");
        }

        boolean isRemove = recipe.hasFlag(FlagType.REMOVE);

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
