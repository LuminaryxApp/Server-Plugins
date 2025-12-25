package com.luminary.ranks.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class RankMenuHolder implements InventoryHolder {

    private final MenuType type;
    private Inventory inventory;

    public RankMenuHolder(MenuType type) {
        this.type = type;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public MenuType getType() {
        return type;
    }

    public enum MenuType {
        MAIN,
        RANKS_LIST,
        PRESTIGES_LIST,
        REBIRTHS_LIST
    }
}
