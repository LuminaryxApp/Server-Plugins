package com.luminary.miners.task;

import com.luminary.miners.LuminaryMiners;
import com.luminary.miners.miner.MinerType;
import com.luminary.miners.miner.PlayerMiner;
import com.luminary.miners.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;

/**
 * Runs periodically to produce resources from all active miners.
 */
public class ProductionTask implements Runnable {

    private final LuminaryMiners plugin;
    private BukkitTask task;

    public ProductionTask(LuminaryMiners plugin) {
        this.plugin = plugin;
    }

    public void start() {
        int intervalSeconds = plugin.getConfigManager().getProductionInterval();
        long intervalTicks = intervalSeconds * 20L;

        task = Bukkit.getScheduler().runTaskTimer(plugin, this, intervalTicks, intervalTicks);
        plugin.getLogger().info("Production task started with " + intervalSeconds + "s interval.");
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    @Override
    public void run() {
        // Process all online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            processPlayer(player.getUniqueId());
        }

        // Also process loaded offline players
        Map<UUID, PlayerData> loadedData = plugin.getPlayerDataManager().getLoadedData();
        for (UUID playerId : loadedData.keySet()) {
            Player online = Bukkit.getPlayer(playerId);
            if (online == null) {
                processPlayer(playerId);
            }
        }
    }

    /**
     * Process production for a single player's miners.
     */
    public void processPlayer(UUID playerId) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(playerId);

        for (PlayerMiner miner : data.getMiners()) {
            if (!miner.isActive()) {
                continue;
            }

            MinerType type = plugin.getMinerManager().getMinerType(miner.getTypeId());
            if (type == null) {
                continue;
            }

            // Calculate production for this interval
            double productionPerHour = type.getProductionPerHour(miner.getTier());
            double intervalSeconds = plugin.getConfigManager().getProductionInterval();
            double production = (productionPerHour / 3600.0) * intervalSeconds;

            // Add to miner storage
            double maxStorage = type.getStorageCapacity(miner.getTier());
            miner.addResources(production, maxStorage);
            data.markDirty();
        }
    }

    /**
     * Calculate and apply offline production for a player.
     */
    public void processOfflineProduction(UUID playerId) {
        if (!plugin.getConfigManager().isOfflineProductionEnabled()) {
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(playerId);
        long offlineMs = data.getOfflineTime();

        // Cap offline time
        int maxOfflineHours = plugin.getConfigManager().getMaxOfflineHours();
        long maxOfflineMs = maxOfflineHours * 60L * 60L * 1000L;
        offlineMs = Math.min(offlineMs, maxOfflineMs);

        // Skip if less than a minute offline
        if (offlineMs < 60000) {
            return;
        }

        double offlineHours = offlineMs / (1000.0 * 60.0 * 60.0);

        for (PlayerMiner miner : data.getMiners()) {
            if (!miner.isActive()) {
                continue;
            }

            MinerType type = plugin.getMinerManager().getMinerType(miner.getTypeId());
            if (type == null) {
                continue;
            }

            // Calculate offline production
            double productionPerHour = type.getProductionPerHour(miner.getTier());
            double production = productionPerHour * offlineHours;

            // Add to miner storage (capped by storage limit)
            double maxStorage = type.getStorageCapacity(miner.getTier());
            miner.addResources(production, maxStorage);
        }

        data.markDirty();
        data.updateLastOnlineTime();

        // Notify player
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            int hours = (int) (offlineMs / (1000 * 60 * 60));
            int minutes = (int) ((offlineMs % (1000 * 60 * 60)) / (1000 * 60));
            if (hours > 0 || minutes > 0) {
                String timeStr = hours > 0 ? hours + "h " + minutes + "m" : minutes + "m";
                player.sendMessage(plugin.getConfigManager().getMessage("offline-production",
                    "{time}", timeStr));
            }
        }
    }

    /**
     * Force production cycle for testing.
     */
    public void forceCycle() {
        run();
    }
}
