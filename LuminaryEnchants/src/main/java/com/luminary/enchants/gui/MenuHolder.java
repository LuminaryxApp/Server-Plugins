package com.luminary.enchants.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Inventory holder that links inventories to their menu.
 */
public class MenuHolder implements InventoryHolder {

    private final AbstractMenu menu;

    public MenuHolder(AbstractMenu menu) {
        this.menu = menu;
    }

    public AbstractMenu getMenu() {
        return menu;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return menu.getInventory();
    }
}
