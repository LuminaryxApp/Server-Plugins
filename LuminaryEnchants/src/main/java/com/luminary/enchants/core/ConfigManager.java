package com.luminary.enchants.core;

import com.luminary.core.LuminaryCore;
import com.luminary.core.api.LuminaryAPI;
import com.luminary.enchants.LuminaryEnchants;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.InputStream;

/**
 * Config manager that uses LuminaryCore's centralized config system.
 * All configs are stored in: plugins/LuminaryCore/configs/LuminaryEnchants/
 */
public class ConfigManager {

    private static final String PLUGIN_NAME = "LuminaryEnchants";

    private final LuminaryEnchants plugin;
    private final LuminaryAPI coreAPI;

    private FileConfiguration mainConfig;
    private FileConfiguration enchantsConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration guiConfig;

    public ConfigManager(LuminaryEnchants plugin) {
        this.plugin = plugin;
        this.coreAPI = LuminaryCore.getAPI();
    }

    public void loadAll() {
        copyDefaults();
        mainConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "config.yml");
        enchantsConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "enchants.yml");
        messagesConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "messages.yml");
        guiConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "gui.yml");
        plugin.getLogger().info("Configs loaded from: LuminaryCore/configs/" + PLUGIN_NAME + "/");
    }

    private void copyDefaults() {
        if (!coreAPI.configExists(PLUGIN_NAME, "config.yml")) {
            InputStream stream = plugin.getResource("config.yml");
            if (stream != null) coreAPI.copyDefaultConfig(PLUGIN_NAME, "config.yml", stream);
        }
        if (!coreAPI.configExists(PLUGIN_NAME, "enchants.yml")) {
            InputStream stream = plugin.getResource("enchants.yml");
            if (stream != null) coreAPI.copyDefaultConfig(PLUGIN_NAME, "enchants.yml", stream);
        }
        if (!coreAPI.configExists(PLUGIN_NAME, "messages.yml")) {
            InputStream stream = plugin.getResource("messages.yml");
            if (stream != null) coreAPI.copyDefaultConfig(PLUGIN_NAME, "messages.yml", stream);
        }
        if (!coreAPI.configExists(PLUGIN_NAME, "gui.yml")) {
            InputStream stream = plugin.getResource("gui.yml");
            if (stream != null) coreAPI.copyDefaultConfig(PLUGIN_NAME, "gui.yml", stream);
        }
    }

    public void saveEnchantsConfig() {
        coreAPI.savePluginConfig(PLUGIN_NAME, "enchants.yml");
    }

    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public FileConfiguration getEnchantsConfig() {
        return enchantsConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public FileConfiguration getGuiConfig() {
        return guiConfig;
    }

    // Convenience methods for common config values
    public String getMessage(String key) {
        return messagesConfig.getString("messages." + key, "&cMissing message: " + key);
    }

    public int getClickDebounceMs() {
        return mainConfig.getInt("anti-exploit.click-debounce-ms", 250);
    }

    public int getMaxBlocksPerProc() {
        return mainConfig.getInt("performance.max-blocks-per-proc", 64);
    }

    public int getMaxSearchNodes() {
        return mainConfig.getInt("performance.max-search-nodes", 256);
    }

    public int getCacheTtlMs() {
        return mainConfig.getInt("performance.cache-ttl-ms", 250);
    }

    public boolean isDebugMode() {
        return mainConfig.getBoolean("debug", false);
    }
}
