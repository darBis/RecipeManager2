package haveric.recipeManager.recipes.anvil;

import haveric.recipeManager.RecipeManager;
import haveric.recipeManager.Recipes;
import haveric.recipeManager.Settings;
import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.flag.args.Args;
import haveric.recipeManager.flag.flags.any.FlagItemName;
import haveric.recipeManager.flag.flags.any.FlagModLevel;
import haveric.recipeManager.flag.flags.any.FlagNeedLevel;
import haveric.recipeManager.messages.Messages;
import haveric.recipeManager.messages.SoundNotifier;
import haveric.recipeManager.recipes.ItemResult;
import haveric.recipeManager.recipes.anvil.data.Anvil;
import haveric.recipeManager.recipes.anvil.data.Anvils;
import haveric.recipeManager.tools.Tools;
import haveric.recipeManager.tools.ToolsItem;
import haveric.recipeManager.tools.Version;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AnvilEvents implements Listener {
    public AnvilEvents() { }

    public void clean() {
        HandlerList.unregisterAll(this);
    }

    public static void reload() {
        HandlerList.unregisterAll(RecipeManager.getAnvilEvents());
        Bukkit.getPluginManager().registerEvents(RecipeManager.getAnvilEvents(), RecipeManager.getPlugin());
    }

    @EventHandler
    public void prepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();

        ItemStack left = inventory.getItem(0);
        ItemStack right = inventory.getItem(1);

        AnvilRecipe recipe = Recipes.getInstance().getAnvilRecipe(left, right);
        if (recipe == null) {
            InventoryView view = event.getView();
            Player player = (Player) view.getPlayer();

            String renameText = "";

            // 1.10 didn't support repair cost or rename text
            if (Version.has1_11Support()) {
                renameText = inventory.getRenameText();
            }

            if (left != null && renameText != null && !renameText.isEmpty()) {
                List<Material> renamingMaterials = Settings.getInstance().getAnvilRenaming();

                if (Settings.getInstance().getSpecialAnvilRenaming()) {
                    if (!renamingMaterials.isEmpty() && !renamingMaterials.contains(left.getType())) {
                        event.setResult(new ItemStack(Material.AIR));
                        player.updateInventory();
                    }
                } else if (renamingMaterials.isEmpty() || renamingMaterials.contains(left.getType())) {
                    event.setResult(new ItemStack(Material.AIR));
                    player.updateInventory();
                }
            }

            if (left != null && right != null) {
                if (left.getType().getMaxDurability() > 0) {
                    if (right.getItemMeta() instanceof BookMeta) {
                        List<Material> enchantMaterials = Settings.getInstance().getAnvilMaterialEnchant();
                        Map<Enchantment, List<Integer>> enchantEnchantments = Settings.getInstance().getAnvilEnchantments();

                        if (Settings.getInstance().getSpecialAnvilEnchant()) {
                            if (!enchantMaterials.isEmpty() && !enchantMaterials.contains(left.getType())) {
                                event.setResult(new ItemStack(Material.AIR));
                                player.updateInventory();
                            } else if (!enchantEnchantments.isEmpty()) {
                                Map<Enchantment, Integer> bookEnchantments = right.getEnchantments();

                                boolean enchantAllowed = false;
                                for (Map.Entry<Enchantment, Integer> entry : bookEnchantments.entrySet()) {
                                    List<Integer> levels = enchantEnchantments.get(entry.getKey());

                                    if (levels != null && levels.contains(entry.getValue())) {
                                        enchantAllowed = true;
                                        break;
                                    }
                                }

                                if (!enchantAllowed) {
                                    event.setResult(new ItemStack(Material.AIR));
                                    player.updateInventory();
                                }
                            }
                        } else {
                            if (enchantMaterials.isEmpty() || enchantMaterials.contains(left.getType())) {
                                event.setResult(new ItemStack(Material.AIR));
                                player.updateInventory();
                            } else {
                                boolean enchantNotAllowed = true;

                                if (!enchantEnchantments.isEmpty()) {
                                    Map<Enchantment, Integer> bookEnchantments = right.getEnchantments();

                                    for (Map.Entry<Enchantment, Integer> entry : bookEnchantments.entrySet()) {
                                        List<Integer> levels = enchantEnchantments.get(entry.getKey());

                                        if (levels != null && levels.contains(entry.getValue())) {
                                            enchantNotAllowed = false;
                                            break;
                                        }
                                    }
                                }

                                if (enchantNotAllowed) {
                                    event.setResult(new ItemStack(Material.AIR));
                                    player.updateInventory();
                                }
                            }
                        }
                    } else if (right.getType().getMaxDurability() > 0) {
                        List<Material> combineMaterials = Settings.getInstance().getAnvilCombineItem();
                        if (Settings.getInstance().getSpecialAnvilCombineItem()) {
                            if (!combineMaterials.isEmpty() && !combineMaterials.contains(left.getType())) {
                                event.setResult(new ItemStack(Material.AIR));
                                player.updateInventory();
                            }
                        } else if (combineMaterials.isEmpty() || combineMaterials.contains(left.getType())) {
                            event.setResult(new ItemStack(Material.AIR));
                            player.updateInventory();
                        }
                    } else {
                        List<Material> repairMaterial = Settings.getInstance().getAnvilRepairMaterial();
                        if (Settings.getInstance().getSpecialAnvilRepairMaterial()) {
                            if (!repairMaterial.isEmpty() && !repairMaterial.contains(right.getType())) {
                                event.setResult(new ItemStack(Material.AIR));
                                player.updateInventory();
                            }
                        } else if (repairMaterial.isEmpty() || repairMaterial.contains(right.getType())) {
                            event.setResult(new ItemStack(Material.AIR));
                            player.updateInventory();
                        }
                    }
                }
            }
        } else {
            Location location = inventory.getLocation();

            if (location != null) {
                Block block = location.getBlock();
                InventoryView view = event.getView();

                Player player = (Player) view.getPlayer();

                ItemResult result;

                boolean sameLeft = false;
                boolean sameRight = false;
                Anvil anvil = Anvils.get(player);
                if (anvil != null) {
                    ItemStack lastLeft = anvil.getLeftIngredient();
                    sameLeft = left == null && lastLeft == null;
                    if (!sameLeft && left != null && lastLeft != null) {
                        sameLeft = left.hashCode() == lastLeft.hashCode();
                    }

                    if (sameLeft) {
                        ItemStack lastRight = anvil.getRightIngredient();
                        sameRight = right == null && lastRight == null;
                        if (!sameRight && right != null && lastRight != null) {
                            sameRight = right.hashCode() == lastRight.hashCode();
                        }
                    }
                }

                if (sameLeft && sameRight) {
                    result = anvil.getResult();
                } else {
                    Args a = Args.create().player(player).inventoryView(view).location(block.getLocation()).recipe(recipe).build();
                    result = recipe.getDisplayResult(a);
                    if (result != null) {
                        a.setResult(result);

                        if (recipe.sendPrepare(a)) {
                            a.sendEffects(a.player(), Messages.getInstance().get("flag.prefix.recipe"));
                        } else {
                            a.sendReasons(a.player(), Messages.getInstance().get("flag.prefix.recipe"));
                            result = null;
                        }

                        if (result != null) {
                            if (result.sendPrepare(a)) {
                                a.sendEffects(a.player(), Messages.getInstance().get("flag.prefix.recipe"));
                            } else {
                                a.sendReasons(a.player(), Messages.getInstance().get("flag.prefix.recipe"));
                                result = null;
                            }
                        }
                    }
                }

                event.setResult(result);


                String renameText = "";

                // 1.10 didn't support repair cost or rename text
                if (Version.has1_11Support()) {
                    renameText = inventory.getRenameText();
                    int repairCost = recipe.getRepairCost();
                    if (recipe.isRenamingAllowed() && renameText != null && !renameText.isEmpty()) {
                        repairCost += 1;
                    }

                    int finalRepairCost = repairCost;

                    updateRepairCost(player, inventory, finalRepairCost);
                    Bukkit.getScheduler().runTaskLater(RecipeManager.getPlugin(), () -> updateRepairCost(player, inventory, finalRepairCost), 2);
                }

                Anvils.remove(player);
                Anvils.add(player, recipe, left, right, result, renameText);
            }
        }
    }

    private void updateRepairCost(Player player, AnvilInventory inventory, int repairCost) {
        if (repairCost > 40) {
            inventory.setMaximumRepairCost(repairCost);
        }
        inventory.setRepairCost(repairCost);
        if (player != null) {
            player.updateInventory();
        }
    }

    @EventHandler(priority=EventPriority.MONITOR)
    public void anvilInventoryClose(InventoryCloseEvent event) {
        HumanEntity ent = event.getPlayer();
        if (ent instanceof Player) {
            Inventory inv = event.getInventory();

            if (inv instanceof AnvilInventory) {
                Location location = inv.getLocation();

                if (location != null) {
                    Anvils.remove((Player) ent);
                }
            }
        }
    }

    @EventHandler
    public void anvilDrag(InventoryDragEvent event) {
        HumanEntity ent = event.getWhoClicked();
        if (ent instanceof Player) {
            Inventory inv = event.getInventory();

            if (inv instanceof AnvilInventory) {
                AnvilInventory anvilInventory = (AnvilInventory) inv;
                Location location = inv.getLocation();
                if (location != null) {
                    Player player = (Player) ent;
                    Anvil anvil = Anvils.get(player);
                    if (anvil != null) {
                        // Force refresh by updating the cost
                        int originalRepair = anvilInventory.getRepairCost();
                        if (originalRepair > 0) {
                            updateRepairCost(null, anvilInventory, originalRepair + 1);

                            Bukkit.getScheduler().runTaskLater(RecipeManager.getPlugin(), () -> updateRepairCost(player, anvilInventory, originalRepair), 0);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void anvilInventoryClick(InventoryClickEvent event) {
        HumanEntity ent = event.getWhoClicked();
        if (ent instanceof Player) {
            Inventory inv = event.getInventory();
            if (inv instanceof AnvilInventory) {
                AnvilInventory anvilInventory = (AnvilInventory) inv;
                Location location = inv.getLocation();
                if (location != null) {
                    Player player = (Player) ent;
                    Anvil anvil = Anvils.get(player);
                    if (anvil != null) {
                        if (event.getRawSlot() == 2) {
                            if (!RecipeManager.getPlugin().canCraft(player)) {
                                event.setCancelled(true);
                                return;
                            }

                            ClickType clickType = event.getClick();
                            if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
                                event.setCancelled(true);
                                craftFinishAnvil(event, player, anvilInventory, true);
                            } else if (clickType == ClickType.LEFT || clickType == ClickType.RIGHT) {
                                event.setCancelled(true);
                                craftFinishAnvil(event, player, anvilInventory, false);
                            }
                        }
                    }
                }
            }
        }
    }

    private void craftFinishAnvil(InventoryClickEvent event, Player player, AnvilInventory inventory, boolean isShiftClick) {
        InventoryView view = event.getView();
        Location location = inventory.getLocation();

        Anvil anvil = Anvils.get(player);

        int times = 1;
        if (isShiftClick) {
            times = 64;
        }

        // Clone the recipe so we can add custom flags to it
        AnvilRecipe recipe = new AnvilRecipe(anvil.getRecipe());
        Args a = Args.create().player(player).inventoryView(view).recipe(recipe).location(location).build();

        String renameText = anvil.getRenameText();
        boolean toRename = recipe.isRenamingAllowed() && renameText != null && !renameText.isEmpty();

        // Convert repair cost into need/mod level flags
        int repairCost = recipe.getRepairCost();
        if (toRename) {
            repairCost += 1;
        }
        if (repairCost != 0 && player.getGameMode() != GameMode.CREATIVE) {
            FlagNeedLevel needLevel = new FlagNeedLevel();
            needLevel.setMinLevel(repairCost);
            needLevel.setMaxLevel(repairCost);
            recipe.addFlag(needLevel);

            FlagModLevel modLevel = new FlagModLevel();
            modLevel.setAmount(-repairCost);
            modLevel.setFailMessage("false");
            recipe.addFlag(modLevel);
        }

        if (!recipe.checkFlags(a)) {
            SoundNotifier.sendDenySound(player, location);
            return;
        }


        if (toRename) {
            FlagItemName itemName = new FlagItemName();
            itemName.setResultName(renameText);

            for (ItemResult result : recipe.getResults()) {
                result.addFlag(itemName);
            }
        }

        ItemResult result = anvil.getResult();

        // We're handling durability on the result line outside of flags, so the original damage should be saved here
        int originalDamage = -1;
        if (result != null && Version.has1_13Support()) {
            ItemMeta meta = result.getItemMeta();
            if (meta instanceof org.bukkit.inventory.meta.Damageable) {
                originalDamage = ((Damageable) meta).getDamage();
            }

            result.clearMetadata(); // Reset result's metadata to remove prepare's effects
        }

        if (result != null) {
            a = Args.create().player(player).inventoryView(view).recipe(recipe).location(location).result(result).build();

            boolean firstRun = true;
            for (int i = 0; i < times; i++) {
                // Make sure block is still an anvil
                if (location != null) {
                    Material blockType = location.getBlock().getType();
                    if (blockType != Material.ANVIL && (!Version.has1_13BasicSupport() || (blockType != Material.CHIPPED_ANVIL && blockType != Material.DAMAGED_ANVIL))) {
                        break;
                    }
                }
                ItemStack left = inventory.getItem(0);
                ItemStack right = inventory.getItem(1);

                // Make sure no items have changed or stop crafting
                if (!ToolsItem.isSameItemHash(left, anvil.getLeftIngredient()) || !ToolsItem.isSameItemHash(right, anvil.getRightIngredient())) {
                    break;
                }

                // Make sure all flag conditions are still valid or stop crafting
                if (!recipe.checkFlags(a)) {
                    break;
                }

                boolean skipCraft = false;
                List<ItemResult> potentialResults = recipe.getResults();
                if (recipe.isMultiResult()) {
                    boolean hasMatch = false;
                    if (recipe.hasFlag(FlagType.INDIVIDUAL_RESULTS)) {
                        for (ItemResult r : potentialResults) {
                            a.clear();

                            if (r.checkFlags(a)) {
                                result = r.clone();
                                hasMatch = true;
                                break;
                            }
                        }
                    } else {
                        float maxChance = 0;

                        List<ItemResult> matchingResults = new ArrayList<>();
                        for (ItemResult r : potentialResults) {
                            a.clear();

                            if (r.checkFlags(a)) {
                                matchingResults.add(r);
                                maxChance += r.getChance();
                            }
                        }

                        float rand = RecipeManager.random.nextFloat() * maxChance;
                        float chance = 0;

                        for (ItemResult r : matchingResults) {
                            chance += r.getChance();

                            if (chance >= rand) {
                                hasMatch = true;
                                result = r.clone();
                                break;
                            }
                        }
                    }

                    if (!hasMatch || result.getType() == Material.AIR) {
                        skipCraft = true;
                    }
                } else {
                    result = potentialResults.get(0).clone();

                    if (!result.checkFlags(a)) {
                        break;
                    }
                }
                a.setResult(result);

                boolean recipeCraftSuccess = false;
                boolean resultCraftSuccess = false;
                if (!skipCraft) {
                    // Reset result's metadata for each craft
                    result.clearMetadata();

                    // We're handling durability on the result line outside of flags, so it needs to be reset after clearing the metadata
                    if (originalDamage != -1) {
                        ItemMeta meta = result.getItemMeta();
                        if (meta instanceof Damageable) {
                            ((Damageable) meta).setDamage(originalDamage);
                            result.setItemMeta(meta);
                        }
                    }

                    a.setFirstRun(firstRun); // TODO: Remove and create onCraftComplete
                    a.clear();

                    recipeCraftSuccess = recipe.sendCrafted(a);
                    if (recipeCraftSuccess) {
                        a.sendEffects(a.player(), Messages.getInstance().get("flag.prefix.recipe"));
                    }

                    a.clear();

                    resultCraftSuccess = result.sendCrafted(a);
                    if (resultCraftSuccess) {
                        a.sendEffects(a.player(), Messages.getInstance().parse("flag.prefix.result", "{item}", ToolsItem.print(result)));
                    }
                }

                if ((recipeCraftSuccess && resultCraftSuccess) || skipCraft) {
                    boolean noResult = false;

                    if (skipCraft) {
                        noResult = true;
                    } else {
                        if (recipe.hasFlag(FlagType.INDIVIDUAL_RESULTS)) {
                            float chance = result.getChance();
                            float rand = RecipeManager.random.nextFloat() * 100;

                            if (chance >= 0 && chance < rand) {
                                noResult = true;
                            }
                        }

                        if (result.hasFlag(FlagType.NO_RESULT)) {
                            noResult = true;
                        } else if (event.isShiftClick() || ToolsItem.merge(event.getCursor(), result) == null) {
                            noResult = true;
                            // Make sure inventory can fit the results or drop on the ground
                            if (Tools.playerCanAddItem(player, result)) {
                                player.getInventory().addItem(result.clone());
                            } else {
                                player.getWorld().dropItem(player.getLocation(), result.clone());
                            }
                        }
                    }

                    if (!noResult) {
                        ItemStack merged = ToolsItem.merge(event.getCursor(), result);
                        player.setItemOnCursor(merged);
                    }
                }

                recipe.subtractIngredients(inventory, result, false);

                damageAnvil(location, recipe.getAnvilDamageChance());

                // TODO call post-event ?

                firstRun = false;
            }
        }
    }

    private void damageAnvil(Location location, double damageChance) {
        if (location != null) {
            boolean broken = false;
            while (damageChance > 0 && !broken) {
                double random = RecipeManager.random.nextFloat() * 100;
                if (random < damageChance) {
                    Block block = location.getBlock();

                    if (Version.has1_13BasicSupport()) {
                        Material blockType = block.getType();
                        if (blockType == Material.ANVIL) {
                            block.setType(Material.CHIPPED_ANVIL);
                        } else if (blockType == Material.CHIPPED_ANVIL) {
                            block.setType(Material.DAMAGED_ANVIL);
                        } else if (blockType == Material.DAMAGED_ANVIL) {
                            block.setType(Material.AIR);
                            broken = true;
                        }
                    } else {
                        byte blockData = block.getData();
                        if (blockData < 8) {
                            BlockState state = block.getState();
                            state.setRawData((byte) (blockData + 4));
                            state.update();
                        } else {
                            block.setType(Material.AIR);
                            broken = true;
                        }
                    }
                }

                damageChance -= 100;
            }
        }
    }

    private void updateAnvilInventory(Player player, AnvilInventory anvilInventory) {
        Anvil anvil = Anvils.get(player);

        boolean leftMatch = ToolsItem.isSameItemHash(anvilInventory.getItem(0), anvil.getLeftSingleStack());

        boolean rightMatch = false;
        if (leftMatch) {
            rightMatch = ToolsItem.isSameItemHash(anvilInventory.getItem(1), anvil.getRightSingleStack());

            if (rightMatch) {
                // Force a new prepare event by setting an item
                anvilInventory.setItem(0, anvil.getLeftIngredient());
            }
        }

        if (!leftMatch || !rightMatch) {
            Anvils.remove(player);
        }
    }


    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void anvilPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Material blockType = event.getClickedBlock().getType();

            if (blockType == Material.ANVIL || (Version.has1_13BasicSupport() && (blockType == Material.CHIPPED_ANVIL || blockType == Material.DAMAGED_ANVIL))) {
                if (!RecipeManager.getPlugin().canCraft(event.getPlayer())) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
