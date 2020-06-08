package haveric.recipeManager.commands;

import haveric.recipeManager.RecipeManager;
import haveric.recipeManager.common.RMCVanilla;
import haveric.recipeManager.common.recipes.RMCRecipeInfo;
import haveric.recipeManager.common.util.ParseBit;
import haveric.recipeManager.common.util.RMCUtil;
import haveric.recipeManager.messages.MessageSender;
import haveric.recipeManager.messages.Messages;
import haveric.recipeManager.recipes.BaseRecipe;
import haveric.recipeManager.recipes.PreparableResultRecipe;
import haveric.recipeManager.recipes.SingleResultRecipe;
import haveric.recipeManager.recipes.campfire.RMCampfireRecipe;
import haveric.recipeManager.recipes.combine.CombineRecipe;
import haveric.recipeManager.recipes.combine.CombineRecipe1_13;
import haveric.recipeManager.recipes.craft.CraftRecipe;
import haveric.recipeManager.recipes.craft.CraftRecipe1_13;
import haveric.recipeManager.recipes.fuel.FuelRecipe;
import haveric.recipeManager.recipes.fuel.FuelRecipe1_13;
import haveric.recipeManager.recipes.furnace.RMBaseFurnaceRecipe1_13;
import haveric.recipeManager.recipes.furnace.RMFurnaceRecipe;
import haveric.recipeManager.recipes.stonecutting.RMStonecuttingRecipe;
import haveric.recipeManager.tools.Tools;
import haveric.recipeManager.tools.ToolsItem;
import haveric.recipeManager.tools.Version;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.Map.Entry;

public class RecipeCommand implements TabExecutor {
    private static Map<UUID, Pages> pagination = new HashMap<>();

    public static void clean(UUID uuid) {
        pagination.remove(uuid);
    }

    public static void clean() {
        pagination.clear();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> list = new ArrayList<>();

        if (args.length == 1) {
            if (sender instanceof Player) {
                UUID playerUUID = ((Player) sender).getUniqueId();

                Pages currentPages = pagination.get(playerUUID);
                if (currentPages != null) {
                    if (currentPages.page > 0) {
                        list.add("prev");
                    } else if (currentPages.page + 1 < currentPages.pages.length) {
                        list.add("next");
                    }
                }
            }

            String input = args[0];
            if (!input.contains(":")) {
                String inputMaterial = RMCUtil.parseAliasName(input);
                for (Material mat : Material.values()) {
                    String matName = RMCUtil.parseAliasName(mat.name());

                    if (matName.contains(inputMaterial)) {
                        list.add(mat.name().toLowerCase());
                    }
                }
            }
        }

        return list;
    }

    public static class Pages {
        private UUID playerUUID;
        private ItemStack item;
        private int page = -1;
        private String[] pages;
        private BukkitTask task;

        public Pages(UUID uuid, ItemStack newItem, List<String> newPages) {
            playerUUID = uuid;
            item = newItem;
            pages = newPages.toArray(new String[0]);
        }

        private void doTask() {
            if (task != null) {
                task.cancel();
            }

            task = new BukkitRunnable() {
                public void run() {
                    pagination.remove(playerUUID);
                }
            }.runTaskLater(RecipeManager.getPlugin(), 20 * 60);

        }

        public boolean hasNext() {
            return pages.length > (page + 1);
        }

        public String next() {
            page++;

            if (page >= pages.length) {
                return null;
            }

            doTask();
            return pages[page];
        }

        public boolean hasPrev() {
            return page > 0;
        }

        public String prev() {
            if (page <= 0) {
                return null;
            }

            page--;

            doTask();
            return pages[page];
        }
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            UUID playerUUID = null;
            if (sender instanceof Player) {
                playerUUID = ((Player) sender).getUniqueId();
            }

            boolean next = args[0].equalsIgnoreCase("next");

