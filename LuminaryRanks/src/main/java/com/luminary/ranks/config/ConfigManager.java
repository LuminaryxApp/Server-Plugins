package com.luminary.ranks.config;

import com.luminary.core.LuminaryCore;
import com.luminary.core.api.LuminaryAPI;
import com.luminary.ranks.LuminaryRanks;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.InputStream;

/**
 * Config manager that uses LuminaryCore's centralized config system.
 * All configs are stored in: plugins/LuminaryCore/configs/LuminaryRanks/
 */
public class ConfigManager {

    private static final String PLUGIN_NAME = "LuminaryRanks";

    private final LuminaryRanks plugin;
    private final LuminaryAPI coreAPI;

    private FileConfiguration mainConfig;
    private FileConfiguration ranksConfig;
    private FileConfiguration messagesConfig;

    public ConfigManager(LuminaryRanks plugin) {
        this.plugin = plugin;
        this.coreAPI = LuminaryCore.getAPI();
    }

    public void loadAll() {
        copyDefaults();
        mainConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "config.yml");
        ranksConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "ranks.yml");
        messagesConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "messages.yml");
        plugin.getLogger().info("Configs loaded from: LuminaryCore/configs/" + PLUGIN_NAME + "/");
    }

    private void copyDefaults() {
        if (!coreAPI.configExists(PLUGIN_NAME, "config.yml")) {
            InputStream stream = plugin.getResource("config.yml");
            if (stream != null) coreAPI.copyDefaultConfig(PLUGIN_NAME, "config.yml", stream);
        }
        if (!coreAPI.configExists(PLUGIN_NAME, "ranks.yml")) {
            InputStream stream = plugin.getResource("ranks.yml");
            if (stream != null) coreAPI.copyDefaultConfig(PLUGIN_NAME, "ranks.yml", stream);
        }
        if (!coreAPI.configExists(PLUGIN_NAME, "messages.yml")) {
            InputStream stream = plugin.getResource("messages.yml");
            if (stream != null) coreAPI.copyDefaultConfig(PLUGIN_NAME, "messages.yml", stream);
        }
    }

    public void saveRanksConfig() {
        coreAPI.savePluginConfig(PLUGIN_NAME, "ranks.yml");
    }

    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public FileConfiguration getRanksConfig() {
        return ranksConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public String getMessage(String key) {
        String prefix = messagesConfig.getString("prefix", "&6&lRanks &8» &7");
        String message = messagesConfig.getString(key, "&cMissing message: " + key);
        return prefix + message;
    }

    public String getRawMessage(String key) {
        return messagesConfig.getString(key, "&cMissing message: " + key);
    }

    public String getMessage(String key, Object... replacements) {
        String message = getMessage(key);
        for (int i = 0; i < replacements.length; i += 2) {
            if (i + 1 < replacements.length) {
                message = message.replace(String.valueOf(replacements[i]), String.valueOf(replacements[i + 1]));
            }
        }
        return message;
    }

    public String getCurrencyType() {
        return mainConfig.getString("economy.currency", "tokens");
    }

    public boolean isConfirmationEnabled() {
        return mainConfig.getBoolean("settings.confirm-rankup", false);
    }

    public boolean isBroadcastEnabled() {
        return mainConfig.getBoolean("settings.broadcast-rankup", true);
    }

    public boolean isBroadcastPrestigeEnabled() {
        return mainConfig.getBoolean("settings.broadcast-prestige", true);
    }

    public boolean isBroadcastRebirthEnabled() {
        return mainConfig.getBoolean("settings.broadcast-rebirth", true);
    }

    public int getAutoSaveInterval() {
        return mainConfig.getInt("settings.auto-save-interval", 300);
    }
}
