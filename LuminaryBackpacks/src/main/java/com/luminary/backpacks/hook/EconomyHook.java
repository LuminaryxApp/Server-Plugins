package com.luminary.backpacks.hook;

import com.luminary.backpacks.LuminaryBackpacks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.UUID;

public class EconomyHook {

    private final LuminaryBackpacks plugin;
    private Plugin economyPlugin;
    private Object economyManager;
    private Method getBalanceMethod;
    private Method depositMethod;
    private Method withdrawMethod;
    private boolean hooked = false;

    public EconomyHook(LuminaryBackpacks plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        economyPlugin = Bukkit.getPluginManager().getPlugin("LuminaryEconomy");
        if (economyPlugin == null || !economyPlugin.isEnabled()) {
            plugin.getLogger().info("LuminaryEconomy not found. Using basic economy.");
            return;
        }

        try {
            Method getEconomyManager = economyPlugin.getClass().getMethod("getEconomyManager");
            economyManager = getEconomyManager.invoke(economyPlugin);

            getBalanceMethod = economyManager.getClass().getMethod("getBalance", UUID.class);
            depositMethod = economyManager.getClass().getMethod("deposit", UUID.class, double.class);
            withdrawMethod = economyManager.getClass().getMethod("withdraw", UUID.class, double.class);

            hooked = true;
            plugin.getLogger().info("Successfully hooked into LuminaryEconomy!");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into LuminaryEconomy: " + e.getMessage());
            hooked = false;
        }
    }

    public boolean isHooked() {
        return hooked;
    }

    public double getBalance(Player player) {
        if (!hooked) return 0;

        try {
            return (double) getBalanceMethod.invoke(economyManager, player.getUniqueId());
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean deposit(Player player, double amount) {
        if (!hooked) return false;

        try {
            depositMethod.invoke(economyManager, player.getUniqueId(), amount);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean withdraw(Player player, double amount) {
        if (!hooked) return false;

        try {
            double balance = getBalance(player);
            if (balance < amount) {
                return false;
            }
            withdrawMethod.invoke(economyManager, player.getUniqueId(), amount);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasBalance(Player player, double amount) {
        return getBalance(player) >= amount;
    }
}
