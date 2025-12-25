package com.luminary.core.command;

import com.luminary.core.LuminaryCore;
import com.luminary.core.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class UnbanCommand implements CommandExecutor, TabCompleter {

    private final LuminaryCore plugin;

    public UnbanCommand(LuminaryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("luminarycore.unban")) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            MessageUtil.send(sender, "&cUsage: /unban <player>");
            return true;
        }

        String targetName = args[0];

        @SuppressWarnings("deprecation")
        var offlinePlayer = Bukkit.getOfflinePlayer(targetName);

        if (!plugin.getModerationManager().isBanned(offlinePlayer.getUniqueId())) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("not-banned", "{player}", targetName));
            return true;
        }

        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "Unbanned";
        plugin.getModerationManager().unban(offlinePlayer.getUniqueId(), sender.getName(), reason);
        MessageUtil.send(sender, plugin.getConfigManager().getMessage("unban-success", "{player}", targetName));

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("luminarycore.unban")) return completions;
        return completions;
    }
}
