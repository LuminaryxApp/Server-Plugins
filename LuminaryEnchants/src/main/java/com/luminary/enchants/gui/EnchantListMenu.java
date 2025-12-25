package com.luminary.enchants.gui;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.pickaxe.EnchantDefinition;
import com.luminary.enchants.pickaxe.EnchantRarity;
import com.luminary.enchants.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Menu listing all available pickaxe enchants.
 */
public class EnchantListMenu extends AbstractMenu {

    private static final int SLOT_BACK = 45;
    private static final int[] ENCHANT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final List<EnchantDefinition> enchantList;
    private int page = 0;

    public EnchantListMenu(LuminaryEnchants plugin, Player player) {
        super(plugin, player, 54, "&5&lPickaxe Enchants");
        this.enchantList = plugin.getEnchantRegistry().getEnchantsSortedByRarity();
    }

    @Override
    protected void init() {
        inventory.clear();
        fillBorder(Material.PURPLE_STAINED_GLASS_PANE);

        ItemStack pickaxe = player.getInventory().getItemInMainHand();
        Map<String, Integer> playerEnchants = plugin.getPickaxeDataManager().getEnchants(pickaxe);

        // Display enchants for current page
        int startIndex = page * ENCHANT_SLOTS.length;
        int endIndex = Math.min(startIndex + ENCHANT_SLOTS.length, enchantList.size());

        for (int i = 0; i < ENCHANT_SLOTS.length; i++) {
            int enchantIndex = startIndex + i;
            if (enchantIndex >= enchantList.size()) {
                break;
            }

            EnchantDefinition def = enchantList.get(enchantIndex);
            int currentLevel = playerEnchants.getOrDefault(def.getId(), 0);

            inventory.setItem(ENCHANT_SLOTS[i], createEnchantItem(def, currentLevel));
        }

        // Navigation
        inventory.setItem(SLOT_BACK, createBackButton());

        if (page > 0) {
            inventory.setItem(48, createItem(Material.ARROW, "&aPrevious Page",
                    "&7Click to go to page " + page));
        }

        int totalPages = (int) Math.ceil((double) enchantList.size() / ENCHANT_SLOTS.length);
        if (page < totalPages - 1) {
            inventory.setItem(50, createItem(Material.ARROW, "&aNext Page",
                    "&7Click to go to page " + (page + 2)));
        }

        // Page indicator
        inventory.setItem(49, createItem(Material.PAPER, "&7Page " + (page + 1) + "/" + totalPages));
    }

    private ItemStack createEnchantItem(EnchantDefinition def, int currentLevel) {
        Material material = getMaterialForRarity(def.getRarity());
        String rarityColor = getRarityColor(def.getRarity());

        List<String> lore = new ArrayList<>();
        lore.add(rarityColor + def.getRarity().name());
        lore.add("");
        lore.add("&7" + def.getDescription());
        lore.add("");
        lore.add("&fLevel: " + (currentLevel > 0 ? "&a" + currentLevel : "&c0") +
                " &7/ &f" + def.getMaxLevel());

        // Show proc chance
        double procChance = def.calculateProcChance(Math.max(1, currentLevel), 1.0);
        lore.add("&fProc Chance: &b" + TextUtil.formatPercent(procChance));

        if (def.getCooldownMs() > 0) {
            lore.add("&fCooldown: &e" + TextUtil.formatDuration(def.getCooldownMs()));
        }

        lore.add("");

        if (currentLevel < def.getMaxLevel()) {
            long nextCost = def.calculateUpgradeCost(currentLevel, 1);
            lore.add("&7Next Upgrade: &e" + TextUtil.formatNumber(nextCost) + " tokens");
        } else {
            lore.add("&a&lMAX LEVEL");
        }

        lore.add("");
        lore.add("&eClick to view details!");

        return createItem(material, rarityColor + "&l" + def.getDisplayName(), lore);
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
            plugin.getMenuManager().openMainMenu(player);
            return;
        }

        if (slot == 48 && page > 0) {
            page--;
            refresh();
            return;
        }

        int totalPages = (int) Math.ceil((double) enchantList.size() / ENCHANT_SLOTS.length);
        if (slot == 50 && page < totalPages - 1) {
            page++;
            refresh();
            return;
        }

        // Check if clicking an enchant slot
        for (int i = 0; i < ENCHANT_SLOTS.length; i++) {
            if (slot == ENCHANT_SLOTS[i]) {
                int enchantIndex = page * ENCHANT_SLOTS.length + i;
                if (enchantIndex < enchantList.size()) {
                    EnchantDefinition def = enchantList.get(enchantIndex);
                    plugin.getMenuManager().openEnchantDetailMenu(player, def.getId());
                }
                return;
            }
        }
    }
}
