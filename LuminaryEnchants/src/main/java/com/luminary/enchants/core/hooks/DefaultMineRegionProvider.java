package com.luminary.enchants.core.hooks;

import com.luminary.enchants.api.MineRegionProvider;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Default mine region provider when no mine plugin is available.
 * Allows breaking everywhere (no restrictions).
 */
public class DefaultMineRegionProvider implements MineRegionProvider {

    @Override
    public boolean isInMineRegion(Location location) {
        return true; // Allow everywhere when no mine plugin
    }

    @Override
    public boolean canPlayerBreakAt(Player player, Location location) {
        return true; // Allow everywhere when no mine plugin
    }

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public String getProviderName() {
        return "None";
    }
}
