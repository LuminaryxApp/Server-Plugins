package com.luminary.economy.listener;

import com.luminary.economy.LuminaryEconomy;
import com.luminary.economy.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player join/quit events for data loading and scoreboards.
 */
public class PlayerListener implements Listener {

    private final LuminaryEconomy plugin;

    public PlayerListener(LuminaryEconomy plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load player data
        plugin.getDataManager().loadPlayer(player.getUniqueId(), player.getName());

        // Create scoreboard after a short delay (let other plugins set things up)
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                PlayerData data = plugin.getDataManager().getPlayerData(player.getUniqueId());
                if (data != null && data.isScoreboardEnabled()) {
                    plugin.getScoreboardManager().createScoreboard(player);
                }
            }
        }, 10L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Remove scoreboard
        plugin.getScoreboardManager().removeScoreboard(player);

        // Save and unload player data
        plugin.getDataManager().unloadPlayer(player.getUniqueId());
    }
}
