package com.luminary.ranks.rank;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a prestige level.
 */
public class Prestige {

    private final int level;
    private final String displayName;
    private final String prefix;
    private final long cost;
    private final double multiplier; // Token multiplier bonus
    private final List<String> commands;

    public Prestige(int level, ConfigurationSection section) {
        this.level = level;
        this.displayName = section.getString("display-name", "&5Prestige " + level);
        this.prefix = section.getString("prefix", "&5[P" + level + "]");
        this.cost = section.getLong("cost", 100000L * level);
        this.multiplier = section.getDouble("multiplier", 1.0 + (level * 0.1));
        this.commands = section.getStringList("commands");
    }

    public Prestige(int level, String displayName, String prefix, long cost, double multiplier) {
        this.level = level;
        this.displayName = displayName;
        this.prefix = prefix;
        this.cost = cost;
        this.multiplier = multiplier;
        this.commands = new ArrayList<>();
    }

    public int getLevel() {
        return level;
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

    public double getMultiplier() {
        return multiplier;
    }

    public List<String> getCommands() {
        return commands;
    }
}
