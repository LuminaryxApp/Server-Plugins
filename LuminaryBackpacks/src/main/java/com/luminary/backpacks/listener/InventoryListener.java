package com.luminary.backpacks.listener;

import com.luminary.backpacks.LuminaryBackpacks;
import com.luminary.backpacks.data.PlayerBackpackData;
import com.luminary.backpacks.gui.BackpackHolder;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.text.NumberFormat;
import java.util.Locale;

public class InventoryListener implements Listener {

    private final LuminaryBackpacks plugin;
    private static final NumberFormat FORMATTER = NumberFormat.getInstance(Locale.US);

    public InventoryListener(LuminaryBackpacks plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof BackpackHolder bpHolder)) return;

        // Handle backpack inventory - allow normal interaction
        if (bpHolder.isBackpack()) {
            // Allow clicks within the backpack
            return;
        }

        // Cancel clicks for other menus
        event.setCancelled(true);

        int slot = event.getRawSlot();

        if (bpHolder.isUpgradeMenu()) {
            handleUpgradeClick(player, slot);
        } else if (bpHolder.isSettingsMenu()) {
            handleSettingsClick(player, slot);
        }
    }

    private void handleUpgradeClick(Player player, int slot) {
        if (slot == 15) {
            // Upgrade button
            PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            if (data == null) return;

            int currentTier = data.getTier();
            int maxTier = plugin.getConfigManager().getMaxTier();

            if (currentTier >= maxTier) {
                sendMessage(player, plugin.getConfigManager().getMessage("upgrade-max"));
                return;
            }

            long cost = plugin.getConfigManager().getUpgradeCost(currentTier);
            if (!plugin.getEconomyHook().hasBalance(player, cost)) {
                sendMessage(player, plugin.getConfigManager().getMessage("upgrade-no-money",
                        "{cost}", FORMATTER.format(cost)));
                return;
            }

            if (plugin.getBackpackManager().upgradeTier(player)) {
                int newTier = data.getTier();
                String tierName = plugin.getConfigManager().getTierName(newTier);
                sendMessage(player, plugin.getConfigManager().getMessage("upgrade-success",
                        "{tier}", tierName));
                // Refresh menu
                plugin.getBackpackGUI().openUpgradeMenu(player);
            }
        } else if (slot == 22) {
            // Close button
            player.closeInventory();
        }
    }

    private void handleSettingsClick(Player player, int slot) {
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        switch (slot) {
            case 11 -> {
                // Toggle auto-pickup
                plugin.getPlayerDataManager().toggleAutoPickup(player.getUniqueId());
                if (data.isAutoPickupEnabled()) {
                    sendMessage(player, plugin.getConfigManager().getMessage("auto-pickup-enabled"));
                } else {
                    sendMessage(player, plugin.getConfigManager().getMessage("auto-pickup-disabled"));
                }
                plugin.getBackpackGUI().openSettingsMenu(player);
            }
            case 13 -> {
                // Toggle auto-sell
                plugin.getPlayerDataManager().toggleAutoSell(player.getUniqueId());
                if (data.isAutoSellEnabled()) {
                    sendMessage(player, plugin.getConfigManager().getMessage("auto-sell-enabled"));
                } else {
                    sendMessage(player, plugin.getConfigManager().getMessage("auto-sell-disabled"));
                }
                plugin.getBackpackGUI().openSettingsMenu(player);
            }
            case 15 -> {
                // Sell all
                double sold = plugin.getBackpackManager().sellAll(player);
                if (sold > 0) {
                    sendMessage(player, plugin.getConfigManager().getMessage("sold-items",
                            "{amount}", FORMATTER.format(sold)));
                } else {
                    sendMessage(player, plugin.getConfigManager().getMessage("nothing-to-sell"));
                }
                plugin.getBackpackGUI().openSettingsMenu(player);
            }
            case 22 -> {
                // Close button
                player.closeInventory();
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof BackpackHolder bpHolder)) return;

        // Only allow drags in the backpack itself
        if (!bpHolder.isBackpack()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof BackpackHolder bpHolder)) return;

        // Save backpack contents when closed
        if (bpHolder.isBackpack()) {
            PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
            if (data != null) {
                Inventory inv = event.getInventory();
                data.setContents(inv.getContents());
                plugin.getPlayerDataManager().savePlayer(player.getUniqueId());
            }
        }
    }

    private void sendMessage(Player player, String message) {
        player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));
    }
}
