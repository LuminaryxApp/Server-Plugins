package com.luminary.enchants.core.hooks;

import com.luminary.enchants.api.MineRegionProvider;
import com.luminary.mines.LuminaryMines;
import com.luminary.mines.mine.Mine;
import com.luminary.mines.schematic.MineSchematic;
import com.luminary.mines.schematic.SchematicManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Hook into LuminaryMines for mine region protection.
 * Only allows enchant block breaking within the mine's interior region.
 */
public class LuminaryMinesHook implements MineRegionProvider {

    private final LuminaryMines minesPlugin;

    public LuminaryMinesHook(LuminaryMines minesPlugin) {
        this.minesPlugin = minesPlugin;
    }

    @Override
    public boolean isInMineRegion(Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        // Check if the location is in the mine world
        String mineWorld = minesPlugin.getConfigManager().getMineWorld();
        if (!location.getWorld().getName().equals(mineWorld)) {
            // Not in mine world - allow breaking (could be in normal world)
            return true;
        }

        // In mine world - check if in a mine's interior region
        Mine mine = minesPlugin.getMineManager().getMineAt(location);
        if (mine == null) {
            // Not in any mine bounds - don't allow (could be outside structure)
            return false;
        }

        // Check if in the interior (fillable) region of the mine
        return isInMineInterior(mine, location);
    }

    @Override
    public boolean canPlayerBreakAt(Player player, Location location) {
        if (location == null || location.getWorld() == null) {
            return false;
        }

        // Check if the location is in the mine world
        String mineWorld = minesPlugin.getConfigManager().getMineWorld();
        if (!location.getWorld().getName().equals(mineWorld)) {
            // Not in mine world - allow (normal world rules apply)
            return true;
        }

        // In mine world - check mine ownership/whitelist AND region
        Mine mine = minesPlugin.getMineManager().getMineAt(location);
        if (mine == null) {
            // Not in any mine - don't allow breaking random stuff
            return false;
        }

        // Check if player is owner or whitelisted
        if (!mine.isWhitelisted(player.getUniqueId()) &&
            !player.hasPermission("luminarymines.bypass")) {
            return false;
        }

        // Check if in the interior region
        return isInMineInterior(mine, location);
    }

    /**
     * Check if a location is within the mine's interior (fillable) region.
     */
    private boolean isInMineInterior(Mine mine, Location location) {
        SchematicManager schematicManager = minesPlugin.getSchematicManager();
        SchematicManager.MineRegion region = schematicManager.getMineRegion(mine);

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();

        return x >= region.minX && x <= region.maxX &&
               y >= region.minY && y <= region.maxY &&
               z >= region.minZ && z <= region.maxZ;
    }

    @Override
    public boolean isAvailable() {
        return minesPlugin != null && minesPlugin.isEnabled();
    }

    @Override
    public String getProviderName() {
        return "LuminaryMines";
    }
}
