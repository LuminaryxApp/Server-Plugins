package com.luminary.miners.miner;

/**
 * Types of resources miners can generate.
 */
public enum ResourceType {
    TOKENS("Tokens", "&6", "token", "tokens"),
    BEACONS("Beacons", "&b", "beacon", "beacons"),
    GEMS("Gems", "&d", "gem", "gems");

    private final String displayName;
    private final String color;
    private final String singular;
    private final String plural;

    ResourceType(String displayName, String color, String singular, String plural) {
        this.displayName = displayName;
        this.color = color;
        this.singular = singular;
        this.plural = plural;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColor() {
        return color;
    }

    public String getSingular() {
        return singular;
    }

    public String getPlural() {
        return plural;
    }

    public String format(double amount) {
        return color + String.format("%,.0f", amount) + " " + (amount == 1 ? singular : plural);
    }

    public static ResourceType fromString(String name) {
        for (ResourceType type : values()) {
            if (type.name().equalsIgnoreCase(name) ||
                type.singular.equalsIgnoreCase(name) ||
                type.plural.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return TOKENS;
    }
}
