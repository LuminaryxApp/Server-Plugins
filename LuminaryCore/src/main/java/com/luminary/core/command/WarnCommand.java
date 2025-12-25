package com.luminary.core.command;

import com.luminary.core.LuminaryCore;
import com.luminary.core.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WarnCommand implements CommandExecutor, TabCompleter {

    private final LuminaryCore plugin;

    public WarnCommand(LuminaryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("luminarycore.warn")) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            MessageUtil.send(sender, "&cUsage: /warn <player> <reason>");
            return true;
        }

        String targetName = args[0];
        String reason = String.join(" ", Arrays.copyOfRange(args, 1, args.length));

        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            @SuppressWarnings("deprecation")
            var offlinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (!offlinePlayer.hasPlayedBefore() && !plugin.getPlayerDataManager().hasDataFile(offlinePlayer.getUniqueId())) {
                MessageUtil.send(sender, plugin.getConfigManager().getMessage("player-not-found", "{player}", targetName));
                return true;
            }

            plugin.getModerationManager().warn(offlinePlayer.getUniqueId(), targetName, sender, reason);
            int warnCount = plugin.getModerationManager().getActiveWarningCount(offlinePlayer.getUniqueId());
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("warn-success",
                "{player}", targetName,
                "{reason}", reason,
                "{count}", String.valueOf(warnCount)));
            return true;
        }

        if (target.hasPermission("luminarycore.warn.bypass")) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("cannot-punish"));
            return true;
        }

        plugin.getModerationManager().warn(target.getUniqueId(), target.getName(), sender, reason);
        int warnCount = plugin.getModerationManager().getActiveWarningCount(target.getUniqueId());
        MessageUtil.send(sender, plugin.getConfigManager().getMessage("warn-success",
            "{player}", target.getName(),
            "{reason}", reason,
            "{count}", String.valueOf(warnCount)));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("luminarycore.warn")) return completions;

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
