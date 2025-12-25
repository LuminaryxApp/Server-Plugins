package com.luminary.backpacks.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BackpackHolder implements InventoryHolder {

    public enum MenuType {
        BACKPACK,
        UPGRADE,
        SETTINGS
    }

    private final UUID playerUuid;
    private final MenuType menuType;
    private Inventory inventory;

    public BackpackHolder(UUID playerUuid, MenuType menuType) {
        this.playerUuid = playerUuid;
        this.menuType = menuType;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public MenuType getMenuType() {
        return menuType;
    }

    public boolean isBackpack() {
        return menuType == MenuType.BACKPACK;
    }

    public boolean isUpgradeMenu() {
        return menuType == MenuType.UPGRADE;
    }

    public boolean isSettingsMenu() {
        return menuType == MenuType.SETTINGS;
    }
}
