package com.luminary.groups.config;

import com.luminary.core.LuminaryCore;
import com.luminary.core.api.LuminaryAPI;
import com.luminary.groups.LuminaryGroups;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.InputStream;

/**
 * Config manager that uses LuminaryCore's centralized config system.
 * All configs are stored in: plugins/LuminaryCore/configs/LuminaryGroups/
 */
public class ConfigManager {

    private static final String PLUGIN_NAME = "LuminaryGroups";

    private final LuminaryGroups plugin;
    private final LuminaryAPI coreAPI;

    private FileConfiguration mainConfig;
    private FileConfiguration groupsConfig;
    private FileConfiguration messagesConfig;

    public ConfigManager(LuminaryGroups plugin) {
        this.plugin = plugin;
        this.coreAPI = LuminaryCore.getAPI();
    }

    public void loadAll() {
        copyDefaults();
        mainConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "config.yml");
        groupsConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "groups.yml");
        messagesConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "messages.yml");
        plugin.getLogger().info("Configs loaded from: LuminaryCore/configs/" + PLUGIN_NAME + "/");
    }

    private void copyDefaults() {
        if (!coreAPI.configExists(PLUGIN_NAME, "config.yml")) {
            InputStream stream = plugin.getResource("config.yml");
            if (stream != null) coreAPI.copyDefaultConfig(PLUGIN_NAME, "config.yml", stream);
        }
        if (!coreAPI.configExists(PLUGIN_NAME, "groups.yml")) {
            InputStream stream = plugin.getResource("groups.yml");
            if (stream != null) coreAPI.copyDefaultConfig(PLUGIN_NAME, "groups.yml", stream);
        }
        if (!coreAPI.configExists(PLUGIN_NAME, "messages.yml")) {
            InputStream stream = plugin.getResource("messages.yml");
            if (stream != null) coreAPI.copyDefaultConfig(PLUGIN_NAME, "messages.yml", stream);
        }
    }

    public void saveGroupsConfig() {
        coreAPI.savePluginConfig(PLUGIN_NAME, "groups.yml");
    }

    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public FileConfiguration getGroupsConfig() {
        return groupsConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public String getMessage(String key) {
        String prefix = messagesConfig.getString("prefix", "&6&lGroups &8» &7");
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

    public String getChatFormat() {
        return mainConfig.getString("chat.format", "{rank}{prefix}{name}&7: &f{message}");
    }

    public boolean isChatFormattingEnabled() {
        return mainConfig.getBoolean("chat.enabled", true);
    }

    public String getDefaultGroup() {
        return mainConfig.getString("default-group", "default");
    }

    public boolean isTabListEnabled() {
        return mainConfig.getBoolean("tablist.enabled", true);
    }

    public String getTabListFormat() {
        return mainConfig.getString("tablist.format", "{rank}{prefix}{name}");
    }

    public boolean isAutoAssignDefaultGroup() {
        return mainConfig.getBoolean("auto-assign-default", true);
    }
}
