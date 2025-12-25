package com.luminary.enchants.gui;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.api.BeaconEffectProvider;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.pickaxe.EnchantRarity;
import com.luminary.enchants.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Detailed view of a single enchant with upgrade options.
 */
public class EnchantDetailMenu extends AbstractMenu {

    private static final int SLOT_INFO = 13;
    private static final int SLOT_UPGRADE_1 = 29;
    private static final int SLOT_UPGRADE_5 = 30;
    private static final int SLOT_UPGRADE_10 = 32;
    private static final int SLOT_UPGRADE_20 = 33;
    private static final int SLOT_BACK = 45;

    private final String enchantId;
    private final EnchantDefinition definition;

    public EnchantDetailMenu(LuminaryEnchants plugin, Player player, String enchantId) {
        super(plugin, player, 54, "&5&l" + getEnchantTitle(plugin, enchantId));
        this.enchantId = enchantId;
        this.definition = plugin.getEnchantRegistry().getEnchant(enchantId);
    }

    private static String getEnchantTitle(LuminaryEnchants plugin, String enchantId) {
        EnchantDefinition def = plugin.getEnchantRegistry().getEnchant(enchantId);
        return def != null ? def.getDisplayName() : enchantId;
    }

    @Override
    protected void init() {
        if (definition == null) {
            player.closeInventory();
            player.sendMessage(TextUtil.colorize("&cEnchant not found!"));
            return;
        }

        fillBorder(Material.PURPLE_STAINED_GLASS_PANE);

        ItemStack pickaxe = player.getInventory().getItemInMainHand();
        int currentLevel = plugin.getPickaxeDataManager().getEnchantLevel(pickaxe, enchantId);
        int maxLevel = definition.getMaxLevel();

        // Main info display
        inventory.setItem(SLOT_INFO, createInfoItem(currentLevel));

        // Upgrade buttons
        boolean economyAvailable = plugin.getHookManager().isTokenEconomyAvailable();
        long balance = economyAvailable ?
                plugin.getHookManager().getTokenEconomy().get(player.getUniqueId()) : 0;

        inventory.setItem(SLOT_UPGRADE_1, createUpgradeButton(1, currentLevel, maxLevel, balance, economyAvailable));
        inventory.setItem(SLOT_UPGRADE_5, createUpgradeButton(5, currentLevel, maxLevel, balance, economyAvailable));
        inventory.setItem(SLOT_UPGRADE_10, createUpgradeButton(10, currentLevel, maxLevel, balance, economyAvailable));
        inventory.setItem(SLOT_UPGRADE_20, createUpgradeButton(20, currentLevel, maxLevel, balance, economyAvailable));

        // Token balance
        if (economyAvailable) {
            inventory.setItem(4, createItem(Material.SUNFLOWER,
                    "&6&lTokens",
                    "&7Balance: &e" + TextUtil.formatNumber(balance)));
        }

        // Back button
        inventory.setItem(SLOT_BACK, createBackButton());
    }

    private ItemStack createInfoItem(int currentLevel) {
        String rarityColor = getRarityColor(definition.getRarity());
        List<String> lore = new ArrayList<>();

        lore.add(rarityColor + definition.getRarity().name());
        lore.add("");
        lore.add("&7" + definition.getDescription());
        lore.add("");
        lore.add("&fCurrent Level: " + (currentLevel > 0 ? "&a" + currentLevel : "&c0") +
                " &7/ &f" + definition.getMaxLevel());

        // Proc chance with beacon multiplier
        double beaconMultiplier = plugin.getHookManager().getBeaconProvider()
                .multiplier(player.getUniqueId(), BeaconEffectProvider.PROC_CHANCE);
        double procChance = definition.calculateProcChance(Math.max(1, currentLevel), beaconMultiplier);

        lore.add("");
        lore.add("&fProc Chance: &b" + TextUtil.formatPercent(procChance));
        if (beaconMultiplier > 1.0) {
            lore.add("  &7(includes &d" + TextUtil.formatPercent(beaconMultiplier - 1) + " &7beacon bonus)");
        }

        if (definition.getCooldownMs() > 0) {
            long cooldown = definition.getCooldownMs();
            double cooldownReduction = plugin.getHookManager().getBeaconProvider()
                    .multiplier(player.getUniqueId(), BeaconEffectProvider.COOLDOWN_REDUCTION);
            if (cooldownReduction < 1.0 && cooldownReduction > 0) {
                cooldown = (long) (cooldown * cooldownReduction);
            }
            lore.add("&fCooldown: &e" + TextUtil.formatDuration(cooldown));
        }

        // Conflicts
        if (!definition.getConflicts().isEmpty()) {
            lore.add("");
            lore.add("&cConflicts with:");
            for (String conflict : definition.getConflicts()) {
                EnchantDefinition conflictDef = plugin.getEnchantRegistry().getEnchant(conflict);
                String name = conflictDef != null ? conflictDef.getDisplayName() : conflict;
                lore.add("  &c- " + name);
            }
        }

        Material material = getMaterialForRarity(definition.getRarity());
        return createItem(material, rarityColor + "&l" + definition.getDisplayName(), lore);
    }

