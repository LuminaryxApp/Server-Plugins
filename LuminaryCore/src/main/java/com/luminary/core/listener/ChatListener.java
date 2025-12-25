package com.luminary.core.listener;

import com.luminary.core.LuminaryCore;
import com.luminary.core.moderation.Punishment;
import com.luminary.core.util.MessageUtil;
import com.luminary.core.util.TimeUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    private final LuminaryCore plugin;

    public ChatListener(LuminaryCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        // Check if muted
        Punishment mute = plugin.getModerationManager().getMute(player.getUniqueId());
        if (mute != null) {
            event.setCancelled(true);

            String remaining = mute.isPermanent() ? "Permanent" :
                TimeUtil.formatRemaining(mute.getExpiresAt());

            MessageUtil.send(player, plugin.getConfigManager().getMessage("muted-chat",
                "{reason}", mute.getReason(),
                "{remaining}", remaining));
            return;
        }

        // Check if frozen
        if (plugin.getModerationManager().isFrozen(player.getUniqueId())) {
            event.setCancelled(true);
            MessageUtil.send(player, plugin.getConfigManager().getMessage("frozen-cannot-chat"));
            return;
        }

        // Check if staff chat mode is enabled
        if (plugin.getStaffManager().hasStaffChatEnabled(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getStaffManager().sendStaffMessage(player, event.getMessage());
            return;
        }
    }
}
