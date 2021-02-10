package haveric.recipeManager.flag;

import haveric.recipeManager.RecipeProcessor;
import haveric.recipeManager.TestMetaBanner;
import haveric.recipeManager.common.recipes.RMCRecipeInfo;
import haveric.recipeManager.flag.args.ArgBuilder;
import haveric.recipeManager.flag.args.Args;
import haveric.recipeManager.flag.flags.result.FlagBannerItem;
import haveric.recipeManager.recipes.BaseRecipe;
import haveric.recipeManager.recipes.ItemResult;
import haveric.recipeManager.recipes.craft.CraftRecipe1_13;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.banner.PatternType;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

public class FlagBannerItemTest extends FlagBaseTest {

    @Test
    public void onRecipeParse() {
        File file = new File(baseRecipePath + "flagBannerItem/");
        reloadRecipeProcessor(true, file);

        Map<BaseRecipe, RMCRecipeInfo> queued = RecipeProcessor.getRegistrator().getQueuedRecipes();

        assertEquals(5, queued.size());

        for (Map.Entry<BaseRecipe, RMCRecipeInfo> entry : queued.entrySet()) {
            CraftRecipe1_13 recipe = (CraftRecipe1_13) entry.getKey();

            try (MockedStatic<Bukkit> mockedBukkit = mockStatic(Bukkit.class)) {
                mockedBukkit.when(Bukkit::getItemFactory).thenReturn(itemFactory);

                ItemResult result = recipe.getFirstResult();

                Args a = ArgBuilder.create().recipe(recipe).result(result).player(testUUID).build();

                FlagBannerItem flag = (FlagBannerItem) result.getFlag(FlagType.BANNER_ITEM);
                flag.onPrepare(a);

                Material resultType = result.getType();
                if (resultType == Material.DIRT) {
                    assertTrue(a.hasReasons());
                } else {
                    TestMetaBanner meta = (TestMetaBanner) result.getItemMeta();
                    String name = recipe.getName();
                    assertFalse(a.hasReasons());

                    switch (name) {
                        case "base":
                            assertEquals(DyeColor.BLACK, meta.getBaseColor());
                            assertTrue(meta.getPatterns().isEmpty());
                            break;
                        case "one":
                            assertEquals(DyeColor.RED, meta.getBaseColor());
                            assertEquals(1, meta.numberOfPatterns());
                            assertEquals(PatternType.CIRCLE_MIDDLE, meta.getPattern(0).getPattern());
                            assertEquals(DyeColor.BLUE, meta.getPattern(0).getColor());
                            break;
                        case "two":
                            assertEquals(DyeColor.RED, meta.getBaseColor());
                            assertEquals(2, meta.numberOfPatterns());
                            assertEquals(PatternType.CIRCLE_MIDDLE, meta.getPattern(0).getPattern());
                            assertEquals(DyeColor.BLUE, meta.getPattern(0).getColor());
                            assertEquals(PatternType.SKULL, meta.getPattern(1).getPattern());
                            assertEquals(DyeColor.YELLOW, meta.getPattern(1).getColor());
                            break;
                        case "override":
                            assertEquals(DyeColor.GREEN, meta.getBaseColor());
                            assertEquals(2, meta.numberOfPatterns());
                            assertEquals(PatternType.HALF_HORIZONTAL, meta.getPattern(0).getPattern());
                            assertEquals(DyeColor.YELLOW, meta.getPattern(0).getColor());
                            assertEquals(PatternType.CIRCLE_MIDDLE, meta.getPattern(1).getPattern());
                            assertEquals(DyeColor.ORANGE, meta.getPattern(1).getColor());
                            break;
                    }
                }
            }
        }
    }
}
