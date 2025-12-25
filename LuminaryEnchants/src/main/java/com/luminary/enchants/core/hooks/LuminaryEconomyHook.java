package com.luminary.enchants.core.hooks;

import com.luminary.enchants.api.TokenEconomy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Hook into LuminaryEconomy for token transactions.
 * Uses reflection to avoid hard dependency.
 */
public class LuminaryEconomyHook implements TokenEconomy {

    private Object economyAPI;
    private Method getBalanceMethod;
    private Method addBalanceMethod;
    private Method removeBalanceMethod;
    private final String currencyId;
    private boolean available = false;

    public LuminaryEconomyHook(String currencyId) {
        this.currencyId = currencyId;
        initialize();
    }

    private void initialize() {
        try {
            // Get the LuminaryEconomy plugin
            var plugin = Bukkit.getPluginManager().getPlugin("LuminaryEconomy");
            if (plugin == null) {
                throw new RuntimeException("LuminaryEconomy not found");
            }

            // Get the API via reflection
            Method getAPIMethod = plugin.getClass().getMethod("getAPI");
            economyAPI = getAPIMethod.invoke(plugin);

            // Cache the methods we need (using UUID versions)
            Class<?> apiClass = economyAPI.getClass();
            getBalanceMethod = apiClass.getMethod("getBalance", UUID.class, String.class);
            addBalanceMethod = apiClass.getMethod("addBalance", UUID.class, String.class, String.class, double.class);
            removeBalanceMethod = apiClass.getMethod("removeBalance", UUID.class, String.class, String.class, double.class);

            available = true;
        } catch (Exception e) {
            throw new RuntimeException("Failed to hook into LuminaryEconomy: " + e.getMessage(), e);
        }
    }

    @Override
    public long get(UUID playerId) {
        try {
            Object result = getBalanceMethod.invoke(economyAPI, playerId, currencyId);
            return ((Number) result).longValue();
        } catch (Exception e) {
            // Fallback: try getting by player object
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                try {
                    Method playerMethod = economyAPI.getClass().getMethod("getBalance", Player.class, String.class);
                    Object result = playerMethod.invoke(economyAPI, player, currencyId);
                    return ((Number) result).longValue();
                } catch (Exception ex) {
                    return 0;
                }
            }
            return 0;
        }
    }

    @Override
    public boolean withdraw(UUID playerId, long amount, String reason) {
        try {
            Object result = removeBalanceMethod.invoke(economyAPI, playerId, "Unknown", currencyId, (double) amount);
            return (Boolean) result;
        } catch (Exception e) {
            // Fallback: try by player object
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                try {
                    Method playerMethod = economyAPI.getClass().getMethod("removeBalance", Player.class, String.class, double.class);
                    Object result = playerMethod.invoke(economyAPI, player, currencyId, (double) amount);
                    return (Boolean) result;
                } catch (Exception ex) {
                    return false;
                }
            }
            return false;
        }
    }

    @Override
    public void deposit(UUID playerId, long amount, String reason) {
        try {
            addBalanceMethod.invoke(economyAPI, playerId, "Unknown", currencyId, (double) amount);
        } catch (Exception e) {
            // Fallback: try by player object
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                try {
                    Method playerMethod = economyAPI.getClass().getMethod("addBalance", Player.class, String.class, double.class);
                    playerMethod.invoke(economyAPI, player, currencyId, (double) amount);
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @Override
    public String getProviderName() {
        return "LuminaryEconomy";
    }
}
