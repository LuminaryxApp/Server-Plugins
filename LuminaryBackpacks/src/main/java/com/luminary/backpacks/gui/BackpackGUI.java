package com.luminary.backpacks.gui;

import com.luminary.backpacks.LuminaryBackpacks;
import com.luminary.backpacks.data.PlayerBackpackData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BackpackGUI {

    private final LuminaryBackpacks plugin;
    private static final NumberFormat FORMATTER = NumberFormat.getInstance(Locale.US);

    public BackpackGUI(LuminaryBackpacks plugin) {
        this.plugin = plugin;
    }

    public void openBackpack(Player player) {
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) {
            plugin.getPlayerDataManager().loadPlayer(player.getUniqueId());
            data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        }

        int tier = data.getTier();
        int size = data.getSize();
        String tierName = plugin.getConfigManager().getTierName(tier);
        String tierColor = plugin.getConfigManager().getTierColor(tier);

        Component title = colorize(tierColor + tierName);
        BackpackHolder holder = new BackpackHolder(player.getUniqueId(), BackpackHolder.MenuType.BACKPACK);
        Inventory inventory = Bukkit.createInventory(holder, size, title);
        holder.setInventory(inventory);

        // Set contents
        inventory.setContents(data.getContents());

        player.openInventory(inventory);
    }

    public void openUpgradeMenu(Player player) {
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        int currentTier = data.getTier();
        int maxTier = plugin.getConfigManager().getMaxTier();

        BackpackHolder holder = new BackpackHolder(player.getUniqueId(), BackpackHolder.MenuType.UPGRADE);
        Inventory inventory = Bukkit.createInventory(holder, 27, colorize("&6Backpack Upgrade"));
        holder.setInventory(inventory);

        // Fill with glass
        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, glass);
        }

        // Current backpack info (slot 11)
        String currentColor = plugin.getConfigManager().getTierColor(currentTier);
        String currentName = plugin.getConfigManager().getTierName(currentTier);
        int currentSize = plugin.getConfigManager().getTierSize(currentTier);

        ItemStack currentItem = createItem(Material.CHEST, currentColor + "&lCurrent Backpack",
                "&7Tier: " + currentColor + currentTier,
                "&7Size: &e" + currentSize + " slots",
                "&7Fill: &e" + data.getUsedSlots() + "/" + currentSize);
        inventory.setItem(11, currentItem);

        // Upgrade button or max tier (slot 15)
        if (currentTier >= maxTier) {
            ItemStack maxItem = createItem(Material.BARRIER, "&c&lMax Tier Reached",
                    "&7You have the maximum backpack tier!");
            inventory.setItem(15, maxItem);
        } else {
            int nextTier = currentTier + 1;
            String nextColor = plugin.getConfigManager().getTierColor(nextTier);
            String nextName = plugin.getConfigManager().getTierName(nextTier);
            int nextSize = plugin.getConfigManager().getTierSize(nextTier);
            long cost = plugin.getConfigManager().getUpgradeCost(currentTier);

            double balance = plugin.getEconomyHook().getBalance(player);
            boolean canAfford = balance >= cost;

            ItemStack upgradeItem = createItem(
                    canAfford ? Material.LIME_CONCRETE : Material.RED_CONCRETE,
                    "&a&lUpgrade to " + nextColor + nextName,
                    "&7New Size: &e" + nextSize + " slots",
                    "&7Cost: &e" + FORMATTER.format(cost) + " tokens",
                    "",
                    canAfford ? "&aClick to upgrade!" : "&cNot enough tokens!");
            inventory.setItem(15, upgradeItem);
        }

        // Close button (slot 22)
        ItemStack closeItem = createItem(Material.BARRIER, "&c&lClose");
        inventory.setItem(22, closeItem);

        player.openInventory(inventory);
    }

    public void openSettingsMenu(Player player) {
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return;

        BackpackHolder holder = new BackpackHolder(player.getUniqueId(), BackpackHolder.MenuType.SETTINGS);
        Inventory inventory = Bukkit.createInventory(holder, 27, colorize("&6Backpack Settings"));
        holder.setInventory(inventory);

        // Fill with glass
        ItemStack glass = createItem(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, glass);
        }

        // Auto-pickup toggle (slot 11)
        boolean autoPickup = data.isAutoPickupEnabled();
        ItemStack pickupItem = createItem(
                autoPickup ? Material.LIME_DYE : Material.GRAY_DYE,
                "&e&lAuto-Pickup",
                "&7Automatically pick up mined blocks",
                "",
                autoPickup ? "&aEnabled" : "&cDisabled",
                "",
                "&7Click to toggle!");
        inventory.setItem(11, pickupItem);

        // Auto-sell toggle (slot 13)
        boolean autoSell = data.isAutoSellEnabled();
        ItemStack sellItem = createItem(
                autoSell ? Material.LIME_DYE : Material.GRAY_DYE,
                "&e&lAuto-Sell",
                "&7Automatically sell backpack contents",
                "",
                autoSell ? "&aEnabled" : "&cDisabled",
                "",
                "&7Click to toggle!");
        inventory.setItem(13, sellItem);

        // Sell all button (slot 15)
        double totalValue = plugin.getBackpackManager().getTotalValue(player);
        ItemStack sellAllItem = createItem(Material.GOLD_INGOT, "&6&lSell All",
                "&7Sell all items in your backpack",
                "",
                "&7Value: &e" + FORMATTER.format(totalValue) + " tokens",
                "",
                "&eClick to sell!");
        inventory.setItem(15, sellAllItem);

        // Close button (slot 22)
        ItemStack closeItem = createItem(Material.BARRIER, "&c&lClose");
        inventory.setItem(22, closeItem);

        player.openInventory(inventory);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(colorize(name));
            if (lore.length > 0) {
                List<Component> loreList = new ArrayList<>();
                for (String line : lore) {
                    loreList.add(colorize(line));
                }
                meta.lore(loreList);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private Component colorize(String text) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(text)
                .decoration(TextDecoration.ITALIC, false);
    }
}
