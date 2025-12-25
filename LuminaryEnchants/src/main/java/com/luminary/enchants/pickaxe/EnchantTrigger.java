package com.luminary.enchants.pickaxe;

/**
 * Trigger types for when enchants can activate.
 */
public enum EnchantTrigger {
    BLOCK_BREAK,
    MINE_TICK,
    INTERACT;

    public static EnchantTrigger fromString(String name) {
        try {
            return valueOf(name.toUpperCase().replace("-", "_"));
        } catch (IllegalArgumentException e) {
            return BLOCK_BREAK;
        }
    }
}
