package com.luminary.crates.gui;

import com.luminary.crates.LuminaryCrates;
import com.luminary.crates.crate.Crate;
import com.luminary.crates.crate.CrateReward;
import com.luminary.crates.util.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Manages the virtual crate menu GUI.
 * Players can browse all crates and open them without physical locations.
 */
public class CrateMenuManager {

    private final LuminaryCrates plugin;
    private static final int SIZE = 54; // 6 rows

    public CrateMenuManager(LuminaryCrates plugin) {
        this.plugin = plugin;
    }

    /**
     * Open the main crate menu for a player.
     */
    public void openCrateMenu(Player player) {
        Collection<Crate> crates = plugin.getCrateRegistry().getAllCrates();

        CrateMenuHolder holder = new CrateMenuHolder(CrateMenuHolder.MenuType.MAIN);
        Inventory inventory = Bukkit.createInventory(holder, SIZE,
                TextUtil.colorize("&6&lVirtual Crates"));
        holder.setInventory(inventory);

        // Fill border with black glass
        fillBorder(inventory, Material.BLACK_STAINED_GLASS_PANE);

        // Add crate items in center area
        int slot = 10;
        for (Crate crate : crates) {
            if (slot >= SIZE - 9) break;

            // Skip border slots
            if (slot % 9 == 0) slot++;
            if (slot % 9 == 8) {
                slot += 2;
                continue;
            }

            ItemStack crateItem = createCrateItem(player, crate);
            inventory.setItem(slot, crateItem);
            slot++;
        }

        // Add info item at bottom center
        ItemStack infoItem = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = infoItem.getItemMeta();
        if (infoMeta != null) {
            infoMeta.displayName(TextUtil.colorize("&e&lHow to Use"));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.colorize("&7Left-click a crate to preview rewards"));
            lore.add(TextUtil.colorize("&7Right-click a crate to open it"));
            lore.add(Component.empty());
            lore.add(TextUtil.colorize("&7You need a key in your inventory to open!"));
            infoMeta.lore(lore);
            infoItem.setItemMeta(infoMeta);
        }
        inventory.setItem(49, infoItem);

        // Add close button
        ItemStack closeButton = new ItemStack(Material.BARRIER);
        ItemMeta closeMeta = closeButton.getItemMeta();
        if (closeMeta != null) {
            closeMeta.displayName(TextUtil.colorize("&c&lClose"));
            closeButton.setItemMeta(closeMeta);
        }
        inventory.setItem(45, closeButton);

        player.openInventory(inventory);
    }

    /**
     * Create a crate display item for the menu.
     */
    private ItemStack createCrateItem(Player player, Crate crate) {
        ItemStack item = new ItemStack(crate.getDisplayMaterial());
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(TextUtil.colorize(crate.getTier().getColor() + "&l" + crate.getDisplayName()));

            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.colorize("&7Tier: " + crate.getTier().getColoredName()));
            lore.add(TextUtil.colorize("&7Rewards: &e" + crate.getRewards().size()));
            lore.add(Component.empty());

            // Show how many keys player has
            int keyCount = plugin.getKeyManager().countKeys(player, crate);
            if (keyCount > 0) {
                lore.add(TextUtil.colorize("&aYou have &l" + keyCount + "&a key(s)!"));
            } else {
                lore.add(TextUtil.colorize("&cYou don't have any keys!"));
            }

            lore.add(Component.empty());
            lore.add(TextUtil.colorize("&e&lLeft-click &7to preview"));
            lore.add(TextUtil.colorize("&e&lRight-click &7to open"));

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Open crate confirmation menu.
     */
    public void openConfirmMenu(Player player, Crate crate) {
        CrateMenuHolder holder = new CrateMenuHolder(CrateMenuHolder.MenuType.CONFIRM, crate);
        Inventory inventory = Bukkit.createInventory(holder, 27,
                TextUtil.colorize("&6Open " + crate.getDisplayName() + "?"));
        holder.setInventory(inventory);

        // Fill with gray glass
        ItemStack grayGlass = createGlassPane(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, grayGlass);
        }

        // Show crate in center
        ItemStack crateDisplay = createCrateItem(player, crate);
        inventory.setItem(13, crateDisplay);

        // Confirm button (green)
        ItemStack confirmButton = new ItemStack(Material.LIME_CONCRETE);
        ItemMeta confirmMeta = confirmButton.getItemMeta();
        if (confirmMeta != null) {
            confirmMeta.displayName(TextUtil.colorize("&a&lConfirm Open"));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.colorize("&7Click to open the crate!"));
            confirmMeta.lore(lore);
            confirmButton.setItemMeta(confirmMeta);
        }
        inventory.setItem(11, confirmButton);

        // Cancel button (red)
        ItemStack cancelButton = new ItemStack(Material.RED_CONCRETE);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.displayName(TextUtil.colorize("&c&lCancel"));
            List<Component> lore = new ArrayList<>();
            lore.add(TextUtil.colorize("&7Click to go back"));
            cancelMeta.lore(lore);
            cancelButton.setItemMeta(cancelMeta);
        }
        inventory.setItem(15, cancelButton);

        player.openInventory(inventory);
    }

    /**
     * Handle opening a crate from the virtual menu.
     */
    public void openCrateVirtual(Player player, Crate crate) {
        // Check if player is already in animation
        if (plugin.getAnimationManager().isAnimating(player)) {
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("crate.already-opening")));
            return;
        }

        // Check if player has a key
        ItemStack keyItem = plugin.getKeyManager().findKeyInInventory(player, crate);
        if (keyItem == null) {
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("crate.no-key")
                            .replace("{crate}", crate.getDisplayName())));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        // Check permission
        String permission = "luminarycrates.open." + crate.getId();
        if (!player.hasPermission(permission) && !player.hasPermission("luminarycrates.open.*")) {
            player.sendMessage(TextUtil.colorize(
                    plugin.getConfigManager().getMessage("crate.no-permission")));
            return;
        }

        // Close the menu first
        player.closeInventory();

        // Consume the key
        plugin.getKeyManager().consumeKey(player, keyItem);

        // Roll for reward
        CrateReward reward = crate.rollReward();

        // Play open sound
        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);

        // Start animation
        plugin.getAnimationManager().startAnimation(player, crate, reward);
    }

    /**
     * Fill border with glass panes.
     */
    private void fillBorder(Inventory inventory, Material material) {
        ItemStack glass = createGlassPane(material);

        // Top row
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, glass);
        }

        // Bottom row
        for (int i = SIZE - 9; i < SIZE; i++) {
            inventory.setItem(i, glass);
        }

        // Left and right columns
        for (int i = 9; i < SIZE - 9; i += 9) {
            inventory.setItem(i, glass);
            inventory.setItem(i + 8, glass);
        }
    }

    /**
     * Create a glass pane item.
     */
    private ItemStack createGlassPane(Material material) {
        ItemStack glass = new ItemStack(material);
        ItemMeta meta = glass.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            glass.setItemMeta(meta);
        }
        return glass;
    }
}
