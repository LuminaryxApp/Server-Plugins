package com.luminary.miners.miner;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Represents a tier level for miners.
 */
public class MinerTier {

    private final int level;
    private String displayName;
    private String color;
    private double productionMultiplier;
    private double storageMultiplier;
    private double upgradeCost;
    private String upgradeCurrency; // tokens, beacons, gems
    private Material icon;

    public MinerTier(int level) {
        this.level = level;
        this.displayName = "Tier " + level;
        this.color = "&7";
        this.productionMultiplier = 1.0 + (level - 1) * 0.5;
        this.storageMultiplier = 1.0 + (level - 1) * 0.5;
        this.upgradeCost = 1000 * Math.pow(2, level - 1);
        this.upgradeCurrency = "tokens";
        this.icon = Material.IRON_INGOT;
    }

    public MinerTier(int level, ConfigurationSection section) {
        this.level = level;
        this.displayName = section.getString("display-name", "Tier " + level);
        this.color = section.getString("color", "&7");
        this.productionMultiplier = section.getDouble("production-multiplier", 1.0 + (level - 1) * 0.5);
        this.storageMultiplier = section.getDouble("storage-multiplier", 1.0 + (level - 1) * 0.5);
        this.upgradeCost = section.getDouble("upgrade-cost", 1000 * Math.pow(2, level - 1));
        this.upgradeCurrency = section.getString("upgrade-currency", "tokens");

        String iconName = section.getString("icon", "IRON_INGOT");
        try {
            this.icon = Material.valueOf(iconName.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.icon = Material.IRON_INGOT;
        }
    }

    public void save(ConfigurationSection section) {
        section.set("display-name", displayName);
        section.set("color", color);
        section.set("production-multiplier", productionMultiplier);
        section.set("storage-multiplier", storageMultiplier);
        section.set("upgrade-cost", upgradeCost);
        section.set("upgrade-currency", upgradeCurrency);
        section.set("icon", icon.name());
    }

    // Getters and setters
    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getProductionMultiplier() {
        return productionMultiplier;
    }

    public void setProductionMultiplier(double productionMultiplier) {
        this.productionMultiplier = productionMultiplier;
    }

    public double getStorageMultiplier() {
        return storageMultiplier;
    }

    public void setStorageMultiplier(double storageMultiplier) {
        this.storageMultiplier = storageMultiplier;
    }

    public double getUpgradeCost() {
        return upgradeCost;
    }

    public void setUpgradeCost(double upgradeCost) {
        this.upgradeCost = upgradeCost;
    }

    public String getUpgradeCurrency() {
        return upgradeCurrency;
    }

    public void setUpgradeCurrency(String upgradeCurrency) {
        this.upgradeCurrency = upgradeCurrency;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public String getFormattedName() {
        return color + displayName;
    }
}
