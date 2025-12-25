package com.luminary.groups.player;

import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Stores a player's group memberships and custom settings.
 */
public class PlayerGroupData {

    private final UUID playerId;
    private String playerName;
    private final Set<String> groups;
    private String primaryGroup;
    private String customPrefix; // Override prefix
    private String customSuffix; // Override suffix
    private String customNameColor;
    private boolean dirty = false;

    public PlayerGroupData(UUID playerId, String playerName) {
        this.playerId = playerId;
        this.playerName = playerName;
        this.groups = new LinkedHashSet<>();
        this.primaryGroup = null;
        this.customPrefix = null;
        this.customSuffix = null;
        this.customNameColor = null;
    }

    public PlayerGroupData(UUID playerId, ConfigurationSection section) {
        this.playerId = playerId;
        this.playerName = section.getString("name", "Unknown");
        this.primaryGroup = section.getString("primary-group", null);
        this.customPrefix = section.getString("custom-prefix", null);
        this.customSuffix = section.getString("custom-suffix", null);
        this.customNameColor = section.getString("custom-name-color", null);

        this.groups = new LinkedHashSet<>();
        List<String> groupList = section.getStringList("groups");
        for (String group : groupList) {
            groups.add(group.toLowerCase());
        }
    }

    public void save(ConfigurationSection section) {
        section.set("name", playerName);
        section.set("groups", new ArrayList<>(groups));
        section.set("primary-group", primaryGroup);
        section.set("custom-prefix", customPrefix);
        section.set("custom-suffix", customSuffix);
        section.set("custom-name-color", customNameColor);
        dirty = false;
    }

    // Group management
    public boolean addGroup(String groupId) {
        if (groups.add(groupId.toLowerCase())) {
            dirty = true;
            return true;
        }
        return false;
    }

    public boolean removeGroup(String groupId) {
        if (groups.remove(groupId.toLowerCase())) {
            // Clear primary if it was the removed group
            if (groupId.equalsIgnoreCase(primaryGroup)) {
                primaryGroup = null;
            }
            dirty = true;
            return true;
        }
        return false;
    }

    public boolean hasGroup(String groupId) {
        return groups.contains(groupId.toLowerCase());
    }

    public Set<String> getGroups() {
        return Collections.unmodifiableSet(groups);
    }

    // Getters and setters
    public UUID getPlayerId() {
        return playerId;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
        this.dirty = true;
    }

    public String getPrimaryGroup() {
        return primaryGroup;
    }

    public void setPrimaryGroup(String primaryGroup) {
        this.primaryGroup = primaryGroup != null ? primaryGroup.toLowerCase() : null;
        this.dirty = true;
    }

    public String getCustomPrefix() {
        return customPrefix;
    }

    public void setCustomPrefix(String customPrefix) {
        this.customPrefix = customPrefix;
        this.dirty = true;
    }

    public String getCustomSuffix() {
        return customSuffix;
    }

    public void setCustomSuffix(String customSuffix) {
        this.customSuffix = customSuffix;
        this.dirty = true;
    }

    public String getCustomNameColor() {
        return customNameColor;
    }

    public void setCustomNameColor(String customNameColor) {
        this.customNameColor = customNameColor;
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public void clearCustomizations() {
        this.customPrefix = null;
        this.customSuffix = null;
        this.customNameColor = null;
        this.dirty = true;
    }
}
