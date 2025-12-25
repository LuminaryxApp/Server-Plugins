package com.luminary.groups.permission;

import com.luminary.groups.LuminaryGroups;
import com.luminary.groups.group.Group;
import com.luminary.groups.player.PlayerGroupData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages permission attachments for players.
 */
public class PermissionManager {

    private final LuminaryGroups plugin;
    private final Map<UUID, PermissionAttachment> attachments = new ConcurrentHashMap<>();

    public PermissionManager(LuminaryGroups plugin) {
        this.plugin = plugin;
    }

    /**
     * Set up permissions for a player when they join.
     */
    public void setupPlayer(Player player) {
        UUID uuid = player.getUniqueId();

        // Remove old attachment if exists
        removePermissions(uuid);

        // Create new attachment
        PermissionAttachment attachment = player.addAttachment(plugin);
        attachments.put(uuid, attachment);

        // Apply permissions
        applyPermissions(player, attachment);
    }

    /**
     * Apply all permissions from player's groups.
     */
    private void applyPermissions(Player player, PermissionAttachment attachment) {
        PlayerGroupData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) {
            return;
        }

        Set<String> allPermissions = new LinkedHashSet<>();

        // Collect permissions from all groups
        for (String groupId : data.getGroups()) {
            Group group = plugin.getGroupManager().getGroup(groupId);
            if (group != null) {
                Set<String> groupPerms = plugin.getGroupManager().getAllPermissions(group);
                allPermissions.addAll(groupPerms);
            }
        }

        // Apply permissions
        for (String permission : allPermissions) {
            boolean value = true;
            String perm = permission;

            // Handle negated permissions
            if (permission.startsWith("-")) {
                perm = permission.substring(1);
                value = false;
            }

            attachment.setPermission(perm, value);
        }
    }

    /**
     * Refresh permissions for a specific player.
     */
    public void refreshPlayer(Player player) {
        setupPlayer(player);
    }

    /**
     * Refresh permissions for all online players.
     */
    public void refreshAllPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            refreshPlayer(player);
        }
    }

    /**
     * Remove permissions for a player.
     */
    public void removePermissions(UUID uuid) {
        PermissionAttachment attachment = attachments.remove(uuid);
        if (attachment != null) {
            try {
                attachment.remove();
            } catch (Exception e) {
                // Player might have logged off
            }
        }
    }

    /**
     * Remove all permission attachments.
     */
    public void removeAllPermissions() {
        for (UUID uuid : new HashSet<>(attachments.keySet())) {
            removePermissions(uuid);
        }
        attachments.clear();
    }

    /**
     * Check if a player has a specific permission through groups.
     */
    public boolean hasPermission(Player player, String permission) {
        PlayerGroupData data = plugin.getPlayerDataManager().getPlayerData(player);
        if (data == null) {
            return false;
        }

        for (String groupId : data.getGroups()) {
            Group group = plugin.getGroupManager().getGroup(groupId);
            if (group != null) {
                Set<String> groupPerms = plugin.getGroupManager().getAllPermissions(group);

                // Check for wildcard
                if (groupPerms.contains("*")) {
                    return true;
                }

                // Check for exact permission
                if (groupPerms.contains(permission.toLowerCase())) {
                    return true;
                }

                // Check for negated permission
                if (groupPerms.contains("-" + permission.toLowerCase())) {
                    return false;
                }
            }
        }

        return false;
    }
}
