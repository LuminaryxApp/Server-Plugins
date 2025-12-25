package com.luminary.economy.currency;

import com.luminary.economy.LuminaryEconomy;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

/**
 * Manages all currency types.
 */
public class CurrencyManager {

    private final LuminaryEconomy plugin;
    private final Map<String, Currency> currencies = new LinkedHashMap<>();
    private Currency defaultCurrency;

    public CurrencyManager(LuminaryEconomy plugin) {
        this.plugin = plugin;
    }

    public void loadCurrencies() {
        currencies.clear();

        FileConfiguration config = plugin.getConfigManager().getCurrenciesConfig();
        ConfigurationSection currenciesSection = config.getConfigurationSection("currencies");

        if (currenciesSection == null) {
            plugin.getLogger().warning("No currencies section found in currencies.yml!");
            createDefaultCurrency();
            return;
        }

        for (String key : currenciesSection.getKeys(false)) {
            ConfigurationSection section = currenciesSection.getConfigurationSection(key);
            if (section != null) {
                try {
                    Currency currency = new Currency(section);
                    currencies.put(currency.getId(), currency);
                    plugin.getLogger().info("Loaded currency: " + currency.getId() +
                            " (" + currency.getDisplayName() + ")");
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load currency '" + key + "': " + e.getMessage());
                }
            }
        }

        // Set default currency
        String defaultId = plugin.getConfigManager().getDefaultCurrency();
        defaultCurrency = currencies.get(defaultId);
        if (defaultCurrency == null && !currencies.isEmpty()) {
            defaultCurrency = currencies.values().iterator().next();
            plugin.getLogger().warning("Default currency '" + defaultId + "' not found, using: " +
                    defaultCurrency.getId());
        }

        if (currencies.isEmpty()) {
            plugin.getLogger().warning("No currencies loaded! Creating default...");
            createDefaultCurrency();
        }
    }

    private void createDefaultCurrency() {
        // Create a basic tokens currency if none exist
        // This is a fallback - normally currencies come from config
        plugin.getLogger().info("Creating fallback tokens currency");
    }

    public Currency getCurrency(String id) {
        return currencies.get(id.toLowerCase());
    }

    public Currency getDefaultCurrency() {
        return defaultCurrency;
    }

    public Collection<Currency> getAllCurrencies() {
        return Collections.unmodifiableCollection(currencies.values());
    }

    public Set<String> getCurrencyIds() {
        return Collections.unmodifiableSet(currencies.keySet());
    }

    public boolean hasCurrency(String id) {
        return currencies.containsKey(id.toLowerCase());
    }

    public int getCurrencyCount() {
        return currencies.size();
    }

    /**
     * Get currency by name (case-insensitive, matches id or display name).
     */
    public Currency findCurrency(String name) {
        // Try exact ID match first
        Currency currency = currencies.get(name.toLowerCase());
        if (currency != null) return currency;

        // Try display name match
        String lowerName = name.toLowerCase();
        for (Currency c : currencies.values()) {
            if (c.getDisplayName().toLowerCase().equals(lowerName)) {
                return c;
            }
        }

        // Try partial match
        for (Currency c : currencies.values()) {
            if (c.getId().contains(lowerName) ||
                    c.getDisplayName().toLowerCase().contains(lowerName)) {
                return c;
            }
        }

        return null;
    }
}
