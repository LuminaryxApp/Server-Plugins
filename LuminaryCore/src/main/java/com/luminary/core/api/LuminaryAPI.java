package com.luminary.core.api;

import com.luminary.core.LuminaryCore;
import com.luminary.core.config.ConfigManager;
import com.luminary.core.moderation.ModerationManager;
import com.luminary.core.moderation.Punishment;
import com.luminary.core.player.PlayerData;
import com.luminary.core.player.PlayerDataManager;
import com.luminary.core.staff.StaffManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

/**
 * Public API for other Luminary plugins to interact with LuminaryCore.
 * Access via LuminaryCore.getAPI()
 */
public class LuminaryAPI {

    private final LuminaryCore plugin;

    public LuminaryAPI(LuminaryCore plugin) {
        this.plugin = plugin;
    }

    // ========== CONFIG API ==========

    /**
     * Get the centralized config folder for a plugin.
     * Creates: LuminaryCore/configs/<pluginName>/
     */
    public File getPluginConfigFolder(String pluginName) {
        return plugin.getConfigManager().getPluginFolder(pluginName);
    }

    /**
     * Get or create a config file for a plugin.
     * Path: LuminaryCore/configs/<pluginName>/<fileName>
     */
    public FileConfiguration getPluginConfig(String pluginName, String fileName) {
        return plugin.getConfigManager().getPluginConfig(pluginName, fileName);
    }

    /**
     * Save a plugin's config file.
     */
    public void savePluginConfig(String pluginName, String fileName) {
        plugin.getConfigManager().savePluginConfig(pluginName, fileName);
    }

    /**
     * Reload a plugin's config file.
     */
    public void reloadPluginConfig(String pluginName, String fileName) {
        plugin.getConfigManager().reloadPluginConfig(pluginName, fileName);
    }

    /**
     * Copy a default config from resources if it doesn't exist.
     */
    public void copyDefaultConfig(String pluginName, String fileName, InputStream defaultContent) {
        plugin.getConfigManager().copyDefaultPluginConfig(pluginName, fileName, defaultContent);
    }

    /**
     * Check if a plugin config exists.
     */
    public boolean configExists(String pluginName, String fileName) {
        return plugin.getConfigManager().pluginConfigExists(pluginName, fileName);
    }

    // ========== PLAYER DATA API ==========

    /**
     * Get player data for a UUID.
     */
    public PlayerData getPlayerData(UUID playerId) {
        return plugin.getPlayerDataManager().getPlayerData(playerId);
    }

    /**
     * Get player data for an online player.
     */
    public PlayerData getPlayerData(Player player) {
        return plugin.getPlayerDataManager().getPlayerData(player);
    }

    /**
     * Check if a player has been on before.
     */
    public boolean hasPlayedBefore(UUID playerId) {
        return plugin.getPlayerDataManager().hasDataFile(playerId);
    }

    // ========== MODERATION API ==========

    /**
     * Check if a player is banned.
     */
    public boolean isBanned(UUID playerId) {
        return plugin.getModerationManager().isBanned(playerId);
    }

    /**
     * Get a player's active ban.
     */
    public Punishment getBan(UUID playerId) {
        return plugin.getModerationManager().getBan(playerId);
    }

    /**
     * Check if a player is muted.
     */
    public boolean isMuted(UUID playerId) {
        return plugin.getModerationManager().isMuted(playerId);
    }

    /**
     * Get a player's active mute.
     */
    public Punishment getMute(UUID playerId) {
        return plugin.getModerationManager().getMute(playerId);
    }

    /**
     * Check if a player is frozen.
     */
    public boolean isFrozen(UUID playerId) {
        return plugin.getModerationManager().isFrozen(playerId);
    }

    /**
     * Get warning count for a player.
     */
    public int getWarningCount(UUID playerId) {
        return plugin.getModerationManager().getActiveWarningCount(playerId);
    }

    /**
     * Get punishment history for a player.
     */
    public List<Punishment> getHistory(UUID playerId) {
        return plugin.getModerationManager().getHistory(playerId);
    }

    // ========== STAFF API ==========

    /**
     * Check if a player is vanished.
     */
    public boolean isVanished(UUID playerId) {
        return plugin.getStaffManager().isVanished(playerId);
    }

    /**
     * Check if a player is vanished.
     */
    public boolean isVanished(Player player) {
        return plugin.getStaffManager().isVanished(player);
    }

    /**
     * Send a message to staff chat.
     */
    public void sendStaffMessage(String sender, String message) {
        plugin.getStaffManager().sendStaffMessage(sender, message);
    }

    /**
     * Send an alert to staff.
     */
    public void sendStaffAlert(String message) {
        plugin.getStaffManager().sendStaffAlert(message);
    }

    /**
     * Broadcast a message to all players.
     */
    public void broadcast(String message) {
        plugin.getStaffManager().broadcast(message);
    }

    // ========== MAINTENANCE API ==========

    /**
     * Check if maintenance mode is enabled.
     */
    public boolean isMaintenanceMode() {
        return plugin.isMaintenanceMode();
    }

    /**
     * Set maintenance mode.
     */
    public void setMaintenanceMode(boolean enabled) {
        plugin.setMaintenanceMode(enabled);
    }

    // ========== UTILITY ==========

    /**
     * Get the plugin instance.
     */
    public LuminaryCore getPlugin() {
        return plugin;
    }
}
