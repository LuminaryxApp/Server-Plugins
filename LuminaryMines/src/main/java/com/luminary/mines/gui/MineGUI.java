package com.luminary.mines.gui;

import com.luminary.mines.LuminaryMines;
import com.luminary.mines.mine.Mine;
import com.luminary.mines.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * Handles all mine GUI menus.
 */
public class MineGUI {

    private final LuminaryMines plugin;

    public MineGUI(LuminaryMines plugin) {
        this.plugin = plugin;
    }

    /**
     * Open the main mine menu.
     */
    public void openMainMenu(Player player, Mine mine) {
        MineMenuHolder holder = new MineMenuHolder(MineMenuHolder.MenuType.MAIN_MENU, mine);
        Inventory inv = Bukkit.createInventory(holder, 45, TextUtil.colorize("&6&lYour Private Mine"));
        holder.setInventory(inv);

        // Fill border
        fillBorder(inv, Material.GRAY_STAINED_GLASS_PANE);

        // Teleport button
        inv.setItem(11, createItem(Material.ENDER_PEARL, "&a&lTeleport to Mine",
                "&7Click to teleport to your mine",
                "",
                "&eClick to teleport!"));

        // Mine info
        double minedPercent = plugin.getResetTask().getMinedPercentage(mine);
        long resetTime = mine.getTimeUntilReset() / 1000;
        inv.setItem(13, createItem(Material.DIAMOND_PICKAXE, "&b&lMine Info",
                "&7Owner: &f" + mine.getOwnerName(),
                "&7Tier: &e" + mine.getTier(),
                "&7Mined: &f" + TextUtil.formatPercentage(minedPercent),
                "&7Reset in: &f" + TextUtil.formatTime(resetTime),
                "",
                "&7Schematic: &f" + mine.getSchematicName()));

        // Reset button
        inv.setItem(15, createItem(Material.TNT, "&c&lReset Mine",
                "&7Click to manually reset your mine",
                "&7This will refill all blocks.",
                "",
                "&eClick to reset!"));

        // Settings button
        inv.setItem(29, createItem(Material.COMPARATOR, "&e&lSettings",
                "&7Configure your mine settings",
                "",
                "&eClick to open!"));

        // Whitelist button
        int whitelistSize = mine.getWhitelist().size();
        int maxWhitelist = plugin.getConfigManager().getMaxWhitelistSize();
        inv.setItem(31, createItem(Material.PLAYER_HEAD, "&d&lWhitelist",
                "&7Manage who can access your mine",
                "&7Players: &f" + whitelistSize + "&7/&f" + maxWhitelist,
                "",
                "&eClick to manage!"));

        // Blocks button
        inv.setItem(33, createItem(Material.COAL_ORE, "&6&lBlock Composition",
                "&7View and upgrade your mine blocks",
                "",
                "&eClick to view!"));

        // Close button
        inv.setItem(40, createItem(Material.BARRIER, "&c&lClose",
                "&7Close this menu"));

        player.openInventory(inv);
    }

    /**
     * Open the settings menu.
     */
    public void openSettingsMenu(Player player, Mine mine) {
        MineMenuHolder holder = new MineMenuHolder(MineMenuHolder.MenuType.SETTINGS, mine);
        Inventory inv = Bukkit.createInventory(holder, 27, TextUtil.colorize("&e&lMine Settings"));
        holder.setInventory(inv);

        fillBorder(inv, Material.YELLOW_STAINED_GLASS_PANE);

        // Auto-reset toggle
        Material toggleMat = mine.isAutoReset() ? Material.LIME_DYE : Material.GRAY_DYE;
        String toggleStatus = mine.isAutoReset() ? "&aEnabled" : "&cDisabled";
        inv.setItem(11, createItem(toggleMat, "&b&lAuto Reset",
                "&7Automatically reset your mine",
                "&7Status: " + toggleStatus,
                "",
                "&eClick to toggle!"));

        // Reset interval
        inv.setItem(13, createItem(Material.CLOCK, "&6&lReset Interval",
                "&7Time between automatic resets",
                "&7Current: &f" + TextUtil.formatTime(mine.getResetInterval()),
                "",
                "&eLeft-click: +1 minute",
                "&eRight-click: -1 minute"));

        // Set spawn
        inv.setItem(15, createItem(Material.RED_BED, "&a&lSet Spawn Point",
                "&7Set where you teleport to",
                "&7in your mine",
                "",
                "&eClick to set spawn here!"));

        // Back button
        inv.setItem(22, createItem(Material.ARROW, "&7&lBack",
                "&7Return to main menu"));

        player.openInventory(inv);
    }

