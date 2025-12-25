package com.luminary.crates.gui;

import com.luminary.crates.crate.Crate;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

/**
 * Inventory holder for virtual crate menus.
 */
public class CrateMenuHolder implements InventoryHolder {

    public enum MenuType {
        MAIN,       // Main crate selection menu
        CONFIRM     // Open confirmation menu
    }

    private final MenuType menuType;
    private final Crate selectedCrate;
    private Inventory inventory;

    public CrateMenuHolder(MenuType menuType) {
        this(menuType, null);
    }

    public CrateMenuHolder(MenuType menuType, Crate selectedCrate) {
        this.menuType = menuType;
        this.selectedCrate = selectedCrate;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public MenuType getMenuType() {
        return menuType;
    }

    public Crate getSelectedCrate() {
        return selectedCrate;
    }

    public boolean isMainMenu() {
        return menuType == MenuType.MAIN;
    }

    public boolean isConfirmMenu() {
        return menuType == MenuType.CONFIRM;
    }
}
