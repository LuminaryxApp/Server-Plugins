package com.luminary.economy.api;

import com.luminary.economy.LuminaryEconomy;
import com.luminary.economy.currency.Currency;
import com.luminary.economy.data.PlayerData;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Public API for other plugins to interact with LuminaryEconomy.
 *
 * Usage example:
 * <pre>
 * EconomyAPI api = LuminaryEconomy.getInstance().getAPI();
 * api.addBalance(player, "tokens", 1000);
 * double balance = api.getBalance(player, "tokens");
 * </pre>
 */
public class EconomyAPI {

    private final LuminaryEconomy plugin;

    public EconomyAPI(LuminaryEconomy plugin) {
        this.plugin = plugin;
    }

    // ==================== Balance Operations ====================

    /**
     * Get a player's balance for a specific currency.
     */
    public double getBalance(Player player, String currencyId) {
        return getBalance(player.getUniqueId(), currencyId);
    }

    /**
     * Get a player's balance for a specific currency by UUID.
     */
    public double getBalance(UUID uuid, String currencyId) {
        PlayerData data = plugin.getDataManager().getPlayerData(uuid);
        if (data == null) return 0;
        return data.getBalance(currencyId);
    }

    /**
     * Get a player's balance for the default currency.
     */
    public double getBalance(Player player) {
        Currency defaultCurrency = plugin.getCurrencyManager().getDefaultCurrency();
        if (defaultCurrency == null) return 0;
        return getBalance(player, defaultCurrency.getId());
    }

    /**
     * Set a player's balance for a specific currency.
     */
    public void setBalance(Player player, String currencyId, double amount) {
        setBalance(player.getUniqueId(), player.getName(), currencyId, amount);
    }

    /**
     * Set a player's balance for a specific currency by UUID.
     */
    public void setBalance(UUID uuid, String name, String currencyId, double amount) {
        PlayerData data = plugin.getDataManager().getOrLoadPlayerData(uuid, name);
        data.setBalance(currencyId, amount);
    }

    /**
     * Add to a player's balance for a specific currency.
     * @return The new balance
     */
    public double addBalance(Player player, String currencyId, double amount) {
        return addBalance(player.getUniqueId(), player.getName(), currencyId, amount);
    }

    /**
     * Add to a player's balance for a specific currency by UUID.
     * @return The new balance
     */
    public double addBalance(UUID uuid, String name, String currencyId, double amount) {
        PlayerData data = plugin.getDataManager().getOrLoadPlayerData(uuid, name);
        return data.addBalance(currencyId, amount);
    }

    /**
     * Remove from a player's balance for a specific currency.
     * @return true if successful (had enough balance)
     */
    public boolean removeBalance(Player player, String currencyId, double amount) {
        return removeBalance(player.getUniqueId(), player.getName(), currencyId, amount);
    }

    /**
     * Remove from a player's balance for a specific currency by UUID.
     * @return true if successful (had enough balance)
     */
    public boolean removeBalance(UUID uuid, String name, String currencyId, double amount) {
        PlayerData data = plugin.getDataManager().getOrLoadPlayerData(uuid, name);
        return data.removeBalance(currencyId, amount);
    }

    /**
     * Check if a player has at least the specified amount.
     */
    public boolean hasBalance(Player player, String currencyId, double amount) {
        return hasBalance(player.getUniqueId(), currencyId, amount);
    }

    /**
     * Check if a player has at least the specified amount by UUID.
     */
    public boolean hasBalance(UUID uuid, String currencyId, double amount) {
        PlayerData data = plugin.getDataManager().getPlayerData(uuid);
        if (data == null) return false;
        return data.hasBalance(currencyId, amount);
    }

    // ==================== Currency Operations ====================

    /**
     * Get a currency by ID.
     */
    public Currency getCurrency(String id) {
        return plugin.getCurrencyManager().getCurrency(id);
    }

    /**
     * Get the default currency.
     */
    public Currency getDefaultCurrency() {
        return plugin.getCurrencyManager().getDefaultCurrency();
    }

    /**
     * Check if a currency exists.
     */
    public boolean hasCurrency(String id) {
        return plugin.getCurrencyManager().hasCurrency(id);
    }

    /**
     * Format an amount for a specific currency.
     */
    public String format(String currencyId, double amount) {
        Currency currency = getCurrency(currencyId);
        if (currency == null) return String.valueOf(amount);
        return currency.format(amount);
    }

    // ==================== Convenience Methods ====================

    /**
     * Give tokens to a player (convenience method).
     */
    public double giveTokens(Player player, double amount) {
        return addBalance(player, "tokens", amount);
    }

    /**
     * Give beacons to a player (convenience method).
     */
    public double giveBeacons(Player player, double amount) {
        return addBalance(player, "beacons", amount);
    }

    /**
     * Give gems to a player (convenience method).
     */
    public double giveGems(Player player, double amount) {
        return addBalance(player, "gems", amount);
    }

    /**
     * Take tokens from a player (convenience method).
     */
    public boolean takeTokens(Player player, double amount) {
        return removeBalance(player, "tokens", amount);
    }

    /**
     * Take beacons from a player (convenience method).
     */
    public boolean takeBeacons(Player player, double amount) {
        return removeBalance(player, "beacons", amount);
    }

    /**
     * Take gems from a player (convenience method).
     */
    public boolean takeGems(Player player, double amount) {
        return removeBalance(player, "gems", amount);
    }

    /**
     * Get tokens balance (convenience method).
     */
    public double getTokens(Player player) {
        return getBalance(player, "tokens");
    }

    /**
     * Get beacons balance (convenience method).
     */
    public double getBeacons(Player player) {
        return getBalance(player, "beacons");
    }

    /**
     * Get gems balance (convenience method).
     */
    public double getGems(Player player) {
        return getBalance(player, "gems");
    }

    // ==================== Transaction Methods ====================

    /**
     * Transfer currency between two players.
     * @return true if successful
     */
    public boolean transfer(Player from, Player to, String currencyId, double amount) {
        if (!hasBalance(from, currencyId, amount)) {
            return false;
        }

        removeBalance(from, currencyId, amount);
        addBalance(to, currencyId, amount);
        return true;
    }

    /**
     * Transfer currency with tax.
     * @return The amount received by the target (after tax)
     */
    public double transferWithTax(Player from, Player to, String currencyId, double amount, double taxPercent) {
        if (!hasBalance(from, currencyId, amount)) {
            return -1;
        }

        double tax = amount * (taxPercent / 100.0);
        double received = amount - tax;

        removeBalance(from, currencyId, amount);
        addBalance(to, currencyId, received);

        return received;
    }
}
