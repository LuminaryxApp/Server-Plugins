package com.luminary.crates.animation;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Inventory holder for crate animations.
 * Used to identify animation inventories and prevent item theft.
 */
public class CrateAnimationHolder implements InventoryHolder {

    private final AnimationManager.CrateAnimation animation;

    public CrateAnimationHolder(AnimationManager.CrateAnimation animation) {
        this.animation = animation;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public AnimationManager.CrateAnimation getAnimation() {
        return animation;
    }
}
