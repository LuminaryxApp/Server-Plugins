package com.luminary.miners.listener;

import com.luminary.miners.LuminaryMiners;
import com.luminary.miners.task.ProductionTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player join/quit events for data loading and offline production.
 */
public class PlayerListener implements Listener {

    private final LuminaryMiners plugin;

    public PlayerListener(LuminaryMiners plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Load player data
        plugin.getPlayerDataManager().getPlayerData(event.getPlayer());

        // Process offline production
        ProductionTask task = plugin.getProductionTask();
        if (task != null) {
            task.processOfflineProduction(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Update last online time and save
        plugin.getPlayerDataManager().getPlayerData(event.getPlayer()).updateLastOnlineTime();
        plugin.getPlayerDataManager().savePlayerData(event.getPlayer());
    }
}
