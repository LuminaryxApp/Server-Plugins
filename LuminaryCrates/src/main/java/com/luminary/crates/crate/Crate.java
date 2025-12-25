package com.luminary.crates.crate;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a crate type with its rewards.
 */
public class Crate {

    private final String id;
    private final String displayName;
    private final List<String> description;
    private final CrateTier tier;
    private final Material blockMaterial;
    private final Material keyMaterial;
    private final String keyName;
    private final List<CrateReward> rewards;
    private final boolean broadcastWin;
    private final double minBroadcastChance;

    public Crate(ConfigurationSection section) {
        this.id = section.getName();
        this.displayName = section.getString("display-name", id);
        this.description = section.getStringList("description");
        this.tier = CrateTier.fromString(section.getString("tier", "COMMON"));

        String blockMat = section.getString("block-material", "CHEST");
        this.blockMaterial = Material.matchMaterial(blockMat) != null ?
                Material.matchMaterial(blockMat) : Material.CHEST;

        String keyMat = section.getString("key-material", "TRIPWIRE_HOOK");
        this.keyMaterial = Material.matchMaterial(keyMat) != null ?
                Material.matchMaterial(keyMat) : Material.TRIPWIRE_HOOK;
        this.keyName = section.getString("key-name", "&e" + displayName + " Key");

        this.broadcastWin = section.getBoolean("broadcast-win", false);
        this.minBroadcastChance = section.getDouble("min-broadcast-chance", 0.01);

        // Load rewards
        this.rewards = new ArrayList<>();
        ConfigurationSection rewardsSection = section.getConfigurationSection("rewards");
        if (rewardsSection != null) {
            for (String rewardKey : rewardsSection.getKeys(false)) {
                ConfigurationSection rewardSection = rewardsSection.getConfigurationSection(rewardKey);
                if (rewardSection != null) {
                    rewards.add(new CrateReward(rewardSection));
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<String> getDescription() {
        return description;
    }

    public CrateTier getTier() {
        return tier;
    }

    public Material getBlockMaterial() {
        return blockMaterial;
    }

    public Material getKeyMaterial() {
        return keyMaterial;
    }

    public String getKeyName() {
        return keyName;
    }

    public List<CrateReward> getRewards() {
        return rewards;
    }

    public boolean shouldBroadcastWin() {
        return broadcastWin;
    }

    public double getMinBroadcastChance() {
        return minBroadcastChance;
    }

    public int getTotalWeight() {
        return rewards.stream().mapToInt(CrateReward::getWeight).sum();
    }

    /**
     * Select a random reward based on weights.
     */
    public CrateReward selectReward() {
        int totalWeight = getTotalWeight();
        if (totalWeight <= 0 || rewards.isEmpty()) {
            return null;
        }

        int random = (int) (Math.random() * totalWeight);
        int cumulative = 0;

        for (CrateReward reward : rewards) {
            cumulative += reward.getWeight();
            if (random < cumulative) {
                return reward;
            }
        }

        return rewards.get(rewards.size() - 1);
    }

    /**
     * Roll for a reward (alias for selectReward).
     */
    public CrateReward rollReward() {
        return selectReward();
    }

    /**
     * Get the material to display this crate in GUIs.
     */
    public Material getDisplayMaterial() {
        return blockMaterial;
    }
}
