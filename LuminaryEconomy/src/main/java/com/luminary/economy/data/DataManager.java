package com.luminary.economy.data;

import com.luminary.economy.LuminaryEconomy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player data storage and retrieval.
 */
public class DataManager {

    private final LuminaryEconomy plugin;
    private final Map<UUID, PlayerData> playerDataCache = new ConcurrentHashMap<>();
    private BukkitTask autoSaveTask;
    private File dataFolder;

    public DataManager(LuminaryEconomy plugin) {
        this.plugin = plugin;
    }

    public void initialize() {
        this.dataFolder = new File(plugin.getDataFolder(), "playerdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }

        // Load data for online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            loadPlayer(player.getUniqueId(), player.getName());
        }

        // Start auto-save task
        if (plugin.getConfigManager().isAutoSaveEnabled()) {
            int interval = plugin.getConfigManager().getAutoSaveInterval() * 20; // Convert to ticks
            autoSaveTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::saveAllDirty,
                    interval, interval);
            plugin.getLogger().info("Auto-save enabled with " +
                    plugin.getConfigManager().getAutoSaveInterval() + "s interval");
        }
    }

    public void shutdown() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        saveAll();
    }

    /**
     * Load or create player data.
     */
    public PlayerData loadPlayer(UUID uuid, String name) {
        PlayerData data = playerDataCache.get(uuid);
        if (data != null) {
            data.setPlayerName(name);
            data.updateLastSeen();
            return data;
        }

        // Try to load from file
        data = loadFromFile(uuid);
        if (data != null) {
            data.setPlayerName(name);
            data.updateLastSeen();
            playerDataCache.put(uuid, data);
            return data;
        }

        // Create new player data
        data = new PlayerData(uuid, name);
        playerDataCache.put(uuid, data);
        return data;
    }

    /**
     * Get player data (must be loaded first).
     */
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataCache.get(uuid);
    }

    /**
     * Get player data, loading if necessary.
     */
    public PlayerData getOrLoadPlayerData(UUID uuid, String name) {
        PlayerData data = playerDataCache.get(uuid);
        if (data == null) {
            data = loadPlayer(uuid, name);
        }
        return data;
    }

    /**
     * Check if player data is loaded.
     */
    public boolean isLoaded(UUID uuid) {
        return playerDataCache.containsKey(uuid);
    }

    /**
     * Unload player data (saves first).
     */
    public void unloadPlayer(UUID uuid) {
        PlayerData data = playerDataCache.remove(uuid);
        if (data != null) {
            saveToFile(data);
        }
    }

    /**
     * Save all dirty player data.
     */
    public void saveAllDirty() {
        for (PlayerData data : playerDataCache.values()) {
            if (data.isDirty()) {
                saveToFile(data);
                data.setDirty(false);
            }
        }
    }

    /**
     * Save all player data.
     */
    public void saveAll() {
        for (PlayerData data : playerDataCache.values()) {
            saveToFile(data);
            data.setDirty(false);
        }
        plugin.getLogger().info("Saved data for " + playerDataCache.size() + " players");
    }

    /**
     * Save specific player data.
     */
    public void savePlayer(UUID uuid) {
        PlayerData data = playerDataCache.get(uuid);
        if (data != null) {
            saveToFile(data);
            data.setDirty(false);
        }
    }

    private PlayerData loadFromFile(UUID uuid) {
        File file = new File(dataFolder, uuid.toString() + ".yml");
        if (!file.exists()) {
            return null;
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            String name = config.getString("name", "Unknown");
            PlayerData data = new PlayerData(uuid, name);

            data.setScoreboardEnabled(config.getBoolean("scoreboard-enabled", true));

            // Load balances
            ConfigurationSection balancesSection = config.getConfigurationSection("balances");
            if (balancesSection != null) {
                Map<String, Double> balances = new HashMap<>();
                for (String key : balancesSection.getKeys(false)) {
                    balances.put(key.toLowerCase(), balancesSection.getDouble(key, 0.0));
                }
                data.loadBalances(balances);
            }

            data.setDirty(false);
            return data;

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load player data for " + uuid + ": " + e.getMessage());
            return null;
        }
    }

    private void saveToFile(PlayerData data) {
        File file = new File(dataFolder, data.getUuid().toString() + ".yml");

        try {
            FileConfiguration config = new YamlConfiguration();

            config.set("uuid", data.getUuid().toString());
            config.set("name", data.getPlayerName());
            config.set("scoreboard-enabled", data.isScoreboardEnabled());
            config.set("last-seen", data.getLastSeen());

            // Save balances
            for (Map.Entry<String, Double> entry : data.getAllBalances().entrySet()) {
                config.set("balances." + entry.getKey(), entry.getValue());
            }

            config.save(file);

        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save player data for " + data.getUuid() + ": " + e.getMessage());
        }
    }

    /**
     * Get all cached player data (for leaderboards, etc.).
     */
    public Collection<PlayerData> getAllCachedData() {
        return Collections.unmodifiableCollection(playerDataCache.values());
    }

    /**
     * Get top players for a specific currency.
     */
    public List<PlayerData> getTopPlayers(String currencyId, int limit) {
        List<PlayerData> allData = new ArrayList<>(playerDataCache.values());

        // Also load offline players if needed for complete leaderboard
        loadAllOfflinePlayers();

        allData = new ArrayList<>(playerDataCache.values());
        allData.sort((a, b) -> Double.compare(b.getBalance(currencyId), a.getBalance(currencyId)));

        return allData.subList(0, Math.min(limit, allData.size()));
    }

    /**
     * Load all offline player data files into cache.
     */
    private void loadAllOfflinePlayers() {
        File[] files = dataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) return;

        for (File file : files) {
            String uuidStr = file.getName().replace(".yml", "");
            try {
                UUID uuid = UUID.fromString(uuidStr);
                if (!playerDataCache.containsKey(uuid)) {
                    PlayerData data = loadFromFile(uuid);
                    if (data != null) {
                        playerDataCache.put(uuid, data);
                    }
                }
            } catch (IllegalArgumentException ignored) {
                // Invalid UUID filename
            }
        }
    }
}
