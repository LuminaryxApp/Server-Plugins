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

public class FreezeCommand implements CommandExecutor, TabCompleter {

    private final LuminaryCore plugin;

    public FreezeCommand(LuminaryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("luminarycore.freeze")) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length < 1) {
            MessageUtil.send(sender, "&cUsage: /freeze <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("player-not-online", "{player}", args[0]));
            return true;
        }

        if (target.hasPermission("luminarycore.freeze.bypass")) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("cannot-punish"));
            return true;
        }

        boolean isFrozen = plugin.getModerationManager().isFrozen(target.getUniqueId());

        if (isFrozen) {
            plugin.getModerationManager().unfreeze(target.getUniqueId());
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("unfreeze-success", "{player}", target.getName()));
            MessageUtil.send(target, plugin.getConfigManager().getMessage("unfrozen"));
        } else {
            plugin.getModerationManager().freeze(target.getUniqueId());
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("freeze-success", "{player}", target.getName()));
            MessageUtil.send(target, plugin.getConfigManager().getMessage("frozen"));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("luminarycore.freeze")) return completions;

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
