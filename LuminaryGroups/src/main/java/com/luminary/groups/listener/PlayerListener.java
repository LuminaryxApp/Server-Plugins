package com.luminary.groups.listener;

import com.luminary.groups.LuminaryGroups;
import com.luminary.groups.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles player join and quit events.
 */
public class PlayerListener implements Listener {

    private final LuminaryGroups plugin;

    public PlayerListener(LuminaryGroups plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load player data
        plugin.getPlayerDataManager().loadPlayer(player);

        // Set up permissions
        plugin.getPermissionManager().setupPlayer(player);

        // Update tab list name
        updateTabListName(player);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Remove permissions
        plugin.getPermissionManager().removePermissions(player.getUniqueId());

        // Save and unload player data
        plugin.getPlayerDataManager().unloadPlayer(player.getUniqueId());
    }

    /**
     * Update a player's tab list name with their prefix.
     */
    public void updateTabListName(Player player) {
        if (!plugin.getConfigManager().isTabListEnabled()) {
            return;
        }

        String prefix = plugin.getPlayerDataManager().getPrefix(player.getUniqueId());
        String suffix = plugin.getPlayerDataManager().getSuffix(player.getUniqueId());
        String nameColor = plugin.getPlayerDataManager().getNameColor(player.getUniqueId());

        // Get rank display if hooked
        String rankDisplay = "";
        if (plugin.getRanksHook().isHooked()) {
            rankDisplay = plugin.getRanksHook().getCombinedRankDisplay(player);
        }

        String format = plugin.getConfigManager().getTabListFormat();
        format = format.replace("{rank}", rankDisplay);
        format = format.replace("{prefix}", prefix);
        format = format.replace("{suffix}", suffix);
        format = format.replace("{name_color}", nameColor);
        format = format.replace("{name}", player.getName());
        format = format.replace("{displayname}", player.getName());

        Component tabName = ColorUtil.toComponent(format);
        player.playerListName(tabName);
    }

    /**
     * Refresh tab list name for a player (call after group changes).
     */
    public void refreshPlayer(Player player) {
        updateTabListName(player);
    }
}
