package com.luminary.miners.miner;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a type of miner (e.g., Token Robot, Gem Golem).
 */
public class MinerType {

    private final String id;
    private String displayName;
    private MinerCategory category;
    private ResourceType resourceType;
    private double baseProduction; // Per hour
    private double baseStorage;
    private double purchaseCost;
    private String purchaseCurrency;
    private Material icon;
    private String description;
    private final Map<Integer, MinerTier> tiers = new LinkedHashMap<>();
    private int maxTier;

    public MinerType(String id) {
        this.id = id.toLowerCase();
        this.displayName = id;
        this.category = MinerCategory.ROBOT;
        this.resourceType = ResourceType.TOKENS;
        this.baseProduction = 100;
        this.baseStorage = 1000;
        this.purchaseCost = 5000;
        this.purchaseCurrency = "tokens";
        this.icon = Material.IRON_BLOCK;
        this.description = "A basic miner";
        this.maxTier = 5;
        initializeDefaultTiers();
    }

    public MinerType(String id, ConfigurationSection section) {
        this.id = id.toLowerCase();
        this.displayName = section.getString("display-name", id);
        this.category = MinerCategory.fromString(section.getString("category", "ROBOT"));
        this.resourceType = ResourceType.fromString(section.getString("resource-type", "TOKENS"));
        this.baseProduction = section.getDouble("base-production", 100);
        this.baseStorage = section.getDouble("base-storage", 1000);
        this.purchaseCost = section.getDouble("purchase-cost", 5000);
        this.purchaseCurrency = section.getString("purchase-currency", "tokens");
        this.description = section.getString("description", "A miner");
        this.maxTier = section.getInt("max-tier", 5);

        String iconName = section.getString("icon", "IRON_BLOCK");
        try {
            this.icon = Material.valueOf(iconName.toUpperCase());
        } catch (IllegalArgumentException e) {
            this.icon = Material.IRON_BLOCK;
        }

        // Load tiers
        ConfigurationSection tiersSection = section.getConfigurationSection("tiers");
        if (tiersSection != null) {
            for (String tierKey : tiersSection.getKeys(false)) {
                try {
                    int level = Integer.parseInt(tierKey);
                    ConfigurationSection tierSection = tiersSection.getConfigurationSection(tierKey);
                    if (tierSection != null) {
                        tiers.put(level, new MinerTier(level, tierSection));
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }

        // Fill in missing tiers with defaults
        for (int i = 1; i <= maxTier; i++) {
            if (!tiers.containsKey(i)) {
                tiers.put(i, new MinerTier(i));
            }
        }
    }

    private void initializeDefaultTiers() {
        for (int i = 1; i <= maxTier; i++) {
            tiers.put(i, new MinerTier(i));
        }
    }

    public void save(ConfigurationSection section) {
        section.set("display-name", displayName);
        section.set("category", category.name());
        section.set("resource-type", resourceType.name());
        section.set("base-production", baseProduction);
        section.set("base-storage", baseStorage);
        section.set("purchase-cost", purchaseCost);
        section.set("purchase-currency", purchaseCurrency);
        section.set("icon", icon.name());
        section.set("description", description);
        section.set("max-tier", maxTier);

        // Save tiers
        for (Map.Entry<Integer, MinerTier> entry : tiers.entrySet()) {
            ConfigurationSection tierSection = section.createSection("tiers." + entry.getKey());
            entry.getValue().save(tierSection);
        }
    }

    /**
     * Calculate production per hour at a specific tier.
     */
    public double getProductionPerHour(int tier) {
        MinerTier minerTier = tiers.get(tier);
        double multiplier = minerTier != null ? minerTier.getProductionMultiplier() : 1.0;
        return baseProduction * multiplier;
    }

    /**
     * Calculate production per day at a specific tier.
     */
    public double getProductionPerDay(int tier) {
        return getProductionPerHour(tier) * 24;
    }

    /**
     * Calculate production per week at a specific tier.
     */
    public double getProductionPerWeek(int tier) {
        return getProductionPerDay(tier) * 7;
    }

    /**
     * Get storage capacity at a specific tier.
     */
    public double getStorageCapacity(int tier) {
        MinerTier minerTier = tiers.get(tier);
        double multiplier = minerTier != null ? minerTier.getStorageMultiplier() : 1.0;
        return baseStorage * multiplier;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public MinerCategory getCategory() {
        return category;
    }

    public void setCategory(MinerCategory category) {
        this.category = category;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public void setResourceType(ResourceType resourceType) {
        this.resourceType = resourceType;
    }

    public double getBaseProduction() {
        return baseProduction;
    }

    public void setBaseProduction(double baseProduction) {
        this.baseProduction = baseProduction;
    }

    public double getBaseStorage() {
        return baseStorage;
    }

    public void setBaseStorage(double baseStorage) {
        this.baseStorage = baseStorage;
    }

    public double getPurchaseCost() {
        return purchaseCost;
    }

    public void setPurchaseCost(double purchaseCost) {
        this.purchaseCost = purchaseCost;
    }

    public String getPurchaseCurrency() {
        return purchaseCurrency;
    }

    public void setPurchaseCurrency(String purchaseCurrency) {
        this.purchaseCurrency = purchaseCurrency;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMaxTier() {
        return maxTier;
    }

    public void setMaxTier(int maxTier) {
        this.maxTier = maxTier;
    }

    public MinerTier getTier(int level) {
        return tiers.get(level);
    }

    public Map<Integer, MinerTier> getTiers() {
        return tiers;
    }
}
