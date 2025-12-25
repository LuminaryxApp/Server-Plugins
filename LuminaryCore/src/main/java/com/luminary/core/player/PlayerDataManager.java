package com.luminary.core.player;

import com.luminary.core.LuminaryCore;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player data storage and retrieval.
 */
public class PlayerDataManager {

    private final LuminaryCore plugin;
    private final File dataFolder;
    private final Map<UUID, PlayerData> loadedData = new HashMap<>();

    public PlayerDataManager(LuminaryCore plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    /**
     * Get or load player data.
     */
    public PlayerData getPlayerData(UUID playerId) {
        PlayerData data = loadedData.get(playerId);
        if (data != null) {
            return data;
        }

        data = loadPlayerData(playerId);
        loadedData.put(playerId, data);
        return data;
    }

    /**
     * Get player data for online player.
     */
    public PlayerData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    /**
     * Load player data from file.
     */
    private PlayerData loadPlayerData(UUID playerId) {
        File file = getPlayerFile(playerId);
        if (file.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            return new PlayerData(playerId, config);
        }
        return new PlayerData(playerId);
    }

    /**
     * Save player data to file.
     */
    public void savePlayerData(UUID playerId) {
        PlayerData data = loadedData.get(playerId);
        if (data == null) {
            return;
        }

        File file = getPlayerFile(playerId);
        YamlConfiguration config = new YamlConfiguration();
        data.save(config);

        try {
            config.save(file);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data for " + playerId + ": " + e.getMessage());
        }
    }

    /**
     * Save player data.
     */
    public void savePlayerData(Player player) {
        savePlayerData(player.getUniqueId());
    }

    /**
     * Save all loaded player data.
     */
    public void saveAll() {
        for (UUID playerId : loadedData.keySet()) {
            savePlayerData(playerId);
        }
        plugin.getLogger().info("Saved data for " + loadedData.size() + " players.");
    }

    /**
     * Unload player data from cache.
     */
    public void unloadPlayerData(UUID playerId) {
        savePlayerData(playerId);
        loadedData.remove(playerId);
    }

    /**
     * Handle player join.
     */
    public void handleJoin(Player player) {
        PlayerData data = getPlayerData(player);
        String ip = player.getAddress() != null ? player.getAddress().getAddress().getHostAddress() : "unknown";
        data.startSession(player.getName(), ip);
    }

    /**
     * Handle player quit.
     */
    public void handleQuit(Player player) {
        PlayerData data = getPlayerData(player);
        data.endSession();
        savePlayerData(player);
    }

    /**
     * Get the file for a player's data.
     */
    private File getPlayerFile(UUID playerId) {
        return new File(dataFolder, playerId.toString() + ".yml");
    }

    /**
     * Check if player has any data file.
     */
    public boolean hasDataFile(UUID playerId) {
        return getPlayerFile(playerId).exists();
    }

    /**
     * Get all loaded data.
     */
    public Map<UUID, PlayerData> getLoadedData() {
        return loadedData;
    }
}
