package haveric.recipeManager.flags;

import haveric.recipeManager.*;
import haveric.recipeManager.recipes.BaseRecipe;
import haveric.recipeManager.recipes.CraftRecipe;
import haveric.recipeManager.recipes.ItemResult;
import haveric.recipeManagerCommon.recipes.RMCRecipeInfo;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.ItemMeta;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class FlagEnchantItemTest extends FlagBaseTest {

    @Before
    public void setup() {
        Enchantment.registerEnchantment(new TestEnchantmentSharpness(16));
        Enchantment.registerEnchantment(new TestEnchantmentOxygen(5));
        Enchantment.registerEnchantment(new TestEnchantmentDigSpeed(32));
        Enchantment.registerEnchantment(new TestEnchantmentInfinity(51));
        when(settings.getEnchantment("sharpness")).thenReturn(Enchantment.DAMAGE_ALL);
        when(settings.getEnchantment("oxygen")).thenReturn(Enchantment.OXYGEN);
        when(settings.getEnchantment("digspeed")).thenReturn(Enchantment.DIG_SPEED);
        when(settings.getEnchantment("arrowinfinite")).thenReturn(Enchantment.ARROW_INFINITE);
    }
    @Test
    public void onRecipeParse() {
        File file = new File("src/test/resources/recipes/flagEnchantItem/");
        RecipeProcessor.reload(null, true, file.getPath(), workDir.getPath());

        Map<BaseRecipe, RMCRecipeInfo> queued = RecipeProcessor.getRegistrator().getQueuedRecipes();

        assertEquals(5, queued.size());

        for (Map.Entry<BaseRecipe, RMCRecipeInfo> entry : queued.entrySet()) {
            CraftRecipe recipe = (CraftRecipe) entry.getKey();

            Args a = ArgBuilder.create().recipe(recipe).build();
            a.setPlayerUUID(testUUID);

            ItemResult result = recipe.getResult(a);

            FlagEnchantItem flag = (FlagEnchantItem) result.getFlag(FlagType.ENCHANT_ITEM);

            flag.onPrepare(a);

            Material resultType = result.getType();
            ItemMeta meta = result.getItemMeta();
            if (resultType == Material.DIRT) {
                assertEquals(1, meta.getEnchants().size());
                assertTrue(meta.getEnchants().containsKey(Enchantment.OXYGEN));
                assertEquals(1, meta.getEnchants().get(Enchantment.OXYGEN).intValue());
            } else if (resultType == Material.STONE_SWORD) {
                assertEquals(1, meta.getEnchants().size());
                assertTrue(meta.getEnchants().containsKey(Enchantment.DIG_SPEED));
                assertEquals(3, meta.getEnchants().get(Enchantment.DIG_SPEED).intValue());
            } else if (resultType == Material.GOLD_SWORD) {
                assertEquals(1, meta.getEnchants().size());
                assertTrue(meta.getEnchants().containsKey(Enchantment.ARROW_INFINITE));
                assertEquals(127, meta.getEnchants().get(Enchantment.ARROW_INFINITE).intValue());
            } else if (resultType == Material.IRON_SWORD) {
                assertEquals(0, meta.getEnchants().size());
            } else if (resultType == Material.DIAMOND_SWORD) {
                assertEquals(2, meta.getEnchants().size());
                assertTrue(meta.getEnchants().containsKey(Enchantment.DIG_SPEED));
                assertTrue(meta.getEnchants().containsKey(Enchantment.DAMAGE_ALL));
                assertEquals(1, meta.getEnchants().get(Enchantment.DIG_SPEED).intValue());
                assertEquals(1, meta.getEnchants().get(Enchantment.DAMAGE_ALL).intValue());
            }
        }
    }
}
