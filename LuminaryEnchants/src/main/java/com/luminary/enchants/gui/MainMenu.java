package com.luminary.enchants.gui;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Main menu for the pickaxe enchant system.
 */
public class MainMenu extends AbstractMenu {

    private static final int SLOT_PICKAXE_PREVIEW = 13;
    private static final int SLOT_ENCHANTS = 20;
    private static final int SLOT_MY_PICKAXE = 22;
    private static final int SLOT_HELP = 24;
    private static final int SLOT_CLOSE = 40;

    public MainMenu(LuminaryEnchants plugin, Player player) {
        super(plugin, player, 45, "&5&lPickaxe Enchants");
    }

    @Override
    protected void init() {
        fillBorder(Material.PURPLE_STAINED_GLASS_PANE);

        // Pickaxe preview
        ItemStack pickaxe = player.getInventory().getItemInMainHand();
        if (plugin.getPickaxeDataManager().isPickaxe(pickaxe)) {
            inventory.setItem(SLOT_PICKAXE_PREVIEW, pickaxe.clone());
        } else {
            inventory.setItem(SLOT_PICKAXE_PREVIEW, createItem(Material.IRON_PICKAXE,
                    "&c&lNo Pickaxe",
                    "&7Hold a pickaxe in your main hand",
                    "&7to view and upgrade enchants."));
        }

        // Token balance display
        if (plugin.getHookManager().isTokenEconomyAvailable()) {
            long balance = plugin.getHookManager().getTokenEconomy().get(player.getUniqueId());
            inventory.setItem(4, createItem(Material.SUNFLOWER,
                    "&6&lTokens",
                    "&7Balance: &e" + TextUtil.formatNumber(balance)));
        }

        // Enchants button
        inventory.setItem(SLOT_ENCHANTS, createItem(Material.ENCHANTED_BOOK,
                "&b&lEnchants",
                "&7View and upgrade your",
                "&7pickaxe enchantments.",
                "",
                "&eClick to browse enchants!"));

        // My Pickaxe button
        List<String> pickaxeLore = new ArrayList<>();
        pickaxeLore.add("&7View detailed stats about");
        pickaxeLore.add("&7your current pickaxe.");
        pickaxeLore.add("");

        if (plugin.getPickaxeDataManager().isPickaxe(pickaxe)) {
            Map<String, Integer> enchants = plugin.getPickaxeDataManager().getEnchants(pickaxe);
            pickaxeLore.add("&fEnchants: &a" + enchants.size());
            int totalLevels = enchants.values().stream().mapToInt(Integer::intValue).sum();
            pickaxeLore.add("&fTotal Levels: &a" + totalLevels);
        }
        pickaxeLore.add("");
        pickaxeLore.add("&eClick to view details!");

        inventory.setItem(SLOT_MY_PICKAXE, createItem(Material.DIAMOND_PICKAXE,
                "&d&lMy Pickaxe", pickaxeLore));

        // Help button
        inventory.setItem(SLOT_HELP, createItem(Material.BOOK,
                "&a&lHelp / Info",
                "&7Learn about the enchant system",
                "&7and how to use it effectively.",
                "",
                "&eClick for help!"));

        // Close button
        inventory.setItem(SLOT_CLOSE, createCloseButton());
    }

    @Override
    public void handleClick(int slot, ClickType clickType) {
        switch (slot) {
            case SLOT_ENCHANTS -> {
                // Check if holding pickaxe
                if (!plugin.getPickaxeDataManager().isPickaxe(
                        player.getInventory().getItemInMainHand())) {
                    player.sendMessage(TextUtil.colorize(
                            "&cYou must hold a pickaxe to browse enchants!"));
                    return;
                }
                plugin.getMenuManager().openEnchantListMenu(player);
            }
            case SLOT_MY_PICKAXE -> {
                // Show pickaxe details (reopen main menu for now)
                player.sendMessage(TextUtil.colorize("&7Pickaxe details coming soon!"));
            }
            case SLOT_HELP -> {
                player.sendMessage(TextUtil.colorize(""));
                player.sendMessage(TextUtil.colorize("&5&l=== Pickaxe Enchants Help ==="));
                player.sendMessage(TextUtil.colorize("&7Right-click with a pickaxe to open this menu."));
                player.sendMessage(TextUtil.colorize("&7Browse enchants and upgrade them using tokens."));
                player.sendMessage(TextUtil.colorize("&7Each enchant has a chance to proc when mining."));
                player.sendMessage(TextUtil.colorize("&7Higher levels = better proc chance and effects!"));
                player.sendMessage(TextUtil.colorize(""));
            }
            case SLOT_CLOSE -> player.closeInventory();
        }
    }
}
