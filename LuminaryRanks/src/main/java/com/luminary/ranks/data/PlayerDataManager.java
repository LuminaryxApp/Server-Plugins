package com.luminary.ranks.data;

import com.luminary.ranks.LuminaryRanks;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player rank data persistence.
 */
public class PlayerDataManager {

    private final LuminaryRanks plugin;
    private final Map<UUID, PlayerRankData> playerData = new ConcurrentHashMap<>();
    private final File dataFolder;
    private BukkitTask autoSaveTask;

    public PlayerDataManager(LuminaryRanks plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // Load all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayer(player.getUniqueId(), player.getName());
        }

        // Start auto-save task
        int interval = plugin.getConfigManager().getAutoSaveInterval();
        autoSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin,
                this::saveAll, interval * 20L, interval * 20L);
    }

    public PlayerRankData getPlayerData(UUID playerId) {
        return playerData.get(playerId);
    }

    public PlayerRankData getOrLoadPlayerData(UUID playerId, String playerName) {
        PlayerRankData data = playerData.get(playerId);
        if (data == null) {
            data = loadPlayer(playerId, playerName);
        }
        return data;
    }

    public PlayerRankData loadPlayer(UUID playerId, String playerName) {
        File playerFile = new File(dataFolder, playerId.toString() + ".yml");
        PlayerRankData data;

        if (playerFile.exists()) {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
            data = new PlayerRankData(playerId, config);
            // Update name in case it changed
            if (!data.getPlayerName().equals(playerName)) {
                data.setPlayerName(playerName);
            }
        } else {
            // Create new player data
            data = new PlayerRankData(playerId, playerName);
            data.setDirty(true);
        }

        playerData.put(playerId, data);
        return data;
    }

    public void savePlayer(UUID playerId) {
        PlayerRankData data = playerData.get(playerId);
        if (data == null || !data.isDirty()) {
            return;
        }

        File playerFile = new File(dataFolder, playerId.toString() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        data.save(config);

        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save player data for " + playerId + ": " + e.getMessage());
        }
    }

    public void unloadPlayer(UUID playerId) {
        savePlayer(playerId);
        playerData.remove(playerId);
    }

    public void saveAll() {
        for (UUID playerId : playerData.keySet()) {
            savePlayer(playerId);
        }
    }

    public void shutdown() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        saveAll();
    }
}
