package com.luminary.miners.player;

import com.luminary.miners.miner.PlayerMiner;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Stores all data for a single player.
 */
public class PlayerData {

    private final UUID playerId;
    private final Map<UUID, PlayerMiner> miners = new LinkedHashMap<>();
    private long lastOnlineTime;
    private boolean dirty;

    public PlayerData(UUID playerId) {
        this.playerId = playerId;
        this.lastOnlineTime = System.currentTimeMillis();
        this.dirty = false;
    }

    public PlayerData(UUID playerId, ConfigurationSection section) {
        this.playerId = playerId;
        this.lastOnlineTime = section.getLong("last-online", System.currentTimeMillis());
        this.dirty = false;

        // Load miners
        ConfigurationSection minersSection = section.getConfigurationSection("miners");
        if (minersSection != null) {
            for (String key : minersSection.getKeys(false)) {
                ConfigurationSection minerSection = minersSection.getConfigurationSection(key);
                if (minerSection != null) {
                    PlayerMiner miner = new PlayerMiner(minerSection);
                    miners.put(miner.getMinerId(), miner);
                }
            }
        }
    }

    public void save(ConfigurationSection section) {
        section.set("last-online", lastOnlineTime);

        // Clear and save miners
        section.set("miners", null);
        int index = 0;
        for (PlayerMiner miner : miners.values()) {
            ConfigurationSection minerSection = section.createSection("miners.miner" + index);
            miner.save(minerSection);
            index++;
        }

        dirty = false;
    }

    public UUID getPlayerId() {
        return playerId;
    }

    public Collection<PlayerMiner> getMiners() {
        return Collections.unmodifiableCollection(miners.values());
    }

    public PlayerMiner getMiner(UUID minerId) {
        return miners.get(minerId);
    }

    public List<PlayerMiner> getMinersByType(String typeId) {
        List<PlayerMiner> result = new ArrayList<>();
        for (PlayerMiner miner : miners.values()) {
            if (miner.getTypeId().equalsIgnoreCase(typeId)) {
                result.add(miner);
            }
        }
        return result;
    }

    public int getMinerCount() {
        return miners.size();
    }

    public int getMinerCountByType(String typeId) {
        int count = 0;
        for (PlayerMiner miner : miners.values()) {
            if (miner.getTypeId().equalsIgnoreCase(typeId)) {
                count++;
            }
        }
        return count;
    }

    public boolean addMiner(PlayerMiner miner) {
        miners.put(miner.getMinerId(), miner);
        dirty = true;
        return true;
    }

    public boolean removeMiner(UUID minerId) {
        PlayerMiner removed = miners.remove(minerId);
        if (removed != null) {
            dirty = true;
            return true;
        }
        return false;
    }

    public long getLastOnlineTime() {
        return lastOnlineTime;
    }

    public void setLastOnlineTime(long lastOnlineTime) {
        this.lastOnlineTime = lastOnlineTime;
        this.dirty = true;
    }

    public void updateLastOnlineTime() {
        this.lastOnlineTime = System.currentTimeMillis();
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void markDirty() {
        this.dirty = true;
    }

    /**
     * Get time since last online in milliseconds.
     */
    public long getOfflineTime() {
        return System.currentTimeMillis() - lastOnlineTime;
    }
}
