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

public class MaintenanceCommand implements CommandExecutor, TabCompleter {

    private final LuminaryCore plugin;

    public MaintenanceCommand(LuminaryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("luminarycore.maintenance")) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            // Toggle
            boolean newState = !plugin.isMaintenanceMode();
            plugin.setMaintenanceMode(newState);

            if (newState) {
                MessageUtil.send(sender, plugin.getConfigManager().getMessage("maintenance-enabled"));
                // Kick non-bypass players
                String kickMessage = MessageUtil.colorize(plugin.getConfigManager().getMessage("maintenance-kick"));
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("luminarycore.maintenance.bypass")) {
                        player.kickPlayer(kickMessage);
                    }
                }
            } else {
                MessageUtil.send(sender, plugin.getConfigManager().getMessage("maintenance-disabled"));
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "on":
            case "enable":
                if (plugin.isMaintenanceMode()) {
                    MessageUtil.send(sender, "&cMaintenance mode is already enabled!");
                    return true;
                }
                plugin.setMaintenanceMode(true);
                MessageUtil.send(sender, plugin.getConfigManager().getMessage("maintenance-enabled"));
                String kickMessage = MessageUtil.colorize(plugin.getConfigManager().getMessage("maintenance-kick"));
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!player.hasPermission("luminarycore.maintenance.bypass")) {
                        player.kickPlayer(kickMessage);
                    }
                }
                break;

            case "off":
            case "disable":
                if (!plugin.isMaintenanceMode()) {
                    MessageUtil.send(sender, "&cMaintenance mode is already disabled!");
                    return true;
                }
                plugin.setMaintenanceMode(false);
                MessageUtil.send(sender, plugin.getConfigManager().getMessage("maintenance-disabled"));
                break;

            case "status":
                MessageUtil.send(sender, "&7Maintenance mode: " +
                    (plugin.isMaintenanceMode() ? "&aEnabled" : "&cDisabled"));
                break;

            default:
                MessageUtil.send(sender, "&cUsage: /maintenance [on|off|status]");
                break;
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("luminarycore.maintenance")) return completions;

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (String option : Arrays.asList("on", "off", "status")) {
                if (option.startsWith(input)) {
                    completions.add(option);
                }
            }
        }
        return completions;
    }
}
