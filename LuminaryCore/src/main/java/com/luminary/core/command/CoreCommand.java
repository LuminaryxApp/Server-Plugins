package com.luminary.core.command;

import com.luminary.core.LuminaryCore;
import com.luminary.core.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CoreCommand implements CommandExecutor, TabCompleter {

    private final LuminaryCore plugin;

    public CoreCommand(LuminaryCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("luminarycore.admin")) {
            MessageUtil.send(sender, plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "help":
                sendHelp(sender);
                break;

            case "reload":
                plugin.reload();
                MessageUtil.send(sender, "&aLuminaryCore configuration reloaded!");
                break;

            case "info":
                MessageUtil.send(sender, "&6=== LuminaryCore Info ===");
                MessageUtil.send(sender, "&7Version: &f" + plugin.getDescription().getVersion());
                MessageUtil.send(sender, "&7Maintenance: " + (plugin.isMaintenanceMode() ? "&aEnabled" : "&cDisabled"));
                MessageUtil.send(sender, "&7Config folder: &f" + plugin.getConfigManager().getPluginConfigsFolder().getAbsolutePath());
                break;

            default:
                MessageUtil.send(sender, "&cUnknown subcommand. Use /luminarycore help");
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender) {
        MessageUtil.send(sender, "&6=== LuminaryCore Commands ===");
        MessageUtil.send(sender, "&e/luminarycore reload &7- Reload configuration");
        MessageUtil.send(sender, "&e/luminarycore info &7- Show plugin info");
        MessageUtil.send(sender, "");
        MessageUtil.send(sender, "&6=== Moderation Commands ===");
        MessageUtil.send(sender, "&e/ban <player> [reason] &7- Permanently ban a player");
        MessageUtil.send(sender, "&e/tempban <player> <duration> [reason] &7- Temporarily ban");
        MessageUtil.send(sender, "&e/unban <player> &7- Unban a player");
        MessageUtil.send(sender, "&e/kick <player> [reason] &7- Kick a player");
        MessageUtil.send(sender, "&e/mute <player> [reason] &7- Permanently mute");
        MessageUtil.send(sender, "&e/tempmute <player> <duration> [reason] &7- Temporarily mute");
        MessageUtil.send(sender, "&e/unmute <player> &7- Unmute a player");
        MessageUtil.send(sender, "&e/warn <player> <reason> &7- Warn a player");
        MessageUtil.send(sender, "&e/freeze <player> &7- Freeze/unfreeze a player");
        MessageUtil.send(sender, "&e/history <player> &7- View punishment history");
        MessageUtil.send(sender, "");
        MessageUtil.send(sender, "&6=== Staff Commands ===");
        MessageUtil.send(sender, "&e/vanish [player] &7- Toggle vanish");
        MessageUtil.send(sender, "&e/staffchat [message] &7- Toggle or send to staff chat");
        MessageUtil.send(sender, "&e/broadcast <message> &7- Broadcast to all players");
        MessageUtil.send(sender, "&e/maintenance [on|off] &7- Toggle maintenance mode");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (!sender.hasPermission("luminarycore.admin")) return completions;

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (String option : Arrays.asList("help", "reload", "info")) {
                if (option.startsWith(input)) {
                    completions.add(option);
                }
            }
        }
        return completions;
    }
}
