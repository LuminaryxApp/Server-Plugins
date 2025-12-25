package com.luminary.core.command;

import com.luminary.core.LuminaryCore;
import com.luminary.core.moderation.Punishment;
import com.luminary.core.util.MessageUtil;
import com.luminary.core.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HistoryCommand implements CommandExecutor, TabCompleter {

    private final LuminaryCore plugin;

    public HistoryCommand(LuminaryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("luminarycore.history")) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            MessageUtil.send(sender, "&cUsage: /history <player>");
            return true;
        }

        String targetName = args[0];

        @SuppressWarnings("deprecation")
        var offlinePlayer = Bukkit.getOfflinePlayer(targetName);

        if (!offlinePlayer.hasPlayedBefore() && !plugin.getPlayerDataManager().hasDataFile(offlinePlayer.getUniqueId())) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("player-not-found", "{player}", targetName));
            return true;
        }

        List<Punishment> history = plugin.getModerationManager().getHistory(offlinePlayer.getUniqueId());

        if (history.isEmpty()) {
            MessageUtil.send(sender, "&7" + targetName + " has no punishment history.");
            return true;
        }

        MessageUtil.send(sender, "&6=== Punishment History for " + targetName + " ===");

        for (Punishment p : history) {
            String status = p.isActive() ? "&a[Active]" : "&7[Expired]";
            String type = "&e" + p.getType().name();
            String time = "&7" + TimeUtil.formatTimestamp(p.getIssuedAt());

            MessageUtil.send(sender, status + " " + type + " &7by &f" + p.getIssuerName());
            MessageUtil.send(sender, "  &7Reason: &f" + p.getReason());
            MessageUtil.send(sender, "  &7Date: &f" + time);

            if (p.getType() != Punishment.Type.WARN && p.getType() != Punishment.Type.KICK) {
                if (p.isPermanent()) {
                    MessageUtil.send(sender, "  &7Duration: &cPermanent");
                } else {
                    MessageUtil.send(sender, "  &7Duration: &f" + TimeUtil.formatDuration(p.getDuration()));
                    if (p.isActive()) {
                        MessageUtil.send(sender, "  &7Remaining: &f" + TimeUtil.formatRemaining(p.getExpiresAt()));
                    }
                }
            }

            if (p.getRevokedBy() != null) {
                MessageUtil.send(sender, "  &7Revoked by: &f" + p.getRevokedBy());
            }

            MessageUtil.send(sender, "");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("luminarycore.history")) return completions;

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(input)) {
                    completions.add(player.getName());
                }
            }
        }
        return completions;
    }
}
