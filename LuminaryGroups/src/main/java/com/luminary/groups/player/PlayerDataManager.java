package com.luminary.groups.player;

import com.luminary.groups.LuminaryGroups;
import com.luminary.groups.group.Group;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player group data storage and retrieval.
 */
public class PlayerDataManager {

    private final LuminaryGroups plugin;
    private final Map<UUID, PlayerGroupData> playerData = new ConcurrentHashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    public PlayerDataManager(LuminaryGroups plugin) {
        this.plugin = plugin;
        loadDataFile();
    }

    private void loadDataFile() {
        dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create playerdata.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
    }

    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        String uuidStr = uuid.toString();

        ConfigurationSection section = dataConfig.getConfigurationSection(uuidStr);

        PlayerGroupData data;
        if (section != null) {
            data = new PlayerGroupData(uuid, section);
            // Update name if changed
            if (!data.getPlayerName().equals(player.getName())) {
                data.setPlayerName(player.getName());
            }
        } else {
            data = new PlayerGroupData(uuid, player.getName());
            // Add to default group
            Group defaultGroup = plugin.getGroupManager().getDefaultGroup();
            if (defaultGroup != null) {
                data.addGroup(defaultGroup.getId());
                data.setPrimaryGroup(defaultGroup.getId());
            }
        }

        playerData.put(uuid, data);
    }

    public void unloadPlayer(UUID uuid) {
        PlayerGroupData data = playerData.remove(uuid);
        if (data != null && data.isDirty()) {
            savePlayerData(uuid, data);
        }
    }

    public PlayerGroupData getPlayerData(UUID uuid) {
        return playerData.get(uuid);
    }

    public PlayerGroupData getPlayerData(Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public void savePlayer(UUID uuid) {
        PlayerGroupData data = playerData.get(uuid);
        if (data != null) {
            savePlayerData(uuid, data);
        }
    }

    private void savePlayerData(UUID uuid, PlayerGroupData data) {
        ConfigurationSection section = dataConfig.createSection(uuid.toString());
        data.save(section);

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save player data for " + uuid + ": " + e.getMessage());
        }
    }

    public void saveAll() {
        for (Map.Entry<UUID, PlayerGroupData> entry : playerData.entrySet()) {
            PlayerGroupData data = entry.getValue();
            if (data.isDirty()) {
                ConfigurationSection section = dataConfig.createSection(entry.getKey().toString());
                data.save(section);
            }
        }

        try {
            dataConfig.save(dataFile);
            plugin.getLogger().info("Saved all player data.");
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save playerdata.yml: " + e.getMessage());
        }
    }

    /**
     * Get a player's effective group (primary or highest weight).
     */
    public Group getEffectiveGroup(UUID uuid) {
        PlayerGroupData data = playerData.get(uuid);
        if (data == null) {
            return plugin.getGroupManager().getDefaultGroup();
        }

        // If player has a primary group set, use it
        String primaryId = data.getPrimaryGroup();
        if (primaryId != null) {
            Group primary = plugin.getGroupManager().getGroup(primaryId);
            if (primary != null) {
                return primary;
            }
        }

        // Otherwise, use highest weight group
        return plugin.getGroupManager().getHighestPriorityGroup(data.getGroups());
    }

    /**
     * Get a player's display prefix.
     */
    public String getPrefix(UUID uuid) {
        PlayerGroupData data = playerData.get(uuid);
        if (data != null && data.getCustomPrefix() != null) {
            return data.getCustomPrefix();
        }

        Group group = getEffectiveGroup(uuid);
        return group != null ? group.getPrefix() : "";
    }

    /**
     * Get a player's display suffix.
     */
    public String getSuffix(UUID uuid) {
        PlayerGroupData data = playerData.get(uuid);
        if (data != null && data.getCustomSuffix() != null) {
            return data.getCustomSuffix();
        }

        Group group = getEffectiveGroup(uuid);
        return group != null ? group.getSuffix() : "";
    }

    /**
     * Get a player's name color.
     */
    public String getNameColor(UUID uuid) {
        PlayerGroupData data = playerData.get(uuid);
        if (data != null && data.getCustomNameColor() != null) {
            return data.getCustomNameColor();
        }

        Group group = getEffectiveGroup(uuid);
        return group != null ? group.getNameColor() : "&7";
    }

    /**
     * Get all loaded player data.
     */
    public Collection<PlayerGroupData> getAllPlayerData() {
        return Collections.unmodifiableCollection(playerData.values());
    }

    /**
     * Check if a player is loaded.
     */
    public boolean isLoaded(UUID uuid) {
        return playerData.containsKey(uuid);
    }
}
