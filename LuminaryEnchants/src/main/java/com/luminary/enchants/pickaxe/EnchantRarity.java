package com.luminary.enchants.pickaxe;

/**
 * Rarity levels for pickaxe enchants.
 */
public enum EnchantRarity {
    COMMON,
    UNCOMMON,
    RARE,
    EPIC,
    LEGENDARY;

    public static EnchantRarity fromString(String name) {
        try {
            return valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return COMMON;
        }
    }
}
