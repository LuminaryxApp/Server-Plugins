package com.luminary.enchants.pickaxe;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Defines a pickaxe enchant loaded from configuration.
 */
public class EnchantDefinition {

    private final String id;
    private final String displayName;
    private final String description;
    private final EnchantRarity rarity;
    private final int maxLevel;

    // Upgrade configuration
    private final long upgradeCostBase;
    private final long upgradeCostPerLevel;
    private final double upgradeCostExponentialFactor;

    // Success chance configuration
    private final boolean successChanceEnabled;
    private final double successChanceBase;
    private final double successChancePerLevelDelta;
    private final FailMode failMode;

    // Trigger configuration
    private final Set<EnchantTrigger> triggers;
    private final long cooldownMs;

    // Proc chance configuration
    private final double procChanceBase;
    private final double procChancePerLevel;
    private final double procChanceCap;

    // Custom parameters for each enchant type
    private final Map<String, Object> params;

    // Block filters
    private final Set<Material> blacklist;
    private final Set<Material> whitelist;

    // Anti-abuse settings
    private final boolean ignoreIfCreative;
    private final boolean ignoreIfNoPermission;
    private final String requiresMineRegion;
    private final int maxProcsPerSecond;

    // Conflicts
    private final Set<String> conflicts;

    public EnchantDefinition(ConfigurationSection section) {
        this.id = section.getName();
        this.displayName = section.getString("display", id);
        this.description = section.getString("description", "No description available.");
        this.rarity = EnchantRarity.fromString(section.getString("rarity", "COMMON"));
        this.maxLevel = section.getInt("maxLevel", 100);

        // Upgrade costs
        ConfigurationSection upgradeSection = section.getConfigurationSection("upgrade");
        if (upgradeSection != null) {
            ConfigurationSection costSection = upgradeSection.getConfigurationSection("cost");
            if (costSection != null) {
                this.upgradeCostBase = costSection.getLong("base", 100);
                this.upgradeCostPerLevel = costSection.getLong("perLevel", 50);
                this.upgradeCostExponentialFactor = costSection.getDouble("exponentialFactor", 1.0);
            } else {
                this.upgradeCostBase = 100;
                this.upgradeCostPerLevel = 50;
                this.upgradeCostExponentialFactor = 1.0;
            }

            ConfigurationSection successSection = upgradeSection.getConfigurationSection("successChance");
            if (successSection != null && successSection.getBoolean("enabled", false)) {
                this.successChanceEnabled = true;
                this.successChanceBase = successSection.getDouble("base", 1.0);
                this.successChancePerLevelDelta = successSection.getDouble("perLevelDelta", 0.0);
                this.failMode = FailMode.fromString(successSection.getString("failMode", "NO_CHANGE"));
            } else {
                this.successChanceEnabled = false;
                this.successChanceBase = 1.0;
                this.successChancePerLevelDelta = 0.0;
                this.failMode = FailMode.NONE;
            }
        } else {
            this.upgradeCostBase = 100;
            this.upgradeCostPerLevel = 50;
            this.upgradeCostExponentialFactor = 1.0;
            this.successChanceEnabled = false;
            this.successChanceBase = 1.0;
            this.successChancePerLevelDelta = 0.0;
            this.failMode = FailMode.NONE;
        }

        // Triggers
        this.triggers = EnumSet.noneOf(EnchantTrigger.class);
        List<String> triggerList = section.getStringList("trigger");
        if (triggerList.isEmpty()) {
            String singleTrigger = section.getString("trigger", "BLOCK_BREAK");
            triggers.add(EnchantTrigger.fromString(singleTrigger));
        } else {
            for (String t : triggerList) {
                triggers.add(EnchantTrigger.fromString(t));
            }
        }

        this.cooldownMs = section.getLong("cooldownMs", 0);

        // Proc chance
        ConfigurationSection chanceSection = section.getConfigurationSection("chance");
        if (chanceSection != null) {
            this.procChanceBase = chanceSection.getDouble("base", 0.1);
            this.procChancePerLevel = chanceSection.getDouble("perLevel", 0.01);
            this.procChanceCap = chanceSection.getDouble("cap", 1.0);
        } else {
            this.procChanceBase = 0.1;
            this.procChancePerLevel = 0.01;
            this.procChanceCap = 1.0;
        }

        // Custom params
        ConfigurationSection paramsSection = section.getConfigurationSection("params");
        if (paramsSection != null) {
            this.params = new HashMap<>();
            for (String key : paramsSection.getKeys(false)) {
                params.put(key, paramsSection.get(key));
            }
        } else {
            this.params = Collections.emptyMap();
        }

        // Block filters
        this.blacklist = parseMaterialSet(section.getStringList("blacklist"));
        this.whitelist = parseMaterialSet(section.getStringList("whitelist"));

        // Anti-abuse
        ConfigurationSection antiAbuseSection = section.getConfigurationSection("antiAbuse");
        if (antiAbuseSection != null) {
            this.ignoreIfCreative = antiAbuseSection.getBoolean("ignoreIfCreative", true);
            this.ignoreIfNoPermission = antiAbuseSection.getBoolean("ignoreIfNoPermission", false);
            this.requiresMineRegion = antiAbuseSection.getString("requiresMineRegion", null);
            this.maxProcsPerSecond = antiAbuseSection.getInt("maxProcsPerSecond", 20);
        } else {
            this.ignoreIfCreative = true;
            this.ignoreIfNoPermission = false;
            this.requiresMineRegion = null;
            this.maxProcsPerSecond = 20;
        }

        // Conflicts
        this.conflicts = new HashSet<>(section.getStringList("conflicts"));
    }

