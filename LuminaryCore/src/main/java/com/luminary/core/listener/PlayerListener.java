package com.luminary.core.listener;

import com.luminary.core.LuminaryCore;
import com.luminary.core.moderation.Punishment;
import com.luminary.core.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;

public class PlayerListener implements Listener {

    private final LuminaryCore plugin;

    public PlayerListener(LuminaryCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        // Check maintenance mode
        if (plugin.isMaintenanceMode() && !player.hasPermission("luminarycore.maintenance.bypass")) {
            String message = MessageUtil.colorize(plugin.getConfigManager().getMessage("maintenance-kick"));
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, message);
            return;
        }

        // Check ban
        Punishment ban = plugin.getModerationManager().getBan(player.getUniqueId());
        if (ban != null) {
            String message = plugin.getModerationManager().buildBanMessage(ban);
            event.disallow(PlayerLoginEvent.Result.KICK_BANNED, message);
            return;
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Handle player data
        plugin.getPlayerDataManager().handleJoin(player);

        // Handle vanish visibility
        plugin.getStaffManager().handleJoin(player);

        // Custom join message
        if (plugin.getConfigManager().getConfig().getBoolean("join-quit.custom-messages", true)) {
            String joinMessage = plugin.getConfigManager().getMessage("join-message",
                "{player}", player.getName());

            // Don't show join message for vanished players
            if (plugin.getStaffManager().isVanished(player)) {
                event.setJoinMessage(null);
            } else {
                event.setJoinMessage(MessageUtil.colorize(joinMessage));
            }
        }

        // First join message
        if (!player.hasPlayedBefore()) {
            String firstJoinMessage = plugin.getConfigManager().getMessage("first-join-message",
                "{player}", player.getName());
            if (!firstJoinMessage.isEmpty()) {
                Bukkit.broadcastMessage(MessageUtil.colorize(firstJoinMessage));
            }
        }

        // Staff join notification (for vanished joins)
        if (plugin.getStaffManager().isVanished(player)) {
            for (Player staff : Bukkit.getOnlinePlayers()) {
                if (staff.hasPermission("luminarycore.vanish.see")) {
                    MessageUtil.send(staff, plugin.getConfigManager().getMessage("vanish-join",
                        "{player}", player.getName()));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Handle player data
        plugin.getPlayerDataManager().handleQuit(player);

        // Custom quit message
        if (plugin.getConfigManager().getConfig().getBoolean("join-quit.custom-messages", true)) {
            // Don't show quit message for vanished players
            if (plugin.getStaffManager().isVanished(player)) {
                event.setQuitMessage(null);
            } else {
                String quitMessage = plugin.getConfigManager().getMessage("quit-message",
                    "{player}", player.getName());
                event.setQuitMessage(MessageUtil.colorize(quitMessage));
            }
        }

        // Clean up vanish state
        if (plugin.getStaffManager().isVanished(player)) {
            plugin.getStaffManager().setVanished(player, false);
        }

        // Clean up freeze state
        plugin.getModerationManager().unfreeze(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Check if frozen
        if (plugin.getModerationManager().isFrozen(event.getPlayer().getUniqueId())) {
            Location from = event.getFrom();
            Location to = event.getTo();

            // Allow head movement but not position
            if (to != null && (from.getX() != to.getX() || from.getY() != to.getY() || from.getZ() != to.getZ())) {
                event.setTo(from);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        // Check if frozen and trying to use non-allowed commands
        if (plugin.getModerationManager().isFrozen(player.getUniqueId())) {
            String cmd = event.getMessage().toLowerCase().split(" ")[0];

            // Allow certain commands while frozen
            if (!cmd.equals("/msg") && !cmd.equals("/r") && !cmd.equals("/tell")) {
                event.setCancelled(true);
                MessageUtil.send(player, plugin.getConfigManager().getMessage("frozen-cannot-command"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // Prevent interaction while frozen
        if (plugin.getModerationManager().isFrozen(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        // Prevent dropping items while frozen
        if (plugin.getModerationManager().isFrozen(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
