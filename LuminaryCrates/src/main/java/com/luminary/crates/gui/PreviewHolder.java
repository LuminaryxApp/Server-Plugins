package com.luminary.crates.gui;

import com.luminary.crates.crate.Crate;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Inventory holder for crate preview GUIs.
 */
public class PreviewHolder implements InventoryHolder {

    private final Crate crate;
    private final int page;
    private final int totalPages;
    private Inventory inventory;

    // Button slot positions
    private static final int BACK_SLOT = 45;
    private static final int PREV_PAGE_SLOT = 48;
    private static final int NEXT_PAGE_SLOT = 50;

    public PreviewHolder(Crate crate, int page, int totalPages) {
        this.crate = crate;
        this.page = page;
        this.totalPages = totalPages;
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    public Crate getCrate() {
        return crate;
    }

    public int getPage() {
        return page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean hasPrevPage() {
        return page > 0;
    }

    public boolean hasNextPage() {
        return page < totalPages - 1;
    }

    public int getBackSlot() {
        return BACK_SLOT;
    }

    public int getPrevPageSlot() {
        return PREV_PAGE_SLOT;
    }

    public int getNextPageSlot() {
        return NEXT_PAGE_SLOT;
    }
}
