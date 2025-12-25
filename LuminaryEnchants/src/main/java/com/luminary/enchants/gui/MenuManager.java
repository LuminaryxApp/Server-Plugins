package com.luminary.enchants.gui;

import com.luminary.enchants.LuminaryEnchants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages all GUI menus for the plugin.
 */
public class MenuManager implements Listener {

    private final LuminaryEnchants plugin;
    private final Map<UUID, AbstractMenu> openMenus = new HashMap<>();
    private final Map<UUID, Long> lastClickTime = new HashMap<>();

    public MenuManager(LuminaryEnchants plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void openMainMenu(Player player) {
        MainMenu menu = new MainMenu(plugin, player);
        openMenu(player, menu);
    }

    public void openEnchantListMenu(Player player) {
        EnchantListMenu menu = new EnchantListMenu(plugin, player);
        openMenu(player, menu);
    }

    public void openEnchantDetailMenu(Player player, String enchantId) {
        EnchantDetailMenu menu = new EnchantDetailMenu(plugin, player, enchantId);
        openMenu(player, menu);
    }

    public void openUpgradeConfirmMenu(Player player, String enchantId, int levelsToAdd) {
        UpgradeConfirmMenu menu = new UpgradeConfirmMenu(plugin, player, enchantId, levelsToAdd);
        openMenu(player, menu);
    }

    private void openMenu(Player player, AbstractMenu menu) {
        UUID playerId = player.getUniqueId();

        // Close any existing menu
        AbstractMenu existing = openMenus.get(playerId);
        if (existing != null) {
            existing.close();
        }

        openMenus.put(playerId, menu);
        menu.open();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        AbstractMenu menu = openMenus.get(playerId);

        if (menu == null) {
            return;
        }

        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null) {
            return;
        }

        // Check if clicking in our menu
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof MenuHolder menuHolder) || menuHolder.getMenu() != menu) {
            return;
        }

        event.setCancelled(true);

        // Debounce clicks
        long now = System.currentTimeMillis();
        Long lastClick = lastClickTime.get(playerId);
        int debounceMs = plugin.getConfigManager().getClickDebounceMs();

        if (lastClick != null && (now - lastClick) < debounceMs) {
            return;
        }
        lastClickTime.put(playerId, now);

        // Handle the click
        if (clickedInventory.equals(event.getView().getTopInventory())) {
            menu.handleClick(event.getSlot(), event.getClick());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        UUID playerId = player.getUniqueId();
        AbstractMenu menu = openMenus.get(playerId);

        if (menu == null) {
            return;
        }

        InventoryHolder holder = event.getInventory().getHolder();
        if (holder instanceof MenuHolder menuHolder && menuHolder.getMenu() == menu) {
            openMenus.remove(playerId);
            menu.onClose();
        }
    }

    public void handlePlayerQuit(UUID playerId) {
        AbstractMenu menu = openMenus.remove(playerId);
        if (menu != null) {
            menu.close();
        }
        lastClickTime.remove(playerId);
    }

    public void closeAll() {
        for (AbstractMenu menu : openMenus.values()) {
            menu.close();
        }
        openMenus.clear();
        lastClickTime.clear();
    }

    public LuminaryEnchants getPlugin() {
        return plugin;
    }
}
