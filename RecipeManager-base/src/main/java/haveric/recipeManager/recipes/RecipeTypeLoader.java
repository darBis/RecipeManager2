package haveric.recipeManager.recipes;

import haveric.recipeManager.common.recipes.RMCRecipeType;
import haveric.recipeManager.messages.MessageSender;
import haveric.recipeManager.recipes.anvil.AnvilEvents;
import haveric.recipeManager.recipes.anvil.AnvilRecipe;
import haveric.recipeManager.recipes.anvil.AnvilRecipeParser;
import haveric.recipeManager.recipes.brew.BrewEvents;
import haveric.recipeManager.recipes.brew.BrewRecipe;
import haveric.recipeManager.recipes.brew.BrewRecipeParser;
import haveric.recipeManager.recipes.campfire.RMCampfireEvents;
import haveric.recipeManager.recipes.campfire.RMCampfireRecipe;
import haveric.recipeManager.recipes.campfire.RMCampfireRecipeParser;
import haveric.recipeManager.recipes.cartography.CartographyEvents;
import haveric.recipeManager.recipes.cartography.CartographyRecipe;
import haveric.recipeManager.recipes.cartography.CartographyRecipeParser;
import haveric.recipeManager.recipes.combine.CombineRecipe;
import haveric.recipeManager.recipes.combine.CombineRecipe1_13;
import haveric.recipeManager.recipes.combine.CombineRecipeParser;
import haveric.recipeManager.recipes.compost.CompostEvents;
import haveric.recipeManager.recipes.compost.CompostRecipe;
import haveric.recipeManager.recipes.compost.CompostRecipeParser;
import haveric.recipeManager.recipes.craft.CraftRecipe;
import haveric.recipeManager.recipes.craft.CraftRecipe1_13;
import haveric.recipeManager.recipes.craft.CraftRecipeParser;
import haveric.recipeManager.recipes.fuel.FuelRecipe;
import haveric.recipeManager.recipes.fuel.FuelRecipeParser;
import haveric.recipeManager.recipes.furnace.*;
import haveric.recipeManager.recipes.grindstone.GrindstoneEvents;
import haveric.recipeManager.recipes.grindstone.GrindstoneRecipe;
import haveric.recipeManager.recipes.grindstone.GrindstoneRecipeParser;
import haveric.recipeManager.recipes.stonecutting.RMStonecuttingRecipe;
import haveric.recipeManager.recipes.stonecutting.RMStonecuttingRecipeParser;
import haveric.recipeManager.tools.Version;
import org.bukkit.ChatColor;

public class RecipeTypeLoader {
    public RecipeTypeLoader() {
        loadDefaultRecipeTypes();
    }

    private void loadDefaultRecipeTypes() {
        if (Version.has1_9Support()) {
            loadRecipeType(RMCRecipeType.ANVIL.getDirective(), new AnvilRecipe(), new AnvilRecipeParser(), new AnvilEvents());
        }

        loadRecipeType(RMCRecipeType.BREW.getDirective(), new BrewRecipe(), new BrewRecipeParser(), new BrewEvents());
        loadRecipeType(RMCRecipeType.FUEL.getDirective(), new FuelRecipe(), new FuelRecipeParser());

        loadRecipeType(RMCRecipeType.SPECIAL.getDirective(), new RemoveResultRecipe(), new RemoveResultsParser());

        if (Version.has1_13Support()) {
            loadRecipeType(RMCRecipeType.COMBINE.getDirective(), new CombineRecipe1_13(), new CombineRecipeParser(), new WorkbenchEvents());
            loadRecipeType(RMCRecipeType.CRAFT.getDirective(), new CraftRecipe1_13(), new CraftRecipeParser(), new WorkbenchEvents());
            loadRecipeType(RMCRecipeType.SMELT.getDirective(), new RMFurnaceRecipe1_13(), new RMBaseFurnaceRecipeParser(RMCRecipeType.SMELT), new RMBaseFurnaceEvents());
        } else {
            loadRecipeType(RMCRecipeType.COMBINE.getDirective(), new CombineRecipe(), new CombineRecipeParser(), new WorkbenchEvents());
            loadRecipeType(RMCRecipeType.CRAFT.getDirective(), new CraftRecipe(), new CraftRecipeParser(), new WorkbenchEvents());
            loadRecipeType(RMCRecipeType.SMELT.getDirective(), new RMFurnaceRecipe(), new RMBaseFurnaceRecipeParser(RMCRecipeType.SMELT), new RMBaseFurnaceEvents());
        }

        if (Version.has1_14Support()) {
            loadRecipeType(RMCRecipeType.BLASTING.getDirective(), new RMBlastingRecipe(), new RMBaseFurnaceRecipeParser(RMCRecipeType.BLASTING), new RMBaseFurnaceEvents());
            loadRecipeType(RMCRecipeType.CAMPFIRE.getDirective(), new RMCampfireRecipe(), new RMCampfireRecipeParser(), new RMCampfireEvents());
            loadRecipeType(RMCRecipeType.CARTOGRAPHY.getDirective(), new CartographyRecipe(), new CartographyRecipeParser(), new CartographyEvents());
            loadRecipeType(RMCRecipeType.GRINDSTONE.getDirective(), new GrindstoneRecipe(), new GrindstoneRecipeParser(), new GrindstoneEvents());
            loadRecipeType(RMCRecipeType.COMPOST.getDirective(), new CompostRecipe(), new CompostRecipeParser(), new CompostEvents());
            loadRecipeType(RMCRecipeType.SMOKING.getDirective(), new RMSmokingRecipe(), new RMBaseFurnaceRecipeParser(RMCRecipeType.SMOKING), new RMBaseFurnaceEvents());
            loadRecipeType(RMCRecipeType.STONECUTTING.getDirective(), new RMStonecuttingRecipe(), new RMStonecuttingRecipeParser());
        }
    }

    public void loadRecipeType(String recipeTypeName, BaseRecipe recipe, BaseRecipeParser parser) {
        loadRecipeType(recipeTypeName, recipe, parser, null);
    }

    public void loadRecipeType(String recipeTypeName, BaseRecipe recipe, BaseRecipeParser parser, BaseRecipeEvents events) {
        if (RecipeTypeFactory.getInstance().isInitialized()) {
            MessageSender.getInstance().info(ChatColor.RED + "Custom recipe types must be added in your onEnable() method.");
        } else {
            RecipeTypeFactory.getInstance().initializeRecipeType(recipeTypeName, recipe, parser, events);
        }
    }
}
