package com.luminary.miners.miner;

import org.bukkit.configuration.ConfigurationSection;

import java.util.UUID;

/**
 * Represents a miner owned by a player.
 */
public class PlayerMiner {

    private final UUID minerId;
    private final String typeId;
    private int tier;
    private double storedResources;
    private long lastCollectionTime;
    private boolean active;

    public PlayerMiner(String typeId) {
        this.minerId = UUID.randomUUID();
        this.typeId = typeId;
        this.tier = 1;
        this.storedResources = 0;
        this.lastCollectionTime = System.currentTimeMillis();
        this.active = true;
    }

    public PlayerMiner(ConfigurationSection section) {
        this.minerId = UUID.fromString(section.getString("id", UUID.randomUUID().toString()));
        this.typeId = section.getString("type", "token_robot");
        this.tier = section.getInt("tier", 1);
        this.storedResources = section.getDouble("stored", 0);
        this.lastCollectionTime = section.getLong("last-collection", System.currentTimeMillis());
        this.active = section.getBoolean("active", true);
    }

    public void save(ConfigurationSection section) {
        section.set("id", minerId.toString());
        section.set("type", typeId);
        section.set("tier", tier);
        section.set("stored", storedResources);
        section.set("last-collection", lastCollectionTime);
        section.set("active", active);
    }

    /**
     * Add resources to storage.
     * @return Amount that couldn't fit (overflow)
     */
    public double addResources(double amount, double maxStorage) {
        double space = maxStorage - storedResources;
        if (amount <= space) {
            storedResources += amount;
            return 0;
        } else {
            storedResources = maxStorage;
            return amount - space;
        }
    }

    /**
     * Collect all stored resources.
     * @return Amount collected
     */
    public double collectResources() {
        double collected = storedResources;
        storedResources = 0;
        lastCollectionTime = System.currentTimeMillis();
        return collected;
    }

    /**
     * Upgrade to next tier.
     * @return true if upgraded successfully
     */
    public boolean upgrade(int maxTier) {
        if (tier < maxTier) {
            tier++;
            return true;
        }
        return false;
    }

    // Getters and setters
    public UUID getMinerId() {
        return minerId;
    }

    public String getTypeId() {
        return typeId;
    }

    public int getTier() {
        return tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public double getStoredResources() {
        return storedResources;
    }

    public void setStoredResources(double storedResources) {
        this.storedResources = storedResources;
    }

    public long getLastCollectionTime() {
        return lastCollectionTime;
    }

    public void setLastCollectionTime(long lastCollectionTime) {
        this.lastCollectionTime = lastCollectionTime;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Get time since last collection in milliseconds.
     */
    public long getTimeSinceCollection() {
        return System.currentTimeMillis() - lastCollectionTime;
    }

    /**
     * Check if storage is full.
     */
    public boolean isStorageFull(double maxStorage) {
        return storedResources >= maxStorage;
    }

    /**
     * Get storage percentage.
     */
    public double getStoragePercentage(double maxStorage) {
        if (maxStorage <= 0) return 100;
        return (storedResources / maxStorage) * 100;
    }
}
