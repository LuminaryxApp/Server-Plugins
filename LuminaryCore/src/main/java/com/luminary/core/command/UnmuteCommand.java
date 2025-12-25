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
import java.util.List;

public class UnmuteCommand implements CommandExecutor, TabCompleter {

    private final LuminaryCore plugin;

    public UnmuteCommand(LuminaryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("luminarycore.unmute")) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            MessageUtil.send(sender, "&cUsage: /unmute <player>");
            return true;
        }

        String targetName = args[0];

        @SuppressWarnings("deprecation")
        var offlinePlayer = Bukkit.getOfflinePlayer(targetName);

        if (!plugin.getModerationManager().isMuted(offlinePlayer.getUniqueId())) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("not-muted", "{player}", targetName));
            return true;
        }

        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "Unmuted";
        plugin.getModerationManager().unmute(offlinePlayer.getUniqueId(), sender.getName(), reason);
        MessageUtil.send(sender, plugin.getConfigManager().getMessage("unmute-success", "{player}", targetName));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("luminarycore.unmute")) return completions;

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(input) &&
                    plugin.getModerationManager().isMuted(player.getUniqueId())) {
                    completions.add(player.getName());
                }
            }
        }
        return completions;
    }
}
