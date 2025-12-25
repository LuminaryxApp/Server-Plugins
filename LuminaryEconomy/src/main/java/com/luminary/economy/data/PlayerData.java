package com.luminary.economy.data;

import com.luminary.economy.LuminaryEconomy;
import com.luminary.economy.currency.Currency;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Stores economy data for a single player.
 */
public class PlayerData {

    private final UUID uuid;
    private String playerName;
    private final Map<String, Double> balances = new HashMap<>();
    private boolean scoreboardEnabled = true;
    private long lastSeen;
    private boolean dirty = false;

    public PlayerData(UUID uuid, String playerName) {
        this.uuid = uuid;
        this.playerName = playerName;
        this.lastSeen = System.currentTimeMillis();
        initializeBalances();
    }

    /**
     * Initialize all currency balances with starting values.
     */
    private void initializeBalances() {
        LuminaryEconomy plugin = LuminaryEconomy.getInstance();
        for (Currency currency : plugin.getCurrencyManager().getAllCurrencies()) {
            balances.putIfAbsent(currency.getId(), currency.getStartingBalance());
        }
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        this.dirty = true;
    }

    public boolean isScoreboardEnabled() {
        return scoreboardEnabled;
    }

    public void setScoreboardEnabled(boolean enabled) {
        this.scoreboardEnabled = enabled;
        this.dirty = true;
    }

    public long getLastSeen() {
        return lastSeen;
    }

    public void updateLastSeen() {
        this.lastSeen = System.currentTimeMillis();
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    // Balance operations

    /**
     * Get balance for a specific currency.
     */
    public double getBalance(String currencyId) {
        return balances.getOrDefault(currencyId.toLowerCase(), 0.0);
    }

    /**
     * Get balance for a specific currency.
     */
    public double getBalance(Currency currency) {
        return getBalance(currency.getId());
    }

    /**
     * Set balance for a specific currency.
     */
    public void setBalance(String currencyId, double amount) {
        Currency currency = LuminaryEconomy.getInstance().getCurrencyManager().getCurrency(currencyId);
        if (currency != null) {
            amount = currency.clamp(amount);
        }
        balances.put(currencyId.toLowerCase(), amount);
        this.dirty = true;
    }

    /**
     * Set balance for a specific currency.
     */
    public void setBalance(Currency currency, double amount) {
        setBalance(currency.getId(), currency.clamp(amount));
    }

    /**
     * Add to balance for a specific currency.
     */
    public double addBalance(String currencyId, double amount) {
        double current = getBalance(currencyId);
        double newBalance = current + amount;
        setBalance(currencyId, newBalance);
        return getBalance(currencyId);
    }

    /**
     * Add to balance for a specific currency.
     */
    public double addBalance(Currency currency, double amount) {
        return addBalance(currency.getId(), amount);
    }

    /**
     * Remove from balance for a specific currency.
     * Returns true if successful (had enough balance).
     */
    public boolean removeBalance(String currencyId, double amount) {
        double current = getBalance(currencyId);
        Currency currency = LuminaryEconomy.getInstance().getCurrencyManager().getCurrency(currencyId);

        if (currency != null && !currency.isAllowNegative() && current < amount) {
            return false;
        }

        setBalance(currencyId, current - amount);
        return true;
    }

    /**
     * Remove from balance for a specific currency.
     */
    public boolean removeBalance(Currency currency, double amount) {
        return removeBalance(currency.getId(), amount);
    }

    /**
     * Check if player has at least the specified amount.
     */
    public boolean hasBalance(String currencyId, double amount) {
        return getBalance(currencyId) >= amount;
    }

    /**
     * Check if player has at least the specified amount.
     */
    public boolean hasBalance(Currency currency, double amount) {
        return hasBalance(currency.getId(), amount);
    }

    /**
     * Get all balances as a map.
     */
    public Map<String, Double> getAllBalances() {
        return new HashMap<>(balances);
    }

    /**
     * Load balances from a map.
     */
    public void loadBalances(Map<String, Double> data) {
        balances.putAll(data);
        initializeBalances(); // Ensure any new currencies get starting balance
    }

    /**
     * Reset all balances to starting values.
     */
    public void resetBalances() {
        balances.clear();
        initializeBalances();
        this.dirty = true;
    }

    /**
     * Reset a specific currency to starting value.
     */
    public void resetBalance(String currencyId) {
        Currency currency = LuminaryEconomy.getInstance().getCurrencyManager().getCurrency(currencyId);
        if (currency != null) {
            setBalance(currencyId, currency.getStartingBalance());
        }
    }
}
