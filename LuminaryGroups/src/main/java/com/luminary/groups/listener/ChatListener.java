package com.luminary.groups.listener;

import com.luminary.groups.LuminaryGroups;
import com.luminary.groups.group.Group;
import com.luminary.groups.util.ColorUtil;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Handles chat formatting with group prefixes.
 */
public class ChatListener implements Listener {

    private final LuminaryGroups plugin;

    public ChatListener(LuminaryGroups plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        if (!plugin.getConfigManager().isChatFormattingEnabled()) {
            return;
        }

        Player player = event.getPlayer();

        // Get group information
        String prefix = plugin.getPlayerDataManager().getPrefix(player.getUniqueId());
        String suffix = plugin.getPlayerDataManager().getSuffix(player.getUniqueId());
        String nameColor = plugin.getPlayerDataManager().getNameColor(player.getUniqueId());

        // Get rank information if hooked
        String rankDisplay = "";
        if (plugin.getRanksHook().isHooked()) {
            rankDisplay = plugin.getRanksHook().getCombinedRankDisplay(player);
        }

        // Get chat color from group
        Group effectiveGroup = plugin.getPlayerDataManager().getEffectiveGroup(player.getUniqueId());
        String chatColor = effectiveGroup != null ? effectiveGroup.getChatColor() : "&f";

        // Get message as plain text
        String message = LegacyComponentSerializer.legacySection().serialize(event.message());

        // Format the chat message
        String format = plugin.getConfigManager().getChatFormat();

        format = format.replace("{rank}", rankDisplay);
        format = format.replace("{prefix}", prefix);
        format = format.replace("{suffix}", suffix);
        format = format.replace("{name_color}", nameColor);
        format = format.replace("{chat_color}", chatColor);
        format = format.replace("{name}", player.getName());
        format = format.replace("{displayname}", player.getName());
        format = format.replace("{message}", chatColor + message);

        // Apply colors
        String coloredFormat = ColorUtil.colorize(format);

        // Set the rendered message
        Component formattedMessage = LegacyComponentSerializer.legacySection().deserialize(coloredFormat);

        event.renderer((source, sourceDisplayName, msg, viewer) -> formattedMessage);
    }
}
