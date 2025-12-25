package com.luminary.economy.currency;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Represents a currency type in the economy system.
 */
public class Currency {

    private final String id;
    private final String displayName;
    private final String symbol;
    private final String color;
    private final Material icon;
    private final double startingBalance;
    private final double maxBalance;
    private final double minBalance;
    private final boolean allowNegative;
    private final boolean payable;
    private final int decimalPlaces;
    private final String format;

    public Currency(ConfigurationSection section) {
        this.id = section.getName().toLowerCase();
        this.displayName = section.getString("display-name", id);
        this.symbol = section.getString("symbol", "$");
        this.color = section.getString("color", "&f");

        String iconMat = section.getString("icon", "GOLD_INGOT");
        this.icon = Material.matchMaterial(iconMat) != null ?
                Material.matchMaterial(iconMat) : Material.GOLD_INGOT;

        this.startingBalance = section.getDouble("starting-balance", 0.0);
        this.maxBalance = section.getDouble("max-balance", Double.MAX_VALUE);
        this.minBalance = section.getDouble("min-balance", 0.0);
        this.allowNegative = section.getBoolean("allow-negative", false);
        this.payable = section.getBoolean("payable", true);
        this.decimalPlaces = section.getInt("decimal-places", 0);
        this.format = section.getString("format", "{symbol}{amount}");
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getColor() {
        return color;
    }

    public String getColoredName() {
        return color + displayName;
    }

    public Material getIcon() {
        return icon;
    }

    public double getStartingBalance() {
        return startingBalance;
    }

    public double getMaxBalance() {
        return maxBalance;
    }

    public double getMinBalance() {
        return minBalance;
    }

    public boolean isAllowNegative() {
        return allowNegative;
    }

    public boolean isPayable() {
        return payable;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    /**
     * Format an amount according to this currency's format.
     */
    public String format(double amount) {
        String formattedAmount;
        if (decimalPlaces == 0) {
            formattedAmount = String.format("%,d", (long) amount);
        } else {
            formattedAmount = String.format("%,." + decimalPlaces + "f", amount);
        }

        return format
                .replace("{symbol}", symbol)
                .replace("{amount}", formattedAmount)
                .replace("{name}", displayName)
                .replace("{color}", color);
    }

    /**
     * Format an amount with color codes.
     */
    public String formatColored(double amount) {
        return color + format(amount);
    }

    /**
     * Clamp a value to the currency's min/max bounds.
     */
    public double clamp(double value) {
        if (value > maxBalance) return maxBalance;
        if (value < minBalance && !allowNegative) return minBalance;
        if (value < minBalance) return minBalance;
        return value;
    }

    /**
     * Check if a transaction amount is valid.
     */
    public boolean isValidAmount(double amount) {
        return amount > 0;
    }
}
