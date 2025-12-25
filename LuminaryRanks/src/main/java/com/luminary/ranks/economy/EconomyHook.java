package com.luminary.ranks.economy;

import com.luminary.ranks.LuminaryRanks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Hooks into LuminaryEconomy for token transactions.
 */
public class EconomyHook {

    private final LuminaryRanks plugin;
    private Object economyAPI;
    private Method getBalanceMethod;
    private Method removeBalanceMethod;
    private String currencyId;
    private boolean available = false;

    public EconomyHook(LuminaryRanks plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        currencyId = plugin.getConfigManager().getCurrencyType();

        try {
            var economyPlugin = Bukkit.getPluginManager().getPlugin("LuminaryEconomy");
            if (economyPlugin == null || !economyPlugin.isEnabled()) {
                plugin.getLogger().warning("LuminaryEconomy not found! Ranks will be free.");
                return;
            }

            // Get the API
            Method getAPIMethod = economyPlugin.getClass().getMethod("getAPI");
            economyAPI = getAPIMethod.invoke(economyPlugin);

            // Cache methods
            Class<?> apiClass = economyAPI.getClass();
            getBalanceMethod = apiClass.getMethod("getBalance", Player.class, String.class);
            removeBalanceMethod = apiClass.getMethod("removeBalance", Player.class, String.class, double.class);

            available = true;
            plugin.getLogger().info("Hooked into LuminaryEconomy! Using currency: " + currencyId);

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into LuminaryEconomy: " + e.getMessage());
            plugin.getLogger().warning("Ranks will be free!");
        }
    }

    public boolean isAvailable() {
        return available;
    }

    public long getBalance(Player player) {
        if (!available) return Long.MAX_VALUE;

        try {
            Object result = getBalanceMethod.invoke(economyAPI, player, currencyId);
            return ((Number) result).longValue();
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean withdraw(Player player, long amount) {
        if (!available) return true;

        try {
            Object result = removeBalanceMethod.invoke(economyAPI, player, currencyId, (double) amount);
            return (Boolean) result;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to withdraw " + amount + " from " + player.getName() + ": " + e.getMessage());
            return false;
        }
    }

    public boolean hasBalance(Player player, long amount) {
        return getBalance(player) >= amount;
    }

    public String getCurrencyId() {
        return currencyId;
    }
}
