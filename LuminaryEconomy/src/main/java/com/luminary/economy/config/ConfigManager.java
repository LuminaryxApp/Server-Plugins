package com.luminary.economy.config;

import com.luminary.core.LuminaryCore;
import com.luminary.core.api.LuminaryAPI;
import com.luminary.economy.LuminaryEconomy;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.InputStream;

/**
 * Config manager that uses LuminaryCore's centralized config system.
 * All configs are stored in: plugins/LuminaryCore/configs/LuminaryEconomy/
 */
public class ConfigManager {

    private static final String PLUGIN_NAME = "LuminaryEconomy";

    private final LuminaryEconomy plugin;
    private final LuminaryAPI coreAPI;

    private FileConfiguration mainConfig;
    private FileConfiguration currenciesConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration scoreboardConfig;

    public ConfigManager(LuminaryEconomy plugin) {
        this.plugin = plugin;
        this.coreAPI = LuminaryCore.getAPI();
    }

    public void loadAll() {
        // Copy default configs if they don't exist
        copyDefaults();

        // Load all configs from LuminaryCore's centralized folder
        mainConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "config.yml");
        currenciesConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "currencies.yml");
        messagesConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "messages.yml");
        scoreboardConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "scoreboard.yml");

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

        if (!coreAPI.configExists(PLUGIN_NAME, "currencies.yml")) {
            InputStream stream = plugin.getResource("currencies.yml");
            if (stream != null) {
                coreAPI.copyDefaultConfig(PLUGIN_NAME, "currencies.yml", stream);
            }
        }

        if (!coreAPI.configExists(PLUGIN_NAME, "messages.yml")) {
            InputStream stream = plugin.getResource("messages.yml");
            if (stream != null) {
                coreAPI.copyDefaultConfig(PLUGIN_NAME, "messages.yml", stream);
            }
        }

        if (!coreAPI.configExists(PLUGIN_NAME, "scoreboard.yml")) {
            InputStream stream = plugin.getResource("scoreboard.yml");
            if (stream != null) {
                coreAPI.copyDefaultConfig(PLUGIN_NAME, "scoreboard.yml", stream);
            }
        }
    }

    public void saveMainConfig() {
        coreAPI.savePluginConfig(PLUGIN_NAME, "config.yml");
    }

    public void saveCurrenciesConfig() {
        coreAPI.savePluginConfig(PLUGIN_NAME, "currencies.yml");
    }

    public void saveMessagesConfig() {
        coreAPI.savePluginConfig(PLUGIN_NAME, "messages.yml");
    }

    public void saveScoreboardConfig() {
        coreAPI.savePluginConfig(PLUGIN_NAME, "scoreboard.yml");
    }

    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public FileConfiguration getCurrenciesConfig() {
        return currenciesConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public FileConfiguration getScoreboardConfig() {
        return scoreboardConfig;
    }

    public String getMessage(String key) {
        return messagesConfig.getString("messages." + key, "&cMissing message: " + key);
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

    // Main config getters
    public String getStorageType() {
        return mainConfig.getString("storage.type", "yaml");
    }

    public boolean isAutoSaveEnabled() {
        return mainConfig.getBoolean("storage.auto-save.enabled", true);
    }

    public int getAutoSaveInterval() {
        return mainConfig.getInt("storage.auto-save.interval", 300);
    }

    public String getDefaultCurrency() {
        return mainConfig.getString("economy.default-currency", "tokens");
    }

    public boolean isPayEnabled() {
        return mainConfig.getBoolean("economy.pay.enabled", true);
    }

    public double getPayTax() {
        return mainConfig.getDouble("economy.pay.tax-percent", 0.0);
    }

    public double getPayMinimum() {
        return mainConfig.getDouble("economy.pay.minimum", 1.0);
    }

    public boolean isScoreboardEnabled() {
        return scoreboardConfig.getBoolean("scoreboard.enabled", true);
    }

    public int getScoreboardUpdateInterval() {
        return scoreboardConfig.getInt("scoreboard.update-interval", 20);
    }
}
