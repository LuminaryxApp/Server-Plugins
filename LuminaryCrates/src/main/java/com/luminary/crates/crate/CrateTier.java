package com.luminary.crates.crate;

/**
 * Tier/rarity levels for crates.
 */
public enum CrateTier {
    COMMON("&f", "Common"),
    UNCOMMON("&a", "Uncommon"),
    RARE("&b", "Rare"),
    EPIC("&5", "Epic"),
    LEGENDARY("&6", "Legendary"),
    MYTHIC("&d", "Mythic");

    private final String color;
    private final String displayName;

    CrateTier(String color, String displayName) {
        this.color = color;
        this.displayName = displayName;
    }

    public String getColor() {
        return color;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getColoredName() {
        return color + displayName;
    }

    public static CrateTier fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return COMMON;
        }
    }
}
