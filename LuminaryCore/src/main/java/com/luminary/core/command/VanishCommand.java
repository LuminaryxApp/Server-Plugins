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

public class VanishCommand implements CommandExecutor, TabCompleter {

    private final LuminaryCore plugin;

    public VanishCommand(LuminaryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("luminarycore.vanish")) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        Player target;

        if (args.length > 0 && sender.hasPermission("luminarycore.vanish.others")) {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                MessageUtil.send(sender, plugin.getConfigManager().getMessage("player-not-online", "{player}", args[0]));
                return true;
            }
        } else if (sender instanceof Player) {
            target = (Player) sender;
        } else {
            MessageUtil.send(sender, "&cUsage: /vanish <player>");
            return true;
        }

        plugin.getStaffManager().toggleVanish(target);

        if (target != sender) {
            boolean vanished = plugin.getStaffManager().isVanished(target);
            MessageUtil.send(sender, vanished ?
                plugin.getConfigManager().getMessage("vanish-enabled-other", "{player}", target.getName()) :
                plugin.getConfigManager().getMessage("vanish-disabled-other", "{player}", target.getName()));
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("luminarycore.vanish.others")) return completions;

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
