package com.luminary.miners.hook;

import com.luminary.miners.LuminaryMiners;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Hooks into LuminaryEconomy for currency management.
 */
public class EconomyHook {

    private final LuminaryMiners plugin;
    private Object economyApi;
    private boolean hooked = false;

    public EconomyHook(LuminaryMiners plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        Plugin economyPlugin = Bukkit.getPluginManager().getPlugin("LuminaryEconomy");
        if (economyPlugin != null && economyPlugin.isEnabled()) {
            try {
                // Get the API instance via reflection
                Method getApiMethod = economyPlugin.getClass().getMethod("getApi");
                economyApi = getApiMethod.invoke(economyPlugin);
                hooked = true;
                plugin.getLogger().info("Successfully hooked into LuminaryEconomy!");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to hook into LuminaryEconomy: " + e.getMessage());
                hooked = false;
            }
        } else {
            plugin.getLogger().warning("LuminaryEconomy not found! Economy features disabled.");
            hooked = false;
        }
    }

    public boolean isHooked() {
        return hooked;
    }

    /**
     * Get a player's balance for a specific currency.
     */
    public double getBalance(UUID playerId, String currency) {
        if (!hooked || economyApi == null) {
            return 0;
        }

        try {
            Method method = economyApi.getClass().getMethod("getBalance", UUID.class, String.class);
            return (double) method.invoke(economyApi, playerId, currency.toLowerCase());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to get balance: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get a player's balance for a specific currency.
     */
    public double getBalance(OfflinePlayer player, String currency) {
        return getBalance(player.getUniqueId(), currency);
    }

    /**
     * Add currency to a player's balance.
     */
    public boolean addBalance(UUID playerId, String currency, double amount) {
        if (!hooked || economyApi == null) {
            return false;
        }

        try {
            Method method = economyApi.getClass().getMethod("addBalance", UUID.class, String.class, double.class);
            return (boolean) method.invoke(economyApi, playerId, currency.toLowerCase(), amount);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to add balance: " + e.getMessage());
            return false;
        }
    }

    /**
     * Add currency to a player's balance.
     */
    public boolean addBalance(OfflinePlayer player, String currency, double amount) {
        return addBalance(player.getUniqueId(), currency, amount);
    }

    /**
     * Remove currency from a player's balance.
     */
    public boolean removeBalance(UUID playerId, String currency, double amount) {
        if (!hooked || economyApi == null) {
            return false;
        }

        try {
            Method method = economyApi.getClass().getMethod("removeBalance", UUID.class, String.class, double.class);
            return (boolean) method.invoke(economyApi, playerId, currency.toLowerCase(), amount);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to remove balance: " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove currency from a player's balance.
     */
    public boolean removeBalance(OfflinePlayer player, String currency, double amount) {
        return removeBalance(player.getUniqueId(), currency, amount);
    }

    /**
     * Check if player has enough balance.
     */
    public boolean hasBalance(UUID playerId, String currency, double amount) {
        return getBalance(playerId, currency) >= amount;
    }

    /**
     * Check if player has enough balance.
     */
    public boolean hasBalance(OfflinePlayer player, String currency, double amount) {
        return hasBalance(player.getUniqueId(), currency, amount);
    }

    /**
     * Withdraw from a player (remove if they have enough).
     */
    public boolean withdraw(UUID playerId, String currency, double amount) {
        if (hasBalance(playerId, currency, amount)) {
            return removeBalance(playerId, currency, amount);
        }
        return false;
    }

    /**
     * Withdraw from a player (remove if they have enough).
     */
    public boolean withdraw(OfflinePlayer player, String currency, double amount) {
        return withdraw(player.getUniqueId(), currency, amount);
    }
}
