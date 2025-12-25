package com.luminary.core.staff;

import com.luminary.core.LuminaryCore;
import com.luminary.core.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages staff features like vanish and staff chat.
 */
public class StaffManager {

    private final LuminaryCore plugin;

    // Vanished players
    private final Set<UUID> vanishedPlayers = ConcurrentHashMap.newKeySet();

    // Players with staff chat enabled
    private final Set<UUID> staffChatEnabled = ConcurrentHashMap.newKeySet();

    public StaffManager(LuminaryCore plugin) {
        this.plugin = plugin;
    }

    // ========== VANISH ==========

    public void setVanished(Player player, boolean vanished) {
        if (vanished) {
            vanishedPlayers.add(player.getUniqueId());
            // Hide from all players without permission
            for (Player other : Bukkit.getOnlinePlayers()) {
                if (!other.hasPermission("luminarycore.vanish.see")) {
                    other.hidePlayer(plugin, player);
                }
            }
            MessageUtil.send(player, plugin.getConfigManager().getMessage("vanish-enabled"));
        } else {
            vanishedPlayers.remove(player.getUniqueId());
            // Show to all players
            for (Player other : Bukkit.getOnlinePlayers()) {
                other.showPlayer(plugin, player);
            }
            MessageUtil.send(player, plugin.getConfigManager().getMessage("vanish-disabled"));
        }
    }

    public void toggleVanish(Player player) {
        setVanished(player, !isVanished(player));
    }

    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }

    public boolean isVanished(UUID playerId) {
        return vanishedPlayers.contains(playerId);
    }

    public Set<UUID> getVanishedPlayers() {
        return vanishedPlayers;
    }

    /**
     * Handle a player joining - apply vanish visibility rules.
     */
    public void handleJoin(Player player) {
        // If this player can see vanished players
        if (player.hasPermission("luminarycore.vanish.see")) {
            // They can see everyone
            return;
        }

        // Hide all vanished players from this player
        for (UUID vanishedId : vanishedPlayers) {
            Player vanished = Bukkit.getPlayer(vanishedId);
            if (vanished != null) {
                player.hidePlayer(plugin, vanished);
            }
        }
    }

    // ========== STAFF CHAT ==========

    public void setStaffChatEnabled(UUID playerId, boolean enabled) {
        if (enabled) {
            staffChatEnabled.add(playerId);
        } else {
            staffChatEnabled.remove(playerId);
        }
    }

    public void toggleStaffChat(Player player) {
        boolean newState = !hasStaffChatEnabled(player.getUniqueId());
        setStaffChatEnabled(player.getUniqueId(), newState);

        if (newState) {
            MessageUtil.send(player, plugin.getConfigManager().getMessage("staffchat-enabled"));
        } else {
            MessageUtil.send(player, plugin.getConfigManager().getMessage("staffchat-disabled"));
        }
    }

    public boolean hasStaffChatEnabled(UUID playerId) {
        return staffChatEnabled.contains(playerId);
    }

    /**
     * Send a message to staff chat.
     */
    public void sendStaffMessage(Player sender, String message) {
        String format = plugin.getConfigManager().getMessage("staffchat-format",
            "{player}", sender.getName(),
            "{message}", message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("luminarycore.staffchat")) {
                MessageUtil.send(player, format);
            }
        }

        // Log to console
        plugin.getLogger().info("[StaffChat] " + sender.getName() + ": " + message);
    }

    /**
     * Send a staff chat message from console.
     */
    public void sendStaffMessage(String sender, String message) {
        String format = plugin.getConfigManager().getMessage("staffchat-format",
            "{player}", sender,
            "{message}", message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("luminarycore.staffchat")) {
                MessageUtil.send(player, format);
            }
        }

        plugin.getLogger().info("[StaffChat] " + sender + ": " + message);
    }

    // ========== STAFF ALERTS ==========

    /**
     * Send an alert to all staff members.
     */
    public void sendStaffAlert(String message) {
        String format = plugin.getConfigManager().getMessage("staff-alert",
            "{message}", message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.hasPermission("luminarycore.staff")) {
                MessageUtil.send(player, format);
            }
        }
    }

    /**
     * Broadcast a message to all online players.
     */
    public void broadcast(String message) {
        String format = plugin.getConfigManager().getMessage("broadcast-format",
            "{message}", message);

        for (Player player : Bukkit.getOnlinePlayers()) {
            MessageUtil.send(player, format);
        }

        plugin.getLogger().info("[Broadcast] " + MessageUtil.stripColor(message));
    }
}
