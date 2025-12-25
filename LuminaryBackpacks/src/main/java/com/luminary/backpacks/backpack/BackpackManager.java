package com.luminary.backpacks.backpack;

import com.luminary.backpacks.LuminaryBackpacks;
import com.luminary.backpacks.data.PlayerBackpackData;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BackpackManager {

    private final LuminaryBackpacks plugin;

    public BackpackManager(LuminaryBackpacks plugin) {
        this.plugin = plugin;
    }

    /**
     * Try to add an item to a player's backpack.
     * Returns the amount that couldn't be added (0 if all added).
     */
    public int addItem(Player player, ItemStack item) {
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return item.getAmount();

        return data.addItem(item.clone());
    }

    /**
     * Try to add items to a player's backpack.
     * Returns items that couldn't be added.
     */
    public Map<Integer, ItemStack> addItems(Player player, ItemStack... items) {
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        Map<Integer, ItemStack> leftover = new HashMap<>();

        if (data == null) {
            for (int i = 0; i < items.length; i++) {
                leftover.put(i, items[i]);
            }
            return leftover;
        }

        for (int i = 0; i < items.length; i++) {
            int remaining = data.addItem(items[i].clone());
            if (remaining > 0) {
                ItemStack left = items[i].clone();
                left.setAmount(remaining);
                leftover.put(i, left);
            }
        }

        return leftover;
    }

    /**
     * Sell all items in the backpack.
     * Returns the total value sold.
     */
    public double sellAll(Player player) {
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return 0;

        double total = 0;
        ItemStack[] contents = data.getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() != Material.AIR) {
                double price = plugin.getConfigManager().getSellPrice(item.getType());
                if (price > 0) {
                    total += price * item.getAmount();
                    data.setItem(i, null);
                }
            }
        }

        if (total > 0) {
            plugin.getEconomyHook().deposit(player, total);
        }

        return total;
    }

    /**
     * Auto-sell for a player (if enabled).
     */
    public void autoSell(Player player) {
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null || !data.isAutoSellEnabled()) return;

        double sold = sellAll(player);
        if (sold > 0) {
            try {
                String soundName = plugin.getConfigManager().getAutoSellSound();
                Sound sound = Sound.valueOf(soundName);
                player.playSound(player.getLocation(), sound, 0.5f, 1.0f);
            } catch (Exception ignored) {}
        }
    }

    /**
     * Check if backpack has space.
     */
    public boolean hasSpace(Player player) {
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        return data != null && data.hasSpace();
    }

    /**
     * Check if backpack has space for a specific item.
     */
    public boolean hasSpaceFor(Player player, ItemStack item) {
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        return data != null && data.hasSpaceFor(item);
    }

    /**
     * Get backpack fill percentage.
     */
    public double getFillPercentage(Player player) {
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return 0;

        int used = 0;
        int total = data.getSize();
        for (ItemStack item : data.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                used++;
            }
        }

        return (double) used / total * 100;
    }

    /**
     * Get total value of backpack contents.
     */
    public double getTotalValue(Player player) {
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return 0;

        double total = 0;
        for (ItemStack item : data.getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                double price = plugin.getConfigManager().getSellPrice(item.getType());
                total += price * item.getAmount();
            }
        }

        return total;
    }

    /**
     * Upgrade a player's backpack tier.
     * Returns true if successful.
     */
    public boolean upgradeTier(Player player) {
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null) return false;

        int currentTier = data.getTier();
        int maxTier = plugin.getConfigManager().getMaxTier();

        if (currentTier >= maxTier) {
            return false;
        }

        long cost = plugin.getConfigManager().getUpgradeCost(currentTier);
        if (cost > 0 && !plugin.getEconomyHook().withdraw(player, cost)) {
            return false;
        }

        data.setTier(currentTier + 1);
        return true;
    }

    /**
     * Set a player's backpack tier.
     */
    public void setTier(UUID uuid, int tier) {
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data != null) {
            int maxTier = plugin.getConfigManager().getMaxTier();
            data.setTier(Math.max(1, Math.min(tier, maxTier)));
        }
    }

    /**
     * Get a player's backpack tier.
     */
    public int getTier(UUID uuid) {
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        return data != null ? data.getTier() : plugin.getConfigManager().getDefaultTier();
    }

    /**
     * Clear a player's backpack.
     */
    public void clearBackpack(UUID uuid) {
        PlayerBackpackData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data != null) {
            data.clear();
        }
    }
}
