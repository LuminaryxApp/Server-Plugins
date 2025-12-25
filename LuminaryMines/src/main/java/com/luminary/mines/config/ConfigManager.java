package com.luminary.mines.config;

import com.luminary.core.LuminaryCore;
import com.luminary.core.api.LuminaryAPI;
import com.luminary.mines.LuminaryMines;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Config manager that uses LuminaryCore's centralized config system.
 * All configs are stored in: plugins/LuminaryCore/configs/LuminaryMines/
 */
public class ConfigManager {

    private static final String PLUGIN_NAME = "LuminaryMines";

    private final LuminaryMines plugin;
    private final LuminaryAPI coreAPI;

    private FileConfiguration mainConfig;
    private FileConfiguration messagesConfig;
    private FileConfiguration minesDataConfig;

    public ConfigManager(LuminaryMines plugin) {
        this.plugin = plugin;
        this.coreAPI = LuminaryCore.getAPI();
    }

    public void loadAll() {
        copyDefaults();
        mainConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "config.yml");
        messagesConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "messages.yml");
        minesDataConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "mines.yml");
        ensureSchematicsFolder();
        plugin.getLogger().info("Configs loaded from: LuminaryCore/configs/" + PLUGIN_NAME + "/");
    }

    private void copyDefaults() {
        if (!coreAPI.configExists(PLUGIN_NAME, "config.yml")) {
            InputStream stream = plugin.getResource("config.yml");
            if (stream != null) coreAPI.copyDefaultConfig(PLUGIN_NAME, "config.yml", stream);
        }
        if (!coreAPI.configExists(PLUGIN_NAME, "messages.yml")) {
            InputStream stream = plugin.getResource("messages.yml");
            if (stream != null) coreAPI.copyDefaultConfig(PLUGIN_NAME, "messages.yml", stream);
        }
        // mines.yml is created empty if it doesn't exist
        if (!coreAPI.configExists(PLUGIN_NAME, "mines.yml")) {
            coreAPI.getPluginConfig(PLUGIN_NAME, "mines.yml");
        }
    }

    private void ensureSchematicsFolder() {
        File schematicsFolder = new File(plugin.getDataFolder(), "schematics");
        if (!schematicsFolder.exists()) {
            schematicsFolder.mkdirs();
            plugin.getLogger().info("Created schematics folder. Add .schem files to define mine layouts!");
        }
    }

    public void saveMinesData() {
        coreAPI.savePluginConfig(PLUGIN_NAME, "mines.yml");
    }

    public FileConfiguration getMainConfig() {
        return mainConfig;
    }

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    public FileConfiguration getMinesData() {
        return minesDataConfig;
    }

    public String getMessage(String key) {
        String message = messagesConfig.getString(key, "&cMissing message: " + key);
        String prefix = messagesConfig.getString("prefix", "&6&lMines &8» &7");
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

    // World settings
    public String getMineWorld() {
        return mainConfig.getString("world.name", "mines");
    }

    public boolean isAutoCreateWorld() {
        return mainConfig.getBoolean("world.auto-create", true);
    }

    public String getWorldType() {
        return mainConfig.getString("world.type", "VOID");
    }

    // Placement settings
    public int getStartX() {
        return mainConfig.getInt("placement.start-x", 0);
    }

    public int getStartZ() {
        return mainConfig.getInt("placement.start-z", 0);
    }

    public int getMineSpacing() {
        return mainConfig.getInt("placement.spacing", 200);
    }

    public int getYLevel() {
        return mainConfig.getInt("placement.y-level", 64);
    }

    public int getMinesPerRow() {
        return mainConfig.getInt("placement.mines-per-row", 50);
    }

    // Reset settings
    public int getDefaultResetInterval() {
        return mainConfig.getInt("reset.default-interval", 300);
    }

    public int getMinResetInterval() {
        return mainConfig.getInt("reset.min-interval", 60);
    }

    public int getMaxResetInterval() {
        return mainConfig.getInt("reset.max-interval", 3600);
    }

    public double getAutoResetThreshold() {
        return mainConfig.getDouble("reset.auto-reset-threshold", 95.0);
    }

    public int getResetCheckInterval() {
        return mainConfig.getInt("reset.reset-check-interval", 20);
    }

    // Whitelist settings
    public int getMaxWhitelistSize() {
        return mainConfig.getInt("whitelist.max-size", 10);
    }

    public int getWhitelistSizeForPermission(String permission) {
        return mainConfig.getInt("whitelist.permission-sizes." + permission, getMaxWhitelistSize());
    }

    // Schematic settings
    public String getSchematicsFolder() {
        return mainConfig.getString("schematics.folder", "schematics");
    }

    public String getDefaultSchematic() {
        return mainConfig.getString("schematics.default", "default");
    }

    public boolean isSchematicChoiceAllowed() {
        return mainConfig.getBoolean("schematics.allow-choice", true);
    }

    public File getSchematicsFolderFile() {
        return new File(plugin.getDataFolder(), getSchematicsFolder());
    }

    // Protection settings
    public boolean isEntryRestricted() {
        return mainConfig.getBoolean("protection.restrict-entry", true);
    }

    public boolean isBuildingPrevented() {
        return mainConfig.getBoolean("protection.prevent-building", true);
    }

    public boolean isExteriorBreakingAllowed() {
        return mainConfig.getBoolean("protection.allow-exterior-breaking", false);
    }

    // Storage settings
    public int getAutoSaveInterval() {
        return mainConfig.getInt("storage.auto-save-interval", 300);
    }

    public String getDataFolder() {
        return mainConfig.getString("storage.data-folder", "data");
    }

    // Default blocks for tier
    public Map<String, Integer> getBlocksForTier(int tier) {
        String path = "tiers." + tier + ".blocks";
        if (mainConfig.isConfigurationSection(path)) {
            Map<String, Integer> blocks = new HashMap<>();
            for (String key : mainConfig.getConfigurationSection(path).getKeys(false)) {
                blocks.put(key.toUpperCase(), mainConfig.getInt(path + "." + key));
            }
            return blocks;
        }
        // Fall back to default blocks
        return getDefaultBlocks();
    }

    public Map<String, Integer> getDefaultBlocks() {
        Map<String, Integer> blocks = new HashMap<>();
        if (mainConfig.isConfigurationSection("default-blocks")) {
            for (String key : mainConfig.getConfigurationSection("default-blocks").getKeys(false)) {
                blocks.put(key.toUpperCase(), mainConfig.getInt("default-blocks." + key));
            }
        }
        if (blocks.isEmpty()) {
            blocks.put("STONE", 50);
            blocks.put("COBBLESTONE", 25);
            blocks.put("COAL_ORE", 15);
            blocks.put("IRON_ORE", 8);
            blocks.put("GOLD_ORE", 2);
        }
        return blocks;
    }

    public String getTierName(int tier) {
        return mainConfig.getString("tiers." + tier + ".name", "Tier " + tier);
    }
}
