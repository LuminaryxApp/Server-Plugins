package com.luminary.core.command;

import com.luminary.core.LuminaryCore;
import com.luminary.core.util.MessageUtil;
import com.luminary.core.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TempmuteCommand implements CommandExecutor, TabCompleter {

    private final LuminaryCore plugin;

    public TempmuteCommand(LuminaryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("luminarycore.tempmute")) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 2) {
            MessageUtil.send(sender, "&cUsage: /tempmute <player> <duration> [reason]");
            MessageUtil.send(sender, "&7Duration format: 1d, 2h, 30m, 1w, etc.");
            return true;
        }

        String targetName = args[0];
        String durationStr = args[1];
        long duration = TimeUtil.parseTime(durationStr);

        if (duration < 0) {
            MessageUtil.send(sender, "&cInvalid duration format. Use: 1d, 2h, 30m, 1w, etc.");
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            @SuppressWarnings("deprecation")
            var offlinePlayer = Bukkit.getOfflinePlayer(targetName);
            if (!offlinePlayer.hasPlayedBefore() && !plugin.getPlayerDataManager().hasDataFile(offlinePlayer.getUniqueId())) {
                MessageUtil.send(sender, plugin.getConfigManager().getMessage("player-not-found", "{player}", targetName));
                return true;
            }

            String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "No reason provided";
            plugin.getModerationManager().mute(offlinePlayer.getUniqueId(), targetName, sender, reason, duration);
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("tempmute-success",
                "{player}", targetName,
                "{duration}", TimeUtil.formatDuration(duration),
                "{reason}", reason));
            return true;
        }

        if (target.hasPermission("luminarycore.mute.bypass")) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("cannot-punish"));
            return true;
        }

        String reason = args.length > 2 ? String.join(" ", Arrays.copyOfRange(args, 2, args.length)) : "No reason provided";
        plugin.getModerationManager().mute(target.getUniqueId(), target.getName(), sender, reason, duration);
        MessageUtil.send(sender, plugin.getConfigManager().getMessage("tempmute-success",
            "{player}", target.getName(),
            "{duration}", TimeUtil.formatDuration(duration),
            "{reason}", reason));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("luminarycore.tempmute")) return completions;

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(input)) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 2) {
            completions.addAll(Arrays.asList("10m", "30m", "1h", "6h", "12h", "1d", "3d", "7d"));
        }
        return completions;
    }
}
