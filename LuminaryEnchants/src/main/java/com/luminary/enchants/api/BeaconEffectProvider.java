package com.luminary.enchants.api;

import java.util.UUID;

/**
 * Interface for beacon effects/multipliers integration.
 * Implementations connect to LuminaryBeacons plugin.
 */
public interface BeaconEffectProvider {

    /**
     * Get the multiplier for a specific effect key.
     * @param playerId The player's UUID
     * @param key The effect key (e.g., "PROC_CHANCE", "TOKEN_GAIN")
     * @return The multiplier (default 1.0 if not present)
     */
    double multiplier(UUID playerId, String key);

    /**
     * Get the level of a specific effect.
     * @param playerId The player's UUID
     * @param key The effect key
     * @return The level (default 0 if not present)
     */
    int level(UUID playerId, String key);

    /**
     * Check if a player has a specific effect active.
     * @param playerId The player's UUID
     * @param key The effect key
     * @return true if the effect is active
     */
    boolean has(UUID playerId, String key);

    /**
     * Check if the beacon provider is available.
     * @return true if the provider can be used
     */
    boolean isAvailable();

    // Standard effect keys
    String PROC_CHANCE = "PROC_CHANCE";
    String TOKEN_GAIN = "TOKEN_GAIN";
    String KEY_GAIN = "KEY_GAIN";
    String EXTRA_DROPS = "EXTRA_DROPS";
    String COOLDOWN_REDUCTION = "COOLDOWN_REDUCTION";
}
