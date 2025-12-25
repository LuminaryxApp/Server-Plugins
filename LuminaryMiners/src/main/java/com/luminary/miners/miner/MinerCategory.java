package com.luminary.miners.miner;

/**
 * Categories of miners available.
 */
public enum MinerCategory {
    ROBOT("Robot", "Mechanical miners that tirelessly collect resources"),
    GOLEM("Golem", "Mystical constructs powered by ancient magic");

    private final String displayName;
    private final String description;

    MinerCategory(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public static MinerCategory fromString(String name) {
        for (MinerCategory category : values()) {
            if (category.name().equalsIgnoreCase(name)) {
                return category;
            }
        }
        return ROBOT;
    }
}
