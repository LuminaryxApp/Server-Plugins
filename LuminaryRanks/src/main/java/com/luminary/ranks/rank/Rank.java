package com.luminary.ranks.rank;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single rank (A-Z).
 */
public class Rank {

    private final String id;
    private final String displayName;
    private final String prefix;
    private final long cost;
    private final int order;
    private final List<String> commands;
    private final List<String> permissions;

    public Rank(String id, ConfigurationSection section, int order) {
        this.id = id;
        this.order = order;
        this.displayName = section.getString("display-name", "&7" + id);
        this.prefix = section.getString("prefix", "&7[" + id + "]");
        this.cost = section.getLong("cost", 1000L * order);
        this.commands = section.getStringList("commands");
        this.permissions = section.getStringList("permissions");
    }

    public Rank(String id, String displayName, String prefix, long cost, int order) {
        this.id = id;
        this.displayName = displayName;
        this.prefix = prefix;
        this.cost = cost;
        this.order = order;
        this.commands = new ArrayList<>();
        this.permissions = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getPrefix() {
        return prefix;
    }

    public long getCost() {
        return cost;
    }

    public int getOrder() {
        return order;
    }

    public List<String> getCommands() {
        return commands;
    }

    public List<String> getPermissions() {
        return permissions;
    }
}
