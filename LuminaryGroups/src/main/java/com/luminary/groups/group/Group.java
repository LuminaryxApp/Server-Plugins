package com.luminary.groups.group;

import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Represents a permission group with prefix, suffix, and permissions.
 */
public class Group {

    private final String id;
    private String displayName;
    private String prefix;
    private String suffix;
    private String nameColor;
    private String chatColor;
    private int weight; // Higher weight = higher priority
    private final Set<String> permissions;
    private final Set<String> inheritedGroups;
    private boolean isDefault;

    public Group(String id) {
        this.id = id.toLowerCase();
        this.displayName = id;
        this.prefix = "";
        this.suffix = "";
        this.nameColor = "&7";
        this.chatColor = "&f";
        this.weight = 0;
        this.permissions = new LinkedHashSet<>();
        this.inheritedGroups = new LinkedHashSet<>();
        this.isDefault = false;
    }

    public Group(String id, ConfigurationSection section) {
        this.id = id.toLowerCase();
        this.displayName = section.getString("display-name", id);
        this.prefix = section.getString("prefix", "");
        this.suffix = section.getString("suffix", "");
        this.nameColor = section.getString("name-color", "&7");
        this.chatColor = section.getString("chat-color", "&f");
        this.weight = section.getInt("weight", 0);
        this.isDefault = section.getBoolean("default", false);

        this.permissions = new LinkedHashSet<>();
        List<String> permList = section.getStringList("permissions");
        permissions.addAll(permList);

        this.inheritedGroups = new LinkedHashSet<>();
        List<String> inheritList = section.getStringList("inherit");
        for (String inherit : inheritList) {
            inheritedGroups.add(inherit.toLowerCase());
        }
    }

    public void save(ConfigurationSection section) {
        section.set("display-name", displayName);
        section.set("prefix", prefix);
        section.set("suffix", suffix);
        section.set("name-color", nameColor);
        section.set("chat-color", chatColor);
        section.set("weight", weight);
        section.set("default", isDefault);
        section.set("permissions", new ArrayList<>(permissions));
        section.set("inherit", new ArrayList<>(inheritedGroups));
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getNameColor() {
        return nameColor;
    }

    public void setNameColor(String nameColor) {
        this.nameColor = nameColor;
    }

    public String getChatColor() {
        return chatColor;
    }

    public void setChatColor(String chatColor) {
        this.chatColor = chatColor;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Set<String> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public void addPermission(String permission) {
        permissions.add(permission.toLowerCase());
    }

    public void removePermission(String permission) {
        permissions.remove(permission.toLowerCase());
    }

    public boolean hasDirectPermission(String permission) {
        return permissions.contains(permission.toLowerCase()) ||
               permissions.contains("*");
    }

    public Set<String> getInheritedGroups() {
        return Collections.unmodifiableSet(inheritedGroups);
    }

    public void addInheritance(String groupId) {
        inheritedGroups.add(groupId.toLowerCase());
    }

    public void removeInheritance(String groupId) {
        inheritedGroups.remove(groupId.toLowerCase());
    }

    /**
     * Get the formatted prefix for display.
     */
    public String getFormattedPrefix() {
        if (prefix.isEmpty()) {
            return "";
        }
        return prefix + " ";
    }

    /**
     * Get the formatted suffix for display.
     */
    public String getFormattedSuffix() {
        if (suffix.isEmpty()) {
            return "";
        }
        return " " + suffix;
    }
}
