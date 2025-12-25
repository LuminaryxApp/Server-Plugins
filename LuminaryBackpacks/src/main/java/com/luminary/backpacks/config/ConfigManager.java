package com.luminary.backpacks.config;

import com.luminary.backpacks.LuminaryBackpacks;
import com.luminary.core.LuminaryCore;
import com.luminary.core.api.LuminaryAPI;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private static final String PLUGIN_NAME = "LuminaryBackpacks";

    private final LuminaryBackpacks plugin;
    private final LuminaryAPI coreAPI;

    private FileConfiguration mainConfig;
    private FileConfiguration messagesConfig;

    private final Map<Material, Double> sellPrices = new HashMap<>();

    public ConfigManager(LuminaryBackpacks plugin) {
        this.plugin = plugin;
        this.coreAPI = LuminaryCore.getAPI();
    }

    public void loadAll() {
        copyDefaults();
        mainConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "config.yml");
        messagesConfig = coreAPI.getPluginConfig(PLUGIN_NAME, "messages.yml");
        loadSellPrices();
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
    }

    private void loadSellPrices() {
        sellPrices.clear();
        ConfigurationSection section = mainConfig.getConfigurationSection("sell-prices");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                Material material = Material.matchMaterial(key);
                if (material != null) {
                    sellPrices.put(material, section.getDouble(key));
                }
            }
        }
    }

    public String getMessage(String key) {
        String prefix = messagesConfig.getString("prefix", "&6&lBackpack &8» &7");
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

    // Tier settings
    public int getDefaultTier() {
        return mainConfig.getInt("default-tier", 1);
    }

    public int getMaxTier() {
        ConfigurationSection section = mainConfig.getConfigurationSection("tiers");
        return section != null ? section.getKeys(false).size() : 6;
    }

    public String getTierName(int tier) {
        return mainConfig.getString("tiers." + tier + ".name", "Backpack Tier " + tier);
    }

    public int getTierSize(int tier) {
        return mainConfig.getInt("tiers." + tier + ".size", Math.min(tier * 9, 54));
    }

    public String getTierColor(int tier) {
        return mainConfig.getString("tiers." + tier + ".color", "&7");
    }

    public long getUpgradeCost(int tier) {
        return mainConfig.getLong("tiers." + tier + ".upgrade-cost", 0);
    }

    public String getTierPermission(int tier) {
        return mainConfig.getString("tiers." + tier + ".permission", "");
    }

    // Auto-pickup settings
    public boolean isAutoPickupEnabled() {
        return mainConfig.getBoolean("auto-pickup.enabled", true);
    }

    public boolean isSellableOnly() {
        return mainConfig.getBoolean("auto-pickup.sellable-only", false);
    }

    // Auto-sell settings
    public boolean isAutoSellEnabled() {
        return mainConfig.getBoolean("auto-sell.enabled", true);
    }

    public int getAutoSellInterval() {
        return mainConfig.getInt("auto-sell.interval", 100);
    }

    public String getAutoSellSound() {
        return mainConfig.getString("auto-sell.sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
    }

    // Sell prices
    public double getSellPrice(Material material) {
        return sellPrices.getOrDefault(material, 0.0);
    }

    public boolean isSellable(Material material) {
        return sellPrices.containsKey(material) && sellPrices.get(material) > 0;
    }

    public Map<Material, Double> getSellPrices() {
        return new HashMap<>(sellPrices);
    }
}
