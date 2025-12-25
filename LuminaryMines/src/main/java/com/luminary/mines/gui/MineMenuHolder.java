package com.luminary.mines.gui;

import com.luminary.mines.mine.Mine;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Inventory holder for mine GUIs.
 */
public class MineMenuHolder implements InventoryHolder {

    private final MenuType type;
    private final Mine mine;
    private Inventory inventory;
    private int page = 0;

    public MineMenuHolder(MenuType type, Mine mine) {
        this.type = type;
        this.mine = mine;
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

    public Mine getMine() {
        return mine;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public enum MenuType {
        MAIN_MENU,
        SETTINGS,
        WHITELIST,
        BLOCKS,
        UPGRADES,
        CONFIRM_DELETE
    }
}
