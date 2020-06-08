package haveric.recipeManager.recipes.anvil;

import haveric.recipeManager.ErrorReporter;
import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.recipes.BaseRecipeParser;
import haveric.recipeManager.recipes.ItemResult;
import haveric.recipeManager.tools.Version;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class AnvilRecipeParser extends BaseRecipeParser {
    public AnvilRecipeParser() {
        super();
    }

    @Override
    public boolean parseRecipe(int directiveLine) {
        AnvilRecipe recipe = new AnvilRecipe(fileFlags); // create recipe and copy flags from file

        reader.parseFlags(recipe.getFlags()); // parse recipe's flags

        // get the ingredient
        String[] split = reader.getLine().split("%");
        if (split.length == 0) {
            return ErrorReporter.getInstance().error("Recipe needs an ingredient!");
        }

        // get the ingredients
        String[] ingredientsRaw = split[0].split("\\+");

        List<List<Material>> choicesList = parseIngredients(ingredientsRaw, recipe.getType(), 2, true);
        if (choicesList == null || choicesList.isEmpty()) {
            return false;
        }

        recipe.setPrimaryIngredient(choicesList.get(0));
        if (choicesList.size() > 1) {
            recipe.setSecondaryIngredient(choicesList.get(1));
        }

        parseArgs(recipe, split);

        List<ItemResult> results = new ArrayList<>();
        boolean hasResults = parseResults(recipe, results);

        if (!hasResults) {
            return false;
        }

        recipe.setResults(results);

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

    private void parseArgs(BaseAnvilRecipe recipe, String[] split) {
        if (split.length > 1) {
            String repairString = split[1].trim();

            // Skip if empty
            if (!repairString.isEmpty()) {
                try {
                    int repairCost = Integer.parseInt(repairString);

                    recipe.setRepairCost(repairCost);

                    if (!Version.has1_11Support()) {
                        ErrorReporter.getInstance().warning("Repair Cost is only supported in 1.11 or newer.");
                    }
                } catch (NumberFormatException e) {
                    ErrorReporter.getInstance().error("Recipe has invalid repair cost: " + split[1] + ". Defaulting to 0.");
                }
            }
        }

        if (split.length > 2) {
            String renameText = split[2].trim().toLowerCase();

            // Skip if empty
            if (!renameText.isEmpty()) {
                if (renameText.equals("allowrename") || renameText.equals("true")) {
                    recipe.setRenamingAllowed(true);
                } else if (renameText.equals("false")) {
                    recipe.setRenamingAllowed(false);
                } else {
                    ErrorReporter.getInstance().warning("Invalid rename attribute: " + renameText + ". Defaulting to false. Accepted values: allowrename, true, false.");
                }
            }
        }

        if (split.length > 3) {
            try {
                double anvilDamageChance = Double.parseDouble(split[3].trim());

                if (anvilDamageChance < 0) {
                    ErrorReporter.getInstance().warning("Anvil damage chance cannot be below 0: " + split[3] + ". Allowed values from 0-300 (decimal values allowed). Defaulting to 0.");
                    anvilDamageChance = 0;
                } else if (anvilDamageChance > 300) {
                    ErrorReporter.getInstance().warning("Anvil damage chance cannot be above 300: " + split[3] + ". Allowed values from 0-300 (decimal values allowed). Defaulting to 300.");
                    anvilDamageChance = 300;
                }

                recipe.setAnvilDamageChance(anvilDamageChance);
            } catch (NumberFormatException e) {
                ErrorReporter.getInstance().error("Invalid anvil damage chance: " + split[3] + ". Allowed values from 0-300 (decimal values allowed). Defaulting to 12.");
            }
        }
    }
}
