package com.luminary.core.config;

import com.luminary.core.LuminaryCore;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized configuration manager for all Luminary plugins.
 * Stores all plugin configs in LuminaryCore/configs/<PluginName>/
 */
public class ConfigManager {

    private final LuminaryCore plugin;
    private final File pluginConfigsFolder;
    private final Map<String, PluginConfig> pluginConfigs = new HashMap<>();

    // Core config files
    private FileConfiguration config;
    private File configFile;

    private FileConfiguration messagesConfig;
    private File messagesFile;

    private FileConfiguration punishmentsConfig;
    private File punishmentsFile;

    public ConfigManager(LuminaryCore plugin) {
        this.plugin = plugin;
        this.pluginConfigsFolder = new File(plugin.getDataFolder(), "configs");
        if (!pluginConfigsFolder.exists()) {
            pluginConfigsFolder.mkdirs();
        }
    }

    public void loadAll() {
        // Load core configs
        loadConfig();
        loadMessagesConfig();
        loadPunishmentsConfig();
    }

    // ========== CORE CONFIG ==========

    private void loadConfig() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public FileConfiguration getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml: " + e.getMessage());
        }
    }

    // ========== MESSAGES CONFIG ==========

    private void loadMessagesConfig() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
    }

    public FileConfiguration getMessagesConfig() {
        if (messagesConfig == null) {
            loadMessagesConfig();
        }
        return messagesConfig;
    }

    public String getMessage(String path) {
        return getMessagesConfig().getString("messages." + path, "&cMissing message: " + path);
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

    // ========== PUNISHMENTS CONFIG ==========

    private void loadPunishmentsConfig() {
        punishmentsFile = new File(plugin.getDataFolder(), "punishments.yml");
        if (!punishmentsFile.exists()) {
            plugin.saveResource("punishments.yml", false);
        }
        punishmentsConfig = YamlConfiguration.loadConfiguration(punishmentsFile);
    }

    public FileConfiguration getPunishmentsConfig() {
        if (punishmentsConfig == null) {
            loadPunishmentsConfig();
        }
        return punishmentsConfig;
    }

    public void savePunishmentsConfig() {
        try {
            punishmentsConfig.save(punishmentsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save punishments.yml: " + e.getMessage());
        }
    }

    // ========== CENTRALIZED PLUGIN CONFIGS ==========

    /**
     * Get the centralized configs folder for all Luminary plugins.
     */
    public File getPluginConfigsFolder() {
        return pluginConfigsFolder;
    }

    /**
     * Get the config folder for a specific plugin.
     * Creates: LuminaryCore/configs/<pluginName>/
     */
    public File getPluginFolder(String pluginName) {
        File folder = new File(pluginConfigsFolder, pluginName);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        return folder;
    }

    /**
     * Get or create a config file for another plugin.
     * Path: LuminaryCore/configs/<pluginName>/<fileName>
     */
    public FileConfiguration getPluginConfig(String pluginName, String fileName) {
        String key = pluginName + "/" + fileName;

        if (pluginConfigs.containsKey(key)) {
            return pluginConfigs.get(key).getConfig();
        }

        File folder = getPluginFolder(pluginName);
        File file = new File(folder, fileName);

        FileConfiguration config;
        if (file.exists()) {
            config = YamlConfiguration.loadConfiguration(file);
        } else {
            config = new YamlConfiguration();
        }

        pluginConfigs.put(key, new PluginConfig(file, config));
        return config;
    }

    /**
     * Save a plugin's config file.
     */
    public void savePluginConfig(String pluginName, String fileName) {
        String key = pluginName + "/" + fileName;
        PluginConfig pc = pluginConfigs.get(key);
        if (pc != null) {
            try {
                pc.getConfig().save(pc.getFile());
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save " + key + ": " + e.getMessage());
            }
        }
    }

    /**
     * Reload a plugin's config file.
     */
    public void reloadPluginConfig(String pluginName, String fileName) {
        String key = pluginName + "/" + fileName;
        PluginConfig pc = pluginConfigs.get(key);
        if (pc != null) {
            pc.reload();
        }
    }

    /**
     * Check if a plugin config exists.
     */
    public boolean pluginConfigExists(String pluginName, String fileName) {
        File folder = getPluginFolder(pluginName);
        File file = new File(folder, fileName);
        return file.exists();
    }

    /**
     * Copy default config from a plugin's resources if it doesn't exist.
     */
    public void copyDefaultPluginConfig(String pluginName, String fileName, InputStream defaultContent) {
        File folder = getPluginFolder(pluginName);
        File file = new File(folder, fileName);

        if (!file.exists() && defaultContent != null) {
            try {
                Files.copy(defaultContent, file.toPath());
                plugin.getLogger().info("Created default config: " + pluginName + "/" + fileName);
            } catch (IOException e) {
                plugin.getLogger().warning("Could not copy default config " + pluginName + "/" + fileName + ": " + e.getMessage());
            }
        }
    }

    /**
     * Inner class to hold plugin config data.
     */
    private static class PluginConfig {
        private final File file;
        private FileConfiguration config;

        public PluginConfig(File file, FileConfiguration config) {
            this.file = file;
            this.config = config;
        }

        public File getFile() {
            return file;
        }

        public FileConfiguration getConfig() {
            return config;
        }

        public void reload() {
            config = YamlConfiguration.loadConfiguration(file);
        }
    }
}
