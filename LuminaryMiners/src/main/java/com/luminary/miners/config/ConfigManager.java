package com.luminary.miners.config;

import com.luminary.core.LuminaryCore;
import com.luminary.core.api.LuminaryAPI;
import com.luminary.miners.LuminaryMiners;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.InputStream;

/**
 * Config manager that uses LuminaryCore's centralized config system.
 * All configs are stored in: plugins/LuminaryCore/configs/LuminaryMiners/
 */
public class ConfigManager {

    private static final String PLUGIN_NAME = "LuminaryMiners";

    private final LuminaryMiners plugin;
    private final LuminaryAPI coreAPI;

    private FileConfiguration mainConfig;
    private FileConfiguration minersConfig;
    private FileConfiguration messagesConfig;

    public ConfigManager(LuminaryMiners plugin) {
        this.plugin = plugin;
        this.coreAPI = LuminaryCore.getAPI();
    }

    public void loadAll() {
        // Copy default configs if they don't exist
        copyDefaults();

        // Load all configs from LuminaryCore's centralized folder
        mainConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "config.yml");
        minersConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "miners.yml");
        messagesConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "messages.yml");

        plugin.getLogger().info("Configs loaded from: LuminaryCore/configs/" + PLUGIN_NAME + "/");
    }

    private void copyDefaults() {
        // Copy default configs from jar resources if they don't exist
        if (!coreAPI.configExists(PLUGIN_NAME, "config.yml")) {
            InputStream stream = plugin.getResource("config.yml");
            if (stream != null) {
                coreAPI.copyDefaultConfig(PLUGIN_NAME, "config.yml", stream);
            }
        }

        if (!coreAPI.configExists(PLUGIN_NAME, "miners.yml")) {
            InputStream stream = plugin.getResource("miners.yml");
            if (stream != null) {
                coreAPI.copyDefaultConfig(PLUGIN_NAME, "miners.yml", stream);
            }
        }

        if (!coreAPI.configExists(PLUGIN_NAME, "messages.yml")) {
            InputStream stream = plugin.getResource("messages.yml");
            if (stream != null) {
                coreAPI.copyDefaultConfig(PLUGIN_NAME, "messages.yml", stream);
            }
        }
    }

    public FileConfiguration getConfig() {
        return mainConfig;
    }

    public FileConfiguration getMinersConfig() {
        return minersConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public void saveMinersConfig() {
        coreAPI.savePluginConfig(PLUGIN_NAME, "miners.yml");
    }

    public void saveMessagesConfig() {
        coreAPI.savePluginConfig(PLUGIN_NAME, "messages.yml");
    }

    public void saveMainConfig() {
        coreAPI.savePluginConfig(PLUGIN_NAME, "config.yml");
    }

    // Main config shortcuts
    public int getProductionInterval() {
        return mainConfig.getInt("production.interval-seconds", 60);
    }

    public boolean isOfflineProductionEnabled() {
        return mainConfig.getInt("production.offline-max-hours", 24) > 0;
    }

    public int getMaxOfflineHours() {
        return mainConfig.getInt("production.offline-max-hours", 24);
    }

    public int getMaxMinersPerPlayer() {
        return mainConfig.getInt("limits.max-miners-per-player", 10);
    }

    public int getMaxMinersPerType() {
        return mainConfig.getInt("limits.max-per-type", 3);
    }

    public String getMessage(String path) {
        return messagesConfig.getString("messages." + path, "&cMissing message: " + path);
    }

    public String getMessage(String path, Object... replacements) {
        String message = getMessage(path);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(String.valueOf(replacements[i]), String.valueOf(replacements[i + 1]));
            }
        }
        return message;
    }
}
