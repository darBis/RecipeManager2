package haveric.recipeManager.flag;

import haveric.recipeManager.flag.flags.any.*;
import haveric.recipeManager.flag.flags.any.flagCooldown.FlagCooldown;
import haveric.recipeManager.flag.flags.any.flagSummon.FlagSummon;
import haveric.recipeManager.flag.flags.recipe.*;
import haveric.recipeManager.flag.flags.result.*;
import haveric.recipeManager.flag.flags.result.applyEnchantment.FlagApplyEnchantment;
import haveric.recipeManager.flag.flags.result.applyEnchantment.FlagStoreEnchantment;
import haveric.recipeManager.messages.MessageSender;
import haveric.recipeManager.tools.Supports;
import haveric.recipeManager.tools.Version;
import org.bukkit.ChatColor;

public class FlagLoader {
    public FlagLoader() {
        loadDefaultFlags(false);
    }

    public FlagLoader(boolean force) {
        loadDefaultFlags(force);
    }

    private void loadDefaultFlags(boolean force) {
        int ANY = FlagBit.RECIPE | FlagBit.INGREDIENT | FlagBit.RESULT;
        int RECIPE_OR_RESULT = FlagBit.RECIPE | FlagBit.RESULT;
        int APPLIED_TO_ITEM = FlagBit.INGREDIENT | FlagBit.RESULT | FlagBit.NO_DELAY;

        loadFlag(FlagType.BIOME, new FlagBiome(), RECIPE_OR_RESULT | FlagBit.NO_DELAY);
        loadFlag(FlagType.BLOCK_POWERED, new FlagBlockPowered(), RECIPE_OR_RESULT | FlagBit.NO_VALUE_REQUIRED | FlagBit.NO_DELAY, "poweredblock", "blockpower", "redstonepowered");
        loadFlag(FlagType.BROADCAST, new FlagBroadcast(), RECIPE_OR_RESULT, "announce", "msgall");
        loadFlag(FlagType.COMMAND, new FlagCommand(), RECIPE_OR_RESULT, "cmd", "commands");
        loadFlag(FlagType.COOLDOWN, new FlagCooldown(), RECIPE_OR_RESULT, "cooltime");
        loadFlag(FlagType.EXPLODE, new FlagExplode(), RECIPE_OR_RESULT | FlagBit.NO_VALUE_REQUIRED, "explosion", "boom", "tnt");
        loadFlag(FlagType.FOR_CHANCE, new FlagForChance(), RECIPE_OR_RESULT, "bychance", "chance");
        loadFlag(FlagType.FOR_DELAY, new FlagForDelay(), RECIPE_OR_RESULT, "delay");
        loadFlag(FlagType.FOR_PERMISSION, new FlagForPermission(), RECIPE_OR_RESULT, "forperm");
        loadFlag(FlagType.FOR_REPEAT, new FlagForRepeat(), RECIPE_OR_RESULT, "repeat");
        loadFlag(FlagType.GAMEMODE, new FlagGameMode(), RECIPE_OR_RESULT | FlagBit.NO_DELAY, "needgm");
        loadFlag(FlagType.GROUP, new FlagGroup(), RECIPE_OR_RESULT | FlagBit.NO_DELAY, "groups", "permissiongroup", "permgroup");
        loadFlag(FlagType.HEIGHT, new FlagHeight(), RECIPE_OR_RESULT | FlagBit.NO_DELAY, "depth");
        loadFlag(FlagType.HOLD_ITEM, new FlagHoldItem(), RECIPE_OR_RESULT | FlagBit.NO_DELAY, "hold");
        loadFlag(FlagType.ITEM_LORE, new FlagItemLore(), ANY | FlagBit.NO_VALUE_REQUIRED | FlagBit.NO_DELAY, "lore", "itemdesc");
        loadFlag(FlagType.ITEM_NAME, new FlagItemName(), ANY | FlagBit.NO_DELAY, "name", "displayname");
        loadFlag(FlagType.ITEM_NBT, new FlagItemNBT(), ANY | FlagBit.NO_DELAY, "nbt");
        loadFlag(FlagType.INGREDIENT_CONDITION, new FlagIngredientCondition(), RECIPE_OR_RESULT | FlagBit.NO_DELAY, "ingrcondition", "ingrcond", "ifingredient", "ifingr");
        loadFlag(FlagType.INVENTORY, new FlagInventory(), RECIPE_OR_RESULT | FlagBit.NO_DELAY);
        loadFlag(FlagType.KEEP_ITEM, new FlagKeepItem(), RECIPE_OR_RESULT | FlagBit.NO_DELAY, "returnitem", "replaceitem");
        loadFlag(FlagType.LAUNCH_FIREWORK, new FlagLaunchFirework(), RECIPE_OR_RESULT | FlagBit.ONCE_PER_SHIFT, "setfirework");
        loadFlag(FlagType.LIGHT_LEVEL, new FlagLightLevel(), RECIPE_OR_RESULT | FlagBit.NO_DELAY, "blocklight", "sunlight", "light");
        loadFlag(FlagType.MESSAGE, new FlagMessage(), RECIPE_OR_RESULT, "craftmsg", "msg");
        loadFlag(FlagType.MOD_EXP, new FlagModExp(), RECIPE_OR_RESULT, "expmod", "modxp", "xpmod", "exp", "xp", "giveexp", "givexp", "takeexp", "takexp");
        loadFlag(FlagType.MOD_LEVEL, new FlagModLevel(), RECIPE_OR_RESULT, "levelmod", "setlevel", "level");
        loadFlag(FlagType.MOD_MONEY, new FlagModMoney(), RECIPE_OR_RESULT, "moneymod", "setmoney", "money");
        loadFlag(FlagType.NEED_EXP, new FlagNeedExp(), RECIPE_OR_RESULT | FlagBit.NO_DELAY, "needxp", "reqexp", "expreq", "reqxp", "xpreq");
        loadFlag(FlagType.NEED_LEVEL, new FlagNeedLevel(), RECIPE_OR_RESULT | FlagBit.NO_DELAY, "reqlevel", "levelreq");
        loadFlag(FlagType.NEED_MONEY, new FlagNeedMoney(), RECIPE_OR_RESULT | FlagBit.NO_DELAY, "reqmoney", "moneyreq");
        loadFlag(FlagType.PERMISSION, new FlagPermission(), RECIPE_OR_RESULT | FlagBit.NO_DELAY, "permissions", "perm");
        loadFlag(FlagType.POTION_EFFECT, new FlagPotionEffect(), RECIPE_OR_RESULT | FlagBit.NO_DELAY, "potionfx");
        loadFlag(FlagType.SECRET, new FlagSecret(), RECIPE_OR_RESULT | FlagBit.NO_VALUE_REQUIRED | FlagBit.NO_FOR | FlagBit.NO_DELAY);
        loadFlag(FlagType.SET_BLOCK, new FlagSetBlock(), RECIPE_OR_RESULT | FlagBit.NO_DELAY, "changeblock");
        loadFlag(FlagType.SOUND, new FlagSound(), RECIPE_OR_RESULT | FlagBit.ONCE_PER_SHIFT, "playsound");
        loadFlag(FlagType.SPAWN_PARTICLE, new FlagSpawnParticle(), RECIPE_OR_RESULT | FlagBit.ONCE_PER_SHIFT, "particle");
        loadFlag(FlagType.SUMMON, new FlagSummon(), RECIPE_OR_RESULT, "spawn", "creature", "mob", "animal");
        loadFlag(FlagType.TEMPERATURE, new FlagTemperature(), RECIPE_OR_RESULT | FlagBit.NO_DELAY, "temp");
        loadFlag(FlagType.WEATHER, new FlagWeather(), RECIPE_OR_RESULT | FlagBit.NO_DELAY);
        loadFlag(FlagType.WORLD, new FlagWorld(), RECIPE_OR_RESULT | FlagBit.NO_DELAY, "needworld", "worlds");

        // TELEPORT(FlagTeleport(), "tpto", "goto"), // TODO finish flag
        // REALTIME(FlagRealTime(), FlagBit.NONE, "time", "date"),
        // ONLINETIME(FlagOnlineTime(), FlagBit.NONE, "playtime", "onlinefor"),
        // WORLDTIME(FlagWorldTime(), FlagBit.NONE),
        // PROXIMITY(FlagProximity(), FlagBit.NONE, "distance", "nearby"),
        // DEBUG(FlagDebug(), FlagBit.NO_VALUE_REQUIRED | FlagBit.NO_FOR | FlagBit.NO_SKIP_PERMISSION, "monitor", "log"),

        // Recipe only flags
        loadFlag(FlagType.ADD_TO_BOOK, new FlagAddToBook(), FlagBit.RECIPE | FlagBit.NO_FOR | FlagBit.NO_SKIP_PERMISSION, "recipebook");
        loadFlag(FlagType.DISPLAY_RESULT, new FlagDisplayResult(), FlagBit.RECIPE | FlagBit.NO_DELAY, "resultdisplay", "showresult");
        loadFlag(FlagType.FAIL_MESSAGE, new FlagFailMessage(), FlagBit.RECIPE | FlagBit.NO_DELAY, "failmsg");
        loadFlag(FlagType.INDIVIDUAL_RESULTS, new FlagIndividualResults(), FlagBit.RECIPE | FlagBit.NO_VALUE_REQUIRED | FlagBit.NO_FOR, "individual");
        loadFlag(FlagType.OVERRIDE, new FlagOverride(), FlagBit.RECIPE | FlagBit.NO_FOR | FlagBit.NO_VALUE_REQUIRED | FlagBit.NO_SKIP_PERMISSION, "edit", "overwrite", "supercede", "replace");
        loadFlag(FlagType.REMOVE, new FlagRemove(), FlagBit.RECIPE | FlagBit.NO_FOR | FlagBit.NO_VALUE_REQUIRED | FlagBit.NO_SKIP_PERMISSION, "delete");
        loadFlag(FlagType.RESTRICT, new FlagRestrict(), FlagBit.RECIPE | FlagBit.NO_VALUE_REQUIRED | FlagBit.NO_FOR, "disable", "denied", "deny");

        // Result only flags
        loadFlag(FlagType.NO_RESULT, new FlagNoResult(), FlagBit.RESULT | FlagBit.NO_DELAY | FlagBit.NO_VALUE_REQUIRED);

        loadFlag(FlagType.APPLY_ENCHANTMENT, new FlagApplyEnchantment(), FlagBit.RESULT | FlagBit.NO_VALUE_REQUIRED | FlagBit.NO_DELAY, "applyenchant", "applyenchantments", "applyenchants");
        loadFlag(FlagType.CLONE_INGREDIENT, new FlagCloneIngredient(), FlagBit.RESULT | FlagBit.NO_DELAY, "clone", "copy", "copyingredient"); // TODO finish
        loadFlag(FlagType.GET_RECIPE_BOOK, new FlagGetRecipeBook(), FlagBit.RESULT, "getbook", "bookresult");

        // Ingredient or Result flags
        if (Supports.axolotlBucketMeta()) {
            loadFlag(FlagType.AXOLOTL_BUCKET_ITEM, new FlagAxolotlBucketItem(), APPLIED_TO_ITEM, "axolotlbucket");
        }
        loadFlag(FlagType.BANNER_ITEM, new FlagBannerItem(), APPLIED_TO_ITEM, "banner");
        loadFlag(FlagType.BOOK_ITEM, new FlagBookItem(), APPLIED_TO_ITEM, "book");
        if (Version.has1_17Support()) {
            loadFlag(FlagType.BUNDLE_ITEM, new FlagBundleItem(), APPLIED_TO_ITEM, "bundle");
        }
        if (Supports.compassMeta()) {
            loadFlag(FlagType.COMPASS_ITEM, new FlagCompassItem(), APPLIED_TO_ITEM, "compass");
        }
        if (Version.has1_16Support()) {
            loadFlag(FlagType.CROSSBOW_ITEM, new FlagCrossbowItem(), APPLIED_TO_ITEM, "crossbow");
        }
        if (Version.has1_14Support()) {
            loadFlag(FlagType.CUSTOM_MODEL_DATA, new FlagCustomModelData(), APPLIED_TO_ITEM, "modeldata");
        }
        loadFlag(FlagType.ENCHANTED_BOOK, new FlagEnchantedBook(), APPLIED_TO_ITEM, "enchantbook", "enchantingbook");
        loadFlag(FlagType.ENCHANT_ITEM, new FlagEnchantItem(), APPLIED_TO_ITEM, "enchant", "enchantment");
        loadFlag(FlagType.FIREWORK_ITEM, new FlagFireworkItem(), APPLIED_TO_ITEM, "firework", "fireworkrocket");
        loadFlag(FlagType.FIREWORK_STAR_ITEM, new FlagFireworkStarItem(), APPLIED_TO_ITEM, "fireworkstar", "fireworkchargeitem", "fireworkcharge", "fireworkeffect");
        loadFlag(FlagType.HIDE, new FlagHide(), APPLIED_TO_ITEM);
        if (Version.has1_13BasicSupport()) {
            loadFlag(FlagType.ITEM_ATTRIBUTE, new FlagItemAttribute(), APPLIED_TO_ITEM, "attribute");
        }
        if (Version.has1_11Support()) {
            loadFlag(FlagType.ITEM_UNBREAKABLE, new FlagItemUnbreakable(), APPLIED_TO_ITEM | FlagBit.NO_FALSE | FlagBit.NO_VALUE_REQUIRED, "unbreakable");
        }
        if (Supports.knowledgeBookMeta()) {
            loadFlag(FlagType.KNOWLEDGE_BOOK_ITEM, new FlagKnowledgeBookItem(), APPLIED_TO_ITEM, "knowledgebook");
        }
        loadFlag(FlagType.LEATHER_COLOR, new FlagLeatherColor(), APPLIED_TO_ITEM, "leathercolour", "color", "colour");
        loadFlag(FlagType.LOCALIZED_NAME, new FlagLocalizedName(), APPLIED_TO_ITEM);
        loadFlag(FlagType.MAP_ITEM, new FlagMapItem(), APPLIED_TO_ITEM, "map");
        loadFlag(FlagType.MONSTER_SPAWNER, new FlagMonsterSpawner(), APPLIED_TO_ITEM, "spawner", "mobspawner");
        loadFlag(FlagType.POTION_ITEM, new FlagPotionItem(), APPLIED_TO_ITEM, "potion");
        loadFlag(FlagType.REPAIR_COST, new FlagRepairCost(), APPLIED_TO_ITEM);
        loadFlag(FlagType.SKULL_OWNER, new FlagSkullOwner(), APPLIED_TO_ITEM, "skullitem, skull, head");
        if (!Version.has1_13BasicSupport() || force) {
            loadFlag(FlagType.SPAWN_EGG, new FlagSpawnEgg(), APPLIED_TO_ITEM, "monsteregg", "egg");
        }
        loadFlag(FlagType.STORE_ENCHANTMENT, new FlagStoreEnchantment(), FlagBit.RESULT | FlagBit.NO_VALUE_REQUIRED | FlagBit.NO_DELAY, "storeenchant", "storeenchantments", "storeenchants");
        if (Supports.suspiciousStewMeta()) {
            loadFlag(FlagType.SUSPICIOUS_STEW_ITEM, new FlagSuspiciousStewItem(), APPLIED_TO_ITEM, "suspicioussoupitem", "suspiciousstew", "suspicioussoup");
        }
        if (Supports.tropicalFishBucketMeta()) {
            loadFlag(FlagType.TROPICAL_FISH_BUCKET_ITEM, new FlagTropicalFishBucketItem(), APPLIED_TO_ITEM, "tropicalfishbucket", "fishbucket");
        }
    }

    public void loadFlag(String mainAlias, Flag newFlag, String aliases) {
        loadFlag(mainAlias, newFlag, FlagBit.NONE, aliases);
    }

    public void loadFlag(String mainAlias, Flag newFlag, int bits, String... aliases) {
        if (FlagFactory.getInstance().isInitialized()) {
            MessageSender.getInstance().info(ChatColor.RED + "Custom flags must be added in your onEnable() method.");
        } else {
            FlagFactory.getInstance().initializeFlag(mainAlias, newFlag, bits, aliases);
        }
    }
}
