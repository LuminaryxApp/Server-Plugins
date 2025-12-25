package com.luminary.mines.listener;

import com.luminary.mines.LuminaryMines;
import com.luminary.mines.gui.MineGUI;
import com.luminary.mines.gui.MineMenuHolder;
import com.luminary.mines.mine.Mine;
import com.luminary.mines.util.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

/**
 * Handles mine GUI interactions.
 */
public class MineListener implements Listener {

    private final LuminaryMines plugin;
    private final MineGUI gui;

    public MineListener(LuminaryMines plugin) {
        this.plugin = plugin;
        this.gui = new MineGUI(plugin);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof MineMenuHolder menuHolder)) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        int slot = event.getRawSlot();
        Mine mine = menuHolder.getMine();

        switch (menuHolder.getType()) {
            case MAIN_MENU -> handleMainMenu(player, mine, slot);
            case SETTINGS -> handleSettingsMenu(player, mine, slot, event.isRightClick());
            case WHITELIST -> handleWhitelistMenu(player, mine, slot, menuHolder.getPage());
            case BLOCKS -> handleBlocksMenu(player, mine, slot);
            case CONFIRM_DELETE -> handleDeleteConfirm(player, mine, slot);
        }
    }

    private void handleMainMenu(Player player, Mine mine, int slot) {
        switch (slot) {
            case 11 -> { // Teleport
                player.closeInventory();
                org.bukkit.Location spawn = mine.getSpawnLocation();
                if (spawn != null && spawn.getWorld() != null) {
                    player.teleport(spawn);
                    player.sendMessage(TextUtil.colorize(plugin.getConfigManager().getMessage("mine.teleported")));
                }
            }
            case 15 -> { // Reset
                player.closeInventory();
                player.sendMessage(TextUtil.colorize("&eResetting your mine..."));
                plugin.getResetTask().forceReset(mine);
            }
            case 29 -> gui.openSettingsMenu(player, mine); // Settings
            case 31 -> gui.openWhitelistMenu(player, mine, 0); // Whitelist
            case 33 -> gui.openBlocksMenu(player, mine); // Blocks
            case 40 -> player.closeInventory(); // Close
        }
    }

    private void handleSettingsMenu(Player player, Mine mine, int slot, boolean rightClick) {
        switch (slot) {
            case 11 -> { // Toggle auto-reset
                mine.setAutoReset(!mine.isAutoReset());
                plugin.getMineManager().saveMine(mine);
                gui.openSettingsMenu(player, mine);
            }
            case 13 -> { // Reset interval
                int change = rightClick ? -60 : 60; // +/- 1 minute
                int newInterval = Math.max(60, mine.getResetInterval() + change);
                mine.setResetInterval(newInterval);
                plugin.getMineManager().saveMine(mine);
                gui.openSettingsMenu(player, mine);
            }
            case 15 -> { // Set spawn
                mine.setSpawn(player.getLocation());
                plugin.getMineManager().saveMine(mine);
                player.sendMessage(TextUtil.colorize("&aSpawn point set to your current location!"));
                gui.openSettingsMenu(player, mine);
            }
            case 22 -> gui.openMainMenu(player, mine); // Back
        }
    }

    private void handleWhitelistMenu(Player player, Mine mine, int slot, int page) {
        if (slot == 45) { // Back
            gui.openMainMenu(player, mine);
        } else if (slot == 48 && page > 0) { // Previous page
            gui.openWhitelistMenu(player, mine, page - 1);
        } else if (slot == 50) { // Next page
            gui.openWhitelistMenu(player, mine, page + 1);
        } else if (slot == 49) { // Add player
            player.closeInventory();
            player.sendMessage(TextUtil.colorize("&eType the player name in chat to add them:"));
            // Would need a chat listener for this - simplified for now
            player.sendMessage(TextUtil.colorize("&7Use: &e/mine whitelist add <player>"));
        } else if (slot >= 10 && slot < 44) { // Player head clicked - remove
            // Get the UUID from whitelist based on slot
            java.util.List<java.util.UUID> whitelist = new java.util.ArrayList<>(mine.getWhitelist());
            int startIndex = page * 28;
            int adjustedSlot = slot;

            // Calculate actual index
            int itemIndex = 0;
            int checkSlot = 10;
            while (checkSlot < slot) {
                if (checkSlot % 9 != 0 && checkSlot % 9 != 8) {
                    itemIndex++;
                }
                checkSlot++;
            }

            int actualIndex = startIndex + itemIndex;
            if (actualIndex < whitelist.size()) {
                java.util.UUID toRemove = whitelist.get(actualIndex);
                mine.removeFromWhitelist(toRemove);
                plugin.getMineManager().saveMine(mine);
                player.sendMessage(TextUtil.colorize("&aRemoved player from whitelist!"));
                gui.openWhitelistMenu(player, mine, page);
            }
        }
    }

    private void handleBlocksMenu(Player player, Mine mine, int slot) {
        if (slot == 36) { // Back
            gui.openMainMenu(player, mine);
        }
    }

    private void handleDeleteConfirm(Player player, Mine mine, int slot) {
        if (slot == 11) { // Confirm
            player.closeInventory();
            plugin.getMineManager().deleteMine(player.getUniqueId());
            player.sendMessage(TextUtil.colorize("&cYour mine has been deleted!"));
        } else if (slot == 15) { // Cancel
            gui.openMainMenu(player, mine);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof MineMenuHolder) {
            event.setCancelled(true);
        }
    }
}
