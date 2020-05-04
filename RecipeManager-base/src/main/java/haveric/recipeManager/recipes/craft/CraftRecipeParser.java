package haveric.recipeManager.recipes.craft;

import haveric.recipeManager.ErrorReporter;
import haveric.recipeManager.Files;
import haveric.recipeManager.common.RMCVanilla;
import haveric.recipeManager.common.util.ParseBit;
import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.flag.Flags;
import haveric.recipeManager.flag.args.ArgBuilder;
import haveric.recipeManager.flag.args.Args;
import haveric.recipeManager.recipes.BaseRecipeParser;
import haveric.recipeManager.recipes.ItemResult;
import haveric.recipeManager.tools.Tools;
import haveric.recipeManager.tools.ToolsItem;
import haveric.recipeManager.tools.Version;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CraftRecipeParser extends BaseRecipeParser {
    public CraftRecipeParser() {
        super();
    }

    @Override
    public boolean parseRecipe(int directiveLine) {
        BaseCraftRecipe recipe;
        if (Version.has1_13Support()) {
            recipe = new CraftRecipe1_13(fileFlags);
        } else {
            recipe = new CraftRecipe(fileFlags); // create recipe and copy flags from file
        }

        reader.parseFlags(recipe.getFlags()); // parse recipe's flags

        List<String> choiceShapeString = new ArrayList<>();

        String shapeFormatLine = reader.getLine().toLowerCase();
        if (shapeFormatLine.startsWith("shape")) {
            if (recipe instanceof CraftRecipe1_13) {
                Map<Character, Integer> ingredientCharacters = new HashMap<>();
                Map<Character, RecipeChoice> ingredientRecipeChoiceMap = new HashMap<>();
                String[] shapeLines = shapeFormatLine.substring("shape".length()).split("\\|", 3);

                for (String shapeLine : shapeLines) {
                    shapeLine = shapeLine.trim();

                    if (shapeLine.length() > 3) {
                        ErrorReporter.getInstance().warning("Shape row has more than 3 characters: " + shapeLine + ". Using only the first three: " + shapeLine.substring(0, 3));
                        shapeLine = shapeLine.substring(0, 3);
                    }
                    choiceShapeString.add(shapeLine);

                    for (char c : shapeLine.toCharArray()) {
                        if (!ingredientCharacters.containsKey(c)) {
                            ingredientCharacters.put(c, 1);
                        } else {
                            ingredientCharacters.put(c, ingredientCharacters.get(c) + 1);
                        }
                    }
                }

                reader.nextLine();

                int ingredientsNum = 0;
                while (!reader.lineIsResult()) {
                    String line = reader.getLine();
                    char ingredientChar = line.substring(0, 2).trim().charAt(0);

                    if (ingredientCharacters.containsKey(ingredientChar)) {
                        List<Material> choices = Tools.parseChoice(line.substring(2), ParseBit.NONE);
                        if (choices == null || choices.isEmpty()) {
                            return false;
                        }

                        ingredientsNum += ingredientCharacters.get(ingredientChar);

                        Flags ingredientFlags = new Flags();
                        reader.parseFlags(ingredientFlags);

                        if (ingredientFlags.hasFlags()) {
                            List<ItemStack> items = new ArrayList<>();
                            for (Material choice : choices) {
                                Args a = ArgBuilder.create().result(new ItemStack(choice)).build();
                                ingredientFlags.sendCrafted(a, true);

                                items.add(a.result());
                            }

                            if (!ingredientRecipeChoiceMap.containsKey(ingredientChar)) {
                                ingredientRecipeChoiceMap.put(ingredientChar, new RecipeChoice.ExactChoice(items));
                            } else {
                                ingredientRecipeChoiceMap.put(ingredientChar, ToolsItem.mergeRecipeChoiceWithItems(ingredientRecipeChoiceMap.get(ingredientChar), items));
                            }
                        } else {
                            if (!ingredientRecipeChoiceMap.containsKey(ingredientChar)) {
                                ingredientRecipeChoiceMap.put(ingredientChar, new RecipeChoice.MaterialChoice(choices));
                            } else {
                                ingredientRecipeChoiceMap.put(ingredientChar, ToolsItem.mergeRecipeChoiceWithMaterials(ingredientRecipeChoiceMap.get(ingredientChar), choices));
                            }
                        }
                    } else {
                        ErrorReporter.getInstance().warning("Character " + ingredientChar + " not found in shape.");
                    }
                }

                if (ingredientsNum == 0) { // no ingredients were processed
                    return ErrorReporter.getInstance().error("Recipe doesn't have ingredients!", "Consult '" + Files.FILE_INFO_BASICS + "' for proper recipe syntax.");
                } else if (ingredientsNum == 2) {
                    if (!conditionEvaluator.checkRecipeChoices(ingredientRecipeChoiceMap)) {
                        return false;
                    }
                }

                // Add extra air to fill rectangle
                if (choiceShapeString.size() > 1) {
                    int min = choiceShapeString.get(0).length();
                    int max = min;

                    for (int i = 1; i < choiceShapeString.size(); i++) {
                        String characters = choiceShapeString.get(i);
                        max = Math.max(characters.length(), max);
                        min = Math.min(characters.length(), min);
                    }

                    char availableChar = '0';
                    for (char letter = 'a'; letter < 'z'; letter ++) {
                        if (!ingredientRecipeChoiceMap.containsKey(letter)) {
                            availableChar = letter;
                            break;
                        }
                    }

                    if (min < max) {
                        for (int i = 0; i < choiceShapeString.size(); i++) {
                            String shape = choiceShapeString.get(i);
                            for (int j = shape.length(); j < max; j++) {
                                shape += availableChar;
                            }
                            choiceShapeString.set(i, shape);
                        }

                        ingredientRecipeChoiceMap.put(availableChar, null);
                    }
                }

                ((CraftRecipe1_13) recipe).setChoiceShape(choiceShapeString.toArray(new String[0]));
                ((CraftRecipe1_13) recipe).setIngredientsRecipeChoiceMap(ingredientRecipeChoiceMap);
            } else {
                return ErrorReporter.getInstance().error("Shape is only supported on 1.13 or newer servers.");
            }
        } else {
            Map<Character, List<Material>> ingredientsChoiceMap = new HashMap<>();
            char characterKey = 'a';

            ItemStack[] ingredients = new ItemStack[9];
            String[] split;

            int rows = 0;
            int ingredientsNum = 0;
            boolean ingredientErrors = false;

            while (rows < 3) { // loop until we find 3 rows of ingredients (or bump into the result along the way)
                if (rows > 0) {
                    reader.nextLine();
                }

                if (reader.getLine() == null) {
                    if (rows == 0) {
                        return ErrorReporter.getInstance().error("No ingredients defined!");
                    }

                    break;
                }

                if (reader.lineIsResult()) { // if we bump into the result prematurely (smaller recipes)
                    break;
                }

                split = reader.getLine().split("\\+"); // split ingredients by the + sign
                int rowLen = split.length;

                if (rowLen > 3) { // if we find more than 3 ingredients warn the user and limit it to 3
                    rowLen = 3;
                    ErrorReporter.getInstance().warning("You can't have more than 3 ingredients on a row, ingredient(s) ignored.", "Remove the extra ingredient(s).");
                }

                for (int i = 0; i < rowLen; i++) { // go through each ingredient on the line
                    if (Version.has1_13Support()) {
                        List<Material> choices = Tools.parseChoice(split[i], ParseBit.NONE);

                        if (choices == null || choices.isEmpty()) { // No items found
                            ingredientErrors = true;
                        }

                        if (!ingredientErrors) {
                            if (choiceShapeString.size() == rows) {
                                choiceShapeString.add("" + characterKey);
                            } else {
                                choiceShapeString.set(rows, choiceShapeString.get(rows) + characterKey);
                            }

                            ingredientsChoiceMap.put(characterKey, choices);

                            characterKey++;
                            ingredientsNum++;
                        }
                    } else {
                        ItemStack item = Tools.parseItem(split[i], RMCVanilla.DATA_WILDCARD, ParseBit.NO_AMOUNT | ParseBit.NO_META);
                        if (item == null) { // invalid item
                            ingredientErrors = true;
                        }

                        // no point in adding more ingredients if there are errors
                        if (!ingredientErrors) {
                            // Minecraft 1.11 required air ingredients to include a data value of 0
                            if ((Version.has1_11Support() && !Version.has1_12Support()) || item.getType() != Material.AIR) {
                                ingredients[(rows * 3) + i] = item;
                                ingredientsNum++;
                            }
                        }
                    }
                }

                rows++;
            }

            if (ingredientErrors) { // invalid ingredients found
                return ErrorReporter.getInstance().error("Recipe has some invalid ingredients, fix them!");
            } else if (ingredientsNum == 0) { // no ingredients were processed
                return ErrorReporter.getInstance().error("Recipe doesn't have ingredients!", "Consult '" + Files.FILE_INFO_BASICS + "' for proper recipe syntax.");
            } else if (ingredientsNum == 2) {
                if (Version.has1_13Support()) {
                    if (!conditionEvaluator.checkMaterialChoices(ingredientsChoiceMap)) {
                        return false;
                    }
                } else {
                    if (!conditionEvaluator.checkIngredients(ingredients)) {
                        return false;
                    }
                }
            }

            // done with ingredients, set 'em
            if (recipe instanceof CraftRecipe1_13) {

                // Add extra air to fill rectangle
                if (choiceShapeString.size() > 1) {
                    int min = choiceShapeString.get(0).length();
                    int max = min;

                    for (int i = 1; i < choiceShapeString.size(); i++) {
                        String characters = choiceShapeString.get(i);
                        max = Math.max(characters.length(), max);
                        min = Math.min(characters.length(), min);
                    }

                    if (min < max) {
                        for (int i = 0; i < choiceShapeString.size(); i++) {
                            String shape = choiceShapeString.get(i);
                            for (int j = shape.length(); j < max; j++) {
                                shape += characterKey;
                            }
                            choiceShapeString.set(i, shape);
                        }

                        List<Material> airList = new ArrayList<>();
                        airList.add(Material.AIR);

                        ingredientsChoiceMap.put(characterKey, airList);
                    }
                }

                ((CraftRecipe1_13) recipe).setChoiceShape(choiceShapeString.toArray(new String[0]));
                ((CraftRecipe1_13) recipe).setIngredientsChoiceMap(ingredientsChoiceMap);

            } else {
                ((CraftRecipe) recipe).setIngredients(ingredients);
            }
        }

        if (recipe.hasFlag(FlagType.REMOVE) && !Version.has1_12Support()) { // for mc1.12, matching requires outcome too...
            reader.nextLine(); // Skip the results line, if it exists
        } else {
            // get results
            List<ItemResult> results = new ArrayList<>();

            if (!parseResults(recipe, results)) { // results have errors
                return false;
            }

            if (recipe instanceof CraftRecipe1_13) {
                CraftRecipe1_13 craftRecipe = (CraftRecipe1_13) recipe;
                craftRecipe.setResults(results); // done with results, set 'em

                if (!craftRecipe.hasValidResult()) {
                    return ErrorReporter.getInstance().error("Recipe must have at least one non-air result!");
                }
            } else {
                CraftRecipe craftRecipe = (CraftRecipe) recipe;
                craftRecipe.setResults(results); // done with results, set 'em

                if (!craftRecipe.hasValidResult()) {
                    return ErrorReporter.getInstance().error("Recipe must have at least one non-air result!");
                }
            }
        }

        // check if the recipe already exists...
        if (!conditionEvaluator.recipeExists(recipe, directiveLine, reader.getFileName())) {
            return recipe.hasFlag(FlagType.REMOVE);
        }

        if (recipeName != null && !recipeName.isEmpty()) {
            recipe.setName(recipeName); // set recipe's name if defined
        }

        // add the recipe to the Recipes class and to the list for later adding to the server
        recipeRegistrator.queueRecipe(recipe, reader.getFileName());
        
        return true; // successfully added
    }


}
