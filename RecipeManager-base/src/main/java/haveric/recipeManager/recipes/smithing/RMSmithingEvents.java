package haveric.recipeManager.recipes.smithing;

import haveric.recipeManager.RecipeManager;
import haveric.recipeManager.Recipes;
import haveric.recipeManager.UpdateInventory;
import haveric.recipeManager.common.recipes.RMCRecipeType;
import haveric.recipeManager.data.BaseRecipeData;
import haveric.recipeManager.flag.FlagType;
import haveric.recipeManager.flag.args.Args;
import haveric.recipeManager.messages.MessageSender;
import haveric.recipeManager.messages.Messages;
import haveric.recipeManager.messages.SoundNotifier;
import haveric.recipeManager.recipes.BaseRecipe;
import haveric.recipeManager.recipes.BaseRecipeEvents;
import haveric.recipeManager.recipes.ItemResult;
import haveric.recipeManager.recipes.smithing.data.SmithingTables;
import haveric.recipeManager.tools.Tools;
import haveric.recipeManager.tools.ToolsInventory;
import haveric.recipeManager.tools.ToolsItem;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class RMSmithingEvents extends BaseRecipeEvents {
    public RMSmithingEvents() { }

    @EventHandler(priority = EventPriority.LOW)
    public void rmSmithingTablePlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();

            if (block != null && block.getType() == Material.SMITHING_TABLE) {
                Player player = event.getPlayer();

                SmithingTables.add(player, null, null, null, block.getLocation().clone());
            }
        }
    }
    @EventHandler(priority= EventPriority.MONITOR)
    public void smithingTableInventoryClose(InventoryCloseEvent event) {
        HumanEntity ent = event.getPlayer();
        if (ent instanceof Player) {
            SmithingTables.remove((Player) ent);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerTeleport(PlayerTeleportEvent event) {
        SmithingTables.remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerDeath(PlayerDeathEvent event) {
        SmithingTables.remove(event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerQuit(PlayerQuitEvent event) {
        SmithingTables.remove(event.getPlayer());
    }

    @EventHandler
    public void smithingTableDrag(InventoryDragEvent event) {
        HumanEntity ent = event.getWhoClicked();
        if (ent instanceof Player) {
            Inventory inv = event.getInventory();

            if (inv instanceof SmithingInventory) {
                SmithingInventory smithingTableInventory = (SmithingInventory) inv;
                //Location location = inv.getLocation();
                //if (location != null) {
                    Player player = (Player) ent;

                    prepareSmithingTableLater(smithingTableInventory, player, event.getView());
                //}
            }
        }
    }

    @EventHandler
    public void smithingTableInventoryClick(InventoryClickEvent event) {
        HumanEntity ent = event.getWhoClicked();
        if (ent instanceof Player) {
            Inventory inv = event.getInventory();
            if (inv instanceof SmithingInventory) {
                SmithingInventory smithingTableInventory = (SmithingInventory) inv;
                //Location location = inv.getLocation();

                //if (location != null) {
                    Player player = (Player) ent;

                    ClickType clickType = event.getClick();
                    int rawSlot = event.getRawSlot();
                    if (rawSlot == 2) {
                        if (!RecipeManager.getPlugin().canCraft(player)) {
                            event.setCancelled(true);
                            return;
                        }

                        BaseRecipeData smithingTable = SmithingTables.get(player);
                        if (smithingTable != null) {
                            if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT || clickType == ClickType.CONTROL_DROP) {
                                craftFinishSmithingTable(event, player, smithingTableInventory, true);
                                prepareSmithingTableLater(smithingTableInventory, player, event.getView());
                                new UpdateInventory(player, 2);
                            } else if (clickType == ClickType.LEFT || clickType == ClickType.RIGHT || clickType == ClickType.NUMBER_KEY || clickType == ClickType.DROP) {
                                craftFinishSmithingTable(event, player, smithingTableInventory, false);
                                prepareSmithingTableLater(smithingTableInventory, player, event.getView());
                                new UpdateInventory(player, 2);
                            }
                        }
                    } else if (rawSlot == 0 || rawSlot == 1) {
                        if (clickType == ClickType.NUMBER_KEY) {
                            event.setCancelled(true);
                            ToolsInventory.simulateHotbarSwap(smithingTableInventory, rawSlot, event.getView().getBottomInventory(), event.getHotbarButton());
                        } else if (clickType != ClickType.SHIFT_LEFT && clickType != ClickType.SHIFT_RIGHT && clickType != ClickType.DOUBLE_CLICK) {
                            event.setCancelled(true);
                            ToolsInventory.simulateDefaultClick(player, smithingTableInventory, rawSlot, clickType);
                        }

                        prepareSmithingTableLater(smithingTableInventory, player, event.getView());
                    } else if (rawSlot > 2) {
                        if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
                            ItemStack currentItem = event.getCurrentItem();

                            if (currentItem != null) {
                                if (currentItem.getItemMeta() instanceof EnchantmentStorageMeta || currentItem.getType().getMaxDurability() > 0) {
                                    prepareSmithingTableLater(smithingTableInventory, player, event.getView());
                                }
                            }
                        }
                    }
                //}
            }
        }
    }

    private void prepareSmithingTableLater(SmithingInventory inventory, Player player, InventoryView view) {
        new BukkitRunnable() {
            @Override
            public void run() {
                prepareSmithingTable(inventory, player, view);
            }
        }.runTaskLater(RecipeManager.getPlugin(), 0);
    }

    private void prepareSmithingTable(SmithingInventory inventory, Player player, InventoryView view) {
        ItemStack top = inventory.getItem(0);
        ItemStack bottom = inventory.getItem(1);

        List<ItemStack> ingredients = new ArrayList<>();
        ingredients.add(top);
        ingredients.add(bottom);

        BaseRecipe baseRecipe = Recipes.getInstance().getRecipe(RMCRecipeType.SMITHING, ingredients, null);
        if (baseRecipe instanceof RMSmithingRecipe) {
            RMSmithingRecipe recipe = (RMSmithingRecipe) baseRecipe;

            //Location location = inventory.getLocation();

            //if (location != null) {
                Location location = SmithingTables.get(player).getLocation();
                Block block = location.getBlock();

                Args a = Args.create().player(player).inventoryView(view).location(block.getLocation()).recipe(recipe).build();
                ItemResult result = recipe.getDisplayResult(a);
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

                inventory.setItem(2, result);
                player.updateInventory();

                SmithingTables.remove(player);
                SmithingTables.add(player, recipe, ingredients, result, location);
            //}
        }
    }

    private void craftFinishSmithingTable(InventoryClickEvent event, Player player, SmithingInventory inventory, boolean isShiftClick) {
        InventoryView view = event.getView();
        Location location = inventory.getLocation();

        BaseRecipeData smithingTable = SmithingTables.get(player);

        int times = 1;
        if (isShiftClick) {
            times = 64;
        }
        // Clone the recipe so we can add custom flags to it
        RMSmithingRecipe recipe = new RMSmithingRecipe(smithingTable.getRecipe());
        MessageSender.getInstance().info("Craft finish Smithing: " + recipe);
        Args a = Args.create().player(player).inventoryView(view).recipe(recipe).location(location).build();

        if (!recipe.checkFlags(a)) {
            SoundNotifier.sendDenySound(player, location);
            event.setCancelled(true);
            return;
        }

        ItemResult result = smithingTable.getResult();

        // We're handling durability on the result line outside of flags, so the original damage should be saved here
        int originalDamage = -1;
        if (result != null) {
            ItemMeta meta = result.getItemMeta();
            if (meta instanceof Damageable) {
                originalDamage = ((Damageable) meta).getDamage();
            }

            result.clearMetadata(); // Reset result's metadata to remove prepare's effects
        }

        if (result != null) {
            a = Args.create().player(player).inventoryView(view).recipe(recipe).location(location).result(result).build();

            boolean firstRun = true;
            for (int i = 0; i < times; i++) {
                // Make sure block is still valid
                if (location != null) {
                    Material blockType = location.getBlock().getType();
                    if (!recipe.isValidBlockMaterial(blockType)) {
                        break;
                    }
                }
                ItemStack top = inventory.getItem(0);
                ItemStack bottom = inventory.getItem(1);

                // Make sure no items have changed or stop crafting
                if (!ToolsItem.isSameItemHash(top, smithingTable.getIngredientSingleStack(0)) || !ToolsItem.isSameItemHash(bottom, smithingTable.getIngredientSingleStack(1))) {
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

                // TODO call post-event ?

                firstRun = false;
            }
        }
    }
}