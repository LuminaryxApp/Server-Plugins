package com.luminary.enchants.gui;

import com.luminary.enchants.LuminaryEnchants;
import com.luminary.enchants.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for all GUI menus.
 */
public abstract class AbstractMenu {

    protected final LuminaryEnchants plugin;
    protected final Player player;
    protected final Inventory inventory;
    protected final MenuHolder holder;

    public AbstractMenu(LuminaryEnchants plugin, Player player, int size, String title) {
        this.plugin = plugin;
        this.player = player;
        this.holder = new MenuHolder(this);
        this.inventory = Bukkit.createInventory(holder, size, TextUtil.colorize(title));
    }

    /**
     * Initialize the menu contents.
     */
    protected abstract void init();

    /**
     * Handle a click in the menu.
     */
    public abstract void handleClick(int slot, ClickType clickType);

    /**
     * Called when the menu is closed.
     */
    public void onClose() {
        // Override in subclasses if needed
    }

    /**
     * Open the menu for the player.
     */
    public void open() {
        init();
        player.openInventory(inventory);
    }

    /**
     * Close the menu.
     */
    public void close() {
        if (player.getOpenInventory().getTopInventory().equals(inventory)) {
            player.closeInventory();
        }
    }

    /**
     * Refresh the menu contents.
     */
    public void refresh() {
        inventory.clear();
        init();
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    // ========== Helper Methods ==========

    /**
     * Create an item with a display name and lore.
     */
    protected ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(TextUtil.colorize(name));
            if (lore.length > 0) {
                List<Component> loreComponents = new ArrayList<>();
                for (String line : lore) {
                    loreComponents.add(TextUtil.colorize(line));
                }
                meta.lore(loreComponents);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Create an item with a display name and lore list.
     */
    protected ItemStack createItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(TextUtil.colorize(name));
            if (lore != null && !lore.isEmpty()) {
                List<Component> loreComponents = new ArrayList<>();
                for (String line : lore) {
                    loreComponents.add(TextUtil.colorize(line));
                }
                meta.lore(loreComponents);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Fill empty slots with a filler item.
     */
    protected void fillEmpty(Material material) {
        ItemStack filler = createItem(material, " ");
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }
    }

    /**
     * Fill the border with a material.
     */
    protected void fillBorder(Material material) {
        ItemStack border = createItem(material, " ");
        int size = inventory.getSize();
        int rows = size / 9;

        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border); // Top row
            inventory.setItem(size - 9 + i, border); // Bottom row
        }

        for (int row = 1; row < rows - 1; row++) {
            inventory.setItem(row * 9, border); // Left column
            inventory.setItem(row * 9 + 8, border); // Right column
        }
    }

    /**
     * Create a back button item.
     */
    protected ItemStack createBackButton() {
        return createItem(Material.ARROW, "&cBack", "&7Click to go back");
    }

    /**
     * Create a close button item.
     */
    protected ItemStack createCloseButton() {
        return createItem(Material.BARRIER, "&cClose", "&7Click to close");
    }
}
