package com.luminary.mines.mine;

import com.luminary.mines.LuminaryMines;
import com.luminary.mines.schematic.MineSchematic;
import com.luminary.mines.schematic.SchematicManager;
import com.luminary.mines.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Handles periodic mine resets and block filling.
 */
public class MineResetTask {

    private final LuminaryMines plugin;
    private BukkitTask checkTask;
    private final Random random = new Random();

    // Block fill rate (blocks per tick for async filling)
    private static final int BLOCKS_PER_TICK = 5000;

    public MineResetTask(LuminaryMines plugin) {
        this.plugin = plugin;
    }

    public void start() {
        // Check every second for mines that need resetting
        checkTask = Bukkit.getScheduler().runTaskTimer(plugin, this::checkMines, 20L, 20L);
    }

    public void stop() {
        if (checkTask != null) {
            checkTask.cancel();
            checkTask = null;
        }
    }

    public void reload() {
        stop();
        start();
    }

    /**
     * Track a block break for auto-reset threshold.
     */
    public void trackBlockBreak(Mine mine) {
        // Check if we should auto-reset based on threshold
        double threshold = plugin.getConfigManager().getAutoResetThreshold();
        if (threshold > 0) {
            double minedPercent = getMinedPercentage(mine);
            if (minedPercent >= threshold && mine.isAutoReset()) {
                forceReset(mine);
            }
        }
    }

    /**
     * Check all mines and reset those that need it.
     */
    private void checkMines() {
        List<Mine> needsReset = plugin.getMineManager().getMinesNeedingReset();

        for (Mine mine : needsReset) {
            if (!mine.isAutoReset()) {
                continue;
            }

            long timeUntil = mine.getTimeUntilReset();

            // Send warning at 10 seconds
            if (timeUntil <= 10000L && timeUntil > 9000L) {
                notifyMinePlayers(mine, plugin.getConfigManager().getRawMessage("mine.reset-warning"));
            }

            // Reset the mine
            if (timeUntil <= 0) {
                resetMine(mine);
            }
        }
    }

    /**
     * Reset a mine (fill with blocks).
     */
    public void resetMine(Mine mine) {
        MineSchematic schematic = plugin.getSchematicManager().getSchematic(mine.getSchematicName());

        // Get the interior region to fill
        SchematicManager.MineRegion region;
        if (schematic != null) {
            region = plugin.getSchematicManager().getInteriorRegion(mine, schematic);
        } else {
            // Fallback: use mine bounds with 1 block inset
            Location min = mine.getMinCorner();
            Location max = mine.getMaxCorner();
            if (min == null || max == null) return;

            region = new SchematicManager.MineRegion(
                    min.getBlockX() + 1, min.getBlockY() + 1, min.getBlockZ() + 1,
                    max.getBlockX() - 1, max.getBlockY() - 1, max.getBlockZ() - 1
            );
        }

        // Teleport players out of the mine before reset
        teleportPlayersOut(mine);

        // Fill the mine with blocks
        fillMine(mine, region);

        // Mark as reset
        mine.markReset();
        plugin.getMineManager().saveMine(mine);

        // Notify players
        notifyMinePlayers(mine, plugin.getConfigManager().getMessage("mine.reset-complete"));
    }

    /**
     * Fill a mine region with blocks based on composition.
     */
    private void fillMine(Mine mine, SchematicManager.MineRegion region) {
        World world = Bukkit.getWorld(mine.getWorldName());
        if (world == null) return;

        Map<String, Integer> composition = mine.getBlockComposition();
        if (composition.isEmpty()) {
            // Default composition
            composition = new LinkedHashMap<>();
            composition.put("STONE", 100);
        }

        // Build weighted block list
        List<Material> weightedBlocks = buildWeightedBlockList(composition);
        if (weightedBlocks.isEmpty()) return;

        // Fill synchronously in chunks to avoid lag
        int totalBlocks = region.getVolume();
        int blocksPlaced = 0;

        for (int x = region.minX; x <= region.maxX; x++) {
            for (int y = region.minY; y <= region.maxY; y++) {
                for (int z = region.minZ; z <= region.maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    Material material = weightedBlocks.get(random.nextInt(weightedBlocks.size()));
                    block.setType(material, false);
                    blocksPlaced++;
                }
            }
        }

        plugin.getLogger().info("Reset mine for " + mine.getOwnerName() +
                " - " + blocksPlaced + " blocks filled");
    }

    /**
     * Build a weighted list of materials for random selection.
     */
    private List<Material> buildWeightedBlockList(Map<String, Integer> composition) {
        List<Material> weighted = new ArrayList<>();
        int totalWeight = composition.values().stream().mapToInt(Integer::intValue).sum();

        // Normalize to 100 entries for easy random selection
        for (Map.Entry<String, Integer> entry : composition.entrySet()) {
            Material material = Material.matchMaterial(entry.getKey());
            if (material != null && material.isBlock()) {
                int count = (entry.getValue() * 100) / totalWeight;
                for (int i = 0; i < count; i++) {
                    weighted.add(material);
                }
            }
        }

        // Ensure at least one block type
        if (weighted.isEmpty()) {
            weighted.add(Material.STONE);
        }

        return weighted;
    }

    /**
     * Teleport players out of a mine before reset.
     */
    private void teleportPlayersOut(Mine mine) {
        Location spawn = mine.getSpawnLocation();
        if (spawn == null) return;

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (mine.isInMine(player.getLocation())) {
                player.teleport(spawn);
                player.sendMessage(TextUtil.colorize(
                        plugin.getConfigManager().getMessage("mine.teleported-for-reset")));
            }
        }
    }

    /**
     * Notify players in or owning a mine.
     */
    private void notifyMinePlayers(Mine mine, String message) {
        // Notify owner
        Player owner = Bukkit.getPlayer(mine.getOwnerId());
        if (owner != null) {
            owner.sendMessage(TextUtil.colorize(message));
        }

        // Notify whitelisted players in the mine
        for (UUID whitelisted : mine.getWhitelist()) {
            Player player = Bukkit.getPlayer(whitelisted);
            if (player != null && mine.isInMine(player.getLocation())) {
                player.sendMessage(TextUtil.colorize(message));
            }
        }
    }

    /**
     * Manually reset a specific mine.
     */
    public void forceReset(Mine mine) {
        resetMine(mine);
    }

    /**
     * Get the percentage of blocks mined in a mine.
     */
    public double getMinedPercentage(Mine mine) {
        Location min = mine.getMinCorner();
        Location max = mine.getMaxCorner();
        if (min == null || max == null) return 0;

        World world = min.getWorld();
        if (world == null) return 0;

        int total = 0;
        int air = 0;

        // Count air blocks in the interior
        for (int x = min.getBlockX() + 1; x < max.getBlockX(); x++) {
            for (int y = min.getBlockY() + 1; y < max.getBlockY(); y++) {
                for (int z = min.getBlockZ() + 1; z < max.getBlockZ(); z++) {
                    total++;
                    if (world.getBlockAt(x, y, z).getType() == Material.AIR) {
                        air++;
                    }
                }
            }
        }

        return total > 0 ? (air * 100.0 / total) : 0;
    }
}