            if (next || args[0].equalsIgnoreCase("prev")) {
                Pages pages = pagination.get(playerUUID);

                if (pages == null) {
                    Messages.getInstance().send(sender, "cmd.recipes.needquery");
                } else {
                    if (next ? pages.hasNext() : pages.hasPrev()) {
                        String page = (next ? pages.next() : pages.prev());
                        Messages.getInstance().send(sender, "cmd.recipes.header", "{item}", ToolsItem.print(pages.item), "{num}", (pages.page + 1), "{total}", pages.pages.length);
                        MessageSender.getInstance().send(sender, page);

                        if (pages.hasNext()) {
                            Messages.getInstance().send(sender, "cmd.recipes.more", "{cmdnext}", "/" + label + " next", "{cmdprev}", "/" + label + " prev");
                        } else {
                            Messages.getInstance().send(sender, "cmd.recipes.end");
                        }
                    } else {
                        if (next) {
                            Messages.getInstance().send(sender, "cmd.recipes.nonext", "{command}", "/" + label + " prev");
                        } else {
                            Messages.getInstance().send(sender, "cmd.recipes.noprev", "{command}", "/" + label + " next");
                        }
                    }
                }
            } else {
                ItemStack item;

                if (args[0].equalsIgnoreCase("this")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        if (Version.has1_12Support()) {
                            item = player.getInventory().getItemInMainHand();
                        } else {
                            //noinspection deprecation
                            item = player.getItemInHand();
                        }

                        if (item == null) {
                            Messages.getInstance().send(player, "cmd.recipes.nohand");
                            return true;
                        }
                    } else {
                        sender.sendMessage("The 'this' argument can't be used from console.");
                        return true;
                    }
                } else {
                    item = Tools.parseItem(args[0], RMCVanilla.DATA_WILDCARD, ParseBit.NO_META | ParseBit.NO_PRINT);

                    if (item == null) {
                        Messages.getInstance().send(sender, "cmd.recipes.invaliditem", "{arg}", args[0]);
                        return true;
                    }
                }

                boolean ingredient = (args.length > 1 && args[1].charAt(0) == 'i');

                List<String> list = new ArrayList<>();

                for (Entry<BaseRecipe, RMCRecipeInfo> e : RecipeManager.getRecipes().getRecipeList().entrySet()) {
                    BaseRecipe recipe = e.getKey();

                    if (hasItem(recipe, item, ingredient)) {
                        list.addAll(recipe.printChat());
                    }
                }

