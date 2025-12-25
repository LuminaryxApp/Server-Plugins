package com.luminary.backpacks.data;

import com.luminary.backpacks.LuminaryBackpacks;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final LuminaryBackpacks plugin;
    private final Map<UUID, PlayerBackpackData> playerData = new ConcurrentHashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    public PlayerDataManager(LuminaryBackpacks plugin) {
        this.plugin = plugin;
        loadDataFile();
    }

    private void loadDataFile() {
        dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!dataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create playerdata.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void loadPlayer(UUID uuid) {
        String uuidStr = uuid.toString();
        ConfigurationSection section = dataConfig.getConfigurationSection(uuidStr);

        PlayerBackpackData data;
        if (section != null) {
            data = new PlayerBackpackData(uuid, section);
        } else {
            data = new PlayerBackpackData(uuid);
        }

        playerData.put(uuid, data);
    }

    public void unloadPlayer(UUID uuid) {
        PlayerBackpackData data = playerData.remove(uuid);
        if (data != null && data.isDirty()) {
            savePlayerData(uuid, data);
        }
    }

    public PlayerBackpackData getPlayerData(UUID uuid) {
        return playerData.get(uuid);
    }

    public void savePlayer(UUID uuid) {
        PlayerBackpackData data = playerData.get(uuid);
        if (data != null) {
            savePlayerData(uuid, data);
        }
    }

    private void savePlayerData(UUID uuid, PlayerBackpackData data) {
        ConfigurationSection section = dataConfig.createSection(uuid.toString());
        data.save(section);

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save player data for " + uuid + ": " + e.getMessage());
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, PlayerBackpackData> entry : playerData.entrySet()) {
            PlayerBackpackData data = entry.getValue();
            if (data.isDirty()) {
                ConfigurationSection section = dataConfig.createSection(entry.getKey().toString());
                data.save(section);
            }
        }

        try {
            dataConfig.save(dataFile);
            plugin.getLogger().info("Saved all player backpack data.");
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save playerdata.yml: " + e.getMessage());
        }
    }

    public boolean isAutoPickupEnabled(UUID uuid) {
        PlayerBackpackData data = playerData.get(uuid);
        return data != null && data.isAutoPickupEnabled();
    }

    public boolean isAutoSellEnabled(UUID uuid) {
        PlayerBackpackData data = playerData.get(uuid);
        return data != null && data.isAutoSellEnabled();
    }

    public void setAutoPickup(UUID uuid, boolean enabled) {
        PlayerBackpackData data = playerData.get(uuid);
        if (data != null) {
            data.setAutoPickup(enabled);
        }
    }

    public void setAutoSell(UUID uuid, boolean enabled) {
        PlayerBackpackData data = playerData.get(uuid);
        if (data != null) {
            data.setAutoSell(enabled);
        }
    }

    public void toggleAutoPickup(UUID uuid) {
        PlayerBackpackData data = playerData.get(uuid);
        if (data != null) {
            data.setAutoPickup(!data.isAutoPickupEnabled());
        }
    }

    public void toggleAutoSell(UUID uuid) {
        PlayerBackpackData data = playerData.get(uuid);
        if (data != null) {
            data.setAutoSell(!data.isAutoSellEnabled());
        }
    }

    public boolean isLoaded(UUID uuid) {
        return playerData.containsKey(uuid);
    }

    public void resetPlayer(UUID uuid) {
        PlayerBackpackData data = playerData.get(uuid);
        if (data != null) {
            data.setTier(plugin.getConfigManager().getDefaultTier());
            data.clear();
            data.setAutoPickup(true);
            data.setAutoSell(false);
        }
    }
}
