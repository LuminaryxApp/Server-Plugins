package com.luminary.enchants.api;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Provider for mine region checks.
 * Used to prevent enchants from breaking blocks outside mine areas.
 */
public interface MineRegionProvider {

    /**
     * Check if a location is within the breakable region of a mine.
     * This should check the interior/fillable region, not the entire mine bounds.
     *
     * @param location The location to check
     * @return true if the location is in a mine's breakable region
     */
    boolean isInMineRegion(Location location);

    /**
     * Check if a player is allowed to break at a location in a mine.
     *
     * @param player The player
     * @param location The location to check
     * @return true if the player can break blocks at this location
     */
    boolean canPlayerBreakAt(Player player, Location location);

    /**
     * Check if this provider is available.
     */
    boolean isAvailable();

    /**
     * Get the provider name.
     */
    String getProviderName();
}
