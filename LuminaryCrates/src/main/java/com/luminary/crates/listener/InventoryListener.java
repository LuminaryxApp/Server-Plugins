package com.luminary.crates.listener;

import com.luminary.crates.LuminaryCrates;
import com.luminary.crates.animation.CrateAnimationHolder;
import com.luminary.crates.crate.Crate;
import com.luminary.crates.gui.CrateMenuHolder;
import com.luminary.crates.gui.PreviewHolder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

/**
 * Handles inventory events for crate GUIs.
 */
public class InventoryListener implements Listener {

    private final LuminaryCrates plugin;

    public InventoryListener(LuminaryCrates plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // Handle animation inventory
        if (holder instanceof CrateAnimationHolder) {
            event.setCancelled(true);
            return;
        }

        // Handle preview inventory
        if (holder instanceof PreviewHolder previewHolder) {
            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }

            int slot = event.getRawSlot();

            // Check for navigation buttons
            if (slot == previewHolder.getBackSlot()) {
                // Go back to main crate menu
                plugin.getCrateMenuManager().openCrateMenu(player);
            } else if (slot == previewHolder.getPrevPageSlot() && previewHolder.hasPrevPage()) {
                plugin.getPreviewManager().openPreview(player, previewHolder.getCrate(),
                        previewHolder.getPage() - 1);
            } else if (slot == previewHolder.getNextPageSlot() && previewHolder.hasNextPage()) {
                plugin.getPreviewManager().openPreview(player, previewHolder.getCrate(),
                        previewHolder.getPage() + 1);
            }
            return;
        }

        // Handle virtual crate menu
        if (holder instanceof CrateMenuHolder menuHolder) {
            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player player)) {
                return;
            }

            int slot = event.getRawSlot();
            ClickType clickType = event.getClick();

            if (menuHolder.isMainMenu()) {
                handleMainMenuClick(player, slot, clickType, event.getInventory());
            } else if (menuHolder.isConfirmMenu()) {
                handleConfirmMenuClick(player, slot, menuHolder);
            }
        }
    }

    /**
     * Handle clicks in the main crate menu.
     */
    private void handleMainMenuClick(Player player, int slot, ClickType clickType, Inventory inventory) {
        // Close button
        if (slot == 45) {
            player.closeInventory();
            return;
        }

        // Check if clicked on a crate item
        ItemStack clicked = inventory.getItem(slot);
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }

        // Skip glass panes and other UI elements
        if (clicked.getType().name().contains("GLASS_PANE") ||
                clicked.getType() == Material.BARRIER ||
                clicked.getType() == Material.BOOK) {
            return;
        }

        // Find which crate was clicked by matching the material and slot
        Crate clickedCrate = findCrateAtSlot(slot);
        if (clickedCrate == null) {
            return;
        }

        if (clickType == ClickType.LEFT) {
            // Preview the crate
            plugin.getPreviewManager().openPreview(player, clickedCrate);
        } else if (clickType == ClickType.RIGHT) {
            // Open the crate (confirmation or direct)
            plugin.getCrateMenuManager().openCrateVirtual(player, clickedCrate);
        }
    }

    /**
     * Find which crate is at a given slot in the main menu.
     */
    private Crate findCrateAtSlot(int clickedSlot) {
        Collection<Crate> crates = plugin.getCrateRegistry().getAllCrates();

        int slot = 10;
        for (Crate crate : crates) {
            if (slot >= 45) break;

            // Skip border slots
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) {
                slot += 2;
                continue;
            }

            if (slot == clickedSlot) {
                return crate;
            }
            slot++;
        }
        return null;
    }

    /**
     * Handle clicks in the confirm menu.
     */
    private void handleConfirmMenuClick(Player player, int slot, CrateMenuHolder menuHolder) {
        if (slot == 11) {
            // Confirm button - open the crate
            Crate crate = menuHolder.getSelectedCrate();
            if (crate != null) {
                plugin.getCrateMenuManager().openCrateVirtual(player, crate);
            }
        } else if (slot == 15) {
            // Cancel button - go back to main menu
            plugin.getCrateMenuManager().openCrateMenu(player);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // Prevent dragging in crate GUIs
        if (holder instanceof CrateAnimationHolder ||
                holder instanceof PreviewHolder ||
                holder instanceof CrateMenuHolder) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        // Handle animation close
        if (holder instanceof CrateAnimationHolder animationHolder) {
            if (!(event.getPlayer() instanceof Player player)) {
                return;
            }

            // If animation isn't finished, cancel it and give reward anyway
            if (!animationHolder.getAnimation().isFinished()) {
                plugin.getAnimationManager().cancelAnimation(player);
            }
        }
    }
}