    /**
     * Open the whitelist menu.
     */
    public void openWhitelistMenu(Player player, Mine mine, int page) {
        MineMenuHolder holder = new MineMenuHolder(MineMenuHolder.MenuType.WHITELIST, mine);
        holder.setPage(page);
        Inventory inv = Bukkit.createInventory(holder, 54,
                TextUtil.colorize("&d&lMine Whitelist &7(Page " + (page + 1) + ")"));
        holder.setInventory(inv);

        fillBorder(inv, Material.PINK_STAINED_GLASS_PANE);

        // Add player heads for whitelisted players
        List<UUID> whitelist = new ArrayList<>(mine.getWhitelist());
        int startIndex = page * 28;
        int slot = 10;

        for (int i = startIndex; i < Math.min(startIndex + 28, whitelist.size()); i++) {
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;
            if (slot >= 45) break;

            UUID uuid = whitelist.get(i);
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(offlinePlayer);
                String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";
                meta.displayName(TextUtil.colorize("&e" + name));
                List<Component> lore = new ArrayList<>();
                lore.add(TextUtil.colorize("&7Click to remove from whitelist"));
                meta.lore(lore);
                head.setItemMeta(meta);
            }

            inv.setItem(slot, head);
            slot++;
        }

        // Add player button
        inv.setItem(49, createItem(Material.EMERALD, "&a&lAdd Player",
                "&7Type a player name in chat",
                "&7to add them to your whitelist",
                "",
                "&eClick to add!"));

        // Navigation
        if (page > 0) {
            inv.setItem(48, createItem(Material.ARROW, "&7&lPrevious Page", ""));
        }
        if (startIndex + 28 < whitelist.size()) {
            inv.setItem(50, createItem(Material.ARROW, "&7&lNext Page", ""));
        }

        // Back button
        inv.setItem(45, createItem(Material.BARRIER, "&c&lBack",
                "&7Return to main menu"));

        player.openInventory(inv);
    }

    /**
     * Open the block composition menu.
     */
    public void openBlocksMenu(Player player, Mine mine) {
        MineMenuHolder holder = new MineMenuHolder(MineMenuHolder.MenuType.BLOCKS, mine);
        Inventory inv = Bukkit.createInventory(holder, 45,
                TextUtil.colorize("&6&lBlock Composition"));
        holder.setInventory(inv);

        fillBorder(inv, Material.ORANGE_STAINED_GLASS_PANE);

        // Show current block composition
        Map<String, Integer> blocks = mine.getBlockComposition();
        int slot = 10;

        for (Map.Entry<String, Integer> entry : blocks.entrySet()) {
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) slot += 2;
            if (slot >= 36) break;

            Material material = Material.matchMaterial(entry.getKey());
            if (material != null) {
                inv.setItem(slot, createItem(material,
                        "&f" + formatMaterialName(entry.getKey()),
                        "&7Chance: &e" + entry.getValue() + "%"));
                slot++;
            }
        }

        // Info
        inv.setItem(40, createItem(Material.BOOK, "&e&lBlock Info",
                "&7These are the blocks that",
                "&7spawn when your mine resets.",
                "",
                "&7Upgrade your mine tier to",
                "&7unlock better ores!"));

        // Back button
        inv.setItem(36, createItem(Material.ARROW, "&7&lBack",
                "&7Return to main menu"));

        player.openInventory(inv);
    }

    /**
     * Open delete confirmation menu.
     */
    public void openDeleteConfirmMenu(Player player, Mine mine) {
        MineMenuHolder holder = new MineMenuHolder(MineMenuHolder.MenuType.CONFIRM_DELETE, mine);
        Inventory inv = Bukkit.createInventory(holder, 27,
                TextUtil.colorize("&c&lDelete Mine?"));
        holder.setInventory(inv);

        fillBorder(inv, Material.RED_STAINED_GLASS_PANE);

        // Confirm
        inv.setItem(11, createItem(Material.LIME_WOOL, "&a&lConfirm Delete",
                "&7This will permanently delete",
                "&7your mine and all progress!",
                "",
                "&cThis cannot be undone!"));

        // Cancel
        inv.setItem(15, createItem(Material.RED_WOOL, "&c&lCancel",
                "&7Go back to the main menu"));

        player.openInventory(inv);
    }

    // Helper methods

    private void fillBorder(Inventory inv, Material material) {
        ItemStack glass = createItem(material, " ");
        int size = inv.getSize();

        // Top and bottom rows
        for (int i = 0; i < 9; i++) {
            inv.setItem(i, glass);
            inv.setItem(size - 9 + i, glass);
        }

        // Left and right columns
        for (int i = 9; i < size - 9; i += 9) {
            inv.setItem(i, glass);
            inv.setItem(i + 8, glass);
        }
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(TextUtil.colorize(name));

            if (lore.length > 0) {
                List<Component> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(TextUtil.colorize("&7" + line.replace("&7", "")));
                }
                meta.lore(loreList);
            }

            item.setItemMeta(meta);
        }

        return item;
    }

    private String formatMaterialName(String material) {
        String[] parts = material.toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1))
                        .append(" ");
            }
        }
        return sb.toString().trim();
    }
}