    private Set<Material> parseMaterialSet(List<String> list) {
        Set<Material> materials = EnumSet.noneOf(Material.class);
        for (String name : list) {
            try {
                materials.add(Material.valueOf(name.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        return materials;
    }

    // Getters

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public EnchantRarity getRarity() {
        return rarity;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public long getUpgradeCostBase() {
        return upgradeCostBase;
    }

    public long getUpgradeCostPerLevel() {
        return upgradeCostPerLevel;
    }

    public double getUpgradeCostExponentialFactor() {
        return upgradeCostExponentialFactor;
    }

    public boolean isSuccessChanceEnabled() {
        return successChanceEnabled;
    }

    public double getSuccessChanceBase() {
        return successChanceBase;
    }

    public double getSuccessChancePerLevelDelta() {
        return successChancePerLevelDelta;
    }

    public FailMode getFailMode() {
        return failMode;
    }

    public Set<EnchantTrigger> getTriggers() {
        return triggers;
    }

    public boolean hasTrigger(EnchantTrigger trigger) {
        return triggers.contains(trigger);
    }

    public long getCooldownMs() {
        return cooldownMs;
    }

    public double getProcChanceBase() {
        return procChanceBase;
    }

    public double getProcChancePerLevel() {
        return procChancePerLevel;
    }

    public double getProcChanceCap() {
        return procChanceCap;
    }

    /**
     * Calculate proc chance for a given level with optional beacon multiplier.
     */
    public double calculateProcChance(int level, double beaconMultiplier) {
        double chance = procChanceBase + (procChancePerLevel * level);
        chance *= beaconMultiplier;
        return Math.min(chance, procChanceCap);
    }

    public Map<String, Object> getParams() {
        return params;
    }

    @SuppressWarnings("unchecked")
    public <T> T getParam(String key, T defaultValue) {
        Object value = params.get(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return (T) value;
        } catch (ClassCastException e) {
            return defaultValue;
        }
    }

    public int getParamInt(String key, int defaultValue) {
        Object value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    public double getParamDouble(String key, double defaultValue) {
        Object value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    public long getParamLong(String key, long defaultValue) {
        Object value = params.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return defaultValue;
    }

    public String getParamString(String key, String defaultValue) {
        Object value = params.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }

    public boolean getParamBoolean(String key, boolean defaultValue) {
        Object value = params.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    public List<String> getParamStringList(String key) {
        Object value = params.get(key);
        if (value instanceof List) {
            return (List<String>) value;
        }
        return Collections.emptyList();
    }

    public Set<Material> getBlacklist() {
        return blacklist;
    }

    public Set<Material> getWhitelist() {
        return whitelist;
    }

    public boolean isBlockAllowed(Material material) {
        if (!whitelist.isEmpty()) {
            return whitelist.contains(material);
        }
        return !blacklist.contains(material);
    }

    public boolean isIgnoreIfCreative() {
        return ignoreIfCreative;
    }

    public boolean isIgnoreIfNoPermission() {
        return ignoreIfNoPermission;
    }

    public String getRequiresMineRegion() {
        return requiresMineRegion;
    }

    public int getMaxProcsPerSecond() {
        return maxProcsPerSecond;
    }

    public Set<String> getConflicts() {
        return conflicts;
    }

    public boolean conflictsWith(String enchantId) {
        return conflicts.contains(enchantId);
    }

    /**
     * Calculate the total cost to upgrade from currentLevel by levelsToAdd levels.
     */
    public long calculateUpgradeCost(int currentLevel, int levelsToAdd) {
        long totalCost = 0;
        for (int i = 1; i <= levelsToAdd; i++) {
            int targetLevel = currentLevel + i;
            long levelCost;
            if (upgradeCostExponentialFactor != 1.0) {
                levelCost = (long) (upgradeCostBase * Math.pow(upgradeCostExponentialFactor, targetLevel - 1));
            } else {
                levelCost = upgradeCostBase + (upgradeCostPerLevel * (targetLevel - 1));
            }
            totalCost += levelCost;
        }
        return totalCost;
    }

    /**
     * Calculate success chance for upgrading to a target level.
     */
    public double calculateSuccessChance(int targetLevel) {
        if (!successChanceEnabled) {
            return 1.0;
        }
        double chance = successChanceBase + (successChancePerLevelDelta * (targetLevel - 1));
        return Math.max(0.0, Math.min(1.0, chance));
    }

    public enum FailMode {
        NONE,           // No failure possible (100% success)
        NO_CHANGE,      // On failure, level stays the same
        DOWNGRADE,      // On failure, lose one level
        REMOVE;         // On failure, enchant is removed

        public static FailMode fromString(String name) {
            try {
                return valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return NO_CHANGE;
            }
        }
    }
}