    private ItemStack createUpgradeButton(int levels, int currentLevel, int maxLevel,
                                           long balance, boolean economyAvailable) {
        int levelsRemaining = maxLevel - currentLevel;
        int actualLevels = Math.min(levels, levelsRemaining);

        if (actualLevels <= 0 || currentLevel >= maxLevel) {
            return createItem(Material.GRAY_DYE, "&7+" + levels + " Levels",
                    "&cAlready at max level!");
        }

        long cost = definition.calculateUpgradeCost(currentLevel, actualLevels);
        boolean canAfford = economyAvailable && balance >= cost;

        List<String> lore = new ArrayList<>();
        lore.add("&7Upgrade from level &f" + currentLevel + " &7to &f" + (currentLevel + actualLevels));
        lore.add("");
        lore.add("&fCost: " + (canAfford ? "&a" : "&c") + TextUtil.formatNumber(cost) + " tokens");

        if (definition.isSuccessChanceEnabled()) {
            double successChance = definition.calculateSuccessChance(currentLevel + actualLevels);
            lore.add("&fSuccess Chance: &e" + TextUtil.formatPercent(successChance));
            lore.add("&7Failure: " + definition.getFailMode().name().toLowerCase().replace("_", " "));
        }

        lore.add("");
        if (!economyAvailable) {
            lore.add("&cEconomy unavailable!");
        } else if (!canAfford) {
            lore.add("&cInsufficient tokens!");
        } else {
            lore.add("&aClick to upgrade!");
        }

        Material material = canAfford ? Material.LIME_DYE : Material.RED_DYE;
        String title = (canAfford ? "&a" : "&c") + "+" + actualLevels + " Level" + (actualLevels > 1 ? "s" : "");

        return createItem(material, title, lore);
    }

    private Material getMaterialForRarity(EnchantRarity rarity) {
        return switch (rarity) {
            case COMMON -> Material.BOOK;
            case UNCOMMON -> Material.WRITABLE_BOOK;
            case RARE -> Material.ENCHANTED_BOOK;
            case EPIC -> Material.KNOWLEDGE_BOOK;
            case LEGENDARY -> Material.NETHER_STAR;
        };
    }

    private String getRarityColor(EnchantRarity rarity) {
        return switch (rarity) {
            case COMMON -> "&f";
            case UNCOMMON -> "&a";
            case RARE -> "&b";
            case EPIC -> "&5";
            case LEGENDARY -> "&6";
        };
    }

    @Override
    public void handleClick(int slot, ClickType clickType) {
        if (slot == SLOT_BACK) {
            plugin.getMenuManager().openEnchantListMenu(player);
            return;
        }

        int levels = switch (slot) {
            case SLOT_UPGRADE_1 -> 1;
            case SLOT_UPGRADE_5 -> 5;
            case SLOT_UPGRADE_10 -> 10;
            case SLOT_UPGRADE_20 -> 20;
            default -> 0;
        };

        if (levels > 0) {
            // Validate pickaxe is still in hand
            ItemStack pickaxe = player.getInventory().getItemInMainHand();
            if (!plugin.getPickaxeDataManager().isPickaxe(pickaxe)) {
                player.sendMessage(TextUtil.colorize("&cYou must hold a pickaxe to upgrade!"));
                return;
            }

            int currentLevel = plugin.getPickaxeDataManager().getEnchantLevel(pickaxe, enchantId);
            int actualLevels = Math.min(levels, definition.getMaxLevel() - currentLevel);

            if (actualLevels <= 0) {
                player.sendMessage(TextUtil.colorize("&cThis enchant is already at max level!"));
                return;
            }

            plugin.getMenuManager().openUpgradeConfirmMenu(player, enchantId, actualLevels);
        }
    }
}
