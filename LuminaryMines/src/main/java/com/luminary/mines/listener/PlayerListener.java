package com.luminary.mines.listener;

import com.luminary.mines.LuminaryMines;
import com.luminary.mines.mine.Mine;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

/**
 * Handles player interactions with mines (access control, protection).
 */
public class PlayerListener implements Listener {

    private final LuminaryMines plugin;

    public PlayerListener(LuminaryMines plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Location blockLoc = event.getBlock().getLocation();

        Mine mine = getMineAtLocation(blockLoc);
        if (mine == null) {
            return;
        }

        // Check if player has access
        if (!hasAccess(player, mine)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getMessage("mine.no-access"));
            return;
        }

        // Track block break for reset percentage
        plugin.getResetTask().trackBlockBreak(mine);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location blockLoc = event.getBlock().getLocation();

        Mine mine = getMineAtLocation(blockLoc);
        if (mine == null) {
            return;
        }

        // Only allow placing in own mine or if whitelisted
        if (!hasAccess(player, mine)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getMessage("mine.no-access"));
            return;
        }

        // Prevent placing blocks in the mine fill area
        if (isInMineRegion(blockLoc, mine)) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getMessage("mine.cannot-place"));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Only check if actually moving to a new block
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();
        Location to = event.getTo();

        Mine mine = getMineAtLocation(to);
        if (mine == null) {
            return;
        }

        // Check if player has access
        if (!hasAccess(player, mine)) {
            // Push player back
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getMessage("mine.no-access"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();

        if (to == null) return;

        Mine mine = getMineAtLocation(to);
        if (mine == null) {
            return;
        }

        // Check if player has access (bypass for admins)
        if (!hasAccess(player, mine) && !player.hasPermission("luminarymines.admin")) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getMessage("mine.no-access"));
        }
    }

    /**
     * Check if a player has access to a mine.
     */
    private boolean hasAccess(Player player, Mine mine) {
        UUID playerId = player.getUniqueId();

        // Owner always has access
        if (mine.getOwnerId().equals(playerId)) {
            return true;
        }

        // Admin bypass
        if (player.hasPermission("luminarymines.admin")) {
            return true;
        }

        // Check whitelist
        return mine.isWhitelisted(playerId);
    }

    /**
     * Get the mine at a specific location.
     */
    private Mine getMineAtLocation(Location location) {
        String worldName = location.getWorld() != null ? location.getWorld().getName() : null;
        if (worldName == null) {
            return null;
        }

        // Check all mines
        for (Mine mine : plugin.getMineManager().getAllMines()) {
            if (!worldName.equals(mine.getWorldName())) {
                continue;
            }

            // Check if location is within mine bounds
            if (isInMineBounds(location, mine)) {
                return mine;
            }
        }

        return null;
    }

    /**
     * Check if a location is within a mine's full bounds (including structure).
     */
    private boolean isInMineBounds(Location loc, Mine mine) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return x >= mine.getMinX() && x <= mine.getMaxX() &&
               y >= mine.getMinY() && y <= mine.getMaxY() &&
               z >= mine.getMinZ() && z <= mine.getMaxZ();
    }

    /**
     * Check if a location is within the mineable region of a mine.
     */
    private boolean isInMineRegion(Location loc, Mine mine) {
        // Get the mine region from schematic manager
        var region = plugin.getSchematicManager().getMineRegion(mine);
        if (region == null) {
            return false;
        }

        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();

        return x >= region.minX && x <= region.maxX &&
               y >= region.minY && y <= region.maxY &&
               z >= region.minZ && z <= region.maxZ;
    }
}
