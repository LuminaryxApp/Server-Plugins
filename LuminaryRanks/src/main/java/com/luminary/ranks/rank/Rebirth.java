package com.luminary.ranks.rank;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a rebirth level (highest progression, resets everything).
 */
public class Rebirth {

    private final int level;
    private final String displayName;
    private final String prefix;
    private final long cost;
    private final int requiredPrestige; // Must be at max prestige to rebirth
    private final double permanentMultiplier; // Permanent token bonus
    private final List<String> commands;

    public Rebirth(int level, ConfigurationSection section) {
        this.level = level;
        this.displayName = section.getString("display-name", "&c&lRebirth " + level);
        this.prefix = section.getString("prefix", "&c[R" + level + "]");
        this.cost = section.getLong("cost", 1000000L * level);
        this.requiredPrestige = section.getInt("required-prestige", -1); // -1 means max prestige
        this.permanentMultiplier = section.getDouble("permanent-multiplier", 1.0 + (level * 0.25));
        this.commands = section.getStringList("commands");
    }

    public Rebirth(int level, String displayName, String prefix, long cost, int requiredPrestige, double permanentMultiplier) {
        this.level = level;
        this.displayName = displayName;
        this.prefix = prefix;
        this.cost = cost;
        this.requiredPrestige = requiredPrestige;
        this.permanentMultiplier = permanentMultiplier;
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

    public int getRequiredPrestige() {
        return requiredPrestige;
    }

    public double getPermanentMultiplier() {
        return permanentMultiplier;
    }

    public List<String> getCommands() {
        return commands;
    }
}
