package com.luminary.crates.config;

import com.luminary.core.LuminaryCore;
import com.luminary.core.api.LuminaryAPI;
import com.luminary.crates.LuminaryCrates;
import com.luminary.crates.crate.CrateTier;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.InputStream;

/**
 * Config manager that uses LuminaryCore's centralized config system.
 * All configs are stored in: plugins/LuminaryCore/configs/LuminaryCrates/
 */
public class ConfigManager {

    private static final String PLUGIN_NAME = "LuminaryCrates";

    private final LuminaryCrates plugin;
    private final LuminaryAPI coreAPI;

    private FileConfiguration mainConfig;
    private FileConfiguration cratesConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration locationsConfig;

    public ConfigManager(LuminaryCrates plugin) {
        this.plugin = plugin;
        this.coreAPI = LuminaryCore.getAPI();
    }

    public void loadAll() {
        copyDefaults();
        mainConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "config.yml");
        cratesConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "crates.yml");
        messagesConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "messages.yml");
        locationsConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "locations.yml");
        plugin.getLogger().info("Configs loaded from: LuminaryCore/configs/" + PLUGIN_NAME + "/");
    }

    private void copyDefaults() {
        if (!coreAPI.configExists(PLUGIN_NAME, "config.yml")) {
            InputStream stream = plugin.getResource("config.yml");
            if (stream != null) coreAPI.copyDefaultConfig(PLUGIN_NAME, "config.yml", stream);
        }
        if (!coreAPI.configExists(PLUGIN_NAME, "crates.yml")) {
            InputStream stream = plugin.getResource("crates.yml");
            if (stream != null) coreAPI.copyDefaultConfig(PLUGIN_NAME, "crates.yml", stream);
        }
        if (!coreAPI.configExists(PLUGIN_NAME, "messages.yml")) {
            InputStream stream = plugin.getResource("messages.yml");
            if (stream != null) coreAPI.copyDefaultConfig(PLUGIN_NAME, "messages.yml", stream);
        }
    }

    public void saveCratesConfig() {
        coreAPI.savePluginConfig(PLUGIN_NAME, "crates.yml");
    }

    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public FileConfiguration getCratesConfig() {
        return cratesConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public String getMessage(String key) {
        return messagesConfig.getString("messages." + key, "&cMissing message: " + key);
    }

    public int getAnimationDuration() {
        return mainConfig.getInt("animation.duration-ticks", 100);
    }

    public int getAnimationSpeed() {
        return mainConfig.getInt("animation.speed", 2);
    }

    public boolean isPreviewEnabled() {
        return mainConfig.getBoolean("crates.preview-enabled", true);
    }

    public CrateTier getBroadcastMinRarity() {
        String rarity = mainConfig.getString("broadcast.min-rarity", "LEGENDARY");
        return CrateTier.fromString(rarity);
    }

    public boolean isBroadcastEnabled() {
        return mainConfig.getBoolean("broadcast.enabled", true);
    }

    public FileConfiguration getLocationsConfig() {
        return locationsConfig;
    }

    public void saveLocationsConfig() {
        coreAPI.savePluginConfig(PLUGIN_NAME, "locations.yml");
    }
}
