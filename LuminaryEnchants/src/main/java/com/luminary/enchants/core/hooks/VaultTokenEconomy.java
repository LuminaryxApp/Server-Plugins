package com.luminary.enchants.core.hooks;

import com.luminary.enchants.api.TokenEconomy;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

/**
 * Vault economy implementation of TokenEconomy.
 * Uses Vault's Economy API as a fallback when LuminaryTokens is not available.
 */
public class VaultTokenEconomy implements TokenEconomy {

    private final Economy economy;

    public VaultTokenEconomy(Economy economy) {
        this.economy = economy;
    }

    @Override
    public long get(UUID playerId) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        return (long) economy.getBalance(player);
    }

    @Override
    public boolean withdraw(UUID playerId, long amount, String reason) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        if (economy.getBalance(player) < amount) {
            return false;
        }
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    @Override
    public void deposit(UUID playerId, long amount, String reason) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(playerId);
        economy.depositPlayer(player, amount);
    }

    @Override
    public boolean isAvailable() {
        return economy != null;
    }

    @Override
    public String getProviderName() {
        return "Vault";
    }
}