                if (list.isEmpty()) {
                    Messages.getInstance().send(sender, "cmd.recipes.noresults", "{item}", ToolsItem.print(item));
                } else {
                    Pages pages = new Pages(playerUUID, item, list);
                    pagination.put(playerUUID, pages);

                    Messages.getInstance().send(sender, "cmd.recipes.header", "{item}", ToolsItem.print(pages.item), "{num}", 1, "{total}", pages.pages.length);
                    MessageSender.getInstance().send(sender, pages.next());

                    if (pages.hasNext()) {
                        Messages.getInstance().send(sender, "cmd.recipes.more", "{cmdnext}", "/" + label + " next", "{cmdprev}", "/" + label + " prev");
                    } else {
                        Messages.getInstance().send(sender, "cmd.recipes.end");
                    }
                }
            }
        } else {
            int mc = 0;
            int rm = 0;
            int other = 0;

            for (RMCRecipeInfo info : RecipeManager.getRecipes().getRecipeList().values()) {
                switch (info.getOwner()) {
                    case MINECRAFT:
                        mc++;
                        break;
                    case RECIPEMANAGER:
                        rm++;
                        break;
                    default:
                        other++;
                }
            }

            Messages.getInstance().send(sender, "cmd.recipes.usage", "{command}", label);
            Messages.getInstance().send(sender, "cmd.recipes.stats.mc", "{num}", mc);
            Messages.getInstance().send(sender, "cmd.recipes.stats.rm", "{num}", rm);
            Messages.getInstance().send(sender, "cmd.recipes.stats.other", "{num}", other);
        }

        return true;
    }

    private boolean hasItem(BaseRecipe recipe, ItemStack item, boolean ingredient) {
        if (ingredient) {
            if (recipe instanceof CraftRecipe1_13) {
                return containsRecipeChoiceMap(((CraftRecipe1_13) recipe).getIngredientsChoiceMap(), item);
            } else if (recipe instanceof CraftRecipe) {
                return containsItem(Arrays.asList(((CraftRecipe) recipe).getIngredients()), item, true);
            } else if (recipe instanceof CombineRecipe1_13) {
                return containsRecipeChoiceList(((CombineRecipe1_13) recipe).getIngredientChoiceList(), item);
            } else if (recipe instanceof CombineRecipe) {
                return containsItem(((CombineRecipe) recipe).getIngredients(), item, true);
            } else if (recipe instanceof RMFurnaceRecipe) {
                return containsItem(Collections.singletonList(((RMFurnaceRecipe) recipe).getIngredient()), item, true);
            } else if (recipe instanceof RMBaseFurnaceRecipe1_13) {
                return containsRecipeChoice(((RMBaseFurnaceRecipe1_13) recipe).getIngredientChoice(), item.getType());
            } else if (recipe instanceof RMCampfireRecipe) {
                return containsRecipeChoice(((RMCampfireRecipe) recipe).getIngredientChoice(), item.getType());
            } else if (recipe instanceof RMStonecuttingRecipe) {
                return containsRecipeChoice(((RMStonecuttingRecipe) recipe).getIngredientChoice(), item.getType());
            } else if (recipe instanceof FuelRecipe1_13) {
                return containsRecipeChoice(((FuelRecipe1_13) recipe).getIngredientChoice(), item.getType());
            } else if (recipe instanceof FuelRecipe) {
                return containsItem(Collections.singletonList(((FuelRecipe) recipe).getIngredient()), item, true);
            }
        } else {
            if (recipe instanceof PreparableResultRecipe) {
                PreparableResultRecipe r = (PreparableResultRecipe) recipe;

                return containsItem(r.getResults(), item, false);
            } else if (recipe instanceof SingleResultRecipe) {
                SingleResultRecipe r = (SingleResultRecipe) recipe;

                return containsItem(Collections.singletonList(r.getResult()), item, false);
            }
        }

        return false;
    }

    private boolean containsRecipeChoiceMap(Map<Character, RecipeChoice> choiceMap, ItemStack item) {
        boolean contains = false;
        for (RecipeChoice choice : choiceMap.values()) {
            if (choice instanceof RecipeChoice.MaterialChoice) {
                RecipeChoice.MaterialChoice materialChoice = (RecipeChoice.MaterialChoice) choice;
                contains = containsMaterial(materialChoice.getChoices(), item.getType());
            } else if (choice instanceof RecipeChoice.ExactChoice) {
                RecipeChoice.ExactChoice exactChoice = (RecipeChoice.ExactChoice) choice;
                contains = containsItem(exactChoice.getChoices(), item, true);
            }

            if (contains) {
                break;
            }
        }

        return contains;
    }

    private boolean containsRecipeChoiceList(List<RecipeChoice> choiceList, ItemStack item) {
        boolean contains = false;
        for (RecipeChoice choice : choiceList) {
            if (choice instanceof RecipeChoice.MaterialChoice) {
                RecipeChoice.MaterialChoice materialChoice = (RecipeChoice.MaterialChoice) choice;
                contains = containsMaterial(materialChoice.getChoices(), item.getType());
            } else if (choice instanceof RecipeChoice.ExactChoice) {
                RecipeChoice.ExactChoice exactChoice = (RecipeChoice.ExactChoice) choice;
                contains = containsItem(exactChoice.getChoices(), item, true);
            }

            if (contains) {
                break;
            }
        }

        return contains;
    }

    private boolean containsRecipeChoice(RecipeChoice choice, Material material) {
        if (choice instanceof RecipeChoice.MaterialChoice) {
            RecipeChoice.MaterialChoice materialChoice = (RecipeChoice.MaterialChoice) choice;
            return containsMaterial(materialChoice.getChoices(), material);
        } else if (choice instanceof RecipeChoice.ExactChoice) {
            RecipeChoice.ExactChoice exactChoice = (RecipeChoice.ExactChoice) choice;
            return containsMaterialFromItems(exactChoice.getChoices(), material);
        }

        return false;
    }

    private boolean containsMaterialFromItems(List<ItemStack> items, Material materialToMatch) {
        for (ItemStack item : items) {
            if (materialToMatch == item.getType()) {
                return true;
            }
        }

        return false;
    }

    private boolean containsMaterial(List<Material> materials, Material materialToMatch) {
        for (Material material : materials) {
            if (materialToMatch == material) {
                return true;
            }
        }

        return false;
    }

    private boolean containsItem(Collection<? extends ItemStack> items, ItemStack item, boolean ingredient) {
        for (ItemStack i : items) {
            if (i != null && i.getType() == item.getType() && (item.getDurability() == RMCVanilla.DATA_WILDCARD || i.getDurability() == item.getDurability()) && (ingredient || item.getAmount() == 1 || item.getAmount() == i.getAmount())) {
                return true;
            }
        }

        return false;
    }
}
